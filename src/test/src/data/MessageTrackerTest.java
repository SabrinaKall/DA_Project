package src.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

class MessageTrackerTest {

    private MessageTracker testObject;

    @BeforeEach
    void init() {
        this.testObject = new MessageTracker();

        SortedSet<Integer> set = new TreeSet<>();
        set.add(4);
        set.add(6);
        set.add(3);

        testObject.setSmallestReceived(1);

        testObject.setBiggerReceived(set);
    }

    @Test
    void containsWorks() {
        Assertions.assertTrue(testObject.alreadyReceived(1));
        Assertions.assertTrue(testObject.alreadyReceived(3));
        Assertions.assertFalse(testObject.alreadyReceived(5));

    }

    @Test
    void smallerMessageReceived() {

        MessageTracker messageTracker = new MessageTracker();
        messageTracker.setSmallestReceived(3);

        Assertions.assertTrue(messageTracker.alreadyReceived(1));

    }

    @Test
    void addWorks() {

        testObject.addReceived(2);

        Assertions.assertEquals(4, testObject.getSmallestReceived());
        Assertions.assertEquals(6, testObject.getBiggerReceived().first().intValue());

        testObject.addReceived(7);

        Assertions.assertEquals(4, testObject.getSmallestReceived());
        Assertions.assertEquals(6, testObject.getBiggerReceived().first().intValue());
        Assertions.assertTrue(testObject.getBiggerReceived().contains(7));

        testObject.addReceived(5);
        Assertions.assertEquals(7, testObject.getSmallestReceived());
        Assertions.assertTrue(testObject.getBiggerReceived().isEmpty());


    }
}
