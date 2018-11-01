package src.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;

import java.net.InetAddress;
import java.net.UnknownHostException;

class MembershipTest {

    @BeforeAll
    static void init() throws BadIPException, UnreadableFileException {
        Memberships.init("src/test/resources/membership");
    }

    @Test
    void containsID(){
        Address address1 = null;
        try {
            address1 = Memberships.getInstance().getAddress(1);
        } catch (UninitialisedMembershipsException e) {
            e.printStackTrace();
        }
        Assertions.assertNotNull(address1);
        Assertions.assertEquals("/127.0.0.1", address1.getIP().toString());
        Assertions.assertEquals(11001, address1.getPort());

    }

    @Test
    void containsAddress(){
        int id = -1;
        try {
            id = Memberships.getInstance().getProcessId(new Address(InetAddress.getByName("127.0.0.1"), 11003));
        } catch (UninitialisedMembershipsException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(3, id);

    }

    @Test
    void nbProcesses() {
        try {
            Assertions.assertEquals(5, Memberships.getInstance().getNbProcesses());
        } catch (UninitialisedMembershipsException e) {
            e.printStackTrace();
        }
    }
}
