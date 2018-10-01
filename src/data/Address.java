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

    //TODO
    public int getProcessNumber() {
        return 0;
    }
}
