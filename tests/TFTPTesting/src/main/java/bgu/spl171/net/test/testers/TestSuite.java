package bgu.spl171.net.test.testers;

import java.util.Map;

/**
 * Created by matan on 1/24/2017.
 */
public interface TestSuite {
    /**
     * Runs the complete set of tests
     * @return A map containing tests names and scores
     */
    public Map<String, Integer> runSuite();

    /**
     * the list with the names of all the tests in the suite
     * @return A list of names of all the tests in the suite
     */
    public String[] getTestSuite();
}
