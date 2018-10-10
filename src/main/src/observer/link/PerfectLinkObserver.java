package src.observer.link;

import src.data.Packet;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.io.IOException;

public interface PerfectLinkObserver extends LinkObserver {
    void deliverPL(Packet p) throws UnreadableFileException, IOException, BadIPException;
}
