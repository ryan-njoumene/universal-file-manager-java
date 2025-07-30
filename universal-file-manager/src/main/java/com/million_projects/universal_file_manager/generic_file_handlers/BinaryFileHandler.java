package com.million_projects.universal_file_manager.generic_file_handlers;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.million_projects.universal_file_manager.Result;

// IMAGE, AUDIO, VIDEO
public interface BinaryFileHandler extends UniversalFileHandler{
    <T> CompletableFuture< Result<T> > readBytes(ExecutorService executor, File file);
    CompletableFuture<Void> writeBytes(ExecutorService executor, File file, byte[] dataToWrite);
}
