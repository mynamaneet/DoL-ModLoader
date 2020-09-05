package com.mynamaneet.dolmodloader;

public class DolPassage {
    public DolPassage(String _file, String _name, String _tweeName){
        this.file = _file;
        this.name = _name;
        this.tweeName = _tweeName;
    }
    public DolPassage(String _file, String _name, String _tweeName, String _parentDirectory){
        this.file = _file;
        this.name = _name;
        this.tweeName = _tweeName;
        this.parentDirectory = _parentDirectory;
    }


    private String file;
    private String name;
    private String tweeName;
    private String parentDirectory = "";


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
}
