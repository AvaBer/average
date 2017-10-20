package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.plugins.averageDuration.utils.AverageDuration;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class AverageDurationViewColumn extends ListViewColumn {

    @DataBoundConstructor
    public AverageDurationViewColumn() {
        super();
    }

    @SuppressWarnings("unchecked")
    public String getAverageBuildDurationString(Job<?, ?> job) {
        AverageDuration avgDuration = AverageDuration.getInstance();
        avgDuration.setJob(job);
        return Util.getTimeSpanString(avgDuration.getEstimatedDuration());
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
