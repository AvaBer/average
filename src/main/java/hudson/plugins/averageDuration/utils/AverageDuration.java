package hudson.plugins.averageDuration.utils;

import hudson.Extension;
import hudson.model.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class AverageDuration<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> implements Describable<AverageDuration<JobT, RunT>> {
    private static final Logger LOGGER = Logger.getLogger(AverageDuration.class.getName());


    public AverageDuration(Job<JobT, RunT> job) {
        this.job = job;
    }

    /* ***************** Config ***************** */

    private int candidates = 3;
    private int stepsBack = 6;
    public final int DEFAULT_NUMBER_OF_CANDIDATES = 3;
    public final int DEFAULT_NUMBER_OF_STEPS_BACK = 6;

    public void setCandidates(String candidates) {
        if (isEmpty(candidates)) {
            this.candidates = DEFAULT_NUMBER_OF_CANDIDATES;
        } else {
            try {
                this.candidates = parseInt(candidates) > this.stepsBack ? this.stepsBack : parseInt(candidates);
            } catch (NumberFormatException nfe) {
                this.candidates = DEFAULT_NUMBER_OF_CANDIDATES;
            }
        }
    }

    public void setStepsBack(String stepsBack) {
        if (isEmpty(stepsBack)) {
            this.stepsBack = DEFAULT_NUMBER_OF_STEPS_BACK;
        } else {
            try {
                this.stepsBack = parseInt(stepsBack) < this.candidates ? this.candidates : parseInt(stepsBack);
            } catch (NumberFormatException nfe) {
                this.candidates = DEFAULT_NUMBER_OF_STEPS_BACK;
            }
        }
    }

    public int getCandidates() {
        return candidates;
    }

    public int getStepsBack() {
        return stepsBack;
    }

    /* *********** Average Build Time *********** */

    private final Job<JobT, RunT> job;

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
        List<RunT> candidates = new ArrayList<RunT>(6);
        List<RunT> fallbackCandidates = new ArrayList<RunT>(6);
        RunT lastSuccessful = getLastSuccessfulBuild();
        int lastSuccessfulNumber = -1;
        if (lastSuccessful != null) {
            candidates.add(lastSuccessful);
            lastSuccessfulNumber = lastSuccessful.getNumber();
        }
        int i = 0;
        RunT r = getLastBuild();
        while (r != null && candidates.size() < 6 && i < 12) {
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

    @Override
    public Descriptor<AverageDuration<JobT, RunT>> getDescriptor() {
        return null;
    }

    @Extension(optional = true)
    public static class DescriptorImpl<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Descriptor<AverageDuration<JobT, RunT>> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return super.getDisplayName();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return super.configure(req, json);
        }
    }
}

