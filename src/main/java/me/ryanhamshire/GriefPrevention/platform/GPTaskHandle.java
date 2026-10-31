package me.ryanhamshire.GriefPrevention.platform;

/**
 * A handle to a scheduled task, abstracting over Bukkit's integer task ids
 * and Folia's ScheduledTask objects. Used wherever the plugin needs to
 * cancel a pending task or check whether it is still queued (for example
 * the held logout messages and siege checkup timers).
 */
public interface GPTaskHandle
{

    /**
     * Cancels the task if it has not run yet.
     */
    void cancel();

    /**
     * Returns true if the task is still scheduled and has not yet run or
     * been cancelled. Mirrors BukkitScheduler#isQueued semantics.
     */
    boolean isPending();

}
