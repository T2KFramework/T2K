
package de.dwslab.T2K.matching.dbpedia.algorithm;

import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_LOG_CANDIDATE_SELECTION;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_REFINEMENT_EDIT_DIST;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_REFINEMENT_K;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_REFINEMENT_SIMILARITY_FUNCTION;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_REFINEMENT_STRING_FILTERING;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.PAR_REFINEMENT_THRESHOLD;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateRefinementComponent.params;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateAbstractMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateRefinementMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowTokenMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.TextBasedMatcher;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author domi
 */
public class CandidateAbstractSelectionComponent extends WebtableToDBpediaMatchingComponent {

    private SimilarityMatrix<TableRow> initialCandidateAbstracrSimilarity;
    
    public SimilarityMatrix<TableRow> getCandidateAbstractSimilarity() {
        return initialCandidateAbstracrSimilarity;
    }
    
    
    
    public static final Parameter PAR_ABSTRACT_THRESHOLD = new Parameter("CandidateAbstract.Threshold", 0.8);

    protected static final List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_ABSTRACT_THRESHOLD);
        params = l;
    }
        
    public CandidateAbstractSelectionComponent() {
        setParameters(params);
    }
        
    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {
                
        CandidateAbstractMatcher candref = getMatchers().getCandidateAbstractMatcher();
        candref.setThreshold((double)config.getValue(PAR_ABSTRACT_THRESHOLD));
    }
    
    @Override
    public void run(Configuration config) {
        
        try {
            initialiseParameters(config);
            initialCandidateAbstracrSimilarity = getMatchers().getCandidateAbstractMatcher().match(getData());     
            //initialCandidateAbstracrSimilarity.makeColumnStochastic();
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println(initialCandidateAbstracrSimilarity.getOutput(null, getGoldStandard().getInstanceGoldStandard().values(), new TableRowUriMatchingAdapter(), null));
            //System.out.println(initialCandidateSimilarity.listPairs());
            }            
            
            
        } catch (Exception ex) {
            Logger.getLogger(CandidateAbstractSelectionComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
