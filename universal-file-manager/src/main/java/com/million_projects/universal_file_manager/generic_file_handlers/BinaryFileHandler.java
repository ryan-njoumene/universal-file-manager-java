package com.million_projects.universal_file_manager.generic_file_handlers;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// IMAGE, AUDIO, VIDEO
public interface BinaryFileHandler extends UniversalFileHandler{
    CompletableFuture<byte[]> readBytes(ExecutorService executor, File file);
    CompletableFuture<Void> writeBytes(ExecutorService executor, File file, byte[] dataToWrite);
}
