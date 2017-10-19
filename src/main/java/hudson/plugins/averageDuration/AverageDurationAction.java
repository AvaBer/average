package hudson.plugins.averageDuration;

import hudson.model.Job;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;

public class AverageDurationAction extends AbstractAverageDurationAction {
    @DataBoundConstructor
    public AverageDurationAction(Job project) {
        super(project);
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Average Duration";
    }
}
