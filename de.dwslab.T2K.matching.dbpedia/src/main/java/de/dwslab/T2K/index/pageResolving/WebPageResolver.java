/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.index.pageResolving;

import de.dwslab.T2K.index.io.DefaultIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.index.IndexWriter;

/**
 *
 * @author domi
 */
public class WebPageResolver {

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        DefaultIndex idx = new DefaultIndex(args[0]);
        InputStream in = new GZIPInputStream(new FileInputStream(new File(args[1])));
        //boolean smallFile = Boolean.parseBoolean(args[2]);
        BufferedReader read = new BufferedReader(new InputStreamReader(in));
        Map<String, String> idToURI = new HashMap<>();
        String line = read.readLine();
        long cnt = 0;
        while (line != null) {
            String content[];
//            if (smallFile) {
//                content = line.split("\t");
//                System.out.println(content[0].replace(".csv", "") + " --- "+ content[1]);
//                idToURI.put(content[0].replace(".csv", ""), content[1]);
//            } else {
                content = line.split("\"\\|");
                if (content.length > 5) {
                    try {
                        idToURI.put(content[5].replace("\"", "").replace("/", "__").replace(".arc.gz", "arc").replace(".csv", ""), content[0].replace("\"", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(line);
                    }
                }
//            }
            cnt++;
            if (cnt % 100000 == 0) {
                System.out.println("Indexed " + cnt + " items." + " id: " + content[5].replace("\"", "") + " uri " + content[0].replace("\"", ""));
            }
            line = read.readLine();
        }
        read.close();

        IndexWriter writer = idx.getIndexWriter();

        for (String id : idToURI.keySet()) {
            WebPageEntry e = new WebPageEntry();
            e.setTableID(id);
            e.setUri(idToURI.get(id));
            try {
                writer.addDocument(e.createDocument());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        System.out.println("Indexed " + cnt + " items.");

        idx.closeIndexWriter();

        System.out.println("done.");
    }

}
