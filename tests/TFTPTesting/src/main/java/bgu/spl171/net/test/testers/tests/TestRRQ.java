package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;

import java.io.File;
import java.io.IOException;

/**
 * Created by matan on 25/01/17.
 */
public class TestRRQ extends TestSpl171Ass3 {
    private File downFile;

    public TestRRQ(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
        downFile = null;
    }

    @Override
    public void init() {
        // create new file in server
        downFile = server.createNewFile(FILE_NAME);
        fillFile(downFile, 1500); // 2 packets of 512 + 1 packet of 476

        super.init();
    }

    /**
     * Test the RRQ packet, scores:
     * 20 - Print complete for RRQ
     * 20 - Print error on file not found request
     * 20 - File exists
     * 40 - File comparison (by %)
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
        int score = 0;
        TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);

        client1.write("LOGRQ c1");
        strCmp(client1, ACK_0);

        client1.write("RRQ " + FILE_NAME);
        int attempts = 3; // in case download takes time, allow up to 15 seconds
        while (attempts-- > 0) {
            if (strCmp(client1, new String[]{"RRQ", FILE_NAME, "complete"})) {
                score += 20;
                break;
            }
        }

        client1.write("RRQ FAKENAME");
        if (strCmp(client1, ERROR_FILE_NOT_FOUND))
            score += 20;

        client1.write("DISC");
        strCmp(client1, ACK_0);

        client1.close();

        File f;

        if ((f = client1.getFile(FILE_NAME)) != null && f.exists()) {
            score += 20;
            score += (40 * cmpFiles(downFile, f));

            if (!f.delete()) {
                System.err.println("PANIC cant delete");
            }
        }

        return score;
    }

    private static final String FILE_NAME = "TestDownFile";

    @Override
    public void close() throws IOException {
        super.close();
        if (downFile != null && downFile.exists()) {
            downFile.delete();
        }
    }
}
