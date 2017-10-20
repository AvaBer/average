package hudson.plugins.averageDuration.utils;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.*;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isEmpty;

@SuppressWarnings("WeakerAccess")
public class AverageDuration<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>>
        extends Plugin implements Describable<AverageDuration<JobT, RunT>> {
    private static final Logger LOGGER = Logger.getLogger(AverageDuration.class.getName());

    /* ******** AverageDuration Instance ******** */
    private static AverageDuration instance = null;

    public AverageDuration() {
        instance = this;
    }

    public static AverageDuration getInstance() {
        return instance;
    }

//    public AverageDuration(Job<JobT, RunT> job) {
//        this.job = job;
//    }

    /* *************** PluginImpl *************** */
    @Override
    public void start() throws Exception {
        super.start();
        load();
    }

    /* ***************** Config ***************** */
    private int candidates = 3;
    private int stepsBack = 6;
    public final int DEFAULT_NUMBER_OF_CANDIDATES = 3;
    public final int DEFAULT_NUMBER_OF_STEPS_BACK = 6;

    public void setCandidates(int candidates) {
        this.candidates = candidates;
    }

    public void setStepsBack(int stepsBack) {
        this.stepsBack = stepsBack;
    }

    public int getCandidates() {
        return candidates;
    }

    public int getStepsBack() {
        return stepsBack;
    }

    /* *********** Average Build Time *********** */

    private Job<JobT, RunT> job;

    public void setJob(Job<JobT, RunT> job) {
        this.job = job;
    }

    private RunT getLastSuccessfulBuild() {
        return this.job.getLastSuccessfulBuild();
    }

    private RunT getLastBuild() {
        return this.job.getLastBuild();
    }

    public long getEstimatedDuration() {
        List<RunT> builds = getEstimatedDurationCandidates();
        if (builds.isEmpty()) return -1;
        long totalDuration = 0;
        for (RunT b : builds) {
            totalDuration += b.getDuration();
        }
        if (totalDuration == 0) return -1;
        return Math.round((double) totalDuration / builds.size());
    }

    private List<RunT> getEstimatedDurationCandidates() {
        List<RunT> candidates = new ArrayList<RunT>(getCandidates());
        List<RunT> fallbackCandidates = new ArrayList<RunT>(getCandidates());
        RunT lastSuccessful = getLastSuccessfulBuild();
        int lastSuccessfulNumber = -1;
        if (lastSuccessful != null) {
            candidates.add(lastSuccessful);
            lastSuccessfulNumber = lastSuccessful.getNumber();
        }
        int i = 0;
        RunT r = getLastBuild();
        while (r != null && candidates.size() < getCandidates() && i < getStepsBack()) {
            if (!r.isBuilding() && r.getResult() != null && r.getNumber() != lastSuccessfulNumber) {
                Result result = r.getResult();
                if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
                    candidates.add(r);
                } else if (result.isCompleteBuild()) {
                    fallbackCandidates.add(r);
                }
            }
            i++;
            r = r.getPreviousBuild();
        }
        while (candidates.size() < 6) {
            if (fallbackCandidates.isEmpty())
                break;
            RunT run = fallbackCandidates.remove(0);
            candidates.add(run);
        }
        return candidates;
    }

    /* ******* AverageDuration Descriptor ******* */
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<AverageDuration<JobT, RunT>> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension(optional = true)
    public static class DescriptorImpl<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Descriptor<AverageDuration<JobT, RunT>> {
        private AverageDuration avg = AverageDuration.getInstance();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Average Build Time";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(AverageDuration.getInstance());
            save();
            return super.configure(req, json);
        }

        public FormValidation doCheckCandidates(@QueryParameter String candidates) {
            if (isEmpty(candidates.trim())) {
                avg.setCandidates(avg.DEFAULT_NUMBER_OF_CANDIDATES);
                return FormValidation.ok("Using default value of: " + getInstance().DEFAULT_NUMBER_OF_CANDIDATES);
            } else {
                int newValue;
                try {
                    newValue = Integer.parseInt(candidates.trim());
                } catch (NumberFormatException nfe) {
                    return FormValidation.error("Value must be a number");
                }
                if (newValue < 1)
                    return FormValidation.error("Value cannot be set to zero");
                if (checkValues(newValue, avg.getCandidates(), avg.getStepsBack(), "candidates")) {
//                    avg.setCandidates(newValue);
                    return FormValidation.ok();
                }
                return FormValidation.error("Number of candidates must be equal to or smaller than number of steps back ");
            }
        }

        public FormValidation doCheckStepsBack(@QueryParameter String stepsBack) {
            if (isEmpty(stepsBack.trim())) {
                avg.setCandidates(avg.DEFAULT_NUMBER_OF_STEPS_BACK);
                return FormValidation.ok("Using default value of: " + getInstance().DEFAULT_NUMBER_OF_STEPS_BACK);
            } else {
                int newValue;
                try {
                    newValue = Integer.parseInt(stepsBack.trim());
                } catch (NumberFormatException nfe) {
                    return FormValidation.error("Value must be a number");
                }
                if (newValue < 1)
                    return FormValidation.error("Value cannot be set to zero");
                if (checkValues(newValue, avg.getStepsBack(), avg.getCandidates(), "stepsBack")) {
//                    avg.setStepsBack(stepsBack);
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

