
package de.dwslab.T2K.matching.experiments;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.utils.io.CSVUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author domi
 */
public class ExternalEvaluation {

    public static class NoChangeAdapter extends EvaluationAdapter<String> {

        @Override
        public Object getUniqueIdentifier(String instance) {
            return instance;
        }
    }

    public static class DBpediaNoChangeAdapter extends EvaluationAdapter<String> {

        private Canoniser canon;

        public DBpediaNoChangeAdapter(Canoniser canoniser) {
            canon = canoniser;
        }

        @Override
        public Object getUniqueIdentifier(String instance) {
            return canon.canoniseResource(instance);
        }

    }

    private static Map<String, String> propertyGoldStandard;
    private static Collection<Correspondence<String>> propertyMappings;
    private static final Canoniser equivPropertyCanoniser = new Canoniser();

    public static void main(String[] args) {
        propertyGoldStandard = new HashMap();
//        equivPropertyCanoniser.loadEquivalentResources(args[2]);        
//        fillGS(args[0]);
//        fillMappings(args[1]);
//        countColumns(args[3]);
        equivPropertyCanoniser.loadEquivalentResources("C:\\Users\\domi\\WebTables\\MarckovLogic\\equiv.tsv");
        fillGS("C:\\Users\\domi\\WebTables\\MarckovLogic\\attributes_5");

        //fillMappings("C:\\Users\\domi\\Downloads\\mapping-x.rdf");
        //fillMappings("C:\\Users\\domi\\WebTables\\MarckovLogic\\mappings_domi.tsv");
        propertyMappings = new HashSet<>();
        //fillMappings("C:\\Users\\domi\\WebTables\\MarckovLogic\\JointMatching\\maps_highRecallEval.tsv");
        
        //fillMappings("C:\\Users\\domi\\WebTables\\MarckovLogic\\JointMatching\\maps_highRecallChristian.tsv");
        
        //only evaluate a 1:1 mapping
        //forceOneToOne();
        //evaluateProperties();

        //File input = new File("C:\\Users\\domi\\WebTables\\MarckovLogic\\JointMatching\\outMappings1To1");
        File input = new File("C:\\Users\\domi\\WebTables\\MarckovLogic\\JointMatching\\mappingsChristian");
        for(File f : input.listFiles()) {
            System.out.println(f.getName());
            propertyMappings = new HashSet<>();
            fillMappings(f.getAbsolutePath());
            if(f.getName().contains("Eval")) {
                continue;
            }
            evaluateProperties();
        }
    }

    public static void forceOneToOne() {
        Map<String, Correspondence> highestValues = new HashMap<>();
        for (Correspondence c : propertyMappings) {
            if (highestValues.containsKey(c.getFirst().toString())) {
                if (c.getSimilarity() > highestValues.get(c.getFirst().toString()).getSimilarity()) {
                    highestValues.put(c.getFirst().toString(), c);
                }
            } else {
                highestValues.put(c.getFirst().toString(), c);
            }
        }
        Set<Correspondence> toDelete = new HashSet<>();
        for (Correspondence s : propertyMappings) {
            if (!highestValues.values().contains(s)) {
                toDelete.add(s);
            }
        }
        for (Correspondence c : toDelete) {
            propertyMappings.remove(c);
        }
    }

