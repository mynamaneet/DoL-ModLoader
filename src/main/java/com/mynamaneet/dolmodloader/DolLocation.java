package com.mynamaneet.dolmodloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DolLocation {
    public DolLocation(File _directoryPath, String _name, String _parent){
        this.directoryPath = _directoryPath;
        this.name = _name;
        this.parent = _parent;
    }
    public DolLocation(File _directoryPath, String _name, String _parent, boolean _hasChanged){
        this.directoryPath = _directoryPath;
        this.name = _name;
        this.parent = _parent;
        this.hasChanged = _hasChanged;
    }

    private File directoryPath;
    private String name;
    private String parent = "";
    private ArrayList<TweeFile> tweeFiles = new ArrayList<>();
    private boolean hasChanged = false;


    public File getDirectoryPath(){
        return directoryPath;
    }
    public String getName(){
        return name;
    }
    public String getParent(){
        return parent;
    }
    public List<TweeFile> getFiles(){
        return tweeFiles;
    }
    public boolean hasChanged(){
        return hasChanged;
    }

    public void addFile(TweeFile file){
        tweeFiles.add(file);
    }
    public void setIsChanged(){
        hasChanged = true;
    }
}
