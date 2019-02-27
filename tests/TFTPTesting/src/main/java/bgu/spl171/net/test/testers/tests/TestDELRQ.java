package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;

import java.io.File;
import java.io.IOException;

/**
 * Created by matan on 25/01/17.
 */
public class TestDELRQ  extends TestSpl171Ass3 {
    private File delFile;
    private static final String FILE_NAME = "TestDelFile";
    private static final String[] BCAST_DEL_FILE = {"BCAST", "del", FILE_NAME};

    public TestDELRQ(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
        delFile = null;
    }

    @Override
    public void init() {
        // create new file in server
        delFile = server.createNewFile(FILE_NAME);
        fillFile(delFile, 1024);

        super.init();
    }

    /**
     * Test the DELRQ packet, scores:
     * 20 - ACK on delete (not error)
     * 50 - File actually deleted
     * 10 - Client 1 (sends DELRQ) received BCAST
     * 10 - Client 2 (logged in) received BCAST
     * 10 - Client 3 (not logged in) did not receive BCAST
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
        int score = 0;
        TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);

        client1.write("LOGRQ c1");
        strCmp(client1, ACK_0);

        TestProgram client2 = new TestClient(clientPath, logPrefix + "_client2", host, port);

        client2.write("LOGRQ c2");
        strCmp(client2, ACK_0);

        TestProgram client3 = new TestClient(clientPath, logPrefix + "_client3", host, port);

        // tests delete
        client1.write("DELRQ " + FILE_NAME);
        if (strCmp(client1, ACK_0)) {
            score += 20;
        }

        // tests broadcast
        if (strCmp(client1, BCAST_DEL_FILE))
            score += 10;
        if (strCmp(client2, BCAST_DEL_FILE))
            score += 10;
        if (!strCmp(client3, BCAST_DEL_FILE))
            score += 10;

        client1.write("DISC");
        client2.write("DISC");
        client3.write("DISC");

        client1.close();
        client2.close();
        client3.close();

        if (!delFile.exists())
            score += 50;

        return score;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (delFile != null && delFile.exists()) {
            delFile.delete();
        }
    }
}
