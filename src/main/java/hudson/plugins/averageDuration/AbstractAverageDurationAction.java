package hudson.plugins.averageDuration;

import hudson.Util;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.averageDuration.utils.JobWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;

@SuppressWarnings("WeakerAccess")
@ExportedBean(defaultVisibility = 10)
public class AbstractAverageDurationAction implements Action {
    private static AverageDurationDescriptor DESCRIPTOR;
    private AverageDurationConfiguration config;
    private JobWrapper jobWrapper = new JobWrapper();
    private final Job<?, ?> project;

    @SuppressWarnings("unchecked")
    @DataBoundConstructor
    public AbstractAverageDurationAction(Job<?, ?> project) {
        DESCRIPTOR = new AverageDurationDescriptor();
        config = DESCRIPTOR.getConfig();
        this.project = project;
        jobWrapper.setConfiguration(config);
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
     * Used by the action.jelly page.<p>
     * The value is false by default, true if set on the global configuration page.<br>
     * If true a text-box will be shown on the job page, otherwise nothing will be displayed on that page.
     *
     * @return show on job page: true/false
     */
    public boolean isShowOnJobPage() {
        return DESCRIPTOR.getConfig().isShowOnJobPage();
    }

    /**
     * Remote API access.
     *
     * @return itself as a visible item in the REST Api
     */
    public final Api getApi() {
        return new Api(this);
    }

    /**
     * Shows the estimated build duration on the job/job-name/api page<br>
     * optionally displays the same information on the job/job-name/ page on the sidebar
     *
     * @return the average duration as a string if available else "N/A"
     */
    @Exported(name = "averageduration")
    public String getAverageBuildDuration() {
        long averageDuration = jobWrapper.getEstimatedDuration();
        if (averageDuration > 0)
            return Util.getTimeSpanString(averageDuration);
        return "N/A";
    }

    @Exported(name = "averageduration-millis")
    public long getAverageBuildDurationMilliseconds() {
        long averageDuration = jobWrapper.getEstimatedDuration();
        if (averageDuration > 0)
            return averageDuration;
        return -1;
    }

    public String getEstimatedTimeRemaining() {
        long estimatedTimeRemaining;
        if (project.isBuilding()) {
            Run<?, ?> lastBuild = project.getLastBuild();
            estimatedTimeRemaining = System.currentTimeMillis() - lastBuild.getStartTimeInMillis();
            if (getAverageBuildDurationMilliseconds() - estimatedTimeRemaining >= 0)
                return Util.getTimeSpanString(getAverageBuildDurationMilliseconds() - estimatedTimeRemaining);
        }
        return "N/A";
    }

    public String getOvertime() {
        long estimatedTimeRemaining;
        if (project.isBuilding()) {
            Run<?, ?> lastBuild = project.getLastBuild();
            estimatedTimeRemaining = System.currentTimeMillis() - lastBuild.getStartTimeInMillis();
            if (getEstimatedTimeRemaining().equals("N/A"))
                return Util.getTimeSpanString(estimatedTimeRemaining - getAverageBuildDurationMilliseconds());
        }
        return "N/A";
    }


}
