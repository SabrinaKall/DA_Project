package src.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logger {

    private static final String PARENT_DIRECTORY = "resources/";

    private int processID;
    private String filepath;

    //do not write to file immediately (constantly opening the file waste of time) -> save in list
    private List<String> tempLogs = new ArrayList<>();

    public Logger(int processID) {
        this.processID = processID;
        this.filepath = PARENT_DIRECTORY + "da_proc_" + processID + ".out";
        File logFile = new File(filepath);
        if(logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        tempLogs.add(message);
    }

    private String messageWithEndline(String message) {
        if(message.charAt(message.length()-1) == '\n') {
            return message;
        } else {
            return message + '\n';
        }
    }

    public void logBroadcast(int seqNr) {
        log("b " + seqNr + "\n");
    }

    public void logDelivery(int sender, int seqNr) {
        log("d " + sender + " " + seqNr + "\n");
    }

    //Before shutdown, write all saved logs to file
    public void writeToFile() {
        List<String> written = new ArrayList<>();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            for(String message: tempLogs) {
                writer.write(messageWithEndline(message));
                written.add(message);
            }
        } catch (IOException e) {
            //java.exception handling left as an exercise for the reader
            tempLogs.removeAll(written);
        }
        tempLogs.removeAll(written);
    }
}
