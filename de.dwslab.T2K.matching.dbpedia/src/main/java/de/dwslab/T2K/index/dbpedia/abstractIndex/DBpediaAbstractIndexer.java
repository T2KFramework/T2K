package de.dwslab.T2K.index.dbpedia.abstractIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.lucene.index.IndexWriter;


import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.DefaultIndex;
import java.io.BufferedReader;
import java.io.FileReader;

public class DBpediaAbstractIndexer {

    public void indexInstances(IIndex index, String uri, String abstractText) {

        IndexWriter writer = index.getIndexWriter();

        DBpediaAbstractIndexEntry e = new DBpediaAbstractIndexEntry();
        e.setUri(uri);
        e.setAbstractText(abstractText);

        try {
            writer.addDocument(e.createDocument());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        index.closeIndexWriter();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {

        DefaultIndex idx = new DefaultIndex(args[0]);
        IndexWriter writer = idx.getIndexWriter();

        System.out.println("Processing directory");

        BufferedReader read = new BufferedReader(new FileReader(new File(args[1])));
        String line = read.readLine();
        long cnt = 0;

        while (line != null) {

            try {
                if (!line.contains("<http://dbpedia.org/ontology/abstract>")) {
                    line = read.readLine();
                    continue;
                }

                String uri = line.split(" <http://dbpedia.org/ontology/abstract> ")[0].replace("<", "").replace(">", "").trim();
                String abstractText = line.split(" <http://dbpedia.org/ontology/abstract> ")[1].replace("\"@en .", "");
                abstractText = abstractText.replace("\"", "");
                abstractText = abstractText.toLowerCase();
                //indexer.indexInstances(idx, uri, abstractText);
                DBpediaAbstractIndexEntry e = new DBpediaAbstractIndexEntry();
                e.setUri(uri);
                e.setAbstractText(abstractText);
                writer.addDocument(e.createDocument());
            } catch (Exception e) {
                line = read.readLine();
            }

            line = read.readLine();
            cnt++;
            if (cnt % 100000 == 0) {
                System.out.println("Indexed " + cnt + " items.");
            }
        }
        idx.closeIndexWriter();
        System.out.println("done.");
    }
}
