package www.etis_midi.fr.panda.io;

import java.util.HashMap;

public class CSVData {
    int[][] matrix;
    HashMap<Integer, String> itemMap;

    public CSVData(int[][] matrix, HashMap<Integer, String> itemMap) {
        this.matrix = matrix;
        this.itemMap = itemMap;
    }

	public int[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}

	public HashMap<Integer, String> getItemMap() {
		return itemMap;
	}

	public void setItemMap(HashMap<Integer, String> itemMap) {
		this.itemMap = itemMap;
	}
}