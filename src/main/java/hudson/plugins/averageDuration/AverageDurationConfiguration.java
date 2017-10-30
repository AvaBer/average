package hudson.plugins.averageDuration;

import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("WeakerAccess")
public class AverageDurationConfiguration implements Describable {
    private int candidates = 3;
    private int stepsBack = 6;
    public final int DEFAULT_CANDIDATES = 3;
    public final int DEFAULT_STEPS_BACK = 6;
    public final int MIN_VALUE = 1;
    public final int MAX_VALUE = 50;
    private boolean showOnJobPage = false;

    @DataBoundConstructor
    public AverageDurationConfiguration() {
    }

    public void setDefaultCandidates() {
        candidates = DEFAULT_CANDIDATES;
    }

    public void setDefaultStepsBack() {
        stepsBack = DEFAULT_STEPS_BACK;
    }

    public void setCandidates(int candidates) {
        this.candidates = candidates;
    }

    public void setStepsBack(int stepsBack) {
        this.stepsBack = stepsBack;
    }

    public void setShowOnJobPage(boolean showOnJobPage) {
        this.showOnJobPage = showOnJobPage;
    }

    public int getCandidates() {
        return candidates;
    }

    public int getStepsBack() {
        return stepsBack;
    }

    public boolean isShowOnJobPage() {
        return showOnJobPage;
    }

    @Override
    public Descriptor getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(AverageDurationConfiguration.class);
    }
}
