package bgu.spl171.net.test;

import bgu.spl171.net.test.testers.TestSuite;
import bgu.spl171.net.test.testers.TestSuiteSpl171Ass3;

import java.io.*;
import java.util.Map;

public class TestSpl171Ass3 {
    /**
     * Start a tests suite
     *
     * @param args - [0] Path to student directory; [1] Port; [2] Output file for score map;
     *             [3] group number (Optional - will be printed first before score); [4] Add header (optional - print header line before score map line)
     */
    public static void main(String[] args) {
       String userPath = ".." + File.separator + "Solution";
       // String userPath = "/home/matan/temp/203";
       String scoreMapFile = "scoreMap.csv";
       FileOutputStream fos = null;
       boolean addHeader = false;
       Integer groupNo = null;
       int port = 7777;
       if (args.length < 3) {
           System.err.println("No arguments received, Using default mode. Assume user folder ../Solution/ and port 7777");
           System.err.println("Run Example:\n../ID1_ID2 6478 scores.csv 14336 yes");
           System.err.println("To avoid header just don't use the fifth parameter as such:\n../ID1_ID2 6478 scores.csv 14336\n\n");
       } else {
           userPath = args[0];
           port = Integer.valueOf(args[1]);
           scoreMapFile = args[2];
           if (args.length > 3) {
               groupNo = Integer.valueOf(args[3]);
           }
           if (args.length > 4) {
               addHeader = true;
           }
       }

        try {
            File f = new File(scoreMapFile);
            if (!f.exists()) {
                f.createNewFile();
            }
            fos = new FileOutputStream(f, true);
        } catch (IOException e) {
            System.err.println("Failed to open output file");
            System.exit(-1);
        }

       TestSuite ts = new TestSuiteSpl171Ass3(userPath, port, groupNo);

       Map<String, Integer> scoreMap = ts.runSuite();
       try {
           writeScoreMap(scoreMap, fos, groupNo, addHeader, ts);
       } catch (IOException e) {
           System.err.println("Failed to print to output file");
           try {
               fos.close();
           } catch (IOException ignore) { }
       }
    }

    /**
     * Print score map to output stream. Also prints to standard output with headers.
     * @param map The score map to print
     * @param os the output stream to print into
     * @param groupNo if not null will be pronted as first parameter
     * @param isHeader True will include the header in the Os stream, false will only print the scores
     * @throws IOException error writing to file
     */
    private static void writeScoreMap(Map<String, Integer> map, OutputStream os, Integer groupNo, boolean isHeader, TestSuite ts) throws IOException {
        String[] testSuite = ts.getTestSuite();
        String header = "";
        String scores = "";
        if (groupNo != null) {
            header += "Group No,";
            scores += groupNo + ",";
        }

        for (int i = 0 ; i < testSuite.length ; ++i) {
            header += testSuite[i];
            scores += map.get(testSuite[i]);
            if (i + 1 < testSuite.length) {
                header += ",";
                scores += ",";
            }
        }

        // Print to screen
        System.out.println(header + "\n" + scores);

        // Print to file
        if (isHeader) {
            os.write(header.getBytes());
            os.write("\n".getBytes());
        }
        os.write(scores.getBytes());
        os.write("\n".getBytes());
    }
}


