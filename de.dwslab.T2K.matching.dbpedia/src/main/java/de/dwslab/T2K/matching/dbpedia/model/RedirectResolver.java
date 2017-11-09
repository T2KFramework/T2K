/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author domi
 */
public class RedirectResolver {

//    static File directoryIn = new File("C:\\Users\\domi\\WebTables\\OliVersion\\matching.dbpedia\\Cheng\\table_gold_new");
//    static File directoryOut = new File("C:\\Users\\domi\\WebTables\\OliVersion\\matching.dbpedia\\Cheng\\table_prop_gold_new_redirects");
//    static File tablesToDo = new File("C:\\Users\\domi\\WebTables\\OliVersion\\matching.dbpedia\\Cheng\\tables_prop_all");
    static File directoryIn = new File("C:\\Users\\dritze\\save\\Ziqi\\gs_new2");
    static File directoryOut = new File("C:\\Users\\dritze\\save\\Ziqi\\gs_new3");
    static File redirectInstances = new File("C:\\Users\\dritze\\save\\Ziqi\\redirects2.csv");

    public static void main(String args[]) throws FileNotFoundException, IOException, InterruptedException {
        //directoryIn = new File(args[0]);
        //directoryOut = new File(args[1]);       
//        List<String> todo = new ArrayList<>();
//        for(File y : tablesToDo.listFiles()) {
//            todo.add(y.getName());
//        }

        BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(redirectInstances), "UTF-8"));

        for (File f : directoryIn.listFiles()) {
            //if(todo.contains(f.getName())) {
            List<String> l = resolveRedirectsLimaye(f);
            for (String s : l) {
                write.write(s + "\n");
            }

            write.flush();
        }

    }

    private static List<String> resolveRedirectsLimaye(File f) throws FileNotFoundException, IOException, InterruptedException {
        System.out.println("NAME: " + f.getName());
        Charset inputCharset = Charset.forName("ISO-8859-1");
        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(f), inputCharset));
        BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(directoryOut + "/" + f.getName())), "UTF-8"));

        List<String> redirectsFromFile = new ArrayList<>();

        String line = read.readLine();
        while (line != null) {
            String uri = line.split(",\"")[0].replace("%28", "(").replace("%29", ")").replace("%27", "'").replace("&#39;", "'").replace("&", "%26");
            List<String> redirects = lookForRedirect(uri.replaceAll("\"", ""));

//            if (redirects.contains(uri)) {
//                write.write(line + "\n");
//                line = read.readLine();
//                continue;
//            }
//
//            if (redirects.get(0).contains("UUUUUUU")) {
//                System.out.println("UUUU TABLE: " + f.getName() + "ORI: " + uri + " NEW: " + redirects.get(0));
//                // write.write(line + "\n");
//            } else {
//                //write.write(line.replace(uri, redirects.get(0)) + "\n");
//                System.out.println("TABLE: " + f.getName() + "ORI: " + uri + " NEW: " + redirects.get(0));
//
//            }

            if (redirects.size() > 1) {
                String allRedirects = "";
                for (String s : redirects) {
                    allRedirects += s + "\t";
                }
                allRedirects = allRedirects.substring(0, allRedirects.length() - 1);
                redirectsFromFile.add(allRedirects);
            }

            line = read.readLine();
        }
        write.flush();
        write.close();
        read.close();

        return redirectsFromFile;
    }

    private static List<String> lookForRedirect(String s) throws InterruptedException, IOException {
        List<String> redirect = new ArrayList<>();
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
            redirect.add(s);
            try {
                //Thread.sleep(50);
                isr = new InputStreamReader(url.openStream(), "UTF-8");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //e1.printStackTrace();
                //TODO: uris with special chars
//                if (s.contains("'")) {
//                    redirect.add(s);
//                } else {
//                    redirect.add("UUUUUUU" + s);
//                }
                //test
                System.out.println("prob: "+url);
                return redirect;
            }

            br = new BufferedReader(isr);
            String line = br.readLine();
            boolean thing = false;
            while (line != null) {
                String addString = "";
                if (line.startsWith("<body about=")) {                    
                    addString = line.split("about=")[1].replace(">", "").replaceAll("\"", "");
                }
                if (line.contains("dbo:wikiPageRedirects") && line.contains(">dbr<")) {
                    addString = line.split("href=")[1].split(">")[0].replaceAll("\"", "");
                }
//                if (line.contains("An Entity of Type : <a href=\"javascript:void()\">Thing</a>")) {
//                    thing = true;
//                }
//                if (thing && line.contains("<p></p>")) {
//                    redirect = "AAAAAAAAA" + redirect;
//                }
                if(!addString.isEmpty() && !redirect.contains(addString)) {
                    redirect.add(addString);
                }
                line = br.readLine();
            }

        }
        return redirect;
    }

    private static void resolveRedirects(File f) throws FileNotFoundException, IOException, InterruptedException {
        System.out.println("NAME: " + f.getName());
        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(directoryOut + "/" + f.getName())), "UTF-8"));
        String line = read.readLine();
        while (line != null) {
            String uri = line.split(",\"")[0].replace("%28", "(").replace("%29", ")").replace("%27", "'").replace("&", "%26");
            String redirect = doIt(uri.replaceAll("\"", ""));
            if (uri.contains(redirect)) {
                write.write(line + "\n");
            } else if (uri.contains("/page/") && uri.replace("/page/", "/resource/").contains(redirect)) {
                write.write(line.replace("/page/", "/resource/") + "\n");
            } else if (uri.contains("&") && uri.replace("&", "&amp;").contains(redirect)) {
                write.write(line + "\n");
            } else {
                if (redirect.contains("UUUUUUU")) {
                    System.out.println("UUUU TABLE: " + f.getName() + "ORI: " + uri + " NEW: " + redirect);
                    write.write(line + "\n");
                } else {
                    write.write(line.replace(uri, redirect) + "\n");
                    System.out.println("TABLE: " + f.getName() + "ORI: " + uri + " NEW: " + redirect);
                }
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
                isr = new InputStreamReader(url.openStream(), "UTF-8");
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
//                if (line.contains("An Entity of Type : <a href=\"javascript:void()\">Thing</a>")) {
//                    thing = true;
//                }
//                if (thing && line.contains("<p></p>")) {
//                    redirect = "AAAAAAAAA" + redirect;
//                }
                line = br.readLine();
            }

        }
        return redirect;

    }
}
