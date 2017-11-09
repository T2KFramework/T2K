package de.dwslab.T2K.matching;

import java.util.Collection;
import java.util.LinkedList;

import de.dwslab.T2K.utils.timer.Timer;

/**
 * super class for all matchers
 * @author Oliver
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMatcher {

    private boolean collectMatchingInfo = false;

    /**
     * returns whether additional information about the matching process is
     * collected
     * 
     * @return
     */
    public boolean isCollectMatchingInfo() {
        return collectMatchingInfo;
    }

    /**
     * sets whether additional information about the matching process is
     * collected
     * 
     * @return
     */
    public void setCollectMatchingInfo(boolean collectMatchingInfo) {
        this.collectMatchingInfo = collectMatchingInfo;

        if (collectMatchingInfo) {
            pairs = new LinkedList<MatchingPair>();
            nonZeroPairs = new LinkedList<MatchingPair>();
        }
    }

    private Collection<MatchingPair> pairs;

    /**
     * returns the instance pairs that were compared during matching (only
     * available if isCollectMatchingInfo()==true)
     * 
     * @return
     */
    public Collection<MatchingPair> getPairs() {
        return pairs;
    }

    private Collection<MatchingPair> nonZeroPairs;

    /**
     * returns the instance pairs that were compared during matching and have a
     * similarity > 0 (only available if isCollectMatchingInfo()==true)
     * 
     * @return
     */
    public Collection<MatchingPair> getNonZeroPairs() {
        return nonZeroPairs;
    }

    private boolean runInParallel = true;
    /**
     * returns whether the matcher will run on multiple threads (if implemented)
     * @return
     */
    public boolean isRunInParallel() {
        return runInParallel;
    }
    /**
     * sets whether the matcher will run on multiple threads (if implemented)
     * @param runInParallel
     */
    public void setRunInParallel(boolean runInParallel) {
        this.runInParallel = runInParallel;
    }
    
    private Timer parentTimer;
    public Timer getParentTimer() {
        return parentTimer;
    }
    public void setParentTimer(Timer parentTimer) {
        this.parentTimer = parentTimer;
    }
}
