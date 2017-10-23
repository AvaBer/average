package hudson.plugins.averageDuration.utils;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.averageDuration.AverageDurationSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
public class JobWrapper<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> {
    private static final Logger LOGGER = Logger.getLogger(JobWrapper.class.getName());
    private AverageDurationSettings settings;
    private transient Job<JobT, RunT> job;

    /* ***************** Config ***************** */

    public AverageDurationSettings getSettings() {
        return settings;
    }

    public void setSettings(AverageDurationSettings settings) {
        this.settings = settings;
    }

    private int getCandidates() {
        return settings.getCandidates();
    }

    private int getStepsBack() {
        return settings.getStepsBack();
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
        LOGGER.info("Candidates: " + getCandidates() + " StepsBack: " + getStepsBack());
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


}
