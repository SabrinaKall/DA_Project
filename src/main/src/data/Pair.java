package src.data;

import java.util.Objects;

public class Pair {
    private int originalSenderID;
    private int messageSeqNum;

    public Pair(int originalSenderID, int messageSeqNum) {
        this.originalSenderID = originalSenderID;
        this.messageSeqNum = messageSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return originalSenderID == pair.originalSenderID &&
                messageSeqNum == pair.messageSeqNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalSenderID, messageSeqNum);
    }
}