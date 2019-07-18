/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.fat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;
import java.util.List;


public class ZipCachingIntrospectorOutput{
    
    private String introspectorDescription, entryCacheSettings, zipReaperSettings, handleIntrospection, zipEntryCache, zipReaperValues;
    private String activeAndPendingIntrospection;
    private String pendingQuickIntrospection;
    private String pendingSlowIntrospection;
    private String completedIntrospection;

    /*
    private PropertyIntrospection parsePropertyLine(String description, String line){
        String[] splitLine = line.split("\\s+[\\[\\]\\s]+\\s*");
        return new PropertyIntrospection(description, splitLine[1], splitLine[2], splitLine[3]);
    }
    */

    public ZipCachingIntrospectorOutput(InputStream in) throws IOException{
        BufferedReader zipCachingReader = new BufferedReader(new InputStreamReader(in));
        String currentLine, aggregateLine;

        zipCachingReader.readLine();//="The description of this introspector:"

        introspectorDescription = zipCachingReader.readLine();//="Liberty zip file caching diagnostics"
        
        do{
            currentLine = zipCachingReader.readLine();
        }while(!(currentLine.equals("No ZipCachingServiceImpl configured") || currentLine.equals("Entry Cache Settings:")));

        //if there was no ZipCachingService to introspect then return early
        if(currentLine.equals("No ZipCachingServiceImpl configured")){
            zipCachingReader.close();
            return;
        }

        aggregateLine = "";
        currentLine = zipCachingReader.readLine();
        while(!currentLine.equals("Zip Reaper Settings:")){
            if(currentLine.equals("") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
            }

            currentLine = zipCachingReader.readLine();
        }

        entryCacheSettings = aggregateLine;
        aggregateLine = "";

        currentLine = zipCachingReader.readLine();
        while(currentLine.equals("") == false){
            aggregateLine = aggregateLine.concat(currentLine.concat("\n"));

            currentLine = zipCachingReader.readLine();
        }

        zipReaperSettings = aggregateLine;
        aggregateLine = "";

        zipCachingReader.readLine();//="The entry cache is a cache of small zip file entries."
        zipCachingReader.readLine();//="The entry cache is disabled if either setting is 0."
        zipCachingReader.readLine();//=""
        zipCachingReader.readLine();//="The zip reaper is a service which delays closes of zip files."
        zipCachingReader.readLine();//="The zip reaper is disabled if the maximum pending closes setting is 0."
        zipCachingReader.readLine();//=""
        zipCachingReader.readLine();//="Active and Cached ZipFile Handles:"

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** NONE **") == false){
            while(currentLine.equals("") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            aggregateLine = null;
            zipCachingReader.readLine();
        }
        
        handleIntrospection = aggregateLine;
        aggregateLine = "";

        zipCachingReader.readLine();//="Zip Entry Cache:"

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** DISABLED **") == false){
            while(currentLine.equals("") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            aggregateLine = null;
            zipCachingReader.readLine();
        }

        zipEntryCache = aggregateLine;
        aggregateLine = "";

        zipCachingReader.readLine();//="Zip Reaper:"

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** DISABLED **") == false){
            while(currentLine.equals("Active and Pending Data:") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            //nothing after Zip Reaper: if it is disabled
            zipCachingReader.close();
            return;
        }

        zipReaperValues = aggregateLine;
        aggregateLine = "";

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** NONE **") == false){
            while(currentLine.equals("Zip File Data [ pendingQuick ]") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            aggregateLine = null;
            zipCachingReader.readLine();
            currentLine = zipCachingReader.readLine();//="Zip File Data [ pendingQuick ]"
        }

        activeAndPendingIntrospection = aggregateLine;
        aggregateLine = "";

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** NONE **") == false){
            while(currentLine.equals("Zip File Data [ pendingSlow ]") == false){
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            aggregateLine = null;
            zipCachingReader.readLine();
            currentLine = zipCachingReader.readLine();//="Zip File Data [ pendingSlow ]"
        }

        pendingQuickIntrospection = aggregateLine;
        aggregateLine = "";

        currentLine = zipCachingReader.readLine();
        if(currentLine.equals("  ** NONE **") == false){
            while(!(currentLine.equals("Zip File Data [ completed ]") || currentLine.equals("Completed zip file data is not being tracked"))) {
                aggregateLine = aggregateLine.concat(currentLine.concat("\n"));
    
                currentLine = zipCachingReader.readLine();
            }
        }
        else{
            aggregateLine = null;
            zipCachingReader.readLine();
            currentLine = zipCachingReader.readLine();//="Zip File Data [ completed ]"
        }

        pendingSlowIntrospection = aggregateLine;
        aggregateLine = "";

        if(currentLine.equals("Completed zip file data is not being tracked")){
            zipCachingReader.close();
            return;
        }

        zipCachingReader.readLine();//=""
        currentLine = zipCachingReader.readLine();
        while(currentLine != null){
            aggregateLine = aggregateLine.concat(currentLine.concat("\n"));

            currentLine = zipCachingReader.readLine();
        }

        completedIntrospection = aggregateLine;

        zipCachingReader.close();
    }

