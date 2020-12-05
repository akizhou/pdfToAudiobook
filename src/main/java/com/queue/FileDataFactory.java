package com.queue;

import java.io.File;

public class FileDataFactory implements DataFactory {

    public IFileData Create(File file, int id)
    {
        return new FileData(file, id);
    }
}
