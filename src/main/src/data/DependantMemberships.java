package src.data;

import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependantMemberships {

    private static boolean isLoaded = false;
    private static DependantMemberships instance;
    private int nbProcesses;
    private Map<Integer, Address> memberships_by_id;
    private Map<Address, Integer> memberships_by_address;
    private Map<Integer, List<Integer>> dependencies;

    public synchronized static DependantMemberships getInstance() throws UninitialisedMembershipsException {
        if (!isLoaded) {
            throw new UninitialisedMembershipsException();
        } else {
            return instance;
        }
    }

    public static void init(String filename) throws UnreadableFileException, BadIPException {
        instance = new DependantMemberships(filename);
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

    public List<Integer> getDependenciesOf(int id){
        return dependencies.get(id);
    }

    private DependantMemberships(String filename) throws BadIPException, UnreadableFileException {

        memberships_by_id = new HashMap<>();
        memberships_by_address = new HashMap<>();
        dependencies = new HashMap<>();

        try {
            Path fileToRead = Paths.get(filename);
            List<String> allLines = Files.readAllLines(fileToRead);

            nbProcesses = Integer.parseInt(allLines.get(0));

            //get addresses
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
                    throw new BadIPException("Unknown address for process " + processId);
                }
            }

            //get dependencies
            for (int i = nbProcesses + 1; i <= 2*nbProcesses; ++i) {
                String line = allLines.get(i);
                String words[] = line.split(" ");

                int processId = Integer.parseInt(words[0]);

                List<Integer> dependencyList = new ArrayList<>();

                for(int j = 1; j < words.length; ++j) {
                    dependencyList.add(Integer.parseInt(words[j]));
                }

                dependencies.put(processId, dependencyList);
            }

        } catch (IOException e) {
            throw new UnreadableFileException("Membership file unreadable");
        }

        isLoaded = true;

    }
}
