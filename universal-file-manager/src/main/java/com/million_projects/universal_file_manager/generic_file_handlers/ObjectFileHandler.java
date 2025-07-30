package com.million_projects.universal_file_manager.generic_file_handlers;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.million_projects.universal_file_manager.Result;

// JSON, CSV, XML, YAML, PROPERTIES, PROTOCOL_BUFFER
public interface ObjectFileHandler extends UniversalFileHandler {
    public <T> CompletableFuture< Result<T> > readObject(ExecutorService executor, File fileToRead, Class<T> classToDeserialize);
    public CompletableFuture<Void> writeObject(ExecutorService executor, File fileToWrite, Object objectToWrite);
}

/*
 * For JSON, you use new ObjectMapper().

For XML, you use new XmlMapper() (which extends ObjectMapper).

For YAML, you use new YAMLMapper() (which extends ObjectMapper).

For Properties, you use new JavaPropsMapper() (which extends ObjectMapper).
 */
