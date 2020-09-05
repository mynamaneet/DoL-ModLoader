package com.mynamaneet.dolmodloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DolLocation {
    public DolLocation(File _directoryPath, String _name){
        this.directoryPath = _directoryPath;
        this.name = _name;
    }
    public DolLocation(File _directoryPath, String _name, String _parent){
        this.directoryPath = _directoryPath;
        this.name = _name;
        this.parent = _parent;
    }

    private File directoryPath;
    private String name;
    private String parent = "";
    private ArrayList<TweeFile> tweeFiles = new ArrayList<>();


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

    public void addFile(TweeFile file){
        tweeFiles.add(file);
    }
}
