package src.data.message.link;

import src.data.message.Message;
import src.data.message.SequenceMessage;

public class PerfectLinkMessage extends SequenceMessage {
    private boolean ack = false;

    public boolean isAck() {
        return ack;
    }

    public PerfectLinkMessage(Message m, int messageSequenceNumber, boolean ack) {
        super(m, messageSequenceNumber);
        this.ack = ack;
    }
}
