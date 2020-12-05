package com.pdfUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import static org.junit.Assert.*;

public class PDFBoxConverterTest {
    File pdfFile;
    File notPdfFile;
    PDFBoxConverter pdfConverter;
    String pdfContent;

    @Before
    public void setUp() {
        pdfFile = new File("src/test/resources/isPDF.pdf");
        notPdfFile = new File("src/test/resources/notPDF.docx");
        pdfConverter = new PDFBoxConverter();
    }


    @Test
    public void isValidPDF() {
        assertTrue(pdfConverter.isValidPDF(pdfFile));
        assertFalse(pdfConverter.isValidPDF(notPdfFile));
    }

    @Test
    public void loadParameters() throws IOException {
        pdfConverter.loadParameters(pdfFile);
        assertEquals(1,pdfConverter.getStartPage());
        assertEquals(1,pdfConverter.getEndPage());
    }

    @Test
    public void convert2Text() throws IOException {
        pdfConverter.loadParameters(pdfFile);
        File convertedFile = null;
        convertedFile = pdfConverter.convert2Text(pdfFile);
        assertNotNull(convertedFile);
    }
}