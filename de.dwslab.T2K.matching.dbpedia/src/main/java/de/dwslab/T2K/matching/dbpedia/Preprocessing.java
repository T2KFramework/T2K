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
package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.index.surfaceForms.SurfaceForm;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class Preprocessing {

//    private static SurfaceFormIndex i;
    private static Map<String, List<SurfaceForm>> surfaceForms;
    private static String surfaceFormPath;
    private static Map<String, String> redirects;
    private static String redirectPath;

    /**
     * @param aSurfaceFormPath the surfaceFormPath to set
     */
    public static void setSurfaceFormPath(String aSurfaceFormPath) {
        surfaceFormPath = aSurfaceFormPath;
    }

    public static void setRedirectsPath(String aRedirectPath) {
        redirectPath = aRedirectPath;
    }
    
    

    public static void loadRedirects(String path) throws FileNotFoundException, IOException {
        if (redirects == null) {
            redirectPath = path;
            Timer tLoad = Timer.getNamed("Load Redirects", null);
            redirects = new HashMap<>();
            BufferedReader read = new BufferedReader(new FileReader(new File(redirectPath)));
            String line = read.readLine();
            while (line != null) {
                if (!line.contains("\t")) {
                    line = read.readLine();
                    continue;
                }
                String[] currentRedirect = line.split("\t");
                if (currentRedirect.length < 2) {
                    line = read.readLine();
                    continue;
                }
                String redirectedPage = line.split("\t")[0];
                String redirect = line.split("\t")[1];

                if (!redirectedPage.equals(redirect) && !redirect.isEmpty()) {
                    if (surfaceForms != null && surfaceForms.containsKey(redirectedPage.toLowerCase())) {
                        line = read.readLine();
                        continue;
                    }
                    redirects.put(redirectedPage.toLowerCase(), redirect);
                }
                line = read.readLine();
            }
            tLoad.stop();
        }
    }

    public static void addRedirects(Table t, boolean addSFForKeyColumns, MatchingData data,boolean addOri) throws FileNotFoundException, IOException {
//        if (redirects == null) {
//            Timer tLoad = Timer.getNamed("Load Redirects", null);
//            redirects = new HashMap<>();
//            BufferedReader read = new BufferedReader(new FileReader(new File(redirectPath)));
//            String line = read.readLine();
//            while (line != null) {
//                if (!line.contains("\t")) {
//                    line = read.readLine();
//                    continue;
//                }
//                String[] currentRedirect = line.split("\t");
//                if(currentRedirect.length<2) {
//                    line = read.readLine();
//                    continue;
//                }
//                String redirectedPage = line.split("\t")[0];
//                String redirect = line.split("\t")[1];
//                
//                if (!redirectedPage.equals(redirect) && !redirect.isEmpty()) {
//                    if(surfaceForms!=null && surfaceForms.containsKey(redirectedPage.toLowerCase())) {
//                        line = read.readLine();
//                        continue;
//                    }
//                    redirects.put(redirectedPage.toLowerCase(), redirect);
//                }
//                line = read.readLine();
//                //original file
////                if (!line.contains("http://dbpedia.org/ontology/wikiPageRedirects")) {
////                    line = read.readLine();
////                    continue;
////                }
////                line = line.replace("> <http://dbpedia.org/ontology/wikiPageRedirects> <", "XX");
////                String redirectedPage = line.split("XX")[0];
////                String redirect = line.split("XX")[1];
////                redirectedPage = redirectedPage.replace("<", "");
////                redirect = redirect.replace("> .", "");
////                i++;
////                if (!getLabelByURI(redirectedPage).equals(getLabelByURI(redirect))) {
////                    redirects.put(getLabelByURI(redirectedPage), getLabelByURI(redirect));
////                }
////                line = read.readLine();
//            }
//            tLoad.stop();
//        }
        for (TableColumn c : t.getColumns()) {
            if (!addSFForKeyColumns && c.isKey()) {
                continue;
            }
            if (c.getDataType() == TableColumn.ColumnDataType.string) {
                for (Integer key : c.getValues().keySet()) {
                    //CASE LIST:
                    if (c.getValues().get(key) instanceof List) {
                        List<String> newValues = new ArrayList<>();
                        List<String> currentValues = (List<String>) c.getValues().get(key);
                        for (String s : currentValues) {
                            String value = redirects.get(s.toLowerCase());

                            //if (redirects.containsKey(s.toLowerCase()) && !redirects.get(s.toLowerCase()).equals(s.toLowerCase())) {
                            if (!s.toLowerCase().equals(s)) {
                                newValues.add(redirects.get(s.toLowerCase()));
                            }
                            if(addOri) {
                                newValues.add(s);
                            }
                        }
                        c.getValues().put(key, newValues);
                    } else {
                        if (c.getValues().get(key) != null) {
                            String s = (String) c.getValues().get(key);
                            if (redirects.containsKey(s.toLowerCase())) {
                                List<String> allForms = new ArrayList<>();
                                //System.out.println("REDIRECT!!!: " + s + " --- " + redirects.get(s));
                                if(addOri) {
                                    allForms.add(s);
                                }
                                allForms.add(redirects.get(s.toLowerCase()));
                                c.getValues().put(key, allForms);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadSurfaceForms(String path) throws FileNotFoundException, IOException {
        if (surfaceForms == null) {
            surfaceFormPath = path;
            surfaceForms = new HashMap<>();
            File f = new File(surfaceFormPath);
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            String key, value;
            double score;
            int j = 0;
            //read in the surface forms from the file
            while (line != null) {
                String[] strings = line.split("\t");
                if (strings.length > 2) {
                    key = strings[0].toLowerCase();
                    value = strings[1].toLowerCase();
                    try {
                        score = Double.parseDouble(strings[2]);
                    } catch (Exception e) {
                        score = 0.0;
                    }
                    SurfaceForm form = new SurfaceForm(key, score);
                    //if the URI fragment is already contained as key, just add the additional
                    //surface form to the list
                    if (surfaceForms.containsKey(value)) {
                        surfaceForms.get(value).add(form);
                    } else {
                        List<SurfaceForm> l = new ArrayList();
                        l.add(form);
                        surfaceForms.put(value, l);
                    }
                    if (j % 1000000 == 0) {
                        System.out.println("i: " + j);
                    }
                    j++;
                }
                line = read.readLine();
            }
            read.close();
        }
    }

    public static void addSurfaceForms(Table t, boolean addSFForKeyColumns, MatchingData data, boolean addOri) throws FileNotFoundException, IOException {
//        if (surfaceForms == null) {
////            i = new SurfaceFormIndex(new InMemoryIndex(), SurfaceFormIndexEntry.LABEL_FIELD);
//            surfaceForms = new HashMap<>();
//            File f = new File(surfaceFormPath);
//            BufferedReader read = new BufferedReader(new FileReader(f));
//            String line = read.readLine();
//            String key, value;
//            double score;
//            int j = 0;
//            //read in the surface forms from the file
//            while (line != null) {
//                String[] strings = line.split("\t");
//                key = strings[0];
//                value = strings[1];
//                score = Double.parseDouble(strings[2]);
//                SurfaceForm form = new SurfaceForm(key, score);
//                //if the URI is already contained as key, just add the additional
//                //surface form to the list
//                if (surfaceForms.containsKey(value)) {
//                    surfaceForms.get(value).add(form);
//                } else {
//                    List<SurfaceForm> l = new ArrayList();
//                    l.add(form);
//                    surfaceForms.put(value, l);
//                }
//                if (j % 1000000 == 0) {
//                    System.out.println("i: " + j);
//                }
//                j++;
//                line = read.readLine();
//            }
//            read.close();
////            System.out.println("before indexed, size map: " + surfaceForms.size());
////            i.indexSurfaceForms(surfaceForms);
////            System.out.println("indexed");
//        }
        for (TableColumn c : t.getColumns()) {
            if (!addSFForKeyColumns && c.isKey()) {
                continue;
            }
            if (c.getDataType() == TableColumn.ColumnDataType.string || c.getDataType() == TableColumn.ColumnDataType.link) {
                for (Integer key : c.getValues().keySet()) {
                    //need to search for all surface forms for all list entries
                    if (c.getValues().get(key) instanceof List) {
                        List<String> newValues = new ArrayList<>();
                        List<String> currentValues = (List<String>) c.getValues().get(key);
                        for (String s : currentValues) {
//                            s = s.replace("http://dbpedia.org/page/", "");
//                            s = s.replace("http://dbpedia.org/resource/", "");
//                            s = s.toLowerCase();
//                            s = s.replace("_", "");
//                            List<String> allForms = getListOfAllSurfaceValues(s);
//                            if(!allForms.isEmpty()) {
//                                newValues.addAll(allForms);
//                            }
//                            if (surfaceForms.get(s) != null) {
//                                for (int k = 0; k < surfaceForms.get(s).size(); k++) {
//                                    if (surfaceForms.get(s).get(k).getLabel().toLowerCase().contains(s)) {
//                                        continue;
//                                    }
//                                    newValues.add(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data));
//                                }
//                            }
                            if (surfaceForms.get(s) != null && surfaceForms.get(s).size() > 1) {
                                System.out.println(surfaceForms.get(s));
                                Collections.sort(surfaceForms.get(s));
                                if (surfaceForms.get(s).get(1).getScore() / surfaceForms.get(s).get(0).getScore() > 0.8) {
                                    //only add the top 5 surface forms
                                    if (surfaceForms.get(s).size() > 3) {
                                        int count = 0;
                                        for (int k = 0; k < surfaceForms.get(s).size(); k++) {
                                            if (count == 3) {
                                                break;
                                            }
                                            if (surfaceForms.get(s).get(k).getLabel().toLowerCase().contains(s)) {
                                                continue;
                                            }
                                            newValues.add(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data));
                                            count++;
                                        }
                                    } else {
                                        for (SurfaceForm s2 : surfaceForms.get(s)) {
                                            if (s2.getLabel().toLowerCase().contains(s)) {
                                                continue;
                                            }
                                            if (newValues.contains(getLabelByPart(s2.getLabel(), data).toLowerCase())) {
                                                continue;
                                            }
                                            newValues.add(getLabelByPart(s2.getLabel(), data).toLowerCase());
                                        }
                                    }
                                }
                            }
                            if(addOri) {
                                newValues.add(s);
                            }
                        }
                        c.getValues().put(key, newValues);
                    } else {
                        String s = (String) c.getValues().get(key);
//                       List<String> allForms = getListOfAllSurfaceValues((String)c.getValues().get(key));
//                        if(!allForms.isEmpty()) {
//                            c.getValues().put(key, allForms);
////                        }
//                        s = s.replace("http://dbpedia.org/page/", "");
//                        s = s.replace("http://dbpedia.org/resource/", "");
//                        s = s.toLowerCase();
//                        s = s.replace("_", "");
                        if (s != null && surfaceForms.get(s) != null) {

                            List<String> allForms = new ArrayList<>();
                            if(addOri) {
                                allForms.add(s);
                            }

//                            if (surfaceForms.get(s) != null) {
//                                for (int k = 0; k < surfaceForms.get(s).size(); k++) {
//                                    if (surfaceForms.get(s).get(k).getLabel().toLowerCase().contains(s)) {
//                                        continue;
//                                    }
//                                    if (allForms.contains(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data).toLowerCase())) {
//                                        continue;
//                                    }
//                                    //System.out.println(surfaceForms.get(s).get(k).getLabel() + " --- " + surfaceForms.get(s).get(k).getScore());
//
//                                    allForms.add(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data).toLowerCase());
//
//                                }
//                            }
                            //do not take all the surface forms!
                            Collections.sort(surfaceForms.get(s));
                            if (surfaceForms.get(s).size() > 1 && (surfaceForms.get(s).get(1).getScore() / surfaceForms.get(s).get(0).getScore() > 0.8)) {
//                                System.out.println("SURFACE!!!: " + s );
//                                for(SurfaceForm sf : surfaceForms.get(s)) {
//                                    System.out.println(sf.getLabel() + " --- " + sf.getScore());
//                                }
                                if (surfaceForms.get(s).size() > 3) {
                                    int count = 0;
                                    for (int k = 0; k < surfaceForms.get(s).size(); k++) {
                                        if (count == 3) {
                                            break;
                                        }
                                        if (surfaceForms.get(s).get(k).getLabel().toLowerCase().contains(s)) {
                                            continue;
                                        }
                                        if (allForms.contains(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data).toLowerCase())) {
                                            continue;
                                        }
                                        //System.out.println(surfaceForms.get(s).get(k).getLabel() + " --- " + surfaceForms.get(s).get(k).getScore());

                                        allForms.add(getLabelByPart(surfaceForms.get(s).get(k).getLabel(), data).toLowerCase());
                                        count++;
                                    }
                                } else {
                                    for (SurfaceForm s2 : surfaceForms.get(s)) {
                                        if (s2.getLabel().toLowerCase().contains(s)) {
                                            continue;
                                        }
                                        if (allForms.contains(getLabelByPart(s2.getLabel(), data).toLowerCase())) {
                                            continue;
                                        }
                                        allForms.add(getLabelByPart(s2.getLabel(), data).toLowerCase());
                                    }
                                }
                            }
                            //also overwrite if we have no value based on the SF
                           // if (allForms.size() > 1) {
                                c.getValues().put(key, allForms);
                         //   }
                        }
                    }
                }
            }
        }
    }

    protected static String getLabelByPart(String uriPart, MatchingData data) {
        List<TableRow> row = data.getCandidateMap().get("http://dbpedia.org/resource/" + uriPart);

        if (row != null && !row.isEmpty()) {
            return (String) new TableRowMatchingAdapter().getLabel(row.get(0));
        } else {
            return uriPart.replace("_", " ");
        }
    }

    protected static String getLabelByURI(String uri) {
        uri = uri.replace("http://dbpedia.org/resource/", "");
        uri = uri.replace("_", " ");
        return uri.toLowerCase();

    }
//    private List<String> getListOfAllSurfaceValues(String s) {
//        List<SurfaceFormIndexEntry> entries = i.search(s);
//        List<String> allForms = new ArrayList<>();
//        for (SurfaceFormIndexEntry e : entries) {
//            if (e.getLabel().equals(s)) {
//                //add the "old" value
//                allForms.add(s);
//                //add all surface forms
//                allForms.addAll(surfaceForms.get(e.getLabel()));
//            }
//        }
//        return allForms;
//    }

    public static void handleIncorrectDates(Table t) {
        for (TableColumn c : t.getColumns()) {
            if (c.getHeader().toString().toLowerCase().contains("releasedate") && t.toString().toLowerCase().contains("video")
                    && c.getDataType() == TableColumn.ColumnDataType.date) {
                for (int row : c.getValues().keySet()) {
                    List<Date> dates = null;
                    if (c.getValues().get(row) instanceof List) {
                        dates = new ArrayList<>();
                        for (Date d : (List<Date>) c.getValues().get(row)) {
                            dates.add(d);
                            dates.add(checkDate(d));
                        }
                    } else {
                        dates = new ArrayList<>();
                        dates.add((Date) c.getValues().get(row));
                        dates.add(checkDate((Date) c.getValues().get(row)));
                    }
                    c.getValues().put(row, dates);
                }
            }
        }
    }

    private static Date checkDate(Date d) {

        // make sure the year is in a range that can be a day (otherwise the date class will turn this into a completely uncorrelated date)
        //TODO this range check is incomplete!
        int newDate = d.getYear() > 100 ? d.getYear() - 100 : d.getYear();

        // assume that a date is more likely to be in the past than in the future, so if a flipped date part results in a year in the future, move it 100 years to the past
        int newYear = d.getDate() <= new Date().getYear() ? d.getDate() + 100 : d.getDate();

        Date additionalDate = new Date(newYear, d.getMonth(), newDate);

        return additionalDate;
    }
}
