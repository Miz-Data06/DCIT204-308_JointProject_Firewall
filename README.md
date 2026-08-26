# DCIT204-308_JointProject_FoodDelivery

A Java-based Ghana-localized service operations platform with custom data structures, graph algorithms, and JDBC database integration - DCIT 204/308 joint semester project.

## Run the GUI Demo

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.gui.FoodDeliveryGuiApp"
```

The Swing GUI is the visual presentation demo. It loads the project dataset, lets the team place a delivery request, shows the fastest route/result with a route diagram, attempts rider/resource assignment, and displays simple system summary counts.

## Console Fallback

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.Main"
```

## Documentation

Final project documentation is available under [`docs/final/`](docs/final/).
