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

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isEmpty;

@SuppressWarnings("Duplicates")
@Extension
public class AverageDurationDescriptor extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(AverageDurationDescriptor.class.getName());
    private AverageDurationConfiguration config = new AverageDurationConfiguration();

    public AverageDurationConfiguration getConfig() {
        return config;
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
        return false;
    }


    /* *** Form validation stuffs *** */
    public FormValidation doCheckCandidates(@QueryParameter String candidates, @QueryParameter String stepsBack) {
        return checkFields(candidates, stepsBack, Messages.CandidatesName(), config.DEFAULT_CANDIDATES);
    }

    public FormValidation doCheckStepsBack(@QueryParameter String stepsBack, @QueryParameter String candidates) {
        return checkFields(candidates, stepsBack, Messages.StepsBackName(), config.DEFAULT_STEPS_BACK);
    }

    public FormValidation checkFields(String candidatesStr, String stepsBackStr, String target, int defaultVal) {
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
