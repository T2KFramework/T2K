/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dwslab.T2K.index.dbpedia.abstractIndex;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.DefaultIndex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.index.IndexWriter;

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
