package src.observer.broadcast;

import src.data.message.Message;

public interface BestEffortBroadcastObserver {
    void deliverFromBestEffortBroadcast(Message msg, int senderID);
}
