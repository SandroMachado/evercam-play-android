package io.evercam.androidapp.test.video;

import junit.framework.TestCase;

import io.evercam.androidapp.video.SnapshotManager;

public class SnapshotManagerTest extends TestCase
{
    private final String[] ALL_FILES_ARRAY = {"hello_20131231_240802.png",
            "hello_20110101_000000.jpg", "hello_20141212_120000.jpg", "hello_20141212_120108.png"};

    public void testGetLatestFileName()
    {
        assertEquals("hello_20141212_120108.png", SnapshotManager.getLatestFileName(ALL_FILES_ARRAY));
    }

}
