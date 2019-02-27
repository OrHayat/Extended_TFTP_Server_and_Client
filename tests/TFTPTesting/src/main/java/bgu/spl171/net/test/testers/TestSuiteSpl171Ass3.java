package bgu.spl171.net.test.testers;

import bgu.spl171.net.test.runners.TestReactorServer;
import bgu.spl171.net.test.runners.TestServer;
import bgu.spl171.net.test.runners.TestTPCServer;
import bgu.spl171.net.test.testers.tests.*;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by matan on 1/24/2017.
 */
public class TestSuiteSpl171Ass3 implements TestSuite {
    private String userPath;
    private String serverPath;
    private String clientPath;

    private int port;
    private Integer groupId;

    /**
     * Create a new SPL171 assignment 3 single user tests suite
     *
     * @param userPath Path to base directory (within is the Server and Client directory)
     * @param port     port to run
     * @param groupId  group id tested, can be Null
     */
    public TestSuiteSpl171Ass3(String userPath, int port, Integer groupId) {
        this.userPath = userPath;
        this.port = port;
        this.serverPath = userPath + File.separator + "Server";
        this.clientPath = userPath + File.separator + "Client";
        this.groupId = groupId;
    }


    /**
     * Runs a full tests suite and returns results
     *
     * @return Test suite score map
     */
    public Map<String, Integer> runSuite() {
        Map<String, Integer> scoreMap = new TreeMap<>();

        for (int serverIndex = 0; serverIndex < SERVER_TYPES.length; ++serverIndex) {
            for (int testIndex = 0; testIndex < TEST_SUITE.length; ++testIndex) {
                String test = SERVER_TYPES[serverIndex] + "_" + TEST_SUITE[testIndex];
                scoreMap.put(test, runTest(SERVER_TYPES[serverIndex], TEST_SUITE[testIndex]));
            }
        }

        return scoreMap;
    }


    /*
    Each has to return a number between 0 to 100

    To add new server add it to the TEST_SUITE array and update the runTest function
    */
    private static final String[] TEST_SUITE = {"LOGRQ", "DISC", "DELRQ", "RRQ", "WRQ", "DIRQ"};

    /**
     * Runs a single tests with server defined in ServerStr
     *
     * @param serverStr Server name
     * @param test      tests name
     * @return score of tests
     */
    private int runTest(String serverStr, String test) {
        int res = 0;
        String logFile = serverStr + "_" + test;
        if (groupId != null)
            logFile = groupId + "_" + logFile;
        Test testSelection = null;

        TestServer server = getServer(serverStr, logFile + "_server");
        if ("LOGRQ".equals(test)) {
            testSelection = new TestLOGRQ(logFile, HOST, port, clientPath, server);
        } else if ("DELRQ".equals(test)) {
            testSelection = new TestDELRQ(logFile, HOST, port, clientPath, server);
        } else if ("RRQ".equals(test)) {
            testSelection = new TestRRQ(logFile, HOST, port, clientPath, server);
        } else if ("WRQ".equals(test)) {
            testSelection = new TestWRQ(logFile, HOST, port, clientPath, server);
        } else if ("DISC".equals(test)) {
            testSelection = new TestDISC(logFile, HOST, port, clientPath, server);
        } else if ("DIRQ".equals(test)) {
            testSelection = new TestDIRQ(logFile, HOST, port, clientPath, server);
        } else {
            System.err.println("UNKNOWN TEST - PANIC");
            System.exit(-1);
        }


        try (Test currentTest = testSelection) {
            currentTest.init();
            res = currentTest.test();
        } catch (Exception e) {
            System.err.println("Failed running or closing tests");
            e.printStackTrace();
        }

        System.out.println("[Final] " + logFile + " = " + res);
        return res;
    }


    /**
     * Create a new server by given name
     *
     * @param serverStr server name
     * @param log       log file for output
     * @return A new TestServer
     */
    protected TestServer getServer(String serverStr, String log) {
        TestServer server = null;
        if ("TPC".equals(serverStr)) {
            server = new TestTPCServer(serverPath, log, port);
        } else if ("REACTOR".equals(serverStr)) {
            server = new TestReactorServer(serverPath, log, port);
        } else {
            System.err.println("UNKNOWN SERVER - PANIC");
            System.exit(-1);
        }

        return server;
    }


    public String[] getTestSuite() {
        String[] fullTestSuite = new String[TEST_SUITE.length * SERVER_TYPES.length];
        for (int serverIndex = 0; serverIndex < SERVER_TYPES.length; ++serverIndex) {
            for (int testIndex = 0; testIndex < TEST_SUITE.length; ++testIndex) {
                fullTestSuite[serverIndex * TEST_SUITE.length + testIndex] =
                        SERVER_TYPES[serverIndex] + "_" + TEST_SUITE[testIndex];
            }
        }
        return fullTestSuite;
    }

    private static final String HOST = "127.0.0.1";
    private static final String[] SERVER_TYPES = {"TPC", "REACTOR"};
}
