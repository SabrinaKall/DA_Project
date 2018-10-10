package src.data.message;

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
