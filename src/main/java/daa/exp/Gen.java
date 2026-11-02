package daa.exp;

import java.util.Random;

/** Reproducible input generator: the same (type, n) always gives the same strings (fixed seeds). */
public final class Gen {
    public static final long BASE_SEED = 2026L;
    private Gen() {}

    public static long seed(int n, int typeId) { return BASE_SEED + 31L * n + typeId; }

    public static String random(long seed, int len, String alphabet) {
        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }

    /** Pair of strings of length n for Edit Distance. type: random | identical | disjoint. */
    public static String[] editPair(String type, int n) {
        switch (type) {
            case "random": {
                long s = seed(n, 1);
                return new String[]{random(s, n, "abcdefghijklmnopqrstuvwxyz"),
                                    random(s + 1, n, "abcdefghijklmnopqrstuvwxyz")};
            }
            case "identical": {
                String a = random(seed(n, 2), n, "abcdefghijklmnopqrstuvwxyz");
                return new String[]{a, a};
            }
            case "disjoint": {
                long s = seed(n, 3);
                return new String[]{random(s, n, "abcdefghijklm"), random(s + 1, n, "nopqrstuvwxyz")};
            }
            default: throw new IllegalArgumentException(type);
        }
    }

    /** {text, pattern} for string search, pattern length m. type: random | best_naive | worst_naive. */
    public static String[] searchPair(String type, int n, int m) {
        switch (type) {
            case "random": {
                long s = seed(n, 4);
                return new String[]{random(s, n, "ACGT"), random(s + 1, m, "ACGT")};
            }
            case "best_naive":   // pattern's first char never occurs in the text
                return new String[]{"A".repeat(n), "C" + "A".repeat(m - 1)};
            case "worst_naive":  // naive compares m chars at each start and fails on the last one
                return new String[]{"A".repeat(n), "A".repeat(m - 1) + "C"};
            default: throw new IllegalArgumentException(type);
        }
    }
}
