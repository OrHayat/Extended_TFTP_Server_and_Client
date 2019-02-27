package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;

/**
 * Created by matan on 25/01/17.
 */
public class TestLOGRQ extends TestSpl171Ass3 {

    public TestLOGRQ(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
    }

    /**
     * Test the LOGRQ packet, scores:
     * 50  - successful login of 1 client
     * 25 - failed login in same name
     * 25 - successful login after failure
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
            int score = 0;
            TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);

            client1.write("LOGRQ c1");
            if (strCmp(client1, ACK_0))
                score += 50;

            TestProgram client2 = new TestClient(clientPath, logPrefix + "_client2", host, port);

            client2.write("LOGRQ c1");
            if (strCmp(client2, ERROR_USER_LOGGED_IN)) {
                score += 25;
            }

            client2.write("LOGRQ c2");
            if (strCmp(client2, ACK_0)) {
                score += 25;
            }

            client1.write("DISC");
            client2.write("DISC");

            client1.close();
            client2.close();

            return score;
    }
}
