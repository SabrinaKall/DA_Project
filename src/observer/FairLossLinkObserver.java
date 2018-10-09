package observer;

import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;

import java.io.IOException;

public interface FairLossLinkObserver extends LinkObserver {
    void deliverFLL(Packet p) throws BadIPException, IOException, UnreadableFileException;
}
