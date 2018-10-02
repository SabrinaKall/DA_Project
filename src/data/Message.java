package data;

import java.io.*;

public class Message implements Serializable {

    private static final long serialVersionUID = 5687140591059032687L;

    private int id;
    private String message;
    private boolean ack;

    public Message(String packet) {
        String idString = packet.substring(0, packet.indexOf("\n"));
        this.id = Integer.parseInt(idString);
        this.message = packet.substring(packet.indexOf("\n") + 1, packet.length());
        ack = false;
    }

    public Message(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public Message(boolean ack, int id) {
        this.ack = ack;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return id + "\n" + message;
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
