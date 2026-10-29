package petadoptionmanagementsystem;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Uganda Shillings (UGX) currency formatting utility.
 *
 * Uganda Shillings have no sub-units in everyday use, so we format
 * as whole numbers with thousands separators.
 *
 * Examples:
 *   UGX.format(150000)  →  "UGX 150,000"
 *   UGX.format(75000)   →  "UGX 75,000"
 *   UGX.parse("150,000") → 150000.0
 */
public class UGX {

    private static final NumberFormat NUMBER_FMT =
            NumberFormat.getIntegerInstance(Locale.US);   // comma thousands separator

    private UGX() {}   // utility class — no instances

    /** Formats a fee as "UGX 150,000" (whole number, comma-separated). */
    public static String format(double amount) {
        return "UGX " + NUMBER_FMT.format(Math.round(amount));
    }

    /** Short label used inside tables where space is tight: "150,000". */
    public static String compact(double amount) {
        return NUMBER_FMT.format(Math.round(amount));
    }

    /**
     * Parses a UGX string back to a double.
     * Accepts "150,000", "UGX 150,000", "150000".
     */
    public static double parse(String text) {
        if (text == null || text.isBlank()) return 0;
        String cleaned = text.replace("UGX", "").replace(",", "").trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
