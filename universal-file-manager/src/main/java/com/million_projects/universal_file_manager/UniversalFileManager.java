package com.million_projects.universal_file_manager;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.million_projects.universal_file_manager.file_handling_exceptions.FileHandlingException;
import com.million_projects.universal_file_manager.file_handling_exceptions.NoSuitableFileHandlerException;
import com.million_projects.universal_file_manager.generic_file_handlers.BinaryFileHandler;
import com.million_projects.universal_file_manager.generic_file_handlers.ObjectFileHandler;
import com.million_projects.universal_file_manager.generic_file_handlers.TextFileHandler;
import com.million_projects.universal_file_manager.generic_file_handlers.UniversalFileHandler;
import com.million_projects.universal_file_manager.specialized_file_handlers.JsonFileHandler;
import com.million_projects.universal_file_manager.specialized_file_handlers.TxtFileHandler;

// To avoid Any SRP violation in UniversalFileManager framework, heplers and handlers methods were refactored 
// into a new class or type that exposes the composite functionality.

// A Violation of the Single Responsibility Principle (SRP) occurs when a class or module has more than one reason to change,
// responsible for more than one distinct aspect of the functionality. 
// Essentially, a class should have only one job or responsibility. 

// Central The Facade/Registry
public class UniversalFileManager {

        // FIELDS
    //Single Instance (Singleton): static final fields ensure that your X Fields are created only once when the Class is first loaded.
    private static final Logger logger = LoggerFactory.getLogger(UniversalFileManager.class);
    // Registry for all Specialized FileHandler Available
    private final List<UniversalFileHandler> handlers = new ArrayList<>();
    
        // CONSTRUCTOR
    public UniversalFileManager(){}
    public UniversalFileManager(boolean registerAllFileHandlers ){
        // Interface Segregation Principle (ISP) ==>
        // A client should not be forced to depend on methods they don't use.
        // In simpler terms, interfaces should be specific to the needs of the classes that implement them,
        // and no class should be burdened with methods it doesn't need.
        registerFileHandler(new TxtFileHandler());
        registerFileHandler(new JsonFileHandler());
        // Add other handlers here as you implement them (e.g., XmlFileHandlerImpl, AudioFileHandlerImpl)
    }

    public final void registerFileHandler(UniversalFileHandler handler) {
        this.handlers.add(handler);
        logger.info("Registered File Handler: {}", handler.getClass().getSimpleName());
    }

    /*
     * Strategy Pattern combined with a Registry/Factory approach, implemented elegantly using Java Streams 
     * Stream API used to filter and find a matching strategy (in this case, a FileHandler).
     */

