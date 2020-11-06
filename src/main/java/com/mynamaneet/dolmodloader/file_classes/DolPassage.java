package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.mynamaneet.dolmodloader.Mod;

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
    private ArrayList<Mod> changedMod = new ArrayList<>();
    private ArrayList<Integer> changedInt = new ArrayList();


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
    
    public void addModChange(Mod mod){
        if(changedMod.contains(mod)){
            int location = changedMod.indexOf(mod);
            changedInt.set(location, changedInt.get(location) + 1);
        } else{
            changedMod.add(mod);
            changedInt.add(1);
        }
    }
    
    public int getModChange(Mod mod){
        if(changedMod.contains(mod)){
            return changedInt.get(changedMod.indexOf(mod));
        } else{
            return 0;
        }
    }
}