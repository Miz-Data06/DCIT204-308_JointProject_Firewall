# Archived Historical Project Report Draft

This file was retained for historical traceability. The active final report source is `docs/final/submission/Food_Delivery_System_Final_Report.md`.

# Food Delivery System - Project Report Draft

## Title Page Information

**Project title:** Food Delivery System
**Course context:** DCIT 204/308 Joint Semester Project
**Team name:** Firewall
**Repository:** https://github.com/Miz-Data06/DCIT204-308_JointProject_Firewall.git
**Package name:** `com.fooddelivery`
**Main language and tools:** Java 17, Maven, JUnit 5, SQLite JDBC
**Current working branch:** `main`
**Current HEAD:** `bf1156a`

Project brief and handoff files were not found locally apart from the README. Where a required detail was not available in the provided local materials, this report marks it as **Not specified in provided brief/handoff**.

## Project Overview

The Food Delivery System is a Java-based service operations platform for handling delivery locations, roads, riders, delivery requests, database import, and application-driven demonstrations. It combines custom data structures, graph algorithms, request optimization algorithms, CSV dataset loading, a SQLite JDBC database layer, a Swing GUI application workflow, and a console fallback workflow.

The final system loads the dataset, imports it into SQLite, builds a custom weighted road graph, and runs demonstrations through the Java Swing application and menu-driven console path. The interactive flow covers searching, sorting, BFS/DFS traversal, Dijkstra fastest routing, Prim and Kruskal MST, request selection, greedy rider assignment, and priority scoring.

## Problem Statement

Food delivery operations need to manage service locations, roads, requests, riders, and dispatch decisions. The problem is to design and implement a coursework-scale system that models these items, stores them, and demonstrates core data structures and algorithms using real dataset-backed examples.

## System Objectives

1. Implement custom data structures instead of using Java collection substitutes for core work.
2. Implement required searching, sorting, graph, MST, and request optimization algorithms.
3. Load and normalize the provided CSV datasets.
4. Store normalized data in a JDBC database.
5. Build a delivery road graph from real locations and roads.
6. Run dataset-backed algorithm demonstrations from a console menu.
7. Provide tests and evidence showing the system works end to end.

## Food Delivery Context

The system represents a delivery network as locations connected by roads. Roads have normal travel time and road-condition weights, so routing and network algorithms use effective travel time:

`baseTravelTimeMinutes * roadConditionMultiplier`

Delivery requests include source and destination locations, urgency, status, submission time, deadline, and priority. Riders have a current location, vehicle type, carrying capacity, and availability.

## Dataset Description

| File | Rows | Purpose |
|---|---:|---|
| `locations.csv` | 150 | Delivery locations and coordinates |
| `roads.csv` | 300 | Road connections, road condition labels, and travel multipliers |
| `service_requests.csv` | 900 | Delivery request records |
| `resources.csv` | 90 | Rider/resource records |
| `algorithm_runs.csv` | 60 | Filtered performance support rows for implemented algorithms |
| `audit_events.csv` | 0 | Header-only audit support file; no app-generated audit history was invented |

The four core CSVs loaded into models are locations, roads, service requests, and resources. The two audit/experiment CSV files are documented as support data and were not required for the main console workflow.

## Data Cleaning and Mapping Decisions

- `locations.csv` maps directly to `Location`.
- `roads.csv` contains stable road IDs as `ROAD001`, `ROAD002`, and so on.
- `distance` maps to `distanceKm`.
- `base_travel_time_minutes` maps to normal travel time.
- `road_condition_multiplier` maps to the road condition multiplier used by routing and MST algorithms.
- `Low`, `Medium`, and `High` urgency values map to `1`, `2`, and `3`.
- Request times use the fixed synthetic date `2026-01-01`. If a deadline time is earlier than submitted time, it is treated as the next day.
- `In Transit` maps to `RequestStatus.PICKED_UP`.
- Request capacity defaults to `1.0` because the CSV does not include capacity and category values do not imply size.
- Priority score is computed deterministically using existing priority scoring and normalized urgency/deadline/waiting proxies.
- `resources.csv` maps `resource_id` to `riderId`.
- Rider names are generated as `Rider {resource_Id}`.
- `homeLocation` is a location name and is resolved to `locationId`.
- `Available` maps to `true`; `In Use` and `Under Maintenance` map to `false`.

## System Architecture

| Package | Responsibility |
|---|---|
| `com.fooddelivery.model` | Core domain models and enums |
| `com.fooddelivery.datastructures` | Custom linear, queue, graph, hash, heap, and tree structures |
| `com.fooddelivery.algorithms` | Searching, sorting, graph, MST, optimization, and priority algorithms |
