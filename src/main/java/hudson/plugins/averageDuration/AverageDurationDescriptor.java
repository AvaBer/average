package hudson.plugins.averageDuration;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isEmpty;

@SuppressWarnings("Duplicates")
@Extension
public class AverageDurationDescriptor extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(AverageDurationDescriptor.class.getName());
    private AverageDurationConfiguration config = new AverageDurationConfiguration();
    private String candidateName = "Candidates";
    private String stepsBackName = "StepsBack";
    private final Map<String, String> requirementError = ImmutableMap.of(
            candidateName, "Number of candidates must be equal to or smaller than number of steps back ",
            stepsBackName, "Number of steps back must be equal to or greater than number of candidates ");

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
                config.setCandidates(valueOf(candidates));
            }
        }
        if (validateStepsBack.kind == FormValidation.Kind.OK) {
            if (validateStepsBack.toString().contains("default")) {
                config.setDefaultStepsBack();
            } else {
                config.setStepsBack(valueOf(stepsBack));
            }
        }
        if (FormApply.isApply(req))
            save();
        save();
        return false;
    }


    /* *** Form validation stuffs *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates, @QueryParameter String stepsBack) {
        return checkStuff(candidates, stepsBack, candidateName, config.DEFAULT_CANDIDATES);
    }


    // TODO: 2017-10-26 implement the same changes done in doCheckCandidates
    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        return checkStuff(candidates, stepsBack, stepsBackName, config.DEFAULT_STEPS_BACK);
    }

    private int parseInputWithName(String input, String name) throws NumberFormatException {
        if (isEmpty(input.trim()))
            return -1;
        try {
            return parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The value of: " + name + ", must be a number or empty");
        }
    }

    public FormValidation checkStuff(String candidates, String stepsBack, String target, int targetDefaultValue) {
        int candidateValue;
        int stepsBackValue;
        try {
            candidateValue = parseInputWithName(candidates, candidateName);
            stepsBackValue = parseInputWithName(stepsBack, stepsBackName);
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }

        if ((stepsBackValue == -1 && candidateValue == -1))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);


        if ((target.equals(candidateName) && candidateValue == -1 && config.DEFAULT_CANDIDATES <= stepsBackValue) ||
                (target.equals(stepsBackName) && stepsBackValue == -1 && config.DEFAULT_STEPS_BACK >= candidateValue))
            return FormValidation.ok("Using default value of: " + targetDefaultValue);

        candidateValue = candidateValue == -1 ? config.DEFAULT_CANDIDATES : candidateValue;
        stepsBackValue = stepsBackValue == -1 ? config.DEFAULT_STEPS_BACK : stepsBackValue;

        if (target.equals(candidateName) && candidateValue < config.MIN_VALUE ||
                target.equals(stepsBackName) && stepsBackValue < config.MIN_VALUE)
            return FormValidation.error("Minimum value is: " + config.MIN_VALUE);

        if (target.equals(candidateName) && candidateValue > config.MAX_VALUE ||
                target.equals(stepsBackName) && stepsBackValue > config.MAX_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_VALUE);

        if (candidateValue <= stepsBackValue)
            return FormValidation.ok();
        return FormValidation.error(requirementError.get(target));
    }

    public FormValidation checkStuffs(String candidatesStr, String stepsBackStr, String target, int defaultVal) {
        int candidates, stepsBack;
        FormValidation okTargetDefaultValueMessage = FormValidation.ok("Using default value of: " + defaultVal);
        try {
            candidates = checkInputIntOrExceptionWithMessage(candidatesStr, candidateName);
            stepsBack = checkInputIntOrExceptionWithMessage(stepsBackStr, stepsBackName);
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }
        if (((candidates == stepsBack) && candidates == -1) ||
                (target.equals(candidateName) && candidates == -1 && defaultVal <= stepsBack) ||
                (target.equals(stepsBackName) && stepsBack == -1 && defaultVal >= candidates))
            return FormValidation.ok("Using default value of: " + defaultVal);
        // Check if value is lower than MIN_VALUE but not -1 which means default
        if (target.equals(candidateName) && candidates < config.MIN_VALUE && candidates != -1||
                target.equals(stepsBackName) && stepsBack < config.MIN_VALUE && stepsBack != -1)
            return FormValidation.error("Minimum value is: " + config.MIN_VALUE);

        if (target.equals(candidateName) && candidates > config.MAX_VALUE ||
                target.equals(stepsBackName) && stepsBack > config.MAX_VALUE)
            return FormValidation.error("Maximum value is: " + config.MAX_VALUE);

        if (candidates <= stepsBack)
            return FormValidation.ok();
        return FormValidation.error(requirementError.get(target));
    }

    private Integer valOf(String s) {
        return valueOf(s);
    }

    private Integer valOf(int i) {
        return valueOf(i);
    }

    private int checkInputIntOrExceptionWithMessage(String input, String name) throws NumberFormatException {
        if (isEmpty(input.trim()))
            return -1;
        try {
            return parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The value of: " + name + ", must be a number or empty");
        }
    }
}
