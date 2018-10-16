package src.observer.broadcast;

import src.data.message.Message;

public interface ReliableBroadcastObserver {
    void deliverRB(Message msg, int senderID);
}
