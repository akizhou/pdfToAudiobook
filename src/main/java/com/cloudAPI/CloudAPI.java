package com.cloudAPI;

import com.google.cloud.storage.StorageException;

import javax.naming.TimeLimitExceededException;
import java.io.File;
import java.io.IOException;

public interface CloudAPI {
    void uploadFile(File rawFile) throws StorageException, IOException;
    void downloadFile(String path) throws StorageException, IOException, TimeLimitExceededException;
}
