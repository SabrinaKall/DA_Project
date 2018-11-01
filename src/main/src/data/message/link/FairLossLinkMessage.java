package src.data.message.link;

import src.data.message.Message;

public class FairLossLinkMessage extends Message {

    private Message message;
    private int senderID;

    public FairLossLinkMessage(Message message, int senderID) {
        this.message = message;
        this.senderID = senderID;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getSenderID() {
        return senderID;
    }
}
