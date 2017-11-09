package de.dwslab.T2K.matching.firstline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang3.StringUtils;
import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;

/**
 * A wrapper class for the FastJoin matcher
 * @author Oliver
 *
 * @param <T>
 */
public class FastJoinMatcher<T> extends LabelBasedMatcher<T> {

    private String fastJoinPath;
    public String getFastJoinPath() {
        return fastJoinPath;
    }
    public void setFastJoinPath(String fastJoinPath) {
        this.fastJoinPath = fastJoinPath;
    }

    public enum FastJoinMeasure {
        FJACCARD,
        FCOSINE,
        FDICE
    }
    
    private FastJoinMeasure fastJoinMeasure;
    public FastJoinMeasure getFastJoinMeasure() {
        return fastJoinMeasure;
    }
    public void setFastJoinMeasure(FastJoinMeasure fastJoinMeasure) {
        this.fastJoinMeasure = fastJoinMeasure;
    }
    
    private double delta;
    public double getDelta() {
        return delta;
    }
    public void setDelta(double delta) {
        this.delta = delta;
    }
    
    private double tau;
    public double getTau() {
        return tau;
    }
    public void setTau(double tau) {
        this.tau = tau;
    }
    
    @Override
    public SimilarityMatrix<T> match(Collection<T> instancesToMatch,
            final Collection<T> candidates, MatchingAdapter<T> adapter) {
        
        // create similarity matrix
        SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(instancesToMatch.size(), candidates.size());
        
        final Collection<T> blockedCandidates;
        
        if(getBlocking()!=null) {
            blockedCandidates = new ConcurrentLinkedQueue<T>();
            
            // iterate over all instances to match
            try {
                new Parallel<T>().foreach(instancesToMatch, new Consumer<T>() {
    
                    public void execute(T parameter) {
                        // iterate over all candidates (all instances that could be matched to the current instance)
                        for(T candidate : getBlocking().getCandidates(parameter, candidates))
                        {
                            blockedCandidates.add(candidate);
                        }
                    }
                    
                }, "FastJoinMatcher: Blocking");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            blockedCandidates = candidates;
        }
        
        // run fastjoin
        matchPairs(instancesToMatch, blockedCandidates, adapter, sim);
        
        return sim;
    }
    
    
    public void matchPairs(Collection<T> first, Collection<T> second, MatchingAdapter<T> adapter, SimilarityMatrix<T> sim){
        String firstName, secondName;
        firstName = "fastjoin_first.txt";
        secondName = "fastjoin_second.txt";
        
        Map<Integer, T> firstMap = null;
        Map<Integer, T> secondMap = null;
        
        firstMap = writeValues(first, firstName, adapter);
        secondMap = writeValues(second, secondName, adapter);
        
        boolean switchOrder = secondMap.size() > firstMap.size();
        System.out.println("Switching inputs as second one is larger than first one!");
        
        Process p;
        List<String> cmd = new ArrayList<String>();
        cmd.add(getFastJoinPath());
        cmd.add(getFastJoinMeasure().toString());
        cmd.add(Double.toString(getDelta()));
        cmd.add(Double.toString(getTau()));
        
        if(!switchOrder)
        {
            cmd.add(firstName);
            cmd.add(secondName);
        }
        else
        {
            cmd.add(secondName);
            cmd.add(firstName);
            
            Map<Integer, T> tmp = firstMap;
            firstMap = secondMap;
            secondMap = tmp;
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        try {
            System.out.println("Running ...");
            for(String s : cmd)
             System.out.print(s + " ");
            System.out.println();
            
            p = pb.start();
            
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedWriter w = new BufferedWriter(new FileWriter("fastjoin.output"));
            
            String line;
            double similarity = 0.0;
            String srcLine1, srcLine2;
            String entity1;
            String entity2;

            while ((line = bri.readLine()) != null) {
                w.write(line + "\n");
                
                String[] parts = line.split(" ");
                similarity = Double.parseDouble(parts[0]);
                

                srcLine1 = parts[1];
                srcLine2 = parts[2];
                entity1 = bri.readLine();
                w.write(entity1 + "\n");
                entity2 = bri.readLine();
                w.write(entity2 + "\n");

                bri.readLine();
                w.write("\n");
                
                T instance = null, candidate = null;
                
                if(!switchOrder)
                {
                    instance = firstMap.get(Integer.parseInt(srcLine1));
                    candidate = secondMap.get(Integer.parseInt(srcLine2));
                }
                else
                {
                    instance = secondMap.get(Integer.parseInt(srcLine2));
                    candidate = firstMap.get(Integer.parseInt(srcLine1));
                }
                 
                sim.set(instance, candidate, similarity);
                
                
            }
            bri.close();
            w.close();
            
            BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = er.readLine()) != null) {
            System.out.println(line);
            }
            er.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Map<Integer, T> writeValues(Collection<T> values, String fileName, MatchingAdapter<T> adapter)
    {
        Map<Integer, T> m = new HashMap<Integer, T>();
        
        System.out.print("Writing " + fileName);
        
        int i = 0;
        
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
            for(T instance : values)
            {
                m.put(i, instance);
                String normValue = StringNormalizer.normaliseValue(adapter.getLabel(instance).toString(), true);
                normValue = normaliseString4FastJoin(normValue);
                w.write(normValue + "\n");
                i++;
            }
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println(String.format(" (%d lines)", i));
        
        return m;
    }
    
    protected String normaliseString4FastJoin(String value) {
        if(value==null) {
            return value;
        } else {
            String v = value.replaceAll("\\P{InBasic_Latin}", "");
            
            String[] tokens = v.split("\\s");
            
            if(tokens.length>127) {
                ArrayList<String> first = new ArrayList<String>(128);
                
                for(int i=0;i<128;i++) {
                    first.add(tokens[i]);
                }
                
                v = StringUtils.join(first, " ");
            }
            return v;
        }
    }
}
