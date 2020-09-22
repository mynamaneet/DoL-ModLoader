package com.mynamaneet.dolmodloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TweeFile {
    public TweeFile (File _filePath, String _name, String _parent){
        this.filePath = _filePath;
        this.name = _name;
        this.parent = _parent;
    }

    private File filePath;
    private String name;
    private String parent = "";
    private ArrayList<DolPassage> passages = new ArrayList<>();


    public File getDirectoryPath(){
        return filePath;
    }
    public String getName(){
        return name;
    }
    public String getParent(){
        return parent;
    }
    public List<DolPassage> getPassages(){
        return passages;
    }

    public void addPassage(DolPassage pass){
        passages.add(pass);
    }
}
