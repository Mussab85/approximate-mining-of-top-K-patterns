package www.etis_midi.fr.panda.converter;

import java.io.*;
import java.util.*;

/**
 * Converts raw transaction data into a binary matrix CSV file.
 */
public class RawTransactionConverter {

    public static void convertToBinaryMatrix(String inputFile, String outputFile) throws IOException {
        List<List<String>> transactions = new ArrayList<>();
        Set<String> uniqueItems = new TreeSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> transaction = new ArrayList<>();
                for (String item : line.split(",")) {
                    String trimmed = item.trim();
                    if (!trimmed.isEmpty()) {
                        transaction.add(trimmed);
                        uniqueItems.add(trimmed);
                    }
                }
                transactions.add(transaction);
            }
        }

        List<String> sortedItems = new ArrayList<>(uniqueItems);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write header
            writer.println(String.join(",", sortedItems));

            // Write rows as binary vectors
            for (List<String> txn : transactions) {
                int[] binaryRow = new int[sortedItems.size()];
                for (String item : txn) {
                    int index = sortedItems.indexOf(item);
                    binaryRow[index] = 1;
                }
                writer.println(arrayToCsv(binaryRow));
            }
        }
    }

    private static String arrayToCsv(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i != arr.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
