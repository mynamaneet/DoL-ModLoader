package com.mynamaneet.dolmodloader;

import java.io.File;

public abstract class Mod {
    public Mod(String _modName, String _modVersion, String _modAuthor){
        this.modName = _modName;
        this.modVersion = _modVersion;
        this.modAuthor = _modAuthor;

        ModLoader.subscribeMod(this);
    }
    
    protected String modName;
    protected String modVersion;
    protected String modAuthor;
    protected File modFolder;

    
    public abstract void modApp();


    public String getModName(){
        return modName;
    }

    public String getModVersion(){
        return modVersion;
    }

    public String getModAuthor(){
        return modAuthor;
    }

    public File getModFolder(){
        return modFolder;
    }

    public void setModFolder(File path){
        this.modFolder = path;
    }
}