package io.alena;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

public class DownloadChunk implements Callable<Chunk> {
    private final HttpClient client;
    private final HttpRequest request;
    private Chunk chunk = null;
    private final int id;

    public DownloadChunk(
            HttpClient client,
            HttpRequest request,
            int id
    ) {
        this.client = client;
        this.request = request;
        this.id = id;
    }

    @Override
    public Chunk call() throws Exception {

        HttpResponse<byte[]> getResponse =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        if (getResponse.statusCode() < 200 || getResponse.statusCode() >= 300) {
            throw new IOException(
                    "Chunk GET request failed with HTTP status: " + getResponse.statusCode()
            );
        }


        return new Chunk(getResponse.body(), id);
    }
}
