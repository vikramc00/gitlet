package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Vikram Cherukuri
 */
public class Main {

    /** gitlet object. */
    private static Gitlet g;

    /** helper1 used? */
    private static boolean helper1Used;

    /** helper2 used? */
    private static boolean helper2Used;

    /** helper3 used? */
    private static boolean helper3Used;
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        g = new Gitlet();
        helper1Used = true;
        helper2Used = true;
        helper3Used = true;
        if (args.length > 0) {
            String command = args[0];
            helper1(command, args);
            helper3(command, args);
            helper2(command, args);
            if (!helper1Used && !helper2Used && !helper3Used) {
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    /** helper 1.
     * @param command the command.
     * @param args the arguments. */
    public static void helper1(String command, String[] args) {
        if (command.equals("init")) {
            g.initialize();
        } else if (command.equals("commit")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.commit(args[1], "", "", false);
            }
        } else if (command.equals("add")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.addFile(args[1]);
            }

        } else if (command.equals("branch")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.branch(args[1]);
            }
        } else {
            helper1Used = false;
        }
    }

    /** helper 2.
     * @param command the command.
     * @param args the arguments. */
    public static void helper2(String command, String[] args) {
        if (command.equals("global-log")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.globalLog();
            }

        } else if (command.equals("find")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.find(args[1]);
            }

        } else if (command.equals("status")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.status();
            }

        } else if (command.equals("rm-branch")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.removeBranch(args[1]);
            }

        } else if (command.equals("reset")) {
            if (!g.listFiles(Utils.join(g.getCurrentDir()))
                    .contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.reset(args[1]);
            }

        } else if (command.equals("merge")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.merge(args[1]);
            }
        } else {
            helper2Used = false;
        }
    }

    /** helper 3.
     * @param command the command.
     * @param args the arguments. */
    public static void helper3(String command, String[] args) {
        if (command.equals("checkout")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                if (args[1].equals("--")) {
                    g.checkoutFile(args[2]);
                } else if (args.length == 2) {
                    g.checkout(args[1]);
                } else if (args[2].equals("--")) {
                    g.checkout(args[3], args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
            }
        } else if (command.equals("log")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.log();
            }

        } else if (command.equals("rm")) {
            if (!g.listFiles(Utils.join(
                    g.getCurrentDir())).contains(".gitlet")) {
                System.out.println(
                        "Not in an initialized Gitlet directory.");
            } else {
                g.remove(args[1]);
            }
        } else {
            helper3Used = false;
        }
    }
}
