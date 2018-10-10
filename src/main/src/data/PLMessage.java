package src.data;

public class PLMessage extends SequenceMessage {
    private boolean ack = false;

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public PLMessage(Message m, int messageSequenceNumber) {
        super(m, messageSequenceNumber);
    }

    public PLMessage(Message m, int messageSequenceNumber, boolean ack) {
        super(m, messageSequenceNumber);
        this.ack = ack;
    }
}
