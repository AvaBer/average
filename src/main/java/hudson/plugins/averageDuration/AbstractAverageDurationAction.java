package hudson.plugins.averageDuration;

import hudson.Util;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Job;
import hudson.plugins.averageDuration.utils.JobWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
@ExportedBean(defaultVisibility = 2)
public class AbstractAverageDurationAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(AbstractAverageDurationAction.class.getName());
    private static transient AverageDurationDescriptor DESCRIPTOR;
    private JobWrapper jobWrapper = new JobWrapper();
    private final Job<?, ?> project;

    @SuppressWarnings("unchecked")
    @DataBoundConstructor
    public AbstractAverageDurationAction(Job<?, ?> project) {
        DESCRIPTOR = new AverageDurationDescriptor();
        this.project = project;
        jobWrapper.setConfiguration(DESCRIPTOR.getConfig());
        jobWrapper.setJob(project);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }

    public Job getProject() {
        return project;
    }

    /**
     * Used by the action.jelly page.
     * If true a text-box will be shown on the job page, otherwise nothing will be displayed on that page.
     */
    public boolean isShowOnJobPage() {
        return DESCRIPTOR.getConfig().isShowOnJobPage();
    }

    /**
     * Remote API access.
     */
    public final Api getApi() {
        return new Api(this);
    }

    /**
     * Shows the estimated build duration on the job/job-name/api page
     * optionally displays the same information on the job/job-name/ page on the sidebar
     */
    @Exported
    public String getAverageBuildDuration() {
        long averageDuration = jobWrapper.getEstimatedDuration();
        if (averageDuration > 0)
            return Util.getTimeSpanString(averageDuration);
        return "N/A";
    }
}
