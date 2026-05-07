package io.alena;

public class Chunk {
    private final byte[] data;
    private final int id;

    public Chunk(byte[] data, int id){
        this.id = id;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public int getId() {
        return id;
    }

}
