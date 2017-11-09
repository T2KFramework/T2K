package de.dwslab.T2K.matching.secondline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Aggregates the second matrix to the dimensions of the first matrix
 * 
 * @author Oliver
 *
 */
public class Aggregate<TFirst, TSecond> extends HierarchyMatcher<TFirst, TSecond> {
	
	private AggregationType aggregationType;
	public void setAggregationType(AggregationType aggregationType) {
		this.aggregationType = aggregationType;
	}
	public AggregationType getAggregationType() {
		return aggregationType;
	}

	private SimilarityMatrix<TFirst> result;
	
	@Override
	protected String getProgressMessage() {
	    return "Aggregate";
	}
	
	@Override
	protected void calculateScoreForFirstMatrix(TFirst instance,
	        TFirst candidate, Collection<TSecond> firstParts,
	        Collection<TSecond> secondParts, Double firstSimilarity) {
	    
	    if(isCollectMatchingInfo() && !isRunInParallel()) {
	        currentLog = new StringBuilder();
	        currentLog.append(instance);
	        currentLog.append("<->");
	        currentLog.append(candidate);
	        currentLog.append(": ");
	    }
	    
	    List<Double> scores = new ArrayList<Double>();
	    
	    enumerateSecond(instance, candidate, firstParts, secondParts, firstSimilarity, scores);
	    
        // determine the aggregated score
        Double score = null;
        
        if(scores.size()>0) {
            switch (getAggregationType()) {
            case Sum:
                
                score = 0.0;
                for(Double d : scores) {
                    score += d;
                }
                
                break;
            case Max:
                
                for(Double d : scores) {
                    score = Math.max(d, score);
                }
    
                break;
            case Count:
                
                score = (double) scores.size();
                
                break;
            case Average:
                
                score = 0.0;
                for(Double d : scores) {
                    score += d;
                }
                
                score /= (double)scores.size();
                
                break;
            default:
                break;
            }
        }
        
        if(score!=null) {
            synchronized (result) {
                result.set(instance, candidate, score);
            }
            
            if(isCollectMatchingInfo() && !isRunInParallel()) {
                currentLog.append(String.format(" = %.2f", score));
                currentLog.append("\n");
                log.append(currentLog);
            }
        }
	}
	
	public SimilarityMatrix<TFirst> match(SimilarityMatrix<TFirst> first, SimilarityMatrix<TSecond> second, MatchingHierarchyAdapater<TFirst, TSecond> hierarchy) {
	    
	    log = new StringBuilder();
	    
	    initialise(first, second, hierarchy);
            
	    result = getSimilarityMatrixFactory().createSimilarityMatrix(first.getFirstDimension().size(), first.getSecondDimension().size());
	    
	    enumerateFirst();
	    
	    return result;
	}
	
	private StringBuilder log;
	private StringBuilder currentLog;
	
	public StringBuilder getLog() {
        return log;
    }
	
	@Override
	protected void logValue(TFirst instance, TFirst candidate,
	        TSecond firstValue, TSecond secondValue, Double firstSimilarity,
	        Double secondSimilarity, Double combinedSimilarity) {
	    
	    if(!isRunInParallel()) {
	        currentLog.append("+");
	        currentLog.append(firstValue);
	        currentLog.append("==");
	        currentLog.append(secondValue);
	        currentLog.append(String.format(" (%.2f) ", secondSimilarity));
        }
	    
	}
	
	@Override
	public void setCollectMatchingInfo(boolean collectMatchingInfo) {
	    super.setCollectMatchingInfo(collectMatchingInfo);
	    
	    if(collectMatchingInfo) {
    	    // to use the log, we must run on a single thread
    	    //setNumThreads(1);
	        setRunInParallel(false);
	    }
	}
	
//	public SimilarityMatrix<TFirst> match(SimilarityMatrix<TFirst> first, SimilarityMatrix<TSecond> second, MatchingHierarchyAdapater<TFirst, TSecond> hierarchy)
//	{
//		
//		// create similarity matrix
//		SimilarityMatrix<TFirst> sim = getSimilarityMatrixFactory().createSimilarityMatrix(first.getFirstDimension().size(), first.getSecondDimension().size());
//		
//		int steps = 0;
//		
//		List<Double> scores = new ArrayList<Double>();
//		
//		// enumerate all instances of the first dimension of the first matrix
//		for(TFirst instance : first.getFirstDimension())
//		{
//				
//		    Collection<TSecond> firstParts = hierarchy.getParts(instance);
//		    
//			// and enumerate all matching candidates of the second dimension
//			for(TFirst candidate : first.getMatches(instance))
//			{
//				// this combination of instance and candidate will appear in the resulting matrix
//				
//				scores.clear();
//				Collection<TSecond> parts = hierarchy.getParts(candidate);
//				
//				// for each of the instances, list all values
//				for(TSecond firstValue : firstParts)
//				{
//					
//					// and do the same for all candidates
//					for(TSecond secondValue : second.getMatches(firstValue))
//					{
//
//					    if(parts.contains(secondValue)) {
//    						// Collect the scores from the second matrix
//    						scores.add(second.get(firstValue, secondValue));
//    						steps++;
//					    }
//					}
//				}
//				
//				// determine the aggregated score
//				double score = 0.0;
//				
//				switch (getAggregationType()) {
//				case Sum:
//					
//					for(Double d : scores)
//						score += d;
//					
//					break;
//
//				default:
//					break;
//				}
//				
//				sim.set(instance, candidate, score);
//			}
//		}
//		
//		System.out.println(steps + " steps during aggregation.");
//		
//		return sim;
//	}
	
}
