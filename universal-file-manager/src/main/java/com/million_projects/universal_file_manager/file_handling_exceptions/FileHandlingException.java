package com.million_projects.universal_file_manager.file_handling_exceptions;

public class FileHandlingException extends RuntimeException{
    public FileHandlingException(String message) { super(message); }
    public FileHandlingException(String message, Throwable cause) { super(message, cause); }
}