        // INSTANCE METHODS
    // TextFileHandler support ==> TXT, MARKDOWN, LOGS
    /**
     * Reads text content from a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param file The file to read.
     * @return A CompletableFuture containing the file content as a String.
     * @throws NoSuitableFileHandlerException if no handler can read the file as text.
     */
    public <T> CompletableFuture< Result<String> > readTextFromFile(ExecutorService executor, File fileToRead) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<TextFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof TextFileHandler)
                .map(h -> (TextFileHandler) h)
                .filter(h -> h.canHandle(fileToRead))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().readText(executor, fileToRead);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable text file handler found for file: " + fileToRead.getName())
            );
        }
    }

    /**
     * Writes text content to a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param fileToWrite The file to write to.
     * @param contentToWrite The string content to write.
     * @return A CompletableFuture<Void> indicating completion.
     * @throws NoSuitableFileHandlerException if no handler can write the file as text.
     */
    public CompletableFuture<Void> writeTextToFile(ExecutorService executor, File fileToWrite, String contentToWrite, StandardOpenOption standardOpenOption) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<TextFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof TextFileHandler)
                .map(h -> (TextFileHandler) h)
                .filter(h -> h.canHandle(fileToWrite))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().writeText(executor, fileToWrite, contentToWrite, standardOpenOption);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable text file handler found for file: " + fileToWrite.getName())
            );
        }
    }


    // ObjectFileHandler support ==> JSON, CSV, XML, YAML, PROPERTIES, PROTOCOL BUFFER
    /**
     * Reads an object from a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param file The file to read.
     * @param classToDeserialize The class to deserialize the object into.
     * @param <T> The type of the object.
     * @return A CompletableFuture containing the deserialized object.
     * @throws NoSuitableFileHandlerException if no handler can read the file as an object.
     */
    public <T> CompletableFuture< Result<T> > readObjectFromFile(ExecutorService executor, File fileToRead, Class<T> classToDeserialize) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<ObjectFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof ObjectFileHandler)
                .map(h -> (ObjectFileHandler) h)
                .filter(h -> h.canHandle(fileToRead))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().readObject(executor, fileToRead, classToDeserialize);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable object file handler found for file: " + fileToRead.getName())
            );
        }
    }
    
    /**
     * Writes an object to a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param fileToWrite The file to write to.
     * @param objectToWrite The object to write.
     * @return A CompletableFuture<Void> indicating completion.
     * @throws NoSuitableFileHandlerException if no handler can write the file as an object.
     */
    public CompletableFuture<Void> writeObjectToFile(ExecutorService executor, File fileToWrite, Object objectToWrite) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<ObjectFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof ObjectFileHandler)
                .map(h -> (ObjectFileHandler) h)
                .filter(h -> h.canHandle(fileToWrite))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().writeObject(executor, fileToWrite, objectToWrite);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable object file handler found for file: " + fileToWrite.getName())
            );
        }
    }


    // BinaryFileHandler support ==> JSON, CSV, XML, YAML, PROPERTIES, PROTOCOL BUFFER
    /**
     * Reads an object from a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param file The file to read.
     * @param classToDeserialize The class to deserialize the object into.
     * @param <T> The type of the object.
     * @return A CompletableFuture containing the deserialized object.
     * @throws NoSuitableFileHandlerException if no handler can read the file as an object.
     */
    public <T> CompletableFuture< Result<T> > readBytesFromFile(ExecutorService executor, File fileToRead) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<BinaryFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof BinaryFileHandler)
                .map(h -> (BinaryFileHandler) h)
                .filter(h -> h.canHandle(fileToRead))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().readBytes(executor, fileToRead);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable object file handler found for file: " + fileToRead.getName())
            );
        }
    }
    
    /**
     * Writes an object to a file using the appropriate handler.
     *
     * @param executor The ExecutorService to use.
     * @param fileToWrite The file to write to.
     * @param objectToWrite The object to write.
     * @return A CompletableFuture<Void> indicating completion.
     * @throws NoSuitableFileHandlerException if no handler can write the file as an object.
     */
    public CompletableFuture<Void> writeBytesToFile(ExecutorService executor, File fileToWrite, byte[] dataToWrite) {
        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<BinaryFileHandler> handler = this.handlers.stream()
                .filter(h -> h instanceof BinaryFileHandler)
                .map(h -> (BinaryFileHandler) h)
                .filter(h -> h.canHandle(fileToWrite))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().writeBytes(executor, fileToWrite, dataToWrite);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable object file handler found for file: " + fileToWrite.getName())
            );
        }
    }


    // ==> Read Many File of Any Type
    public <T> Map<String, Result<T> > readManyFileBlocking(ExecutorService executor, Map<String, Class<?> > fileAndTypes, boolean toleratePartialFailures){
        logger.info("Main thread: Initiating blocking asynchronous reads for {} files using Async Thread Executor...", fileAndTypes.size() );
        Map<String, CompletableFuture< Result<T> >> futuresMap = new HashMap<>();

        for (Map.Entry<String, Class<?>> entry : fileAndTypes.entrySet()) {
            String fileName = entry.getKey();
            Class<?> targetClass = entry.getValue();
            File fileToRead = new File(fileName);
            
            CompletableFuture<Result<T>> rawFuture = readAnyFile(executor, fileToRead, (Class<T>) targetClass);
            rawFuture = handlePartialFailures(fileToRead, rawFuture, toleratePartialFailures);

            futuresMap.put(fileName, rawFuture);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futuresMap.values().toArray(new CompletableFuture[0]));
        
        try {
            allFutures.join(); // This will block. If toleratePartialFailures is false and an error occurs, this line will throw.
        } catch (CompletionException e) {
            // If not tolerating partial failures, and an exception occurred, re-throw it.
            if (!toleratePartialFailures) {
                logger.error("Batch file reading failed (not tolerating partial failures). Overall exception: {}", e.getMessage(), e);
                throw new FileHandlingException("Batch file reading failed due to critical errors.", e);
            }
            // This else block should not be reached if toleratePartialFailures is true,
            // as allFutures.join() would not throw in that case.
            logger.error("Unexpected CompletionException caught in blocking read: {}", e.getMessage(), e);
        }

        logger.info("Main thread: All asynchronous file read initiated and joined.");
        Map<String, Result<T> > loadedData = new HashMap<>();

        for (Map.Entry<String, CompletableFuture< Result<T> >> entry : futuresMap.entrySet()) {
            String fileName = entry.getKey();
            CompletableFuture< Result<T> > future = entry.getValue();
            try {
                // Get the actual object from the completed future
                Result<T> fileResult = future.get();
                loadedData.put(fileName, fileResult);
                if(fileResult.isSuccess()){
                    logger.debug("Successfully loaded {} for file {}", fileResult.getValue().get().getClass().getSimpleName(), fileName);
                } 
                else{
                    logger.warn("File {} failed to load, recorded as null.", fileName);
                }

            } catch (InterruptedException | ExecutionException e) {
                // This catch block should only be hit for unexpected errors in the future's plumbing,
                // or if an InterruptedException occurs.
                logger.error("Unexpected error retrieving future result for file {}: {}", fileName, e.getMessage(), e);
                loadedData.put(fileName, Result.failure(e)); // Fallback for unexpected errors
            }
        }
        return loadedData;
    }

    public <T> CompletableFuture< Map<String, Result<T> > > readManyFileNonBlocking(ExecutorService executor, Map<String, Class<?> > fileAndTypes, boolean toleratePartialFailures){
        Map<String, CompletableFuture< Result<T> >> futuresMap = new HashMap<>();

        for (Map.Entry<String, Class<?>> entry : fileAndTypes.entrySet()) {
            String fileName = entry.getKey();
            Class<?> targetClass = entry.getValue();
            File fileToRead = new File(fileName);
            
            CompletableFuture<Result<T>> rawFuture = readAnyFile(executor, fileToRead, (Class<T>) targetClass);
            rawFuture = handlePartialFailures(fileToRead, rawFuture, toleratePartialFailures);

            futuresMap.put(fileName, rawFuture );
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futuresMap.values().toArray(new CompletableFuture[0]));

        CompletableFuture< Map<String, Result<T> > > mappedData = allFutures.thenApply(voidResult -> {
            logger.info("Background thread: All asynchronous file reads completed. Compiling results.");
            Map<String, Result<T> > loadedData = new HashMap<>();
            for (Map.Entry<String, CompletableFuture< Result<T> >> entry : futuresMap.entrySet()) {
                String fileName = entry.getKey();
                CompletableFuture< Result<T> > future = entry.getValue();
                try {
                    // get() is safe here because allFutures.thenApply() only runs after all futures are done
                    Result<T> fileResult = future.get();
                    loadedData.put(fileName, fileResult);

                    if(fileResult.isSuccess()){
                        logger.debug("Successfully loaded {} for file {}", fileResult.getValue().get().getClass().getSimpleName(), fileName);
                    } 
                    else{
                        logger.warn("File {} failed to load, recorded as null.", fileName);
                    }

                } catch (InterruptedException | ExecutionException e) {
                    // should not reach this point if everything is properly setup
                    logger.error("Unexpected critical error retrieving future result for file {}: {}", fileName, e.getMessage(), e);
                    loadedData.put(fileName, Result.failure(e) ); // return the unexpected error
                }
            }
            return loadedData;
        });
            if (!toleratePartialFailures) {
                return mappedData.exceptionally(ex -> {
                // This block executes ONLY if toleratePartialFailures was FALSE,
                // AND allFutures (and thus one of the rawFutures) completed exceptionally.
                logger.error("Batch file reading failed (not tolerating partial failures). Overall exception: {}", ex.getMessage(), ex);
                throw new FileHandlingException("Batch file reading failed due to critical errors.", ex);
            });
        }
        return mappedData;
    }    

    public <T> CompletableFuture< Result<T> > readAnyFile(ExecutorService executor, File fileToRead, Class<T> targetClass){

        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<UniversalFileHandler> handler = this.handlers.stream()
                .filter(h -> h.canHandle(fileToRead))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().read(executor, fileToRead, targetClass);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable File handler found for file: " + fileToRead.getName())
            );
        }
    }


    // ==> Write Many File of Any Type
    public boolean writeManyFileOnForeground(ExecutorService executor, Map<String, Object> fileAndTypes, Object option){
        Map<String, CompletableFuture<Void>> futuresMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : fileAndTypes.entrySet()) {
            String fileName = entry.getKey();
            Object contentToWrite = entry.getValue();
            File fileToWrite = new File(fileName);
            option = handleWriteFileOption(fileToWrite, option);
            futuresMap.put(fileName, writeAnyFile(executor, fileToWrite, contentToWrite, option) );
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futuresMap.values().toArray(new CompletableFuture[0]));
        allFutures.join();
        logger.info("Main thread: All asynchronous file write initiated and joined.");
        return true;
    }

    public CompletableFuture<Void> writeAnyFile(ExecutorService executor, File fileToRead, Object contentToWrite, Object option){

        // iterates through its registered handlers, checks canHandle(), and then delegates the actual work to the first suitable handler found.
        Optional<UniversalFileHandler> handler = this.handlers.stream()
                .filter(h -> h.canHandle(fileToRead))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().write(executor, fileToRead, contentToWrite, option);
        } else {
            return CompletableFuture.failedFuture(
                new NoSuitableFileHandlerException("No suitable File handler found for file: " + fileToRead.getName())
            );
        }
    }    

    private Object handleWriteFileOption(File fileToRead, Object option){

        String fileName = fileToRead.getName();
        int startingPointOfExtension = fileName.indexOf(".");
        String fileExtension = fileName.substring(startingPointOfExtension);

        switch (fileExtension) {
            case ".txt" -> option = (StandardOpenOption) option; // ex: StandardOpenOption.CREATE
            default -> throw new NoSuitableFileHandlerException("No suitable File handler with Special Option that exist for {" + fileExtension + "} found for file: " + fileToRead.getName());
        }
        return option;
    }

    private <T> CompletableFuture< Result<T> > handlePartialFailures( File fileToRead, CompletableFuture<Result<T>> rawFuture, boolean toleratePartialFailures){
        CompletableFuture<Result<T>> individualFuture;

        if (toleratePartialFailures) {
            // If true, we "handle" the exception at the individual future level.
            // This means the individualFuture itself will always complete successfully,
            // but its contained Result<T> will be a Failure if an error occurred.
            individualFuture = rawFuture.handle((result, ex) -> {
                if (ex != null) {
                    logger.warn("Individual file read failed for {}: {}", fileToRead.getName(), ex.getMessage());
                    return Result.failure(ex); // Return a Failure Result
                }
                return result; // Return the successful Result
            });
        } else {
            // If false, we do NOT handle the exception at the individual future level.
            // If rawFuture completes exceptionally, it remains exceptional.
            individualFuture = rawFuture; // Propagate exception if not tolerating
        }
        return individualFuture;
    }


}

