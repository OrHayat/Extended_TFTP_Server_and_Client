package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;

import java.util.concurrent.TimeUnit;

/**
 * Created by matan on 25/01/17.
 */
public class TestDISC extends TestSpl171Ass3 {

    public TestDISC(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
    }

    /**
     * Test the DISC packet, scores:
     * 50 - ACK 0 for request
     * 50 - closed alone
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
            int score = 0;
            TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);

            client1.write("LOGRQ c1");
            strCmp(client1, ACK_0);

            client1.write("DISC");
            if (strCmp(client1, ACK_0))
                score += 50;

            // in case of press any key
            try {
                client1.write("pressed");
            } catch (Exception ignore) {}

            if (client1.waitExit(TIMEOUT)) {
                score += 50;
            }

            client1.close();

            return score;
    }
}
