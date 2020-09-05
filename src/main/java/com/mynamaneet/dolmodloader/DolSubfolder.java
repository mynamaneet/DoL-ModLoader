package com.mynamaneet.dolmodloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DolSubfolder {
    public DolSubfolder(File _directoryPath, String _name){
        this.directoryPath = _directoryPath;
        this.name = _name;
    }

    private File directoryPath;
    private String name;
    private ArrayList<DolLocation> locations = new ArrayList<>();


    public File getDirectoryPath(){
        return directoryPath;
    }
    public String getName(){
        return name;
    }
    public List<DolLocation> getLocations(){
        return locations;
    }

    public void addLocation(DolLocation loc){
        locations.add(loc);
    }
}
