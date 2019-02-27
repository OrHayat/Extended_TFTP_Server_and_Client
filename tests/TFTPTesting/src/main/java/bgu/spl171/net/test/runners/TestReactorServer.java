package bgu.spl171.net.test.runners;

/**
 * Created by matan on 1/24/2017.
 */
public class TestReactorServer extends TestServer {
    protected static final String REACTOR_RUN_LINE = "-Dexec.mainClass=bgu.spl171.net.impl.TFTPreactor.ReactorMain";

    public TestReactorServer(String cwd, String log_name, int port) {
        super(cwd, new String[]{REACTOR_RUN_LINE, "-Dexec.args=" + port}, log_name);
    }
}
