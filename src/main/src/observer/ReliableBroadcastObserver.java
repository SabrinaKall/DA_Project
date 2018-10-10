package src.observer;

import src.data.Packet;

public interface ReliableBroadcastObserver {
    void deliverReliably(Packet p);
}
