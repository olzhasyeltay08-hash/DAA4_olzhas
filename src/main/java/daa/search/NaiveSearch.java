package daa.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Naive search: try every start position s, compare t[s+j] with p[j] until a mismatch or a full match.
 * Worst case O((n-m+1)*m) comparisons, e.g. t = "AAAA...A", p = "AAA...AB".
 * Counter: every comparison of one text char with one pattern char.
 */
public final class NaiveSearch implements StringSearch {
    private long comparisons;

    @Override
    public List<Integer> search(String t, String p) {
        if (p == null || p.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;
        List<Integer> res = new ArrayList<>();
        int n = t.length(), m = p.length();
        for (int s = 0; s + m <= n; s++) {
            int j = 0;
            while (j < m) {
                comparisons++;
                if (t.charAt(s + j) != p.charAt(j)) break;
                j++;
            }
            if (j == m) res.add(s);
        }
        return res;
    }

    @Override public String name() { return "search_naive"; }
    @Override public long comparisons() { return comparisons; }
    @Override public long preprocessComparisons() { return 0; }
}
