package hudson.plugins.averageDuration;

import hudson.Util;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.averageDuration.utils.AverageDuration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.util.logging.Logger;

@ExportedBean(defaultVisibility=2)
public class AbstractAverageDurationAction <JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> implements Action {
    private static final Logger LOGGER = Logger.getLogger(AbstractAverageDurationAction.class.getName());
    private final AverageDuration averageDuration = AverageDuration.getInstance();
    private final Job<JobT,RunT> project;


    @SuppressWarnings("unchecked")
    @DataBoundConstructor
    public AbstractAverageDurationAction(Job<JobT,RunT> project) {
        this.project = project;
        averageDuration.setJob(project);
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

    public AverageDuration getAverageDuration() {
        return averageDuration;
    }

    public Job<JobT, RunT> getProject() {
        return project;
    }

    /**
     * Remote API access.
     */
    public final Api getApi() {
        return new Api(this);
    }
    @Exported
    public String getAverageBuildDuration() {
        long averageDuration = getAverageDuration().getEstimatedDuration();
        if (averageDuration > 0)
            return Util.getTimeSpanString(averageDuration);
        return "N/A";
    }
}
