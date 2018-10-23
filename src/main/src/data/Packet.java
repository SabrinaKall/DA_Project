package src.data;

import src.data.message.Message;

public class Packet {

    private Message message;
    private int processId;

    public Packet(Message message, int processId) {
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
}
