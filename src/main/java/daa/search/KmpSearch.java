package daa.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt search.
 *
 * lps[i] = length of the longest proper prefix of p[0..i] that is also a suffix of p[0..i].
 * When a mismatch happens after j matched chars, the pattern pointer falls back to lps[j-1],
 * while the text pointer i NEVER moves backwards: every text char is read once forward,
 * and every extra comparison on a mismatch lowers j, so the total is at most 2n comparisons.
 */
public final class KmpSearch implements StringSearch {
    private long comparisons;
    private long lpsComparisons;

    /** Public so that tests can call it directly. */
    public static int[] lps(String p) {
        return build(p, null);
    }

    private static int[] build(String p, long[] counter) {
        int m = p.length();
        int[] lps = new int[m];
        int len = 0;      // length of the current matched prefix-suffix
        int i = 1;
        while (i < m) {
            if (counter != null) counter[0]++;
            if (p.charAt(i) == p.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];   // fall back, do not advance i
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String t, String p) {
        if (p == null || p.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;
        long[] c = new long[1];
        int[] lps = build(p, c);
        lpsComparisons = c[0];
        List<Integer> res = new ArrayList<>();
        int n = t.length(), m = p.length();
        if (m > n) return res;
        int j = 0;                                   // chars of p matched so far
        for (int i = 0; i < n; i++) {                // i only grows
            while (true) {
                comparisons++;
                if (t.charAt(i) == p.charAt(j)) { j++; break; }
                if (j == 0) break;
                j = lps[j - 1];
            }
            if (j == m) {
                res.add(i - m + 1);
                j = lps[j - 1];                      // allows overlapping matches
            }
        }
        return res;
    }

    @Override public String name() { return "search_kmp"; }
    @Override public long comparisons() { return comparisons; }
    @Override public long preprocessComparisons() { return lpsComparisons; }
}
