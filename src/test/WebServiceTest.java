package test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import web.WebService;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.*;

/** Runs and tests the web interface. */
public class WebServiceTest {
    private static final int PORT = 1234;
    private static Thread thread;

    @BeforeClass
    public static void setUp() throws Exception {
        WebService ws = new WebService(1234);
        thread = new Thread(ws);
        thread.start();
        ws.waitReady();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        thread.interrupt();
    }

    @Test public void testIndex()   throws Exception { testURL("/"); }
    @Test public void testChat()    throws Exception { testURL("/chat/"); }
    @Test public void testLogs()    throws Exception { testURL("/logs/"); }
    @Test public void testFiles()   throws Exception { testURL("/files/"); }
    @Test public void testStats()   throws Exception { testURL("/status/"); }
    @Test public void testThreads() throws Exception { testURL("/threads/"); }

    private void testURL(String requestURL) throws Exception {
        assertTrue(request(requestURL).startsWith("<html>"));
    }

    private String request(String requestUrl) throws Exception {
        String tpl = "http://localhost:%s%s";
        String urlString = String.format(tpl, PORT, requestUrl);
        InputStream is = new URL(urlString).openStream();
        Scanner scanner = new Scanner(is, "UTF-8");
        String out = scanner.useDelimiter("\\A").next();
        scanner.close();
        return out;
    }
}
