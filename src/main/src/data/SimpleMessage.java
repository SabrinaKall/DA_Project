package src.data;

import java.io.*;

public class SimpleMessage extends Message implements Serializable {

    private static final long serialVersionUID = 5687140591059032687L;

    private String text;

    public SimpleMessage(String whatever) {
        this.text = whatever;
    }

    public String getText() {
        return text;
    }
}