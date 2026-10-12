import java.util.Comparator;

/** Natural deterministic ordering for identifiers such as A2, A10, WR-2 and WR-10. */
public final class IdOrder {
    private IdOrder() { }
    public static final Comparator<String> NATURAL = (left, right) -> {
        String a = left == null ? "" : left;
        String b = right == null ? "" : right;
        int ai = suffixNumber(a), bi = suffixNumber(b);
        String ap = prefix(a), bp = prefix(b);
        int prefixCompare = ap.compareTo(bp);
        if (prefixCompare != 0) return prefixCompare;
        if (ai >= 0 && bi >= 0) {
            int numberCompare = Integer.compare(ai, bi);
            if (numberCompare != 0) return numberCompare;
        }
        return a.compareTo(b);
    };
    private static String prefix(String value) {
        int i = value.length();
        while (i > 0 && Character.isDigit(value.charAt(i - 1))) i--;
        return value.substring(0, i);
    }
    private static int suffixNumber(String value) {
        int i = value.length();
        while (i > 0 && Character.isDigit(value.charAt(i - 1))) i--;
        if (i == value.length()) return -1;
        try { return Integer.parseInt(value.substring(i)); }
        catch (NumberFormatException ex) { return -1; }
    }
}
