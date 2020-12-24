package com.mynamaneet.dolmodloader.file_classes;

import java.io.File;

import com.mynamaneet.dolmodloader.Mod;
import com.mynamaneet.dolmodloader.enums.TweeVarType;

public class CustomTweeVariable extends TweeVariable {
    public CustomTweeVariable(String _name, String _value, Mod _mod){
        super(_name, _value);
        this.mod = _mod;
    }
    public CustomTweeVariable(String _name, Integer _value, Mod _mod){
        super(_name, _value);
        this.mod = _mod;
    }

    //TODO Array Constructor Needed

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
