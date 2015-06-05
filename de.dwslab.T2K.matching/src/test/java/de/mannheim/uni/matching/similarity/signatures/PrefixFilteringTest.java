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
package de.mannheim.uni.matching.similarity.signatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.adapters.IdentityAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.similarity.signatures.JaccardPrefixFiltering;
import de.dwslab.T2K.matching.similarity.signatures.PrefixFiltering;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.functions.string.SimpleJaccardSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import junit.framework.TestCase;

public class PrefixFilteringTest extends TestCase {

    private final int runs = 100;
    
    public void testFiltering() {
        PrefixFiltering<String> filter = new JaccardPrefixFiltering<>();
        //JaccardSimilarity sim = new JaccardSimilarity();
        SimpleJaccardSimilarity sim = new SimpleJaccardSimilarity();
        MatchingAdapter<String> adapter = new IdentityAdapter<>();
        
        List<String> values = new ArrayList<>();
        values.add("hello world test");
        List<String> candidates = new ArrayList<>();
        candidates.add("hello");
        candidates.add("world");
        candidates.add("test");
        candidates.add("hello world");
        candidates.add("hello world test");
        candidates.add("world test");
        candidates.add("hello test");
        candidates.add("does not match");
        candidates.add("just some other text");
        candidates.add("no");
        candidates.add("aaa bbb ccc ddd eee fff ggg hhh iii jjj kkk lll hello world test");
        
        System.out.println("Data:");
        for(String c : candidates) {
            System.out.println(String.format("%s <-> %s ==> %.4f", values.get(0), c, sim.calculate(values.get(0), c)));
        }
        
        Collection<Pair<String, String>> filtered = filter.filterCandidates(values, candidates, sim, adapter, 0.3);
        
        System.out.println("Candidates:");
        for(Pair<String, String> p : filtered) {
            System.out.println(String.format("%s <-> %s ==> %.4f", p.getFirst(), p.getSecond(), sim.calculate(p.getFirst(), p.getSecond())));
        }
    }
    
    public void testMatchingWithFiltering() {

//        System.out.println("Waiting ... attach Profiler now");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("Starting test...");
        
        List<String> values = new ArrayList<>();
        values.add("hello world test");
        List<String> candidates = new ArrayList<>();
        for(int j=0; j<1000; j++) {
            //candidates.add("hello " + j);
            //candidates.add("world " + j);
            //candidates.add("test " + j);
            candidates.add("hello world " + j);
            candidates.add("hello world test " + j);
            //candidates.add("world test " + j);
            //candidates.add("hello test " + j);
            candidates.add("does not match " + j);
            candidates.add("just some other text " + j);
            candidates.add("no " + j);
            candidates.add("aaa bbb ccc ddd eee fff ggg hhh iii jjj kkk lll hello world test " + j);
        }

//        testOli(values, candidates);
//        
//        testSecondString(values, candidates);


    }
    
