package src.data.message.broadcast;

import src.data.UniqueMessageID;
import src.data.message.Message;
import src.data.message.SequenceMessage;


public class BroadcastMessage extends SequenceMessage {
    private int originalSenderID;

    public BroadcastMessage(Message m, int messageSequenceNumber, int originalSenderID) {
        super(m, messageSequenceNumber);
        this.originalSenderID = originalSenderID;
    }

    public int getOriginalSenderID() {
        return originalSenderID;
    }

    public UniqueMessageID getUniqueIdentifier() {
        return new UniqueMessageID(originalSenderID, getMessageSequenceNumber());
    }


}
