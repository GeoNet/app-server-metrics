package nz.org.geonet.metrics.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Collects metrics for the Jetty web server.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 12:39 PM
 */
public class JettyCollector implements Collector {

    private boolean jettyJMX = false;
    private boolean jettyStatistics = false;
    private final long collectionInterval;

    private List<Collector> collectors;

    private final MetricsClient metricsClient;

    public JettyCollector(MetricsClient metricsClient, long collectionInterval) {
        this.metricsClient = metricsClient;
        this.collectionInterval = collectionInterval;

        collectors = new ArrayList<Collector>();
        collectors.add(new JVMCollector(metricsClient, collectionInterval));

        JdbcDataSourceCollector dataSourceCollector = new JdbcDataSourceCollector(metricsClient, serverType(), collectionInterval);
        if (dataSourceCollector.hasDataSources()) collectors.add(dataSourceCollector);

        jettyJMX = metricsClient.enabled("org.eclipse.jetty.util.thread:type=queuedthreadpool,id=0");
        if (jettyJMX) {
            jettyStatistics = metricsClient.enabled("org.eclipse.jetty.server.handler:type=statisticshandler,id=0");
        }
    }

    public String serverType() {
        return ServerType.Jetty.name();
    }

    public HashMap<String, Number> gather() {
        HashMap<String, Number> metrics = new HashMap<String, Number>();

        for (Collector collector : collectors) {
            metrics.putAll(collector.gather());
        }

        if (jettyJMX) {
            metrics.putAll(jettyQueuedThreadPool());
        }

        if (jettyStatistics) {
            metrics.putAll(jettyStatistics());
        }

        return metrics;
    }

    public long collectionInterval() {
        return collectionInterval;
    }

    /**
     * @return metrics about the size of the queued thread pool.
     */
    HashMap<String, Number> jettyQueuedThreadPool() {

        HashMap<String, Number> responseMap = metricsClient.read("org.eclipse.jetty.util.thread:type=queuedthreadpool,id=0", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("idleThreads")) {
            result.put("Queuedthreadpool.idleThreads", responseMap.get("idleThreads"));
        }

        if (responseMap.containsKey("threads")) {
            result.put("Queuedthreadpool.threads", responseMap.get("threads"));
        }

        if (responseMap.containsKey("maxThreads")) {
            result.put("Queuedthreadpool.maxThreads", responseMap.get("maxThreads"));
        }

        return result;
    }

    /**
     * Gathers statistics such as requests per second etc.  The statisticshandler must be enabled.
     *
     * @return statistics from the Jetty statistics handler e.g., requests per second etc.
     */
    HashMap<String, Number> jettyStatistics() {
        HashMap<String, Number> responseMap = metricsClient.read("org.eclipse.jetty.server.handler:type=statisticshandler,id=0", null);

        // Reset the Jetty stats counters.
        metricsClient.exec("org.eclipse.jetty.server.handler:type=statisticshandler,id=0", "statsReset");

        HashMap<String, Number> result = new HashMap<String, Number>();

        double seconds = 0.0;

        if (responseMap.containsKey("statsOnMs")) {
            seconds = (Long) responseMap.get("statsOnMs") / 1000.0;
        }

        if (responseMap.containsKey("requestTimeMean")) {
            result.put("Statistics.requestTimeMean", responseMap.get("requestTimeMean"));
        }

        if (responseMap.containsKey("requestTimeMax")) {
            result.put("Statistics.requestTimeMax", responseMap.get("requestTimeMax"));
        }

        if (seconds != 0.0) {
            if (responseMap.containsKey("requests")) {
                result.put("Statistics.requests", Math.round(((Long) responseMap.get("requests") / seconds)));
            }

            if (responseMap.containsKey("responses2xx")) {
                result.put("Statistics.responses2xx", Math.round(((Long) responseMap.get("responses2xx") / seconds)));
            }

            if (responseMap.containsKey("responses3xx")) {
                result.put("Statistics.responses3xx", Math.round(((Long) responseMap.get("responses3xx") / seconds)));
            }

            if (responseMap.containsKey("responses4xx")) {
                result.put("Statistics.responses4xx", Math.round(((Long) responseMap.get("responses4xx") / seconds)));
            }

            if (responseMap.containsKey("responses5xx")) {
                result.put("Statistics.responses5xx", Math.round(((Long) responseMap.get("responses5xx") / seconds)));
            }
        }

        return result;
    }
}
