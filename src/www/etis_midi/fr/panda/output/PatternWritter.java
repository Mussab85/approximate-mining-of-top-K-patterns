package www.etis_midi.fr.panda.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import www.etis_midi.fr.panda.model.Pattern;

/**
 * Utility class to write discovered patterns to a file.
 */
public class PatternWritter {

    public static void writePatternWithMapping(String filename, List<Pattern> patterns, HashMap<Integer, String> itemMap) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            int count = 1;
            for (Pattern p : patterns) {
                writer.write("Pattern " + count++ + ": Items -> ");
                for (int index : p.items.items) {
                    String label = itemMap.getOrDefault(index, "Item" + index);
                    writer.write(label + " ");
                }

                writer.write(" | Transactions: ");
                for (int tid : p.transc.transc) {
                    writer.write(tid + " ");
                }
                writer.write("\n");
            }
        }
    }
}
