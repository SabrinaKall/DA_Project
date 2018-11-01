package src.data;

import java.util.Objects;

public class UniqueMessageID {

    private int processID;
    private int SeqNb;

    public UniqueMessageID(int processID, int SeqNb) {
        this.processID = processID;
        this.SeqNb = SeqNb;
    }

    public int getProcessID() { return processID; }
    public int getSeqNb() { return SeqNb; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueMessageID uniqueMessageID = (UniqueMessageID) o;
        return processID == uniqueMessageID.processID &&
                SeqNb == uniqueMessageID.SeqNb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processID, SeqNb);
    }
}