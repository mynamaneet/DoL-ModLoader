package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TweeFile {
    public TweeFile (File _filePath, String _name, String _parent){
        this.filePath = _filePath;
        this.name = _name;
        this.parent = _parent;
    }
    public TweeFile (File _filePath, String _name, String _parent, boolean _hasChanged){
        this.filePath = _filePath;
        this.name = _name;
        this.parent = _parent;
        this.hasChanged = _hasChanged;
    }

    private File filePath;
    private String name;
    private String parent = "";
    private ArrayList<DolPassage> passages = new ArrayList<>();
    private boolean hasChanged = false;
    private boolean overwriten = false;


    public File getPath(){
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
    public boolean hasChanged(){
        return hasChanged;
    }
    public boolean isOverwriten(){
        return overwriten;
    }

    public void addPassage(DolPassage pass){
        passages.add(pass);
    }
    public void setHasChanged(){
        hasChanged = true;
    }
    public void setOverwriten(){
        overwriten = true;
    }
}
