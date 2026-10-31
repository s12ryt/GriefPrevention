package me.ryanhamshire.GriefPrevention.platform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Classic BukkitScheduler backend for Spigot. All synchronous entry points
 * route to the main thread, which is the correct behaviour on a
 * single-threaded server.
 */
final class BukkitBackend implements SchedulerBackend
{

    private final Plugin plugin;

    BukkitBackend(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void runGlobal(Runnable task)
    {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public GPTaskHandle runGlobalDelayed(Runnable task, long delayTicks)
    {
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks);
        return new BukkitTaskHandle(taskId);
    }

    @Override
    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks)
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runAtLocation(Location location, Runnable task)
    {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public GPTaskHandle runAtLocationDelayed(Location location, Runnable task, long delayTicks)
    {
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks);
        return new BukkitTaskHandle(taskId);
    }

    @Override
    public void runAtEntity(Entity entity, Runnable task, Runnable retired)
    {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public GPTaskHandle runAtEntityDelayed(Entity entity, Runnable task, long delayTicks, Runnable retired)
    {
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks);
        return new BukkitTaskHandle(taskId);
    }

    @Override
    public void runAsync(Runnable task)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runAsyncDelayed(Runnable task, long delayTicks)
    {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks)
    {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public boolean isTickThread()
    {
        return Bukkit.isPrimaryThread();
    }

    /**
     * Handle wrapping a BukkitScheduler integer task id, preserving the
     * isQueued/cancelTask semantics used before the Folia port.
     */
    private static final class BukkitTaskHandle implements GPTaskHandle
    {

        private final int taskId;

        private BukkitTaskHandle(int taskId)
        {
            this.taskId = taskId;
        }

        @Override
        public void cancel()
        {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        @Override
        public boolean isPending()
        {
            return Bukkit.getScheduler().isQueued(taskId);
        }
    }

}
