package com.million_projects.universal_file_manager.generic_file_handlers;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.million_projects.universal_file_manager.Result;

// TXT, MARKDOWN, LOGS
public interface TextFileHandler extends UniversalFileHandler{
    <T> CompletableFuture< Result<String> > readText(ExecutorService executor, File fileToRead);
    CompletableFuture<Void> writeText(ExecutorService executor, File fileToWrite, String contentToWrite, StandardOpenOption standardOpenOption);
}
