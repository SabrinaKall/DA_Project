package src.data;

import java.util.Objects;

public class BMID {
    private int originalSenderID;
    private int messageSeqNum;

    public BMID(int originalSenderID, int messageSeqNum) {
        this.originalSenderID = originalSenderID;
        this.messageSeqNum = messageSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BMID bmid = (BMID) o;
        return originalSenderID == bmid.originalSenderID &&
                messageSeqNum == bmid.messageSeqNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalSenderID, messageSeqNum);
    }
}