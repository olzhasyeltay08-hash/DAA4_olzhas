package daa;

import daa.search.KmpSearch;
import daa.search.NaiveSearch;
import daa.search.StringSearch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SearchTest {
    private final List<StringSearch> all = List.of(new NaiveSearch(), new KmpSearch());

    @Test
    void lpsReferenceArrays() {
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, KmpSearch.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, KmpSearch.lps("ABABCABAB"));
        assertArrayEquals(new int[]{0}, KmpSearch.lps("A"));
    }

    @Test
    void referenceMatches() {
        for (StringSearch s : all) {
            assertEquals(List.of(10), s.search("ABABDABACDABABCABAB", "ABABCABAB"), s.name());
            assertEquals(List.of(0, 1, 2, 3), s.search("AAAAA", "AA"), s.name());
            assertEquals(List.of(0, 2, 4), s.search("ABABABA", "ABA"), s.name());
        }
    }

    @Test
    void noMatchGivesEmptyList() {
        for (StringSearch s : all) {
            assertEquals(List.of(), s.search("ABCDEF", "XYZ"), s.name());
            assertEquals(List.of(), s.search("", "A"), s.name());
        }
    }

    @Test
    void emptyPatternThrows() {
        for (StringSearch s : all) {
            assertThrows(IllegalArgumentException.class, () -> s.search("ABC", ""), s.name());
            assertThrows(IllegalArgumentException.class, () -> s.search("", ""), s.name());
        }
    }

    @Test
    void patternLongerThanTextGivesEmptyList() {
        for (StringSearch s : all) {
            assertEquals(List.of(), s.search("AB", "ABC"), s.name());
        }
    }

    @Test
    void patternEqualToText() {
        for (StringSearch s : all) {
            assertEquals(List.of(0), s.search("ABC", "ABC"), s.name());
        }
    }

    @Test
    void naiveAndKmpAgreeOnRandomTexts() {
        Random r = new Random(2026);
        NaiveSearch naive = new NaiveSearch();
        KmpSearch kmp = new KmpSearch();
        for (int t = 0; t < 100; t++) {
            String text = randomString(r, 1 + r.nextInt(200), 2);   // small alphabet => many overlaps
            String pat = randomString(r, 1 + r.nextInt(6), 2);
            assertEquals(naive.search(text, pat), kmp.search(text, pat), "text=" + text + " pat=" + pat);
        }
    }

    @Test
    void kmpUsesAtMostTwoNComparisons() {
        KmpSearch kmp = new KmpSearch();
        String text = "A".repeat(1000);
        kmp.search(text, "A".repeat(99) + "B");
        assertTrue(kmp.comparisons() <= 2L * text.length(), "comparisons=" + kmp.comparisons());
        NaiveSearch naive = new NaiveSearch();
        naive.search(text, "A".repeat(99) + "B");
        assertEquals((1000L - 100 + 1) * 100, naive.comparisons());  // measured worst case of naive
    }

    private static String randomString(Random r, int len, int alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('A' + r.nextInt(alphabet)));
        return sb.toString();
    }
}
