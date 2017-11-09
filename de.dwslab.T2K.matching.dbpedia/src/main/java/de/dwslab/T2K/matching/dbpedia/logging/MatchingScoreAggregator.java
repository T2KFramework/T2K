package de.dwslab.T2K.matching.dbpedia.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.dbpedia.model.MatchingEvaluation;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.utils.data.ValueAggregator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MatchingScoreAggregator {

    public static void printResults(Collection<MatchingResult> results, Collection<HashMap<String, HashMap<String, EvaluationResult>>> steps) throws IOException {
        CSVWriter instances = null;
        //instances = new CSVWriter(new FileWriter("instances.csv"));
        instances = new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File("instances.csv")), "UTF-8"));
        CSVWriter properties = null;
        //properties = new CSVWriter(new FileWriter("properties.csv"));
        properties = new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File("properties.csv")), "UTF-8"));
        boolean isFirst = true;
        CSVWriter propertyRanges = null;
        propertyRanges = new CSVWriter(new FileWriter("propertyRanges.csv"));

        CSVWriter performance = new CSVWriter(new FileWriter("performance.csv"));
        String[] perfHeaders = new String[]{
            "table",
            "instance baseline precision",
            "instance baseline recall",
            "instance baseline f1",
            "instance precision",
            "instance recall",
            "instance f1",
            "property precision",
            "property recall",
            "property f1",
            "class precision",
            "class recall",
            "class f1",
            "property range precision",
            "property range recall",
            "property range f1",
            "#property mappings",
            "#property ranges GS",
            "#instance mappings",
            "#property ranges",
            "#instance mappings correct",
            "#instances",
            "#instance mappings in gs",
            "#property mappings correct",
            "#properties",
            "#property mappings in gs",
            "correct key?",
            "class"    
        };
        performance.writeNext(perfHeaders);

        ValueAggregator vaInstP = new ValueAggregator();
        ValueAggregator vaInstR = new ValueAggregator();
        ValueAggregator vaInstF1 = new ValueAggregator();
        ValueAggregator vaInstAcc = new ValueAggregator();

        ValueAggregator vaPropP = new ValueAggregator();
        ValueAggregator vaPropR = new ValueAggregator();
        ValueAggregator vaPropF1 = new ValueAggregator();
        ValueAggregator vaPropAcc = new ValueAggregator();

        ValueAggregator vaClassP = new ValueAggregator();
        ValueAggregator vaClassR = new ValueAggregator();
        ValueAggregator vaClassF1 = new ValueAggregator();
        ValueAggregator vaClassAcc = new ValueAggregator();

        ValueAggregator vaPropRP = new ValueAggregator();
        ValueAggregator vaPropRR = new ValueAggregator();
        ValueAggregator vaPropR1 = new ValueAggregator();
        ValueAggregator vaPropRAcc = new ValueAggregator();

        ValueAggregator vaInstBlP = new ValueAggregator();
        ValueAggregator vaInstBlR = new ValueAggregator();
        ValueAggregator vaInstBlF1 = new ValueAggregator();
        ValueAggregator vaInstBlAcc = new ValueAggregator();

        ValueAggregator vaInstMaxP = new ValueAggregator();
        ValueAggregator vaInstMaxR = new ValueAggregator();
        ValueAggregator vaInstMaxF1 = new ValueAggregator();
        ValueAggregator vaInstMaxAcc = new ValueAggregator();

        ValueAggregator vaKey = new ValueAggregator();
