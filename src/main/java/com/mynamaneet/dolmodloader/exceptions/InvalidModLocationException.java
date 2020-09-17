package com.mynamaneet.dolmodloader.exceptions;

public class InvalidModLocationException extends Exception{
    public InvalidModLocationException(String errorMessage){
        super(errorMessage);
    }
    private static final long serialVersionUID = 1L;
}
