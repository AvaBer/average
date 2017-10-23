package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.plugins.averageDuration.utils.JobWrapper;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.isEmpty;

@SuppressWarnings("WeakerAccess")
public class AverageDuration implements Describable<AverageDuration> {
    private static final Logger LOGGER = Logger.getLogger(AverageDuration.class.getName());
    private JobWrapper jobWrapper;
    /* ******** AverageDuration Instance ******** */
//    private static AverageDuration instance = null;

    public AverageDuration() {
        getDescriptor().load();
        jobWrapper = new JobWrapper();
    }

    @DataBoundConstructor
    public AverageDuration(AverageDurationSettings adSettings) {
        this.adSettings = adSettings;
    }

    /* ***************** Config ***************** */
    private AverageDurationSettings adSettings;

    public AverageDurationSettings getAdSettings() {
        if (adSettings != null) {
            return adSettings;
        } else {
            return new AverageDurationSettings();
        }

    }

    public JobWrapper getJobWrapper() {
        jobWrapper.setSettings(getAdSettings());
        return jobWrapper;
    }

    /* ******* AverageDuration Descriptor ******* */
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<AverageDuration> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<AverageDuration> {
//        @DataBoundConstructor
        public DescriptorImpl() {
            load();
        }

        private AverageDurationSettings adS = new AverageDurationSettings();

        public AverageDurationSettings getAdS() {
            return adS;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Average Build Time";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {

//            avg = req.bindParameters(AverageDuration.class,"average-duration");

            adS = req.bindJSON(AverageDurationSettings.class, json);
            save();
            return true;
        }

        public FormValidation doCheckCandidates(@QueryParameter String candidates) {
            LOGGER.info(candidates);
            if (isEmpty(candidates.trim())) {
                adS.setCandidates(adS.DEFAULT_NUMBER_OF_CANDIDATES);
                return FormValidation.ok("Using default value of: " + adS.DEFAULT_NUMBER_OF_CANDIDATES);
            } else {
                int newValue;
                try {
                    newValue = Integer.parseInt(candidates.trim());
                } catch (NumberFormatException nfe) {
                    return FormValidation.error("Value must be a number");
                }
                if (newValue < 1)
                    return FormValidation.error("Value cannot be set to zero");
                if (checkValues(newValue, adS.getCandidates(), adS.getStepsBack(), "candidates")) {
                    adS.setCandidates(newValue);
                    return FormValidation.ok();
                }
                return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
            }
        }

        public FormValidation doCheckStepsBack(@QueryParameter String stepsBack) {
            LOGGER.info(stepsBack);
            if (isEmpty(stepsBack.trim())) {
                adS.setStepsBack(adS.DEFAULT_NUMBER_OF_STEPS_BACK);
                return FormValidation.ok("Using default value of: " + adS.DEFAULT_NUMBER_OF_STEPS_BACK);
            } else {
                int newValue;
                try {
                    newValue = Integer.parseInt(stepsBack.trim());
                } catch (NumberFormatException nfe) {
                    return FormValidation.error("Value must be a number");
                }
                if (newValue < 1)
                    return FormValidation.error("Value cannot be set to zero");
                if (checkValues(newValue, adS.getStepsBack(), adS.getCandidates(), "stepsBack")) {
                    adS.setStepsBack(newValue);
                    return FormValidation.ok();
                }
                return FormValidation.error("Number of steps back must be equal to or greater than number of candidates ");
            }
        }

        private boolean checkValues(int newValue, int oldValue, int oldCounterpart, String targetVariable) {
            if (newValue == oldValue)
                return true;
            switch (targetVariable) {
                case "candidates":
                    return newValue <= oldCounterpart;
                case "stepsBack":
                    return newValue >= oldCounterpart;
                default:
                    return false;
            }
        }

    }
}

