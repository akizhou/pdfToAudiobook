package com.queue;

import java.io.File;
import java.util.List;

public interface FileQueue {
    void Add(File file);
    IFileData Remove();
    void Remove(IFileData fileData);
    List<IFileData> GetQueue();
    void SetQueue(List<IFileData> list);
}
