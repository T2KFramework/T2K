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
package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import java.util.Date;

import de.dwslab.T2K.matching.blocking.SecondLineBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableCellMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.firstline.ValueBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.IdentitySimilarity;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.similarity.functions.set.GeneralisedJaccard;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.TypeBasedSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Value-based instance matcher
 * 
 * @author Oliver
 * 
 */
public class ValueBasedInstanceMatcher extends PartialMatcher<TableCell> {

    public ValueBasedInstanceMatcher(Similarities sim, MatchingParameters par,
            Timer t, GoldStandard g, MatchingLogger logger) {
        super(sim, par, t, g, logger);
    }

    private SimilarityMatrix<TableRow> candidateSimilarity;
    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return candidateSimilarity;
    }
    public void setCandidateSimilarity(
            SimilarityMatrix<TableRow> candidateSimilarity) {
        this.candidateSimilarity = candidateSimilarity;
    }
    
    private SimilarityFunction<String> stringSimilarityFunction;
    public SimilarityFunction<String> getStringSimilarityFunction() {
        return stringSimilarityFunction;
    }
    public void setStringSimilarityFunction(
            SimilarityFunction<String> stringSimilarityFunction) {
        this.stringSimilarityFunction = stringSimilarityFunction;
    }
    
    private SimilarityFunction<String> stringSimilarityFunctionForSets;
    public SimilarityFunction<String> getStringSimilarityFunctionForSets() {
        return stringSimilarityFunctionForSets;
    }
    public void setStringSimilarityFunctionForSets(
            SimilarityFunction<String> stringSimilarityFunctionForSets) {
        this.stringSimilarityFunctionForSets = stringSimilarityFunctionForSets;
    }
    
    private SignatureFilter<TableCell> stringFilter;
    public SignatureFilter<TableCell> getStringFilter() {
        return stringFilter;
    }
    public void setStringFilter(SignatureFilter<TableCell> stringFilter) {
        this.stringFilter = stringFilter;
    }
    
    private SimilarityFunction<Double> numericSimilarityFunction;
    public SimilarityFunction<Double> getNumericSimilarityFunction() {
        return numericSimilarityFunction;
    }
    public void setNumericSimilarityFunction(
            SimilarityFunction<Double> numericSimilarityFunction) {
        this.numericSimilarityFunction = numericSimilarityFunction;
    }
    
    private SimilarityFunction<Date> dateSimilarityFunction;
    public SimilarityFunction<Date> getDateSimilarityFunction() {
        return dateSimilarityFunction;
    }
    public void setDateSimilarityFunction(
            SimilarityFunction<Date> dateSimilarityFunction) {
        this.dateSimilarityFunction = dateSimilarityFunction;
    }
    
    private double similarityThreshold;
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    private ComplexSetSimilarity<String> stringSetSimilarity;
    public ComplexSetSimilarity<String> getStringSetSimilarity() {
        return stringSetSimilarity;
    }
    public void setStringSetSimilarity(
            ComplexSetSimilarity<String> stringSetSimilarity) {
        this.stringSetSimilarity = stringSetSimilarity;
    }
    
    private ComplexSetSimilarity<Double> numericSetSimilarity;
    public ComplexSetSimilarity<Double> getNumericSetSimilarity() {
        return numericSetSimilarity;
    }
    public void setNumericSetSimilarity(
            ComplexSetSimilarity<Double> numericSetSimilarity) {
        this.numericSetSimilarity = numericSetSimilarity;
    }
    
    private ComplexSetSimilarity<Date> dateSetSimilarity;
    public ComplexSetSimilarity<Date> getDateSetSimilarity() {
        return dateSetSimilarity;
    }
    public void setDateSetSimilarity(
            ComplexSetSimilarity<Date> dateSetSimilarity) {
        this.dateSetSimilarity = dateSetSimilarity;
    }
    
    private double keyWeight;
    public double getKeyWeight() {
        return keyWeight;
    }
    public void setKeyWeight(double keyWeight) {
        this.keyWeight = keyWeight;
    }
    
    public SimilarityMatrix<TableCell> match(MatchingData data) {
        Timer t = Timer.getNamed("Value matching", getRootTimer());

        /*
         * run value-based instance matching
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("value-based instance matching ... ");
        }

        //ValueBasedMatcher<TableRow, TableCell> instanceMatching = new ValueBasedMatcher<>();
        ValueBasedMatcherWithFiltering<TableRow, TableCell> instanceMatching = new ValueBasedMatcherWithFiltering<>();

        // blocking: re-use blocking from candidateSelection
        instanceMatching.setBlocking(new SecondLineBlocking<TableRow>(
                getCandidateSimilarity()));

        // value blocking based on column types
        instanceMatching.setValueBlocking(new TypeBasedBlocking<TableCell>(
                new TableCellMatchingAdapter()));

        // similarity: based on column types ...
        TypeBasedSimilarityMeasure<TableCell> typeSim = new TypeBasedSimilarityMeasure<>();
        
        typeSim.getSimilarityFunctions().put(ColumnDataType.string, getStringSimilarityFunction());
        typeSim.getSetSimilarities().put(ColumnDataType.string, getStringSetSimilarity());
        if(getStringSimilarityFunctionForSets()!=null) {
            typeSim.getSimilarityFunctionsForSets().put(ColumnDataType.string, getStringSimilarityFunctionForSets());
        }
        //TODO the following two lines speed up similarity computation on lists, but produce much coarser results
        //typeSim.getSimilarityFunctionsForSets().put(ColumnDataType.string, new IdentitySimilarity<>());
        //typeSim.getSetSimilarities().put(ColumnDataType.string, new GeneralisedJaccard<>());

        typeSim.getSimilarityFunctions().put(ColumnDataType.numeric, getNumericSimilarityFunction());
        typeSim.getSetSimilarities().put(ColumnDataType.numeric, getNumericSetSimilarity());

        typeSim.getSimilarityFunctions().put(ColumnDataType.date, getDateSimilarityFunction());
        typeSim.getSetSimilarities().put(ColumnDataType.date, getDateSetSimilarity());

        //typeSim.getSignatureFilters().put(ColumnDataType.string, new JaccardPrefixFiltering<>());
        typeSim.getSignatureFilters().put(ColumnDataType.string, getStringFilter());
        
        instanceMatching.setSimilarityMeasure(typeSim);
        //instanceMatching.setRunInParallel(false);
        //instanceMatching.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
        
        // run matching
        Timer tm = Timer.getNamed("ValueBasedMatcher", t);
        SimilarityMatrix<TableCell> instanceSimilarity = instanceMatching
                .match(data.getWebtableRowSet(), data.getDbpediaRowSet(),
                        new TableRowToCellHierarchyAdapter(),
                        new TableCellMatchingAdapter());
        tm.stop();
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println(instanceMatching.getMatchingLog().toString());
            System.out.println("done.");
        }

        // add the initial scores from fastjoin to have some edit-distance based
        // similarity, but still emphasize exact token matches
        tm = Timer.getNamed("Similarity post-processing", t);
        for (TableRow row : data.getWebtableRowSet()) {

            for (TableRow match : getCandidateSimilarity().getMatches(row)) {

                TableCell rowKey = row.getKeyCell();
                TableCell matchKey = match.getKeyCell();
                Double d = instanceSimilarity.get(rowKey, matchKey);

                if (d == null) {
                    d = 0.0;
                }

                Double d2 = getCandidateSimilarity()
                        .get(row, match);

                if (d2 == null) {
                    d2 = 0.0;
                }

                d = (getKeyWeight() * d + (1-getKeyWeight())*d2);

                if (d > 0.0) {
                    instanceSimilarity.set(row.getKeyCell(),match.getKeyCell(), d);
                }
            }
        }
        tm.stop();

        
        //TODO remove after test:
        // set the value similarities of all webtable columns with kurtosis < 3 to null (should remove mapping of rank columns to runtime)
        for(TableColumn c : data.getWebtable().getColumns()) {
            
            if(c.getDataType()==ColumnDataType.numeric && c.getColumnStatistic().getKurtosis()<3) {
                
                for(TableCell cell : new TableColumnToCellHierarchyAdapter().getParts(c)) {
                    
                    for(TableCell match : instanceSimilarity.getMatches(cell)) {
                        
                        instanceSimilarity.set(cell, match, 0.0);
                        
                    }
                    
                }
                
            }
            
        }
        
        // instanceSimilarity.normalize();

        // instanceSimilarity.prune(0.30);
        instanceSimilarity.prune(getSimilarityThreshold());

        //getSimilarities().setValueSimilarity(instanceSimilarity);
        if (getMatchingParameters().isCollectMatchingInfo()) {
            instanceSimilarity.printStatistics("Instance similarities");

            // System.out.println(instanceSimilarity.getOutput(null,
            // getInstanceGoldStandard().values(), null, new
            // TableRowUriMatchingAdapter()));
            // printMatchingInfo(instanceMatching.getNonZeroPairs());
        }
        t.stop();
        
        return instanceSimilarity;
    }
}
