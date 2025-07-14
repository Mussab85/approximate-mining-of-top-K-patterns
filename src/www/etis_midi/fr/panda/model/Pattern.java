package www.etis_midi.fr.panda.model;

import java.util.List;

public class Pattern implements Cloneable {
    public ListOfItem items;
    public Transactions transc;

    public Pattern() {
        items = new ListOfItem();
        transc = new Transactions();
    }

    public void addItem(int item) {
        items.addItem(item);
    }

    public void addTransaction(int transaction) {
        transc.addTransaction(transaction);
    }
    
    public List<Integer> getTransactions() {
        return transc.transc;
    }
    public List<Integer> getItems() {
        return items.items;  // assuming ListOfItem has getItems() returning List<Integer>
    }

    @Override
    public Pattern clone() throws CloneNotSupportedException {
        Pattern clone = new Pattern();
        clone.items = (ListOfItem) this.items.clone();
        clone.transc = (Transactions) this.transc.clone();
        return clone;
    }
}
