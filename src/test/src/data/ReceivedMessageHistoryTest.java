package src.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

class ReceivedMessageHistoryTest {

    private ReceivedMessageHistory testObject;

    @BeforeEach
    void init() {
        this.testObject = new ReceivedMessageHistory();

        SortedSet<Integer> set = new TreeSet<>();
        set.add(4);
        set.add(6);
        set.add(3);

        testObject.setSmallest(1);

        testObject.setReceived(set);
    }

    @Test
    void containsWorks() {
        Assertions.assertTrue(testObject.contains(1));
        Assertions.assertTrue(testObject.contains(3));
        Assertions.assertFalse(testObject.contains(5));

    }

    @Test
    void smallerMessageReceived() {

        ReceivedMessageHistory receivedMessageHistory = new ReceivedMessageHistory();
        receivedMessageHistory.setSmallest(3);

        Assertions.assertTrue(receivedMessageHistory.contains(1));

    }

    @Test
    void addWorks() {

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
