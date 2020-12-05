package com.queue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * This class holds methods to manipulate the queue, the primary UI structure for pdf file control.
 * for PDF2Audiobook project.
 *
 * Author: Kirill Melnikov
 * Last updated: 2020-12-1 by Kirill Melnikov
 */

public class PDFQueue implements FileQueue {

    private List<IFileData> pdfQueue; //make the list abstract; so it doesn't necessarily have to be arrayList
    private DataFactory dataFactory;
    private int counter;

    public PDFQueue()
    {
        pdfQueue = new ArrayList<>(); //make new instance on class init
        dataFactory = new FileDataFactory();
        counter = 1; //reset counter - we start at 1, b/c we aren't savages that start at 0 when iding files.
    }

    public void Add(File file)
    {
        pdfQueue.add(dataFactory.Create(file, counter));
        counter++; //increments counter on next iteration, each subsequent files is ordered this way.
    }

    public void Remove(IFileData fileData) //if model + queue exists, we can just delete the same object from the queue.
    {
        pdfQueue.remove(fileData);
    }

    private IFileData SearchAndRemove(int id) // I think you can remove before returning, but its a good idea to test this.
    {
        for(IFileData file : pdfQueue) {
            if(file.getID() == id) {
                pdfQueue.remove(file);
                return file;
            }
        }

        return null;
    }

    public IFileData Remove() //removes from top
    {
        return pdfQueue.remove(0); //removes first element from queue
        //We must have some way to notifying the collection JList to update also
        //Could use observer pattern!
    }

    public List<IFileData> GetQueue()
    {
        return pdfQueue;
    }

    public void SetQueue(List<IFileData> list)
    {
        pdfQueue = list;
    }
}
