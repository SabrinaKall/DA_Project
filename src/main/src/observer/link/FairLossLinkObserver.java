package src.observer.link;

import src.data.Packet;
import src.exception.UnreadableFileException;
import src.exception.BadIPException;

import java.io.IOException;

public interface FairLossLinkObserver extends LinkObserver {
    void deliverFLL(Packet p) throws BadIPException, IOException, UnreadableFileException;
}
