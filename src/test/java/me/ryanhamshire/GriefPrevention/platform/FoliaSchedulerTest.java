package me.ryanhamshire.GriefPrevention.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for the Folia scheduler abstraction facade. Verifies that every
 * public scheduling helper delegates to the configured backend with the
 * correct task, delay (in ticks), location, entity and retired callback.
 */
class FoliaSchedulerTest
{

    private RecordingBackend backend;

    @BeforeEach
    void setUp()
    {
        backend = new RecordingBackend();
        FoliaScheduler.setBackend(backend);
    }

    @AfterEach
    void tearDown()
    {
        FoliaScheduler.resetBackend();
    }

    @Test
    void runGlobalDelegatesToBackend()
    {
        Runnable task = () -> { };
        FoliaScheduler.runGlobal(task);
        assertEquals("runGlobal", backend.lastMethod);
        assertSame(task, backend.lastTask);
    }

    @Test
    void runGlobalDelayedReturnsHandleAndPassesDelay()
    {
        Runnable task = () -> { };
        GPTaskHandle handle = FoliaScheduler.runGlobalDelayed(task, 1200L);
        assertEquals("runGlobalDelayed", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertEquals(1200L, backend.lastDelay);
        assertNotNull(handle);
    }

    @Test
    void runGlobalTimerPassesDelayAndPeriod()
    {
        Runnable task = () -> { };
        FoliaScheduler.runGlobalTimer(task, 100L, 200L);
        assertEquals("runGlobalTimer", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertEquals(100L, backend.lastDelay);
        assertEquals(200L, backend.lastPeriod);
    }

    @Test
    void runAtLocationPassesLocation()
    {
        Runnable task = () -> { };
        Location location = new Location(null, 1, 2, 3);
        FoliaScheduler.runAtLocation(location, task);
        assertEquals("runAtLocation", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertSame(location, backend.lastLocation);
    }

    @Test
    void runAtLocationDelayedPassesLocationAndDelay()
    {
        Runnable task = () -> { };
        Location location = new Location(null, 4, 5, 6);
        GPTaskHandle handle = FoliaScheduler.runAtLocationDelayed(location, task, 42L);
        assertEquals("runAtLocationDelayed", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertSame(location, backend.lastLocation);
        assertEquals(42L, backend.lastDelay);
        assertNotNull(handle);
    }

    @Test
    void runAtEntityPassesEntity()
    {
        Runnable task = () -> { };
        Entity entity = mock(Entity.class);
        FoliaScheduler.runAtEntity(entity, task);
        assertEquals("runAtEntity", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertSame(entity, backend.lastEntity);
    }

    @Test
    void runAtEntityDelayedHandleReturnsHandle()
    {
        Runnable task = () -> { };
        Runnable retired = () -> { };
        Entity entity = mock(Entity.class);
        GPTaskHandle handle = FoliaScheduler.runAtEntityDelayedHandle(entity, task, 200L, retired);
        assertEquals("runAtEntityDelayed", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertSame(entity, backend.lastEntity);
        assertEquals(200L, backend.lastDelay);
        assertSame(retired, backend.lastRetired);
        assertNotNull(handle);
    }

    @Test
    void runAsyncDelegatesToBackend()
    {
        Runnable task = () -> { };
        FoliaScheduler.runAsync(task);
        assertEquals("runAsync", backend.lastMethod);
        assertSame(task, backend.lastTask);
    }

    @Test
    void runAsyncDelayedPassesDelay()
    {
        Runnable task = () -> { };
        FoliaScheduler.runAsyncDelayed(task, 60L);
        assertEquals("runAsyncDelayed", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertEquals(60L, backend.lastDelay);
    }

    @Test
    void runAsyncTimerPassesDelayAndPeriod()
    {
        Runnable task = () -> { };
        FoliaScheduler.runAsyncTimer(task, 0L, 6000L);
        assertEquals("runAsyncTimer", backend.lastMethod);
        assertSame(task, backend.lastTask);
        assertEquals(0L, backend.lastDelay);
        assertEquals(6000L, backend.lastPeriod);
    }

    @Test
    void isTickThreadDelegatesToBackend()
    {
        backend.tickThread = true;
        assertTrue(FoliaScheduler.isTickThread());
        backend.tickThread = false;
        assertFalse(FoliaScheduler.isTickThread());
    }

    @Test
    void cancelledHandleIsNoLongerPending()
    {
        GPTaskHandle handle = new PendingHandle();
        assertTrue(handle.isPending());
        handle.cancel();
        assertFalse(handle.isPending());
    }

    /** Simple always-pending-until-cancelled handle implementation for tests. */
    private static final class PendingHandle implements GPTaskHandle
    {
        private boolean cancelled = false;

        @Override
        public void cancel()
        {
            cancelled = true;
        }

        @Override
        public boolean isPending()
        {
            return !cancelled;
        }
    }

    /** Records the most recent backend invocation for assertions. */
    private static final class RecordingBackend implements SchedulerBackend
    {
        String lastMethod = null;
        Runnable lastTask = null;
        Runnable lastRetired = null;
        Location lastLocation = null;
        Entity lastEntity = null;
        long lastDelay = -1L;
        long lastPeriod = -1L;
        boolean tickThread = false;

        @Override
        public void runGlobal(Runnable task)
        {
            lastMethod = "runGlobal";
            lastTask = task;
        }

        @Override
        public GPTaskHandle runGlobalDelayed(Runnable task, long delayTicks)
        {
            lastMethod = "runGlobalDelayed";
            lastTask = task;
            lastDelay = delayTicks;
            return new PendingHandle();
        }

        @Override
        public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks)
        {
            lastMethod = "runGlobalTimer";
            lastTask = task;
            lastDelay = delayTicks;
            lastPeriod = periodTicks;
        }

        @Override
        public void runAtLocation(Location location, Runnable task)
        {
            lastMethod = "runAtLocation";
            lastTask = task;
            lastLocation = location;
        }

        @Override
        public GPTaskHandle runAtLocationDelayed(Location location, Runnable task, long delayTicks)
        {
            lastMethod = "runAtLocationDelayed";
            lastTask = task;
            lastLocation = location;
            lastDelay = delayTicks;
            return new PendingHandle();
        }

        @Override
        public void runAtEntity(Entity entity, Runnable task, Runnable retired)
        {
            lastMethod = "runAtEntity";
            lastTask = task;
            lastEntity = entity;
            lastRetired = retired;
        }

        @Override
        public GPTaskHandle runAtEntityDelayed(Entity entity, Runnable task, long delayTicks, Runnable retired)
        {
            lastMethod = "runAtEntityDelayed";
            lastTask = task;
            lastEntity = entity;
            lastDelay = delayTicks;
            lastRetired = retired;
            return new PendingHandle();
        }

        @Override
        public void runAsync(Runnable task)
        {
            lastMethod = "runAsync";
            lastTask = task;
        }

        @Override
        public void runAsyncDelayed(Runnable task, long delayTicks)
        {
            lastMethod = "runAsyncDelayed";
            lastTask = task;
            lastDelay = delayTicks;
        }

        @Override
        public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks)
        {
            lastMethod = "runAsyncTimer";
            lastTask = task;
            lastDelay = delayTicks;
            lastPeriod = periodTicks;
        }

        @Override
        public boolean isTickThread()
        {
            return tickThread;
        }
    }
}
