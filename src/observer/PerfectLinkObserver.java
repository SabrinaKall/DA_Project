package observer;

import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;

import java.io.IOException;

public interface PerfectLinkObserver extends LinkObserver {
    void deliverPL(Packet p) throws UnreadableFileException, IOException, BadIPException;
}
