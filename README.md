# taskFileDownloader 
This repository contains an implementation of a parallel file downloader, that 
 - downloads file in parallel chunks using [ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
 - writes them directly to the output file using [RandomAccessFile](https://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html)

## Repository Structure
- `src/main`: source code of the downloader implementation
- `src/test`: unit test for chunk calculation logic
- `README.md`: this file

## Usage
1. Open this project in [IntelliJ IDEA](https://www.jetbrains.com/idea/) 😊
2. Run 'Main'
