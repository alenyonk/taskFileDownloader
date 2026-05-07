package io.alena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloaderTest {

    @Test
    void testChunkRanges() {

        FileDownloader downloader =
                new FileDownloader("url", "file", 4);

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
    }

}