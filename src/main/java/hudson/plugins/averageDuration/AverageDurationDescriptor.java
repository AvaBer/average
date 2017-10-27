package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.util.FormApply;
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
    private AverageDurationConfiguration config = new AverageDurationConfiguration();
    private String candidateName = "Candidates";
    private String stepsBackName = "StepsBack";

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
        LOGGER.info(json.toString(4));
        if (validateCandidates.kind == FormValidation.Kind.OK) {
            if (validateCandidates.toString().contains("default")) {
                config.setDefaultCandidates();
            } else {
                config.setCandidates(Integer.valueOf(candidates));
            }
        }
        if (validateStepsBack.kind == FormValidation.Kind.OK) {
            if (validateStepsBack.toString().contains("default")) {
                config.setDefaultStepsBack();
            } else {
                config.setStepsBack(Integer.valueOf(stepsBack));
            }
        }
        if (FormApply.isApply(req))
            save();
        save();
        return false;
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
        int candidateValue;
        int stepsBackValue;
        try {
            candidateValue = parseInput(candidates, candidateName);
            stepsBackValue = parseInput(stepsBack, stepsBackName);
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }

        if ((stepsBackValue == -1 && candidateValue == -1))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);
        if ((targetName.equals(candidateName) && candidateValue == -1
                && config.DEFAULT_CANDIDATES <= stepsBackValue) ||
                (targetName.equals(stepsBackName) && stepsBackValue == -1
                        && config.DEFAULT_STEPS_BACK >= candidateValue))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);

        candidateValue = candidateValue == -1 ? config.DEFAULT_CANDIDATES : candidateValue;
        stepsBackValue = stepsBackValue == -1 ? config.DEFAULT_STEPS_BACK : stepsBackValue;
        if (targetName.equals(candidateName) && candidateValue < config.MIN_FIELD_VALUE ||
                targetName.equals(stepsBackName) && stepsBackValue < config.MIN_FIELD_VALUE)
            return FormValidation.error("Minimum value is: " + config.MIN_FIELD_VALUE);

        if (targetName.equals(candidateName) && candidateValue > config.MAX_FIELD_VALUE ||
                targetName.equals(stepsBackName) && stepsBackValue > config.MAX_FIELD_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_FIELD_VALUE);

        if (candidateValue <= stepsBackValue)
            return FormValidation.ok();
        if (targetName.equals(candidateName))
            return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
        else
            return FormValidation.error("Number of steps back must be equal to or greater than number of candidates ");
    }


}
