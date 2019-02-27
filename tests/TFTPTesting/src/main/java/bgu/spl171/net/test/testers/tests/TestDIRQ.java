package bgu.spl171.net.test.testers.tests;

import bgu.spl171.net.test.runners.TestClient;
import bgu.spl171.net.test.runners.TestProgram;
import bgu.spl171.net.test.runners.TestServer;

import java.io.File;
import java.io.IOException;

/**
 * Created by matan on 25/01/17.
 */
public class TestDIRQ extends TestSpl171Ass3 {
    private static final String FILE_NAME_PREFIX = "TestDirqFile";
    private static final String UPLOAD_FILE = "AdditionalFile";
    private static final int FILE_NO = 2;
    private File[] dirqFiles;

    public TestDIRQ(String logPrefix, String host, int port, String clientPath, TestServer server) {
        super(logPrefix, host, port, clientPath, server);
        dirqFiles = null;
    }

    @Override
    public void init() {
        dirqFiles = new File[FILE_NO];
        // create new files in server
        for (int i = 0; i < FILE_NO; ++i) {
            dirqFiles[i] = server.createNewFile(FILE_NAME_PREFIX + "_" + i);
            fillFile(dirqFiles[i], 15);
        }

        super.init();
    }

    /**
     * Test the DIRQ packet, scores:
     * 60 - Return all files that are already in folder
     * 40 - See additional uploaded file
     *
     * @return the score for the tests
     */
    @Override
    public int test() {
        int score = 0;
        TestProgram client1 = new TestClient(clientPath, logPrefix + "_client1", host, port);

        client1.write("LOGRQ c1");
        strCmp(client1, ACK_0);

        client1.write("DIRQ");
        try {
            // Wait for dirq response
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException ignore) {
        }
        String[][] lines = applyFiles(null);
        if (strCmps(client1, lines)) {
            score += 60;
        }

        File uploadedFile = client1.createNewFile(UPLOAD_FILE);
        fillFile(uploadedFile, 15);
        client1.write("WRQ " + UPLOAD_FILE);
        strCmp(client1, ACK_0);
        strCmp(client1, ACK_1);
        strCmp(client1, new String[]{"WRQ", UPLOAD_FILE, "complete"});

        client1.write("DIRQ");
        try {
            // Wait for dirq response
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException ignore) {
        }
        if (strCmp(client1, new String[]{UPLOAD_FILE})) {
            score += 40;
        }

        client1.write("DISC");
        strCmp(client1, ACK_0);

        client1.close();

        if (!uploadedFile.delete()) {
            System.err.println("PANIC cant delete");
        }
        uploadedFile = server.getFile(UPLOAD_FILE);
        if (!uploadedFile.delete()) {
            System.err.println("PANIC cant delete");
        }

        return score;
    }

    private String[][] applyFiles(String[][] additions) {
        int outerSize = dirqFiles.length;
        if (additions != null)
            outerSize += additions.length;

        String[][] compares = new String[outerSize][];
        for (int i = 0; i < dirqFiles.length; ++i) {
            compares[i] = new String[]{dirqFiles[i].getName()};
        }

        if (additions != null) {
            for (int i = 0; i < additions.length; ++i) {
                compares[i + dirqFiles.length] = additions[i];
            }
        }
        return compares;
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (File f : dirqFiles) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }
}
