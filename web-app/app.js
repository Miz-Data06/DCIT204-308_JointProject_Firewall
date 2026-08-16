const state = {
  locations: [],
  restaurants: []
};

const byId = (id) => document.getElementById(id);

async function api(path, options) {
  const response = await fetch(path, options);
  const text = await response.text();
  let body;
  try {
    body = JSON.parse(text);
  } catch {
    body = { ok: false, error: text || "Invalid server response" };
  }
  if (!response.ok || body.ok === false) {
    throw new Error(body.error || `Request failed with status ${response.status}`);
  }
  return body;
}

function locationName(id) {
  const found = state.locations.find((location) => location.id === id);
  return found ? `${found.name} (${found.id})` : id;
}

function renderOptions(select, items) {
  select.innerHTML = items.map((item) => (
    `<option value="${escapeHtml(item.id)}">${escapeHtml(item.name)} - ${escapeHtml(item.id)}</option>`
  )).join("");
}

function renderCounts(counts) {
  const labels = [
    ["locations", "Locations"],
    ["roads", "Roads"],
    ["requests", "Requests"],
    ["riders", "Riders"],
    ["algorithmRuns", "Algorithm runs"],
    ["auditEvents", "Audit events"]
  ];
  byId("countsGrid").innerHTML = labels.map(([key, label]) => `
    <div class="count">
      <span>${label}</span>
      <strong>${counts[key]}</strong>
    </div>
  `).join("");
}

function renderRecent(orders) {
  byId("recentOrders").innerHTML = orders.map((order) => `
    <tr>
      <td><strong>${escapeHtml(order.requestId)}</strong><br>${escapeHtml(order.category)}</td>
      <td>${escapeHtml(order.sourceLocationId)} -> ${escapeHtml(order.destinationLocationId)}</td>
      <td>${escapeHtml(order.status)}</td>
      <td>${Number(order.priorityScore).toFixed(6)}</td>
    </tr>
  `).join("");
}

function renderConfirmation(data) {
  const order = data.order;
  const route = data.route;
  const assignment = data.assignment;
  byId("confirmation").className = "confirmation-card";
  byId("confirmation").innerHTML = `
    <div>
      <span class="big">${escapeHtml(order.requestId)}</span>
      <p>${escapeHtml(order.status)} order persisted through SQLite/JDBC.</p>
    </div>
    <div class="detail-grid">
      <div class="detail"><span>Priority score</span><strong>${Number(order.priorityScore).toFixed(6)}</strong></div>
      <div class="detail"><span>Effective time</span><strong>${Number(route.totalEffectiveTime).toFixed(2)}</strong></div>
      <div class="detail"><span>Distance km</span><strong>${Number(route.totalDistanceKm).toFixed(2)}</strong></div>
    </div>
    <div class="detail">
      <span>Fastest route</span>
      <strong>${route.path.map(escapeHtml).join(" -> ") || "No reachable route"}</strong>
    </div>
    <div class="detail">
      <span>Rider assignment</span>
      <strong>${assignment.assigned
        ? `${escapeHtml(assignment.riderId)} (${escapeHtml(assignment.vehicleType)}), pickup time ${Number(assignment.pickupEffectiveTime).toFixed(2)}`
        : escapeHtml(assignment.message)}</strong>
    </div>
  `;
}

async function loadInitialData() {
  const health = await api("/api/health");
  byId("healthStatus").textContent = health.status;
  byId("healthStatus").className = "status-pill ok";

  const [locations, restaurants, counts, recent] = await Promise.all([
    api("/api/locations"),
    api("/api/restaurants"),
    api("/api/counts"),
    api("/api/recent-orders")
  ]);
  state.locations = locations;
  state.restaurants = restaurants;
  renderOptions(byId("sourceLocationId"), restaurants);
  renderOptions(byId("destinationLocationId"), locations);
  if (locations.length > 1) {
    byId("destinationLocationId").selectedIndex = 1;
  }
  renderCounts(counts);
  renderRecent(recent);
}

async function refreshAfterOrder() {
  const [counts, recent] = await Promise.all([
    api("/api/counts"),
    api("/api/recent-orders")
  ]);
  renderCounts(counts);
  renderRecent(recent);
}

function formPayload(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

byId("orderForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const button = byId("submitOrder");
  const message = byId("message");
  button.disabled = true;
  message.className = "message";
  message.textContent = "Placing order and running route/assignment logic...";
  try {
    const data = await api("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(formPayload(event.currentTarget))
    });
    renderConfirmation(data);
    await refreshAfterOrder();
    message.textContent = `Order ${data.order.requestId} placed from ${locationName(data.order.sourceLocationId)}.`;
  } catch (error) {
    message.className = "message error";
    message.textContent = error.message;
  } finally {
    button.disabled = false;
  }
});

loadInitialData().catch((error) => {
  byId("healthStatus").textContent = "offline";
  byId("healthStatus").className = "status-pill error";
  byId("message").className = "message error";
  byId("message").textContent = error.message;
});
