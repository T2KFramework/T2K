package de.dwslab.T2K.tableFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.chainsaw.Main;

import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.concurrent.Producer;
import de.dwslab.T2K.utils.io.SynchronizedTextWriter;
import de.dwslab.T2K.utils.io.TarArchive;
import de.dwslab.T2K.utils.io.TarFileIterator;
import de.dwslab.T2K.utils.java.BuildInfo;

public class FilterTablesByMetadata {

    public static void main(String[] args) throws Exception {
        
        System.out.println(String.format("Table Extractor Build %s", BuildInfo.getBuildTimeString(Main.class)));
        
        if(args.length!=3) {
            System.err.println("Usage: <metadata file> <input folder> <output folder>");
        }
        
        String metaFile = args[0];
        String sourceFolder = args[1];
        String destinationFolder = args[2];
        
        FilterTablesByMetadata ftm = new FilterTablesByMetadata();
        
        ftm.run(metaFile, sourceFolder, destinationFolder);
    }
    
    public void run(String metaFile, final String sourceFolder, final String destinationFolder) throws Exception {
        
        System.out.println("Reading metadata");
        final Set<String> allowedFiles = getAllowedTables(metaFile);
        
        final SynchronizedTextWriter log = new SynchronizedTextWriter("processed_files.txt");
        
        final AtomicInteger fileCount = new AtomicInteger();
        final AtomicInteger currentDirectoryNumber = new AtomicInteger(1);
        
        System.out.println(String.format("Found %d allowed tables", allowedFiles.size()));
        
        new Parallel<File>().tryForeach(Arrays.asList(new File(sourceFolder).listFiles()), new Consumer<File>() {

            @Override
            public void execute(File parameter) {
//                TarArchive tar = new TarArchive(parameter.getAbsolutePath());
                
//                File extracted;
                TarFileIterator it = null;
                try {
                    it = new TarFileIterator(parameter.getAbsolutePath(), true, true);
                    
                    InputStream is = null;
                    
                    while((is = it.getNext()) != null) {
                        
                        File f = it.getCurrentFile();
                        
                        if(f.getName().endsWith("csv") && allowedFiles.contains(f.getName())) {
                            String fileName = String.format("%s#%s", parameter.getName(), f.getName());
                            
                            File folder = new File(destinationFolder, String.format("%d000000", currentDirectoryNumber.get()));
                            
                            if(!folder.exists()) {
                                folder.mkdirs();
                            }
                            
                            File fOut = new File(folder, fileName);
                            OutputStream out = new FileOutputStream(fOut);
                            IOUtils.copy(is, out);
                            out.close();
                            
                            int fCnt = fileCount.incrementAndGet();
                            log.write(String.format("Copied file %s [%d]", f.getName(), fCnt));
                            
                            if(fCnt%1000000==0) {
                                currentDirectoryNumber.incrementAndGet();
                                log.write("*** creating new directory ***");
                            }
                        } else {
                            log.write("Skipped file " + f.getName());
                        }
                        
                        is.close();
                    }
                    
                    it.close();
//                    extracted = tar.extract();
//                    
//                    for(File f : extracted.listFiles(new FilenameFilter() {
//                        
//                        @Override
//                        public boolean accept(File dir, String name) {
//                            return name.endsWith("csv");
//                        }
//                    })) {
//                        String fileName = String.format("%s#%s", parameter.getName(), f.getName());
//                        
//                        if(allowedFiles.contains(f.getName())) {
//                            try {
//                                FileUtils.copyFile(f, new File(new File(destinationFolder), fileName));
//                            } catch(Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    
//                    tar.deleteExtracted();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        log.flushAndBlock();
        
        System.out.println("done.");
        
    }
    
    private Set<String> getAllowedTables(final String metaFile) {
        final ConcurrentHashMap<String, Object> allowed = new ConcurrentHashMap<>();
        final Object o = new Object();
        
//        for(TableMetaData m : TableMetaData.readFromFile(metaFile)) {
//            allowed.add(m.getFile());
//        }
        
        new Parallel<String[]>().producerConsumer(new Producer<String[]>() {
            
            @Override
            public void execute() {
                try {
                    FileInputStream fi = new FileInputStream(new File(metaFile));
                    InputStream inStream = null;
                    
                    if(metaFile.endsWith("gz")) {
                        inStream = new GZIPInputStream(fi);
                    } else {
                        inStream = fi;
                    }
                    
                    BufferedReader r = new BufferedReader(new InputStreamReader(inStream));
                    
                    String line = null;
                    
                    while((line = r.readLine()) != null) {
                        
                        String[] values = line.split("\\\"\\|\\\"");
                        
                        produce(values);
                        
                    }
                    
                    r.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Consumer<String[]>() {

            @Override
            public void execute(String[] parameter) {
                if(parameter.length==7 && parameter[5]!=null && parameter[5].contains("/")) {
                    allowed.put(parameter[5].split("/")[1], o);
                }                    
            }
        });
        
        return allowed.keySet();
    }
    
}
