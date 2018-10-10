package src.data;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Address {

    private InetAddress IP;
    private int port;

    public Address(InetAddress IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public Address(String IP, int port) {
        try {
            this.IP = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
    }

    public InetAddress getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }


    @Override
    public String toString() {
        return "Address{" +
                "IP=" + IP.toString() +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return port == address.port && IP.equals(address.IP);
    }

    @Override
    public int hashCode() {
        int result = IP.hashCode();
        result = 31 * result + port;
        return result;
    }
}
