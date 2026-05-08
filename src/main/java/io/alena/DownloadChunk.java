package io.alena;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DownloadChunk implements Runnable {
    private final HttpClient client;
    private final HttpRequest request;
    private final RandomAccessFile file;
    private final long start;

    public DownloadChunk(
            HttpClient client,
            HttpRequest request,
            RandomAccessFile file,
            long start
    ) {
        this.client = client;
        this.request = request;
        this.file = file;
        this.start = start;
    }

    @Override
    public void run() {
        try {
            HttpResponse<byte[]> getResponse =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            if (getResponse.statusCode() < 200 || getResponse.statusCode() >= 300)
                throw new IOException(
                        "Chunk GET request failed with HTTP status: " + getResponse.statusCode()
                );

            synchronized (file) {
                file.seek(start);
                file.write(getResponse.body());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
