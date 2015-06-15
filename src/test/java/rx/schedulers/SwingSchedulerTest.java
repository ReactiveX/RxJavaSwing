/**
 * Copyright 2014 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.schedulers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.awt.EventQueue;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.functions.Action0;
import rx.functions.Actions;
import rx.internal.schedulers.NewThreadWorker;

/**
 * Executes work on the Swing UI thread.
 * This scheduler should only be used with actions that execute quickly.
 */
public final class SwingSchedulerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testInvalidDelayValues() {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();
        final Action0 action = mock(Action0.class);

        inner.schedulePeriodically(action, -1L, 100L, TimeUnit.SECONDS);

        inner.schedulePeriodically(action, 100L, -1L, TimeUnit.SECONDS);

        exception.expect(IllegalArgumentException.class);
        inner.schedulePeriodically(action, 1L + Integer.MAX_VALUE, 100L, TimeUnit.MILLISECONDS);

        exception.expect(IllegalArgumentException.class);
        inner.schedulePeriodically(action, 100L, 1L + Integer.MAX_VALUE / 1000, TimeUnit.SECONDS);
    }

    @Test
    public void testPeriodicScheduling() throws Exception {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();

        final CountDownLatch latch = new CountDownLatch(4);

        final Action0 innerAction = mock(Action0.class);
        final Action0 action = new Action0() {
            @Override
            public void call() {
                try {
                    innerAction.call();
                    assertTrue(SwingUtilities.isEventDispatchThread());
                } finally {
                    latch.countDown();
                }
            }
        };

        inner.schedulePeriodically(action, 50, 200, TimeUnit.MILLISECONDS);

        if (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            fail("timed out waiting for tasks to execute");
        }

        inner.unsubscribe();
        waitForEmptyEventQueue();
        verify(innerAction, times(4)).call();
    }

    @Test
    public void testNestedActions() throws Exception {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();

        final Action0 firstStepStart = mock(Action0.class);
        final Action0 firstStepEnd = mock(Action0.class);

        final Action0 secondStepStart = mock(Action0.class);
        final Action0 secondStepEnd = mock(Action0.class);

        final Action0 thirdStepStart = mock(Action0.class);
        final Action0 thirdStepEnd = mock(Action0.class);

        final Action0 firstAction = new Action0() {
            @Override
            public void call() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                firstStepStart.call();
                firstStepEnd.call();
            }
        };
        final Action0 secondAction = new Action0() {
            @Override
            public void call() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                secondStepStart.call();
                inner.schedule(firstAction);
                secondStepEnd.call();
            }
        };
        final Action0 thirdAction = new Action0() {
            @Override
            public void call() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                thirdStepStart.call();
                inner.schedule(secondAction);
                thirdStepEnd.call();
            }
        };

        InOrder inOrder = inOrder(firstStepStart, firstStepEnd, secondStepStart, secondStepEnd, thirdStepStart, thirdStepEnd);

        inner.schedule(thirdAction);
        waitForEmptyEventQueue();

// Workers are non-reentrant!
//        inOrder.verify(thirdStepStart, times(1)).call();
//        inOrder.verify(secondStepStart, times(1)).call();
//        inOrder.verify(firstStepStart, times(1)).call();
//        inOrder.verify(firstStepEnd, times(1)).call();
//        inOrder.verify(secondStepEnd, times(1)).call();
//        inOrder.verify(thirdStepEnd, times(1)).call();
        inOrder.verify(thirdStepStart, times(1)).call();
        inOrder.verify(thirdStepEnd, times(1)).call();
        inOrder.verify(secondStepStart, times(1)).call();
        inOrder.verify(secondStepEnd, times(1)).call();
        inOrder.verify(firstStepStart, times(1)).call();
        inOrder.verify(firstStepEnd, times(1)).call();
    }

    private static void waitForEmptyEventQueue() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // nothing to do, we're just waiting here for the event queue to be emptied
            }
        });
    }
    
    @Test(timeout = 30000)
    public void testCancelledTaskRetention() throws InterruptedException {
        System.out.println("Wait before GC");
        Thread.sleep(1000);
        
        System.out.println("GC");
        System.gc();
        
        Thread.sleep(1000);

        
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage();
        long initial = memHeap.getUsed();
        
        System.out.printf("Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        
        Scheduler.Worker w = SwingScheduler.getInstance().createWorker();
        for (int i = 0; i < 250000; i++) {
            if (i % 50000 == 0) {
                System.out.println("  -> still scheduling: " + i);
            }
            w.schedule(Actions.empty(), 1, TimeUnit.DAYS);
        }
        
        memHeap = memoryMXBean.getHeapMemoryUsage();
        long after = memHeap.getUsed();
        System.out.printf("Peak: %.3f MB%n", after / 1024.0 / 1024.0);
        
        w.unsubscribe();
        
        System.out.println("Wait before second GC");
        Thread.sleep(NewThreadWorker.PURGE_FREQUENCY + 2000);
        
        System.out.println("Second GC");
        System.gc();
        
        Thread.sleep(1000);
        
        memHeap = memoryMXBean.getHeapMemoryUsage();
        long finish = memHeap.getUsed();
        System.out.printf("After: %.3f MB%n", finish / 1024.0 / 1024.0);
        
        if (finish > initial * 5) {
            Assert.fail(String.format("Tasks retained: %.3f -> %.3f -> %.3f", initial / 1024 / 1024.0, after / 1024 / 1024.0, finish / 1024 / 1024d));
        }
    }
    
    @Test
    public void testRecursiveScheduling() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger(1000);
        final AtomicBoolean reenter = new AtomicBoolean();
        final CountDownLatch cdl = new CountDownLatch(1);
        
        final Scheduler.Worker w = SwingScheduler.getInstance().createWorker();
        
        Action0 action = new Action0() {
            @Override
            public void call() {
                if (counter.getAndIncrement() == 0) {
                    if (times.decrementAndGet() > 0) {
                        w.schedule(this);
                    } else {
                        cdl.countDown();
                        return;
                    }
                    if (counter.decrementAndGet() == 0) {
                        return;
                    }
                }
                reenter.set(true);
                cdl.countDown();
            }
        };
        
        w.schedule(action, 100, TimeUnit.MILLISECONDS);
        
        cdl.await();
        
        Assert.assertFalse("Reenter detected", reenter.get());
        Assert.assertEquals(0, times.get());
    }
}
