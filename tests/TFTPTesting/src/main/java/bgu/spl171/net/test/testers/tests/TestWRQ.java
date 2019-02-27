package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;
import bgu.spl171.net.test.testers.tests.TestSpl171Ass3;

import java.io.File;
import java.io.IOException;

/**
 * Created by matan on 25/01/17.
 */
public class TestWRQ extends TestSpl171Ass3 {
    private static final String FILE_NAME = "TestUpFile";
    private static final String[] BCAST_ADD_FILE = {"BCAST" , "add", FILE_NAME};

    public TestWRQ(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
    }

    /**
     * Test the WRQ packet, scores:
     * 5 - ACK on WRQ (not error)
     * 5 - ACK on Data packet 1
     * 5 - WRQ complete message
     * 15 - File exists
     * 50 - File comparison
     * 5 - Client 1 (sends DELRQ) received BCAST
     * 5 - Client 2 (logged in) received BCAST
     * 5 - Client 3 (not logged in) did not receive BCAST
     * 5 - Error file exists, second upload
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
        int score = 0;

        TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);
        // create new file in client
        File upFile = client1.createNewFile(FILE_NAME);
        fillFile(upFile, 345);

        client1.write("LOGRQ c1");
        strCmp(client1, ACK_0);

        TestProgram client2 = new TestClient(clientPath, logPrefix + "_client2", host, port);

        client2.write("LOGRQ c2");
        strCmp(client2, ACK_0);

        TestProgram client3 = new TestClient(clientPath, logPrefix + "_client3", host, port);

        // tests upload (1 block)
        client1.write("WRQ " + FILE_NAME);
        if (strCmp(client1, ACK_0)) {
            score += 5;
            if (strCmp(client1, ACK_1)) {
                score += 5;
                if (strCmp(client1, new String[]{"WRQ", FILE_NAME, "complete"})) {
                    score += 5;
                }
            }
        }

        // tests broadcast
        if (strCmp(client1, BCAST_ADD_FILE))
            score += 5;
        if (strCmp(client2, BCAST_ADD_FILE))
            score += 5;
        if (!strCmp(client3, BCAST_ADD_FILE))
            score += 5;

        client1.write("WRQ " + FILE_NAME);
        if (strCmp(client1, ERROR_FILE_EXISTS)) {
            score += 5;
        }

        client1.write("DISC");
        client2.write("DISC");
        client3.write("DISC");

        client1.close();
        client2.close();
        client3.close();

        File f;
        if ((f = server.getFile(FILE_NAME)) != null && f.exists()) {
            score += 15;
            score += (50 * cmpFiles(upFile, f));

            if (!f.delete()) {
                System.err.println("PANIC cant delete");
            }
        }

        if (!upFile.delete()) {
            System.err.println("PANIC cant delete");
        }

        return score;
    }
}
