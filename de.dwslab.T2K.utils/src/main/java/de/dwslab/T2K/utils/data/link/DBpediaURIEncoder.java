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
package de.dwslab.T2K.utils.data.link;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author domi
 */
public class DBpediaURIEncoder {

    public static String encodeURIForDBpedia(String uri) throws UnsupportedEncodingException {
        String fragment ="";
        if(uri.contains("/resource/")) {
            fragment = uri.split("/resource/")[1];
        }
        if(uri.contains("/page/")) {
            fragment = uri.split("/page/")[1];
        }        
        fragment = URLEncoder.encode(fragment, "UTF-8");
        if (fragment.contains("%21")) {            
            fragment = fragment.replace("%21", "!");
        }
        if (fragment.contains("%25")) {            
            fragment = fragment.replace("%25", "%");
        }
        if (fragment.contains("%2C")) {
            fragment = fragment.replace("%2C", ",");
        }
        if (fragment.contains("%26")) {
            fragment = fragment.replace("%26", "&");
        }
        if (fragment.contains("%27")) {
            fragment = fragment.replace("%27", "'");
        }
        if (fragment.contains("%28")) {
            fragment = fragment.replace("%28", "(");
        }
        if (fragment.contains("%29")) {
            fragment = fragment.replace("%29", ")");
        }
        if (fragment.contains("%3A")) {
            fragment = fragment.replace("%3A", ":");
        }        
        if (fragment.contains("%C3%A9")) {
            fragment = fragment.replace("%C3%A9", "é");
        } 
        //String key = s[1].replaceAll("\\s+", " ");
        String encodedURI = "http://dbpedia.org/resource/" + fragment;
        return encodedURI;
    }
}
