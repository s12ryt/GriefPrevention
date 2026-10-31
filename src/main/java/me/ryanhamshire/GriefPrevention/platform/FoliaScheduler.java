package me.ryanhamshire.GriefPrevention.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Platform-agnostic scheduling facade for GriefPrevention.
 * <p>
 * On Folia (and modern Paper, which implements the same scheduler API) tasks
 * are routed through the region, entity, global and async schedulers so that
 * world and entity access happens on the owning region thread. On Spigot
 * everything falls back to the classic BukkitScheduler.
 * <p>
 * Routing conventions:
 * <ul>
 *   <li>{@code runGlobal*} - work that touches no world state, or iterates
 *       thread-safe server state (configuration, broadcasts, bookkeeping)</li>
 *   <li>{@code runAtLocation*} - work that reads or writes blocks/claims at a
 *       known location (claim edits, nature restoration, visualisations)</li>
 *   <li>{@code runAtEntity*} - work tied to a specific player or entity
 *       (teleports, messages, cooldown checks)</li>
 *   <li>{@code runAsync*} - pure computation and IO that touches no world or
 *       entity state (log writes, file scans)</li>
 * </ul>
 * All delays and periods are in server ticks (20 ticks ~ 1 second).
 * <p>
 * This class must be initialised from {@code onEnable} via {@link #init}
 * before any scheduling call is made.
 */
public final class FoliaScheduler
{

    private static volatile SchedulerBackend backend;

    private FoliaScheduler()
    {
    }

    /**
     * Initialises the scheduler facade, selecting the backend for the
     * detected platform. Called once from onEnable.
     *
     * @param plugin the plugin instance owning the scheduled tasks
     */
    public static void init(Plugin plugin)
    {
        backend = PlatformDetection.isFolia()
                ? new FoliaBackend(plugin)
                : new BukkitBackend(plugin);
    }

    /** Replaces the active backend (unit tests only). */
    static void setBackend(SchedulerBackend newBackend)
    {
        backend = newBackend;
    }

    /** Clears the active backend (unit tests only). */
    static void resetBackend()
    {
        backend = null;
    }

    private static SchedulerBackend backend()
    {
        SchedulerBackend current = backend;
        if (current == null)
        {
            throw new IllegalStateException("FoliaScheduler used before init(Plugin)");
        }
        return current;
    }

    /**
     * @return true when the Folia scheduler API is available (Folia/modern Paper)
     */
    public static boolean isFolia()
    {
        return PlatformDetection.isFolia();
    }

    /**
     * @return true if the current thread is allowed to touch server tick state
     *         (main thread on Spigot, any region/global tick thread on Folia)
     */
    public static boolean isTickThread()
    {
        return backend().isTickThread();
    }

    // ------------------------------------------------------------------
    // Global tasks
    // ------------------------------------------------------------------

    /** Runs the task on the global scheduler (main thread on Spigot). */
    public static void runGlobal(Runnable task)
    {
        backend().runGlobal(task);
    }

    /** Runs the task later on the global scheduler, returning a cancellable handle. */
    public static GPTaskHandle runGlobalDelayed(Runnable task, long delayTicks)
    {
        return backend().runGlobalDelayed(task, delayTicks);
    }

    /** Runs the task repeatedly on the global scheduler. */
    public static void runGlobalTimer(Runnable task, long delayTicks, long periodTicks)
    {
        backend().runGlobalTimer(task, delayTicks, periodTicks);
    }

    // ------------------------------------------------------------------
    // Location-owning tasks
    // ------------------------------------------------------------------

    /** Runs the task on the scheduler owning the given location. */
    public static void runAtLocation(Location location, Runnable task)
    {
        backend().runAtLocation(location, task);
    }

    /** Runs the task later on the scheduler owning the given location, returning a cancellable handle. */
    public static GPTaskHandle runAtLocationDelayed(Location location, Runnable task, long delayTicks)
    {
        return backend().runAtLocationDelayed(location, task, delayTicks);
    }

    // ------------------------------------------------------------------
    // Entity-owning tasks
    // ------------------------------------------------------------------

    /** Runs the task on the entity's scheduler. */
    public static void runAtEntity(Entity entity, Runnable task)
    {
        backend().runAtEntity(entity, task, null);
    }

    /** Runs the task later on the entity's scheduler. */
    public static void runAtEntityDelayed(Entity entity, Runnable task, long delayTicks)
    {
        backend().runAtEntityDelayed(entity, task, delayTicks, null);
    }

    /**
     * Runs the task later on the entity's scheduler, returning a cancellable
     * handle. If the entity is removed before the task runs, {@code retired}
     * executes instead (Folia semantics; not invoked on Spigot).
     */
    public static GPTaskHandle runAtEntityDelayedHandle(Entity entity, Runnable task, long delayTicks, Runnable retired)
    {
        return backend().runAtEntityDelayed(entity, task, delayTicks, retired);
    }

    // ------------------------------------------------------------------
    // Async tasks
    // ------------------------------------------------------------------

    /** Runs the task off the tick threads. Must not touch world or entity state. */
    public static void runAsync(Runnable task)
    {
        backend().runAsync(task);
    }

    /** Runs the task later off the tick threads. Must not touch world or entity state. */
    public static void runAsyncDelayed(Runnable task, long delayTicks)
    {
        backend().runAsyncDelayed(task, delayTicks);
    }

    /** Runs the task repeatedly off the tick threads. Must not touch world or entity state. */
    public static void runAsyncTimer(Runnable task, long delayTicks, long periodTicks)
    {
        backend().runAsyncTimer(task, delayTicks, periodTicks);
    }

}
