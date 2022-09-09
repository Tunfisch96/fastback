package net.pcal.gitback.tasks;

import net.pcal.gitback.FileUtils;
import net.pcal.gitback.GitUtils;
import net.pcal.gitback.Loggr;
import net.pcal.gitback.WorldConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.pcal.gitback.BranchNameUtils.getSnapshotBranchName;
import static net.pcal.gitback.WorldConfig.WORLD_UUID_PATH;

@SuppressWarnings("FieldCanBeLocal")
public class RestoreSnapshotTask extends Task {

    private final Path worldSaveDir;
    private final String snapshotName;
    private final String worldName;
    private final Path saveDir;
    private final TaskListener taskListener;
    private final boolean doClean = true;
    private final Loggr logger;

    public static Runnable create(Path worldSaveDir,
                                  String snapshotName,
                                  String worldName,
                                  Path saveDir,
                                  TaskListener tl,
                                  Loggr logger) {
        return new RestoreSnapshotTask(worldSaveDir, snapshotName, worldName, saveDir, tl, logger);
    }

    private RestoreSnapshotTask(Path worldSaveDir, String snapshotName, String worldName, Path saveDir, TaskListener tl, Loggr logger) {
        this.worldSaveDir = requireNonNull(worldSaveDir);
        this.snapshotName = requireNonNull(snapshotName);
        this.worldName = requireNonNull(worldName);
        this.saveDir = requireNonNull(saveDir);
        this.taskListener = requireNonNull(tl);
        this.logger = requireNonNull(logger);
    }

    public void run() {
        setStarted();
        final WorldConfig config;
        final String branchName;
        try (final Git git = Git.open(this.worldSaveDir.toFile())) {
            config = WorldConfig.load(worldSaveDir, git.getRepository().getConfig());
            branchName = getSnapshotBranchName(config.worldUuid(), this.snapshotName);
            if (!GitUtils.isBranchExtant(git, branchName, logger)) {
                taskListener.error("No such snapshot " + snapshotName);
                return;
            }
        } catch (IOException | GitAPIException e) {
            this.taskListener.internalError();
            logger.error("Unexpected error looking up branch names", e);
            setFailed();
            return;
        }

        final Path targetDirectory;
        try {
            targetDirectory = getTargetDir(this.saveDir, worldName, snapshotName);
            String uri = "file://" + this.worldSaveDir.toAbsolutePath();
            taskListener.feedback("Restoring " + this.snapshotName + " to\n" + targetDirectory);
            try (Git git = Git.cloneRepository().setDirectory(targetDirectory.toFile()).
                    setBranchesToClone(List.of("refs/heads/" + branchName)).setBranch(branchName).setURI(uri).call()) {
            }
        } catch (Exception e) {
            this.taskListener.internalError();
            logger.error("Restoration clone of " + branchName + " failed.", e);
            setFailed();
            return;
        }
        if (config.isPostRestoreCleanupEnabled()) {
            try {
                FileUtils.rmdir(targetDirectory.resolve(".git"));
                FileUtils.rmdir(targetDirectory.resolve(WORLD_UUID_PATH));
            } catch (IOException e) {
                this.taskListener.error("Restoration finished but an unexpected error " +
                        "occurred during cleanup.  See log for details.");
                logger.error("Unexpected error cleaning restored snapshot", e);
                setFailed();
                return;
            }
        }
        taskListener.feedback("Restoration complete");
        setCompleted();
    }

    private static Path getTargetDir(Path saveDir, String worldName, String snapshotName) {
        worldName = worldName.replaceAll("\\W+", ""); // strip out all non-word characters for safety
        Path base = saveDir.resolve(worldName + "-" + snapshotName);
        Path candidate = base;
        int i = 0;
        while (candidate.toFile().exists()) {
            i++;
            candidate = Path.of(base + "_" + i);
            if (i > 1000) {
                throw new IllegalStateException("wat i = " + i);
            }
        }
        return candidate;

    }
}