package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/** the gitlet class.
 * @author Vikram Cherukuri */
public class Gitlet implements Serializable {

    /** the head file. */
    private File head;

    /** the current directory string. */
    private String currentDir;

    /** the dir file. */
    private File dir;

    /** the commits file. */
    private File commits;

    /** the branches file. */
    private File branches;

    /** the headBranch file. */
    private File headBranch;

    /** the staged files. */
    private File stagedFiles;

    /** the removed files. */
    private File removedFiles;

    /** is merge conflicted? */
    private boolean isConflict;

    /** constructor. */
    public Gitlet() {
        currentDir = System.getProperty("user.dir");
        dir = Utils.join(currentDir, ".gitlet");
        head = Utils.join(dir, ".headCommit");
        commits = Utils.join(dir, ".commits");
        branches = Utils.join(dir, ".branches");
        headBranch = Utils.join(dir, ".currBranch");
        stagedFiles = Utils.join(dir, ".staged");
        removedFiles = Utils.join(dir, ".removed");
        isConflict = false;
    }

    /** initializes. */
    public void initialize() {
        if (!dir.exists()) {
            dir.mkdir();
            commits.mkdir();
            branches.mkdir();
            stagedFiles.mkdir();
            head.mkdir();
            removedFiles.mkdir();
            File masterBranch = Utils.join(branches, "master");
            Commit first = new Commit(
                    "initial commit", "",
                    "", "", false);
            byte[] serial = Utils.serialize(first);
            File serialFile = Utils.join(
                    commits, Utils.sha1(serial));
            serialFile.mkdir();
            Utils.writeContents(Utils.join(serialFile,
                    ".serial"), serial);
            File filesFile = Utils.join(serialFile, ".files");
            filesFile.mkdir();
            File headCommitSHA = Utils.join(head, Utils.sha1(serial));
            Utils.writeContents(headCommitSHA, Utils.sha1(serial));
            Utils.writeContents(masterBranch, Utils.sha1(serial));
            Utils.writeContents(headBranch, "master");
        } else {
            System.out.println("A gitlet version control "
                    + "system already exists in the current directory.");
        }
    }

