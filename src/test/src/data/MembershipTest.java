package src.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;

public class MembershipTest {

    @Test
    public void containsID (){
        Address address1 = null;
        try {
            address1 = Memberships.getAddress(1);
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertNotNull(address1);
        Assertions.assertEquals("/127.0.0.1", address1.getIP().toString());
        Assertions.assertEquals(11001, address1.getPort());
    }

    @Test
    public void containsAddress(){
        int id = -1;
        try {
            id = Memberships.getProcessId(new Address("127.0.0.1", 11003));
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertEquals(3, id);
    }

    @Test
    public void nbProcesses() {
        try {
            Assertions.assertEquals(5, Memberships.getNbProcesses());
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
