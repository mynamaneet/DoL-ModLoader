package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;

import com.mynamaneet.dolmodloader.Mod;

public class CustomLocation extends DolLocation {
    public CustomLocation(File _directoryPath, String _name, String _parent, Mod _mod){
        super(_directoryPath, _name, _parent);
        this.mod = _mod;
    }
    public CustomLocation(File _directoryPath, String _name, String _parent, boolean _hasChanged, Mod _mod){
        super(_directoryPath, _name, _parent, _hasChanged);
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
