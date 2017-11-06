package hudson.plugins.averageDuration;

import hudson.model.Job;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.CustomExportedBean;

import javax.annotation.CheckForNull;

public class AverageDurationAction extends AbstractAverageDurationAction implements CustomExportedBean{
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
        return new JSONObject()
                .accumulate("_class", this.getClass())
                .accumulate("averageduration", getAverageBuildDuration())
                .accumulate("averageduration-millis", getAverageBuildDurationMilliseconds());
    }
}
