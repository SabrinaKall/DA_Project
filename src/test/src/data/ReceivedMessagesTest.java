package src.data;

import src.data.ReceivedMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

public class ReceivedMessagesTest {

    private ReceivedMessages testObject;

    @BeforeEach
    public void init() {
        this.testObject = new ReceivedMessages();

        SortedSet<Integer> set = new TreeSet<>();
        set.add(4);
        set.add(6);
        set.add(3);

        testObject.setSmallest(1);

        testObject.setReceived(set);
    }

    @Test
    public void containsWorks() {
        Assertions.assertTrue(testObject.contains(1));
        Assertions.assertTrue(testObject.contains(3));
        Assertions.assertFalse(testObject.contains(5));

    }

    @Test
    public void smallerMessageReceived() {

        ReceivedMessages receivedMessages = new ReceivedMessages();
        receivedMessages.setSmallest(3);

        Assertions.assertTrue(receivedMessages.contains(1));

    }

    @Test
    public void addWorks() {

        testObject.add(2);

        Assertions.assertEquals(4, testObject.getSmallest());
        Assertions.assertEquals(6, testObject.getReceived().first().intValue());

        testObject.add(7);

        Assertions.assertEquals(4, testObject.getSmallest());
        Assertions.assertEquals(6, testObject.getReceived().first().intValue());
        Assertions.assertTrue(testObject.getReceived().contains(7));

        testObject.add(5);
        Assertions.assertEquals(7, testObject.getSmallest());
        Assertions.assertTrue(testObject.getReceived().isEmpty());


    }
}
