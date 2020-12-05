package com.queue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PDFQueueTest {

    FileQueue pdfQueue;
    File fileExample;

    @Before
    public void setUp() throws Exception { //Set-up ran before each test.
        pdfQueue = new PDFQueue();
        fileExample = new File("src/test/resources/isPDF.pdf");
    }

    @Test
    public void add() {
        pdfQueue.Add(fileExample); //Queue can handle duplicates
        pdfQueue.Add(fileExample);
        Assert.assertEquals(2, pdfQueue.GetQueue().size());
    }

    @Test
    public void remove() {
        pdfQueue.Add(fileExample);
        IFileData exampleFileData = new FileData(fileExample, 1);
        pdfQueue.Remove(exampleFileData);
        Assert.assertEquals(1, pdfQueue.GetQueue().size());
        // Removing a random exampleFileData object, that is not the same as was originally added,
        //should not trigger the Remove() to work.
    }

    @Test
    public void removeWithFileReturn() {
        pdfQueue.Add(fileExample);
        Assert.assertEquals(fileExample, pdfQueue.Remove().getFile());
    }

    @Test
    public void getQueue() {
        List<IFileData> queueListExample = new ArrayList<>();
        assertTrue(queueListExample != pdfQueue.GetQueue());
    }

    @Test
    public void setQueue() {
        List<IFileData> queueListExample = new ArrayList<>();
        pdfQueue.SetQueue(queueListExample);
        Assert.assertEquals(queueListExample, pdfQueue.GetQueue());
    }
}