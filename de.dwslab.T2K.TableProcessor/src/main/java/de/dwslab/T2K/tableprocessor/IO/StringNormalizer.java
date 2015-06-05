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
package de.dwslab.T2K.tableprocessor.IO;

import de.dwslab.T2K.util.Variables;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author petar
 *
 */
public class StringNormalizer {
    /**
     * Use normaliseValue and normaliseHeader instead
     *
     * @param value
     * @param removeBrackets
     * @return
     */
    protected static String clearString(String value, boolean removeBrackets) {
        return clearString(value, removeBrackets, false);
    }

    /**
     * Use normaliseValue and normaliseHeader instead
     *
     * @param value
     * @param removeBrackets
     * @param useStemmer
     * @return
     */
    private static String clearString(String value, boolean removeBrackets, boolean useStemmer) {
        try {
            String cleanStr = "";
            for (String str : tokenizeString(value, removeBrackets, useStemmer)) {
                cleanStr += " " + str;
            }
            cleanStr = cleanStr.replaceFirst(" ", "");
            if (cleanStr.equals("")) {
                cleanStr = Variables.nullValue;
            }
            return cleanStr;
        } catch (Exception e) {
            return value;
        }

    }

    public static String clearString4FastJoin(String value, boolean removeBrackets, boolean useStemmer) {
        String v = clearString(value, removeBrackets, useStemmer);
        v = StringNormalizer.clearString(v, false, useStemmer);
        v = v.replaceAll("\\P{InBasic_Latin}", "");
        v = v.substring(0, Math.min(v.length(), 127));
        return v;
    }

    public static List<String> tokenizeString(String string,boolean removeBrackets) {
        return tokenizeString(string, removeBrackets, false);
    }

    public static List<String> tokenizeString(String string, boolean removeBrackets, boolean useStemmer) {
        string = string.replace("&nbsp;", " ");
        string = string.replace("&nbsp", " ");
        string = string.replace("nbsp", " ");

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        List<String> result = new ArrayList<>();

        try {
            Map<String, String> args = new HashMap<>();
            args.put("generateWordParts", "1");
            args.put("generateNumberParts", "1");
            args.put("catenateNumbers", "0");
            args.put("splitOnCaseChange", "1");
            WordDelimiterFilterFactory fact = new WordDelimiterFilterFactory(args);
            // resolve non unicode chars
            string = StringEscapeUtils.unescapeJava(string);
            // remove content in brackets
            if (removeBrackets) {
                string = string.replaceAll("\\(.*\\)", "");
            }
            TokenStream stream = fact.create(new WhitespaceTokenizer(Version.LUCENE_46, new StringReader(string)));
            stream.reset();

            if (useStemmer) {
                stream = new PorterStemFilter(stream);
            }
            stream = new LowerCaseFilter(Version.LUCENE_46, stream);
            stream = new StopFilter(Version.LUCENE_46, stream,
                    ((StopwordAnalyzerBase) analyzer).getStopwordSet());

            if (Variables.stopWords.size() > 0) {
                CharArraySet cas = new CharArraySet(Version.LUCENE_46, Variables.stopWords, true);
                stream = new StopFilter(Version.LUCENE_46, stream, cas);
            }

            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class)
                        .toString());
            }
            stream.close();
            analyzer.close();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
            result = new ArrayList<>();
            result.add(string);
        }
        if (string.contains("$")) {
            if (result.size() > 0 && !result.get(0).equals(string)) {
                result.add("$");
            }
        }
        return result;
    }

    public static String removeCustomStopwords(String s) {
        List<String> stopwords = Variables.stopWords;
        if (stopwords == null || stopwords.isEmpty()) {
            return s;
        }
        String result = s;

        for (String stop : stopwords) {
            result = result.replace(stop, "");
        }
        result = result.replace("  ", " ");

        return result;
    }

    /**
     * cleans the string from unwanted special characters 
     *
     * @param value
     * @return
     */
    public static String simpleStringNormalization(String value,
            boolean removeContentInBrackets) {
        try {
            value = StringEscapeUtils.unescapeJava(value);
            value = value.replace("\"", "");
            value = value.replace("|", " ");
            value = value.replace(",", "");
            value = value.replace("{", "");
            value = value.replace("}", "");
            value = value.replaceAll("\n", "");

            value = value.replace("&nbsp;", " ");
            value = value.replace("&nbsp", " ");
            value = value.replace("nbsp", " ");
            value = value.replaceAll("<.*>", "");
            if (removeContentInBrackets) {
                value = value.replaceAll("\\(.*\\)", "");
            }
            if (value.equals("")) {
                value = Variables.nullValue;
            }
            value = value.toLowerCase();
            value = value.trim();
        } catch (Exception e) {
        }
        return value;
    }

    public static String webStringNormalization(String value) {
        try {
            value = value.replaceAll("\n", "");
            value = value.replace("&nbsp;", " ");
            value = value.replace("&nbsp", " ");
            value = value.replaceAll("[&\\?]#[0-9]{1,3};", "");
            value = value.replace("nbsp", " ");
            value = value.replaceAll("<.*>", "");
            value = value.toLowerCase();
            value = value.trim();
            if (value.equals("")) {
                value = Variables.nullValue;
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    /**
     * Use normaliseValue and normaliseHeader instead
     *
     * @param columnName
     * @return
     */
    public static String cleanWebHeader(String columnName) {
        columnName = columnName.replace("&nbsp;", " ");
        columnName = columnName.replace("&nbsp", " ");
        columnName = columnName.replace("nbsp", " ");
        columnName = columnName.replaceAll("<.*>", "");
        columnName = columnName.replaceAll("\\.", "");
        columnName = columnName.replaceAll("\\$", "");
        // clean the values from additional strings
        if (columnName.contains("/")) {
            columnName = columnName.substring(0, columnName.indexOf("/"));
        }

        if (columnName.contains("\\")) {
            columnName = columnName.substring(0, columnName.indexOf("\\"));
        }

        if (columnName.contains("|")) {
            columnName = columnName.substring(0, columnName.indexOf("|"));
        }
        columnName = columnName.trim();
        return columnName;
    }
}
