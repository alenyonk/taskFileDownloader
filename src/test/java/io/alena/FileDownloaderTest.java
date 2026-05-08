package io.alena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloaderTest {

    @Test
    void testChunkRanges() {

        FileDownloader downloader =
                new FileDownloader("url", "testFile", 4);

        // Base Case

        List<Range> ranges =
                downloader.calculateRanges(1000);

        assertEquals(0, ranges.get(0).start);
        assertEquals(249, ranges.get(0).end);

        assertEquals(250, ranges.get(1).start);
        assertEquals(499, ranges.get(1).end);

        assertEquals(500, ranges.get(2).start);
        assertEquals(749, ranges.get(2).end);

        assertEquals(750, ranges.get(3).start);
        assertEquals(999, ranges.get(3).end);

        // Uneven Split

        List<Range> unevenRanges =
                downloader.calculateRanges(1001);

        assertEquals(0, unevenRanges.get(0).start);
        assertEquals(249, unevenRanges.get(0).end);

        assertEquals(250, unevenRanges.get(1).start);
        assertEquals(499, unevenRanges.get(1).end);

        assertEquals(500, unevenRanges.get(2).start);
        assertEquals(749, unevenRanges.get(2).end);

        assertEquals(750, unevenRanges.get(3).start);
        assertEquals(1000, unevenRanges.get(3).end);

        // File size is smaller than number of threads

        List<Range> smallRanges =
                downloader.calculateRanges(3);

        assertEquals(0, smallRanges.get(0).start);
        assertEquals(0, smallRanges.get(0).end);

        assertEquals(1, smallRanges.get(1).start);
        assertEquals(1, smallRanges.get(1).end);

        assertEquals(2, smallRanges.get(2).start);
        assertEquals(2, smallRanges.get(2).end);


    }

}