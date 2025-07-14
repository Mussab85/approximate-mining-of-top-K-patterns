package www.etis_midi.fr.panda.model;

import java.util.ArrayList;
import java.util.List;

public class ListOfItem implements Cloneable {
    public  List<Integer> items;
    public int size;

    public ListOfItem() {
        this.items = new ArrayList<>();
    }

    public List<Integer> getItems() {
        return items;
    }

    public void addItem(int item) {
        items.add(item);
    }

    @Override
    public ListOfItem clone() {
        ListOfItem copy = new ListOfItem();
        copy.items = new ArrayList<>(this.items);
        return copy;
    }
}
