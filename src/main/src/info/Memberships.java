package src.info;

import src.data.Address;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Memberships {

    private static boolean isLoaded = false;
    private static Memberships instance;
    private static int nbProcesses;
    private static Map<Integer, Address> memberships_by_id;
    private static Map<Address, Integer> memberships_by_address;

    public static Memberships getInstance() throws UninitialisedMembershipsException {
        if (!isLoaded) {
            throw new UninitialisedMembershipsException();
        } else {
            return instance;
        }
    }

    public static void init(String filename) throws UnreadableFileException, BadIPException {
        instance = new Memberships(filename);
    }

    public int getNbProcesses() {
        return nbProcesses;
    }

    public Address getAddress(int processId) {
        return memberships_by_id.get(processId);
    }

    public int getProcessId(Address address) {
        return memberships_by_address.get(address);
    }

    private Memberships(String filename) throws BadIPException, UnreadableFileException {
        memberships_by_id = new HashMap<>();
        memberships_by_address = new HashMap<>();

        try {
            Path fileToRead = Paths.get(filename);
            List<String> allLines = Files.readAllLines(fileToRead);

            nbProcesses = Integer.parseInt(allLines.get(0));

            for (int i = 1; i <= nbProcesses; ++i) {
                String line = allLines.get(i);
                String words[] = line.split(" ");

                int processId = Integer.parseInt(words[0]);
                InetAddress IPAddress;
                try {
                    IPAddress = InetAddress.getByName(words[1]);
                    Address address = new Address(IPAddress, Integer.parseInt(words[2]));
                    memberships_by_id.put(processId, address);
                    memberships_by_address.put(address, processId);
                } catch (UnknownHostException e) {
                    //TODO: logger -> ip address bad -> shut down program
                    String errorMessage = "Unknown address for process " + processId + ": not saved";
                    throw new BadIPException(errorMessage);
                }
            }

        } catch (IOException e) {
            //TODO:logger: could not read file -> shut down program
            throw new UnreadableFileException("Membership file unreadable");
        }

        isLoaded = true;

    }
}
