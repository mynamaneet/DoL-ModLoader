package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;

import com.mynamaneet.dolmodloader.Mod;

public class CustomPassage extends DolPassage {
    public CustomPassage(File _tweeFile, String _name, File _parentDirectory, Mod _mod){
        super(_tweeFile, _name, _parentDirectory);
        this.mod = _mod;
    }
    public CustomPassage(File _tweeFile, String _name, File _parentDirectory, boolean _hasChanged, Mod _mod){
        super(_tweeFile, _name, _parentDirectory, _hasChanged);
        this.mod = _mod;
    }


    
    private Mod mod;

    public String getModName(){
        return mod.getModName();
    }
    public String getModAuthor(){
        return mod.getModAuthor();
    }
    public String getModVersion(){
        return mod.getModVersion();
    }
    public File getModFolder(){
        return mod.getModFolder();
    }
}