    /** commits.
     * @param message the message of the commit.
     * @param curr the current branch head SHA, if isMerged.
     * @param given the given branch head SHA, if isMerged.
     * @param isMerge whether or not the commit is a merge commit. */
    public void commit(String message,
                       String curr, String given, boolean isMerge) {
        if (Utils.plainFilenamesIn(stagedFiles).isEmpty()
                && Utils.plainFilenamesIn(removedFiles).isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.equals("") || message.isEmpty()
                || message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit c = new Commit(message,
                Utils.plainFilenamesIn(head).get(0), curr, given, isMerge);
        byte[] serial = Utils.serialize(c);
        Utils.writeContents(Utils.join(branches,
                Utils.readContentsAsString(headBranch)), Utils.sha1(serial));
        File currCommit = Utils.join(commits, Utils.sha1(serial));
        currCommit.mkdir();
        Utils.writeContents(Utils.join(currCommit, ".serial"), serial);
        File currCommitFiles = Utils.join(currCommit, ".files");
        currCommitFiles.mkdir();
        List<String> total;
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        total = Utils.plainFilenamesIn(Utils.join(
                commits, headCommitSha, ".files"));
        for (String file: total) {
            Utils.writeContents(Utils.join(currCommitFiles, file),
                    Utils.readContents(Utils.join(
                            commits, headCommitSha, ".files", file)));
        }
        for (String sfile: Utils.plainFilenamesIn(stagedFiles)) {
            Utils.writeContents(Utils.join(currCommitFiles, sfile),
                    Utils.readContents(Utils.join(stagedFiles, sfile)));
        }
        for (String remove: Utils.plainFilenamesIn(removedFiles)) {
            Utils.join(currCommitFiles, remove).delete();
        }
        Utils.join(head, headCommitSha).delete();
        File newHead = Utils.join(head, Utils.sha1(serial));
        Utils.writeContents(newHead, Utils.sha1(serial));
        removeStagedRemovedFiles();
    }

    /** quick access to currDir.
     * @return current directory string. */
    public String getCurrentDir() {
        return currentDir;
    }

    /** clears staged and removed. */
    private void removeStagedRemovedFiles() {
        for (String stage: Utils.plainFilenamesIn(stagedFiles)) {
            Utils.join(stagedFiles, stage).delete();
        }
        for (String removed: Utils.plainFilenamesIn(removedFiles)) {
            Utils.join(removedFiles, removed).delete();
        }
    }

    /** adds a file.
     * @param filename name of the to-be-added file. */
    public void addFile(String filename) {
        File f = Utils.join(currentDir, filename);
        File removed = Utils.join(removedFiles, filename);
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        if (!f.exists() && !removed.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (Utils.plainFilenamesIn(Utils.join(
                commits, headCommitSha, ".files")).contains(filename)) {
            if (f.exists() && filesEqual(Utils.join(currentDir, filename),
                    Utils.join(commits, headCommitSha, ".files", filename))) {
                if (Utils.plainFilenamesIn(stagedFiles).contains(filename)) {
                    Utils.join(stagedFiles, filename).delete();
                }
                return;
            } else if (Utils.plainFilenamesIn(
                    removedFiles).contains(filename)) {
                Utils.writeContents(Utils.join(currentDir, filename),
                        Utils.readContents(Utils.join(removedFiles, filename)));
                Utils.join(removedFiles, filename).delete();
                return;
            } else {
                Utils.writeContents(Utils.join(stagedFiles, filename),
                        Utils.readContents(f));
            }
        } else {
            Utils.writeContents(Utils.join(stagedFiles, filename),
                    Utils.readContents(f));
        }
    }

    /** determines if branch is untracked.
     * @param branch branch name.
     * @return is the branch checkout overwriting unsaved changes? */
    private boolean untrackedFiles(String branch) {
        List<String> currentFiles = Utils.plainFilenamesIn(currentDir);
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        List<String> prevFiles = Utils.plainFilenamesIn(
                Utils.join(commits, headCommitSha, ".files"));
        String branchCommitSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        List<String> branchCommitFiles = Utils.plainFilenamesIn(
                Utils.join(commits, branchCommitSha, ".files"));
        boolean untracked = false;
        for (String name: branchCommitFiles) {
            if (currentFiles.contains(name)
                    && !filesEqual(Utils.join(currentDir, name),
                    Utils.join(commits, branchCommitSha, ".files", name))
                    && !prevFiles.contains(name)) {
                untracked = true;
            }
            if (currentFiles.contains(name)
                    && prevFiles.contains(name)
                    && !filesEqual(Utils.join(currentDir, name),
                    Utils.join(Utils.join(
                            commits, headCommitSha, ".files", name)))) {
                untracked = true;
            }
        }
        for (String file: currentFiles) {
            if (!branchCommitFiles.contains(file)
                    && prevFiles.contains(file)
                    && !filesEqual(Utils.join(currentDir, file),
                    Utils.join(commits, headCommitSha, ".files", file))) {
                untracked = true;
            }
            if (Utils.plainFilenamesIn(stagedFiles).contains(file)
                    || Utils.plainFilenamesIn(removedFiles).contains(file)) {
                untracked = true;
            }
        }
        return untracked;
    }

    /** determines if the file is untracked.
     * @param commitID the id of the commit.
     * @return is the commit untracked? */
    private boolean untrackedFilesCommit(String commitID) {
        List<String> currentFiles = Utils.plainFilenamesIn(currentDir);
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        List<String> prevFiles = Utils.plainFilenamesIn(
                Utils.join(commits, headCommitSha, ".files"));
        List<String> commitIDCommitFiles = Utils.plainFilenamesIn(
                Utils.join(commits, commitID, ".files"));
        boolean untracked = false;
        for (String name: commitIDCommitFiles) {
            if (currentFiles.contains(name)
                    && !filesEqual(Utils.join(currentDir, name),
                    Utils.join(commits, commitID, ".files", name))
                    && !prevFiles.contains(name)) {
                untracked = true;
            }
            if (currentFiles.contains(name)
                    && prevFiles.contains(name)
                    && !filesEqual(Utils.join(currentDir, name),
                    Utils.join(Utils.join(
                            commits, headCommitSha, ".files", name)))
                    && !filesEqual(Utils.join(currentDir, name),
                    Utils.join(commits, commitID, ".files", name))) {
                untracked = true;
            }
        }
        for (String file: currentFiles) {
            if (!commitIDCommitFiles.contains(file)
                    && prevFiles.contains(file)
                    && !filesEqual(Utils.join(currentDir, file),
                    Utils.join(commits, headCommitSha, ".files", file))) {
                untracked = true;
            }
        }
        return untracked;
    }

    /** checkout the branch.
     * @param branch branch name. */
    public void checkout(String branch) {
        List<String> branchNames = Utils.plainFilenamesIn(branches);
        List<String> headCommitFiles = Utils.plainFilenamesIn(
                Utils.join(commits,
                        Utils.plainFilenamesIn(head).get(0), ".files"));
        if (!branchNames.contains(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (untrackedFiles(branch)) {
            System.out.println("There is an untracked file in "
                    + "the way; delete it, or add and commit it first.");
            return;
        }
        if (branch.equals(Utils.readContentsAsString(headBranch))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String commitSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        File newFiles = Utils.join(commits, commitSha, ".files");
        for (String name: Utils.plainFilenamesIn(newFiles)) {
            Utils.writeContents(Utils.join(currentDir, name),
                    Utils.readContents(Utils.join(newFiles, name)));
        }
        for (String curr: Utils.plainFilenamesIn(currentDir)) {
            if (headCommitFiles.contains(curr)
                    && !Utils.plainFilenamesIn(newFiles).contains(curr)) {
                Utils.join(currentDir, curr).delete();
            }
        }
        Utils.join(head, Utils.plainFilenamesIn(head).get(0)).delete();
        Utils.writeContents(Utils.join(head, commitSha), commitSha);
        Utils.writeContents(headBranch, branch);
        removeStagedRemovedFiles();
    }

    /** checkout the file.
     * @param filename the name of the file. */
    public void checkoutFile(String filename) {
        String prevCommitSha = Utils.plainFilenamesIn(head).get(0);
        if (!Utils.plainFilenamesIn(Utils.join(
                commits, prevCommitSha, ".files")).contains(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File commitFile = Utils.join(
                commits, prevCommitSha, ".files", filename);
        Utils.writeContents(Utils.join(currentDir, filename),
                Utils.readContents(commitFile));
    }

    /** checkout the given filename with commitID.
     * @param filename the name of the file.
     * @param commitID the ID of the commit. */
    public void checkout(String filename, String commitID) {
        if (!listFiles(commits).contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (!Utils.plainFilenamesIn(Utils.join(
                commits, commitID, ".files")).contains(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File commitFile = Utils.join(
                commits, commitID, ".files", filename);
        Utils.writeContents(Utils.join(
                currentDir, filename), Utils.readContents(commitFile));
    }

    /** resets to given commitID.
     * @param commitID ID of commit. */
    public void reset(String commitID) {
        if (!listFiles(commits).contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        List<String> tracked = Utils.plainFilenamesIn(
                Utils.join(commits, headCommitSha, ".files"));
        if (untrackedFilesCommit(commitID)) {
            System.out.println("There is an untracked file"
                    + " in the way; delete it, or add and commit it first.");
            return;
        }
        List<String> filesOfOldCommit = Utils.plainFilenamesIn(
                Utils.join(commits, commitID, ".files"));
        for (String commitFile: filesOfOldCommit) {
            Utils.writeContents(Utils.join(currentDir, commitFile),
                    Utils.readContents(Utils.join(
                            commits, commitID, ".files", commitFile)));
        }
        for (String cwdFile: Utils.plainFilenamesIn(currentDir)) {
            if (tracked.contains(cwdFile)
                    && filesEqual(Utils.join(
                            commits, headCommitSha, ".files", cwdFile),
                    Utils.join(currentDir, cwdFile))
                    && !filesOfOldCommit.contains(cwdFile)) {
                Utils.join(currentDir, cwdFile).delete();
            }
        }
        Utils.join(head, Utils.plainFilenamesIn(head).get(0)).delete();
        Utils.writeContents(Utils.join(head, commitID), commitID);
        Utils.writeContents(Utils.join(branches,
                Utils.readContentsAsString(headBranch)), commitID);
        removeStagedRemovedFiles();
    }

    /** removes the file.
     * @param  filename name of target file. */
    public void remove(String filename) {
        String headSha = Utils.plainFilenamesIn(head).get(0);
        List<String> stagedAndHead = Utils.plainFilenamesIn(stagedFiles);
        List<String> heads = Utils.plainFilenamesIn(
                Utils.join(commits, headSha, ".files"));
        if (!stagedAndHead.contains(filename) && !heads.contains(filename)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        List<String> headCommitFiles = Utils.plainFilenamesIn(
                Utils.join(commits, headSha, ".files"));
        List<String> currentFiles = Utils.plainFilenamesIn(currentDir);
        if (Utils.plainFilenamesIn(stagedFiles).contains(filename)
                && headCommitFiles.contains(filename)
                && currentFiles.contains(filename)) {
            Utils.writeContents(Utils.join(removedFiles, filename),
                    Utils.readContents(Utils.join(currentDir, filename)));
            Utils.join(stagedFiles, filename).delete();
            Utils.join(currentDir, filename).delete();
        } else if (headCommitFiles.contains(filename)
                && currentFiles.contains(filename)) {
            Utils.writeContents(Utils.join(removedFiles, filename),
                    Utils.readContents(Utils.join(currentDir, filename)));
            Utils.join(currentDir, filename).delete();
        } else if (Utils.plainFilenamesIn(stagedFiles)
                .contains(filename)) {
            Utils.join(stagedFiles, filename).delete();
        } else if (headCommitFiles.contains(filename)
                && !currentFiles.contains(filename)) {
            Utils.writeContents(Utils.join(removedFiles, filename),
                    Utils.readContents(Utils.join(
                            commits, headSha, ".files", filename)));
        }
    }

    /** created a branch.
     * @param branch branch to be created. */
    public void branch(String branch) {
        if (Utils.plainFilenamesIn(branches).contains(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String sha = Utils.plainFilenamesIn(head).get(0);
        Utils.writeContents(Utils.join(branches, branch), sha);
    }

    /** removes the branch.
     * @param branch the to-be-removed branch. */
    public void removeBranch(String branch) {
        if (!Utils.plainFilenamesIn(branches)
                .contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branch.equals(Utils.readContentsAsString(headBranch))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Utils.join(branches, branch).delete();
    }

    /** log method. */
    public void log() {
        if (Utils.plainFilenamesIn(head).size() > 0) {
            String sha = Utils.plainFilenamesIn(head).get(0);
            Commit currCommit = Utils.readObject(Utils.join(
                    commits, sha, ".serial"), Commit.class);
            while (!currCommit.getParentSHA().equals("")) {
                System.out.println("===");
                System.out.println("commit " + sha);
                System.out.println("Date: " + currCommit.getDate());
                System.out.println(currCommit.getMessage());
                System.out.println();
                sha = currCommit.getParentSHA();
                currCommit = Utils.readObject(Utils.join(
                        commits, sha, ".serial"), Commit.class);
            }
            System.out.println("===");
            System.out.println("commit " + sha);
            System.out.println("Date: " + currCommit.getDate());
            System.out.print(currCommit.getMessage());
        }
    }

    /** global log. */
    public void globalLog() {
        List<String> commitShas = listFiles(commits);
        for (String name: commitShas) {
            Commit currCommit = Utils.readObject(
                    Utils.join(commits, name, ".serial"), Commit.class);
            System.out.println("===");
            System.out.println("commit " + name);
            System.out.println("Date: " + currCommit.getDate());
            System.out.println(currCommit.getMessage());
            System.out.println();
        }
    }

    /** lists files that are dir.
     * @param dirs the file directory.
     * @return list of dir files. */
    public List<String> listFiles(File dirs) {
        File[] dirFiles = dirs.listFiles();
        List<String> result = new ArrayList<>();
        for (File file: dirFiles) {
            result.add(file.getName());
        }
        Collections.sort(result);
        return result;
    }

    /** finds the commits.
     * @param  commitMsg the commit message. */
    public void find(String commitMsg) {
        boolean commitExists = false;
        List<String> commitShas = listFiles(commits);
        for (String sha: commitShas) {
            Commit currCommit = Utils.readObject(Utils.join(
                    commits, sha, ".serial"), Commit.class);
            if (currCommit.getMessage().equals(commitMsg)) {
                commitExists = true;
                System.out.println(sha);
            }
        }
        if (!commitExists) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** prints the status. */
    public void status() {
        List<String> sortedBranches = Utils.plainFilenamesIn(branches);
        List<String> sortedStaged = Utils.plainFilenamesIn(stagedFiles);
        List<String> sortedRemoved = Utils.plainFilenamesIn(removedFiles);
        List<String> sortedModsDels = Utils.plainFilenamesIn(currentDir);
        String headCommitSha = Utils.plainFilenamesIn(head).get(0);
        System.out.println("=== Branches ===");
        for (String branch: sortedBranches) {
            if (Utils.readContentsAsString(headBranch)
                    .equals(branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String staged: sortedStaged) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removed: sortedRemoved) {
            System.out.println(removed);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String name: Utils.plainFilenamesIn(Utils.join(
                commits, headCommitSha, ".files"))) {
            if (sortedModsDels.contains(name)) {
                if (isModified(name)) {
                    System.out.println(name + " (modified)");
                }
            } else if (!Utils.plainFilenamesIn(removedFiles).contains(name)
                    && !Utils.plainFilenamesIn(currentDir).contains(name)) {
                System.out.println(name + " (deleted)");
            }
        }
        System.out.println();
        System.out.print("=== Untracked Files ===\n");
        for (String file: Utils.plainFilenamesIn(currentDir)) {
            if (isUntracked(file)) {
                System.out.print(file + "\n");
            }
        }

    }

    /** belongs in modified section if true.
     * @param filename the name of the file.
     * @return is the file modified? */
    private boolean isModified(String filename) {
        String currCommitSha = Utils.plainFilenamesIn(head).get(0);
        File workingDirectoryVersion = Utils.join(currentDir, filename);
        boolean trackedCurr = Utils.plainFilenamesIn(Utils.join(
                commits, currCommitSha, ".files")).contains(filename);
        if (trackedCurr) {
            File headCommitVersion = Utils.join(
                    commits, currCommitSha, ".files", filename);
            if (!filesEqual(headCommitVersion, workingDirectoryVersion)
                    && !Utils.plainFilenamesIn(
                            stagedFiles).contains(filename)) {
                return true;
            }
            if (!Utils.plainFilenamesIn(currentDir).contains(filename)
                    && !Utils.plainFilenamesIn(
                            removedFiles).contains(filename)) {
                return true;
            }
        }
        if (Utils.plainFilenamesIn(stagedFiles).contains(filename)) {
            if (!filesEqual(workingDirectoryVersion,
                    Utils.join(stagedFiles, filename))) {
                return true;
            }
            if (!Utils.plainFilenamesIn(currentDir).contains(filename)) {
                return true;
            }
        }
        return false;
    }

    /** is untracked boolean.
     * @param  filename the name of the file.
     * @return is the file untracked? */
    private boolean isUntracked(String filename) {
        String currCommitSha = Utils.plainFilenamesIn(head).get(0);
        boolean trackedCurr = Utils.plainFilenamesIn(Utils.join(
                commits, currCommitSha, ".files")).contains(filename);
        if (!Utils.plainFilenamesIn(stagedFiles).contains(
                filename) && !trackedCurr) {
            return true;
        }
        if (Utils.plainFilenamesIn(removedFiles).contains(filename)) {
            return true;
        }
        return false;
    }

    /** filesEqual method.
     * @param first the first file.
     * @param second the second file.
     * @return if they're equal. */
    private boolean filesEqual(File first, File second) {
        return Utils.sha1(Utils.serialize(Utils.readContents(first)))
                .equals(Utils.sha1(
                        Utils.serialize(Utils.readContents(second))));
    }

    /** merge method.
     * @param branch the branch to be merged with. */
    public void merge(String branch) {
        if (!Utils.plainFilenamesIn(branches).contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!Utils.plainFilenamesIn(stagedFiles).isEmpty()
                || !Utils.plainFilenamesIn(removedFiles).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (untrackedFiles(branch)) {
            System.out.println("There is an untracked file"
                    + " in the way; delete it, or add and commit it first.");
            return;
        }
        String currBranch = Utils.readContentsAsString(headBranch);
        if (currBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String firstCurrBranchSha = Utils
                .readContentsAsString(Utils.join(branches, currBranch));
        String inputBranchSha = Utils
                .readContentsAsString(Utils.join(branches, branch));
        String splitSha = splitPSHA(branch);
        if (inputBranchSha.equals(splitSha)) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            System.exit(0);
        }
        if (firstCurrBranchSha.equals(splitSha)) {
            checkout(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        File split = Utils.join(commits, splitSha, ".files");
        File curr = Utils.join(commits,
                Utils.plainFilenamesIn(head).get(0), ".files");
        String branchSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        File branchF = Utils.join(commits, branchSha, ".files");
        List<String> splitFiles = Utils.plainFilenamesIn(split);
        List<String> currBranchFiles = Utils.plainFilenamesIn(curr);
        List<String> inputBranchFiles = Utils.plainFilenamesIn(branchF);
        HashSet<String> currInputFiles = new HashSet<>();
        currInputFiles.addAll(currBranchFiles);
        currInputFiles.addAll(inputBranchFiles);
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(currInputFiles);
        splitListHelper(combinedList, splitFiles,
                currBranchFiles, inputBranchFiles, branch);
        combinedListHelper(combinedList, splitFiles,
                currBranchFiles, inputBranchFiles, branch);
        commit("Merged " + branch
                + " into " + currBranch + ".",
                firstCurrBranchSha, inputBranchSha, true);
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** a helper for merge.
     *
     * @param combinedList the combined list.
     * @param splitFiles the split list.
     * @param currBranchFiles the current branch list.
     * @param inputBranchFiles the input branch list.
     * @param branch the merging branch.
     */
    private void splitListHelper(
            List<String> combinedList,
            List<String> splitFiles,
            List<String> currBranchFiles,
            List<String> inputBranchFiles,
            String branch) {
        String inputBranchSha = Utils
                .readContentsAsString(Utils.join(branches, branch));
        String splitSha = splitPSHA(branch);
        File split = Utils.join(commits, splitSha, ".files");
        File curr = Utils.join(commits,
                Utils.plainFilenamesIn(head).get(0), ".files");
        String branchSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        File branchF = Utils.join(commits, branchSha, ".files");
        for (String splitFile: splitFiles) {
            if (currBranchFiles.contains(splitFile)
                    && !inputBranchFiles.contains(splitFile)) {
                if (filesEqual(Utils.join(curr, splitFile),
                        Utils.join(split, splitFile))) {
                    remove(splitFile);
                } else {
                    isConflict = true;
                    Utils.writeContents(Utils.join(
                            currentDir, splitFile), contentsConflict(Utils.join(
                            curr, splitFile), null));
                    addFile(splitFile);
                }
            }
            if (currBranchFiles.contains(splitFile)
                    && inputBranchFiles.contains(splitFile)) {
                if (filesEqual(Utils.join(curr, splitFile),
                        Utils.join(split, splitFile))
                        && !filesEqual(Utils.join(split, splitFile),
                        Utils.join(branchF, splitFile))) {
                    checkout(splitFile, inputBranchSha);
                    addFile(splitFile);
                }
                if (!filesEqual(Utils.join(curr, splitFile),
                        Utils.join(split, splitFile))
                        && !filesEqual(Utils.join(curr, splitFile),
                        Utils.join(branchF, splitFile))
                        && !filesEqual(Utils.join(split, splitFile),
                        Utils.join(branchF, splitFile))) {
                    isConflict = true;
                    Utils.writeContents(Utils.join(currentDir, splitFile),
                            contentsConflict(Utils.join(curr, splitFile),
                                    Utils.join(branchF, splitFile)));
                    addFile(splitFile);
                }
            }
            if (inputBranchFiles.contains(splitFile)
                    && !currBranchFiles.contains(splitFile)) {
                if (!filesEqual(Utils.join(branchF, splitFile),
                        Utils.join(split, splitFile))) {
                    isConflict = true;
                    Utils.writeContents(Utils.join(
                            currentDir, splitFile), contentsConflict(
                            null, Utils.join(branchF, splitFile)));
                    addFile(splitFile);
                }
            }
        }
    }

    /** a helper for merge.
     *
     * @param combinedList the combined list.
     * @param splitFiles the split list.
     * @param currBranchFiles the current branch list.
     * @param inputBranchFiles the input branch list.
     * @param branch the merging branch.
     */
    private void combinedListHelper(
            List<String> combinedList,
            List<String> splitFiles,
            List<String> currBranchFiles,
            List<String> inputBranchFiles,
            String branch) {
        String currBranch = Utils.readContentsAsString(headBranch);
        File curr = Utils.join(commits,
                Utils.plainFilenamesIn(head).get(0), ".files");
        String inputBranchSha = Utils
                .readContentsAsString(Utils.join(branches, branch));
        String branchSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        File branchF = Utils.join(commits, branchSha, ".files");
        for (String inputF: combinedList) {
            if (!splitFiles.contains(inputF)
                    && !currBranchFiles.contains(inputF)) {
                checkout(inputF, inputBranchSha);
                addFile(inputF);
            }
            if (currBranchFiles.contains(inputF)
                    && inputBranchFiles.contains(inputF)
                    && !filesEqual(Utils.join(curr, inputF), Utils.join(
                    branchF, inputF))
                    && !splitFiles.contains(inputF)) {
                isConflict = true;
                Utils.writeContents(Utils.join(
                        currentDir, inputF), contentsConflict(Utils.join(
                        currentDir, inputF), Utils.join(branchF, inputF)));
                addFile(inputF);
            }
        }
    }


    /** returns the string of a conflicted file.
     * @param curr current file.
     * @param given given file.
     * @return formatted string of conflict. */
    private String contentsConflict(File curr, File given) {
        if (curr == null && given != null) {
            return "<<<<<<< HEAD\n=======\n"
                    + Utils.readContentsAsString(given) + ">>>>>>>\n";
        }
        if (given == null && curr != null) {
            return "<<<<<<< HEAD\n" + Utils.readContentsAsString(curr)
                    + "=======\n>>>>>>>\n";
        }
        return "<<<<<<< HEAD\n" + Utils.readContentsAsString(curr)
                + "=======\n" + Utils.readContentsAsString(given) + ">>>>>>>\n";
    }

    /** Find the split point SHA.
     * @param branch the branch name.
     * @return split point sha string. */
    public String splitPSHA(String branch) {
        String currBranch = Utils.readContentsAsString(headBranch);
        String firstCurrBranchSha = Utils.readContentsAsString(
                Utils.join(branches, currBranch));
        String inputBranchSha = Utils.readContentsAsString(
                Utils.join(branches, branch));
        while (!firstCurrBranchSha.equals("")) {
            String tempInputSha = inputBranchSha;
            Commit currNext = Utils.readObject(Utils.join(
                    commits, firstCurrBranchSha, ".serial"), Commit.class);
            while (!tempInputSha.equals("")) {
                if (currNext.isMerge()) {
                    if (currNext.getGivenBranch().equals(tempInputSha)) {
                        return tempInputSha;
                    }
                }
                if (firstCurrBranchSha.equals(tempInputSha)) {
                    return firstCurrBranchSha;
                }
                Commit inputNext = Utils.readObject(Utils.join(
                        commits, tempInputSha, ".serial"), Commit.class);
                if (inputNext.isMerge()) {
                    if (inputNext.getCurrBranch().equals(firstCurrBranchSha)
                            || inputNext.getGivenBranch()
                            .equals(firstCurrBranchSha)) {
                        return firstCurrBranchSha;
                    }
                }
                tempInputSha = inputNext.getParentSHA();
            }
            firstCurrBranchSha = currNext.getParentSHA();
        }
        return null;
    }
}
