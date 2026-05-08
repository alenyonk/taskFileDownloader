package io.alena;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class FileDownloader implements Runnable {
    private final String fileUrl;
    private int threadCount;
    private final ExecutorService pool;
    private final RandomAccessFile file;
    private final HttpClient client;

    public FileDownloader(String fileUrl, String outputFile, int threadCount) {
        this.fileUrl = fileUrl;
        try {
            this.file = new RandomAccessFile(outputFile, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.threadCount = threadCount;

        this.pool = Executors.newFixedThreadPool(threadCount);
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    private long getFileSize() throws IOException, InterruptedException {
        HttpRequest headRequest = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> headResponse =
                client.send(
                        headRequest,
                        HttpResponse.BodyHandlers.discarding()
                );

        if (headResponse.statusCode() < 200 || headResponse.statusCode() >= 300) {
            throw new IOException(
                    "HEAD request failed with HTTP status: " + headResponse.statusCode()
            );
        }

        HttpHeaders headers = headResponse.headers();

        Optional<String> acceptRanges =
                headers.firstValue("Accept-Ranges");
        if (acceptRanges.isEmpty() || !acceptRanges.get().equals("bytes")){
            throw new IllegalStateException("Server does not accept byte ranges.");
        }

        Optional<String> contentLength =
                headers.firstValue("Content-Length");
        return contentLength.map(Long::parseLong).orElse(0L);
    }

    public List<Range> calculateRanges(long fileSize) {
        List<Range> ranges = new ArrayList<>();
        long chunkSize = fileSize / threadCount;

        if (chunkSize == 0){ // number of threads is bigger than file size
            for (int i = 0; i < fileSize; i++) {
                ranges.add(new Range(i, i));
            }
            return ranges;
        }

        for (int i = 0; i < threadCount; i++) {
            long start = i * chunkSize;
            long end;
            if (i == threadCount - 1) {
                end = fileSize - 1;
            } else {
                end = start + chunkSize - 1;
            }
            ranges.add(new Range(start, end));
        }

        return ranges;
    }

    @Override
    public void run() { // run the service
        try {
            long fileSize = getFileSize();
            List<Range> ranges = calculateRanges(fileSize);
            threadCount = Math.min(threadCount, Math.toIntExact(fileSize));

            for (int i = 0; i < threadCount; i++) {
                long start = ranges.get(i).start;
                long end = ranges.get(i).end;

                String bytes = "bytes=" + start + "-" + end;
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(fileUrl))
                        .header("Range", bytes)
                        .GET()
                        .build();

                pool.submit(
                        new DownloadChunk(client, getRequest, file, start)
                );

            }

        } catch (IOException | InterruptedException ex) {
            pool.shutdown();
            throw new RuntimeException(ex);
        }
    }

}