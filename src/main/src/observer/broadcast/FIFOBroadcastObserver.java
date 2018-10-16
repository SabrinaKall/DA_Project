package src.observer.broadcast;

import src.data.message.Message;

public interface FIFOBroadcastObserver {
    void deliverFIFOB(Message msg, int senderID);
}
