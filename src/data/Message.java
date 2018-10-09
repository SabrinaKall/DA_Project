package data;

import java.io.*;

public class Message implements Serializable {

    private static final long serialVersionUID = 5687140591059032687L;

    private int messageSequenceNumber;
    private boolean ack = false;

    public Message(int messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public Message(boolean ack, int messageSequenceNumber) {
        this.ack = ack;
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public void setMessageSequenceNumber(int messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }


    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public byte[] convertToBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static Message convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (Message) in.readObject();
        }
    }

}
