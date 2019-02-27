package bgu.spl171.net.test.runners;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public abstract class TestProgram implements Closeable {
    protected String cwd;
    private ProcessBuilder builder;
    private BufferedReader reader;
    private BufferedReader error;
    private BufferedWriter writer;
    private PrintWriter log;

    private Process p;

    private final Scanner scanner;

    private final String log_name;

    private static final String LOG_DIR = "Logs";

    public String getLogName() {
        return log_name;
    }

    public TestProgram(String cwd, String[] runLine, String log_name) {
        this.cwd = cwd;
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdir();
        } else if (!logDir.isDirectory()) {
            System.err.println("PANIC \"Logs\" EXIST AND NOT A DIRECTORY");
            System.exit(-1);
        }
        this.log_name = LOG_DIR + File.separator + log_name;
        scanner = new Scanner(System.in);
        builder = new ProcessBuilder(runLine);
        builder.directory(new File(cwd));

        this.p = null;
        this.reader = null;
        this.writer = null;
        this.error = null;
        this.log = null;
    }

    /**
     * starts running the program and setup streams
     *
     * @return True is the run started successfully
     */
    public boolean start() {
        boolean worked = true;
        try {
            p = builder.start();

            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        } catch (IOException e) {
            worked = false;
            System.err.println("Failed to run program: " + builder.toString());
            e.printStackTrace();
        }

        if (worked) {
            try {
                log = new PrintWriter(this.log_name + ".txt", "UTF-8");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return worked;
    }

    public void write(String line) {
        try {
            writer.write(line + '\n');
            writer.flush();
            log.println("< " + line);
            System.out.println(log_name + ": < " + line);
        } catch (IOException e) {
            System.err.println(log_name + ": Failed to write \"" + line + "\"");
        }
    }

    public void write() {
        String line;
        if ((line = scanner.nextLine()) != null)
            write(line);
    }

    private byte[] BUFFER = new byte[1024];
    private int readByNow = 0;

    public String timeoutRead(long timeoutMillis) {
        String line = null;
        try {
            readByNow = readInputStreamWithTimeout(p.getInputStream(), BUFFER, timeoutMillis, readByNow);
            if (readByNow > 0) {
                if (BUFFER[readByNow - 1] == '\n') {
                    line = new String(BUFFER, 0, readByNow - 1);
                    readByNow = 0;
                }
            }
        } catch (IOException e) {
            System.err.println(log_name + ": Failed to timeoutRead \"" + line + "\"");
        }
        if (line != null && !"".equals(line)) {
            log.println(line);
            System.out.println(log_name + ": " + line);
        }
        return line;
    }

    private static int readInputStreamWithTimeout(InputStream is, byte[] b, long timeoutMillis, int readByNow)
            throws IOException {
        int bufferOffset = readByNow;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
            int readLength = java.lang.Math.min(1, b.length - bufferOffset);
            // can alternatively use bufferedReader, guarded by isReady():
            if (is.available() == 0)
                continue;
            int readResult = is.read(b, bufferOffset, 1);
            if (readResult == -1) break;
            bufferOffset += readResult;
            if (b[bufferOffset - 1] == '\n') break;
        }
        return bufferOffset;
    }

    public void close() {
        log.close();
        try {
            p.destroyForcibly();
        } catch (Exception e) {
            System.err.println("Failed to close program: " + this);
        }
    }

    public abstract File getFile(String fileName);

    /**
     * create a new file in the proper directory (according to implementation)
     *
     * @param fileName Name of file to create
     * @return File object for the new file
     */
    public abstract File createNewFile(String fileName);

    public String read() {
        return readBuffered(reader);
    }

    public String readError() {
        return readBuffered(error);
    }

    private String readBuffered(BufferedReader readReader) {
        String line = "";
        try {
            if ((line = reader.readLine()) != null) {
                log.println(line);
                System.out.println(log_name + ": " + line);
            }
        } catch (IOException e) {
            System.err.println(log_name + ": Failed to read \"" + line + "\"");
        }
        return line;
    }

    public boolean waitExit(long timeoutMillis) {
        boolean res = true;
        if (p != null) {
            try {
                res = p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                res = false;
            }
        }
        return res;
    }
}
