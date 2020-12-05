package com.pdfUtil;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class This class has utility methods used to validate, parse PDFs.
 *
 * Author: Hamza Kamal
 * Last updated: 2020-11-24 by Aki Zhou
 */
public class PDFBoxConverter implements PDFConverter {

    /***** Attributes *****/

    private PDDocument doc;
    private int pageStartIndex;
    private int pageEndIndex;
    private String filename;
    private boolean verified;

    public PDFBoxConverter() {
        pageStartIndex = 1;
    }

    /**
     * @return  Index of start page of loaded PDF.
     */
    public int getStartPage() {
        return this.pageStartIndex;
    }

    /**
     * @return  Index of last page of loaded PDF.
     */
    public int getEndPage() {
        return this.pageEndIndex;
    }

    /**
     * @param start First page to start parsing
     **/
    public void setStartPage(int start) {
        this.pageStartIndex = start;
    }

    /**
     * @param end Last page to parse up until
     **/
    public void setEndPage(int end) {
        this.pageEndIndex = end;
    }

    /**
     * This method checks if a file is legitimately a PDF file.
     * @param f  File to be verified
     * @return  Boolean condition indicating the verification result.
     */
    public boolean isValidPDF(File f) {
        PDDocument validatorDoc = null;
        try {
            validatorDoc = PDDocument.load(f, MemoryUsageSetting.setupTempFileOnly());
            return true;
        }
        catch (IOException exc) {
            return false;
        }
        finally {
            if (validatorDoc != null) {
                try {
                    validatorDoc.close();
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    /**
     * This method load selected PDF's information such as the page numbers.
     * @precondition File has been verified to be a PDF by isValidPDF().
     * @param f  Target file to load information from.
     * @throws IllegalStateException  Thrown when the precondition is not met.
     * @throws IOException  Thrown on failure to read the file.
     */
    public void loadParameters(File f) throws IOException {
        try {
            this.doc = PDDocument.load(f, MemoryUsageSetting.setupTempFileOnly()); //create new PDDocument to parse

            // By default all the pages are selected for parsing
            if (f.getName().endsWith(".pdf")) {
                this.filename = f.getName().replace(".pdf", ".txt");
            }
            else {
                this.filename = f.getName();
            }

            this.pageStartIndex = 1;
            this.pageEndIndex = this.doc.getNumberOfPages();
        }
        catch (IOException exp) {
            throw new IOException("Failed to read PDF doc");
        }
        finally {
            // Close the PDDocument  after use
            if (this.doc != null) {
                try {
                    System.out.println("Closing the PDDocument");
                    this.doc.close();
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    /**
     * This method extracts text from a PDF file.
     * Referenced https://www.w3schools.com/java/java_files_create.asp for file creation/writing.
     * @return Text file that contains extracted text where each line is max 5000 characters long
     * @throws IOException  Thrown on failure when attempting to read/write files
     */
    public File convert2Text(File f) throws IOException {
        // Create a file to be returned
        File convertedTextFile;
        String filePath = System.getProperty("user.dir")+ File.separator + "resources" + File.separator + filename;
        try {
            System.out.println("Parsing PDF to text");

            convertedTextFile = new File(filePath);
            if (convertedTextFile.createNewFile()) {
                System.out.println("Text file created: " + convertedTextFile.getName());
            } else {
                System.out.println("Text file with the same name already exists. Overwriting the old file.");
            }
        }
        catch (IOException e) {
            throw new IOException("Failed to create a file") ;
        }

        // Extract the text from PDF using PDFTextStripper
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String convertedText;
        try {
            doc = PDDocument.load(f, MemoryUsageSetting.setupTempFileOnly());
            pdfStripper.setStartPage(pageStartIndex);
            pdfStripper.setEndPage(pageEndIndex);
            convertedText = pdfStripper.getText(this.doc);
        }
        catch (IOException exp) {
            throw new IOException("Failed to read PDF doc");
        }
        finally {
            // Close the PDDocument  after use
            if (this.doc != null) {
                try {
                    System.out.println("Closing the PDDocument");
                    this.doc.close();
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }

        // Post processing
        try (
                // Windows 1252 encoding will cause valueError for characters outside of ASCII, enforce UTF-8
                FileOutputStream fos = new FileOutputStream(filePath);
                OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                BufferedWriter writer = new BufferedWriter(osw)
        ) {
            // Make each line of the text file max 4000 characters.
            // 5000 characters per api request is the maximum of quota but generates a lot of timing overlap cancellation
            // 4000 was tested to be the a number that minimizes the cancellation
            while (convertedText.length() != 0) {
                // Removes the first 4000 characters from the convertedText
                String fiveThousandChars = convertedText.substring(0,Math.min(convertedText.length(), 4000));
                // Finds the last \n and then substring fiveThousandChars till it so we have last \n in the first 4000 characters
                int lastEndLineCharacter = fiveThousandChars.lastIndexOf("\n");
                // find the substring till the last newline char while still less than 5000 and then remove all newlines and only add one at end
                String tillLastEndLine = fiveThousandChars.substring(0,lastEndLineCharacter).replace(System.lineSeparator(), "") + "\n";
                writer.append(tillLastEndLine);
                // Removing the just deleted characters from convertedText
                convertedText = convertedText.substring(tillLastEndLine.length());
            }
            System.out.println("Successfully wrote to the file");
        }
        catch (IOException e) {
            throw new IOException("Failed to write converted text to file");
        }

        return convertedTextFile;
    }

    /**
     * This method deletes the text file generated by parsing the PDF for when it is no longer needed.
     */
    public void cleanUpFile() {
        String filePath = System.getProperty("user.dir")+ File.separator + "resources" + File.separator + filename;
        File file = new File(filePath);
        if (file.delete()) {
            System.out.println("Cleaned up " + filename);
        } else {
            System.out.println("Failed to clean up " + filename);
        }
    }
}
