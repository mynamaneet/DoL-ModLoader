package com.mynamaneet.dolmodloader;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mynamaneet.dolmodloader.exceptions.InvalidLocationException;
import com.mynamaneet.dolmodloader.exceptions.InvalidPassageException;
import com.mynamaneet.dolmodloader.exceptions.InvalidSubfolderException;
import com.mynamaneet.dolmodloader.exceptions.InvalidTweeFileException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public final class ModLoader {
    private ModLoader() {
    }

    private static final Logger LOGGER = Logger.getLogger(ModLoader.class.getName());
    private static ArrayList<Mod> mods = new ArrayList<>();
    private static ArrayList<DolSubfolder> dolSubfolders = new ArrayList<>();


    
    private static String getRunningPath() {
        try {
            return new File(ModLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException|NullPointerException ex) {
            LOGGER.log(Level.SEVERE, "Error occur in getRunningPath().", ex);
        }
        return "FAILED";
    }


    private static void getMods(String curRunningPath) throws IOException{
        try{
            File modFilesPath = new File(curRunningPath);
            File[] modFiles = modFilesPath.listFiles();
            for (File file : modFiles) {
                //Get Mod class and run it's main method
                if(file.isDirectory()){
                    File innerModFilesPath = new File(file.getPath());
                    File[] innerModFiles = innerModFilesPath.listFiles(new FilenameFilter(){
                        @Override
                        public boolean accept(File dir, String name){
                            return name.toLowerCase().endsWith(".jar");
                        }
                    });

                    for(File innerFile : innerModFiles){
                        URLClassLoader child = new URLClassLoader(new URL[] {innerFile.toURI().toURL()}, ModLoader.class.getClassLoader());
                        Properties prop = new Properties();
                        prop.load(new FileInputStream(new File(innerFile.toString()+(".properties"))));
                        Class<?> classToLoad = Class.forName(prop.getProperty("modClass"), true, child);
                        Method method = classToLoad.getDeclaredMethod("main");
                        Object instance = classToLoad.getDeclaredConstructor().newInstance();
                        method.invoke(instance);
                        child.close();
                    }
                }
                else if(FilenameUtils.getExtension(file.getName()).equals("jar")){
                    URLClassLoader child = new URLClassLoader(new URL[] {file.toURI().toURL()}, ModLoader.class.getClassLoader());
                    Properties prop = new Properties();
                    prop.load(new FileInputStream(new File(file.toString()+(".properties"))));
                    Class<?> classToLoad= Class.forName(prop.getProperty("modClass"), true, child);
                    Method method = classToLoad.getDeclaredMethod("main");
                    Object instance = classToLoad.getDeclaredConstructor().newInstance();
                    method.invoke(instance);
                    child.close();
                }
            }
        } catch(ClassNotFoundException|NoSuchMethodException|IllegalAccessException|InstantiationException|InvocationTargetException|IOException|NullPointerException ex){
            LOGGER.log(Level.SEVERE, "Error occur during getMods()", ex);
        }
    }


    private static void linkLocationToSubfolder(String subfolderName, DolLocation location){
        try {
            getDolSubfolder(subfolderName).addLocation(location);
        } catch (InvalidSubfolderException ex) {
            LOGGER.log(Level.SEVERE, "Error occured while adding location to subfolder", ex);
        }
    }


    private static void linkTweeToLocation(String locationName, TweeFile file){
        try{
            DolLocation location =  getDolLocation(locationName);
            location.addFile(file);
        } catch(InvalidLocationException ex){
            LOGGER.log(Level.SEVERE, "Error occured while linking twee to location.");
        }
    }


    private static void linkPassageToTwee(String locationName, String tweeName, DolPassage passage){
        try{
            DolLocation location = getDolLocation(locationName);
            TweeFile file = getTweeFile(location, tweeName);
            file.addPassage(passage);
        } catch(InvalidLocationException|InvalidTweeFileException ex){
            LOGGER.log(Level.SEVERE, "Error occured while linking passage to twee file", ex);
        }
    }
    


    private static void setupDolSubfolders(){
        String gamePath = getRunningPath()+"\\dol-files\\game";

        //overworld-town
        dolSubfolders.add(new DolSubfolder(new File(gamePath+"\\overworld-town"), "overworld-town"));
    }


    private static void setupDolLocations(){
        String gamePath = getRunningPath()+"\\dol-files\\game";

        //loc-home
        linkLocationToSubfolder("overworld-town", new DolLocation(new File(gamePath+"\\overworld-town\\loc-home"), "loc-home", "overworld-town")); 
    }


    private static void setupTweeFiles(){
        String gamePath = getRunningPath()+"\\dol-files\\game";

        //inside loc-home
        //main.twee
        linkTweeToLocation("loc-home", new TweeFile(new File(gamePath+"\\overworld-town\\loc-home\\main.twee"), "main", "loc-home"));
    }


    private static void setupDolPassages(){
        String gamePath = getRunningPath()+"\\dol-files\\game";

        //Bedroom
        linkPassageToTwee("loc-home", "main", new DolPassage(gamePath+"\\overworld-town\\loc-home\\main.twee", "Bedroom", "main", gamePath+"\\overworld-town\\loc-home"));
    }


    private static void writeToTwee(String filePath, ArrayList<String> targetString, ArrayList<String> insertStrings){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            ArrayList<String> lines = new ArrayList<>();

            //Record all lines
            while((line=bufferedReader.readLine()) != null){
                lines.add(line);
            }

            bufferedReader.close();

            //Find Target Line
            int targetIndex = -1;
            int targetDepth = 0;
            for (int i = 0; i < lines.size(); i++) {
                String curLine = lines.get(i);
                if(curLine.equals(targetString.get(targetDepth))){
                    if(targetDepth == targetString.size()-1){
                        targetIndex = i;
                        break;
                    }
                    targetDepth++;
                }
            }

            //Place text
            if(targetIndex != -1){
                targetIndex++;
                for (String newString : insertStrings) {
                    lines.add(targetIndex, newString);
                }

                //Rewrite file
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
                for (String curLine : lines) {
                    bufferedWriter.write(curLine);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } else{
                LOGGER.severe("Failed to find targetString");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ("Error occured while writing to "+filePath), ex);
        }
    }





    public static void subscribeMod(Mod mod){
        mods.add(mod);
        LOGGER.info("Added mod "+mod.getModName());
    }


    public static void logMessage(String message){
        LOGGER.info(message);
    }


    public static DolSubfolder getDolSubfolder(String name) throws InvalidSubfolderException {
        for (DolSubfolder subfolder : dolSubfolders) {
            if(subfolder.getName().equals(name)){
                return subfolder;
            }
        }
        throw new InvalidSubfolderException("Error finding DolSubfolder : " + name);
    }


    public static DolLocation getDolLocation(String name) throws InvalidLocationException{
        for (DolSubfolder subfolder : dolSubfolders) {
            for (DolLocation location : subfolder.getLocations()) {
                if(location.getName().equals(name)){
                    return location;
                }
            }
        }
        throw new InvalidLocationException("Error finding DolLocation : " + name);
    }


    public static TweeFile getTweeFile(DolLocation location, String tweeName) throws InvalidTweeFileException{
        for (TweeFile file : location.getFiles()) {
            if(file.getName().equals(tweeName)){
                return file;
            }
        }
        throw new InvalidTweeFileException("Error finding TweeFile (" + tweeName + ") in DolLocation (" + location.getName() +").");
    }


    public static DolPassage getDolPassage(TweeFile twee, String passageName) throws InvalidPassageException{
        for (DolPassage passage : twee.getPassages()) {
            if(passage.getName().equals(passageName)){
                return passage;
            }
        }
        throw new InvalidPassageException("Error finding DolPassage (" + passageName + ") in TweeFile (" + twee.getName() + ").");
    }


    public static void addPassageText(ArrayList<String> message, DolPassage passage){
        ArrayList<String> targets = new ArrayList<>();
        targets.add(":: "+passage.getName()+" [nobr]");
        targets.add("/*newtext*/");
        
        writeToTwee(passage.getFilePath(), targets, message);
    }



    public static void main(String[] args) {
        Handler consoleHandler = null;
        Handler fileHandler = null;
        try{
            //Creating LOGGER Handlers
            consoleHandler = new ConsoleHandler();
            fileHandler = new FileHandler("./debug-log.log");

            //Assigning handlers to LOGGER object
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);

            //Setting levels to LOGGER handlers and LOGGER
            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.CONFIG);
            LOGGER.setLevel(Level.ALL);

            LOGGER.config("Configuration done.");
            LOGGER.removeHandler(consoleHandler);

            //Get running path
            String curRunningPath = getRunningPath()+"\\mods";
            LOGGER.log(Level.INFO, curRunningPath);

            if(curRunningPath.equals("FAILED")){
                LOGGER.log(Level.SEVERE, "Closing Process");
                System.exit(1);
            }

            //Get Mods
            getMods(curRunningPath);

            //Log Mod Names
            for (Mod mod : mods) {
                LOGGER.log(Level.INFO, String.format("foreach: %s", mod.getModName()));
            }
        } catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", ex);
        }


        //Load backup into dol-files
        File backupDir = new File(getRunningPath()+"\\backup");
        File dolFilesLocation = new File(getRunningPath()+"\\dol-files");
        
        LOGGER.info("Loading dol-files...");

        try{
            FileUtils.deleteDirectory(dolFilesLocation);
        } catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Error occured while deleted dol-files.", ex);
        } catch(IllegalArgumentException ex){
            LOGGER.log(Level.WARNING, "Couldn't delete old dol-files, directory doesn't exist.", ex);
        }

        try{
            FileUtils.copyDirectory(backupDir, dolFilesLocation);
        } catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Error occured while loading backup DOL files.", ex);
        }

        LOGGER.info("Loaded dol-files.");

        //Setup DolSubfolders
        LOGGER.info("Setting up DOL Subfolders...");
        setupDolSubfolders();
        LOGGER.info("Finished setting up DOL Subfolders.");

        //Setup DolLocations
        LOGGER.info("Setting up DOL Locations...");
        setupDolLocations();
        LOGGER.info("Finished setting up DOL Locations.");

        //Setup TweeFiles
        LOGGER.info("Setting up TweeFiles...");
        setupTweeFiles();
        LOGGER.info("Finished setting up TweeFiles.");

        //Setup DolPassages
        LOGGER.info("Setting up DOL Passages...");
        setupDolPassages();
        LOGGER.info("Finished setting up DOL Passages.");


        //Load Mods
        LOGGER.info("Loading mods...");
        for (Mod mod : mods) {
            LOGGER.info("Loading " + mod.getModName() + " | Version: " + mod.getModVersion() + " | Author: " + mod.getModAuthor());
            mod.modApp();
        }
        LOGGER.info("Loaded mods.");
    }
}
