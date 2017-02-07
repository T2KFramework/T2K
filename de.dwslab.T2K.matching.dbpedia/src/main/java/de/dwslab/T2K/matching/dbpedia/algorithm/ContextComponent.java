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
package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.dbpedia.matchers.schema.ContextBasedClassMatcher;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TableColumnNameAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TableContentAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TableContextAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TablePageTitleAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TableTitleAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.TableURLAdapter;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author domi
 */
public class ContextComponent extends WebtableToDBpediaMatchingComponent {

    public static final Parameter PAR_PAGE_TITLE_WEIGHT = new Parameter("Context.PageTitleWeight", 0.5);
    public static final Parameter PAR_CONTEXT_WEIGHT = new Parameter("Context.ContextWeight", 0.5);
    public static final Parameter PAR_COLUMN_NAME_WEIGHT = new Parameter("Context.ColumnNameWeight", 0.5);
    public static final Parameter PAR_TITLE_WEIGHT = new Parameter("Context.TitleWeight", 0.5);
    public static final Parameter PAR_CONTENT_WEIGHT = new Parameter("Context.ContentWeight", 0.5);
    public static final Parameter PAR_URL_WEIGHT = new Parameter("Context.UrlWeight", 0.5);
    public static final Parameter PAR_CONTEXT_THRESHOLD = new Parameter("Context.ContextThreshold", 0.7);
    protected static final List<Parameter> params;

