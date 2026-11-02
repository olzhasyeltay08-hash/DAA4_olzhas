package daa;

import daa.edit.EditDistance;
import daa.edit.RecursiveEditDistance;
import daa.edit.TableEditDistance;
import daa.edit.TwoRowEditDistance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EditDistanceTest {
    private final List<EditDistance> all =
            List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());

    @Test
    void referenceValues() {
        for (EditDistance ed : all) {
            assertEquals(3, ed.distance("kitten", "sitting"), ed.name());
            assertEquals(2, ed.distance("flaw", "lawn"), ed.name());
            assertEquals(5, ed.distance("intention", "execution"), ed.name());
            assertEquals(3, ed.distance("", "abc"), ed.name());
            assertEquals(0, ed.distance("abc", "abc"), ed.name());
        }
    }

    @Test
    void emptyStrings() {
        for (EditDistance ed : all) {
            assertEquals(0, ed.distance("", ""), ed.name());
            assertEquals(3, ed.distance("abc", ""), ed.name());
            assertEquals(3, ed.distance("", "abc"), ed.name());
        }
    }

    @Test
    void symmetric() {
        for (EditDistance ed : all) {
            assertEquals(ed.distance("sunday", "saturday"), ed.distance("saturday", "sunday"), ed.name());
        }
    }

    @Test
    void threeVersionsAgreeOnRandomShortStrings() {
        Random r = new Random(12345);
        EditDistance rec = all.get(0), tab = all.get(1), two = all.get(2);
        for (int t = 0; t < 100; t++) {
            String a = randomString(r, r.nextInt(8)); // length 0..7, includes empty
            String b = randomString(r, r.nextInt(8));
            int expected = tab.distance(a, b);
            assertEquals(expected, rec.distance(a, b), "recursive on '" + a + "','" + b + "'");
            assertEquals(expected, two.distance(a, b), "two rows on '" + a + "','" + b + "'");
        }
    }

    @Test
    void countersAreMeasuredInsideTheAlgorithm() {
        TableEditDistance tab = new TableEditDistance();
        tab.distance("abcd", "xyz");
        assertEquals(4 * 3, tab.work());      // n*m cells
        assertEquals(5 * 4, tab.memCells());  // (n+1)*(m+1)
        TwoRowEditDistance two = new TwoRowEditDistance();
        two.distance("abcd", "xyz");
        assertEquals(4 * 3, two.work());
        assertEquals(2 * 4, two.memCells());  // two rows of (m+1), m = shorter length
        RecursiveEditDistance rec = new RecursiveEditDistance();
        rec.distance("abc", "abc");
        assertEquals(4, rec.work());          // identical strings: n+1 calls
    }

    private static String randomString(Random r, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('a' + r.nextInt(3)));
        return sb.toString();
    }
}