/*
 * Jackson uses @JsonManagedReference and @JsonBackReference to handle bidirectional (circular) relationships between objects.

    @JsonManagedReference: Marks the "owner" or "parent" side of the relationship. This field will be fully serialized.

    @JsonBackReference: Marks the "child" or "back" side of the relationship. 
    This field will not be serialized (during writing) and will be populated by Jackson during deserialization when it encounters
    the corresponding managed reference.

    The error "Multiple back-reference properties with name 'defaultReference'" occurs because:

    By default, if you use @JsonBackReference without specifying a value (e.g., @JsonBackReference), Jackson assigns a default 
    logical name (like "defaultReference").

    If you have more than one field in your entire object graph that is annotated with @JsonBackReference and they all use this 
    default name, Jackson gets confused during deserialization. 
    It doesn't know which @JsonManagedReference (which also defaults to "defaultReference" if not named) corresponds to which 
    @JsonBackReference.
 */

/**
 * How Markdown Rendering Works:
Read the Raw Markdown: Your readTextFileAsync method (using Files.readString) is perfect for this first step. It gets the raw Markdown text into a Java String.

Parse the Markdown: A Markdown parsing library takes this raw Markdown String and converts it into an Abstract Syntax Tree (AST). The AST is a programmatic representation of the Markdown document's structure (e.g., "this is a paragraph, this is a heading, this word is emphasized").

Render the AST: A Markdown rendering library then takes the AST and converts it into a displayable format, most commonly HTML. Once you have HTML, you can display it in a web view (like a WebView in a desktop app, or directly in a web browser for web apps), or use other rendering engines.

Popular Java Markdown Parsing and Rendering Libraries:
If you want to display Markdown in your Java application, you'll typically add one of these libraries to your project:

commonmark-java: This is a very popular and robust Java library for parsing and rendering Markdown text according to the CommonMark specification. It's fast, flexible, and extensible. It can render Markdown to HTML.

flexmark-java: Another powerful and flexible Markdown processor that supports CommonMark and many extensions. It also has modules for converting Markdown to HTML, PDF, and DOCX.

Example of using commonmark-java (conceptual):

Java

// Add commonmark-java dependency (e.g., Maven):
/*
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.21.0</version> <!-- Use the latest version -->
</dependency>
*/

