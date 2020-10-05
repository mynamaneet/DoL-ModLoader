package com.mynamaneet.dolmodloader.file_classes;

public class DolPassage {
    public DolPassage(TweeFile _tweeFile, String _name, String _parentDirectory){
        this.file = _tweeFile;
        this.name = _name;
        this.parentDirectory = _parentDirectory;
    }
    public DolPassage(TweeFile _tweeFile, String _name, String _parentDirectory, boolean _hasChanged){
        this.file = _tweeFile;
        this.name = _name;
        this.parentDirectory = _parentDirectory;
        this.hasChanged = _hasChanged;
    }


    private TweeFile file;
    private String name;
    private String parentDirectory = "";
    private boolean hasChanged = false;
    private boolean overwriten = false;


    public TweeFile getTweeFile(){
        return file;
    }
    public String getName(){
        return name;
    }
    public String getParentDirectory(){
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
