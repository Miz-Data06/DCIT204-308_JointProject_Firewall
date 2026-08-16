const counts = [
  { label: "Locations", value: 150 },
  { label: "Roads", value: 300 },
  { label: "Service requests", value: 900 },
  { label: "Riders/resources", value: 90 },
  { label: "Algorithm runs", value: 60 },
  { label: "Audit events", value: 0 }
];

const datasets = [
  {
    file: "locations.csv",
    rows: 150,
    purpose: "Delivery network places such as restaurants, campuses, homes, markets, landmarks, and junctions.",
    fields: ["location_id", "name", "type", "latitude", "longitude"],
    support: "Provides graph vertices and delivery endpoints."
  },
  {
    file: "roads.csv",
    rows: 300,
    purpose: "Road connections between locations with distance, base travel time, condition label, and multiplier.",
    fields: ["road_id", "from_location_id", "to_location_id", "road_condition_multiplier"],
    support: "Provides weighted graph edges for routing and MST algorithms."
  },
  {
    file: "service_requests.csv",
    rows: 900,
    purpose: "Food delivery requests with source, destination, urgency, deadline, status, and capacity.",
    fields: ["request_id", "source_location_id", "destination_location_id", "urgency", "status"],
    support: "Supports searching, sorting, priority scoring, and request selection."
  },
  {
    file: "resources.csv",
    rows: 90,
    purpose: "Rider/resource records with vehicle type, home location, capacity, and availability.",
    fields: ["resource_id", "type", "home_location_id", "capacity", "availability_status"],
    support: "Supports rider assignment and dispatch decisions."
  },
  {
    file: "algorithm_runs.csv",
    rows: 60,
    purpose: "Performance rows filtered to implemented project algorithms.",
    fields: ["run_id", "algorithm_name", "input_size", "runtime_ns", "memory_kb"],
    support: "Supports performance graphs and analysis."
  },
  {
    file: "audit_events.csv",
    rows: 0,
    purpose: "Official audit event schema with no seed rows.",
    fields: ["audit_id", "entity_type", "entity_id", "action", "performed_by"],
    support: "Supports runtime audit events without inventing operational history."
  }
];

const databaseTables = [
  ["locations", "location_id", "Stores named places and coordinates used by graph vertices."],
  ["roads", "road_id", "References from_location_id and to_location_id. Stores travel weights."],
  ["service_requests", "request_id", "References source_location_id and destination_location_id."],
  ["resources", "resource_id", "References home_location_id for rider base/current location."],
  ["algorithm_runs", "run_id", "Stores measured experiment/performance records."],
  ["audit_events", "audit_id", "Stores runtime audit events. Uses SYSTEM or app/user ids for performer values."]
];

const structures = [
  ["Dynamic array", "Stores resizable collections of locations, roads, requests, and riders after CSV loading."],
  ["Linked list", "Supports ordered traversal where insert/remove behavior is useful for coursework structure coverage."],
  ["Iterator", "Provides controlled traversal over custom collections without exposing internals."],
  ["Stack", "Can model undo-style operations or last-in-first-out trace review."],
  ["Queue", "Supports first-in-first-out request processing and BFS traversal behavior."],
  ["Circular queue", "Useful for rotating rider/resource availability checks."],
  ["Deque", "Allows double-ended request or route-step processing."],
  ["Priority heap", "Supports highest-priority dispatch decisions and efficient priority access."],
  ["BST", "Supports ordered lookup examples over comparable delivery data."],
  ["Red-black tree", "Provides balanced ordered storage for stable search performance."],
  ["B-tree", "Models multiway indexed storage for larger searchable records."],
  ["Hash table", "Supports fast id-based access such as location id or request id lookup."],
  ["Map", "Maps names to ids, such as rider home location name to location id."],
  ["Set", "Tracks visited locations and selected vertices without duplicates."],
  ["Adjacency list graph", "Represents the sparse delivery road network for traversal, Dijkstra, and MST."],
  ["Adjacency matrix graph", "Alternative graph representation useful for dense graph comparisons."],
  ["Disjoint set", "Detects cycles while Kruskal accepts roads for the minimum spanning tree."]
];

