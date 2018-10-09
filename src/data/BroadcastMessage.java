package data;

import info.Memberships;
import javafx.util.Pair;

public class BroadcastMessage extends SequenceMessage {
    private int originalSenderID;

    public BroadcastMessage(Message m, int messageSequenceNumber, int originalSenderID) {
        super(m, messageSequenceNumber);
        this.originalSenderID = originalSenderID;
    }

    public int getOriginalSenderID() {
        return originalSenderID;
    }

    public Pair<Integer, Integer> getUniqueIdentifier() {
        return new Pair<>(originalSenderID, getMessageSequenceNumber());
    }


}
