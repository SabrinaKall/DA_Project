package src.observer.broadcast;

import src.data.message.Message;

public interface LocalizedBroadcastObserver {
    void deliverFromLocalizedBroadcast(Message msg, int senderID);
}
