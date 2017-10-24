package hudson.plugins.averageDuration.utils;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.averageDuration.AverageDurationConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
public class JobWrapper<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> {
    private static final Logger LOGGER = Logger.getLogger(JobWrapper.class.getName());
    private AverageDurationConfiguration configuration;
    private transient Job<JobT, RunT> job;

    /* ***************** Config ***************** */

    public AverageDurationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AverageDurationConfiguration configuration) {
        this.configuration = configuration;
    }

    private int getTargetCandidatePool() {
        return configuration.getCandidates();
    }

    private int getTargetNumberOfStepsBack() {
        return configuration.getStepsBack();
    }

    /* *********** Average Build Time *********** */
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
        List<RunT> candidates = new ArrayList<RunT>(getTargetCandidatePool());
        List<RunT> fallbackCandidates = new ArrayList<RunT>(getTargetCandidatePool());
        RunT lastSuccessful = getLastSuccessfulBuild();
        int lastSuccessfulNumber = -1;
        if (lastSuccessful != null) {
            candidates.add(lastSuccessful);
            lastSuccessfulNumber = lastSuccessful.getNumber();
        }
        int i = 0;
        RunT r = getLastBuild();
        while (r != null && candidates.size() < getTargetCandidatePool() && i < getTargetNumberOfStepsBack()) {
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
        while (candidates.size() < 3) {
            if (getTargetCandidatePool() < 3 && candidates.size() == getTargetCandidatePool())
                break;
            if (fallbackCandidates.isEmpty())
                break;
            RunT run = fallbackCandidates.remove(0);
            candidates.add(run);
        }
        return candidates;
    }


}
