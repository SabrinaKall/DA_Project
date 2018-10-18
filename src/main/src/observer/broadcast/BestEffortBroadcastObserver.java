package src.observer.broadcast;

import src.data.message.Message;

public interface BestEffortBroadcastObserver {
    void deliverBEB(Message msg, int senderID);
}
