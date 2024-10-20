package deque;

import java.util.Comparator;

public class MaxArrayDeque<Item> extends ArrayDeque<Item> {

    private Comparator<Item> comparator;
    private Item maxitem;

    public MaxArrayDeque(Comparator<Item> c){
        super();
        this.comparator = c;
        this.maxitem = null;
    }

    public Item max(){
        if (isEmpty()) {
            return null;
        }
        return maxitem;
    }

    public Item max(Comparator<Item> c){
        if (isEmpty()) return null;
        Item max = null;
        for (Item i : this.node){
            if (c.compare(i, max) < 0) {
                max = i;
            }
        }
        maxitem = max;
        return max;
    }

}
