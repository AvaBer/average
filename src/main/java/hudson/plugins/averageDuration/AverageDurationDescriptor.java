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
    /**
     * The maximum value for both fields, if a higher value is set this will override it.
     */
    private final int MAX_VALUE = 50;

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
        String candidates = json.getString("candidates");
        String stepsBack = json.getString("stepsBack");
        this.config.setCandidates(parseInput(candidates, config.DEFAULT_NUMBER_OF_CANDIDATES));
        this.config.setStepsBack(parseInput(stepsBack, config.DEFAULT_NUMBER_OF_STEPS_BACK));
        save();
        return false;
    }

    private int parseInput(String input, int defaultValue) {
        if (isEmpty(input.trim()))
            return defaultValue;
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /* *** Form validation stuffs *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates, @QueryParameter String stepsBack) {
        if (isEmpty(candidates.trim())) {
            config.setCandidates(config.DEFAULT_NUMBER_OF_CANDIDATES);
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_CANDIDATES);
        } else {
            int newCandidates;
            int newStepsBack;
            try {
                newCandidates = Integer.parseInt(candidates.trim());
                newStepsBack = isEmpty(stepsBack.trim())
                        ? config.DEFAULT_NUMBER_OF_STEPS_BACK : Integer.parseInt(stepsBack.trim());
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Value must be a number");
            }
            if (newCandidates < 1)
                return FormValidation.error("Value cannot be set to zero");
            if (checkValues(newCandidates, config.getCandidates(), newStepsBack, "candidates")) {
                config.setCandidates(newCandidates);
                return FormValidation.ok();
            }
            return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
        }
    }

    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        if (isEmpty(stepsBack.trim())) {
            config.setStepsBack(config.DEFAULT_NUMBER_OF_STEPS_BACK);
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_STEPS_BACK);
        } else {
            int newStepsBack;
            int newCandidates;
            try {
                newStepsBack = Integer.parseInt(stepsBack.trim());
                newCandidates = isEmpty(candidates.trim())
                        ? config.DEFAULT_NUMBER_OF_CANDIDATES : Integer.parseInt(candidates.trim());
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Value must be a number");
            }
            if (newStepsBack < 1)
                return FormValidation.error("Value cannot be set to zero");
            if (checkValues(newStepsBack, config.getStepsBack(), newCandidates, "stepsBack")) {
                config.setStepsBack(newStepsBack);
                return FormValidation.ok();
            }
            return FormValidation.error("Number of steps back must be equal to or greater than number of candidates ");
        }
    }

    private boolean checkValues(int newValue, int oldValue, int counterpart, String targetVariable) {
        if (newValue == oldValue)
            return true;
        switch (targetVariable) {
            case "candidates":
                return newValue <= counterpart;
            case "stepsBack":
                return newValue >= counterpart;
            default:
                return false;
        }
    }


}
