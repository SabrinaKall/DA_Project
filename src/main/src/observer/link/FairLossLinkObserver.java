package src.observer.link;

import src.data.message.Message;
import src.exception.UnreadableFileException;
import src.exception.BadIPException;

import java.io.IOException;

public interface FairLossLinkObserver extends LinkObserver {
    void deliverFLL(Message m, int senderID) throws BadIPException, IOException, UnreadableFileException;
}
