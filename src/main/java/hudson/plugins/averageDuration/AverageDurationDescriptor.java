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
        if (doCheckCandidates(candidates, stepsBack) == FormValidation.ok()) {
            if (doCheckCandidates(candidates, stepsBack).getMessage() != null) {
                config.setDefaultCandidates();
            } else {
                config.setCandidates(Integer.valueOf(candidates));
            }
        }
        if (doCheckStepsBack(stepsBack, candidates) == FormValidation.ok()) {
            if (doCheckStepsBack(stepsBack, candidates).getMessage() != null) {
                config.setDefaultStepsBack();
            } else {
                config.setStepsBack(Integer.valueOf(stepsBack));
            }
        }
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

    private int parseInput(String input, String name) throws NumberFormatException {
        if (isEmpty(input.trim()))
            return -1;
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The value of: " + name + ", must be a number or empty");
        }
    }

    /* *** Form validation stuffs *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates, @QueryParameter String stepsBack) {
        int newCandidates;
        int newStepsBack;
        try {
            newCandidates = parseInput(candidates, "Candidates");
            newStepsBack = parseInput(stepsBack, "StepsBack");
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }
        if ((newCandidates == -1 && config.DEFAULT_NUMBER_OF_CANDIDATES <= newStepsBack) || (newStepsBack == -1 && newCandidates == -1))
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_CANDIDATES);
        if (newStepsBack == -1 && newCandidates <= config.DEFAULT_NUMBER_OF_STEPS_BACK)
            return FormValidation.ok();
        if (newCandidates < config.MIN_FIELD_VALUE)
            return FormValidation.error("Minimum value is: " + config.MIN_FIELD_VALUE);
        if (newCandidates > config.MAX_FIELD_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_FIELD_VALUE);

        if (newCandidates <= newStepsBack)
            return FormValidation.ok();

        return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
    }

    // TODO: 2017-10-26 implement the same changes done in doCheckCandidates
    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        int newStepsBack;
        int newCandidates;
        try {
            newStepsBack = parseInput(stepsBack, "StepsBack");
            newCandidates = parseInput(candidates, "Candidates");
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }
        if (newStepsBack == -1 && config.DEFAULT_NUMBER_OF_STEPS_BACK >= newCandidates)
            return FormValidation.ok("Using default value of: " + config.DEFAULT_NUMBER_OF_STEPS_BACK);

        if (newStepsBack < config.MIN_FIELD_VALUE)
            return FormValidation.error("Minimum value is: " + config.MIN_FIELD_VALUE);
        if (newStepsBack > config.MAX_FIELD_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_FIELD_VALUE);

        if (newCandidates <= newStepsBack)
            return FormValidation.ok();

        return FormValidation.error("Number of steps back must be equal to or greater than number of candidates ");
    }

}
