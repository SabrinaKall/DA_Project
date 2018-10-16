package src.observer.broadcast;

import src.data.message.Message;

public interface UniformBroadcastObserver {
    void deliverURB(Message msg, int senderID);
}
