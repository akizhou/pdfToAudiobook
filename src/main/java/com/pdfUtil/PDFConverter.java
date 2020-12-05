package com.pdfUtil;

import java.io.File;
import java.io.IOException;

public interface PDFConverter {
    boolean isValidPDF(File f);
    void loadParameters(File file) throws IllegalStateException, IOException;

    File convert2Text(File f) throws IOException;
    void cleanUpFile();

    void setStartPage(int start);
    void setEndPage(int end);
    int getStartPage();
    int getEndPage();
}
