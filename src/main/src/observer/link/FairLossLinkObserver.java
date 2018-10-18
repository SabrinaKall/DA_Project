package src.observer.link;

import src.data.message.Message;

public interface FairLossLinkObserver {
    void deliverFLL(Message m, int senderID);
}
