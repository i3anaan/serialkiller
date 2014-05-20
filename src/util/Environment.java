package util;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Utility class for making Java application self-aware.
 */
public final class Environment {
    private Environment() {
        /* Disallow construction. */
    }

    public static String getWorkingDir() {
        return System.getProperty("user.dir");
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public static String getGitBranch() {
        try {
            File gitdir = new File(System.getProperty("user.dir"), ".git");
            File head = new File(gitdir, "HEAD");
            String ref = Files.toString(head, Charsets.UTF_8);
            return Iterables.getLast(Arrays.asList(ref.substring(5).split("/"))).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    public static String getGitCommit() {
        try {
            File gitdir = new File(System.getProperty("user.dir"), ".git");
            File refdir = new File(gitdir, "refs");
            File headdir = new File(refdir, "heads");
            File headf = new File(headdir, getGitBranch());
            return Files.toString(headf, Charsets.UTF_8).substring(0, 7);
        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
