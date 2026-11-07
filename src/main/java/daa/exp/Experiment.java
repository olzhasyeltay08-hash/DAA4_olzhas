package daa.exp;

import daa.edit.*;
import daa.search.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Runs the whole experiment from the Experiment Design Card and writes results.csv
 * (one row per run). Usage: Experiment [output.csv]
 */
public final class Experiment {
    static final String HEADER =
            "algorithm,family,input,n,m,rep,time_ms,work,mem_cells,comparisons,lps_comparisons,result";

    static final int REPS = 5;
    static final int REPS_RECURSIVE = 3;
    /** From this n on a recursive run takes seconds, so it is repeated only once. */
    static final int LONG_RUN_FROM_N = 13;

    static final int[] REC_SIZES = {3, 6, 8, 10, 11, 12, 13, 14};
    static final int[] DP_SIZES = {250, 500, 1000, 2000, 4000};
    static final int[] TWO_ROW_EXTRA = {8000, 16000};
    static final int[] SEARCH_SIZES = {10_000, 20_000, 40_000, 80_000, 160_000};
    /** Largest n for the recursion: found in a trial run (one run <= ~8 s on 'disjoint', ~25 s on 'random' at 14). */
    static int recursionMaxN(String type) { return type.equals("disjoint") ? 13 : 14; }
    static final String[] EDIT_INPUTS = {"random", "identical", "disjoint"};
    static final String[] SEARCH_INPUTS = {"random", "best_naive", "worst_naive"};

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out))) {
            w.println(HEADER);
            warmUp();
            runEdit(w);
            runSearch(w);
        }
        System.out.println("Wrote " + out);
    }

    // ---------------- Edit Distance ----------------

    static void runEdit(PrintWriter w) {
        EditDistance rec = new RecursiveEditDistance();
        EditDistance tab = new TableEditDistance();
        EditDistance two = new TwoRowEditDistance();

        // Recursive (small n only), checked against the table version.
        // The cap per input type is FIXED (not time-based), so the same command always gives the same rows.
        for (String type : EDIT_INPUTS) {
            for (int n : REC_SIZES) {
                if (n > recursionMaxN(type)) break;
                String[] p = Gen.editPair(type, n);
                int expected = tab.distance(p[0], p[1]);
                int reps = n >= LONG_RUN_FROM_N ? 1 : REPS_RECURSIVE;   // runs of many seconds are done once
                double worst = 0;
                for (int rep = 1; rep <= reps; rep++) {
                    double ms = timeEdit(rec, p, expected);
                    worst = Math.max(worst, ms);
                    editRow(w, rec, type, n, rep, ms, expected);
                }
                System.out.printf(Locale.ROOT, "%s %s n=%d calls=%d worst=%.1f ms%n", rec.name(), type, n, rec.work(), worst);
            }
        }

        // Table and two rows.
        for (String type : EDIT_INPUTS) {
            for (int n : DP_SIZES) {
                String[] p = Gen.editPair(type, n);
                int expected = two.distance(p[0], p[1]);
                for (int rep = 1; rep <= REPS; rep++) {
                    editRow(w, tab, type, n, rep, timeEdit(tab, p, expected), expected);
                }
                for (int rep = 1; rep <= REPS; rep++) {
                    editRow(w, two, type, n, rep, timeEdit(two, p, expected), expected);
                }
                System.out.printf(Locale.ROOT, "dp %s n=%d d=%d%n", type, n, expected);
            }
            for (int n : TWO_ROW_EXTRA) {
                String[] p = Gen.editPair(type, n);
                int expected = two.distance(p[0], p[1]);
                for (int rep = 1; rep <= REPS; rep++) {
                    editRow(w, two, type, n, rep, timeEdit(two, p, expected), expected);
                }
                System.out.printf(Locale.ROOT, "two_rows %s n=%d d=%d%n", type, n, expected);
            }
        }
    }

    static double timeEdit(EditDistance ed, String[] p, int expected) {
        long t0 = System.nanoTime();
        int d = ed.distance(p[0], p[1]);
        double ms = (System.nanoTime() - t0) / 1e6;
        if (d != expected) throw new IllegalStateException(ed.name() + " disagrees: " + d + " vs " + expected);
        return ms;
    }

    static void editRow(PrintWriter w, EditDistance ed, String type, int n, int rep, double ms, int result) {
        w.printf(Locale.ROOT, "%s,edit_distance,%s,%d,%d,%d,%.4f,%d,%d,,,%d%n",
                ed.name(), type, n, n, rep, ms, ed.work(), ed.memCells(), result);
    }

    // ---------------- String search ----------------

    static void runSearch(PrintWriter w) {
        List<StringSearch> algos = List.of(new NaiveSearch(), new KmpSearch());
        for (String type : SEARCH_INPUTS) {
            for (int n : SEARCH_SIZES) {
                int m = n / 10;
                String[] p = Gen.searchPair(type, n, m);
                List<Integer> expected = null;
                for (StringSearch s : algos) {
                    for (int rep = 1; rep <= REPS; rep++) {
                        long t0 = System.nanoTime();
                        List<Integer> res = s.search(p[0], p[1]);
                        double ms = (System.nanoTime() - t0) / 1e6;
                        if (expected == null) expected = res;
                        else if (!expected.equals(res)) throw new IllegalStateException("search results differ");
                        w.printf(Locale.ROOT, "%s,string_search,%s,%d,%d,%d,%.4f,,,%d,%d,%d%n",
                                s.name(), type, n, m, rep, ms, s.comparisons(), s.preprocessComparisons(), res.size());
                    }
                }
                System.out.printf(Locale.ROOT, "search %s n=%d m=%d matches=%d%n", type, n, m, expected.size());
            }
        }
    }

    // ---------------- JIT warm-up (results are thrown away) ----------------

    static void warmUp() {
        EditDistance[] eds = {new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance()};
        String[] p = Gen.editPair("random", 9);
        String[] q = Gen.editPair("random", 300);
        for (int i = 0; i < 20; i++) {
            eds[0].distance(p[0], p[1]);
            eds[1].distance(q[0], q[1]);
            eds[2].distance(q[0], q[1]);
        }
        String[] s = Gen.searchPair("random", 20_000, 2000);
        StringSearch[] ss = {new NaiveSearch(), new KmpSearch()};
        for (int i = 0; i < 20; i++) for (StringSearch x : ss) x.search(s[0], s[1]);
    }
}
