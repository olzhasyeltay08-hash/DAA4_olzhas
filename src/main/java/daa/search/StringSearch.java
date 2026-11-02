package daa.search;

import java.util.List;

/** One interface for both string search methods. Counters describe the LAST call of search(). */
public interface StringSearch {

    /**
     * Start indices (from 0) of all occurrences of p in t, increasing, overlapping ones included.
     * @throws IllegalArgumentException if p is empty
     */
    List<Integer> search(String t, String p);

    String name();

    /** Number of character comparisons between a text char and a pattern char in the search phase. */
    long comparisons();

    /** Number of character comparisons spent on building the LPS array (0 for naive). */
    long preprocessComparisons();
}
