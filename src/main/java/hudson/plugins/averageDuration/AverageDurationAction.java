package hudson.plugins.averageDuration;

import hudson.Util;
import hudson.model.BallColor;
import hudson.model.Job;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.CustomExportedBean;

import javax.annotation.CheckForNull;

public class AverageDurationAction extends AbstractAverageDurationAction implements CustomExportedBean {
    @DataBoundConstructor
    public AverageDurationAction(Job project) {
        super(project);
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.AverageDuration_DisplayName();
    }

    @Override
    public Object toExportedObject() {
        JSONObject json = new JSONObject()
                .accumulate("_class", this.getClass())
                .accumulate("averageduration", getAverageBuildDuration())
                .accumulate("averageduration-millis", getAverageBuildDurationMilliseconds());
        if (getProject().isBuilding()) {
            json.accumulate("started", Util.getPastTimeString(currentBuildDuration()));
            if (getProject().getIconColor() != BallColor.NOTBUILT_ANIME) {
                if (!getEstimatedTimeRemaining().equals("N/A"))
                    json.accumulate("estimated-remaining-time", getEstimatedTimeRemaining());
                else json.accumulate("estimated-remaining-time", getOvertime() + " overdue");
            }
        }
        return json;
    }


    public String getEstimatedTimeRemaining() {
        if (getProject().isBuilding()) {
            if (getAverageBuildDurationMilliseconds() - currentBuildDuration() >= 0)
                return Util.getTimeSpanString(getAverageBuildDurationMilliseconds() - currentBuildDuration());
        }
        return "N/A";
    }

    public String getOvertime() {
        if (getProject().isBuilding()) {
            if (getEstimatedTimeRemaining().equals("N/A"))
                return Util.getTimeSpanString(currentBuildDuration() - getAverageBuildDurationMilliseconds());
        }
        return "N/A";
    }

    private long currentBuildDuration() {
        return System.currentTimeMillis() - getProject().getLastBuild().getStartTimeInMillis();
    }

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
