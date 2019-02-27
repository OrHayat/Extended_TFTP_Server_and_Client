package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;
import bgu.spl171.net.test.testers.Test;

import java.io.*;
import java.util.Random;

/**
 * Created by matan on 25/01/17.
 */
public abstract class TestSpl171Ass3 implements Test {
    private static final int RETRY = 5;


    protected String logPrefix;
    protected String host;
    protected int port;
    protected String clientPath;
    protected TestServer server;

    public TestSpl171Ass3(String logPrefix, String host, int port, String clientPath, TestServer server) {
        this.logPrefix = logPrefix;
        this.host = host;
        this.port = port;
        this.clientPath = clientPath;
        this.server = server;
    }

    // Compare items
    protected static final String[] ACK_0 = {"ACK", "0"};
    protected static final String[] ACK_1 = {"ACK", "1"};
    protected static final String[] ERROR_USER_LOGGED_IN = {"ERROR", "7"};
    protected static final String[] ERROR_FILE_NOT_FOUND = {"ERROR", "1"};
    protected static final String[] ERROR_FILE_EXISTS = {"ERROR", "5"};


    // Time out for timeout read
    public static final int TIMEOUT = 5000;

    /**
     * By default starts the server. If using own implementation call server start when needed.
     */
    @Override
    public void init() {
        server.start();
    }

    /**
     * By default closes server. If using own call server.close()
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        server.close();
    }

    /**
     * Read prog's input stream and compare with the comp array
     * <p>
     * Issues: (TODO ?) If A doesn't work and B does we might miss B while trying to gather A. Maybe compare from log and set an index line to start with.
     * Example: A doesn't work keep location on 0. B works progress location start to B's position.
     *
     * @param prog The programs to read from
     * @param comp An array of items to find in read line, must be by order
     * @return True if matched, false otherwise (up to {@value #RETRY} tries to allow for access text)
     */
    protected static boolean strCmp(TestProgram prog, String[] comp) {
        boolean work = false;
        int attempt = 0;

        String line;
        while (attempt < RETRY && (line = prog.timeoutRead(TIMEOUT)) != null) {
            line = line.trim().toLowerCase();
            int start = 0;
            boolean found = true;
            for (String s : comp) {
                int loc = line.indexOf(s.toLowerCase(), start);
                if (loc >= 0)
                    start = loc + s.length();
                else {
                    attempt++;
                    found = false;
                    break;
                }
            }
            work = found;
            if (work) {
                System.out.println("Matched(" + prog.getLogName() + "): " + printArray(comp));
                break;
            }
        }

        return work;
    }

    /**
     * Read prog's input stream and compare with the comp array. This version allows for multiple lines without order.
     *
     * @param prog The programs to read from
     * @param comp An array of items to find in read line, must be by order
     * @return True if matched, false otherwise (up to {@value #RETRY} tries to allow for access text)
     */
    protected static boolean strCmps(TestProgram prog, String[][] comp) {
        boolean[] foundArray = new boolean[comp.length];
        int left = comp.length;
        int attempt = 0;

        String line;
        while (attempt < RETRY && left > 0 && (line = prog.timeoutRead(TIMEOUT)) != null) {
            boolean matched = false;
            line = line.trim().toLowerCase();
            int start = 0;
            for (int i = 0; i < comp.length; ++i) {
                if (!foundArray[i]) {
                    boolean found = true;
                    String[] current = comp[i];
                    for (String s : current) {
                        int loc = line.indexOf(s.toLowerCase(), start);
                        if (loc >= 0)
                            start = loc + s.length();
                        else {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        left--;
                        foundArray[i] = true;
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                attempt++;
            }
        }

        if (left == 0) {
            System.out.print("Matched (" + prog.getLogName() + "): ");
            for (int i = 0 ; i < comp.length ; ++i) {
                 System.out.print(printArray(comp[i]));
                 if (i + 1 < comp.length)
                     System.out.print(", ");
            }
            System.out.println();
        }

        return (left == 0);
    }

    private static String printArray(String[] arr) {
        String res = "[";
        for (int i = 0; i < arr.length; ++i) {
            res += arr[i];
            if (i + 1 < arr.length)
                res += " ,";
        }
        res += "]";
        return res;
    }

    /**
     * Fills an existing file with random size bytes
     *
     * @param f    The file to fill
     * @param size The number of bytes to write
     * @return true is successful
     */
    protected static boolean fillFile(File f, long size) {
        boolean worked = false;
        Random r = new Random();
        try (OutputStream os = new FileOutputStream(f)) {
            long written = 0;
            byte[] b = new byte[1];
            while (written < size) {
                r.nextBytes(b);
                os.write(b);
                written += 1;
            }
            os.flush();
        } catch (IOException e) {
            f.delete();
        }
        return worked;
    }

    /**
     * Compare 2 files and return resemblance % by file a
     *
     * @param a The file that will be compared to File b
     * @param b The file that will be compared to File a
     * @return % of resemblance. -1 if failed to open
     */
    protected static double cmpFiles(File a, File b) {
        double resemblance = 0.0;
        boolean exact = true;

        try (FileInputStream aReader = new FileInputStream(a);
             FileInputStream bReader = new FileInputStream(b)) {
            long aSize = a.length();
            long bSize = b.length();
            if (aSize != bSize)
                exact = false;
            long size = Math.min(aSize, bSize);
            long read = 0;
            while (read < size) {
                // If not exact, calculate similarity %
                if (aReader.read() == bReader.read()) {
                    resemblance += (1.0 / aSize);
                } else {
                    exact = false;
                }
                ++read;
            }
        } catch (IOException e) {
            resemblance = -1.0;
        }

        // If exact just put 1 (We need this so we don't get 0.9999999... if exact - java issues)
        if (exact)
            resemblance = 1.0;

        return resemblance;
    }
}
