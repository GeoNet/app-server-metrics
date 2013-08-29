package nz.org.geonet.metrics.collector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Geoff Clitheroe
 *         Date: 8/29/13
 *         Time: 11:49 AM
 */
public class JdbcDataSourceCollector implements Collector {

    private final long collectionInterval;
    private final Map<String, String> jdbcDataSource;
    private boolean dataSourceJDBC = false;

    private final MetricsClient metricsClient;
    private final String serverName;

    // TODO - this has only been tested using org.apache.commons.dbcp.BasicDataSource needs testing with other commection pools.
    /**
     * Retrieve metrics for database connection pools that have been exposed as an mbean named
     *  jdbcDataSource:name=yourPreferredNameHere
     *
     *  See the README for how to do this with spring and dbcp.
     *
     * @param metricsClient
     * @param serverName
     * @param collectionInterval
     */
    public JdbcDataSourceCollector(MetricsClient metricsClient, String serverName, long collectionInterval) {

        this.metricsClient = metricsClient;
        this.collectionInterval = collectionInterval;
        this.serverName = serverName;

        // Search for any database connections exposed as jdbcDataSource.  Build a short name from the mbean names.
        //
        // Initialising this here means a server restart will be needed to find
        // any data source changes.  This may need revising.
        jdbcDataSource = new HashMap<String, String>();
        for (String source : metricsClient.search("jdbcDataSource:name=*")) {
            for (String part : source.split(":")) {
                if (part.startsWith("name=")) {
                    jdbcDataSource.put(part.replaceFirst("name=", ""), source);
                }
            }
        }

        if (jdbcDataSource.size() > 0) {
            dataSourceJDBC = true;
        }
    }

    public HashMap<String, Number> gather() {
        HashMap<String, Number> metrics = new HashMap<String, Number>();

        if (dataSourceJDBC) {
            for (String name : jdbcDataSource.keySet()) {
                metrics.putAll(dataSource(name, jdbcDataSource.get(name)));
            }
        }

        return metrics;
    }

    /**
     *
     * @return true if any jdbc data sources are found.
     */
    public boolean hasDataSources() {
        return dataSourceJDBC;
    }

    HashMap<String, Number> dataSource(String name, String mbeanName) {

        // Not quite sure why we have to specify numIdle etc as attributes - they look like path variables.
        HashMap<String, Number> responseMap = metricsClient.read(mbeanName, "NumIdle,NumActive,MaxActive");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("NumIdle")) {
            result.put(name + ".numIdle", responseMap.get("NumIdle"));
        }

        if (responseMap.containsKey("NumActive")) {
            result.put(name + ".numActive", responseMap.get("NumActive"));
        }

        if (responseMap.containsKey("MaxActive")) {
            result.put(name + ".maxActive", responseMap.get("MaxActive"));
        }

        return result;
    }

    public String serverType() {
        return this.serverName;
    }

    public long collectionInterval() {
       return this.collectionInterval;
    }
}
