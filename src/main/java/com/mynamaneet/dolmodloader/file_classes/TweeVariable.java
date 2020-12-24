package com.mynamaneet.dolmodloader.file_classes;

import com.mynamaneet.dolmodloader.enums.TweeVarType;

public class TweeVariable {
    public TweeVariable(String _name, String _value){
        this.name = _name;
        this.value = _value;
        this.type = TweeVarType.STRING;
    }
    public TweeVariable(String _name, Integer _value){
        this.name = _name;
        this.value = _value;
        this.type = TweeVarType.INTEGER;
    }

    //TODO Array Constructor Needed

    private String name;
    private TweeVarType type;
    private Object value;


    public String getName(){
        return name;
    }

    public TweeVarType getType(){
        return type;
    }

    public Object getValue(){
        return value;
    }
}
