package hudson.plugins.averageDuration;

import hudson.Util;
import org.apache.commons.lang.StringUtils;

public class AverageDurationSettings {
    private int numberOfCandidates = -1;
    private int buildHistoryPool = -1;

    public AverageDurationSettings(int numberOfCandidates, int buildHistoryPool) {
        this.numberOfCandidates = numberOfCandidates;
        this.buildHistoryPool = buildHistoryPool;
    }

    public void setNumberOfCandidates(String numberOfCandidates) {
        if (StringUtils.isEmpty(numberOfCandidates)) {
            this.numberOfCandidates = -1;
        }
    }

    public void setBuildHistoryPool(String buildHistoryPool) {
        this.buildHistoryPool = -1;
    }

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public int getBuildHistoryPool() {
        return buildHistoryPool;
    }


}
