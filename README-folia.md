# GriefPrevention for Folia

This branch (`folia`) is a Folia-compatible fork of GriefPrevention (baseline: `legacy/v16`, MC 26.3), while keeping full compatibility with Paper and Spigot.

## How it works

The plugin detects the server platform at runtime (see `platform/PlatformDetection.java`):

- **Folia / modern Paper** — all scheduling goes through a platform facade (`platform/FoliaScheduler.java`) which routes tasks to Folia's schedulers:
  - `RegionScheduler` for claim/block work at a known location (claim edits, nature restoration, visualization apply)
  - `EntityScheduler` for player-bound tasks (rescues, messages, cooldown checks, kick/ban)
  - `GlobalRegionScheduler` for server-wide bookkeeping (claim block delivery timers, entity cleanup cadence, logout broadcasts)
  - `AsyncScheduler` for pure computation/IO (nature-restoration computation, custom log writes, unused-claim scans)
  - Modern Paper implements the same scheduler interfaces on its single-threaded core, so the same path is used there.
- **Spigot** — everything falls back to the classic `BukkitScheduler` (`platform/BukkitBackend.java`). No behavioral change from upstream.

`plugin.yml` declares `folia-supported: true`.

## Concurrency hardening

- The master `claims` list and the `chunksToClaimsMap` values are `CopyOnWriteArrayList`, safe for concurrent region-thread reads.
- `heldLogoutMessages` is a `ConcurrentHashMap` keyed by task handles.
- Cancellable tasks use the `GPTaskHandle` abstraction (Bukkit task ids on Spigot, `ScheduledTask` on Folia), preserving the old `isQueued`/`cancelTask` semantics.

## Known limitations (by design of v1)

- Siege checkups run anchored to the sieged claim's region; combatants in different regions are still logically processed from that region thread (matches upstream single-thread semantics, but is not per-combatant-threaded).
- Entity cleanup iterates worlds from the global scheduler and dispatches entity removal through entity/region ownership, but the sweep cadence is global.
- These are conscious trade-offs to keep behavior identical to upstream while being safe to run on Folia.

## Building

```
mvn -B -ntp package
```

Requires JDK 21+ (built and tested with JDK 25). Output: `target/GriefPrevention.jar`.

## Tests

Unit tests cover the scheduler facade delegation and platform detection:

```
mvn -B -ntp test
```
