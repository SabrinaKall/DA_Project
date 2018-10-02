package data;

import java.net.InetAddress;

public class Address {

    private InetAddress IP;
    private int port;

    public Address(InetAddress IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

        if (port != address.port) return false;
        return IP.equals(address.IP);
    }

    @Override
    public int hashCode() {
        int result = IP.hashCode();
        result = 31 * result + port;
        return result;
    }
}
