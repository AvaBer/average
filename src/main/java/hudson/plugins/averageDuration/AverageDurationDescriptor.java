package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Extension
public class AverageDurationDescriptor extends GlobalConfiguration {
    private AverageDurationConfiguration config = new AverageDurationConfiguration();

    public AverageDurationConfiguration getConfig() {
        return new AverageDurationConfiguration(config);
    }

    public AverageDurationDescriptor() {
        load();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return Messages.DisplayName();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        String candidates = json.getString("candidates");
        String stepsBack = json.getString("stepsBack");
        FormValidation vC = checkFields(candidates, stepsBack, Messages.CandidatesName(), config.DEFAULT_CANDIDATES);
        FormValidation vS = checkFields(candidates, stepsBack, Messages.StepsBackName(), config.DEFAULT_STEPS_BACK);
        if (vC.kind == FormValidation.Kind.OK) {
            if (vC.toString().contains("default"))
                config.setDefaultCandidates();
            else
                config.setCandidates(valueOf(candidates));
        }
        if (vS.kind == FormValidation.Kind.OK) {
            if (vS.toString().contains("default"))
                config.setDefaultStepsBack();
            else
                config.setStepsBack(valueOf(stepsBack));
        }
        config.setShowOnJobPage(json.getBoolean("showOnJobPage"));
        save();
        return true;
    }

    /* *** FormValidation used in config.jelly *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates, @QueryParameter String stepsBack) {
        return checkFields(candidates, stepsBack, Messages.CandidatesName(), config.DEFAULT_CANDIDATES);
    }

    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        return checkFields(candidates, stepsBack, Messages.StepsBackName(), config.DEFAULT_STEPS_BACK);
    }

    /**
     * Used by the doCheck methods validate the input on the configuration page and to save the input in configure()
     * <p>
     * Combined logic for both fields because they depend on each other.
     * @param candidatesStr the amount of candidates to be used in the average duration pool
     * @param stepsBackStr  the number of steps back in the build history to find candidates
     * @param target        the name of the field is being checked
     * @param defaultVal    the default value of the target, used for feedback if the default value will be used.
     * @return multiple returns:<p>
     * Errors if the input cannot be parsed to an integer or fails to meet the requirement of its counterpart<br>
     * OK for a valid input<br>
     * OK with message if the default value will be used (checked on "save" to use the default value in configure())
     */
    private FormValidation checkFields(String candidatesStr, String stepsBackStr, String target, int defaultVal) {
        int candidates, stepsBack;
        try {
            candidates = checkInputIntOrException(candidatesStr, Messages.CandidatesName(), config.DEFAULT_CANDIDATES);
            stepsBack = checkInputIntOrException(stepsBackStr, Messages.StepsBackName(), config.DEFAULT_STEPS_BACK);
        } catch (NumberFormatException nfe) {
            return FormValidation.error(nfe.getMessage());
        }
        if ((candidates == config.DEFAULT_CANDIDATES && stepsBack == config.DEFAULT_STEPS_BACK) ||
                (target.equals(Messages.CandidatesName()) &&
                        candidates == config.DEFAULT_CANDIDATES && defaultVal <= stepsBack) ||
                (target.equals(Messages.StepsBackName()) &&
                        stepsBack == config.DEFAULT_STEPS_BACK && defaultVal >= candidates))
            return FormValidation.ok(Messages.UseDefaultValueMessage(defaultVal));

        if (target.equals(Messages.CandidatesName()) && candidates > config.MAX_VALUE ||
                target.equals(Messages.StepsBackName()) && stepsBack > config.MAX_VALUE)
            return FormValidation.error(Messages.MaxValueMessage(config.MAX_VALUE));

        if (candidates <= stepsBack)
            return FormValidation.ok();
        return FormValidation.error(target.equals(Messages.CandidatesName()) ?
                Messages.Candidates_Req() : Messages.StepsBack_Req());
    }

    /**
     * Parses the input if possible, returns the default value of the field if the input is empty
     * or if the value is less than 1. <br>
     * Throws an exception with a message if the field cannot be parsed.
     */
    private int checkInputIntOrException(String input, String name, int defaultValue)
            throws NumberFormatException {
        try {
            if (isEmpty(input.trim()) || valueOf(input) < config.MIN_VALUE)
                return defaultValue;
            return parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(Messages.InvalidInput(name));
        }
    }
}