//
//        
//        EvaluationResult resInst = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resProp = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resClass = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resMax = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resPropRange = new EvaluationResult(0, 0, 0, 0);
//        
        for (MatchingResult r : results) {
            if (isFirst) {
                properties.writeNext(r.getPropertiesCSVHeader());
                isFirst = false;
            }
            if(r.getWebtable() == null) {
                continue;
            }
            MatchingEvaluation me = r.getEvaluation();
//            me.merge(me.getInstanceResult());
//            resProp.merge(me.getPropertyResult());
            if (me.getInstanceResult() == null || me.getPropertyResult() == null) {
                System.out.println(r.getWebtable().getHeader());
                continue;
            }

            vaInstP.AddValue(me.getInstanceResult().getCorrect());
            vaInstR.AddValue(me.getInstanceResult().getInputSetSize());
            vaInstF1.AddValue(me.getInstanceResult().getReferenceSetSize());
            vaInstAcc.AddValue(me.getInstanceResult().getTotalPopulationSize());

            boolean noProp = false;
            if (me.getPropertyResult() != null) {
                vaPropP.AddValue(me.getPropertyResult().getCorrect());
                vaPropR.AddValue(me.getPropertyResult().getInputSetSize());
                vaPropF1.AddValue(me.getPropertyResult().getReferenceSetSize());
                vaPropAcc.AddValue(me.getPropertyResult().getTotalPopulationSize());
            } else {
                noProp = true;
            }

            vaClassP.AddValue(me.getClassResult().getCorrect());
            vaClassR.AddValue(me.getClassResult().getInputSetSize());
            vaClassF1.AddValue(me.getClassResult().getReferenceSetSize());
            vaClassAcc.AddValue(me.getClassResult().getTotalPopulationSize());
            boolean noBaseline = false;
            if (me.getInstanceBaseLine() != null) {
                vaInstBlP.AddValue(me.getInstanceBaseLine().getCorrect());
                vaInstBlR.AddValue(me.getInstanceBaseLine().getInputSetSize());
                vaInstBlF1.AddValue(me.getInstanceBaseLine().getReferenceSetSize());
                vaInstBlAcc.AddValue(me.getInstanceBaseLine().getTotalPopulationSize());
            } else {
                noBaseline = true;
            }
//            vaInstMaxP.AddValue(me.getInstanceMax().getCorrect());
//            vaInstMaxR.AddValue(me.getInstanceMax().getInputSetSize());
//            vaInstMaxF1.AddValue(me.getInstanceMax().getReferenceSetSize());
//            vaInstMaxAcc.AddValue(me.getInstanceMax().getTotalPopulationSize());

            vaPropRP.AddValue(me.getPropertyRangeResult().getCorrect());
            vaPropRR.AddValue(me.getPropertyRangeResult().getInputSetSize());
            vaPropR1.AddValue(me.getPropertyRangeResult().getReferenceSetSize());
            vaPropRAcc.AddValue(me.getPropertyRangeResult().getTotalPopulationSize());

            vaKey.AddValue(me.getCorrectKey());

            //r.write();

            //was there
//            if (!noBaseline) {
//                for (String[] line : r.formatInstancesForCSV()) {
//                    instances.writeNext(line);
//                }
//            }
            if (!noProp) {
                for (String[] line : r.formatPropertiesForCSV()) {
                    properties.writeNext(line);
                }
            }
            for (String[] line : r.formatPropertyRangesForCSV()) {
                propertyRanges.writeNext(line);
            }


            String[] perf = new String[perfHeaders.length];
            int k = 0;
            perf[k++] = r.getWebtable().getHeader();
            if (!noBaseline) {
                perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getPrecision());
                perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getRecall());
                perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getF1Score());
            } else {
                perf[k++] = 0 + "";
                perf[k++] = 0 + "";
                perf[k++] = 0 + "";
            }
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getF1Score());
            if (!noProp) {
                perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getPrecision());
                perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getRecall());
                perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getF1Score());
            } else {
                perf[k++] = 0 + "";
                perf[k++] = 0 + "";
                perf[k++] = 0 + "";
            }
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getF1Score());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyRangeResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyRangeResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyRangeResult().getF1Score());
            perf[k++] = Double.toString(r.getPropertyMappings().size());
            perf[k++] = Double.toString(r.getGoldStandard().getPropertyRangeGoldStandard().size());
            perf[k++] = Double.toString(r.getInstanceMappings().size());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyRangeResult().getInputSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getCorrect());
            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getInputSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getReferenceSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getCorrect());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getInputSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getReferenceSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getCorrectKey());

            performance.writeNext(perf);
        }
        instances.flush();
        properties.flush();
        performance.flush();
        propertyRanges.flush();


//        System.err.println("Stepwise evaluation:");
//
//        if (steps != null && steps.size() > 0) {
//            for (String key : steps.iterator().next().keySet()) {