const algorithms = [
  ["Linear search", "Scans requests or resources when the data is not sorted."],
  ["Binary search", "Finds sorted records quickly after ordering by id."],
  ["Selection sort", "Demonstrates simple in-place sorting for small request lists."],
  ["Insertion sort", "Orders small or nearly sorted delivery data."],
  ["Merge sort", "Sorts request records predictably with divide-and-conquer behavior."],
  ["Quicksort", "Sorts request priority demos and shows the top request by priority."],
  ["BFS", "Visits nearby locations level by level from a start location."],
  ["DFS", "Explores the road network deeply from a start location."],
  ["Dijkstra", "Finds fastest route by effective travel time with road condition multipliers."],
  ["Prim", "Builds a low-cost connected road backbone from a chosen start location."],
  ["Kruskal", "Builds an MST by sorting roads and using disjoint set cycle checks."],
  ["Brute force request selection", "Checks all combinations for small request-selection cases."],
  ["Greedy rider assignment", "Chooses a feasible rider based on local pickup-time criteria."],
  ["Dynamic programming knapsack", "Selects requests under rider capacity using scaled integer DP units."],
  ["Index-derived priority scoring", "Combines urgency, deadline pressure, and waiting time using approved weights."]
];

function renderCounts() {
  const max = Math.max(...counts.map((item) => item.value));
  const container = document.querySelector("#countBars");
  container.innerHTML = counts.map((item) => {
    const width = max === 0 ? 0 : Math.max(2, (item.value / max) * 100);
    const note = item.label === "Audit events" ? "0 official seed rows, generated during runtime" : `${item.value}`;
    return `
      <div class="bar-row">
        <span class="bar-label">${item.label}</span>
        <span class="bar-track"><span class="bar-fill" style="width:${width}%"></span></span>
        <span class="bar-value">${note}</span>
      </div>
    `;
  }).join("");
}

function renderDatasets() {
  const container = document.querySelector("#datasetGrid");
  container.innerHTML = datasets.map((dataset) => `
    <article class="card dataset-card">
      <h3>${dataset.file}</h3>
      <p><strong>${dataset.rows}</strong> rows</p>
      <p>${dataset.purpose}</p>
      <div class="tag-list">${dataset.fields.map((field) => `<span class="tag">${field}</span>`).join("")}</div>
      <p>${dataset.support}</p>
    </article>
  `).join("");
}

function renderDatabase() {
  const body = document.querySelector("#databaseRows");
  body.innerHTML = databaseTables.map(([table, key, role]) => `
    <tr>
      <td><strong>${table}</strong></td>
      <td>${key}</td>
      <td>${role} JDBC repositories use prepared statements for inserts and reads.</td>
    </tr>
  `).join("");
}

function renderCards(targetId, items) {
  const target = document.querySelector(targetId);
  target.innerHTML = items.map(([name, text]) => `
    <article class="card item-card" data-search="${`${name} ${text}`.toLowerCase()}">
      <h3>${name}</h3>
      <p>${text}</p>
    </article>
  `).join("");
}

function wireFilter(inputId, gridId) {
  const input = document.querySelector(inputId);
  const grid = document.querySelector(gridId);
  input.addEventListener("input", () => {
    const term = input.value.trim().toLowerCase();
    grid.querySelectorAll(".item-card").forEach((card) => {
      card.classList.toggle("is-hidden", term && !card.dataset.search.includes(term));
    });
  });
}

function wireNav() {
  const links = [...document.querySelectorAll(".nav-link")];
  const sections = links.map((link) => document.querySelector(link.getAttribute("href")));
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        links.forEach((link) => link.classList.remove("active"));
        const active = links.find((link) => link.getAttribute("href") === `#${entry.target.id}`);
        if (active) active.classList.add("active");
      }
    });
  }, { rootMargin: "-30% 0px -60% 0px" });
  sections.forEach((section) => observer.observe(section));
}

renderCounts();
renderDatasets();
renderDatabase();
renderCards("#structureGrid", structures);
renderCards("#algorithmGrid", algorithms);
wireFilter("#structureSearch", "#structureGrid");
wireFilter("#algorithmSearch", "#algorithmGrid");
wireNav();
