package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

@Extension
public class AverageDurationConfig implements Describable<AverageDurationConfig>{
    @Override
    public Descriptor<AverageDurationConfig> getDescriptor() {
        return null;
    }
    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<AverageDurationConfig> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return super.getDisplayName();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return super.configure(req, json);
        }
    }
}