//                HashMap<String, ValueAggregator> p = new LinkedHashMap<String, ValueAggregator>();
//                HashMap<String, ValueAggregator> r = new LinkedHashMap<String, ValueAggregator>();
//                HashMap<String, ValueAggregator> f = new LinkedHashMap<String, ValueAggregator>();

                //stepwise eval!
//                HashMap<String, EvaluationResult> evalSteps = new LinkedHashMap<String, EvaluationResult>();
//                Set<String> iterations = new TreeSet<String>();
//
//                System.err.println("***" + key + "***");
//
//                for (HashMap<String, HashMap<String, EvaluationResult>> data : steps) {
//
//                    for (String iteration : data.get(key).keySet()) {
//
//                        if (!iterations.contains(iteration)) {
//                            iterations.add(iteration);
//                        }
//
//                        EvaluationResult res = data.get(key).get(iteration);
//
//                        EvaluationResult er = evalSteps.get(iteration);
//                        if (er == null) {
//                            er = new EvaluationResult(0, 0, 0, 0);
//                            evalSteps.put(iteration, er);
//                        }
//                        er.merge(res);
//                    }
//
//                }
//
//                StringBuilder sb = new StringBuilder();
//                sb.append(" ");
//                for (String it : iterations) {
//                    sb.append("\t");
//                    sb.append(it);
//                }
//                System.err.println(sb.toString());
//
//                sb.setLength(0);
//                sb.append("precision");
//                for (String it : iterations) {
//                    sb.append("\t");
//                    if (evalSteps.get(it) != null) {
//                        sb.append(evalSteps.get(it).getPrecision());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//
//                sb.setLength(0);
//                sb.append("recall   ");
//                for (String it : iterations) {
//                    sb.append("\t");
//                    if (evalSteps.get(it) != null) {
//                        sb.append(evalSteps.get(it).getRecall());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//
//                sb.setLength(0);
//                sb.append("f1       ");
//                for (String it : iterations) {
//                    sb.append("\t");
//                    if (evalSteps.get(it) != null) {
//                        sb.append(evalSteps.get(it).getF1Score());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//
//            }
//        }

        EvaluationResult m1 = null;
        if (vaInstBlP.getSum() == 0.0) {
            m1 = new EvaluationResult((int) vaInstBlP.getSum(), (int) vaInstBlR.getSum(), (int) vaInstBlF1.getSum(), (int) vaInstBlAcc.getSum());
        }
        EvaluationResult m2 = new EvaluationResult((int) vaInstP.getSum(), (int) vaInstR.getSum(), (int) vaInstF1.getSum(), (int) vaInstAcc.getSum());
        EvaluationResult m3 = new EvaluationResult((int) vaInstMaxP.getSum(), (int) vaInstMaxR.getSum(), (int) vaInstMaxF1.getSum(), (int) vaInstMaxAcc.getSum());
        EvaluationResult m4 = new EvaluationResult((int) vaPropP.getSum(), (int) vaPropR.getSum(), (int) vaPropF1.getSum(), (int) vaPropAcc.getSum());
        EvaluationResult m5 = new EvaluationResult((int) vaClassP.getSum(), (int) vaClassR.getSum(), (int) vaClassF1.getSum(), (int) vaClassAcc.getSum());
        EvaluationResult m6 = new EvaluationResult((int) vaPropRP.getSum(), (int) vaPropRR.getSum(), (int) vaPropR1.getSum(), (int) vaPropRAcc.getSum());

        System.err.println("Instance mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy\tCorrect\tfound Mappings\tReference");
        if (m1 != null) {
            System.err.println(String.format(
                    "baseline:\t     %.2f\t  %.2f\t  %.2f\t  %.2f",
                    m1.getPrecision(), m1.getPrecision(), m1.getF1Score(), m1.getAccuracy()));
        }
