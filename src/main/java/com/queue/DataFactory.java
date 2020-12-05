package com.queue;

import java.io.File;

public interface DataFactory {
    IFileData Create(File file, int id);
}
