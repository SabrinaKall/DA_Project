package src.observer.broadcast;

import src.data.message.Message;

public interface UniformBroadcastObserver {
    void deliverFromUniformReliableBroadcast(Message msg, int senderID);
}
