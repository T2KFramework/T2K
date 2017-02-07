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
package de.dwslab.T2K.matching;

import java.util.Collection;

import de.dwslab.T2K.matching.firstline.FastJoinMatcher;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.matching.firstline.ValueBasedMatcher;
import de.dwslab.T2K.matching.firstline.FastJoinMatcher.FastJoinMeasure;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.BestChoiceMatching;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.matching.secondline.CombineHierarchy;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.matching.secondline.TopKCandidates;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.SimilarityMeasure;

/**
 * Facade class providing static methods for various matching tasks
 * @author Oliver
 *
 */
public class Matcher {

    /**
     * Runs label matching on the provided instances and candidates. The labels are accessed via the labelAdapter and compared with the measure.
     * @param instances
     * @param candidates
     * @param labelAdapter
     * @param measure
     * @return
     */
    public static <T> SimilarityMatrix<T> matchLabels(Collection<T> instances, Collection<T> candidates, MatchingAdapter<T> labelAdapter, SimilarityMeasure measure) {
        LabelBasedMatcher<T> lbl = new LabelBasedMatcher<T>();
        lbl.setSimilarityMeasure(measure);
        return lbl.match(instances, candidates, labelAdapter);
    }
    
    public static <T> SimilarityMatrix<T> matchLabelsWithFastJoin(Collection<T> instances, Collection<T> candidates, MatchingAdapter<T> labelAdapter, String fastJoinPath, FastJoinMeasure measure, double delta, double tau) {
        FastJoinMatcher<T> fj = new FastJoinMatcher<T>();
        fj.setFastJoinPath(fastJoinPath);
        fj.setFastJoinMeasure(measure);
        fj.setDelta(delta);
        fj.setTau(tau);
        return fj.match(instances, candidates, labelAdapter);
    }
    
    public static <TInstance, TValue> SimilarityMatrix<TValue> matchValues(Collection<TInstance> instances, Collection<TInstance> candidates, MatchingHierarchyAdapater<TInstance, TValue> hierarchy, MatchingAdapter<TValue> valueAdapter, SimilarityMeasure measure) {
        ValueBasedMatcher<TInstance, TValue> val = new ValueBasedMatcher<TInstance, TValue>();
        val.setSimilarityMeasure(measure);
        return val.match(instances, candidates, hierarchy, valueAdapter);
    }
    
    public static <T extends Comparable<T>> SimilarityMatrix<T> selectBestCandidateForEachInstance(ConflictResolution conf, SimilarityMatrix<T> input) {
        OneToOneConstraint one = new OneToOneConstraint(conf);
        return one.match(input);
    }
    
    public static <T extends Comparable<T>> SimilarityMatrix<T> selectBestStableCandidateForEachInstance(boolean forceOneToOneMapping, SimilarityMatrix<T> input) {
        BestChoiceMatching best = new BestChoiceMatching();
        best.setForceOneToOneMapping(forceOneToOneMapping);
        return best.match(input);
    }
    
    public static <T extends Comparable<T>> SimilarityMatrix<T> selectBestStableCandidateForEachInstance(boolean forceOneToOneMapping, SimilarityMatrix<T> input, SimilarityMatrixFactory matrixFactory) {
        BestChoiceMatching best = new BestChoiceMatching();
        best.setSimilarityMatrixFactory(matrixFactory);
        best.setForceOneToOneMapping(forceOneToOneMapping);
        return best.match(input);
    }
    
    public static <T extends Comparable<T>> SimilarityMatrix<T> selectedTopKCandidatesForEachInstance(int k, SimilarityMatrix<T> input) {
        TopKCandidates top = new TopKCandidates();
        return top.match(input, k);
    }
    
    public static <T> SimilarityMatrix<T> addSimilarity(SimilarityMatrix<T> first, SimilarityMatrix<T> second) {
        System.out.println("right method");
        Combine<T> c = new Combine<T>();
        c.setAggregationType(CombinationType.Sum);
        return c.match(first, second);
    }
    
