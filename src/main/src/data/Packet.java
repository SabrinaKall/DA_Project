package src.data;

import src.data.message.Message;

public class Packet {

    private boolean empty;
    private Message message;
    private int processId;

    public Packet() {
        this.empty = true;
    }

    public Packet(Message message, int processId) {
        this.empty = false;
        this.message = message;
        this.processId = processId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getProcessId() {
        return processId;
    }

    public boolean isEmpty() {
        return empty;
    }
}
