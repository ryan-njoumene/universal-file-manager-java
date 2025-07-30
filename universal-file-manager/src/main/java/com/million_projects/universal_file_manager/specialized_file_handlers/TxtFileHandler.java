package com.million_projects.universal_file_manager.specialized_file_handlers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.million_projects.universal_file_manager.AsyncOperationHelper;
import com.million_projects.universal_file_manager.Result;
import com.million_projects.universal_file_manager.generic_file_handlers.TextFileHandler;

public class TxtFileHandler implements TextFileHandler{
        // FIELDS
    private static final Logger logger = LoggerFactory.getLogger(TxtFileHandler.class);

    private final static String DATA_FORMAT = "TXT";
    private final static String FILE_EXTENSION = ".txt";
    private final List<String> otherFileExtensionSupported;
    public List<String> getOtherFileExtensionSupported(){ return this.otherFileExtensionSupported; }

        // CONSTRUCTOR
    public TxtFileHandler(){
        this.otherFileExtensionSupported = new ArrayList<>();
        this.otherFileExtensionSupported.add(".md");
        this.otherFileExtensionSupported.add(".log");
    }

        // IMPLEMENTATIONS
    @Override
    public boolean canHandle(File file){
        String fileName = file.getName().toLowerCase();
        boolean isTxtFile = fileName.endsWith(FILE_EXTENSION);

        if(this.otherFileExtensionSupported != null){
            for (String fileExtension : otherFileExtensionSupported) {
                if(fileName.endsWith(fileExtension)){ return true;}
            }
        }
        return isTxtFile;
    }

    @Override
    public <T> CompletableFuture< Result<T> > read(ExecutorService executor, File fileToRead, Class<?> targetClass){
        // Enforce that TextFileHandler can only read into String.class or Object.class
        if(targetClass != String.class && targetClass != Object.class){
            logger.error("TextFileHandler cannot read file {} into targetClass {}. Only String.class or Object.class is supported.", fileToRead.getName(), targetClass.getSimpleName());
            return CompletableFuture.failedFuture(new IllegalArgumentException("TextFileHandler can only read into String.class or Object.class, but received " + targetClass.getSimpleName()));
        }
        CompletableFuture<Result<T>> future = AsyncOperationHelper.executeRead(
            executor,
            fileToRead,
            DATA_FORMAT,
            () -> (T) Files.readString(Paths.get(fileToRead.getAbsolutePath()), StandardCharsets.UTF_8)
        );
        return AsyncOperationHelper.executeCallbacks(future, fileToRead, DATA_FORMAT, "reading");
    }

    @Override
    public CompletableFuture<Void> write(ExecutorService executor, File fileToWrite, Object contentToWrite, Object option){
        // Enforce that TextFileHandler can only read into String.class or Object.class
        if (!(contentToWrite instanceof String)) {
            logger.error("TextFileHandler received non-String content for writing: {}", contentToWrite.getClass().getSimpleName());
            return CompletableFuture.failedFuture(new IllegalArgumentException("TextFileHandler can only write String content."));
        }
        return writeText(executor, fileToWrite, (String) contentToWrite, (StandardOpenOption) option);
    }

    /**
     * Asynchronously reads the entire content of a text file (like Markdown, TXT) into a String.
     * This method is suitable for plain text files.
     *
     * @param fileToRead The File object representing the text file.
     * @return A CompletableFuture that will contain the file content as a String, or complete exceptionally.
     */
    @Override
    public CompletableFuture< Result<String> > readText(ExecutorService executor, File fileToRead){
        CompletableFuture< Result<String> > readfuture = readingStringFromFile(executor, fileToRead);
        readfuture = AsyncOperationHelper.executeCallbacks(readfuture, fileToRead, DATA_FORMAT, "reading");
        return readfuture;
    }

    @Override
    public CompletableFuture<Void> writeText(ExecutorService executor, File fileToWrite, String contentToWrite, StandardOpenOption standardOpenOption){
        CompletableFuture<Void> writeFuture = writingStringFromFile(executor, fileToWrite, contentToWrite, standardOpenOption);
        writeFuture = AsyncOperationHelper.executeCallbacks(writeFuture, fileToWrite, DATA_FORMAT, "writing");
        return writeFuture;
    };

        // INSTANCE METHODS
    // ==> String Reading
    public <T> CompletableFuture< Result<String> > readingStringFromFile(ExecutorService executor, File fileToRead){
        return AsyncOperationHelper.executeRead(executor, fileToRead, DATA_FORMAT, () -> {
            // This is the specific I/O operation for Reading a String from a Txt file
            return Files.readString(Paths.get(fileToRead.getAbsolutePath()), StandardCharsets.UTF_8);
        });
    }
    
        // ==> String Writing
    private CompletableFuture<Void> writingStringFromFile(ExecutorService executor, File fileToWrite, String contentToWrite, StandardOpenOption standardOpenOption){
        return AsyncOperationHelper.executeWrite(executor, fileToWrite, DATA_FORMAT, contentToWrite, () -> {
            // This is the specific I/O operation for Writing a String in a Txt file
            try{
                Files.writeString(Paths.get(fileToWrite.getAbsolutePath()), contentToWrite, StandardCharsets.UTF_8, standardOpenOption);
            } catch( IOException e){
                logger.error("Async Thread {}: Error {} writing {} to {}: {}", Thread.currentThread().getName(), DATA_FORMAT, contentToWrite.getClass().getSimpleName(), fileToWrite.getName(), e.getMessage(), e);
                throw new RuntimeException("Failed to write " + DATA_FORMAT + " file: " + fileToWrite.getName(), e);
            }
        });
    }
}
