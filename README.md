# approximate-mining-of-top-K-patterns

# PaNDa+ Pattern Miner

PaNDa+ is a Java-based tool for **approximate mining of top-K patterns** from transactional datasets. The application includes a graphical user interface (GUI) for loading datasets, setting mining parameters, running the algorithm, and visualizing the results.

---

## How It Works

1. **Input Data**: The tool takes as input a **normal transactional dataset** (not necessarily binary). Each transaction lists the items it contains.

2. **Data Conversion**: The raw dataset is first converted into a **binary matrix** representation internally:
   - Rows correspond to transactions.
   - Columns correspond to unique items.
   - A cell value of 1 means the item appears in the transaction, 0 otherwise.
   
3. **Item Mapping**: A hash map is created to associate item indices with human-readable item labels. This allows meaningful output when patterns are displayed.

4. **Pattern Mining**: PaNDa+ then mines the **top-K patterns** from the binary matrix using approximate mining techniques that allow some noise.

---

## Threshold Parameters

PaNDa+ uses two key threshold parameters that control the tolerance to noise when mining patterns:

- **Epsilon R (εR)**: The maximum allowed noise ratio on rows (transactions). Controls how many incorrect transactions can be included in a pattern.

- **Epsilon C (εC)**: The maximum allowed noise ratio on columns (items). Controls how many missing or spurious items can be present in a pattern.

These thresholds allow PaNDa+ to perform **approximate mining**, finding patterns that are not perfectly exact but still significant, which is useful when datasets contain noise or errors.

---

## Features

- Load raw transactional datasets (CSV or text).
- Automatic conversion to binary matrix and item map.
- Interactive GUI with sliders for setting εR and εC between 0 and 1.
- Configurable top-K parameter.
- Display and export mined patterns with item labels and transaction IDs.

---

## Usage Overview

1. Select your dataset file via the GUI.
2. Adjust parameters: top-K, εR, εC (sliders restrict ε values between 0.0 and 1.0).
3. Run the PaNDa+ algorithm.
4. View the mined patterns in the output area.
5. Export patterns to a file if desired.

---

## Requirements

- Java 8 or higher
- Swing for GUI components

---

## Contact

For questions or feedback, please contact [Mussab Zneika: alznakamosab@hotmail.com].
