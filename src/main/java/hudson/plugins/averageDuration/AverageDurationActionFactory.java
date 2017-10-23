package hudson.plugins.averageDuration;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

@Extension
public class AverageDurationActionFactory extends TransientActionFactory<Job> {
    @Override
    public Class<Job> type() {
        return Job.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Job target) {
        return Collections.singletonList(new AverageDurationAction(target));
    }
}
