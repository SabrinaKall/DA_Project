package src.observer.broadcast;

import src.data.message.Message;
import src.exception.UnreadableFileException;
import src.exception.BadIPException;

import java.io.IOException;

public interface BestEffortBroadcastObserver {
    void deliverBEB(Message msg, int senderID) throws IOException, BadIPException, UnreadableFileException;
}