//        System.err.println(String.format(
//                "result:  \t     %.2f\t  %.2f\t  %.2f",
//                vaInstP.getAvg(), vaInstR.getAvg(), vaInstF1.getAvg()));
        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
                m2.getPrecision(), m2.getRecall(), m2.getF1Score(), m2.getAccuracy(), m2.getCorrect(), m2.getInputSetSize(), m2.getReferenceSetSize()));

        System.err.println(String.format(
                "max:     \t     %.2f\t  %.2f\t  %.2f\t  %.2f",
                m3.getPrecision(), m3.getRecall(), m3.getF1Score(), m3.getAccuracy()));

        System.err.println("\nProperty mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy");
        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
                m4.getPrecision(), m4.getRecall(), m4.getF1Score(), m4.getAccuracy(), m4.getCorrect(), m4.getInputSetSize(), m4.getReferenceSetSize()));

        System.err.println("\nClass mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy");
        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f %d\t %d\t %d",
                m5.getPrecision(), m5.getRecall(), m5.getF1Score(), m5.getAccuracy(), m5.getCorrect(), m5.getInputSetSize(), m5.getReferenceSetSize()));

        System.err.println("\nProperty Range mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy");
        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f",
                m6.getPrecision(), m6.getRecall(), m6.getF1Score(), m6.getAccuracy()));
        System.err.print("key detection correct: " + vaKey.getSum());

        instances.close();
        properties.close();
        performance.close();
        propertyRanges.close();
    }

    public static void printInstances(Collection<MatchingResult> results, Collection<HashMap<String, HashMap<String, EvaluationResult>>> steps) throws IOException {
        CSVWriter instances = null;
        //instances = new CSVWriter(new FileWriter("instances.csv"));
        instances = new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File("instances.csv")), "UTF-8"));
        CSVWriter performance = new CSVWriter(new FileWriter("performance_inst.csv"));
        String[] perfHeaders = new String[]{
            "table",
            "instance baseline precision",
            "instance baseline recall",
            "instance baseline f1",
            "instance precision",
            "instance recall",
            "instance f1",
            "#instance mappings",
            "#instance mappings correct",
            "#instances",
            "#instance mappings in gs",
            "correct key?"
        };
        performance.writeNext(perfHeaders);

        ValueAggregator vaInstP = new ValueAggregator();
        ValueAggregator vaInstR = new ValueAggregator();
        ValueAggregator vaInstF1 = new ValueAggregator();
        ValueAggregator vaInstAcc = new ValueAggregator();

        ValueAggregator vaInstBlP = new ValueAggregator();
        ValueAggregator vaInstBlR = new ValueAggregator();
        ValueAggregator vaInstBlF1 = new ValueAggregator();
        ValueAggregator vaInstBlAcc = new ValueAggregator();

        ValueAggregator vaInstMaxP = new ValueAggregator();
        ValueAggregator vaInstMaxR = new ValueAggregator();
        ValueAggregator vaInstMaxF1 = new ValueAggregator();
        ValueAggregator vaInstMaxAcc = new ValueAggregator();

        ValueAggregator vaKey = new ValueAggregator();
//
//        
//        EvaluationResult resInst = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resProp = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resClass = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resMax = new EvaluationResult(0, 0, 0, 0);
//        EvaluationResult resPropRange = new EvaluationResult(0, 0, 0, 0);
//        
        for (MatchingResult r : results) {
            MatchingEvaluation me = r.getEvaluation();

            if (me.getInstanceResult() == null) {
                continue;
            }

            vaInstP.AddValue(me.getInstanceResult().getCorrect());
            vaInstR.AddValue(me.getInstanceResult().getInputSetSize());
            vaInstF1.AddValue(me.getInstanceResult().getReferenceSetSize());
            vaInstAcc.AddValue(me.getInstanceResult().getTotalPopulationSize());

//            vaInstBlP.AddValue(me.getInstanceBaseLine().getCorrect());
//            vaInstBlR.AddValue(me.getInstanceBaseLine().getInputSetSize());
//            vaInstBlF1.AddValue(me.getInstanceBaseLine().getReferenceSetSize());
//            vaInstBlAcc.AddValue(me.getInstanceBaseLine().getTotalPopulationSize());
//            
//            vaInstMaxP.AddValue(me.getInstanceMax().getCorrect());
//            vaInstMaxR.AddValue(me.getInstanceMax().getInputSetSize());
//            vaInstMaxF1.AddValue(me.getInstanceMax().getReferenceSetSize());
//            vaInstMaxAcc.AddValue(me.getInstanceMax().getTotalPopulationSize());

            vaKey.AddValue(me.getCorrectKey());

            //r.write();

            for (String[] line : r.formatInstancesForCSV()) {
                instances.writeNext(line);
            }


            String[] perf = new String[perfHeaders.length];
            int k = 0;
            perf[k++] = r.getWebtable().getHeader();
//            perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getPrecision());
//            perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getRecall());
//            perf[k++] = Double.toString(r.getEvaluation().getInstanceBaseLine().getF1Score());
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getInstanceResult().getF1Score());
            perf[k++] = Double.toString(r.getInstanceMappings().size());
