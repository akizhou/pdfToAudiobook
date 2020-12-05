package com.queue;

import java.io.File;
/**
 * This class holds methods to access the FileData object, the primary object used within the pdf queue.
 * for PDF2Audiobook project.
 *
 * Author: Kirill Melnikov
 * Last updated: 2020-12-1 by Kirill Melnikov
 */

public class FileData implements IFileData {

    private int id;
    private File file;
    private String path;

    public FileData(File file, int id)
    {
        this.file = file;
        this.id = id;
        this.path = file.getPath();
    }

    public File getFile()
    {
        return file;
    }

    public int getID()
    {
        return id;
    }

    public String getFilePath()
    {
        return path;
    }
}
