package com.million_projects.universal_file_manager.generic_file_handlers;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.million_projects.universal_file_manager.Result;

public interface UniversalFileHandler {
    public boolean canHandle(File file);
    public <T> CompletableFuture< Result<T> > read(ExecutorService executor, File fileToRead, Class<?> targetClass);
    public CompletableFuture<Void> write(ExecutorService executor, File fileToWrite, Object contentToWrite, Object option); 
}
