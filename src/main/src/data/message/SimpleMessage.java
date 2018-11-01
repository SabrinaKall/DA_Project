package src.data.message;

import java.io.*;
import java.util.Objects;

public class SimpleMessage extends Message implements Serializable {

    private static final long serialVersionUID = 5687140591059032687L;

    private String text;

    public SimpleMessage() {
        this.text = null;
    }

    public SimpleMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleMessage)) {
            return false;
        }
        SimpleMessage msg = (SimpleMessage) o;
        return (msg.text).equals(text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}