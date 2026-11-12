
package com.algotrade;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.algotrade.persistence.CsvUtil;

public class CsvUtilTest {

    public static void main(String[] args) throws Exception {

        Path file = Path.of("data", "csv_test.csv");

        List<String[]> original = new ArrayList<>();

        original.add(new String[] {
            "Symbol", "Company", "Price"
        });

        original.add(new String[] {
            "TCS", "Tata Consultancy Services", "3450"
        });

        original.add(new String[] {
            "INFY", "Infosys, Limited", "1800"
        });

        original.add(new String[] {
            "TEST", "Company \"Alpha\"", "250"
        });

        // Write
        CsvUtil.write(file, original);

        // Read
        List<String[]> loaded = CsvUtil.read(file);

        System.out.println("===== ALGOTRADE CSV TEST =====");

        System.out.println("Rows Loaded: " + loaded.size());

        for (String[] row : loaded) {
            System.out.println(String.join(" | ", row));
        }

        // Verify round-trip data
        boolean success = loaded.size() == original.size();

        for (int i = 0; success && i < original.size(); i++) {
            success = java.util.Arrays.equals(
                original.get(i), loaded.get(i));
        }

        System.out.println("CSV Round Trip: "
            + (success ? "PASSED" : "FAILED"));

        if (!success) {
            throw new AssertionError("CSV data mismatch");
        }
    }
}
