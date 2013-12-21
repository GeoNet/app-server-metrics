package nz.org.geonet.metrics.sender;

//import com.sun.org.apache.xml.internal.security.utils.Base64;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sends metrics to Hosted Graphite https://www.hostedgraphite.com
 *
 * @author Geoff Clitheroe
 *         Date: 8/12/13
 *         Time: 11:28 AM
 */
public class HostedGraphiteSender implements Sender {

    private String source;
    HttpClient httpClient;
    String authHeader;

    private final static Logger log = Logger.getLogger(HostedGraphiteSender.class.getSimpleName());

    public HostedGraphiteSender(String apiKey) {

        source = Util.source();

        //  Create an https capable http client.
        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);

        HttpField agent = new HttpField("User-Agent", "app-server-metrics/" +
                (getClass().getPackage().getImplementationVersion() != null ? getClass().getPackage().getImplementationVersion() : "development"));

        httpClient.setUserAgentField(agent);

        try {
            httpClient.start();
        } catch (Exception e) {
            log.error(e);
        }

        this.authHeader = "Basic " + B64Code.encode(apiKey);
    }

    public void send(String serverType, Map<String, Number> metrics) {
        // Haven't bothered specifying the time for the send to Hosted Graphite.  It will default to when the
        // metrics are received.
        try {
            ContentResponse response = httpClient.POST("https://hostedgraphite.com/api/v1/sink").
                    header("Authorization", authHeader).
                    content(new StringContentProvider(graphiteString(source, serverType, metrics))).timeout(10, TimeUnit.SECONDS).send();

            if (response.getStatus() == 202) {
                log.info("Hosted Graphite Sender OK");
            } else {
                log.error("Graphite Sender code: " + response.getStatus());
                log.error("Response content: " + response.getContentAsString());
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    String graphiteString(String source, String serverType, Map<String, Number> metrics) {
        String graphite = "";

        for (String key : metrics.keySet()) {
            graphite = graphite + source + "." + serverType + "." + key + " " + metrics.get(key) + "\n";
        }

        return graphite;
    }

}
