package com.cloudAPI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.enums.Gender;
import com.enums.Language;
import com.exceptions.CredentialsKeyError;
import com.exceptions.MACAddressError;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;

import javax.naming.TimeLimitExceededException;


/**
 * This class has methods to handle file upload and download to/from google cloud storage buckets
 * for PDF2Audiobook project.
 *
 * Author: Aki Zhou
 * Last updated: 2020-11-19 by Aki Zhou
 */
public class GoogleCloudAPI implements CloudAPI {

    /***** Attributes *****/

    private final Storage storage;
    private final String rawFileBucketID;
    private final Bucket rawFileBucket;
    private final String outFileBucketID;
    private final Bucket outFileBucket;
    private final FilenameManager nameManager;

    public GoogleCloudAPI() throws CredentialsKeyError {
        throw new CredentialsKeyError();
    }

    // For testing purpose
    public GoogleCloudAPI(String pathServiceAccountKey) throws CredentialsKeyError {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(pathServiceAccountKey));
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            rawFileBucketID = null;
            rawFileBucket = null;
            outFileBucketID = null;
            outFileBucket = null;
            nameManager = null;
        }
        catch (IOException exc) {
            exc.printStackTrace();
            throw new CredentialsKeyError();
        }
    }

    public GoogleCloudAPI(String pathServiceAccountKey, String rawBucketName, String outBucketName)
            throws CredentialsKeyError, MACAddressError {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(pathServiceAccountKey));
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            rawFileBucketID = rawBucketName;
            rawFileBucket = storage.get(rawFileBucketID);
            outFileBucketID = outBucketName;
            outFileBucket = storage.get(outFileBucketID);
            nameManager = new FilenameManager();
        }
        catch (IOException | StorageException exc) {
            exc.printStackTrace();
            throw new CredentialsKeyError();
        }
    }

    /***** Getter & Setter *****/

    public String getRawFileBucketID() {
        return rawFileBucketID;
    }

    public String getOutFileBucketID() {
        return outFileBucketID;
    }

    /**
     * This method attempts to upload a file to a google cloud storage bucket.
     * @param rawFile  The file being uploaded.
     * @throws StorageException  Thrown when access to gcd fails when calling duplicateNameExists().
     * @throws IOException  Thrown when upload fails due to failure of getting file to upload.
     */
    public void uploadFile(File rawFile) throws StorageException, IOException {
        try {
            // Load filename information into nameManager for later use.
            nameManager.init(rawFile.getName());
            if (duplicateNameExists()) {
                nameManager.timestampBaseName();
            }
            // Create object (upload file) on gcs rawFileBucket.
            System.out.println("Uploading");
            rawFileBucket.create(nameManager.getUploadName(), Files.readAllBytes(Paths.get(rawFile.getCanonicalPath())));
        }
        catch (IOException exc) {
            throw new IOException("Failed to read " + rawFile.getName() + "before uploading");
        }
    }

    /**
     * This method attempts to download a file from a google cloud storage bucket.
     * @throws StorageException  Thrown when access to gcd fails when calling fileProcessed().
     * @throws IOException  Thrown when download fails due to failure of creating a placeholder file.
     */
    public void downloadFile(String path) throws StorageException, IOException, TimeLimitExceededException {
        final long TIMEOUT_LIMIT = 540000;  // 9 minutes
        // Wait in a loop until the file is processed.
        try {
            System.out.println("Waiting for file to be processed");
            long start = System.currentTimeMillis();
            while (!fileProcessed()) {
                TimeUnit.MILLISECONDS.sleep(1);
                if (System.currentTimeMillis()-start >= TIMEOUT_LIMIT) {
                    throw new TimeLimitExceededException();
                }
            }
            System.out.println("File processed");
        }
        catch (InterruptedException exc) {
            exc.printStackTrace();
        }

        // Notify nameManager about user defined download directory.
        nameManager.setPathToFile(path);

        // Prepare an empty file for which data will be downloaded to.
        String filePath = nameManager.getDownloadPath(false);
        File audioFile = new File(filePath);
        String defaultPath = System.getProperty("user.dir") + File.separator + "audioFiles" + File.separator;

        // Make a directory if it doesn't exist yet
        Path dirPath = Paths.get(nameManager.getPathToFile());
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        if (audioFile.createNewFile()) {
            if (filePath.contains(defaultPath)) {
                System.out.println("Downloading to default location as: " + filePath);
            }
            else {
                System.out.println("Downloading to user defined location as: " + filePath);
            }
        }
        else {
            // File with the same name already exists, so padding timestamp to avoid overwriting.
            filePath = nameManager.getDownloadPath(true);
            audioFile = new File(filePath);
            if (audioFile.createNewFile()) {
                if (filePath.contains(defaultPath)) {
                    System.out.println("Downloading to default location as: " + filePath);
                }
                else {
                    System.out.println("Downloading to user defined location as: " + filePath);
                }
            }
            else {
                // If for any unexpected reasons, such as timestamp padded name also already exists, which it
                // shouldn't, throw an exception.
                throw new IOException("The unexpected exception has happened when trying to create download file");
            }
        }

        // Fetch target object from gcs outFileBucket and write its data to prepared file.
        Blob blob = outFileBucket.get(nameManager.getDownloadName());
        if (blob == null) {
            // For unexpected reasons, the file was not found on gcs which shouldn't happen because processed()
            // must return true before calling this method, meaning the file should exist on gcs
            throw new IOException("The unexpected exception has happened when trying to fetch data from gcs");
        }
        blob.downloadTo(audioFile.toPath());

        // Delete uploaded and downloaded files from gcs buckets since at this stage we don't store them for users.
        storage.delete(outFileBucketID, nameManager.getDownloadName());
        storage.delete(rawFileBucketID, nameManager.getUploadName());
    }

    /**
     * This method checks if an uploaded file has been processed.
     * @return  Boolean condition for completed or not.
     * @throws StorageException  Thrown when the access to gcs fails.
     */
    private boolean fileProcessed() throws StorageException {
        return outFileBucket.get(nameManager.getDownloadName()) != null;
    }

    /**
     * This method checks if a file has duplicate name with what's still processing on gcs.
     * @return  Boolean condition for exists or not.
     * @throws StorageException  Thrown when access to gcd fails.
     */
    private boolean duplicateNameExists() throws StorageException {
        return rawFileBucket.get(nameManager.getUploadName()) != null;
    }

    /**
     * This method cleans up any residual files that left on gcs.
     * @param targetBucketID  The name of target bucket to clean.
     */
    public void cleanup(String targetBucketID) {
        Bucket targetBucket = storage.get(targetBucketID);
        Page<Blob> blobs = targetBucket.list();
        for (Blob blob : blobs.iterateAll()) {
            if (blob.getName().contains(nameManager.getID())) {
                System.out.println("Cleaning: " + blob.getName());
                storage.delete(targetBucket.getName(), blob.getName());
            }
        }
    }

    /**
     * This method loads and validates the options.
     * @param language  Speech language
     * @param gender  Voice type gender
     * @param speed  Speech speed
     * @param pitch  Speech pitch
     * @throws IllegalArgumentException  Thrown when options out of boundary.
     */
    public void reflectOptions(Language language, Gender gender, double speed, double pitch) throws IllegalArgumentException {
        if ((speed != 0) && (speed < 0.25 || 4 < speed)) {
            throw new IllegalArgumentException("Speed out of range");
        }
        else if (pitch < -20 || 20 < pitch) {
            throw new IllegalArgumentException("Pitch out of range");
        }

        if (pitch < 0) {
            nameManager.packageOpt(language, gender, String.format("%.2f", speed), String.format("%.1f", pitch).replace("-", "n"));
        }
        else {
            nameManager.packageOpt(language, gender, String.format("%.2f", speed), String.format("%.1f", pitch));
        }
    }
}
