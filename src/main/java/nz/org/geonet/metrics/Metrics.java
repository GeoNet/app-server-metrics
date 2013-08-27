package nz.org.geonet.metrics;

import nz.org.geonet.metrics.collector.Collector;
import nz.org.geonet.metrics.collector.CollectorFactory;
import nz.org.geonet.metrics.exception.ConnectionException;
import nz.org.geonet.metrics.exception.SenderException;
import nz.org.geonet.metrics.sender.Sender;
import nz.org.geonet.metrics.sender.SenderFactory;

/**
 * Gathers and sends metrics.
 *
 * @author Geoff Clitheroe
 * Date: 8/11/13
 * Time: 3:13 PM
 */
class Metrics {

    private Sender sender;
    private Collector collector;

    public Metrics() throws ConnectionException, SenderException {
        collector = CollectorFactory.getCollector();
        sender = SenderFactory.getSender();
    }

    /**
     * Collects application server metrics and sends them to the configured output.
     */
    public void monitor() {
        sender.send(collector.serverType(), collector.gather());
    }

    /**
     * @return the number of milliseconds to wait between gathering metrics.
     */
    public long getSleepInterval() {
        return collector.collectionInterval();
    }
}
