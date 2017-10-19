package hudson.plugins.averageDuration.utils;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JobWrapper<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> {
    private static final Logger LOGGER = Logger.getLogger(JobWrapper.class.getName());
    private final Job<JobT, RunT> job;
    private int candidates = 3;
    private int stepsBack = 6;


    public JobWrapper(Job<JobT,RunT> job) {
        this.job = job;
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

    private RunT getLastSuccessfulBuild() {
        return this.job.getLastSuccessfulBuild();
    }

    private RunT getLastBuild() {
        return this.job.getLastBuild();
    }
}

