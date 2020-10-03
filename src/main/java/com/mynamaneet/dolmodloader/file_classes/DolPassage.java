package com.mynamaneet.dolmodloader.file_classes;

public class DolPassage {
    public DolPassage(String _file, String _name, String _tweeName, String _parentDirectory){
        this.file = _file;
        this.name = _name;
        this.tweeName = _tweeName;
        this.parentDirectory = _parentDirectory;
    }
    public DolPassage(String _file, String _name, String _tweeName, String _parentDirectory, boolean _hasChanged){
        this.file = _file;
        this.name = _name;
        this.tweeName = _tweeName;
        this.parentDirectory = _parentDirectory;
        this.hasChanged = _hasChanged;
    }


    private String file;
    private String name;
    private String tweeName;
    private String parentDirectory = "";
    private boolean hasChanged = false;
    private boolean overwriten = false;


    public String getFilePath(){
        return file;
    }
    public String getName(){
        return name;
    }
    public String getTweeName(){
        return tweeName;
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
