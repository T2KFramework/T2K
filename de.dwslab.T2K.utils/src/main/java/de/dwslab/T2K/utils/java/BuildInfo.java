package de.dwslab.T2K.utils.java;

import java.net.JarURLConnection;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.chainsaw.Main;

public class BuildInfo {

    public static Long getBuildTime(Class<?> cl) {
        try {
            String rn = cl.getName().replace('.', '/') + ".class";
            JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
            return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getBuildTimeString(Class<?> cl) {
        return DateFormatUtils.format(getBuildTime(cl), "yyyy-MM-dd HH:mm:ss");
    }
    
}
