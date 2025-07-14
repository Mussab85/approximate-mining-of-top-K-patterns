package www.etis_midi.fr.panda.algorithm;

import www.etis_midi.fr.panda.model.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the PaNDa+ algorithm for top-K approximate pattern mining.
 * Uses lists for items and transactions instead of arrays for flexibility.
 */
public class PandaTopKPattern {
    private static final double DEFAULT_LAMBDA = 0.75;

    /**
     * Runs the PaNDa+ algorithm to find top-K patterns.
     *
     * @param k Number of patterns to find.
     * @param dataset Binary input matrix (transactions x items).
     * @param epsilonR Max allowed noise per row.
     * @param epsilonC Max allowed noise per column.
     * @return List of discovered patterns.
     * @throws CloneNotSupportedException 
     */
    public static List<Pattern> runPandaPlus(int k, int[][] dataset, double epsilonR, double epsilonC) throws CloneNotSupportedException {
        List<Pattern> discoveredPatterns = new ArrayList<>();
        int[][] residualDataset = cloneMatrix(dataset);

        for (int iter = 0; iter < k; iter++) {
            Pattern core = findCore(residualDataset, discoveredPatterns, dataset);
            if (core == null || core.getItems().isEmpty()) break;

            Pattern extended = extendCore(core, discoveredPatterns, dataset, epsilonR, epsilonC);

            double oldCost = computeCost(discoveredPatterns, dataset, DEFAULT_LAMBDA);
            List<Pattern> testPatterns = new ArrayList<>(discoveredPatterns);
            testPatterns.add(extended);
            double newCost = computeCost(testPatterns, dataset, DEFAULT_LAMBDA);

            if (newCost >= oldCost) break;

            discoveredPatterns.add(extended);
            removePatternCoverage(residualDataset, extended);
        }

        return discoveredPatterns;
    }

    /**
     * Finds the core pattern by greedily selecting frequent items.
     * @throws CloneNotSupportedException 
     */
    public static Pattern findCore(int[][] residual, List<Pattern> currentPatterns, int[][] originalDataset) throws CloneNotSupportedException {
        int numTrans = originalDataset.length;
        int numItems = originalDataset[0].length;

        // Calculate frequency of each item in residual matrix
        int[] itemFreq = new int[numItems];
        for (int i = 0; i < numTrans; i++) {
            for (int j = 0; j < numItems; j++) {
                if (residual[i][j] == 1) {
                    itemFreq[j]++;
                }
            }
        }

        // Sort items descending by frequency
        List<Integer> sortedItems = new ArrayList<>();
        for (int i = 0; i < numItems; i++) sortedItems.add(i);
        sortedItems.sort((a, b) -> Integer.compare(itemFreq[b], itemFreq[a]));

        if (sortedItems.isEmpty() || itemFreq[sortedItems.get(0)] == 0) {
            // No frequent items found
            return null;
        }

        // Initialize core pattern with the most frequent item
        Pattern core = new Pattern();
        core.addItem(sortedItems.get(0));
        // Add all transactions that contain this item
        for (int i = 0; i < numTrans; i++) {
            if (residual[i][sortedItems.get(0)] == 1) {
                core.addTransaction(i);
            }
        }

        // Try to add more items to core pattern if it reduces cost
        for (int idx = 1; idx < sortedItems.size(); idx++) {
            int candidateItem = sortedItems.get(idx);

            // Intersect transactions of current core with transactions containing candidate item
            List<Integer> intersectedTrans = new ArrayList<>();
            for (int t : core.getTransactions()) {
                if (residual[t][candidateItem] == 1) {
                    intersectedTrans.add(t);
                }
            }

            if (intersectedTrans.isEmpty()) {
                continue;
            }

            // Create candidate pattern
            Pattern candidate = core.clone();
            candidate.addItem(candidateItem);
            candidate.getTransactions().clear();
            candidate.getTransactions().addAll(intersectedTrans);

            // Check if candidate pattern improves cost
            List<Pattern> testSet = new ArrayList<>(currentPatterns);
            testSet.add(candidate);
            double newCost = computeCost(testSet, originalDataset, DEFAULT_LAMBDA);

            List<Pattern> oldSet = new ArrayList<>(currentPatterns);
            oldSet.add(core);
            double oldCost = computeCost(oldSet, originalDataset, DEFAULT_LAMBDA);

            if (newCost < oldCost) {
                core = candidate;
            }
        }

        return core;
    }

