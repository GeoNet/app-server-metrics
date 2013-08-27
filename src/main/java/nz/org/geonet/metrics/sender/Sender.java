package nz.org.geonet.metrics.sender;

import java.util.Map;

/**
 * Sender interface
 *
 * @author Geoff Clitheroe
 * Date: 8/16/13
 * Time: 1:48 PM
 */
public interface Sender {

    /**
     * Send metrics to the collector
     * @param serverType the appServer type e.g., Jetty.
     * @param metrics the metrics to send.
     * @see nz.org.geonet.metrics.collector.Collector, nz.org.geonet.metrics.collector.ServerType
     */
    void send(String serverType, Map<String, Number> metrics);
}
