package src.observer.link;

import src.data.message.Message;

public interface PerfectLinkObserver {
    void deliverPL(Message m, int senderID);
}
