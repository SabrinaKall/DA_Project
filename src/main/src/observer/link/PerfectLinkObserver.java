package src.observer.link;

import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.io.IOException;

public interface PerfectLinkObserver extends LinkObserver {
    void deliverPL(Message m, int senderID) throws UnreadableFileException, IOException, BadIPException;
}
