package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** the commit class.
 * @author Vikram Cherukuri
 */
public class Commit implements Serializable {

    /** message of the commit. */
    private String _message;

    /** the time. */
    private Date timestamp;

    /** parent SHA of commit. */
    private String _parent;

    /** current branch of commit. */
    private String _currBranch;

    /** given branch of commit. */
    private String _givenBranch;

    /** is the commit a merge? */
    private boolean _isMerge;

    /** the commit constructor.
     *
     * @param message message of the commit.
     * @param parent parent SHA of the commit.
     * @param currBranch current string branch of commit.
     * @param givenBranch given string branch of commit.
     * @param isMerge is commit a merge?
     *
     */
    public Commit(String message, String parent,
                  String currBranch, String givenBranch, boolean isMerge) {
        if ((message == null || message.isEmpty() || message.equals(""))) {
            throw new GitletException("Please enter a commit message.");
        }
        this._message = message;
        this.timestamp = new Date();
        this._parent = parent;
        this._currBranch = currBranch;
        this._givenBranch = givenBranch;
        this._isMerge = isMerge;
    }

    /** gets the date.
     *
     * @return the string of the date.
     */
    public String getDate() {
        SimpleDateFormat format =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return format.format(this.timestamp);
    }

    /** gets the parent branch.
     *
     * @return the string of the parent branch.
     */
    public String getParentSHA() {
        return this._parent;
    }

    /** gets the message.
     *
     * @return the string of the message.
     */
    public String getMessage() {
        return this._message;
    }

    /** gets the current branch.
     *
     * @return the string of the current branch.
     */
    public String getCurrBranch() {
        return this._currBranch;
    }

    /** gets the given branch.
     *
     * @return the string of the given branch.
     */
    public String getGivenBranch() {
        return this._givenBranch;
    }

    /** merged or not.
     *
     * @return whether or not this commit is merged.
     */
    public boolean isMerge() {
        return this._isMerge;
    }
}