    public static List<Parameter> getParams() {
        return params;
    }

    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_PAGE_TITLE_WEIGHT);
        l.add(PAR_CONTEXT_WEIGHT);
        l.add(PAR_COLUMN_NAME_WEIGHT);
        l.add(PAR_TITLE_WEIGHT);
        l.add(PAR_CONTENT_WEIGHT);
        l.add(PAR_URL_WEIGHT);
        l.add(PAR_CONTEXT_THRESHOLD);
        params = l;
    }

    public ContextComponent() {
        setParameters(params);
    }
    private SimilarityMatrix<Table> pageTitleSimilarity;
    private SimilarityMatrix<Table> titleSimilarity;
    private SimilarityMatrix<Table> contexteSimilarity;
    private SimilarityMatrix<Table> columnNamesSimilarity;
    private SimilarityMatrix<Table> contentSimilarity;
    private SimilarityMatrix<Table> urlSimilarity;
    private SimilarityMatrix<Table> classContextMatrix;

    @Override
    public void run(Configuration config) {

        ContextBasedClassMatcher cbm = getMatchers().getContextMatcher();
        cbm.setThreshold((double) config.getValue(PAR_CONTEXT_THRESHOLD));

        cbm.setAdapter(new TablePageTitleAdapter());
        pageTitleSimilarity = cbm.match(getData());

        cbm.setAdapter(new TableTitleAdapter());
        titleSimilarity = cbm.match(getData());

        cbm.setAdapter(new TableContextAdapter());
        contexteSimilarity = cbm.match(getData());

        cbm.setAdapter(new TableContentAdapter());
        contentSimilarity = cbm.match(getData());

        cbm.setAdapter(new TableColumnNameAdapter());
        columnNamesSimilarity = cbm.match(getData());

        urlSimilarity = new SparseSimilarityMatrixFactory().createSimilarityMatrix(1, getData().getDbpediaTables().size());
        
        if( columnNamesSimilarity.getMatches(getData().getWebtable()).size()<3) {
            columnNamesSimilarity = new SparseSimilarityMatrixFactory().createSimilarityMatrix(0,0);
        }

//        if (contexteSimilarity.getMatches(getData().getWebtable()).size() < 3) {
//        for (Table dbpediaTable : getData().getDbpediaTables()) {
//            if (!dbpediaTable.isHasKey() || dbpediaTable.getKey().getValues().size() < 100) {
//                continue;
//            }
//            String wholeName = dbpediaTable.getHeader().replace(".csv.gz", "");
//            String[] name = StringUtils.splitByCharacterTypeCamelCase(dbpediaTable.getHeader().replace(".csv.gz", ""));
//            List<String> label = (ArrayList) new TableContextAdapter().getTokens(getData().getWebtable());
//            List<String> labelReplace = new ArrayList<>();
//            for (String s : label) {
//                s = s.toLowerCase().replace("y", "ies");
//                if (s.endsWith("s")) {
//                    s = s.substring(0, s.length() - 1);
//                }
//                labelReplace.add(s);
//            }
//
//            for (String s : name) {
//                s = s.toLowerCase().replace("y", "ies");
//                if (s.endsWith("s")) {
//                    s = s.substring(0, s.length() - 1);
//                }
//                if (StopWordRemover.isStopWord(s.toLowerCase()) || s.equals("-")) {
//                    continue;
//                }
//                if (label.contains(s) && !wholeName.contains("_")) {
//                    System.out.println(s.length() + " vs. " + labelReplace.size() + " res: " + (double) s.length() / (double) labelReplace.size());
//                    contexteSimilarity.set(getData().getWebtable(), dbpediaTable, (double) s.length() / (double) labelReplace.size());
//                    System.out.println("COUNTER context " + s);
//                }
//            }
//        }
//          }

        for (Table dbpediaTable : getData().getDbpediaTables()) {
            if (!dbpediaTable.isHasKey() || dbpediaTable.getKey().getValues().size() < 100) {
                continue;
            }
            String wholeName = dbpediaTable.getHeader().replace(".csv.gz", "");
            String[] name = StringUtils.splitByCharacterTypeCamelCase(dbpediaTable.getHeader().replace(".csv.gz", ""));
            String label = new TableURLAdapter().getTokens(getData().getWebtable()).toString().toLowerCase().replace("ies", "y");
            for (String s : name) {
                s = s.toLowerCase().replace("ies", "y");
                //check with game vs. games
                if (s.endsWith("s")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (StopWordRemover.isStopWord(s.toLowerCase()) || s.equals("-")) {
                    continue;
                }
                if (label.contains(s) && !wholeName.contains("_")) {
                    System.out.println(s.length() + " vs. " + label.length() + " res: " + (double) s.length() / (double) label.length());
                    urlSimilarity.set(getData().getWebtable(), dbpediaTable, (double) s.length() / (double) label.length());
                    System.out.println("COUNTER " + s);
                }
            }
        }

        if (pageTitleSimilarity.getMatches(getData().getWebtable()).size() < 3) {
            for (Table dbpediaTable : getData().getDbpediaTables()) {
                if (!dbpediaTable.isHasKey() || dbpediaTable.getKey().getValues().size() < 100) {
                    continue;
                }
                String wholeName = dbpediaTable.getHeader().replace(".csv.gz", "");
                String[] name = StringUtils.splitByCharacterTypeCamelCase(dbpediaTable.getHeader().replace(".csv.gz", ""));
                if (name.length > 1) {
                    continue;
                }
                List<String> label = (ArrayList) new TablePageTitleAdapter().getTokens(getData().getWebtable());
                List<String> labelReplace = new ArrayList<>();
                for (String s : label) {
                    s = s.toLowerCase().replace("ies", "y");
                    if (s.endsWith("s")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    labelReplace.add(s);
                }

                for (String s : name) {
                    if (StopWordRemover.isStopWord(s.toLowerCase()) || s.equals("-")) {
                        continue;
                    }
                    if (labelReplace.contains(s.toLowerCase().replace("ies", "y")) && !wholeName.contains("_")) {
                        pageTitleSimilarity.set(getData().getWebtable(), dbpediaTable, (double) s.length() / (double) labelReplace.size());
                    }
                }
            }
        }

//        for (Table dbpediaTable : getData().getDbpediaTables()) {
//            if(!dbpediaTable.isHasKey() || dbpediaTable.getKey().getValues().size()<100) {
//                continue;
//            }
//            String wholeName = dbpediaTable.getHeader().replace(".csv.gz", "");
//            String[] name = StringUtils.splitByCharacterTypeCamelCase(dbpediaTable.getHeader().replace(".csv.gz", ""));
//            List<String> labelfromAdapter = (ArrayList<String>) new TableContextAdapter().getTokens(getData().getWebtable());
//            List<String> label = new ArrayList<>();
//            for(String s : labelfromAdapter) {
//                label.add(s.toLowerCase());
//            }            
//            for (String s : name) {
//                if(StopWordRemover.isStopWord(s)) {
//                    continue;
//                }
//                if (label.contains(s.toLowerCase().replace("y", "ies")) && !wholeName.contains("_")) {
//                    contexteSimilarity.set(getData().getWebtable(), dbpediaTable, (double) s.length() / (double) label.size());
//                }
//            }
//        }

        CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
        nonOverlap.setAggregationType(CombinationType.WeightedSum);
        nonOverlap.setFirstWeight((double) config.getValue(PAR_CONTEXT_WEIGHT));
        nonOverlap.setSecondWeight((double) config.getValue(PAR_PAGE_TITLE_WEIGHT));
        SimilarityMatrix<Table> out1;
        if (contexteSimilarity == null && pageTitleSimilarity == null) {
            out1 = null;
        } else if (contexteSimilarity == null) {
            out1 = pageTitleSimilarity;
        } else if (pageTitleSimilarity == null) {
            out1 = contexteSimilarity;
        } else {
            out1 = nonOverlap.match(contexteSimilarity, pageTitleSimilarity);
        }

        nonOverlap.setFirstWeight((double) config.getValue(PAR_COLUMN_NAME_WEIGHT));
        nonOverlap.setSecondWeight((double) config.getValue(PAR_CONTENT_WEIGHT));
        SimilarityMatrix<Table> out2;
        if (columnNamesSimilarity == null && contentSimilarity == null) {
            out2 = null;
        } else if (columnNamesSimilarity == null) {
            out2 = columnNamesSimilarity;
        } else if (contentSimilarity == null) {
            out2 = columnNamesSimilarity;
        } else {
            out2 = nonOverlap.match(columnNamesSimilarity, contentSimilarity);
        }

        nonOverlap.setFirstWeight((double) config.getValue(PAR_PAGE_TITLE_WEIGHT));
        nonOverlap.setSecondWeight((double) config.getValue(PAR_URL_WEIGHT));
        SimilarityMatrix<Table> out3;
        if (titleSimilarity == null && urlSimilarity == null) {
            out3 = null;
        } else if (titleSimilarity == null) {
            out3 = urlSimilarity;
        } else if (urlSimilarity == null) {
            out3 = titleSimilarity;
        } else {
            out3 = nonOverlap.match(titleSimilarity, urlSimilarity);
        }

        nonOverlap.setAggregationType(CombinationType.WeightedSum);
        nonOverlap.setFirstWeight(0.3);
        nonOverlap.setSecondWeight(0.7);
        SimilarityMatrix<Table> first;
        if (out1 == null && out2 == null) {
            first = null;
        } else if (out1 == null) {
            first = out2;
        } else if (out2 == null) {
            first = out1;
        } else {
            first = nonOverlap.match(out1, out2);
        }

        SimilarityMatrix<Table> second;
        if (first == null && out3 == null) {
            second = null;
        } else if (first == null) {
            second = out3;
        } else if (out3 == null) {
            second = first;
        } else {
            second = nonOverlap.match(first, out3);
        }

        if (second != null) {
            second.normalize();
        }

        for (Table t : second.getFirstDimension()) {
            System.out.println("t: " + t.getHeader());
            for (Table r : second.getMatches(t)) {
                System.out.println("r: " + r.getHeader() + " -- " + second.get(t, r));
            }
        }
        setClassContextMatrix(second);
    }

    /**
     * @return the pageTitleSimilarity
     */
    public SimilarityMatrix<Table> getPageTitleSimilarity() {
        return pageTitleSimilarity;
    }

    /**
     * @param pageTitleSimilarity the pageTitleSimilarity to set
     */
    public void setPageTitleSimilarity(SimilarityMatrix<Table> pageTitleSimilarity) {
        this.pageTitleSimilarity = pageTitleSimilarity;
    }

    /**
     * @return the titleSimilarity
     */
    public SimilarityMatrix<Table> getTitleSimilarity() {
        return titleSimilarity;
    }

    /**
     * @return the contexteSimilarity
     */
    public SimilarityMatrix<Table> getContexteSimilarity() {
        return contexteSimilarity;
    }

    /**
     * @return the columnNamesSimilarity
     */
    public SimilarityMatrix<Table> getColumnNamesSimilarity() {
        return columnNamesSimilarity;
    }

    /**
     * @return the contentSimilarity
     */
    public SimilarityMatrix<Table> getContentSimilarity() {
        return contentSimilarity;
    }

    /**
     * @return the urlSimilarity
     */
    public SimilarityMatrix<Table> getUrlSimilarity() {
        return urlSimilarity;
    }

    /**
     * @return the classContextMatrix
     */
    public SimilarityMatrix<Table> getClassContextMatrix() {
        return classContextMatrix;
    }

    /**
     * @param classContextMatrix the classContextMatrix to set
     */
    public void setClassContextMatrix(SimilarityMatrix<Table> classContextMatrix) {
        this.classContextMatrix = classContextMatrix;
    }
}
