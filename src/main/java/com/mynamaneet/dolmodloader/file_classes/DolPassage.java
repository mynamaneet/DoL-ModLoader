package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;

public class DolPassage {
    public DolPassage(File _tweeFile, String _name, File _parentDirectory){
        this.file = _tweeFile;
        this.name = _name;
        this.parentDirectory = _parentDirectory;
    }
    public DolPassage(File _tweeFile, String _name, File _parentDirectory, boolean _hasChanged){
        this.file = _tweeFile;
        this.name = _name;
        this.parentDirectory = _parentDirectory;
        this.hasChanged = _hasChanged;
    }


    private File file;
    private String name;
    private File parentDirectory;
    private boolean hasChanged = false;
    private boolean overwriten = false;


    public File getTweeFile(){
        return file;
    }
    public String getName(){
        return name;
    }
    public File getParentDirectory(){
        return parentDirectory;
    }
    public boolean hasChanged(){
        return hasChanged;
    }
    public boolean isOverwriten(){
        return overwriten;
    }

    public void setHasChanged(){
        hasChanged = true;
    }
    public void setOverwriten(){
        overwriten = true;
    }
}