    /**
     * Attempts to extend the core pattern by adding transactions and items greedily,
     * staying within noise thresholds.
     * @throws CloneNotSupportedException 
     */
    public static Pattern extendCore(Pattern core, List<Pattern> currentPatterns, int[][] dataset,
                                    double epsilonR, double epsilonC) throws CloneNotSupportedException {
        int numTrans = dataset.length;
        int numItems = dataset[0].length;

        // === 1. Try to add more transactions ===
        for (int t = 0; t < numTrans; t++) {
            if (!core.getTransactions().contains(t)) {
                boolean allItemsPresent = true;
                for (int item : core.getItems()) {
                    if (dataset[t][item] == 0) {
                        allItemsPresent = false;
                        break;
                    }
                }
                if (!allItemsPresent) continue;

                Pattern candidate = core.clone();
                candidate.addTransaction(t);

                if (!tooNoisy(candidate, dataset, epsilonR, epsilonC)) {
                    double oldCost = computeCost(union(currentPatterns, core), dataset, DEFAULT_LAMBDA);
                    double newCost = computeCost(union(currentPatterns, candidate), dataset, DEFAULT_LAMBDA);
                    if (newCost < oldCost) {
                        core = candidate;
                    }
                }
            }
        }

        // === 2. Greedily add new items with best support in current transactions ===
        while (true) {
            int[] freq = new int[numItems];
            for (int t : core.getTransactions()) {
                for (int item = 0; item < numItems; item++) {
                    if (dataset[t][item] == 1) freq[item]++;
                }
            }

            int bestItem = -1;
            int maxFreq = -1;
            for (int item = 0; item < numItems; item++) {
                if (!core.getItems().contains(item)) {
                    if (freq[item] > maxFreq) {
                        bestItem = item;
                        maxFreq = freq[item];
                    }
                }
            }

            if (bestItem == -1) break;

            Pattern candidate = core.clone();
            candidate.addItem(bestItem);

            if (!tooNoisy(candidate, dataset, epsilonR, epsilonC)) {
                double oldCost = computeCost(union(currentPatterns, core), dataset, DEFAULT_LAMBDA);
                double newCost = computeCost(union(currentPatterns, candidate), dataset, DEFAULT_LAMBDA);

                if (newCost < oldCost) {
                    core = candidate;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return core;
    }

    /**
     * Removes pattern coverage from the residual matrix by setting covered elements to 0.
     */
    private static void removePatternCoverage(int[][] residual, Pattern pattern) {
        for (int t : pattern.getTransactions()) {
            for (int item : pattern.getItems()) {
                residual[t][item] = 0;
            }
        }
    }

    /**
     * Computes the cost function balancing noise and pattern complexity.
     *
     * @param patterns List of patterns.
     * @param dataset Original binary dataset.
     * @param lambda Weight for pattern complexity.
     * @return Total cost.
     */
    public static double computeCost(List<Pattern> patterns, int[][] dataset, double lambda) {
        int[][] noiseMatrix = computeNoiseMatrix(patterns, dataset);

        int noiseCost = 0;
        for (int[] row : noiseMatrix) {
            for (int val : row) {
                noiseCost += val;
            }
        }

        int patternCost = 0;
        for (Pattern p : patterns) {
            patternCost += p.getItems().size() + p.getTransactions().size();
        }

        return noiseCost + lambda * patternCost;
    }

    /**
     * Computes a noise matrix representing mismatches between dataset and pattern coverage.
     */
    private static int[][] computeNoiseMatrix(List<Pattern> patterns, int[][] dataset) {
        int nRows = dataset.length;
        int nCols = dataset[0].length;
        int[][] coverage = new int[nRows][nCols];

        for (Pattern p : patterns) {
            for (int t : p.getTransactions()) {
                if (t < 0 || t >= nRows) continue;  // safety for rows

                for (int item : p.getItems()) {
                    if (item < 0 || item >= nCols) continue;  // safety for columns

                    coverage[t][item] = 1;
                }
            }
        }
        int[][] noise = new int[nRows][nCols];
        for (int i = 0; i < nRows; i++) {
            int[] rowCoverage = coverage[i];
            int[] rowDataset = dataset[i];

            int len = Math.min(rowCoverage.length, rowDataset.length);  // safety for ragged arrays
            for (int j = 0; j < len; j++) {
                noise[i][j] = rowCoverage[j] ^ rowDataset[j];
            }
        }
        return noise;
    }

    /**
     * Checks if a pattern is too noisy beyond allowed thresholds.
     */
    public static boolean tooNoisy(Pattern pattern, int[][] dataset, double epsilonR, double epsilonC) {
        int noiseCount = 0;
        int rows = pattern.getTransactions().size();
        int cols = pattern.getItems().size();

        for (int t : pattern.getTransactions()) {
            for (int item : pattern.getItems()) {
                if (dataset[t][item] == 0) {
                    noiseCount++;
                }
            }
        }

        double maxAllowedNoise = epsilonR * rows * cols;
        return noiseCount > maxAllowedNoise;
    }

    /**
     * Creates a shallow clone of a matrix.
     */
    public static int[][] cloneMatrix(int[][] matrix) {
        int[][] clone = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, clone[i], 0, matrix[i].length);
        }
        return clone;
    }

    /**
     * Returns a new list with all patterns in patterns plus the extra pattern p.
     */
    private static List<Pattern> union(List<Pattern> patterns, Pattern p) {
        List<Pattern> union = new ArrayList<>(patterns);
        union.add(p);
        return union;
    }
}
