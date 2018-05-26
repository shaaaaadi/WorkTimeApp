package com.example.jubransh.workingtime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is a Static (Final) Class which is used to remove, edit or delete files from android device
 *
 * @author  Shadi Jubran
 * @version 1.0
 * @since   01/09/2017
 */

public final class FileManager
{
    /**
     * This method creates new empty folder into the android device
     * this methods create folder in case it's not exist
     * @param location wanted location the folder wanted to be created in
     * @return boolean creating passed or failed.
     */
    public static boolean createNewDir(String location)
    {
        File appDir = new File(location);
        if(appDir.exists() == false)
        {
            return appDir.mkdir();
        }
        return true;
    }

    /**
     * This method creates new empty file into the android device
     * this methods creates the file in case it's not exist
     * @param location wanted location the folder wanted to be created in
     * @param fileName the file name
     * @return File the created file object to be used.
     */
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
            return null;
        }

        return newFile;
    }

    /**
     * This method delete all the data (text) from the referred file
     * @param fileToDeleteItContent as File object
     * @return File the Cleaned file object to be used.
     */
    public static File deleteAllData(File fileToDeleteItContent)
    {
        if(fileToDeleteItContent == null)
            return null;
        String fileLocation = fileToDeleteItContent.getParent().toString();
        String fileName = fileToDeleteItContent.getName();
        fileToDeleteItContent.delete();
        return FileManager.createNewFile(fileLocation, fileName);
    }

    /**
     * This method reaad all lines from file and fill them into string array
     * each item into the array represents one line of the wanted file
     * @param fileToRead as File object to be read
     * @return File the Cleaned file object to be used.
     */
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

    /**
     * This method appends new line into existing file
     * @param myFile as File object.
     * @param body as string, the line should be added to the file
     * @return nothing
     */
    public static void writeLine(File myFile, String body)
    {
        writeToFile(myFile, body, true);
    }

    /**
     * This method appends new lines into existing file
     * @param f as File object.
     * @param lines as string array, the lines should be added to the file
     * @return nothing
     */
    public static void writeAllLines(File f, String[] lines)
    {
        for(int i=0; i<lines.length; i++)
        {
            writeToFile(f, lines[i], true);
        }
    }

    /**
     * This method checks if the file exists in the given location
     * @param parentDirPath file location into the android device (as string).
     * @param fileName file name (as string)
     * @return boolean, true if the file exists, and false else
     */
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

    /**
     * This Private method is used by internal methods like
     *      1. writeAllLines
     *      2.writeLine
    */
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

}
