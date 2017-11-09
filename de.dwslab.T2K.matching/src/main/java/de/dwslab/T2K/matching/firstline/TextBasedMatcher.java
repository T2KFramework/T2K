package de.dwslab.T2K.matching.firstline;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author domi
 */
public class TextBasedMatcher<T> extends FirstLineMatcher<T> {

    private StringBuilder matchingLog;

    private Map<String, Map<String, Double>> vectors = new HashMap();
    private Map<String, Integer> documentTermCount = new HashMap();
    private MatchingAdapter<T> uriAdapter;
    private double threshold;

    public StringBuilder getMatchingLog() {
        return matchingLog;
    }

    public SimilarityMatrix<T> match(Collection<T> instancesToMatch,
            final Collection<T> candidates, final MatchingAdapter<T> adapter) throws Exception {

        matchingLog = new StringBuilder();

        final SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(instancesToMatch.size(), candidates.size());

        try {
            new Parallel<T>(isRunInParallel() ? 0 : 1).foreach(instancesToMatch, new Consumer<T>() {
                public void execute(T parameter) {
                    Collection<T> blockedCandidates = null;

                    Timer t = null;

                    if (getParentTimer() != null) {
                        t = Timer.getNamed("TextBasedMatcher", getParentTimer(), true);
                    }
                    if (getBlocking() != null) {
                        blockedCandidates = getBlocking().getCandidates(parameter, candidates);
                    } else {
                        blockedCandidates = candidates;
                    }
                    if (t != null) {
                        t.stop();
                    }
                    Collection<Pair<T, T>> filtered = new ArrayList<Pair<T, T>>();

                    for (T o : blockedCandidates) {
                        filtered.add(new Pair<>(parameter, o));
                    }
                    Map<T, Double> scores = new HashMap<>();
                    //compute the vector for the row
                    List<String> tokens1 = (List<String>) adapter.getTokens(parameter);
                    if (tokens1 != null && !tokens1.isEmpty()) {

                        String[] tokenArray = new String[tokens1.size()];
                        tokenArray = tokens1.toArray(tokenArray);
                        double[] tableTFIDF = tfIdfCalculator(documentTermCount, tokenArray, getVectors().size());

                        // iterate over all candidates (all instances that could be matched to the current instance)
                        for (Pair<T, T> p : filtered) {
                            // calculate similarity
                            
                            //System.out.println(p.getSecond().toString());
                            Map<String, Double> termsInIdent;
                            if (uriAdapter == null) {
                                termsInIdent = getVectors().get(p.getSecond().toString());
                            } else {
                                termsInIdent = getVectors().get(uriAdapter.getLabel(p.getSecond()).toString());
                            }
                            if (termsInIdent != null) {
                                double[] tfIdfDBpedia = new double[tokenArray.length];

                            //String[] tokenArrayDBpedia = new String[tokenArray.length];
                                try {
                                    for (int i = 0; i < tokenArray.length; i++) {
                                        if (termsInIdent.containsKey(tokenArray[i])) {
                                            //tokenArrayDBpedia[i] = tokenArray[i];
                                            tfIdfDBpedia[i] = termsInIdent.get(tokenArray[i]);
                                        } else {
                                            //tokenArrayDBpedia[i] = null;
                                            tfIdfDBpedia[i] = 0;
                                        }
                                    }
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                                //tfIdfDBpedia = tfIdfCalculator(documentTermCount, tokenArrayDBpedia);
//                        Double[] tableTFIDFObject = ArrayUtils.toObject(tableTFIDF);
//                        Double[] dbpediaTFIDFObject = ArrayUtils.toObject(tfIdfDBpedia);
                                Double similarity = getSimilarityMeasure().calculate(tableTFIDF, tfIdfDBpedia, adapter, null);
//                                if (t != null) {
//                                    t.stop();
//                                }
                                // only add if greater than zero
                                if (similarity != null && similarity > threshold) {
                                    
                                    scores.put(p.getSecond(), similarity);
                                }
                            }
                        }
                        LinkedHashMap<T, Double> sortedMap = sortByValue(scores);
                        synchronized (sim) {
                            int counter = 0;
                            for (Entry<T, Double> candidate : sortedMap.entrySet()) {
                                counter++;
                                sim.set(parameter, candidate.getKey(), sortedMap.get(candidate.getKey()));
                                System.out.println("adapter: " + adapter.getClass().getName() + " para: " + parameter + " cand " + candidate.getKey() + " value " +sortedMap.get(candidate.getKey()));
                                if (counter > 10) {
                                    break;
                                }
                            }
                        }
                    }
                }

            }, "TextBasedMatcher");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sim;
    }

    private LinkedHashMap sortByValue(Map<T, Double> map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                double v1 = (Double) ((Map.Entry) (o1)).getValue();
                double v2 = (Double) ((Map.Entry) (o2)).getValue();
                if (v1 != v2) {
                    return -Double.compare(v1, v2);
                } else {
                    return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
                }
            }
        });

        LinkedHashMap result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static double idfCalculator(Set<String> allTerms, String termToCheck) {
        double count = 0;
        for (String s : allTerms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
                break;
            }
        }
        return 1 + Math.log(allTerms.size() / count);
    }

    public static double[] tfIdfCalculator(Map<String, Integer> allTerms, String[] totalterms, int numberAllDocs) {
        double tf; //term frequency
        double idf = 0; //inverse document frequency
        double tfidf; //term requency inverse document frequency      
        double[] tfidfvector = new double[totalterms.length];
        int count = 0;
        for (String terms : totalterms) {
            if (terms == null) {
                tfidfvector[count] = 0.0;
                count++;
                continue;
            }
            tf = tfCalculator(totalterms, terms);
            if (allTerms.containsKey(terms)) {
                int docuCount = allTerms.get(terms);
                idf = 1 + Math.log(numberAllDocs / docuCount);
            }
            tfidf = tf * idf;
            tfidfvector[count] = tfidf;
            count++;
        }
        
        return tfidfvector;
    }

    public static double tfCalculator(String[] totalterms, String termToCheck) {
        double count = 0;  //to count the overall occurrence of the term termToCheck
        for (String s : totalterms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        return count / (double) totalterms.length;
    }

    /**
     * @return the vectors
     */
    public Map<String, Map<String, Double>> getVectors() {
        return vectors;
    }

    /**
     * @param vectors the vectors to set
     */
    public void setVectors(Map<String, Map<String, Double>> vectors) {
        this.vectors = vectors;
    }

    /**
     * @return the documentTermCount
     */
    public Map<String, Integer> getDocumentTermCount() {
        return documentTermCount;
    }

    /**
     * @param documentTermCount the documentTermCount to set
     */
    public void setDocumentTermCount(Map<String, Integer> documentTermCount) {
        this.documentTermCount = documentTermCount;
    }

    /**
     * @return the uriAdapter
     */
    public MatchingAdapter<T> getUriAdapter() {
        return uriAdapter;
    }

    /**
     * @param uriAdapter the uriAdapter to set
     */
    public void setUriAdapter(MatchingAdapter<T> uriAdapter) {
        this.uriAdapter = uriAdapter;
    }

    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
