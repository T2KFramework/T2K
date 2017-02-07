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
package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToColumnMatchingAdapter;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Collection;

public class PropertyBasedClassMatcher extends PartialMatcher<Table> {

    public PropertyBasedClassMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    private SimilarityMatrix<TableColumn> propertySimilarity;

    public SimilarityMatrix<TableColumn> getPropertySimilarity() {
        return propertySimilarity;
    }

    public void setPropertySimilarity(
            SimilarityMatrix<TableColumn> propertySimilarity) {
        this.propertySimilarity = propertySimilarity;
    }
    private SimilarityMatrix<Table> classSimilarity;

    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }

    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }
    private Table finalClass;

    public Table getFinalClass() {
        return finalClass;
    }

    public SimilarityMatrix<Table> match(MatchingData data) {
        Timer tim = Timer.getNamed("Property-based class matching", getRootTimer());
        SimilarityMatrix<TableColumn> prop = getPropertySimilarity().copy();

        // don't take into account matching keys (we want to remove classes if nothing but the key matches)
        //test!
//        for (Table t : getClassSimilarity().getSecondDimension()) {
//            prop.set(data.getWebtable().getKey(), t.getKey(), null);
//        }

        //TODO it could be a good idea to prune the property similarities beforehand, as we don't want a false class to be matches just because of a single value that overlaps by coincidence

        Aggregate<Table, TableColumn> classPropSum = new Aggregate<Table, TableColumn>();
        classPropSum.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
        // use sum here and make a final class decision afterwards (with count we don't know which class was best)
        // if we don't make a single class decision, we can end up with keys 
        // (properties) from different classes being matched and in each iteration a random key mapping is chosen ...
        classPropSum.setAggregationType(AggregationType.Sum);
        Timer tm = Timer.getNamed("Aggregate property similarities to class similarities", tim);

        SimilarityMatrix<Table> classPropSim = classPropSum.match(getClassSimilarity(), prop, new TableToColumnMatchingAdapter());
                
//        Combine<Table> combineClassAndProp = new Combine<>();
//        combineClassAndProp.setAggregationType(CombinationType.Sum);
//        SimilarityMatrix<Table>classPropSimCombinedWithKey = combineClassAndProp.match(classPropSim, getClassSimilarity());
//        
//        classPropSim = classPropSimCombinedWithKey;
        
        tm.stop();

//      if(getPropertySimilarity().getFirstDimension().size()>1) {
//          classPropSim.normalize(getPropertySimilarity().getFirstDimension().size()-1);
//      }

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property-based class matching");
            System.out.println(classPropSim.getOutput());
            System.out.println(classPropSum.getLog().toString());
        }

        SimilarityMatrix<Table> newClass = null;

        // if there is no matching property, we cannot restrict the classes any further
        if (classPropSim.getNumberOfNonZeroElements() > 0) {
            classPropSim.normalize();

            // multiply with previous class similarity too keep only those classes that have matching properties
            //Combine<Table> c = new Combine<Table>();
            //c.setAggregationType(CombinationType.Multiply);
            //SimilarityMatrix<Table> newClass = c.match(getSimilarities().getClassSimilarity(), classPropSim);
            //SimilarityMatrix<Table> newClass = classPropSim;

            newClass = classPropSim;
            //getSimilarities().setClassSimilarity(newClass);

        } else {
            newClass = classSimilarity;
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println("No property mappings: cannot calculate property-based class similarity!");
            }
        }

        //run a 1:1 matcher here to choose the final class
        OneToOneConstraint one = new OneToOneConstraint(ConflictResolution.Maximum);              
        SimilarityMatrix<Table> finalCls = one.match(newClass);
        finalCls.normalize();
        

        //getSimilarities().setClassSimilarity(finalCls);

        if (finalCls.getFirstDimension().size() > 0 && finalCls.getMatchesAboveThreshold(data.getWebtable(), 0.0).size()>0) {
//            try {
//                finalClass = finalCls.getMatchesAboveThreshold(data.getWebtable(), 0.0).iterator().next();
//            } catch(Exception e) {
//                e.printStackTrace();
//                System.err.println(finalCls.getOutput());
//                System.err.println(data.getWebtable());
//                throw e;
//            }
            finalClass = finalCls.getMatchesAboveThreshold(data.getWebtable(), 0.0).iterator().next();
            
            //getSimilarities().setFinalClass(getSimilarities().getClassSimilarity().getMatchesAboveThreshold(data.getWebtable(), 0.0).iterator().next());
            applyClassHierarchy(finalCls, data.getDbpediaTables());
            getLogger().logData("Class matching\n" + finalCls.getOutput());

            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println("Final class decision: " + getFinalClass().getHeader());
            }
            getLogger().logData("Final class decision: " + getFinalClass().getHeader());
        }
        tim.stop();

        return finalCls;
    }

    protected void applyClassHierarchy(SimilarityMatrix<Table> sim, Collection<Table> dbpediaTables) {
        //SimilarityMatrix<Table> sim = getSimilarities().getClassSimilarity();

        // include class hierarchy
        Table table = sim.getFirstDimension().iterator().next();
        Table cls = sim.getSecondDimension().iterator().next();
        String clsName = cls.getHeader().replace(".csv", "");
        if (getGoldStandard() != null && getGoldStandard().getClassHierarchy() != null) {
            String parent = getGoldStandard().getClassHierarchy().get(clsName);

            while (parent != null) {

                String newParent = null;

                for (Table t : dbpediaTables) {
                    clsName = t.getHeader().replace(".csv", "");

                    if (clsName.equals(parent)) {
                        sim.set(table, t, 0.99); // don't set it to 1 so that the original class gets chosen as final class
                        newParent = getGoldStandard().getClassHierarchy().get(parent);
                        break;
                    }
                }

                parent = newParent;

            }
        }
    }
}
