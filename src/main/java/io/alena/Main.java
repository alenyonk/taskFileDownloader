package io.alena;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main( String[] args){
        String fileUrl = "http://localhost:8080/file.txt.txt";
        String outputFile = "output.txt";

        FileDownloader downloader = new FileDownloader(
                fileUrl,
                outputFile,
                4 // number of parallel threads
        );

        try {
            downloader.run();
            System.out.println("Download completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
