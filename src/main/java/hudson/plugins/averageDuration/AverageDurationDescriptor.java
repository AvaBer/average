package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.isEmpty;
@SuppressWarnings("Duplicates")
@Extension
public class AverageDurationDescriptor extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(AverageDurationDescriptor.class.getName());
    private final AverageDurationConfiguration config = new AverageDurationConfiguration();

    public AverageDurationConfiguration getConfig() {
        return config;
    }

    public AverageDurationDescriptor() {
        load();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Average Build Time Configuration";
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        this.config.setCandidates(
                json.getInt("candidates")
        );
        this.config.setStepsBack(
                json.getInt("stepsBack")
        );
        save();
        return false;
    }

    /* *** Form validation stuffs *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates) {
        if (isEmpty(candidates.trim())) {
            config.setCandidates(config.DEFAULT_NUMBER_OF_CANDIDATES);
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_CANDIDATES);
        } else {
            int newValue;
            try {
                newValue = Integer.parseInt(candidates.trim());
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Value must be a number");
            }
            if (newValue < 1)
                return FormValidation.error("Value cannot be set to zero");
            if (checkValues(newValue, config.getCandidates(), config.getStepsBack(), "candidates")) {
                config.setCandidates(newValue);
                return FormValidation.ok();
            }
            return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
        }
    }

    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack) {
        if (isEmpty(stepsBack.trim())) {
            config.setStepsBack(config.DEFAULT_NUMBER_OF_STEPS_BACK);
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_STEPS_BACK);
        } else {
            int newValue;
            try {
                newValue = Integer.parseInt(stepsBack.trim());
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Value must be a number");
            }
            if (newValue < 1)
                return FormValidation.error("Value cannot be set to zero");
            if (checkValues(newValue, config.getStepsBack(), config.getCandidates(), "stepsBack")) {
                config.setStepsBack(newValue);
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
