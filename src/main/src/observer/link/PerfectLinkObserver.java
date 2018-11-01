package src.observer.link;

import src.data.message.Message;

public interface PerfectLinkObserver {
    void deliverFromPerfectLink(Message m, int senderID);
}