//            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getCorrect());
//            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getInputSetSize());
//            perf[k++] = Integer.toString(r.getEvaluation().getInstanceResult().getReferenceSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getCorrectKey());

            performance.writeNext(perf);
        }
        instances.flush();
        performance.flush();


        System.err.println("Stepwise evaluation:");

//        if(steps != null && steps.size()>0) {
//            for(String key : steps.iterator().next().keySet()) {
//                
////                HashMap<String, ValueAggregator> p = new LinkedHashMap<String, ValueAggregator>();
////                HashMap<String, ValueAggregator> r = new LinkedHashMap<String, ValueAggregator>();
////                HashMap<String, ValueAggregator> f = new LinkedHashMap<String, ValueAggregator>();
//                
//                HashMap<String, EvaluationResult> evalSteps = new LinkedHashMap<String, EvaluationResult>();
//                Set<String> iterations = new TreeSet<String>();
//                
//                System.err.println("***" + key + "***");
//                
//                for(HashMap<String, HashMap<String, EvaluationResult>> data : steps) {
//                    
//                    for(String iteration : data.get(key).keySet()) {
//                        
//                        if(!iterations.contains(iteration)) {
//                            iterations.add(iteration);
//                        }
//                        
//                        EvaluationResult res = data.get(key).get(iteration);
//                        
//                        EvaluationResult er = evalSteps.get(iteration);
//                        if(er==null) {
//                            er = new EvaluationResult(0, 0, 0, 0);
//                            evalSteps.put(iteration, er);
//                        }
//                        er.merge(res);
//                    }
//                    
//                }
//                
//                StringBuilder sb = new StringBuilder();
//                sb.append(" ");
//                for(String it : iterations) {
//                    sb.append("\t");
//                    sb.append(it);
//                }
//                System.err.println(sb.toString());
//                
//                sb.setLength(0);
//                sb.append("precision");
//                for(String it : iterations) {
//                    sb.append("\t");
//                    if(evalSteps.get(it)!=null) {
//                        sb.append(evalSteps.get(it).getPrecision());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//                
//                sb.setLength(0);
//                sb.append("recall   ");
//                for(String it : iterations) {
//                    sb.append("\t");
//                    if(evalSteps.get(it)!=null) {
//                        sb.append(evalSteps.get(it).getRecall());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//                
//                sb.setLength(0);
//                sb.append("f1       ");
//                for(String it : iterations) {
//                    sb.append("\t");
//                    if(evalSteps.get(it)!=null) {
//                        sb.append(evalSteps.get(it).getF1Score());
//                    } else {
//                        sb.append("");
//                    }
//                }
//                System.err.println(sb.toString());
//                
//            }
//        }

//        EvaluationResult m1 = new EvaluationResult((int)vaInstBlP.getSum(), (int)vaInstBlR.getSum(), (int)vaInstBlF1.getSum(), (int) vaInstBlAcc.getSum());
        EvaluationResult m2 = new EvaluationResult((int) vaInstP.getSum(), (int) vaInstR.getSum(), (int) vaInstF1.getSum(), (int) vaInstAcc.getSum());
//        EvaluationResult m3 = new EvaluationResult((int)vaInstMaxP.getSum(), (int)vaInstMaxR.getSum(), (int)vaInstMaxF1.getSum(), (int)vaInstMaxAcc.getSum());        
//        

        System.err.println("Instance mappings");
