package hudson.plugins.averageDuration;

import hudson.Util;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class AverageDurationSettings {
    private int candidates = 3;
    private int stepsBack = 6;
    public final int DEFAULT_NUMBER_OF_CANDIDATES = 3;
    public final int DEFAULT_NUMBER_OF_STEPS_BACK = 6;

    @DataBoundConstructor
    public AverageDurationSettings() {
    }

    public void setCandidates(int candidates) {
        this.candidates = candidates;
    }

    public void setStepsBack(int stepsBack) {
        this.stepsBack = stepsBack;
    }

    public int getCandidates() {
        return candidates;
    }

    public int getStepsBack() {
        return stepsBack;
    }

}
