package com.mynamaneet.dolmodloader.exceptions;

public class ProcessWarningException extends Exception{
    public ProcessWarningException(String errorMessage){
        super(errorMessage);
    }
    public ProcessWarningException(String errorMessage, Throwable ex){
        super(errorMessage, ex);
    }
    private static final long serialVersionUID = 1L;
}
