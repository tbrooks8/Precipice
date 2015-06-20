package net.uncontended.beehive.concurrent;

import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by timbrooks on 12/3/14.
 */
public class ActionThreadPoolTest {

    private ActionThreadPool threadPool;

    @After
    public void tearDown() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    @Test
    public void testPoolRequiresAtLeastOneThread() {
        try {
            threadPool = new ActionThreadPool("Test Action", 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot have fewer than 1 thread", e.getMessage());
        }
    }

    @Test
    public void testPoolPrioritizesFreeThreadsAndExecutes() {
        threadPool = new ActionThreadPool("Test Action", 2);

        final List<String> resultList = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 20; ++i) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    resultList.add(Thread.currentThread().getName());
                }
            });

        }

        while (resultList.size() != 20) {
        }

        int threadZeroCount = 0;
        int threadOneCount = 0;
        for (String threadName : resultList) {
            if ("Test Action-0".equals(threadName)) {
                ++threadZeroCount;
            }
            if ("Test Action-1".equals(threadName)) {
                ++threadOneCount;
            }
        }

        assertEquals(10, threadZeroCount);
        assertEquals(10, threadOneCount);

    }

    @Test
    public void signallyATaskCompleteFreesUpAThread() {
        threadPool = new ActionThreadPool("Test Action", 2);

        final List<String> resultList = new CopyOnWriteArrayList<>();
        Runnable action = new Runnable() {
            @Override
            public void run() {
                resultList.add(Thread.currentThread().getName());
            }
        };

        threadPool.execute(action);
        threadPool.execute(action);
        threadPool.execute(action);

        while (resultList.size() != 3) {
        }

    }
}
