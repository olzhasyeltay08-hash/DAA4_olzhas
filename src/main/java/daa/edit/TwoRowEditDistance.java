package daa.edit;

/**
 * Version C: the same DP, but only two rows are kept. Time O(n*m), extra memory O(m).
 * (Edit distance is symmetric, so the shorter string is used as the row to keep memory minimal.)
 *
 * DP design:
 *  State:      prev[j] = d[i-1][j],  cur[j] = d[i][j]  (only the previous and current row of the table).
 *  Transition: cur[j] = prev[j-1]                                   if a[i-1] == b[j-1]
 *              cur[j] = 1 + min(prev[j], cur[j-1], prev[j-1])       otherwise
 *              after each row: swap(prev, cur).
 *  Base case:  prev[j] = j for the "row 0";  cur[0] = i at the start of row i.
 *
 * The table of version B is never needed as a whole: cell (i, j) reads only row i-1 and row i.
 */
public final class TwoRowEditDistance implements EditDistance {
    private long cells;
    private long mem;

    @Override
    public int distance(String a, String b) {
        if (b.length() > a.length()) { String t = a; a = b; b = t; } // rows are indexed by the shorter string
        int n = a.length(), m = b.length();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        cells = 0;
        mem = prev.length + cur.length;
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                cells++;
                if (ca == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + Math.min(prev[j], Math.min(cur[j - 1], prev[j - 1]));
                }
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[m];
    }

    @Override public String name() { return "edit_two_rows"; }
    @Override public long work() { return cells; }
    @Override public long memCells() { return mem; }
}