//        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy\tCorrect\tfound Mappings\tReference");
//        System.err.println(String.format(
//                "baseline:\t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
//                m1.getPrecision(), m1.getPrecision(), m1.getF1Score(), m1.getAccuracy(), m1.getCorrect(), m1.getInputSetSize(), m1.getReferenceSetSize()));
//        
        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
                m2.getPrecision(), m2.getRecall(), m2.getF1Score(), m2.getAccuracy(), m2.getCorrect(), m2.getInputSetSize(), m2.getReferenceSetSize()));


        instances.close();
        performance.close();
    }

    public static void printProperties(Collection<MatchingResult> results, Collection<HashMap<String, HashMap<String, EvaluationResult>>> steps) throws IOException {
        CSVWriter properties = null;
        //instances = new CSVWriter(new FileWriter("instances.csv"));
        properties = new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File("properties.csv")), "UTF-8"));
        CSVWriter performance = new CSVWriter(new FileWriter("performance_prop.csv"));
        String[] perfHeaders = new String[]{
            "table",
            "property precision",
            "property recall",
            "property f1",
            "#property mappings",
            "#property mappings correct",
            "#properties",
            "#property mappings in gs"
        };
        performance.writeNext(perfHeaders);
//        
        ValueAggregator vaInstP = new ValueAggregator();
        ValueAggregator vaInstR = new ValueAggregator();
        ValueAggregator vaInstF1 = new ValueAggregator();
        ValueAggregator vaInstAcc = new ValueAggregator();
//                
//        ValueAggregator vaInstBlP = new ValueAggregator();
//        ValueAggregator vaInstBlR = new ValueAggregator();
//        ValueAggregator vaInstBlF1 = new ValueAggregator();
//        ValueAggregator vaInstBlAcc = new ValueAggregator();
//        
//        ValueAggregator vaInstMaxP = new ValueAggregator();
//        ValueAggregator vaInstMaxR = new ValueAggregator();
//        ValueAggregator vaInstMaxF1 = new ValueAggregator();
//        ValueAggregator vaInstMaxAcc = new ValueAggregator();
//        
//        ValueAggregator vaKey = new ValueAggregator();
////
////        
////        EvaluationResult resInst = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resProp = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resClass = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resMax = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resPropRange = new EvaluationResult(0, 0, 0, 0);
////        
        for (MatchingResult r : results) {
            MatchingEvaluation me = r.getEvaluation();
//            
            try {
//            
                vaInstP.AddValue(me.getPropertyResult().getCorrect());
                vaInstR.AddValue(me.getPropertyResult().getInputSetSize());
                vaInstF1.AddValue(me.getPropertyResult().getReferenceSetSize());
                vaInstAcc.AddValue(me.getPropertyResult().getTotalPopulationSize());

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error! " + r.getWebtable().getHeader());
            }

            //r.write();
//
            for (String[] line : r.formatPropertiesForCSV()) {
                properties.writeNext(line);
            }
//            
//            
            String[] perf = new String[perfHeaders.length];
            int k = 0;
            perf[k++] = r.getWebtable().getHeader();
            perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getPropertyResult().getF1Score());
            perf[k++] = Double.toString(r.getPropertyMappings().size());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getCorrect());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getInputSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getPropertyResult().getReferenceSetSize());

            performance.writeNext(perf);
        }
        properties.close();
        performance.close();

//        EvaluationResult m1 = new EvaluationResult((int)vaInstBlP.getSum(), (int)vaInstBlR.getSum(), (int)vaInstBlF1.getSum(), (int) vaInstBlAcc.getSum());
        EvaluationResult m2 = new EvaluationResult((int) vaInstP.getSum(), (int) vaInstR.getSum(), (int) vaInstF1.getSum(), (int) vaInstAcc.getSum());
        //       EvaluationResult m3 = new EvaluationResult((int)vaInstMaxP.getSum(), (int)vaInstMaxR.getSum(), (int)vaInstMaxF1.getSum(), (int)vaInstMaxAcc.getSum());        


        System.err.println("Property mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy\tCorrect\tfound Mappings\tReference");
//        System.err.println(String.format(
//                "baseline:\t     %.2f\t  %.2f\t  %.2f\t  %.2f",
//                m1.getPrecision(), m1.getPrecision(), m1.getF1Score(), m1.getAccuracy()));

        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
                m2.getPrecision(), m2.getRecall(), m2.getF1Score(), m2.getAccuracy(), m2.getCorrect(), m2.getInputSetSize(), m2.getReferenceSetSize()));


