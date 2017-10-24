package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.plugins.averageDuration.utils.JobWrapper;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class AverageDurationViewColumn extends ListViewColumn {
    private AverageDurationDescriptor DESCRIPTOR;

    @DataBoundConstructor
    public AverageDurationViewColumn() {
        super();
    }

    @SuppressWarnings("unchecked")
    public String getAverageBuildDurationString(Job<?, ?> job) {
        DESCRIPTOR = new AverageDurationDescriptor();
        JobWrapper jobWrapper = new JobWrapper();
        jobWrapper.setConfiguration(DESCRIPTOR.getConfig());
        jobWrapper.setJob(job);
        return Util.getTimeSpanString(jobWrapper.getEstimatedDuration());
    }

    @Extension(ordinal=DEFAULT_COLUMNS_ORDINAL_PROPERTIES_START-5)
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public boolean shownByDefault() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Average Duration";
        }


    }
}
