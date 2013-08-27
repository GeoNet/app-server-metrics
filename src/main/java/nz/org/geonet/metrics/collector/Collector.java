package nz.org.geonet.metrics.collector;

import java.util.HashMap;

/**
 * Collector interface.
 *
 * @author Geoff Clitheroe
 * Date: 8/16/13
 * Time: 12:38 PM
 */
public interface Collector {

    /**
     * Gathers metrics about the current application server and JVM.
     *
     * @return a map of metric names and values.
     */
    HashMap<String, Number> gather();

    /**
     * The type of the application server e.g., Jetty.  Suitable for identifying the JVM
     * where there are multiple JVMs running on a server.
     *
     * @return the type of the application server.
     */
    String serverType();

    /**
     * @return interval that metrics should be collected at in millis.
     */
    public long collectionInterval();
}

