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
@ExportedBean(defaultVisibility=2)
public class AbstractAverageDurationAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(AbstractAverageDurationAction.class.getName());
    //    private final AverageDuration averageDuration = new AverageDuration();
    private static final AverageDurationDescriptor DESCRIPTOR = new AverageDurationDescriptor();
    private JobWrapper jobWrapper = new JobWrapper();
    private final Job<?,?> project;


    @SuppressWarnings("unchecked")
    @DataBoundConstructor
    public AbstractAverageDurationAction(Job<?,?> project) {
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

//    public AverageDuration getAverageDuration() {
//        return averageDuration;
//    }

    public Job getProject() {
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
        long averageDuration = jobWrapper.getEstimatedDuration();
        if (averageDuration > 0)
            return Util.getTimeSpanString(averageDuration);
        return "N/A";
    }
}
