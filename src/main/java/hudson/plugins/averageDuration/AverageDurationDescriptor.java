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
    private String candidateName = "Candidates";
    private String stepsBackName = "StepsBack";

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
        FormValidation validateCandidates = checkStuff(candidates, stepsBack, candidateName, config.DEFAULT_CANDIDATES);
        FormValidation validateStepsBack = checkStuff(candidates, stepsBack, stepsBackName, config.DEFAULT_STEPS_BACK);
        if (validateCandidates == FormValidation.ok()) {
            if (validateCandidates != null) {
                config.setDefaultCandidates();
            } else {
                config.setCandidates(Integer.valueOf(candidates));
            }
        }
        if (validateStepsBack == FormValidation.ok()) {
            if (validateStepsBack != null) {
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
        return checkStuff(candidates, stepsBack, candidateName, config.DEFAULT_CANDIDATES);
    }


    // TODO: 2017-10-26 implement the same changes done in doCheckCandidates
    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        return checkStuff(candidates, stepsBack, stepsBackName, config.DEFAULT_STEPS_BACK);
    }

    public FormValidation checkStuff(String candidates, String stepsBack, String targetName, int targetDefaultValue) {
        int newCandidateValue;
        int newStepsBackValue;
        try {
            newCandidateValue = parseInput(candidates, candidateName);
            newStepsBackValue = parseInput(stepsBack, stepsBackName);
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }

        if ((newStepsBackValue == -1 && newCandidateValue == -1))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);

        if ((newCandidateValue == -1 && config.DEFAULT_CANDIDATES <= newStepsBackValue)
                || (newStepsBackValue == -1 && config.DEFAULT_STEPS_BACK >= newCandidateValue))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);

        if (newCandidateValue < config.MIN_FIELD_VALUE || newStepsBackValue < config.MIN_FIELD_VALUE)
            return FormValidation.error("Minimum value is: " + config.MIN_FIELD_VALUE);

        if (newCandidateValue > config.MAX_FIELD_VALUE || newStepsBackValue > config.MAX_FIELD_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_FIELD_VALUE);

        if (newCandidateValue <= newStepsBackValue)
            return FormValidation.ok();
        if (targetName.equalsIgnoreCase(candidateName))
            return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
        else
            return FormValidation.error("Number of steps back must be equal to or greater than number of candidates ");
    }


}
