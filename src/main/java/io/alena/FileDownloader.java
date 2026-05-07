package io.alena;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class FileDownloader implements Runnable {
    private final String fileUrl;
    private final String outputFile;
    private final int threadCount;
    private final ExecutorService pool;
    private final HttpClient client;

    public FileDownloader(String fileUrl, String outputFile, int threadCount) {
        this.fileUrl = fileUrl;
        this.outputFile = outputFile;
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

        HttpHeaders headers = headResponse.headers();
        Optional<String> contentLength =
                headers.firstValue("Content-Length");
        return contentLength.map(Long::parseLong).orElse(0L);
    }

    public List<Range> calculateRanges(long fileSize) {
        List<Range> ranges = new ArrayList<>();
        long chunkSize = fileSize / threadCount;

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
            long chunkSize = fileSize / threadCount;
            List<Future<Chunk>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                long start = ranges.get(i).start;
                long end = ranges.get(i).end;

                String bytes = "bytes=" + start + "-" + end;
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(fileUrl))
                        .header("Range", bytes)
                        .GET()
                        .build();

                Future<Chunk> future =
                        pool.submit(
                                new DownloadChunk(client, getRequest, i)
                        );

                futures.add(future);
            }

            List<Chunk> chunks = new ArrayList<>();
            for (Future<Chunk> future : futures) {
                chunks.add(future.get());
            }
            chunks.sort(Comparator.comparingInt(Chunk::getId));

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {

                for (Chunk chunk : chunks) {
                    fos.write(chunk.getData());
                }
            }

            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            pool.shutdown();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}