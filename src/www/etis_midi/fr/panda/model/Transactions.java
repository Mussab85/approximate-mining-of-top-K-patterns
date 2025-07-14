package www.etis_midi.fr.panda.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of transaction IDs in which a pattern appears.
 */
public class Transactions implements Cloneable, Serializable {
    public List<Integer> transc;

    /**
     * Constructor initializes an empty list of transaction IDs.
     */
    public Transactions() {
        transc = new ArrayList<>();
    }

    /**
     * Adds a transaction ID to the list.
     *
     * @param value the transaction ID to add
     */
    public void addTransaction(int value) {
        transc.add(value);
    }

    /**
     * Creates a deep copy of this Transctions object.
     *
     * @return a cloned instance of Transctions
     * @throws CloneNotSupportedException if cloning is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Transactions t = new Transactions();
        t.transc = new ArrayList<>(this.transc);
        return t;
    }
}