    public static EvaluationResult evaluateProperties() {
        EvaluationAdapter<String> evalRow = new NoChangeAdapter();
        EvaluationAdapter<String> evalInstance = new DBpediaNoChangeAdapter(equivPropertyCanoniser);

        Map<Object, List<Object>> tmpList = new HashMap<>();

        for (String o : propertyGoldStandard.keySet()) {
            List<Object> tmp = new ArrayList<>();
            if (propertyGoldStandard.get(o).contains("rdf-syntax-ns#type") || o.contains("rdf-syntax-ns#type")
                    || propertyGoldStandard.get(o).contains("rdf-syntax-ns#label") || o.toString().contains("rdf-syntax-ns#label")
                    || o.toString().contains("isPreferredMeaningOf") || o.toString().contains("core#prefLabel") || o.contains("owl#sameAs")
                    || propertyGoldStandard.get(o).contains("owl#sameAs") || propertyGoldStandard.get(o).equals("URI")
                    || o.equals("URI") || o.contains("thumbnail") || propertyGoldStandard.get(o).contains("thumbnail")
                    || o.contains("rdf-schema#label") || propertyGoldStandard.get(o).toString().contains("rdf-schema#label")
                    || o.toString().contains("longName") || propertyGoldStandard.get(o).toString().contains("longName")
                    || o.toString().contains("dbpedia.org/ontology/type") || propertyGoldStandard.get(o).toString().contains("dbpedia.org/ontology/type")
                    ) {
                continue;
            }
            tmp.add(propertyGoldStandard.get(o));
            tmpList.put(o, tmp);
        }
        Collection<Correspondence<String>> newPropMappings = new HashSet<>();

        for (Correspondence c : propertyMappings) {
            //System.out.println("c: " + c.getFirst() + " --- " + c.getSecond());
            if (c.getSecond().toString().contains("rdf-syntax-ns#type") || c.getFirst().toString().contains("rdf-syntax-ns#type")
                    || c.getFirst().toString().contains("isPreferredMeaningOf")
                    || c.getFirst().toString().contains("core#prefLabel")
                    || c.getFirst().toString().contains("owl#sameAs")
                    || c.getSecond().toString().contains("owl#sameAs")
                    || c.getFirst().toString().equals("URI")
                    || c.getSecond().toString().equals("URI") || c.getFirst().toString().contains("rdf-schema#comment")
                    || c.getSecond().toString().contains("rdf-schema#comment")
                    || c.getFirst().toString().contains("thumbnail") || c.getSecond().toString().contains("thumbnail")
                    || c.getFirst().toString().contains("rdf-schema#label") || c.getSecond().toString().contains("rdf-schema#label")
                    || c.getFirst().toString().contains("longName") || c.getSecond().toString().contains("longName")
                    || c.getFirst().toString().contains("dbpedia.org/ontology/type") || c.getSecond().toString().contains("dbpedia.org/ontology/type")
                    || c.getFirst().toString().contains("rdf-syntax-ns#label") || c.getSecond().toString().contains("rdf-syntax-ns#label")
                    || c.getFirst().toString().contains("purl.org/dc/elements/1.1/description") || c.getSecond().toString().contains("purl.org/dc/elements/1.1/description")) {
                continue;
            }
            newPropMappings.add(c);
        }

        EvaluationResult results = evaluateMatching(newPropMappings, tmpList, 0, evalRow, evalInstance);

        System.out.println("Property prec: " + results.getPrecision()
                + " rec: " + results.getRecall() + " f-mea: "
                + results.getF1Score());

        System.out.println(" corr: " + results.getCorrect() + " --" + results.getInputSetSize() + " ---" + results.getReferenceSetSize() + " --- " + propertyGoldStandard.size());
        
        return results;
    }

    public static <String> EvaluationResult evaluateMatching(
            Collection<Correspondence<String>> correspondences,
            Map<Object, List<Object>> goldStandard,
            int totalInstances,
            EvaluationAdapter<String> firstAdapter,
            EvaluationAdapter<String> secondAdapter) {

        // iterate all correspondences and count
        int correct = 0;
        int matches = correspondences.size();
        int total = goldStandard.keySet().size();
        HashSet<Object> mapped = new HashSet();

        for (Correspondence<String> cor : correspondences) {
            List<Object> correctIds = goldStandard.get(firstAdapter.getUniqueIdentifier(cor.getFirst()));

            Object actualId = null;

            mapped.add(firstAdapter.getUniqueIdentifier(cor.getFirst()));

            if (cor.getSecond() != null) {
                actualId = secondAdapter.getUniqueIdentifier(cor.getSecond());
            }

            if (correctIds == null && actualId == null
                    || (correctIds != null && actualId != null && correctIds.contains(actualId))) {
                correct++;

                cor.setCorrect(true);
            } else {
                cor.setCorrect(false);
                System.out.println(cor.getFirst() + " ---- " + cor.getSecond());
            }
            cor.setCorrectValue(correctIds);
        }

        return new EvaluationResult(correct, matches, total, totalInstances);
    }

//    private static void countColumns(String tableString) {
//        File path = new File(tableString);
//        for (File f : path.listFiles()) {
//            Collection<String[]> corres = CSVUtils.readCSV(f.getAbsolutePath());
//            allCols += corres.iterator().next().length;
//        }
//    }
    private static void fillMappings(String mappingString) {
        File mappings = new File(mappingString);
        Collection<String[]> corres = CSVUtils.readCSV(mappings.getAbsolutePath(), "\t");
        for (String[] s : corres) {
            String indexWithName = s[0];
            String dbpedia = equivPropertyCanoniser.canoniseResource(s[1]);
            Correspondence c;
            if (s.length > 2) {
                c = new Correspondence(indexWithName, dbpedia, Double.parseDouble(s[2]));
            } else {
                c = new Correspondence(indexWithName, dbpedia, 1.0);
            }
            propertyMappings.add(c);
        }
    }

    private static void fillGS(String pathString) {
        File path = new File(pathString);
        try {
            for (File f : path.listFiles()) {
                if (f.exists()) {
                    Collection<String[]> corres = CSVUtils.readCSV(f.getAbsolutePath());

                    // handle equivalent properties
                    for (String[] s : corres) {
                        String uri = equivPropertyCanoniser.canoniseResource(s[0]);
                        if (s.length > 2) {
                            boolean key = Boolean.parseBoolean(s[2]);
                            int index = Integer.parseInt(s[3]);
                            String indexWithTable = f.getName() + "-" + index;
                            if (key != true) {
                                propertyGoldStandard.put(indexWithTable, uri);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("warning! goldstandard missing " + path);
        }
    }

//    private static void computeMaxRecall() {
//        for(Correspondence c : propertyMappings) {
//            if()
//        }
//    }
}
