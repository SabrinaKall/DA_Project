package src.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;

class MembershipTest {

    @Test
    void containsID(){
        Address address1 = null;
        try {
            address1 = Memberships.getInstance().getAddress(1);
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertNotNull(address1);
        Assertions.assertEquals("/127.0.0.1", address1.getIP().toString());
        Assertions.assertEquals(11001, address1.getPort());

        Address address19 = null;
        try {
            address19 = Memberships.getInstance().getAddress(19);
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertNotNull(address19);
        Assertions.assertEquals("/127.0.0.1", address19.getIP().toString());
        Assertions.assertEquals(11019, address19.getPort());
    }

    @Test
    void containsAddress(){
        int id = -1;
        try {
            id = Memberships.getInstance().getProcessId(new Address("127.0.0.1", 11003));
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertEquals(3, id);

        int id10 = -1;
        try {
            id10 = Memberships.getInstance().getProcessId(new Address("127.0.0.1", 11010));
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertEquals(10, id10);
    }

    @Test
    void nbProcesses() {
        try {
            Assertions.assertEquals(19, Memberships.getInstance().getNbProcesses());
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
