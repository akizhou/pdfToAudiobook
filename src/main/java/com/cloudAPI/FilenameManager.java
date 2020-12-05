package com.cloudAPI;

import com.enums.Gender;
import com.enums.Language;
import com.exceptions.MACAddressError;
import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * This class has utility methods to manage filenames primary for the purpose of avoiding race conditions between files
 * with duplicate names belonging to the same user and duplicate names between different users.
 *
 * Author: Aki Zhou
 * Last updated: 2020-11-08 by Aki Zhou
 */
public class FilenameManager {

    /***** Attributes *****/

    private String pathToFile;
    private String fileBaseName;
    private String rawFileExtension;
    private String outFileExtension;
    private String ID;
    private String option;
    private boolean unique;

    FilenameManager() throws MACAddressError {
        pathToFile = System.getProperty("user.dir") + File.separator + "audioFiles" + File.separator;
        fileBaseName = "";
        rawFileExtension = ".pdf";
        outFileExtension = ".mp3";
        ID = "";
        option = "--(en_US,NEUTRAL,1.0,0.00)";
        unique = false;
        generateID();
    }

    public String getID() {
        return ID;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getFileBaseName() {
        return fileBaseName;
    }

    public String getRawFileExtension() {
        return rawFileExtension;
    }

    public String getOutFileExtension() {
        return outFileExtension;
    }

    public String getOption() {
        return option;
    }

    /**
     * This method sets the path of parent directory for download if user have specified any
     * and enforces it ends with / or \.
     * @param newPath  Path of parent directory which downloaded file will reside.
     */
    public void setPathToFile(String newPath) {
        if (newPath.equals("")) {
            return;
        }
        // Enforce that newPath ends with / or \
        String lastChar = newPath.substring(newPath.length() - 1);
        if (!lastChar.equals(File.separator)) {
            newPath += File.separator;
        }
        pathToFile = newPath;
    }

    /**
     * This method returns the path of parent directory which downloaded file will reside.
     * @return  The path.
     */
    public String getPathToFile() {
        return pathToFile;
    }

    /**
     * This method returns path for the file which data will be downloaded to.
     * @param timestamp  boolean indicating if filename needs to be padded with timestamp to avoid overwriting.
     * @return  Path of download destination file
     * @throws IllegalStateException  Thrown on attempts to get download path before handling race condition.
     */
    public String getDownloadPath(boolean timestamp) throws IllegalStateException {
        if (!unique) {
            throw new IllegalStateException("Should not attempt to download not unique file");
        }
        if (timestamp) {
            return pathToFile + padTimestamp(fileBaseName, false) + outFileExtension;
        }
        return pathToFile + fileBaseName + outFileExtension;
    }

    /**
     * This method returns an unique filename for file download.
     * @return  An unique filename used to download. The name must match a file name on the origin of download.
     * @throws IllegalStateException  Thrown on attempts to get download name before handling race condition.
     */
    public String getDownloadName() throws IllegalStateException {
        if (!unique) {
            throw new IllegalStateException("Should not attempt to download non-unique file");
        }
        return ID + "." + fileBaseName + outFileExtension;
    }

    /**
     * This method returns an unique filename for file upload.
     * @return An unique filename used to upload.
     * @throws IllegalStateException  Thrown on attempts to get download name before handling race condition.
     */
    public String getUploadName() throws IllegalStateException {
        if (!unique) {
            throw new IllegalStateException("Should not attempt to download non-unique file");
        }
        return ID + "." + fileBaseName + rawFileExtension + option;
    }

    /**
     * This method prepares other attributes so the names returned by get methods are unique.
     * @param fileName  The filename being parsed and made unique.
     */
    public void init(String fileName) {
        fileBaseName = FilenameUtils.getBaseName(fileName);
        rawFileExtension = "." + FilenameUtils.getExtension(fileName);
        unique = true;
    }

    /**
     * This is a utility method caled by GoogleCloudAPI to pad timestamp for upload filename.
     * @throws IllegalStateException  Thrown on attempts to get download name before handling race condition.
     */
    public void timestampBaseName() throws IllegalStateException {
        if (!unique) {
            throw new IllegalStateException("Should not attempt to download non-unique file");
        }
        fileBaseName = padTimestamp(fileBaseName, true);
    }

    /**
     * This method adds a timestamp to the given filename.
     * @param name  The name for which a timestamp is padded on.
     * @param upload  Boolean indicating if the padding is for upload or download.
     * @return  A new filename with timestamp added before the base name.
     */
    private static String padTimestamp(String name, boolean upload) {
        final SimpleDateFormat TSFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Timestamp timestamp = new Timestamp((System.currentTimeMillis()));

        System.out.println("File with the same name already exists, padding timestamp to the filename.");
        if (upload) {
            return  "(" + name + "-" + TSFormat.format(timestamp) + ")";
        }
        else {
            return TSFormat.format(timestamp) + "-" + name;
        }
    }

    /**
     * This method reads the MAC address of the localhost and creates an unique ID from it.
     * @throws MACAddressError  Thrown on failure of generating an unique ID. This is critical for avoiding race conditions.
     */
    private void generateID() throws MACAddressError {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder MAC = new StringBuilder();
            for (byte b : mac) {
                MAC.append(String.format("%x", b));
            }
            ID =  MAC.toString();
        }
        catch (SocketException | UnknownHostException exc) {
            throw new MACAddressError();
        }
    }

    /**
     * This method bundles the options to be appended to upload name.
     * @param language  Language option
     * @param gender  Gender of voice option
     * @param pitch  Pitch of speech option
     * @param speed  Speed of speech option
     */
    public void packageOpt(Language language, Gender gender, String speed, String pitch) {
        option = "-(" + language.name() + "," + gender + "," + speed + "," + pitch + ")";
        if (option.length() < 25) {
            option = Strings.repeat("-", 25-option.length()) + option;
        }
    }
}
