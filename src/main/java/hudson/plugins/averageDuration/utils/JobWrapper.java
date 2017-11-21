package hudson.plugins.averageDuration.utils;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.averageDuration.AverageDurationConfiguration;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class JobWrapper<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> {
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

    /**
     * Sums up the duration of all candidate builds then divides them by the size of the list.<p>
     * This is a modified version of the method in the {@link Job} class.
     * @return Average duration in milliseconds
     */
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

    /**
     * Returns a set number of candidate builds for calculating the average build time of a job,
     * the amount is set in the jenkins global configuration page.
     * <p>
     * This is a configurable version of the method in the {@link Job} class.
     * <p>
     * If the list contains less than 3 candidates, then up to 3 fallback candidates (unsuccessful builds)
     * will be appended to it, unless a lower value is set then that will be the goal.
     * <p>
     * the fallback candidates are never aborted builds.
     * @return ArrayList of candidates, empty if no builds exist.
     */
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
                if (result != null) {
                    if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
                        candidates.add(r);
                    } else if (result.isCompleteBuild()) {
                        fallbackCandidates.add(r);
                    }
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
