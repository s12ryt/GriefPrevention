package me.ryanhamshire.GriefPrevention.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * SPI for the scheduling abstraction. Two implementations exist:
 * <ul>
 *   <li>{@link BukkitBackend} - classic BukkitScheduler (Spigot)</li>
 *   <li>{@link FoliaBackend} - Folia region/entity/global/async schedulers</li>
 * </ul>
 * All delays and periods are expressed in server ticks (20 ticks ~ 1 second),
 * matching BukkitScheduler conventions.
 */
interface SchedulerBackend
{

    void runGlobal(Runnable task);

    GPTaskHandle runGlobalDelayed(Runnable task, long delayTicks);

    void runGlobalTimer(Runnable task, long delayTicks, long periodTicks);

    void runAtLocation(Location location, Runnable task);

    GPTaskHandle runAtLocationDelayed(Location location, Runnable task, long delayTicks);

    void runAtEntity(Entity entity, Runnable task, Runnable retired);

    GPTaskHandle runAtEntityDelayed(Entity entity, Runnable task, long delayTicks, Runnable retired);

    void runAsync(Runnable task);

    void runAsyncDelayed(Runnable task, long delayTicks);

    void runAsyncTimer(Runnable task, long delayTicks, long periodTicks);

    boolean isTickThread();

}
