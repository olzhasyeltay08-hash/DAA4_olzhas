# DAA Assignment 4 – City Search & Spell-Check (DP and String Algorithms)

Edit Distance in three versions (recursive, table, two rows), Naive and KMP string search, counters,
a seeded experiment, JUnit 5 tests, plots and a report.

## Layout
```
src/main/java/daa/edit     EditDistance (interface), Recursive/Table/TwoRow versions
src/main/java/daa/search   StringSearch (interface), NaiveSearch, KmpSearch (public static int[] lps(String))
src/main/java/daa/exp      Gen (seeded generator), Experiment (writes results/results.csv)
src/test/java/daa          JUnit 5 tests
scripts/plot.py            draws results/plots/*.png from results/results.csv
results/results.csv        all runs (one row per run)
results/plots/             6 PNG plots
REPORT.md                  Experiment Design Card, plots, prediction vs result, short answers
```

## Requirements
JDK 17+ and Maven 3.8+ (tests), Python 3 with `matplotlib` (plots only).

## Build and test
```
mvn test
```

## Run the experiment (about 1.5–2 minutes, needs ~1.5 GB heap for n = 4000 tables)
```
mvn -q compile exec:java -Dexec.args="results/results.csv" -DargLine="-Xmx3g"
# or without exec plugin:
mvn -q compile && java -Xmx3g -cp target/classes daa.exp.Experiment results/results.csv
python3 scripts/plot.py        # draws the plots and prints a summary
```
The generator uses the fixed seed `2026`, so the inputs and all counters are identical on every run;
only the times change a little from machine to machine.

## results.csv columns
`algorithm, family, input, n, m, rep, time_ms, work, mem_cells, comparisons, lps_comparisons, result`
* `work`: recursive calls (recursion) or DP cells computed (table, two rows)
* `mem_cells`: allocated int cells
* `comparisons`, `lps_comparisons`: text/pattern char comparisons in search / in LPS building
* `result`: edit distance, or number of matches

## Git
Work on `main`, final version tagged `v1.0`.

## AI use acknowledgement
An AI assistant (Claude, Anthropic) was used to generate the first version of the code, the tests, the
experiment runner, the plotting script and the draft of the report. The student reviewed and is responsible
for the submitted work. The numbers in the report come from running the code in `results/results.csv`.
