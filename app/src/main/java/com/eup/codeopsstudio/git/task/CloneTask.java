package com.eup.codeopsstudio.git.task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.git.BatchProgressMonitor;
import com.eup.codeopsstudio.git.RepoConfig;
import com.eup.codeopsstudio.git.auth.AuthProvider;
import com.eup.codeopsstudio.git.auth.SshAuthProvider;
import com.eup.codeopsstudio.git.auth.UsernamePasswordAuthProvider;
import com.eup.codeopsstudio.git.listeners.CloneListener;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class CloneTask implements GitTask<Repository> {

    public static final String TAG = "CloneTask";

    private final RepoConfig repoConfig;
    private final CloneListener cloneListener;
    private final BatchProgressMonitor progressMonitor;
    private final String taskName;
    private boolean isTaskSuccessful = false;
    private boolean cloneAllBranches = false;
    private boolean cloneRecursive = false;
    private CloneType cloneType;

    public CloneTask(@NonNull RepoConfig config, @NonNull CloneListener listener) {
        this.repoConfig      = config;
        this.cloneListener   = listener;
        this.progressMonitor = new BatchProgressMonitor(listener, config.getRemoteURI());
        this.taskName        = IdeApplication.getInstance().getString(R.string.msg_git_task_clone);
    }

    @Override
    public Repository call() throws ExecutionException {
        Repository repository = executeClone(createAuthProvider());
        isTaskSuccessful = true;
        cloneListener.onCloneComplete(new File(repoConfig.getLocalURI()));
        return repository;
    }

    @Override
    public void cancel() {
        progressMonitor.cancel();
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.CLONE;
    }

    @Override
    public boolean isCancelled() {
        return progressMonitor.isCancelled();
    }

    @Override
    public boolean isTaskSuccessful() {
        return isTaskSuccessful;
    }

    @Nullable
    private AuthProvider createAuthProvider() {
        return switch (cloneType) {
            case PUBLIC -> null;
            case SSH -> new SshAuthProvider(repoConfig);
            case PRIVATE -> new UsernamePasswordAuthProvider(repoConfig.getAdministrator());
        };
    }

    private Repository executeClone(@Nullable AuthProvider authProvider) throws ExecutionException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(repoConfig.getRemoteURI())
                    .setDirectory(new File(repoConfig.getLocalURI()))
                    .setProgressMonitor(progressMonitor).setCloneAllBranches(cloneAllBranches)
                    .setCloneSubmodules(cloneRecursive);

        if (authProvider != null) {
            authProvider.configureCommand(cloneCommand);
        }

        try {
            return onExecute(cloneCommand);
        } catch (CancellationException e) {
            if (isCancelled()) cleanupRepository();
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_user_invoked_cancellation_error, taskName);
            throw new ExecutionException(msg, e);
        }
    }

    private Repository onExecute(
        @NonNull CloneCommand cloneCommand) throws ExecutionException, CancellationException {
        try (Git git = cloneCommand.call()) {
            var remoteUrl = repoConfig.getRemoteURI();
            var localPath = repoConfig.getLocalURI();
            ILog.debug(TAG, "Cloning from " + remoteUrl + " to " + localPath);
            return git.getRepository();
        } catch (InvalidRemoteException e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_invalid_remote_error, taskName);
            throw new ExecutionException(msg, e);
        } catch (TransportException e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_transport_error, taskName);
            throw new ExecutionException(msg, e);
        } catch (CancellationException e) {
            throw e;
        } catch (CanceledException e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_unexpected_canceled_error,
                                           taskName);
            throw new ExecutionException(msg, e);
        } catch (GitAPIException e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_api_error, taskName);
            throw new ExecutionException(msg, e);
        } catch (JGitInternalException e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_jgit_internal_error, taskName);
            throw new ExecutionException(msg, e);
        } catch (OutOfMemoryError e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_of_memory_error, taskName);
            throw new ExecutionException(msg, e);
        } catch (Exception e) {
            String msg = IdeApplication.getInstance()
                                       .getString(R.string.msg_git_unexpected_error, taskName);
            throw new ExecutionException(msg, e);
        }
    }

    private void cleanupRepository() throws ExecutionException {
        File repoDir = new File(repoConfig.getLocalURI());
        if (repoDir.exists()) {
            try {
                FileUtils.deleteDirectory(repoDir);
                ILog.info(TAG, "Cancelled clone cleaned up successfully");
            } catch (IOException e) {
                ILog.error(TAG, "Cleanup failed: " + e.getMessage());
                String msg = IdeApplication.getInstance()
                                           .getString(R.string.msg_git_delete_stale_repo_error);
                throw new ExecutionException(msg, e);
            }
        }
    }

    public RepoConfig getRepoConfig() {
        return repoConfig;
    }

    public CloneTask setCloneAllBranches(boolean cloneAllBranches) {
        this.cloneAllBranches = cloneAllBranches;
        return this;
    }

    public CloneTask setCloneRecursive(boolean cloneRecursive) {
        this.cloneRecursive = cloneRecursive;
        return this;
    }

    public CloneTask setCloneType(CloneType type) {
        this.cloneType = type;
        return this;
    }

    public enum CloneType {
        PUBLIC,
        PRIVATE,
        SSH
    }
}
