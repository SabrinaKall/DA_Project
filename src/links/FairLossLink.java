package links;

import data.Address;
import data.Message;
import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;
import info.Memberships;
import observer.FairLossLinkObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink implements Link, Runnable {

    private DatagramSocket socket;
    private FairLossLinkObserver obsFLL = null;

    public FairLossLink(int port) throws SocketException, UnreadableFileException, BadIPException {
        this.socket = new DatagramSocket(port);
    }

    public void registerObserver(FairLossLinkObserver obsFLL) {
        this.obsFLL = obsFLL;
    }

    public boolean hasObserver() {
        boolean ret = (this.obsFLL == null);

        return !ret;
    }

    @Override
    public void send(Message message, int destID) throws IOException, BadIPException, UnreadableFileException {

        byte[] messageArray = message.convertToBytes();
        Address destAddress = Memberships.getAddress(destID);
        DatagramPacket packet = new DatagramPacket(messageArray, messageArray.length,
                destAddress.getIP(), destAddress.getPort());

        socket.send(packet);
    }


    public Packet receive() throws IOException, ClassNotFoundException, BadIPException, UnreadableFileException {
        //TODO: watch out for longer strings
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        Message message = Message.convertFromBytes(packet.getData());

        InetAddress senderIP = packet.getAddress();
        int senderPort = packet.getPort();
        Address address = new Address(senderIP, senderPort);
        int processId = Memberships.getProcessId(address);
        return new Packet(message, processId);
    }

    @Override
    public void run() {
        while (true) {

            Packet p = null;
            try {
                p = this.receive();
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                } else {
                    //TODO: logger;
                }
            } catch (ClassNotFoundException e) {
                //TODO: logger
            } catch (BadIPException e) {
                e.printStackTrace();
            } catch (UnreadableFileException e) {
                e.printStackTrace();
            }

            if (hasObserver()) {
                try {
                    this.obsFLL.deliverFLL(p);
                } catch (BadIPException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnreadableFileException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void finalize() {
        socket.close();
    }
}
