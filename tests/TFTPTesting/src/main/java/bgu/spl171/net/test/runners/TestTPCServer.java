package bgu.spl171.net.test.runners;

/**
 * Created by matan on 1/24/2017.
 */
public class TestTPCServer extends TestServer {

    protected static final String TPC_RUN_LINE = "-Dexec.mainClass=bgu.spl171.net.impl.TFTPtpc.TPCMain";

    public TestTPCServer(String cwd, String log_name, int port) {
        super(cwd, new String[] {TPC_RUN_LINE, "-Dexec.args=" + port}, log_name);
    }
}
