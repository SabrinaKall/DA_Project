package src.observer.broadcast;

import src.data.message.Message;

public interface UniformBroadcastObserver {
    void deliverReliably(Message msg, int senderID);
}
