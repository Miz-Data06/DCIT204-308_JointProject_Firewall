# Working Food Delivery Web Demo

This is an optional browser-based demo for the Food Delivery DSA project. It does not replace the official Java console application.

The web demo uses:

- Java 17 built-in `com.sun.net.httpserver.HttpServer`.
- Existing CSV dataset loader.
- Existing SQLite/JDBC database layer.
- Existing Dijkstra fastest-route implementation.
- Existing index-derived priority scoring.
- Existing greedy rider assignment implementation.
- Plain HTML, CSS, and JavaScript with no CDN or npm dependencies.

## Run

From the project root:

```powershell
mvn package
mvn org.codehaus.mojo:exec-maven-plugin:3.3.0:java -Dexec.mainClass=com.fooddelivery.web.WebDemoServer
```

Then open:

```text
http://localhost:8080
```

Optional custom port:

```powershell
mvn org.codehaus.mojo:exec-maven-plugin:3.3.0:java -Dexec.mainClass=com.fooddelivery.web.WebDemoServer -Dexec.args="8090"
```

## Place a Test Order

1. Choose a restaurant/source.
2. Choose a destination.
3. Keep category as `Food Delivery` or enter another non-empty category.
4. Choose urgency.
5. Enter a positive capacity.
6. Enter a positive deadline in minutes.
7. Click **Place Order**.

The confirmation panel should show:

- Generated `WEB###` request ID.
- Priority score.
- Persisted status.
- Fastest route path.
- Total effective travel time.
- Rider assignment result when an eligible rider is available.

## Notes

- New web orders are persisted to `target/food_delivery.db`.
- The server imports the official dataset on startup and keeps any existing `WEB###` orders.
- The console app remains the required runnable demo for assessment.
