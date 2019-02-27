package bgu.spl171.net.test.runners;

import java.io.*;

/**
 * Created by matan on 1/24/2017.
 */
public class TestClient extends TestProgram {
    private static final String RUN_LINE = "bin" + File.separator + "TFTPclient";

    public TestClient(String cwd, String log_name, String host, int port) {
        super(cwd, new String[]{RUN_LINE, host + "", port + ""}, log_name);
        // no reason to wait for init for client, only server.
        start();
    }

    @Override
    public File getFile(String fileName) {
        return new File(this.cwd + File.separator + fileName);
    }

    @Override
    public File createNewFile(String fileName) {
        File f = new File(this.cwd + File.separator + fileName);

        boolean create;
        try {
            create = f.createNewFile();
        } catch (IOException e) {
            create = false;
        }
        if (!create) {
            f = null;
        }

        return f;
    }
}
