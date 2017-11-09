package de.dwslab.T2K.matching.experiments;

import de.dwslab.T2K.matching.dbpedia.Preprocessing;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_TABLE_TYPE;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class CheckGSConsistency {

    public static void main(String args[]) throws IOException {
        String dbp = args[0];
        String eqProps = args[1];
        String prop = args[2];
        String cls = args[3];
        String tables = args[4];

        Preprocessing.loadSurfaceForms(new File("empty").getAbsolutePath());
        Preprocessing.loadRedirects(new File("empty").getAbsolutePath());

        Parallel.SetDefaultNumProcessors(20);

        MatchingParameters para = new MatchingParameters();

        MatchingData m = new MatchingData();
        m.loadDBpedia(dbp, para);
        m.loadEquivalentProperties(eqProps);

        EvaluationParameters eval = new EvaluationParameters();
        eval.setPropertyGoldStandardLocation(new File(prop).getAbsolutePath());
        eval.setClassGoldStandardLocation(new File(cls).getAbsolutePath());

        eval.setEquivalentPropertiesLocation(new File(eqProps).getAbsolutePath());
        eval.setPropertyRangesLocation(new File(new File(eqProps).getParent(), "propertyRanges.tsv").getAbsolutePath());
        eval.setCorrectedInstancesLocation(new File(new File(eqProps).getParent(), "correctedInstances.tsv").getAbsolutePath());
        eval.loadCanoniser();
        eval.setClassHierarchyLocation(new File(new File(eqProps).getParent(), "superclasses.tsv").getAbsolutePath());

        File tableDir = new File(tables);
        for (File f : tableDir.listFiles()) {
            Table t = m.loadWebTable(f.getPath(), new Timer("don't care"), para, WebtableToDBpediaMatchingProcess.tableType.jsonWebTable);
            String webTableName = f.getName();

            GoldStandard goldStandard = new GoldStandard();
            goldStandard.initialise(webTableName, t, eval);

            //System.out.println(goldStandard.getClassGoldStandard().keySet());
            System.out.println(webTableName);
            List<Object> classes = goldStandard.getClassGoldStandard().get(webTableName.split("\\.")[0]);
            if (classes == null || classes.isEmpty()) {
                continue;
            }
            Map<Object, Object> props = goldStandard.getPropertyGoldStandard();

            for (Table dbTabs : m.getDbpediaTables()) {
                for (Object o : classes) {
                    String className = (String) o;
                    String currectTabName = dbTabs.getHeader().toLowerCase().replace(" ", "").replace(".csv.gz", "");
                    if (className.equals(currectTabName)) {
                        //System.out.println("class found! " + className + " - " + webTableName);
                        boolean foundInClass = false;
                        for (Object p : props.values()) {
                            for (TableColumn tc : dbTabs.getColumns()) {
                                if (tc.getURI().equals(p)) {
                                    foundInClass = true;
                                }
                            }
                            if(!foundInClass) {
                                System.out.println("not found in class " + className + " - " + p + " - " + f.getName());
                            }
                        }
                    }
                }
            }
        }
    }

}
