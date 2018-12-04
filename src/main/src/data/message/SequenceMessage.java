package src.data.message;

public class SequenceMessage extends Message {

    private int messageSequenceNumber;
    private Message message;

    public SequenceMessage(Message m, int messageSequenceNumber) {
        this.message = m;
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

}
