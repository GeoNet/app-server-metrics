package nz.org.geonet.metrics.sender;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sends metrics to Librato Metrics https://metrics.librato.com/
 *
 * @author Geoff Clitheroe
 * Date: 8/12/13
 * Time: 11:28 AM
 */
public class LibratoMetricsSender implements Sender {

    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    private final HttpPost httpPost = new HttpPost("https://metrics-api.librato.com/v1/metrics");
    private String source;

    private final static Logger log = Logger.getLogger(LibratoMetricsSender.class.getSimpleName());

    public LibratoMetricsSender(String userName, String apiKey) {

        httpClient.getCredentialsProvider().setCredentials(new AuthScope("metrics-api.librato.com", 443),
                new UsernamePasswordCredentials(userName, apiKey));

        httpPost.setHeader("Content-Type", "application/json");

        try {
            source = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.debug(e);
        }

        if (source == null || "localhost".equals(source.toLowerCase()) || "127.0.0.1".equals(source)) {
            source = UUID.randomUUID().toString();
            log.warn("Can't find a meaningful hostname - using " + source);
        }
    }

    public void send(String serverType, Map<String, Number> metrics) {

        // Haven't bothered specifying the time for the send to Librato.  It will default to when the
        // metrics are received.

        try {
            httpPost.setEntity(new ByteArrayEntity(jsonString(source, serverType, metrics).getBytes("UTF8")));

            HttpResponse response = httpClient.execute(httpPost);

            // Log the response
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String output = "";
            String buffer;

            while ((buffer = in.readLine()) != null) {
                output += buffer + "\n";
            }

            in.close();

            if (response.toString().toUpperCase().matches(".*200 OK.*")) {
                log.info("Librato Metrics Sender OK");
            } else {
                log.error("Librato Metrics: " + response.toString());
                log.error("OUTPUT: " + output);
            }

        } catch (Exception e) {
            log.debug(e);
        }

    }

    /**
     * Converts the Map to a suitable string for sending to the Librato Metrics api.
     * See http://dev.librato.com/v1/post/metrics
     *
     * @param source the source name e.g., the server running the JVM.
     * @param serverType the application server name, should be unique across a source.
     * @param metrics map of metrics.
     * @return a JSON string suitable for sending to the Librato Metrics api.
     */
    String jsonString(String source, String serverType, Map<String, Number> metrics) {
        List<Gauge> gaugeList = new ArrayList<Gauge>();

        for (String key : metrics.keySet()) {
            gaugeList.add(new Gauge(source, serverType + "." + key, metrics.get(key)));
        }

        Gauges gauges = new Gauges(gaugeList);

        return new Gson().toJson(gauges);
    }

    private static class Gauges {
        @SuppressWarnings("UnusedDeclaration")
        private final List<Gauge> gauges;

        public Gauges(List<Gauge> gauges) {
            this.gauges = gauges;
        }
    }

    private static class Gauge {
        @SuppressWarnings("UnusedDeclaration")
        private final String name;
        @SuppressWarnings("UnusedDeclaration")
        private final String source;
        @SuppressWarnings("UnusedDeclaration")
        private final Number value;

        public Gauge(String source, String name, Number value) {
            this.source = source;
            this.name = name;
            this.value = value;
        }
    }


}
