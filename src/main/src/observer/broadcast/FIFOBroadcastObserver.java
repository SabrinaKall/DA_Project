package src.observer.broadcast;

import src.data.message.Message;

public interface FIFOBroadcastObserver {
    void deliverFromFIFOBroadcast(Message msg, int senderID);
}
