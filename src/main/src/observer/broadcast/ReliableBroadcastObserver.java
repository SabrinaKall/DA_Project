package src.observer.broadcast;

import src.data.Packet;

public interface ReliableBroadcastObserver {
    void deliverReliably(Packet p);
}
