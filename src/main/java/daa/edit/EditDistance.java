package daa.edit;

/**
 * One interface for all three Edit Distance versions, so that the experiment is one loop.
 * Besides the answer, every version exposes the counters of its LAST call to distance().
 */
public interface EditDistance {

    /** Smallest number of insertions, deletions and replacements that turn a into b. */
    int distance(String a, String b);

    /** Short name used in results.csv. */
    String name();

    /**
     * Unit of work of the last call:
     * recursive = number of calls of the recursive function,
     * table / two-row = number of DP cells computed (cells with i>=1 and j>=1).
     */
    long work();

    /** Number of int cells allocated by the last call (memory counter). 0 for recursive (no arrays). */
    long memCells();
}
