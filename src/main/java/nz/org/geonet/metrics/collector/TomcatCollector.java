package nz.org.geonet.metrics.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects metrics for the Jetty web server.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 12:39 PM
 */
public class TomcatCollector implements Collector {

    private boolean httpConnector = false;
    private boolean ajpConnector = false;
    private boolean javaxSql = false;
    private List<Collector> collectors;
    private final long collectionInterval;
    private final Map<String, String> javaxDataSource;

    private final MetricsClient metricsClient;

    public TomcatCollector(MetricsClient metricsClient, long collectionInterval) {
        this.metricsClient = metricsClient;
        this.collectionInterval = collectionInterval;

        collectors = new ArrayList<Collector>();
        collectors.add(new JVMCollector(metricsClient, collectionInterval));

        JdbcDataSourceCollector dataSourceCollector = new JdbcDataSourceCollector(metricsClient, serverType(), collectionInterval);
        if (dataSourceCollector.hasDataSources()) collectors.add(dataSourceCollector);

        httpConnector = metricsClient.enabled("Catalina:type=ThreadPool,name=http-8080");
        ajpConnector = metricsClient.enabled("Catalina:type=ThreadPool,name=jk-8009");

        // Search for any JNDI database connections.  Build a short name from the mbean names.
        //
        // Initialising this here means a server restart will be needed to find
        // any data source changes.  This may need revising.
        javaxDataSource = new HashMap<String, String>();
        for (String source : metricsClient.search("Catalina:type=DataSource,class=javax.sql.DataSource,name=*")) {
            for (String part : source.split(",")) {
                if (part.startsWith("name=")) {
                    javaxDataSource.put(part.replaceFirst("name=", "").replaceAll("/", "-").replaceAll("\"", ""), source);
                }
            }
        }

        if (javaxDataSource.size() > 0) {
            javaxSql = true;
        }

    }

    public String serverType() {
        return ServerType.Tomcat.name();
    }

    public HashMap<String, Number> gather() {
        HashMap<String, Number> metrics = new HashMap<String, Number>();

        for (Collector collector : collectors) {
            metrics.putAll(collector.gather());
        }

        if (httpConnector) {
            metrics.putAll(httpConnector());
        }

        if (ajpConnector) {
            metrics.putAll(ajpConnector());
        }

        if (javaxSql) {
            for (String name : javaxDataSource.keySet()) {
                metrics.putAll(javaxSqlDataSource(name, javaxDataSource.get(name)));
            }
        }

        return metrics;
    }

    public long collectionInterval() {
        return collectionInterval;
    }

    HashMap<String, Number> javaxSqlDataSource(String name, String mbeanName) {


        // Not quite sure why we have to specify numIdle etc as attributes - they look like path variables.
        HashMap<String, Number> responseMap = metricsClient.read(mbeanName, "numIdle,numActive,maxActive");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("numIdle")) {
            result.put(name + ".numIdle", responseMap.get("numIdle"));
        }

        if (responseMap.containsKey("numActive")) {
            result.put(name + ".numActive", responseMap.get("numActive"));
        }

        if (responseMap.containsKey("maxActive")) {
            result.put(name + ".maxActive", responseMap.get("maxActive"));
        }

        return result;
    }

    HashMap<String, Number> httpConnector() {
        HashMap<String, Number> responseMap = metricsClient.read("Catalina:type=ThreadPool,name=http-8080", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("maxThreads")) {
            result.put("http-8080.maxThreads", responseMap.get("maxThreads"));
        }

        if (responseMap.containsKey("currentThreadsBusy")) {
            result.put("http-8080.currentThreadsBusy", responseMap.get("currentThreadsBusy"));
        }

        return result;
    }

    HashMap<String, Number> ajpConnector() {
        HashMap responseMap = metricsClient.read("Catalina:type=ThreadPool,name=jk-8009", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("maxThreads")) {
            result.put("jk-8009.maxThreads", (Long) responseMap.get("maxThreads"));
        }

        if (responseMap.containsKey("currentThreadsBusy")) {
            result.put("jk-8009.currentThreadsBusy", (Long) responseMap.get("currentThreadsBusy"));
        }

        return result;
    }

}