    private static boolean matchMany(String line, String... headers){
        for(String header: headers){
            if(line.equals(header)){
                return true;
            }
        }
        return false;
    }

    private static String fastForward(BufferedReader zipCachingReader, String header) throws IOException{
        String currentLine = zipCachingReader.readLine();
        String section = "";
        if(currentLine == null){
            return section;
        }
        while(currentLine != null && !matchMany(currentLine,header)){
            section += currentLine + "\n";

            currentLine = zipCachingReader.readLine();
        }

        return section;
    }

    // private String introspectorDescription, entryCacheSettings, zipReaperSettings, handleIntrospection, zipEntryCache, zipReaperValues;
    // private String activeAndPendingIntrospection;
    // private String pendingQuickIntrospection;
    // private String pendingSlowIntrospection;
    // private String completedIntrospection;
    private String zipCachingTime;
    private String outputFormat;
    private String activeAndCachedZipFileHandles;

    //new constructor for parsing the output zip caching introspector
    public ZipCachingIntrospectorOutput(InputStream in, Object o) throws Exception{
        BufferedReader zipCachingReader = new BufferedReader(new InputStreamReader(in));
        String currentLine, section;
        final String EOF = "------------------------------------------------------------";

        int count = 0;
        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", "Starting the constructor: " + Integer.toString(++count,10));
        
        //description of this introspector
        currentLine = zipCachingReader.readLine();

        //"Liberty zip file caching diagnostics"
        introspectorDescription = zipCachingReader.readLine();
        
        fastForward(zipCachingReader, "Zip Caching Service:");
        
        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        zipCachingTime = fastForward(zipCachingReader, "Format:");

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        outputFormat = fastForward(zipCachingReader, "Entry Cache Settings:");

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        entryCacheSettings = fastForward(zipCachingReader, "Zip Reaper Settings:");
        
        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        zipReaperSettings = fastForward(zipCachingReader, "Active and Cached ZipFile Handles:");
        
        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        //handleintrospections + zipEntryCache = activeAndCachedZipFileHandles
        //"Active and Cached ZipFile Handles:" => "Zip Reaper:"
        section = fastForward(zipCachingReader, "Zip Reaper:");
        {
            if(section.contains("  ** NONE **")){
                activeAndCachedZipFileHandles = null;
            }
            else{
                activeAndCachedZipFileHandles = section;
            }
        }

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        //zipReaperValues
        //"Zip Reaper:" => "Active and Pending Data:" || EOF
        section = fastForward(zipCachingReader, "Active and Pending Data:");
        //check if DISABLED if so then set the rest of the variables to be null and return
        {
            currentLine = zipCachingReader.readLine();

            if(currentLine.equals("  ** DISABLED **")){
                return;
            }
            else{
                zipReaperValues = section;
            }
        }

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        //activeAndPendingIntrospection
        //"Active and Pending Data:" => "Zip File Data [ pendingQuick ]"
        section = fastForward(zipCachingReader, "Zip File Data [ pendingQuick ]");
        //check if is NONE if so then set to null else set as section
        {  
            if(section.contains("  ** NONE **")){
                activeAndPendingIntrospection = null;
            }
            else{
                activeAndPendingIntrospection = section;
            }

        }

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        //pendingQuickIntrospection
        //"Zip File Data [ pendingQuick ]" => "Zip File Data [ pendingSlow ]"
        section = fastForward(zipCachingReader, "Zip File Data [ pendingSlow ]");
        //check if contains NONE if so then set to null else set as section
        {
            if(section.contains("  ** NONE **")){
                pendingQuickIntrospection = null;
            }
            else{
                pendingQuickIntrospection = section;
            }
        }

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

        currentLine = zipCachingReader.readLine();
        section = "";

        //while the current line isn't
        //  start of completed storage
        //  not being tacked message
        //  end of file

        while(!matchMany(currentLine, "Zip File Data [ completed ]", "Completed zip file data is not being tracked", EOF)){
            section += currentLine + "\n";
            currentLine = zipCachingReader.readLine();
        }

        FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));
        //assign pending slow introspections
        if(section.contains("  ** NONE **")){
            pendingSlowIntrospection = null;
        }
        else{
            pendingSlowIntrospection = section;
        }

        //check the current line if it stopped at the start of completed storage or the non tracked line
        if(currentLine.equals("Zip File Data [ completed ]")){

            section = fastForward(zipCachingReader, EOF);

            FATLogging.info(ZipCachingIntrospectorOutput.class,"Construct", Integer.toString(++count,10));

            if(section.contains("  ** NONE **")){
                completedIntrospection = null;
            }
            else{
                completedIntrospection = section;
            }


        }
        else if(currentLine.equals("Completed zip file data is not being tracked")){
            completedIntrospection = null;
        }
        else if(currentLine.equals(EOF)){
            //should not get to the EOF without reaching one of the two lines above
            throw new RuntimeException("Reached EOF unexpectedly for Introspector Output");
        }
        else{
            //should not get to else since output should have 1 of the first 2 conditionals
            throw new RuntimeException("Malformed Introspector Output");
    }


    }

    public String getIntrospectorDescription() {
        return introspectorDescription;
    }

    public String getEntryCacheSettings() {
        return entryCacheSettings;
    }

    public String getZipReaperSettings() {
        return zipReaperSettings;
    }

    public String getHandleIntrospection() {
        return handleIntrospection;
    }

    public String getZipEntryCache() {
        return zipEntryCache;
    }

    public String getZipReaperValues() {
        return zipReaperValues;
    }

    public String getActiveAndPendingIntrospection() {
        return activeAndPendingIntrospection;
    }

    public String getPendingQuickIntrospection() {
        return pendingQuickIntrospection;
    }

    public String getPendingSlowIntrospection() {
        return pendingSlowIntrospection;
    }

    public String getCompletedIntrospection() {
        return completedIntrospection;
    }

    public String getActiveAndCachedZipFileHandles(){
        return activeAndCachedZipFileHandles;
    }

    

    public List<String> getZipHandleArchiveNames(){
        if(getHandleIntrospection() == null)
            return null;
        else{
            List<String> handles = new LinkedList<String>();
            //pattern will capture the file name at the end of the path including comma and directory seperator
            //open-liberty/dev/build.image/wlp/usr/servers/com.ibm.ws.artifact.zipReaper/apps[/testServlet1.war,]
            Pattern p = Pattern.compile("[/\\\\][^/:\\*\\?\\\"<>\\|\\\\]+\\.[ewj]ar,");
            for(String line : getHandleIntrospection().split("\n")){
                if(hasAValidGroup(line, p)){
                    //match for the archive name and remove the comma and directory seperator
                    handles.add(getFirstGroup(line,p, "\\/," ));
                }
            }
            return handles;
        }
        
    }

    public String getZipReaperThreadState(){
        String zipReaperValues = getZipReaperValues();

        //if the output doesn't have "** DISABLED **"
        if(zipReaperValues != null){
            String[] reaperValueLines = zipReaperValues.split("\n");
            for(String line: reaperValueLines){
                if(line.contains("State")){
                    Pattern p = Pattern.compile("\\[.+\\]");
                    if(hasAValidGroup(line, p)){
                        return getFirstGroup(line, p, "[]");
                        
                    }
                }
            }
        }

        return null;
    }

    public String getZipReaperRunnerDelay(){
        String zipReaperValues = getZipReaperValues();

        if(zipReaperValues != null){
            String[] reaperValuesLines = zipReaperValues.split("\n");
            for(String line: reaperValuesLines){
                if(line.contains("Next Delay")){
                    //    Next Delay    [ INDEFINITE (s) ]
                    //    Next Delay    [ ######## (s) ]
                    //Pattern p = Pattern.compile("\\[ .+ \\(s\\) \\]");
                    Pattern p = Pattern.compile("\\[ .+ \\]");
                    if(hasAValidGroup(line, p)){
                        return getFirstGroup(line, p, "[]()s");
                    }
                }
            }
        }


        return null;
    }

    public List<String> getOpenAndActiveArchiveNames(){
        String rawOpen = getActiveAndPendingIntrospection();
        List<String> returnValue = new LinkedList<String>();
        if(rawOpen == null){
            return returnValue;
        }
        
        Pattern p = Pattern.compile("[^/\\\\]+\\.[ewj]ar");
        for(String introspection: rawOpen.split("\n")){
            if(introspection.contains("ZipFile")){
                Matcher m = p.matcher(introspection);
                if(m.find()){
                    returnValue.add(m.group());
                }
            }
        }
        return returnValue;
    }

    public List<String> getAllZipFileDataIntrospections(){
        List<String> returnValue = new LinkedList<String>();

        addZipFileDataIntrospection(returnValue, getActiveAndPendingIntrospection());
        //pendingQuick and pendingSlow introspections are sparse so the full output will only be in active/pending and completed
        addZipFileDataIntrospection(returnValue, getCompletedIntrospection());

        return returnValue;
    }

    private static void addZipFileDataIntrospection(List<String> aggregateList, String rawIntrospection){
        //if there is no raw input then do nothing
        if(rawIntrospection != null){   
            String aggregate = "";

            for(String introspectLine: rawIntrospection.split("\n")){
                //pattern for the first line of the ZipFileData.introspect() output
                if(introspectLine.matches("ZipFile\\s\\[\\s.+\\s\\]")){
                    if(!aggregate.equals("")){
                        aggregateList.add(aggregate);
                        
                    }
                    
                    aggregate = introspectLine + "\n";
                }
                else{
                    aggregate = aggregate.concat(introspectLine + "\n");
                }

            }
            
            if(!aggregate.equals("")){
                aggregateList.add(aggregate);
            }
            
        }
    }

    private static boolean hasAValidGroup(String introspectLine, Pattern matchPattern){
        Matcher match = matchPattern.matcher(introspectLine);
        return match.find();
        
    }

    private static String getFirstGroup(String introspectLine, Pattern matchPattern, CharSequence toRemove){
        Matcher match = matchPattern.matcher(introspectLine);
        if(match.find()){
            String group = match.group();
            for(int character = 0; character < toRemove.length(); character++){
                group = group.replace(toRemove.subSequence(character, character + 1),"");
            }

            return group.trim();
        }
        else{
            return null;
        }
    }
}