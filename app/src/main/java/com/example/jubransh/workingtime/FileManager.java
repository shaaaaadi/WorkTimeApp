package com.example.jubransh.workingtime;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by jubransh on 10/29/2016.
 */

public final class FileManager
{
    public static boolean createNewDir(String location)
    {
        File appDir = new File(location);
        if(appDir.exists() == false)
        {
            return appDir.mkdir();
        }
        return true;
    }
    public static File createNewFile(String location, String fileName)
    {
        File newFile = null;
        try
        {
            newFile = new File(location + "/" + fileName);
            newFile.createNewFile();

        }
        catch(Exception e)
        {
        }

        return newFile;
    }

    public static File deleteAllData(File fileToDeleteItContent)
    {
        if(fileToDeleteItContent == null)
            return null;
        String fileLocation = fileToDeleteItContent.getParent().toString();
        String fileName = fileToDeleteItContent.getName();
        fileToDeleteItContent.delete();
        return FileManager.createNewFile(fileLocation, fileName);
    }
    public static String[] ReadAllLines(File fileToRead)
    {
        String line = null;
        String[] rows = null;

        try
        {
            FileInputStream fileInputStream = new FileInputStream (fileToRead);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            rows = line.split("\n");

            bufferedReader.close();
        }
        catch(FileNotFoundException ex)
        {
        }
        catch(IOException ex)
        {
        }
        return rows;
    }

    public static void writeLine(File myFile, String body)
    {
        writeToFile(myFile, body, true);
    }
    public static void writeAllLines(File f, String[] lines)
    {
        for(int i=0; i<lines.length; i++)
        {
            writeToFile(f, lines[i], true);
        }
    }
    public static void write(File myFile, String body)
    {
        writeToFile(myFile, body, false);
    }
    private static void writeToFile(File myFile, String body, boolean isToWriteNewLine)
    {
        FileOutputStream fos = null;

        if(isToWriteNewLine)
            body = body+"\r\n";

        try
        {
            fos = new FileOutputStream(myFile, true);

            fos.write(body.getBytes());
            fos.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static boolean isFileExists(String parentDirPath)
    {
        File[] listOfFiles = new File(parentDirPath).listFiles();
        if(listOfFiles == null || listOfFiles.length == 0)
            return false;
        return true;
    }
    public static boolean isFileExists(String parentDirPath, String fileName)
    {
        File[] listOfFiles = new File(parentDirPath).listFiles();
        if(listOfFiles == null || listOfFiles.length == 0)
            return false;

        for(int i=0; i<listOfFiles.length; i++)
        {
            if(listOfFiles[i].getName().equalsIgnoreCase(fileName))
                return true;
        }
        return false;
    }
}
