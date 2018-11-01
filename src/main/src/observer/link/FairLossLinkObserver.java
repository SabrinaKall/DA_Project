package src.observer.link;

import src.data.message.Message;

public interface FairLossLinkObserver {
    void deliverFromFairLossLink(Message m, int senderID);
}
