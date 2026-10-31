package me.ryanhamshire.GriefPrevention.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for platform detection, including the Folia detection added for
 * Folia compatibility. Note: the unit test classpath includes paper-api,
 * which ships the threadedregions classes, so tests assert consistency
 * rather than absolute detection results.
 */
class PlatformDetectionTest
{

    @Test
    void classExistsReturnsTrueForRealClass()
    {
        assertTrue(PlatformDetection.classExists("java.lang.String"));
    }

    @Test
    void classExistsReturnsFalseForMissingClass()
    {
        assertFalse(PlatformDetection.classExists("not.a.RealClassXYZ"));
    }

    @Test
    void getPlatformNeverThrowsAndCachesResult()
    {
        PlatformDetection.Platform first = PlatformDetection.getPlatform();
        assertNotNull(first);
        // Cached: second call returns the same enum instance (value equality).
        assertEquals(first, PlatformDetection.getPlatform());
    }

    @Test
    void foliaDetectionIsConsistentWithPlatform()
    {
        boolean folia = PlatformDetection.isFolia();
        if (folia)
        {
            assertEquals(PlatformDetection.Platform.FOLIA, PlatformDetection.getPlatform());
        }
        else
        {
            assertNotEquals(PlatformDetection.Platform.FOLIA, PlatformDetection.getPlatform());
        }
    }
}
