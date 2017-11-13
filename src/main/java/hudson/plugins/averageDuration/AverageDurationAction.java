package hudson.plugins.averageDuration;

import hudson.Util;
import hudson.model.BallColor;
import hudson.model.Job;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.CustomExportedBean;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.CheckForNull;

public class AverageDurationAction extends AbstractAverageDurationAction  {
    @DataBoundConstructor
    public AverageDurationAction(Job project) {
        super(project);
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.AverageDuration_DisplayName();
    }

    /**
     * Exported to the job/api, if the job has a running build some additional info will be displayed there.<br>
     * "skipNull = true" allows this not to be rendered unless the job has a build running.
     * @return null if not building otherwise some additional info.
     */
    @Exported(name = "build-progress", skipNull = true)
    public JSONObject buildProgress() {
        JSONObject json = new JSONObject();
        if (getProject().isBuilding()) {
            json.accumulate("started", Util.getPastTimeString(currentBuildDuration()));
            if (getProject().getIconColor() != BallColor.NOTBUILT_ANIME) {
                if (!getEstimatedTimeRemaining().equals("N/A"))
                    json.accumulate("estimated-remaining-time", getEstimatedTimeRemaining());
                else json.accumulate("estimated-remaining-time", getOvertime() + " overdue");
            }
            return json;
        }
        return null;
    }


    public String getEstimatedTimeRemaining() {
        if (getProject().isBuilding()) {
            if (getAverageBuildDurationMilliseconds() - currentBuildDuration() >= 0)
                return Util.getTimeSpanString(getAverageBuildDurationMilliseconds() - currentBuildDuration());
        }
        return "N/A";
    }

    /**
     * Used in action.jelly as a tooltip when a build is running and exported in the build-progress.
     * If a build runs longer than the time estimate this method will give the time passed since the estimate.
     */
    public String getOvertime() {
        if (getProject().isBuilding()) {
            if (getEstimatedTimeRemaining().equals("N/A"))
                return Util.getTimeSpanString(currentBuildDuration() - getAverageBuildDurationMilliseconds());
        }
        return "N/A";
    }

    /**
     * @return duration since build started
     */
    private long currentBuildDuration() {
        return System.currentTimeMillis() - getProject().getLastBuild().getStartTimeInMillis();
    }

    /**
     * Used in action.jelly for the progress-bar to fill up.<br>
     * this is a percentage representation of the estimated build progress,
     * returns -1 if no builds exist of if it's impossible to calculate.
     * @return estimated build progress
     */
    public int getBuildProgress() {
        long d = getAverageBuildDurationMilliseconds();
        if (getProject().getLastBuild().isBuilding()) {
            if (d <= 0)
                return -1;
            int num = (int) (currentBuildDuration() * 100 / d);
            if (num >= 100)
                return 99;
            return num;
        }
        return -1;
    }

}
