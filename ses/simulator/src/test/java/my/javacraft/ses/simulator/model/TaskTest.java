package my.javacraft.ses.simulator.model;

import org.junit.Assert;
import org.junit.Test;
import my.javacraft.ses.events.Priority;
import my.javacraft.ses.simulator.db.FinanceDao;

import java.util.PriorityQueue;

/**
 * Created by nikilipa on 7/23/16.
 */
public class TaskTest {

    @Test
    public void testPrioritySort() {

        PriorityQueue<Task> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(new Task(Priority.MINOR, 1, "Task 1", FinanceDao.FINANCE_CODE_SUPPORT, 20));
        priorityQueue.add(new Task(Priority.MAJOR, 2, "Task 2", FinanceDao.FINANCE_CODE_MIGRATION, 50));
        priorityQueue.add(new Task(Priority.BLOCKER, 3, "Task 3", FinanceDao.FINANCE_CODE_GENERAL, 100));
        priorityQueue.add(new Task(Priority.MAJOR, 4, "Task 4", FinanceDao.FINANCE_CODE_GENERAL, 100));

        Assert.assertEquals(Priority.BLOCKER, priorityQueue.poll().getPriority());
        Assert.assertEquals(Priority.MAJOR, priorityQueue.poll().getPriority());
        Assert.assertEquals(Priority.MAJOR, priorityQueue.poll().getPriority());
        Assert.assertEquals(Priority.MINOR, priorityQueue.poll().getPriority());

    }
}
