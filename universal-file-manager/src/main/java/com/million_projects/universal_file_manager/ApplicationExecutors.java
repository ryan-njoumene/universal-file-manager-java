package com.million_projects.universal_file_manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApplicationExecutors {
    
        // FIELDS
    private static final Logger logger = LoggerFactory.getLogger(ApplicationExecutors.class);

    //Single Instance (Singleton): static final fields ensure that your X Fields are created only once when the Class is first loaded.
    // ==> created once and reused through the whole application
        // Define a platform thread executor for general use (e.g., CPU-bound or limited I/O)
    public static final ExecutorService PLATFORM_THREAD_EXECUTOR = Executors.newFixedThreadPool(2);
        // Define a virtual thread executor for I/O-bound tasks (Java 21+)
    public static final ExecutorService VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

        // CONSTRUCTOR
    // set to private to prevent instantiation made be external class
    // the class is instanciated once it is called for the first time in the application,
    // the next time, it only reused its existing ExecutorService Fields
    private ApplicationExecutors(){};

    /** A shutdown hook is a Thread that is registered with Runtime.getRuntime().addShutdownHook() and
     * will be executed by the JVM when it begins its shutdown sequence.
     * It Handle the shutdown of the executor when  the application is closed abruptly, 
     * there are many ways a JVM can exit: Normal program completion, User pressing Ctrl+C, System shutdown, 
     * An uncaught exception in a non-daemon thread, Calling System.exit().
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutting down. Initiating ExecutorServices shutdown.");
            shutdownExecutorService(PLATFORM_THREAD_EXECUTOR);
            shutdownExecutorService(VIRTUAL_THREAD_EXECUTOR);
        }));
    }

    /**
     * Gracefully shuts down the ExecutorService.
     * Shut down Only Once the Application is closed.
     */
    public static void shutdownExecutorService(ExecutorService executor) {
        String executorType = (executor == VIRTUAL_THREAD_EXECUTOR) ? "Virtual": "Platform";
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("{} ExecutorService did not terminate gracefully. Forcing shutdown.", executorType);
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS))
                    logger.error("{} ExecutorService did not terminate after forced shutdown.", executorType);
            }
        } catch (InterruptedException ie) {
            logger.error("Shutdown interrupted. Forcing shutdown.", ie);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Application finished.");
    }
    
}
