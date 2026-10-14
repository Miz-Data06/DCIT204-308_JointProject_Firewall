"""Create SVG charts only from measured benchmark aggregate CSV output."""
import csv
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt

ROOT = Path(__file__).parent
rows = []
with (ROOT / "results" / "aggregated_timings.csv").open(newline="", encoding="utf-8") as handle:
    for row in csv.DictReader(handle):
        if row["status"] == "measured":
            rows.append(row)

for family in sorted({row["family"] for row in rows}):
    series = defaultdict(list)
    for row in rows:
        if row["family"] == family:
            series[row["algorithm"]].append((int(row["input_size"]), int(row["mean_nanos"])))
    figure, axis = plt.subplots()
    for algorithm, points in sorted(series.items()):
        points.sort()
        axis.plot([p[0] for p in points], [p[1] for p in points], marker="o", label=algorithm)
    axis.set_title(f"Measured {family} timings")
    axis.set_xlabel("input size (graph: vertices)")
    axis.set_ylabel("mean elapsed nanoseconds")
    axis.grid(True, alpha=0.3)
    axis.legend()
    figure.tight_layout()
    figure.savefig(ROOT / "results" / f"{family}_timings.svg")
    plt.close(figure)
