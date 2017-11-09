package de.dwslab.T2K.matching;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.concurrent.Task;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.data.Triple;

public class SimilarityCollector<T> {

    private Task collectSimilaritiesTask = null;
    private AtomicBoolean isDoneMatching;
    private Queue<Triple<T, T, Double>> similarities;
    private Queue<Pair<T, Object>> labels;
    
    public void queueSimilarity(Triple<T, T, Double> sim) {
        similarities.add(sim);
    }
    
    public void queueLabel(Pair<T, Object> label) {
        labels.add(label);
    }
    
    public void startCollectingSimilarities(final SimilarityMatrix<T> matrix) {
        similarities = new ConcurrentLinkedQueue<>();
        labels = new ConcurrentLinkedQueue<>();
        isDoneMatching = new AtomicBoolean(false);
        
        collectSimilaritiesTask = new Task() {
            
            @Override
            public void execute() {
                while(!isDoneMatching.get() || !similarities.isEmpty() || !labels.isEmpty()) {
                    Triple<T, T, Double> t = similarities.poll();
                    
                    if(t!=null) {
                        matrix.set(t.getFirst(), t.getSecond(), t.getThird());
                    }
                    
                    Pair<T, Object> lbl = labels.poll();
                    if(lbl!=null) {
                        matrix.setLabel(lbl.getFirst(), lbl.getSecond());
                    }
                }
            }
        };
        
        Parallel.run(collectSimilaritiesTask);
    }
    
    public void finishCollectingSimilarities() {
        isDoneMatching.set(true);
        Parallel.join(collectSimilaritiesTask);
    }
    
    public void cancelCollectingSimilarities() {
        isDoneMatching.set(true);
        Parallel.cancel(collectSimilaritiesTask);
    }
    
}
