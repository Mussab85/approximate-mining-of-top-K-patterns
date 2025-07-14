package www.etis_midi.fr.panda.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.HashMap;
import www.etis_midi.fr.panda.io.CSVData;
import www.etis_midi.fr.panda.model.Pattern;
import www.etis_midi.fr.panda.algorithm.PandaTopKPattern;
import www.etis_midi.fr.panda.converter.RawTransactionConverter;
import www.etis_midi.fr.panda.output.PatternWritter;

public class PandaGUI extends JFrame {

    private JTextField filePathField, kField;
    private JSlider epsRSlider, epsCSlider;  // <-- sliders instead of JTextField
    private JLabel epsRLabel, epsCLabel;     // labels to show slider value as decimal
    private JTextArea outputArea;

    public PandaGUI() {
        setTitle("PaNDa+ Pattern Miner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // File selection
        JPanel fileRow = new JPanel(new BorderLayout(5, 5));
        filePathField = new JTextField();
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> chooseFile());
        fileRow.add(new JLabel("Dataset CSV:"), BorderLayout.WEST);
        fileRow.add(filePathField, BorderLayout.CENTER);
        fileRow.add(browseBtn, BorderLayout.EAST);
        inputPanel.add(fileRow);
        inputPanel.add(Box.createVerticalStrut(10));

        // Parameter fields with sliders for epsilon
        JPanel paramRow = new JPanel(new GridLayout(2, 6, 10, 10)); // 2 rows for labels and sliders

        // Top-K
        paramRow.add(new JLabel("Top-K:"));
        kField = new JTextField("100");
        paramRow.add(kField);
        paramRow.add(new JLabel("")); // filler
        paramRow.add(new JLabel("")); // filler
        paramRow.add(new JLabel("")); // filler
        paramRow.add(new JLabel("")); // filler

        // Epsilon R slider with label
        paramRow.add(new JLabel("Epsilon R:"));
        epsRSlider = new JSlider(0, 100, 60); // default 0.6
        epsRSlider.setMajorTickSpacing(20);
        epsRSlider.setMinorTickSpacing(5);
        epsRSlider.setPaintTicks(true);
        epsRSlider.setPaintLabels(true);
        paramRow.add(epsRSlider);
        epsRLabel = new JLabel("0.60");
        paramRow.add(epsRLabel);

        // Epsilon C slider with label
        paramRow.add(new JLabel("Epsilon C:"));
        epsCSlider = new JSlider(0, 100, 60);
        epsCSlider.setMajorTickSpacing(20);
        epsCSlider.setMinorTickSpacing(5);
        epsCSlider.setPaintTicks(true);
        epsCSlider.setPaintLabels(true);
        paramRow.add(epsCSlider);
        epsCLabel = new JLabel("0.60");
        paramRow.add(epsCLabel);

        inputPanel.add(paramRow);
        inputPanel.add(Box.createVerticalStrut(10));

        // Listeners to update label when slider moves
        epsRSlider.addChangeListener(e -> {
            double val = epsRSlider.getValue() / 100.0;
            epsRLabel.setText(String.format("%.2f", val));
        });
        epsCSlider.addChangeListener(e -> {
            double val = epsCSlider.getValue() / 100.0;
            epsCLabel.setText(String.format("%.2f", val));
        });

        // Run button
        JButton runButton = new JButton("Run PaNDa+");
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.addActionListener(this::runAlgorithm);
        inputPanel.add(runButton);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output Patterns"));

        // Add everything to the frame
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            filePathField.setText(selected.getAbsolutePath());
        }
    }

    private void runAlgorithm(ActionEvent e) {
        outputArea.setText("Running PaNDa+...\n");
        try {
            String file = filePathField.getText();
            int k = Integer.parseInt(kField.getText());
            double epsR = epsRSlider.getValue() / 100.0; // get decimal value from slider
            double epsC = epsCSlider.getValue() / 100.0;
            RawTransactionConverter.convertToBinaryMatrix(file, "dataset.csv");

            CSVData data = Main.readMatrixAndItemMap("dataset.csv");
            int[][] matrix = data.getMatrix();
            HashMap<Integer, String> itemMap = data.getItemMap();

            List<Pattern> patterns = PandaTopKPattern.runPandaPlus(k, matrix, epsR, epsC);

            // Show output
            StringBuilder output = new StringBuilder("Top-K Patterns Found:\n\n");
            int count = 1;
            for (Pattern p : patterns) {
                output.append("Pattern ").append(count++).append(": Items -> ");
                for (int item : p.getItems()) {
                    output.append(itemMap.getOrDefault(item, "Item" + item)).append(" ");
                }
                output.append("| Transactions: ");
                for (int tid : p.getTransactions()) {
                    output.append(tid).append(" ");
                }
                output.append("\n");
            }

            outputArea.setText(output.toString());
            PatternWritter.writePatternWithMapping("patterns_output.txt", patterns, itemMap);
        } catch (Exception ex) {
            outputArea.setText("Error:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PandaGUI().setVisible(true));
    }
}
