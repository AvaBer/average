package hudson.plugins.averageDuration;

import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
@SuppressWarnings("WeakerAccess")
public class AverageDurationConfiguration implements Describable {
    private int candidates = 3;
    private int stepsBack = 6;
    public final int DEFAULT_NUMBER_OF_CANDIDATES = 3;
    public final int DEFAULT_NUMBER_OF_STEPS_BACK = 6;

    @DataBoundConstructor
    public AverageDurationConfiguration() {
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


    @Override
    public Descriptor getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(AverageDurationConfiguration.class);
    }
}