    public static <T> SimilarityMatrix<T> multiplySimilarity(SimilarityMatrix<T> first, SimilarityMatrix<T> second) {
        Combine<T> c = new Combine<T>();
        c.setAggregationType(CombinationType.Multiply);
        return c.match(first, second);
    }
    
    public static <T> SimilarityMatrix<T> averageSimilarity(SimilarityMatrix<T> first, SimilarityMatrix<T> second) {
        Combine<T> c = new Combine<T>();
        c.setAggregationType(CombinationType.Average);
        return c.match(first, second);
    }
    
    public static <T> SimilarityMatrix<T> weightedSumSimilarity(SimilarityMatrix<T> first, double firstWeight, SimilarityMatrix<T> second, double secondWeight) {
        Combine<T> c = new Combine<T>();
        c.setAggregationType(CombinationType.WeightedSum);
        c.setFirstWeight(firstWeight);
        c.setSecondWeight(secondWeight);
        return c.match(first, second);
    }
    
    public static <TParent, TChild> SimilarityMatrix<TChild> addParentSimilarity(SimilarityMatrix<TParent> parent, SimilarityMatrix<TChild> child, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        CombineHierarchy<TParent, TChild> c = new CombineHierarchy<TParent, TChild>();
        c.setAggregationType(CombinationType.Sum);
        return c.match(parent, child, hierarchy);
    }
    
    public static <TParent, TChild> SimilarityMatrix<TChild> multiplyParentSimilarity(SimilarityMatrix<TParent> parent, SimilarityMatrix<TChild> child, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        CombineHierarchy<TParent, TChild> c = new CombineHierarchy<TParent, TChild>();
        c.setAggregationType(CombinationType.Multiply);
        return c.match(parent, child, hierarchy);
    }
    
    public static <TParent, TChild> SimilarityMatrix<TChild> averageWithParentSimilarity(SimilarityMatrix<TParent> parent, SimilarityMatrix<TChild> child, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        CombineHierarchy<TParent, TChild> c = new CombineHierarchy<TParent, TChild>();
        c.setAggregationType(CombinationType.Average);
        return c.match(parent, child, hierarchy);
    }
    
    public static <TParent, TChild> SimilarityMatrix<TChild> weightedSumWithParentSimilarity(SimilarityMatrix<TParent> parent, double parentWeight, SimilarityMatrix<TChild> child, double childWeight, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        CombineHierarchy<TParent, TChild> c = new CombineHierarchy<TParent, TChild>();
        c.setAggregationType(CombinationType.WeightedSum);
        c.setFirstWeight(parentWeight);
        c.setSecondWeight(childWeight);
        return c.match(parent, child, hierarchy);
    }
    
    public static <TChild, TParent> SimilarityMatrix<TParent> sumChildrenSimilarity(SimilarityMatrix<TChild> child, SimilarityMatrix<TParent> parent, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        Aggregate<TParent, TChild> a = new Aggregate<TParent, TChild>();
        a.setAggregationType(AggregationType.Sum);
        return a.match(parent, child, hierarchy);
    }
    
    public static <TChild, TParent> SimilarityMatrix<TParent> countChildrenSimilarity(SimilarityMatrix<TChild> child, SimilarityMatrix<TParent> parent, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        Aggregate<TParent, TChild> a = new Aggregate<TParent, TChild>();
        a.setAggregationType(AggregationType.Count);
        return a.match(parent, child, hierarchy);
    }
    
    public static <TChild, TParent> SimilarityMatrix<TParent> averageChildrenSimilarity(SimilarityMatrix<TChild> child, SimilarityMatrix<TParent> parent, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        Aggregate<TParent, TChild> a = new Aggregate<TParent, TChild>();
        a.setAggregationType(AggregationType.Average);
        return a.match(parent, child, hierarchy);
    }
    
    public static <TChild, TParent> SimilarityMatrix<TParent> maxChildrenSimilarity(SimilarityMatrix<TChild> child, SimilarityMatrix<TParent> parent, MatchingHierarchyAdapater<TParent, TChild> hierarchy) {
        Aggregate<TParent, TChild> a = new Aggregate<TParent, TChild>();
        a.setAggregationType(AggregationType.Max);
        return a.match(parent, child, hierarchy);
    }
}
