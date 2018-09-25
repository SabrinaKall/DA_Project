package data;

public class Packet {

    private boolean empty;
    private Message message;
    private Address address;

    public Packet() {
        empty = true;
    }

    public Packet(Message message, Address address) {
        empty = false;
        this.message = message;
        this.address = address;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isEmpty() {
        return empty;
    }
}
