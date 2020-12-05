package com.queue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class FileDataTest {

    File fileExample;
    IFileData fileData;

    @Before
    public void Before() {
        fileExample = new File("src/test/resources/tempFile.txt");
        fileData = new FileData(fileExample, 1);
    }

    @After
    public void tearDown() throws Exception {

        if(fileExample != null) {
            fileExample.delete();
        }
    }

    @Test
    public void getFile() {
        Assert.assertEquals(fileExample, fileData.getFile());
    }

    @Test
    public void getID() {
        Assert.assertEquals(1, fileData.getID());
    }
}