//        instances.close();
//        performance.close();
    }

    public static void printClasses(Collection<MatchingResult> results, Collection<HashMap<String, HashMap<String, EvaluationResult>>> steps) throws IOException {
        CSVWriter properties = null;
        //instances = new CSVWriter(new FileWriter("instances.csv"));

        //properties = new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File("classes.csv")),"UTF-8"));
        CSVWriter performance = new CSVWriter(new FileWriter("performance_class.csv"));
        String[] perfHeaders = new String[]{
            "table",
            "class precision",
            "class recall",
            "class f1",
            "#class mappings",
            "#class mappings correct",
            "#classes",
            "#class mappings in gs"
        };
        performance.writeNext(perfHeaders);
//        
        ValueAggregator vaInstP = new ValueAggregator();
        ValueAggregator vaInstR = new ValueAggregator();
        ValueAggregator vaInstF1 = new ValueAggregator();
        ValueAggregator vaInstAcc = new ValueAggregator();
//                
//        ValueAggregator vaInstBlP = new ValueAggregator();
//        ValueAggregator vaInstBlR = new ValueAggregator();
//        ValueAggregator vaInstBlF1 = new ValueAggregator();
//        ValueAggregator vaInstBlAcc = new ValueAggregator();
//        
//        ValueAggregator vaInstMaxP = new ValueAggregator();
//        ValueAggregator vaInstMaxR = new ValueAggregator();
//        ValueAggregator vaInstMaxF1 = new ValueAggregator();
//        ValueAggregator vaInstMaxAcc = new ValueAggregator();
//        
//        ValueAggregator vaKey = new ValueAggregator();
////
////        
////        EvaluationResult resInst = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resProp = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resClass = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resMax = new EvaluationResult(0, 0, 0, 0);
////        EvaluationResult resPropRange = new EvaluationResult(0, 0, 0, 0);
////        


        for (MatchingResult r : results) {
            MatchingEvaluation me = r.getEvaluation();
//            
//            
            vaInstP.AddValue(me.getClassResult().getCorrect());
            vaInstR.AddValue(me.getClassResult().getInputSetSize());
            vaInstF1.AddValue(me.getClassResult().getReferenceSetSize());
            vaInstAcc.AddValue(me.getClassResult().getTotalPopulationSize());


            //r.write();
//  
////            
////            
            String[] perf = new String[perfHeaders.length];
            int k = 0;
            perf[k++] = r.getWebtable().getHeader();
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getPrecision());
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getRecall());
            perf[k++] = Double.toString(r.getEvaluation().getClassResult().getF1Score());
            perf[k++] = Double.toString(r.getClassMappings().size());
            perf[k++] = Integer.toString(r.getEvaluation().getClassResult().getCorrect());
            perf[k++] = Integer.toString(r.getEvaluation().getClassResult().getInputSetSize());
            perf[k++] = Integer.toString(r.getEvaluation().getClassResult().getReferenceSetSize());

            performance.writeNext(perf);
        }
//        properties.flush();
        performance.flush();

//        EvaluationResult m1 = new EvaluationResult((int)vaInstBlP.getSum(), (int)vaInstBlR.getSum(), (int)vaInstBlF1.getSum(), (int) vaInstBlAcc.getSum());
        EvaluationResult m2 = new EvaluationResult((int) vaInstP.getSum(), (int) vaInstR.getSum(), (int) vaInstF1.getSum(), (int) vaInstAcc.getSum());
        //       EvaluationResult m3 = new EvaluationResult((int)vaInstMaxP.getSum(), (int)vaInstMaxR.getSum(), (int)vaInstMaxF1.getSum(), (int)vaInstMaxAcc.getSum());        


        System.err.println("Class mappings");
        System.err.println("        \tPrecision\tRecall\tF1\tAccuracy\tCorrect\tfound Mappings\tReference");
//        System.err.println(String.format(
//                "baseline:\t     %.2f\t  %.2f\t  %.2f\t  %.2f",
//                m1.getPrecision(), m1.getPrecision(), m1.getF1Score(), m1.getAccuracy()));

        System.err.println(String.format(
                "result:  \t     %.2f\t  %.2f\t  %.2f\t  %.2f\t %d\t %d\t %d",
                m2.getPrecision(), m2.getRecall(), m2.getF1Score(), m2.getAccuracy(), m2.getCorrect(), m2.getInputSetSize(), m2.getReferenceSetSize()));


//        instances.close();
        performance.close();
    }
}
