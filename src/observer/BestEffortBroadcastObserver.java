package observer;

import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;

import java.io.IOException;

public interface BestEffortBroadcastObserver {
    void deliverBEB(Packet p) throws IOException, BadIPException, UnreadableFileException;
}
