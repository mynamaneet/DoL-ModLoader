package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;

import com.mynamaneet.dolmodloader.Mod;

public class CustomSubfolder extends DolSubfolder{
    public CustomSubfolder(File _directoryPath, String _name, Mod _mod){
        super(_directoryPath, _name);
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
