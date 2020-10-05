package com.mynamaneet.dolmodloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mynamaneet.dolmodloader.exceptions.*;
import com.mynamaneet.dolmodloader.file_classes.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public final class ModLoader {
    private ModLoader() {
    }

    private static final Logger LOGGER = Logger.getLogger(ModLoader.class.getName());
    private static ArrayList<Mod> mods = new ArrayList<>();
    private static ArrayList<DolSubfolder> dolSubfolders = new ArrayList<>();

    /*
    File Organization:
    SubFolder stores Location

    Location stores TweeFile
    Location links to SubFolder

    TweeFile stores Passage
    TweeFile links Location

    Passage links TweeFile
    */
    
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
                    //Holds all .jar files inside the found directory
                    File[] innerModFiles = innerModFilesPath.listFiles();

                    for(File innerFile : innerModFiles){
                        //If file is .jar inside the named directory
                        if(FilenameUtils.getExtension(innerFile.getName()).equals("jar")){
                            URLClassLoader child = new URLClassLoader(new URL[] {innerFile.toURI().toURL()}, ModLoader.class.getClassLoader());
                            Properties prop = new Properties();
                            prop.load(new FileInputStream(new File(file.toString()+"\\properties.properties")));
                            Class<?> classToLoad = Class.forName(prop.getProperty("modClass"), true, child);
                            Method method = classToLoad.getDeclaredMethod("main");
                            Object instance = classToLoad.getDeclaredConstructor().newInstance();
                            method.invoke(instance);
                            child.close();
                        }
                    }
                }
                //If file is a .jar in the Mods directory
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
        String gamePathLocation = getRunningPath()+"\\dol-files\\game";
        File gamePath = new File(gamePathLocation);
        File[] files = gamePath.listFiles();
        
        for (File file : files) {
            if(file.getName().equals("overworld-forest") || file.getName().equals("overworld-plains") || file.getName().equals("overworld-town") || file.getName().equals("overworld-underground")){
                dolSubfolders.add(new DolSubfolder(file, file.getName()));
            }
        }
    }


    private static void setupDolLocations(){
        for (DolSubfolder dolSubfolder : dolSubfolders) {
            File[] files = dolSubfolder.getDirectoryPath().listFiles();
            for (File file : files) {
                if(file.isDirectory()){
                    linkLocationToSubfolder(dolSubfolder.getName(), new DolLocation(file, file.getName(), dolSubfolder.getDirectoryPath().getAbsolutePath()));
                }
            }
        }
    }


    private static void setupTweeFiles(){
        for (DolSubfolder dolSubfolder : dolSubfolders) {
            for (DolLocation dolLocation : dolSubfolder.getLocations()) {
                File[] files = dolLocation.getDirectoryPath().listFiles();
                for (File file : files) {
                    if(file.isFile()){
                        linkTweeToLocation(dolLocation.getName(), new TweeFile(file, file.getName(), dolLocation.getDirectoryPath().getAbsolutePath()));
                    } else if(file.getName().equals("classes") && file.isDirectory()){
                        File[] innerFiles = new File(dolLocation.getDirectoryPath().getAbsolutePath() + "\\classes").listFiles();
                        for (File innerFile : innerFiles) {
                            linkTweeToLocation(dolLocation.getName(), new TweeFile(innerFile, innerFile.getName(), dolLocation.getDirectoryPath().getAbsolutePath()+"\\classes"));
                        }
                    }
                }
            }
        }
    }


    private static void setupDolPassages(){
        for (DolSubfolder dolSubfolder : dolSubfolders) {
            for (DolLocation dolLocation : dolSubfolder.getLocations()) {
                for (TweeFile tweeFile : dolLocation.getFiles()) {
                    try{
                        BufferedReader reader = new BufferedReader(getPrivilegedReader(tweeFile.getDirectoryPath()));
                        String line;
                        while((line = reader.readLine()) != null){
                            if(line.length() > 1 && line.charAt(0) == ':' && line.charAt(1) == ':'){
                                ArrayList<Character> passageNameArray = new ArrayList<>();
                                for (int i = 3; i < line.length(); i++) {
                                    if(line.charAt(i) == ' ' && line.charAt(i+1) == '['){
                                        break;
                                    } else{
                                        passageNameArray.add(line.charAt(i));
                                    }
                                }
                                char[] chars = new char[passageNameArray.size()];
                                for (int i = 0; i < passageNameArray.size(); i++) {
                                    chars[i] = passageNameArray.get(i);
                                }


                                linkPassageToTwee(dolLocation.getName(), tweeFile.getName(), new DolPassage(tweeFile.getDirectoryPath(), new String(chars), dolLocation.getDirectoryPath().getAbsolutePath()));
                            }
                        }
                        reader.close();
                    } catch(IOException e){
                        LOGGER.log(Level.SEVERE, "Error in setupDolPassages()", e);
                    }
                }
            }
        }
    }


    private static void setupPassageTextLocations(){
        for (DolSubfolder dolSubfolder : dolSubfolders) {
            for (DolLocation dolLocation : dolSubfolder.getLocations()) {
                for (TweeFile tweeFile : dolLocation.getFiles()){
                        try{
                        BufferedReader reader = new BufferedReader(getPrivilegedReader(tweeFile.getDirectoryPath()));
                        ArrayList<String> lines = new ArrayList<>();
                        String line;
                        while((line = reader.readLine()) != null){
                            lines.add(line);
                        }
                        reader.close();

                        int addLineCount = 1;
                        int curLineCount = 0;
                        boolean foundLink = false;
                        boolean foundCase = false;
                        boolean dontAdd = false;
                        boolean placedFirstAddLine = false;
                        for (int i = 0; i < lines.size()-1; i++) {
                            curLineCount++;
                            foundCase = false;
                            dontAdd = false;

                            //Passage Name
                            if(lines.get(i).length() > 2){
                                if(lines.get(i).charAt(0) == ':' && lines.get(i).charAt(1) == ':'){
                                    addLineCount = 1;
                                    curLineCount = 0;
                                    foundLink = false;
                                    foundCase = true;
                                    dontAdd = true;
                                    placedFirstAddLine = false;
                                }
                            }

                            int offset = 0;
                            if(lines.get(i).length() > 0){
                                for (int j = 0; j < lines.get(i).length(); j++) {
                                    if(lines.get(i).charAt(j) == ' ' || lines.get(i).charAt(j) == '\t'){
                                        offset++;
                                    } else{
                                        break;
                                    }
                                }
                            }


                            //Check for <<set
                            if(!foundLink && !foundCase && lines.get(i).length() > 5+offset){
                                if(lines.get(i).substring(0+offset, 5+offset).equals("<<set")){
                                    foundCase = true;
                                    //Check if beginning set
                                    if(curLineCount <= 2){
                                        lines.add(i+1, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                        placedFirstAddLine = true;
                                        dontAdd = true;
                                    }
                                }
                            }

                            //Check for <<if
                            if(!foundLink && !foundCase && lines.get(i).length() > 4+offset){
                                if(lines.get(i).substring(0+offset, 4+offset).equals("<<if")){
                                    foundCase = true;
                                }
                            }

                            //Check for <</if
                            if(!foundLink && !foundCase && lines.get(i).length() > 5+offset){
                                if(lines.get(i).substring(0+offset, 5+offset).equals("<</if")){
                                    foundCase = true;
                                }
                            }

                            //check for <<else
                            if(!foundLink && !foundCase && lines.get(i).length() > 6+offset){
                                if(lines.get(i).substring(0+offset, 6+offset).equals("<<else")){
                                    foundCase = true;
                                }
                            }

                            //check for <</else
                            if(!foundLink && !foundCase && lines.get(i).length() > 7+offset){
                                if(lines.get(i).substring(0+offset, 7+offset).equals("<</else")){
                                    foundCase = true;
                                }
                            }

                            
                            
                            if(!foundCase && !placedFirstAddLine && curLineCount == 2){
                                lines.add(i+1, "/*line"+addLineCount+"*/");
                                addLineCount++;
                            }

                            
                            //check for <<link
                            if(!foundLink && !foundCase && lines.get(i).length() > 6+offset){
                                if(lines.get(i).substring(0+offset, 6+offset).equals("<<link")){
                                    foundLink = true;
                                }
                            }



                            //Add Lines
                            if(foundCase && !dontAdd){
                                boolean aboveChecked = false;
                                //Check line above
                                if(i != 0){
                                    if(lines.get(i-1).length() < 6){
                                        //Line Empty
                                        aboveChecked = true;
                                        lines.add(i, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                    else if(!(lines.get(i-1).substring(0, 6).equals("/*line"))){
                                        aboveChecked = true;
                                        lines.add(i, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                }

                                //Check line 2 below
                                if(aboveChecked){
                                    if(lines.get(i+2).length() <= 6){
                                        //Line Empty
                                        lines.add(i+2, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                    else if(!(lines.get(i+2).substring(0, 6).equals("/*line"))){
                                        lines.add(i+2, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                } else{
                                    //Check line 1 below
                                    if(lines.get(i+1).length() <= 6){
                                        //Line Empty
                                        lines.add(i+1, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                    else if(!(lines.get(i+1).substring(0, 6).equals("/*line"))){
                                        lines.add(i+1, "/*line"+addLineCount+"*/");
                                        addLineCount++;
                                    }
                                }
                            }
                        }


                        BufferedWriter writer = new BufferedWriter(getPrivilegedWriter(tweeFile.getDirectoryPath()));
                        for (String curLine : lines) {
                            writer.write(curLine);
                            writer.newLine();
                        }
                        writer.close();
                    } catch(IOException e){
                        LOGGER.log(Level.SEVERE, "Error in setupPassageTextLocations()", e);
                    }
                }
            }
        }
    }


    //0 = no errors
    private static int writeToTwee(String filePath, ArrayList<String> targetString, ArrayList<String> insertStrings, ArrayList<String> failReq, boolean ifFailReqChecksFullLine){
        try {
            //Create FileReader with read privileges
            Reader r;
            r = AccessController.doPrivileged(new PrivilegedExceptionAction<Reader>(){
                public Reader run() throws IOException{
                    return new FileReader(filePath);
                }
            });

            BufferedReader bufferedReader = new BufferedReader(r);
            String line;
            ArrayList<String> lines = new ArrayList<>();

            //Record all lines
            while((line=bufferedReader.readLine()) != null){
                lines.add(line);
            }

            bufferedReader.close();
            r.close();

            //Find Target Line
            int targetIndex = -1;
            int targetDepth = 0;
            for (int i = 0; i < lines.size(); i++) {
                String curLine = lines.get(i);

                if(curLine.equals(targetString.get(targetDepth))){
                    if(targetDepth == targetString.size()-1){
                        targetIndex = i;
                        break;
                    }else{
                        targetDepth++;
                    }
                }
                else if(ifFailReqChecksFullLine){
                    if(curLine.equals(failReq.get(targetDepth))){
                        return 1;
                    }
                }else{
                    CharSequence sequence = failReq.get(targetDepth);
                    if(curLine.contains(sequence)){
                        return 2;
                    }
                }
            }

            //Place text
            if(targetIndex != -1){
                targetIndex++;
                for (int i = insertStrings.size()-1; i >= 0; i--) {
                    lines.add(targetIndex, insertStrings.get(i));
                }

                //Create FileWriter with write privileges
                Writer w;
                w = AccessController.doPrivileged(new PrivilegedExceptionAction<Writer>(){
                    public Writer run() throws IOException{
                        return new FileWriter(filePath);
                    }
                });

                //Rewrite file
                BufferedWriter bufferedWriter = new BufferedWriter(w);
                for (String curLine : lines) {
                    bufferedWriter.write(curLine);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } else{
                LOGGER.severe("Failed to find targetString");
            }
            
            return 0;
        } catch (IOException|PrivilegedActionException|AccessControlException ex) {
            LOGGER.log(Level.SEVERE, ("Error occured while writing to "+filePath), ex);
        }
        return 3;
    }


    private static Reader getPrivilegedReader(File file){
        try{
            //Create FileReader with read privileges
            Reader r;
            r = AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<Reader>(){
                public Reader run() throws IOException{
                    return new FileReader(file);
                }
            });
            return r;
        } catch(PrivilegedActionException | AccessControlException e){
            LOGGER.log(Level.SEVERE, "Error occurred while getting Privileged Reader", e);
        }
        

        //If error occurred
        return null;
    }


    private static Writer getPrivilegedWriter(File file){
        try{
            //Create FileWriter with write privileges
            Writer w;
            w = AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<Writer>(){
                public Writer run() throws IOException{
                    return new FileWriter(file);
                }
            });
            return w;
        } catch(PrivilegedActionException e){
            LOGGER.log(Level.SEVERE, "Error occurred while getting Privileged Reader", e);
        }

        //If error occurred
        return null;
    }


    private static void writeNewFile(Writer w, ArrayList<String> text){
        BufferedWriter bufferedWriter = new BufferedWriter(w);
        try{
            for (String curLine : text) {
                bufferedWriter.write(curLine);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error occurred in writeNewFile", e);
        }
    }



    //Start Get Methods



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
        throw new InvalidLocationException("Error finding DolLocation (Name) : " + name);
    }


    public static DolLocation getDolLocation(File path) throws InvalidLocationException{
        for (DolSubfolder subfolder : dolSubfolders) {
            for (DolLocation location : subfolder.getLocations()) {
                if(location.getDirectoryPath().equals(path)){
                    return location;
                }
            }
        }
        throw new InvalidLocationException("Error finding DolLocation (Path) : " + path);
    }


    public static TweeFile getTweeFile(DolLocation location, String tweeName) throws InvalidTweeFileException{
        for (TweeFile file : location.getFiles()) {
            if(file.getName().equals(tweeName)){
                return file;
            }
        }
        throw new InvalidTweeFileException("Error finding TweeFile (" + tweeName + ") in DolLocation (" + location.getName() +").");
    }


    public static DolPassage getDolPassage(String passageName) throws InvalidPassageException{
        for (DolSubfolder dolSubfolder : dolSubfolders) {
            for (DolLocation dolLocation : dolSubfolder.getLocations()) {
                for (TweeFile tweeFile : dolLocation.getFiles()) {
                    for (DolPassage dolPassage : tweeFile.getPassages()) {
                        if(dolPassage.getName().equals(passageName)){
                            return dolPassage;
                        }
                    }
                }                
            }
        }
        throw new InvalidPassageException("Error finding DolPassage (" + passageName + ")");
    }



    //End Get Methods

    //Start modApp methods



    public static void subscribeMod(Mod mod) {
        try{
            mod.setModFolder(new File(mod.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile());
            //Check if mod isn't in a folder.
            if(mod.getModFolder().getAbsolutePath().equals(getRunningPath()+"\\mods")){
                throw new InvalidModLocationException("Mod is located in the 'mods' folder. Mod must be in it's designated folder.");
            }
            //Check if mod isn't in a folder that has the same name as the mod
            LOGGER.info("Mod Folder Name : "+mod.getModFolder().getName());
            LOGGER.info("Mod Name : "+mod.getModName());
            if(!(mod.getModFolder().getName().equals(mod.getModName()))){
                throw new InvalidModLocationException("Mod's parent folder does not have the same name as the Mod's name.");
            }
            mods.add(mod);
            LOGGER.info("Added mod "+mod.getModName()+" @ " + mod.getModFolder().toString());
        } catch (URISyntaxException|InvalidModLocationException e){
            LOGGER.log(Level.SEVERE, "Error while loading mod " + mod.getModName(), e);
        }
    }


    public static void logMessage(String message){
        LOGGER.info(message);
    }


    public static void addPassageText(ArrayList<String> message, DolPassage passage){
        ArrayList<String> targets = new ArrayList<>();
        targets.add(":: "+passage.getName()+" [nobr]");
        targets.add("/*newtext*/");
        ArrayList<String> fail = new ArrayList<>();
        fail.add(null);
        fail.add("::");

        //Check Changed
        if(passage.hasChanged()){
            LOGGER.info("This passage has been previously changed. (" + passage.getName() + ")");    
        }
        
        int succeeded = writeToTwee(passage.getTweeFile().getAbsolutePath(), targets, message, fail, false);

        //writeToTwee Error
        if(succeeded > 0){
            LOGGER.severe("An error occured during addPassageText (Error Code: "+succeeded+")");
        } 
        try{
            //Set Changed
            passage.setHasChanged();
            TweeFile twee = getTweeFile(getDolLocation(new File(passage.getParentDirectory())), passage.getTweeFile().getName());
            twee.setHasChanged();
            getDolLocation(twee.getParent()).setHasChanged();
        } catch(InvalidLocationException e){
            LOGGER.log(Level.SEVERE, "An error occured while logging DolLocation change.", e);
        } catch(InvalidTweeFileException e){
            LOGGER.log(Level.SEVERE, "An error occured while logging TweeFile change.", e);
        }
    }


    public static ArrayList<String> getTextResource(Mod mod, String fileName){
        File resource;
        try{
            //The mod's folder path + \ + fileName  (Ex. "C:\...\Degrees of Lewdity\source\mods\Example Mod\" + fileName)
            resource = new File(mod.modFolder.getAbsolutePath()+"\\"+fileName);
            ArrayList<String> returnString = new ArrayList<>();
            
            if(resource.exists()){
                //Uses ModLoader's Access to read resource
                Reader reader = AccessController.doPrivileged(new PrivilegedExceptionAction<Reader>(){
                    public Reader run() throws IOException{
                        return new FileReader(resource);
                    }
                });

                //Writes each line of resource into returnString
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while((line=bufferedReader.readLine()) != null){
                    returnString.add(line);
                }
                bufferedReader.close();
                reader.close();

                return returnString;
            }
        } catch(PrivilegedActionException|AccessControlException|IOException e){
            LOGGER.log(Level.SEVERE, String.format("Error while getting mod resource ([mod: \"%1$s\"], [fileName: \"%2$s\"])", mod.getModName(), fileName), e);
        }

        //Return null if an error occured
        return new ArrayList<>();
    }


    public static ArrayList<String> readerToString(Reader reader){
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        ArrayList<String> lines = new ArrayList<>();
        try{
            //Record all lines
            while((line=bufferedReader.readLine()) != null){
                lines.add(line);
            }

            bufferedReader.close();
            return lines;
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error occured in readerToString", e);
        }

        return new ArrayList<>();
    }


    public static void overwritePassage(DolPassage passage, ArrayList<String> newText){
        if(passage.isOverwriten()){
            LOGGER.warning("This passage has been previously overwriten. (" + passage.getName() + ")");
        }

        //Get passage's Twee File
        File tweeLocation = new File(passage.getTweeFile().getAbsolutePath());
        ArrayList<String> twee = readerToString(getPrivilegedReader(tweeLocation));

        //Search for passage in Twee File
        //Search for passage index
        int startIndex = -1;
        for (int i = 0; i < twee.size(); i++){
            String curLine = twee.get(i);
            if(curLine.equals(":: "+passage.getName()+" [nobr]")){
                startIndex = i;
                break;
            }
        }

        if(startIndex != -1){
            int endIndex = -1;

            //Search for index before next passage
            for (int i = startIndex+1; i < twee.size(); i++){
                String curLine = twee.get(i);
                CharSequence finding = "::";
                if(curLine.contains(finding)){
                    endIndex = i-1;
                    break;
                }
            }

            
            if(endIndex != -1){
                //Remove passage text
                for (int i = endIndex; i != startIndex; i--) {
                    twee.remove(i);
                }
                twee.add(startIndex+1, "");

                //Add newText to passage
                startIndex += 2;
                for (int i = 0; i < newText.size(); i++) {
                    endIndex = startIndex+i;
                    twee.add(endIndex, newText.get(i));
                }
                twee.add(endIndex+1, "");

                //Write to twee file
                writeNewFile(getPrivilegedWriter(tweeLocation), twee);
                passage.setOverwriten();
            }
        }
    }


    //End modApp methods



    public static void main(String[] args) {
        Handler consoleHandler = null;
        Handler fileHandler = null;
        try{
            //Creating LOGGER Handlers
            consoleHandler = new ConsoleHandler();
            fileHandler = new FileHandler(getRunningPath()+"\\debug-log.log"); 

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
            LOGGER.log(Level.INFO, "Current Running Path : "+curRunningPath);

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
        File images = new File(new File(getRunningPath()).getParentFile().toString() + "\\img");
        
        LOGGER.info("Deleting old dol-files...");
        try{
            FileUtils.deleteDirectory(dolFilesLocation);
        } catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Error occured while deleting old dol-files.", ex);
        } catch(IllegalArgumentException ex){
            LOGGER.log(Level.WARNING, "Couldn't delete old dol-files, directory doesn't exist.", ex);
        }
        LOGGER.info("Deleted old dol-files.");

        if(images.isDirectory()){
            LOGGER.info("Deleting old imgs...");
            try{
                FileUtils.deleteDirectory(images);
            } catch(IOException ex){
                LOGGER.log(Level.SEVERE, "Error occured while deleting old img", ex);
            } catch(IllegalArgumentException ex){
                LOGGER.log(Level.WARNING, "Couldn't delete old img, directory doesn't exist.", ex);
            }
            LOGGER.info("Deleted old imgs.");
        }        

        LOGGER.info("Loading dol-files...");
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

        //Setup PassageTextLocations
        LOGGER.info("Placing text locations...");
        setupPassageTextLocations();
        LOGGER.info("Finished placing text locations.");

        //Load Mods
        LOGGER.info("Loading mods...");
        for (Mod mod : mods) {
            LOGGER.info("Loading " + mod.getModName() + " | Version: " + mod.getModVersion() + " | Author: " + mod.getModAuthor());
            mod.modApp();
        }
        LOGGER.info("Loaded mods.");

        //Load Images
        LOGGER.info("Loading imgs...");
        try{
            FileUtils.copyDirectory(new File(backupDir.toString() + "\\img"), images);
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Error occured while loading backup img files.", ex);
        }
        LOGGER.info("Loaded imgs.");


        //Create HTML
        LOGGER.info("Creating HTML...");
        try{
            String curRunningPath = getRunningPath() + "\\dol-files";
            String batchFile = getRunningPath() + "\\dol-files\\compile.bat";

            Process compile;
            compile = AccessController.doPrivileged(new PrivilegedExceptionAction<Process>(){
                public Process run() throws IOException{
                    return Runtime.getRuntime().exec(("cmd /c start \"\" \"")+batchFile+"\" && exit");
                }
            });

            //Process compile = Runtime.getRuntime().exec(("cmd /c start \"\" \"")+batchFile+"\" && exit"); //Open compile.bat in CMD
            synchronized(compile){
                int failSafe = 0;
                while(compile.isAlive() && failSafe <= 10){
                    compile.wait(1000);
                    failSafe++;
                }
            }
            if(compile.exitValue() != 0){
                throw new ProcessWarningException("An error occured while compiling HTML.", new Throwable());
            }
            String dolVersion = "NOVERSION";
            try(BufferedReader reader = new BufferedReader(new FileReader(curRunningPath+"\\DOL VERSION.txt"))){
                dolVersion = reader.readLine();
            } catch(IOException ex){
                LOGGER.log(Level.SEVERE, "Error occured while getting DOL version.", ex);
            }
            File html = new File(curRunningPath + "\\Degrees of Lewdity VERSION.html");
            File htmlDestination = new File(html.getParentFile().getParentFile().getParentFile().toPath() + "\\Degrees of Lewdity Modded.html");
            if(htmlDestination.exists()){
                Files.delete(htmlDestination.toPath());
            }
            if(!(html.renameTo(htmlDestination))){
                throw new SecurityException("Couldn't move and rename HTML.", new Throwable());
            }
        } catch(IOException|ProcessWarningException|SecurityException|PrivilegedActionException ex){
            LOGGER.log(Level.SEVERE, "Error occured while creating HTML.", ex);
        } catch(InterruptedException ex){
            LOGGER.log(Level.SEVERE, "Error occured while creating HTML.", ex);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Created HTML.");
    }
}
