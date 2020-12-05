package com.queue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class FileDataFactoryTest {

    File fileExample;
    DataFactory fileDataFactory;

    @Before
    public void setUp() throws Exception { //Set-up ran before each test.
        fileExample = new File("src/test/resources/tempFile.txt");
        fileDataFactory = new FileDataFactory();
    }

    @After
    public void tearDown() throws Exception {
        if(fileExample != null) {
            fileExample.delete();
        }
    }

    @Test
    public void Create() {
       IFileData fileDataObj  = fileDataFactory.Create(fileExample, 1);
       Assert.assertTrue(fileDataObj.getFile() == fileExample && fileDataObj.getID() == 1);
    }
}