package de.dwslab.T2K.utils.data.string;

/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


/**
 *
 * @author dritze
 */

public class LanguageEncoder {

    public String encodeString(String s) {
        if (s.contains("&mdash")) {
            s = s.replace("&mdash", "-");
        }
        if (s.contains("&Aring;")) {
            s = s.replace("&Aring;", "Å");
        }
        if (s.contains("&aring;")) {
            s = s.replace("&aring;", "å");
        }
        if (s.contains("&oslash;")) {
            s = s.replace("&oslash;", "ø");
        }
        if (s.contains("&Oslash;")) {
            s = s.replace("&Oslash;", "Ø");
        }
        if (s.contains("&aelig;")) {
            s = s.replace("&aelig;", "æ");
        }
        if (s.contains("&AElig;")) {
            s = s.replace("&AElig;", "Æ");
        }
        
        if (s.contains("&Oacute;")) {
            s = s.replace("&Oacute;", "Ó");
        }
        if (s.contains("&oacute;")) {
            s = s.replace("&oacute;", "ó");
        }
        
        if (s.contains("&Eacute;")) {
            s = s.replace("&Eacute;", "É");
        }
        if (s.contains("&eacute;")) {
            s = s.replace("&eacute;", "é");
        }
        
        if (s.contains("&Iacute;")) {
            s = s.replace("&Iacute;", "Í");
        }
        if (s.contains("&Iacute;")) {
            s = s.replace("&Iacute;", "í");
        }
        
        if (s.contains("&Aacute;")) {
            s = s.replace("&Aacute;", "Á");
        }
        if (s.contains("&aacute;")) {
            s = s.replace("&aacute;", "á");
        }
        
        if (s.contains("&Uacute;")) {
            s = s.replace("&Uacute;", "Ú");
        }
        if (s.contains("&uacute;")) {
            s = s.replace("&uacute;", "ú");
        }
        
        if (s.contains("&Ouml;")) {
            s = s.replace("&Ouml;", "Ö");
        }
        if (s.contains("&ouml;")) {
            s = s.replace("&ouml;", "ö");
        }
        
        if (s.contains("&Auml;")) {
            s = s.replace("&Auml;", "Ä");
        }
        if (s.contains("&auml;")) {
            s = s.replace("&auml;", "ä");
        }
        
        if (s.contains("&Uuml;")) {
            s = s.replace("&Uuml;", "Ü");
        }
        if (s.contains("&uuml;")) {
            s = s.replace("&uuml;", "ü");
        }
                
        if (s.contains("&Iuml;")) {
            s = s.replace("&Iuml;", "Ï");
        }
        if (s.contains("&Iuml;")) {
            s = s.replace("&Iuml;", "ï");
        }
        
        if (s.contains("&Ntilde;")) {
            s = s.replace("&Ntilde;", "Ñ");
        }
        if (s.contains("&ntilde;")) {
            s = s.replace("&ntilde;", "ñ");
        }
        
        if (s.contains("&Agrave;")) {
            s = s.replace("&Agrave;", "À");
        }
        if (s.contains("&agrave;")) {
            s = s.replace("&agrave;", "à");
        }
        
        if (s.contains("&Egrave;")) {
            s = s.replace("&Egrave;", "È");
        }
        if (s.contains("&egrave;")) {
            s = s.replace("&egrave;", "è");
        }
        
        if (s.contains("&Ograve;")) {
            s = s.replace("&Ograve;", "Ò");
        }
        if (s.contains("&ograve;")) {
            s = s.replace("&ograve;", "ò");
        }
        
        if (s.contains("&Ugrave;")) {
            s = s.replace("&Ugrave;", "Ù");
        }
        if (s.contains("&ugrave;")) {
            s = s.replace("&ugrave;", "ù");
        }
        
        if (s.contains("&Ecirc;")) {
            s = s.replace("&Ecirc;", "Ê");
        }
        if (s.contains("&ecirc;")) {
            s = s.replace("&ecirc;", "ê");
        }
        
        if (s.contains("&Icirc;")) {
            s = s.replace("&Icirc;", "Î");
        }
        if (s.contains("&icirc;")) {
            s = s.replace("&icirc;", "î");
        }
        
        if (s.contains("&Acirc;")) {
            s = s.replace("&Acirc;", "Â");
        }
        if (s.contains("&acirc;")) {
            s = s.replace("&acirc;", "â");
        }
        
        if (s.contains("&Ocirc;")) {
            s = s.replace("&Ocirc;", "Ô");
        }
        if (s.contains("&ocirc;")) {
            s = s.replace("&ocirc;", "ô");
        }
        
        if (s.contains("&Ucirc;")) {
            s = s.replace("&Ucirc;", "Û");
        }
        if (s.contains("&ucirc;")) {
            s = s.replace("&ucirc;", "û");
        }
        
        if (s.contains("&Ccedil;")) {
            s = s.replace("&Ccedil;", "Ç");
        }
        if (s.contains("&ccedil;")) {
            s = s.replace("&ccedil;", "ç");
        }
        
        return s;
    }
}