    private void testSecondString(List<String> values, List<String> candidates) {
        System.out.println("*** SecondString Jaccard implementation ***");
        long ttlNoFilter = 0;
        long ttlFilter = 0;
        Timer tSecond = new Timer("SecondString test");
        for(int i=0; i<runs;i++) {
            PrefixFiltering<String> filter = new JaccardPrefixFiltering<>();
            //SimpleJaccardSimilarity sim = new SimpleJaccardSimilarity();
            JaccardSimilarity sim = new JaccardSimilarity();
            MatchingAdapter<String> adapter = new IdentityAdapter<>();
            StringSimilarityMeasure<String> measure = new StringSimilarityMeasure<>(sim, new MaxSimilarity<>());
            StringSimilarityMeasure<String> measureFilter = new StringSimilarityMeasure<>(sim, new MaxSimilarity<>());
            measureFilter.setSignatureFilter(filter);
            LabelBasedMatcher<String> matcher = new LabelBasedMatcher<>();
            LabelBasedMatcherWithFiltering<String> matcherFilter = new LabelBasedMatcherWithFiltering<>();
            matcher.setSimilarityMeasure(measure);
            matcherFilter.setSimilarityMeasure(measureFilter);
            
            matcher.setSimilarityThreshold(0.3);
            matcherFilter.setSimilarityThreshold(0.3);
            
            matcher.setParentTimer(tSecond);
            matcherFilter.setParentTimer(tSecond);
            
            matcher.setRunInParallel(false);
            matcherFilter.setRunInParallel(false);
            
            long start = System.currentTimeMillis();
            SimilarityMatrix<String> matrix = matcher.match(values, candidates, adapter);
            long noFilteringTime = System.currentTimeMillis() - start;
            if(i>0)
                ttlNoFilter += noFilteringTime;
            
            long start2 = System.currentTimeMillis();
            SimilarityMatrix<String> matrixFiltering = matcherFilter.match(values, candidates, adapter);
            long filteringTime = System.currentTimeMillis() - start2;
            if(i>0)
                ttlFilter += filteringTime;
            
            if(i==0)
                System.out.println(String.format("Without filtering: %d\tWith filtering: %d", noFilteringTime, filteringTime));
        }
        System.out.println(String.format("[Average w/o first run] Without filtering: %.2f\tWith filtering: %.2f", (double)ttlNoFilter/(runs-1), (double)ttlFilter/(runs-1)));
        tSecond.stop();
        System.out.println(tSecond.toString());
    }
    
    private void testOli(List<String> values, List<String> candidates) {
        System.out.println("*** Oli's Jaccard implementation ***");
        Timer tOli = new Timer("Oli test");
        long ttlNoFilter=0;
        long ttlFilter=0;
        for(int i=0; i<runs;i++) {
            PrefixFiltering<String> filter = new JaccardPrefixFiltering<>();
            SimpleJaccardSimilarity sim = new SimpleJaccardSimilarity();
            //JaccardSimilarity sim = new JaccardSimilarity();
            MatchingAdapter<String> adapter = new IdentityAdapter<>();
            StringSimilarityMeasure<String> measure = new StringSimilarityMeasure<>(sim, new MaxSimilarity<>());
            StringSimilarityMeasure<String> measureFilter = new StringSimilarityMeasure<>(sim, new MaxSimilarity<>());
            measureFilter.setSignatureFilter(filter);
            LabelBasedMatcher<String> matcher = new LabelBasedMatcher<>();
            LabelBasedMatcherWithFiltering<String> matcherFilter = new LabelBasedMatcherWithFiltering<>();
            matcher.setSimilarityMeasure(measure);
            matcherFilter.setSimilarityMeasure(measureFilter);
            
            matcher.setSimilarityThreshold(0.3);
            matcherFilter.setSimilarityThreshold(0.3);
            
            matcher.setParentTimer(tOli);
            matcherFilter.setParentTimer(tOli);
            
            matcher.setRunInParallel(false);
            matcherFilter.setRunInParallel(false);
            
            long start = System.currentTimeMillis();
            SimilarityMatrix<String> matrix = matcher.match(values, candidates, adapter);
            long noFilteringTime = System.currentTimeMillis() - start;
            if(i>0)
                ttlNoFilter += noFilteringTime;
            
            long start2 = System.currentTimeMillis();
            SimilarityMatrix<String> matrixFiltering = matcherFilter.match(values, candidates, adapter);
            long filteringTime = System.currentTimeMillis() - start2;
            if(i>0)
                ttlFilter += filteringTime;
            
            if(i==0)
                System.out.println(String.format("Without filtering: %d\tWith filtering: %d", noFilteringTime, filteringTime));
        }
        System.out.println(String.format("[Average w/o first run] Without filtering: %.2f\tWith filtering: %.2f", (double)ttlNoFilter/(runs-1), (double)ttlFilter/(runs-1)));
        tOli.stop();
        System.out.println(tOli.toString());
    }
}
