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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author domi
 */
public class RedirectResolver {

    static File directoryIn = new File("C:\\Users\\domi\\WebTables\\OliVersion\\matching.dbpedia\\cheng_gs_part");
    static File directoryOut = new File("C:\\Users\\domi\\WebTables\\OliVersion\\matching.dbpedia\\redirects_cheng");

    public static void main(String args[]) throws FileNotFoundException, IOException, InterruptedException {
        directoryIn = new File(args[0]);
        directoryOut = new File(args[1]);
        for (File f : directoryIn.listFiles()) {
            resolveRedirects(f);
        }
    }

    private static void resolveRedirects(File f) throws FileNotFoundException, IOException, InterruptedException {
        System.out.println("NAME: " + f.getName());
        BufferedReader read = new BufferedReader(new FileReader(f));
        BufferedWriter write = new BufferedWriter(new FileWriter(directoryOut + "/" + f.getName()));
        String line = read.readLine();
        while (line != null) {
            String uri = line.split(",\"")[0].replace("%28", "(").replace("%29", ")").replace("%27", "'");
            String redirect = doIt(uri.replaceAll("\"", ""));
            if (uri.contains(redirect)) {
                write.write(line + "\n");
            } else if (uri.contains("/page/") && uri.replace("/page/", "/resource/").contains(redirect)) {
                write.write(line.replace("/page/", "/resource/") + "\n");
            } else if (uri.contains("&") && uri.replace("&", "&amp;").contains(redirect)) {
                write.write(line + "\n");
            } else {
                write.write(line.replace(uri, redirect) + "\n");
                System.out.println("TABLE: " + f.getName() + "ORI: " + uri + " NEW: " + redirect);
            }
            line = read.readLine();
        }
        write.flush();
        write.close();
        read.close();
    }

    private static String doIt(String s) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        String redirect = "";
        BufferedReader br = null;
        InputStreamReader isr = null;
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (url != null) {
            try {
                Thread.sleep(50);
                isr = new InputStreamReader(url.openStream());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //e1.printStackTrace();
                //TODO: uris with special chars
                if (s.contains("'")) {
                    return s;
                } else {
                    return "UUUUUUU" + s;
                }
            }

            br = new BufferedReader(isr);
            String line = br.readLine();
            boolean thing = false;
            while (line != null) {
                if (line.contains("<body onload=\"init();\" about=")) {
                    redirect = line.split("about=")[1].replace(">", "");
                }
                if (line.contains("An Entity of Type : <a href=\"javascript:void()\">Thing</a>")) {
                    thing = true;
                }
                if (thing && line.contains("<p></p>")) {
                    redirect = "AAAAAAAAA" + redirect;
                }
                line = br.readLine();
            }

        }
        return redirect;

    }
}
