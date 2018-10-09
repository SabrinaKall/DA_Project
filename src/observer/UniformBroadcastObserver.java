package observer;

import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;

import java.io.IOException;

public interface UniformBroadcastObserver {
    void deliverReliably(Packet p);
}