// In your code, after you get the markdownContent String:
// import org.commonmark.node.Node;
// import org.commonmark.parser.Parser;
// import org.commonmark.renderer.html.HtmlRenderer;

// ... (inside a method where you have the markdownContent String)

// 1. Create a Parser
// Parser parser = Parser.builder().build();

// 2. Parse the Markdown text into a Node (AST)
// Node document = parser.parse(markdownContent);

// 3. Create an HTML Renderer
// HtmlRenderer renderer = HtmlRenderer.builder().build();

// 4. Render the AST to HTML
// String htmlContent = renderer.render(document);

// Now, htmlContent will contain HTML like:
// "<p>This is <em>Markdown</em></p>\n<h1>My Game Readme</h1>"
// You would then display this HTML in a component that can render HTML.
// So, to summarize, Files.readString gives you the raw text. To see the bold, italics, etc., as formatted text, you need a Markdown rendering library to convert that raw text into a displayable format like HTML.



 /**
  * 2. Reading Other Structured Data Formats (XML, CSV)
XML: Jackson has modules (e.g., jackson-dataformat-xml) that allow ObjectMapper to read and write XML. If you add the XML module, you could potentially adapt deserializeObjectAsync to work with XML by configuring the ObjectMapper appropriately (e.g., using XmlMapper).

CSV: Jackson also has a CSV module (jackson-dataformat-csv). Similar to XML, you'd configure an CsvMapper to read CSV data into lists of objects or maps.

YAML: Jackson has a YAML module (jackson-dataformat-yaml).

Properties: Jackson has a Properties module (jackson-dataformat-properties).

For these, the core idea is still "deserialization," but using the correct mapper for the format.
  */

  /**Certainly! You're asking if the deserializeObjectAsync method can be used to read file types other than JSON, such as Markdown, and what changes would be needed to support various file formats commonly used in games or business applications.

The current deserializeObjectAsync method is specifically designed for deserializing JSON data into Java objects because it uses objectMapper.readValue(). The ObjectMapper from Jackson is a JSON processing library, so it inherently understands JSON syntax and how to map it to Java objects based on your class structure and annotations. It cannot directly read and interpret a Markdown file or a raw binary file as an object in the same way.

To read different types of files, you would need different approaches:

1. Reading Text-Based Files (like Markdown, TXT, Logs)
For files like Markdown, which are essentially plain text, you wouldn't "deserialize" them into a structured Java object in the same way you do with JSON. Instead, you would read their content as a String.

You can create a separate asynchronous method for this:

2. Reading Other Structured Data Formats (XML, CSV)
XML: Jackson has modules (e.g., jackson-dataformat-xml) that allow ObjectMapper to read and write XML. If you add the XML module, you could potentially adapt deserializeObjectAsync to work with XML by configuring the ObjectMapper appropriately (e.g., using XmlMapper).

CSV: Jackson also has a CSV module (jackson-dataformat-csv). Similar to XML, you'd configure an CsvMapper to read CSV data into lists of objects or maps.

YAML: Jackson has a YAML module (jackson-dataformat-yaml).

Properties: Jackson has a Properties module (jackson-dataformat-properties).

For these, the core idea is still "deserialization," but using the correct mapper for the format.

3. Reading Binary Files (Images, Audio, Game Assets)
For binary files, you would typically read them as byte[] or use specialized libraries for image processing (e.g., ImageIO, Apache Commons Imaging), audio (e.g., Java Sound API, JLayer), or game-specific asset loaders. This is a completely different paradigm than object deserialization. */