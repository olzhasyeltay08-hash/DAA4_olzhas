package daa.edit;

/**
 * Version A: plain recursion, no memory. Slow on purpose.
 *
 * DP design:
 *  State:      f(i, j) = edit distance between the first i chars of a and the first j chars of b.
 *  Transition: if a[i-1] == b[j-1]: f(i, j) = f(i-1, j-1)
 *              else: f(i, j) = 1 + min( f(i-1, j)    // delete a[i-1]
 *                                     , f(i, j-1)    // insert b[j-1]
 *                                     , f(i-1, j-1)) // replace a[i-1] by b[j-1]
 *  Base case:  f(0, j) = j  and  f(i, 0) = i   (so empty strings work).
 *
 * The same pair (i, j) is solved again and again, so the number of calls grows exponentially
 * (about 5.8^n for two strings of length n with no equal characters).
 */
public final class RecursiveEditDistance implements EditDistance {
    private long calls;

    @Override
    public int distance(String a, String b) {
        calls = 0;
        return f(a, b, a.length(), b.length());
    }

    private int f(String a, String b, int i, int j) {
        calls++;
        if (i == 0) return j;
        if (j == 0) return i;
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return f(a, b, i - 1, j - 1);
        }
        int del = f(a, b, i - 1, j);
        int ins = f(a, b, i, j - 1);
        int rep = f(a, b, i - 1, j - 1);
        return 1 + Math.min(del, Math.min(ins, rep));
    }

    @Override public String name() { return "edit_recursive"; }
    @Override public long work() { return calls; }
    @Override public long memCells() { return 0; }
}
