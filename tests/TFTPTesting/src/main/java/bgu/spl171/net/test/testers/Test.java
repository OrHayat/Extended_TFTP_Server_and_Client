package bgu.spl171.net.test.testers;

import java.io.Closeable;

/**
 * Has a pre / post and tests functions
 * Pre: init
 * Test: tests
 * Post: close
 */
public interface Test extends Closeable {
    public void init();
    public int test();
}
