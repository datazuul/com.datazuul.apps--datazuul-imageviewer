package com.datazuul.apps.imageviewer2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * author Dan Messerschmidt CSC241 Title: ImageDir.java Description: Class used by ImageViewer to do file functions
 */
public class ImageDir {

    private FileInputStream f_in;     //file input stream to read data
    private FileOutputStream f_out;		//file output stream to save a file
    private File[] fIndex;   //file pointer to current directory
    private File file;     //current open file 
    private byte[] data;      //buffer to read file into
    private int index, //file index within directory
            length;    //file length in bytes

    /**
     * default constructor
     */
    public ImageDir() {
    }

    /**
     * member function openFile() selects a file from a directory
     */
    public File openFile() {
        JFileChooser selFile = new JFileChooser();

        selFile.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = selFile.showOpenDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        }

        File fName = selFile.getSelectedFile();
        if (fName == null || fName.getName().equals("")) {
            JOptionPane.showMessageDialog(null, "Invalid File Name",
                    "Invalid File Name", JOptionPane.ERROR_MESSAGE);
        } else {
            file = fName;
        }

        return file;
    }

    /**
     * member function openDir() selects a directory for opening files
     */
    public File[] openDir() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        }

        File[] directory = fileChooser.getCurrentDirectory().listFiles();

        return directory;

    }  //openDir

    /**
     * member function saveFile() saves currently open file, allows for new file name
     */
    public void saveFile() {
        JFileChooser saveChooser = new JFileChooser();
        FileOutputStream sFile = null;

        saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = saveChooser.showSaveDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File sName = saveChooser.getSelectedFile();

        try {
            if (sName == null || sName.getName().equals("")) {
                JOptionPane.showMessageDialog(null, "Invalid File Name",
                        "Invalid File Name", JOptionPane.ERROR_MESSAGE);
            } else {
                sFile = new FileOutputStream(sName);
            }

            sFile.write(data);      //use last file data with new file name
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }   //saveFile

    /**
     * member function getDir() sets index and file pointer to beginning to beginning of selected directory
     */
    public void getDir() {
        index = 0;
        fIndex = openDir();
    }

    /**
     * member function getImg() opens filestream and reads bytes into buffer local member function, called by
     * getFileLength(0
     */
    public void getImg() {
        try {
            f_in = new FileInputStream(fIndex[index]);
            length = (int) fIndex[index].length();
            data = new byte[length];
            f_in.read(data);
            f_in.close();
        } catch (FileNotFoundException ex) {
            doLog(ex);
            //out.setText ("File Not Found");
        } catch (IOException ioex) {
            doLog(ioex);
        }
    }   //displayImg()

    /**
     * member function pQuit() exits program when quit button pushed its in this class to close filestreams
     */
    public void pQuit() {
        try {
            if (f_in != null) {
                f_in.close();
            }
            if (f_out != null) {
                f_out.close();
            }
        } catch (IOException ex) {
            doLog(ex);
        }

        System.exit(0);
    }

    /**
     * member function getFileName() returns file name of selected file if directory is not selected, index is always 0
     * else index is location in current directory
     */
    public String getFileName(int option) {
        if (option == 1) {
            index = 0;                //reset index if not in directory mode
            fIndex = new File[1];
            file = openFile();
            fIndex[index] = file;
        }
        return fIndex[index].getPath();
    }

    /**
     * member function getFileLength() returns length in bytes of selected file
     */
    public int getFileLength() {
        getImg();
        int l = data.length;
        return l;
    }

    /**
     * member function getImageByte() returns byte array of image from selected file
     */
    public byte[] getImageByte() {
        return data;
    }

    /**
     * member function setNextIndex() selects the next file in current directory
     */
    public void setNextIndex() {
        if (index < fIndex.length - 1) {
            index++;
        } else {
            index = 0;		//if at the end of directory, go to the beginning
        }
    }

    /**
     * member function setPrevIndex() selects previous file in current directory
     */
    public void setPrevIndex() {
        if (index > 0) {
            index--;
        } else {
            index = fIndex.length - 1;   //if at the beginning, wrap to the end
        }
    }

    /**
     * member function getIndex() returns file index of selected directory, 1 based
     */
    public String getIndex() {
        return ((index + 1) + " of " + fIndex.length);
    }

    /**
     * member function doLog() prints stacktrace for catch/try
     */
    public void doLog(Exception e) {
        StringWriter eStack = new StringWriter();
        e.printStackTrace(new PrintWriter(eStack));

        e.printStackTrace();
    }
}     //class ImageDir

