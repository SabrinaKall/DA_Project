package src.data;

import java.util.SortedSet;
import java.util.TreeSet;

public class ReceptionTracker {

    private int smallestReceived;
    private SortedSet<Integer> biggerReceived;

    public ReceptionTracker() {
        this.smallestReceived = 0;
        this.biggerReceived = new TreeSet<>();
    }

    public int getSmallestReceived() {
        return smallestReceived;
    }

    public void setSmallestReceived(int smallestReceived) {
        this.smallestReceived = smallestReceived;
    }

    public SortedSet<Integer> getBiggerReceived() {
        return biggerReceived;
    }

    public void setBiggerReceived(SortedSet<Integer> biggerReceived) {
        this.biggerReceived = biggerReceived;
    }

    public boolean addReceived(int newReceived) {

        if(alreadyReceived(newReceived)) {
            return false;
        }

        biggerReceived.add(newReceived);

        for (Integer received : biggerReceived) {
            if (smallestReceived + 1 == received) {
                smallestReceived += 1;
            } else {
                break;
            }
        }

        biggerReceived = biggerReceived.tailSet(smallestReceived + 1);

        return true;
    }

    public boolean alreadyReceived(int newReceived) {
        return smallestReceived >= newReceived || biggerReceived.contains(newReceived);
    }

}
