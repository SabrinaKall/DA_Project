package src.data.message.broadcast;

import src.data.message.Message;

import java.util.Map;

public class VectorBroadcastMessage extends BroadcastMessage {

    private Map<Integer, Integer> vectorClock;

    public VectorBroadcastMessage(Message m, int messageSequenceNumber, int originalSenderID, Map<Integer, Integer> vectorClock) {
        super(m, messageSequenceNumber, originalSenderID);
        this.vectorClock = vectorClock;
    }

    public Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }
}
