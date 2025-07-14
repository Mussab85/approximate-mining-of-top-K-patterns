package www.etis_midi.fr.panda.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import www.etis_midi.fr.panda.converter.RawTransactionConverter;
import www.etis_midi.fr.panda.io.CSVData;
import www.etis_midi.fr.panda.model.Pattern;
import www.etis_midi.fr.panda.algorithm.PandaTopKPattern;
import www.etis_midi.fr.panda.output.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // Step 0: Convert raw transactions to binary matrix
        RawTransactionConverter.convertToBinaryMatrix("groceries.csv", "dataset.csv");

        // Step 1: Read dataset and item map from single CSV file
        CSVData data = readMatrixAndItemMap("dataset.csv");
        int[][] D = data.getMatrix();
        HashMap<Integer, String> itemMap = data.getItemMap();

        // Parameters
        int k = 100;
        double epsilon_r = 0.6;
        double epsilon_c = 0.6;

        // Run PaNDa+ algorithm
        List<Pattern> patterns = PandaTopKPattern.runPandaPlus(k, D, epsilon_r, epsilon_c);

        // Print results with item mapping
        System.out.println("Top-K Patterns:");
        int pNum = 1;
        for (Pattern p : patterns) {
            System.out.print("Pattern " + pNum++ + ": Items -> ");
            // Access items through getter returning List<Integer>
            for (int index : p.getItems()) {
                String label = itemMap.getOrDefault(index, "Item" + index);
                System.out.print(label + " ");
            }

            System.out.print(" | Transactions: ");
            // Access transactions through getter returning List<Integer>
            for (int tid : p.getTransactions()) {
                System.out.print(tid + " ");
            }

            System.out.println();
        }

        // Write results to file with mapping
        PatternWritter.writePatternWithMapping("patterns_output.txt", patterns, itemMap);
    }
    public static CSVData readMatrixAndItemMap(String filename) throws Exception {
        ArrayList<int[]> rows = new ArrayList<>();
        HashMap<Integer, String> itemMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String headerLine = br.readLine();
            String[] itemNames = headerLine.trim().split(","); // <-- fix here
            for (int i = 0; i < itemNames.length; i++) {
                itemMap.put(i, itemNames[i].trim());
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split(","); // <-- fix here
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i].trim());
                }
                rows.add(row);
            }
        }

        int[][] matrix = new int[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            matrix[i] = rows.get(i);
        }

        return new CSVData(matrix, itemMap);
    }

}
