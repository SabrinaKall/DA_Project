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
import java.util.*;

public class DependantMemberships {

    private static boolean isLoaded = false;
    private static DependantMemberships instance;
    private int nbProcesses;
    private Map<Integer, Address> memberships_by_id;
    private Map<Address, Integer> memberships_by_address;
    private Map<Integer, Set<Integer>> dependencies;
    private Map<Integer, Set<Integer>> ultimateDependencies;

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

    public Set<Integer> getDependenciesOf(int id){
        return dependencies.get(id);
    }

    public Set<Integer> getUltimateDependenciesOf(int id) {
        return ultimateDependencies.get(id);
    }

    public Map<Integer, Set<Integer>> getAllDependancies() {
        return dependencies;
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

                Set<Integer> dependencyList = new HashSet<>();

                for(int j = 1; j < words.length; ++j) {
                    dependencyList.add(Integer.parseInt(words[j]));
                }

                dependencies.put(processId, dependencyList);
            }

        } catch (IOException e) {
            throw new UnreadableFileException("Membership file unreadable");
        }

        ultimateDependencies = getUltimateDependencies();

        isLoaded = true;

    }


    /**
     * Recursively figure out who a process depends on through other processes
     *
     * The maximum depth of a tree of dependencies is the number of processes - 1
     * Ex:
     * 1 2
     * 2 3
     * 3 4
     * 4 5
     * 5
     *
     * 1 -> 2 -> 3 -> 4 -> 5 (P5 is a 4th degree dependency of P1)
     */
    private Map<Integer, Set<Integer>> getUltimateDependencies() {
        return getXDegreeDependencies(nbProcesses -1, dependencies);
    }

    /**
     * @param X
     * @param formerDependencies
     * @return
     *
     * Recursively get the dependencies at X steps of a process
     *
     * Ex: X=2
     * 1 2
     * 2 3
     * 3 4
     * 4 5
     * 5
     *
     * P1 -> P2 -> P3
     *
     */
    private Map<Integer, Set<Integer>> getXDegreeDependencies(int X, Map<Integer, Set<Integer>> formerDependencies) {
        Map<Integer, Set<Integer>> extendedDependencies = new HashMap<>(formerDependencies);
        for(int i = 0; i < X; i++) {
            extendedDependencies = getNextDegreeDependencies(extendedDependencies);
        }
        return extendedDependencies;
    }

    /**
     * @param formerDependencies
     * @return
     *
     * Get the processes that a dependency depends on and add them to the dependencies
     *
     * Ex:
     * Dependencies of P1 = {2}
     * Dependencies of P2 = {3, 4}
     * -> New dependencies of P1: {2, 3, 4}
     */
    private Map<Integer, Set<Integer>> getNextDegreeDependencies(Map<Integer, Set<Integer>> formerDependencies) {
        Map<Integer, Set<Integer>> extendedDependencies = new HashMap<>(formerDependencies);
        for (int processNb = 1; processNb <= nbProcesses; ++processNb) {
            Set<Integer> formerDeps = formerDependencies.get(processNb);
            Set<Integer> newDeps = new HashSet<>(formerDeps);

            for(Integer dep: formerDeps) {
                newDeps.addAll(formerDependencies.get(dep));
            }
            newDeps.remove(processNb);
            extendedDependencies.put(processNb, newDeps);
        }
        return extendedDependencies;
    }
}
