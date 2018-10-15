package src.observer.broadcast;

import src.data.message.Message;

public interface ReliableBroadcastObserver {
    void deliverReliably(Message msg, int senderID);
}
