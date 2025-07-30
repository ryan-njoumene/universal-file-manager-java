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
    public static <T> CompletableFuture< Result<T> > executeRead(
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
                    return Result.failure( new IOException(dataFormat + " file not found: " + fileToRead.getAbsolutePath()) );
                }
                else{
                    logger.debug("{} Thread {}: Reading {} from {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName());
                    T result = ioOperation.call(); // Execute the actual I/O operation
                    logger.info("{} Thread {}: Finished reading {} from {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName());
                    return Result.success(result);
                }
            } 
            catch (Throwable e) {
                // catch IOException that result from ioOperation.call()
                logger.error("{} Thread {}: Error reading {} from {}: {}", currentThreadType, Thread.currentThread().getName(), dataFormat, fileToRead.getName(), e.getMessage(), e);
                return Result.failure( new FileHandlingException("Failed to read " + dataFormat + " file: " + fileToRead.getName(), e) );
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


    public static <T> CompletableFuture<T> executeCallbacks(
            CompletableFuture<T> future, // Generic type, can be Result<X> or Void or String etc.
            File file,
            String dataFormat,
            String actionExecuted // e.g., "reading", "writing"
    ) {
        String capitalizedActionExecuted = actionExecuted.substring(0,1).toUpperCase() + actionExecuted.substring(1);

        future.whenComplete((result, ex) -> {
            // 'result' here will be the actual value (e.g., Result<Player> for reads, null for writes)

            if (ex == null) {
                // Success path (No Exception Currently Triggered)
                if (result instanceof Result) { // Check if it's a Result<T> (for reads)
                    Result<?> fileResult = (Result<?>) result; // Cast to Result<?> to access isSuccess/isFailure
                    if (fileResult.isSuccess()) {
                        logger.info("Callback (whenComplete): {} {} operation for {} completed successfully! Value type: {}",
                                    dataFormat, capitalizedActionExecuted, file.getName(),
                                    fileResult.getValue().get().getClass().getSimpleName());
                    } else {
                        logger.warn("Callback (whenComplete): {} {} operation for {} completed with failure: {}",
                                    dataFormat, capitalizedActionExecuted, file.getName(),
                                    fileResult.getError().get().getMessage() );
                    }
                } else {
                    // Assume it's a CompletableFuture<Void> (or other simple type) for writes/simple tasks
                    logger.info("Callback (whenComplete): {} {} operation for {} completed successfully.",
                                dataFormat, capitalizedActionExecuted, file.getName());
                }
            } else {
                // Failure path (ex is not null)
                logger.warn("Callback (whenComplete): {} {} operation for {} finished with exception: {}",
                            dataFormat, capitalizedActionExecuted, file.getName(), ex.getMessage());
            }
        });
        return future; // Return the original future, unchanged in type or value
    }    

}
