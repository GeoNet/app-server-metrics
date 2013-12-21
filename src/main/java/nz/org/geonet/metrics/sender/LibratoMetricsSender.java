package nz.org.geonet.metrics.sender;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sends metrics to Librato Metrics https://metrics.librato.com/
 *
 * @author Geoff Clitheroe
 *         Date: 8/12/13
 *         Time: 11:28 AM
 */
public class LibratoMetricsSender implements Sender {

    private String source;
    HttpClient httpClient;
    URI uri;

    private final static Logger log = Logger.getLogger(LibratoMetricsSender.class.getSimpleName());

    public LibratoMetricsSender(String userName, String apiKey) {

        try {
            uri = new URI("https://metrics-api.librato.com/v1/metrics");
        } catch (Exception e) {
            log.error(e);
        }

        //  Create an https capable http client with basic auth.
        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);

        HttpField agent = new HttpField("User-Agent", "app-server-metrics/" +
                (getClass().getPackage().getImplementationVersion() != null ? getClass().getPackage().getImplementationVersion() : "development"));

        httpClient.setUserAgentField(agent);

        httpClient.getAuthenticationStore().addAuthentication(new BasicAuthentication(uri, "Librato API", userName, apiKey));

        try {
            httpClient.start();
        } catch (Exception e) {
            log.error(e);
        }

        source = Util.source();
    }

    public void send(String serverType, Map<String, Number> metrics) {

        // Haven't bothered specifying the time for the send to Librato.  It will default to when the
        // metrics are received.

        try {
            ContentResponse response = httpClient.POST(uri).
                    content(new StringContentProvider(jsonString(source, serverType, metrics)), "application/json").timeout(10, TimeUnit.SECONDS).send();

            if (response.getStatus() == 200) {
                log.info("Librato Metrics Sender OK");
            } else {
                log.error("Librato Metrics Sender code: " + response.getStatus());
                log.error("Response content: " + response.getContentAsString());
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Converts the Map to a suitable string for sending to the Librato Metrics api.
     * See http://dev.librato.com/v1/post/metrics
     *
     * @param source     the source name e.g., the server running the JVM.
     * @param serverType the application server name, should be unique across a source.
     * @param metrics    map of metrics.
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
