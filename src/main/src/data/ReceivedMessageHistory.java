package src.data;

import java.util.SortedSet;
import java.util.TreeSet;

public class ReceivedMessageHistory {

    private int smallest;
    private SortedSet<Integer> received;

    public ReceivedMessageHistory() {
        this.smallest = 0;
        this.received = new TreeSet<>();
    }

    public int getSmallest() {
        return smallest;
    }

    public void setSmallest(int smallest) {
        this.smallest = smallest;
    }

    public SortedSet<Integer> getReceived() {
        return received;
    }

    public void setReceived(SortedSet<Integer> received) {
        this.received = received;
    }

    public void add(int newReceived) {

        if (smallest + 1 == newReceived) {
            smallest = newReceived;
            for (Integer prevReceived : received) {
                if (smallest + 1 == prevReceived) {
                    smallest += 1;
                } else {
                    break;
                }
            }

            received = received.tailSet(smallest + 1);

        } else if (newReceived > smallest) {
            received.add(newReceived);
        }
    }

    public boolean contains(int newReceived) {
        return smallest >= newReceived || received.contains(newReceived);
    }

}
