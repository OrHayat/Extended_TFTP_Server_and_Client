package bgu.spl171.net.test.runners;

import bgu.spl171.net.test.testers.tests.TestSpl171Ass3;

import java.io.File;
import java.io.IOException;

/**
 * Created by matan on 1/24/2017.
 */
public class TestServer extends TestProgram {
    private static final String LOCAL_DIR = "Files";
    protected static final String[] RUN_LINE = {"mvn", "exec:java"};
    //protected static final String[] RUN_LINE = {"mvn", "-X", "exec:java"}; // with full log

    protected static String[] appendArray(String[] a, String[] b) {
        String[] arr = new String[a.length + b.length];
        int i = 0;
        for (String s : a) {
            arr[i++] = s;
        }
        for (String s : b) {
            arr[i++] = s;
        }
        return arr;
    }

    public TestServer(String cwd, String[] runLine, String log_name) {
        super(cwd, appendArray(RUN_LINE, runLine), log_name);
    }

    @Override
    public boolean start() {
        boolean result = super.start();

        // Wait for server to be ready (depends on computer speed) TODO: show to testers
        while (null != timeoutRead(TestSpl171Ass3.TIMEOUT * 4));

        return result;
    }

    private String getFileLoc() {
        return this.cwd + File.separator + LOCAL_DIR + File.separator;
    }

    @Override
    public File getFile(String fileName) {
        return new File(getFileLoc() + fileName);
    }

    @Override
    public File createNewFile(String fileName) {
        File f = new File(getFileLoc() + fileName);

        boolean create;
        try {
            create = f.createNewFile();
        } catch (IOException e) {
            create = false;
        }
        if (!f.exists()) {
            f = null;
        }

        return f;
    }
}
