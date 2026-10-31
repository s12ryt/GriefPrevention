package me.ryanhamshire.GriefPrevention.platform;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Folia backend using the regionised scheduler API. The same API is also
 * implemented by modern Paper, where everything routes to the main thread,
 * so this backend is safe whenever {@link PlatformDetection#isFolia()} is
 * true. On genuine Folia, location tasks run on the owning region thread,
 * entity tasks on the entity's scheduler, and async tasks off the tick
 * threads.
 */
final class FoliaBackend implements SchedulerBackend
{

    private final Plugin plugin;

    FoliaBackend(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void runGlobal(Runnable task)
    {
        Bukkit.getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public GPTaskHandle runGlobalDelayed(Runnable task, long delayTicks)
    {
        ScheduledTask scheduled = Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        return new FoliaTaskHandle(scheduled);
    }

    @Override
    public void runGlobalTimer(Runnable task, long delayTicks, long periodTicks)
    {
        Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks, periodTicks);
    }

    @Override
    public void runAtLocation(Location location, Runnable task)
    {
        Bukkit.getRegionScheduler().execute(plugin, location, task);
    }

    @Override
    public GPTaskHandle runAtLocationDelayed(Location location, Runnable task, long delayTicks)
    {
        ScheduledTask scheduled = Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, scheduledTask -> task.run(), delayTicks);
        return new FoliaTaskHandle(scheduled);
    }

    @Override
    public void runAtEntity(Entity entity, Runnable task, Runnable retired)
    {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
    }

    @Override
    public GPTaskHandle runAtEntityDelayed(Entity entity, Runnable task, long delayTicks, Runnable retired)
    {
        ScheduledTask scheduled = entity.getScheduler()
                .runDelayed(plugin, scheduledTask -> task.run(), retired, delayTicks);
        return new FoliaTaskHandle(scheduled);
    }

    @Override
    public void runAsync(Runnable task)
    {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runAsyncDelayed(Runnable task, long delayTicks)
    {
        Bukkit.getAsyncScheduler()
                .runDelayed(plugin, scheduledTask -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks)
    {
        Bukkit.getAsyncScheduler()
                .runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTickThread()
    {
        // On Folia this is true for any region or global tick thread.
        return Bukkit.isPrimaryThread();
    }

    /** Handle wrapping a Folia ScheduledTask. */
    private static final class FoliaTaskHandle implements GPTaskHandle
    {

        private final ScheduledTask scheduledTask;

        private FoliaTaskHandle(ScheduledTask scheduledTask)
        {
            this.scheduledTask = scheduledTask;
        }

        @Override
        public void cancel()
        {
            scheduledTask.cancel();
        }

        @Override
        public boolean isPending()
        {
            return !scheduledTask.isCancelled()
                    && scheduledTask.getExecutionState() == ScheduledTask.ExecutionState.IDLE;
        }
    }

}
