#!/usr/bin/env python3
"""Reads results/results.csv and draws the plots in results/plots/. Also prints a summary used in REPORT.md."""
import csv, math, os, statistics, sys
from collections import defaultdict
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
CSV = os.path.join(ROOT, "results", "results.csv")
OUT = os.path.join(ROOT, "results", "plots")
os.makedirs(OUT, exist_ok=True)

rows = list(csv.DictReader(open(CSV)))
def num(x): return float(x) if x != "" else None

# (algorithm, input, n) -> dict with median time and counters
agg = defaultdict(lambda: {"t": []})
for r in rows:
    k = (r["algorithm"], r["input"], int(r["n"]))
    d = agg[k]
    d["t"].append(float(r["time_ms"]))
    for c in ("work", "mem_cells", "comparisons", "lps_comparisons", "m", "result"):
        if r[c] != "": d[c] = float(r[c])
for d in agg.values(): d["time"] = statistics.median(d["t"])

def series(alg, inp, field):
    pts = sorted((k[2], v[field]) for k, v in agg.items() if k[0] == alg and k[1] == inp and field in v)
    return [p[0] for p in pts], [p[1] for p in pts]

COL = {"random": "tab:blue", "identical": "tab:green", "disjoint": "tab:red",
       "best_naive": "tab:green", "worst_naive": "tab:red"}
plt.rcParams.update({"figure.figsize": (7.2, 4.6), "axes.grid": True, "grid.alpha": .3, "font.size": 10})

def save(name): plt.tight_layout(); plt.savefig(os.path.join(OUT, name), dpi=140); plt.close()

# P1 recursion explosion
plt.figure()
for inp in ("disjoint", "random", "identical"):
    x, y = series("edit_recursive", inp, "work")
    plt.plot(x, y, "o-", color=COL[inp], label=f"recursive, input '{inp}'")
x, y = series("edit_recursive", "disjoint", "work")
x0, y0 = x[1], y[1]
xs = list(range(x[0], x[-1] + 1))
plt.plot(xs, [y0 * 5.83 ** (v - x0) for v in xs], "k--", lw=1, label=r"reference $\propto 5.83^{n}$")
plt.yscale("log"); plt.xlabel("string length n (characters, both strings)"); plt.ylabel("recursive calls (count, log scale)")
plt.title("P1: Plain recursion makes exponentially many calls"); plt.legend(fontsize=8); save("p1_recursion_calls.png")

# P2 ratio calls / n^2
plt.figure()
for inp in ("disjoint", "random", "identical"):
    x, y = series("edit_recursive", inp, "work")
    plt.plot(x, [c / (n * n) for n, c in zip(x, y)], "o-", color=COL[inp], label=f"input '{inp}'")
plt.axhline(1, color="k", ls="--", lw=1, label="DP table: exactly 1 (n·n cells = n² work)")
plt.yscale("log"); plt.xlabel("string length n (characters)"); plt.ylabel("recursive calls / DP cells (ratio, log scale)")
plt.title("P2: How many times more work the recursion does than the DP table"); plt.legend(fontsize=8); save("p2_calls_over_cells_ratio.png")

# P3 DP time
plt.figure()
for alg, mk, ls in (("edit_table", "s", "-"), ("edit_two_rows", "^", "--")):
    for inp in ("random", "identical", "disjoint"):
        x, y = series(alg, inp, "time")
        plt.plot(x, y, marker=mk, ls=ls, color=COL[inp], label=f"{alg[5:]}, '{inp}'")
x, y = series("edit_table", "random", "time")
plt.plot(x, [y[0] * (v / x[0]) ** 2 for v in x], "k:", lw=1, label=r"reference $\propto n^2$")
plt.xscale("log", base=2); plt.yscale("log")
plt.xlabel("string length n (characters, log scale)"); plt.ylabel("median time (ms, log scale)")
plt.title("P3: Table and two-row DP: time grows like n·m"); plt.legend(fontsize=7, ncol=2); save("p3_dp_time.png")

# P4 memory
plt.figure()
for alg, mk in (("edit_table", "s"), ("edit_two_rows", "^")):
    x, y = series(alg, "random", "mem_cells")
    plt.plot(x, y, marker=mk, label=alg[5:])
plt.xscale("log", base=2); plt.yscale("log")
plt.xlabel("string length n (characters, log scale)"); plt.ylabel("allocated int cells (count, log scale)")
plt.title("P4: Memory of the table vs two rows"); plt.legend(); save("p4_dp_memory.png")

# P5 search comparisons
plt.figure()
for alg, mk, ls in (("search_naive", "s", "-"), ("search_kmp", "^", "--")):
    for inp in ("random", "best_naive", "worst_naive"):
        x, y = series(alg, inp, "comparisons")
        plt.plot(x, y, marker=mk, ls=ls, color=COL[inp], label=f"{alg[7:]}, '{inp}'")
x, _ = series("search_naive", "worst_naive", "comparisons")
plt.plot(x, [0.09 * v * v for v in x], "k:", lw=1, label=r"reference $0.09\,n^2$")
plt.plot(x, [v for v in x], "k-.", lw=1, label=r"reference $n$")
plt.xscale("log", base=2); plt.yscale("log")
plt.xlabel("text length n (characters, pattern m = n/10, log scale)"); plt.ylabel("character comparisons (count, log scale)")
plt.title("P5: Comparisons of naive and KMP"); plt.legend(fontsize=7, ncol=2); save("p5_search_comparisons.png")

# P6 search time
plt.figure()
for alg, mk, ls in (("search_naive", "s", "-"), ("search_kmp", "^", "--")):
    for inp in ("random", "worst_naive"):
        x, y = series(alg, inp, "time")
        plt.plot(x, y, marker=mk, ls=ls, color=COL[inp], label=f"{alg[7:]}, '{inp}'")
plt.xscale("log", base=2); plt.yscale("log")
plt.xlabel("text length n (characters, pattern m = n/10, log scale)"); plt.ylabel("median time (ms, log scale)")
plt.title("P6: Running time of naive and KMP (clock agrees with counters?)"); plt.legend(fontsize=8); save("p6_search_time.png")

# ---------------- summary for the report ----------------
def fmt(v): return f"{v:,.3f}" if v < 100 else f"{v:,.0f}"
print("\n== recursive ==")
for inp in ("random", "identical", "disjoint"):
    x, c = series("edit_recursive", inp, "work"); _, t = series("edit_recursive", inp, "time")
    print(inp, [(a, int(b), round(d, 1)) for a, b, d in zip(x, c, t)])
    print("  growth factors:", [round(c[i + 1] / c[i] / 1.0, 2) for i in range(len(c) - 1)])
print("\n== DP ==")
for alg in ("edit_table", "edit_two_rows"):
    for inp in ("random", "identical", "disjoint"):
        x, t = series(alg, inp, "time"); _, w = series(alg, inp, "work")
        print(alg, inp, [(a, round(b, 2)) for a, b in zip(x, t)], "work==n^2:", all(int(c) == a * a for a, c in zip(x, w)))
print("\n== search ==")
for alg in ("search_naive", "search_kmp"):
    for inp in ("random", "best_naive", "worst_naive"):
        x, c = series(alg, inp, "comparisons"); _, t = series(alg, inp, "time"); _, l = series(alg, inp, "lps_comparisons")
        print(alg, inp)
        print("   comps/n:", [round(b / a, 3) for a, b in zip(x, c)], " time ms:", [round(v, 2) for v in t], " lps:", [int(v) for v in l])
