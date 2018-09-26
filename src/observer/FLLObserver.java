package observer;

import data.Packet;

public interface FLLObserver extends LinkObserver{
    void deliverFLL(Packet p);
}
