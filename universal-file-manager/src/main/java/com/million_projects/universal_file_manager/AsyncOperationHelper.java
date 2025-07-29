package com.million_projects.universal_file_manager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.million_projects.universal_file_manager.file_handling_exceptions.FileHandlingException;

public final class AsyncOperationHelper {

        // FIELDS
    //Single Instance (Singleton): static final fields ensure that your X Fields are created only once when the Class is first loaded.
    private static final Logger logger = LoggerFactory.getLogger(AsyncOperationHelper.class);

    // prevent instantiation
    private AsyncOperationHelper(){}

        // STATIC METHODS
    public static <T> CompletableFuture<T> executeRead(
            ExecutorService executor,
            File fileToRead,
            String dataFormat,
            Callable<T> ioOperation // Use Callable for the actual I/O logic
    ){
        logger.info("Main Thread: Starting asynchronous file read for {} ({} format) ==> using Async Thread Executor...",
            fileToRead.getName(), dataFormat);

        // CompletableFuture.supplyAsync() return a value
        return CompletableFuture.supplyAsync(() -> {
            String currentThreadType = Thread.currentThread().isVirtual() ? "Virtual" : "Platform";
            try {
                if (!fileToRead.exists()) {
                    logger.error("Error: {} File not found at " + fileToRead.getAbsolutePath(), dataFormat);
                    throw new IOException(dataFormat + " file not found: " + fileToRead.getAbsolutePath());
                }
                else{
                    logger.debug("{} Thread {}: Reading {} from {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName());
                    T result = ioOperation.call(); // Execute the actual I/O operation
                    logger.info("{} Thread {}: Finished reading {} from {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName());
                    return result;
                }
            } 
            catch (Throwable e) {
                // catch IOException that result from ioOperation.call()
                logger.error("{} Thread {}: Error reading {} from {}: {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName(), e.getMessage(), e);
                throw new FileHandlingException("Failed to read " + dataFormat + " file: " + fileToRead.getName(), e);
            }
        }, executor);        
    }

    public static CompletableFuture<Void> executeWrite(
        ExecutorService executor,
        File fileToWrite,
        String dataFormat,
        Object contentToWrite,
        Runnable ioOperation // Use Callable for the actual I/O logic
    ){
        logger.info("Main Thread: Starting asynchronous file write for {} ({} format) ==> using Async Thread Executor...",
            fileToWrite.getName(), dataFormat);

        // CompletableFuture.runAsync() doesn't return value
        CompletableFuture<Void> writefuture = CompletableFuture.runAsync(() -> {
            String currentThreadType = Thread.currentThread().isVirtual() ? "Virtual" : "Platform";
            logger.debug("{} Thread {}: {} Writing {} to {}", currentThreadType, Thread.currentThread().getName(), dataFormat, contentToWrite.getClass().getSimpleName(), fileToWrite.getName());
            ioOperation.run(); // Execute the actual I/O operation
            logger.info("{} Thread {}: Finished {} writing {} to {}", currentThreadType, Thread.currentThread().getName(), dataFormat, contentToWrite.getClass().getSimpleName(), fileToWrite.getName());
        }, executor); 
        return writefuture; // Join to wait for this specific Virtual Thread to complete        
    }

    public static <T> CompletableFuture<T> executeCallback(
        CompletableFuture<T> future,
        File file,
        String dataFormat,
        Object contentToWrite,
        String actionExecuted
    ){
        String capitalizedActionExecuted = actionExecuted.substring(0,1).toUpperCase() + actionExecuted.substring(1);
        // Attach callbacks to the CompletableFuture
        future.thenRun(() -> {
            logger.info("Callback (thenRun): {} file {} operation completed successfully!", contentToWrite.getClass(), actionExecuted);
        }).exceptionally(ex -> {
            logger.warn("Callback: {} file {} operation failed with: {}", file.getName(), actionExecuted, ex.getMessage());
            return null; // Return null to indicate the exception was handled
        }).whenComplete((result, ex) -> {
            // This callback runs whether the task succeeds or fails
            if (ex == null) {
                logger.info("Callback (whenComplete): {} {} Task finished without exception.", dataFormat, capitalizedActionExecuted);
            } else {
                logger.warn("Callback (whenComplete): {} {} Task finished with exception: " + ex.getCause().getMessage(), dataFormat, capitalizedActionExecuted);
            }
        });
        return future; // Join to wait for this specific Virtual Thread to complete
    }    

}
