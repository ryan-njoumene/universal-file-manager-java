package com.million_projects.universal_file_manager.specialized_file_handlers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.million_projects.universal_file_manager.AsyncOperationHelper;
import com.million_projects.universal_file_manager.generic_file_handlers.ObjectFileHandler;

public class JsonFileHandler implements ObjectFileHandler{
        // FIELDS
    private static final Logger logger = LoggerFactory.getLogger(JsonFileHandler.class);
    private final ObjectMapper objectMapper;

    private final static String DATA_FORMAT = "JSON";
    private final static String FILE_EXTENSION = ".json";

        // CONSTRUCTOR
    public JsonFileHandler(){
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

        // IMPLEMENTATIONS
    @Override
    public boolean canHandle(File file){
        boolean isJsonFile = file.getName().toLowerCase().endsWith(FILE_EXTENSION);
        return isJsonFile;
    }

    @Override
    public <T> CompletableFuture<T> read(ExecutorService executor, File fileToRead, Object option){
        return readObject(executor, fileToRead, (Class<T>) option);
    }

    @Override
    public CompletableFuture<Void> write(ExecutorService executor, File fileToWrite, Object contentToWrite, Object option){
        return writeObject(executor, fileToWrite, contentToWrite);
    }

    @Override
    public <T> CompletableFuture<T> readObject(ExecutorService executor, File fileToRead, Class<T> classToDeserialize){
        CompletableFuture<T> readfuture = deserializeObject(executor, fileToRead, classToDeserialize);
        readfuture = AsyncOperationHelper.executeCallback(readfuture, fileToRead, DATA_FORMAT, classToDeserialize, "reading");
        return readfuture;
    }

    @Override
    public CompletableFuture<Void> writeObject(ExecutorService executor, File fileToWrite, Object objectToWrite){
        CompletableFuture<Void> writeFuture = serializeObject(executor, fileToWrite, objectToWrite);
        writeFuture = AsyncOperationHelper.executeCallback(writeFuture, fileToWrite, DATA_FORMAT, objectToWrite, "writing");
        return writeFuture;
    };

        // INSTANCE METHODS
    // ==> Object Deserialization
    /**
     * Asynchronously deserializes a JSON file into a Java object of the specified type.
     * This method is specifically for JSON files.
     *
     * @param fileToRead The File object representing the JSON file.
     * @param classToDeserialize The Class object representing the object type for deserialization.
     * @param <T> The type of the object to deserialize.
     * @return A CompletableFuture that will contain the deserialized object, or complete exceptionally.
     */
    public <T> CompletableFuture<T> deserializeObject(ExecutorService executor, File fileToRead, Class<T> classToDeserialize){
        return AsyncOperationHelper.executeRead(executor, fileToRead, DATA_FORMAT, () -> {
            // This is the specific I/O operation for JSON deserialization
            return objectMapper.readValue(fileToRead, classToDeserialize);
        });
    }
    
    // ==> Object Serialization
    private CompletableFuture<Void> serializeObject(ExecutorService executor, File fileToWrite, Object objectToWrite){
        return AsyncOperationHelper.executeWrite(executor, fileToWrite, DATA_FORMAT, objectToWrite, () -> {
            // This is the specific I/O operation for JSON serialization
            try{
                objectMapper.writeValue(fileToWrite, objectToWrite);
            } catch( IOException e){
                logger.error("Async Thread {}: Error {} writing {} to {}: {}", Thread.currentThread().getName(), DATA_FORMAT, objectToWrite.getClass().getSimpleName(), fileToWrite.getName(), e.getMessage(), e);
                throw new RuntimeException("Failed to write " + DATA_FORMAT + " file: " + fileToWrite.getName(), e);
            }
        });
    }
}
