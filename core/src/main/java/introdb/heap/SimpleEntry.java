package introdb.heap;

import java.io.Serializable;

class SimpleEntry implements Serializable {
    private int value;
    private int key;

    public SimpleEntry(int key, int value) {
        this.value = key;
        this.key = value;
    }

    @Override
    public String toString() {
        return "SimpleEntry{" +
            "value=" + value +
            ", key=" + key +
            '}';
    }
}
