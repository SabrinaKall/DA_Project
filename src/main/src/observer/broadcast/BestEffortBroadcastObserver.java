package src.observer.broadcast;

import src.data.Packet;
import src.exception.UnreadableFileException;
import src.exception.BadIPException;

import java.io.IOException;

public interface BestEffortBroadcastObserver {
    void deliverBEB(Packet p) throws IOException, BadIPException, UnreadableFileException;
}
