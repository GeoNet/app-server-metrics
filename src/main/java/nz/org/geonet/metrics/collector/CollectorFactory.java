package nz.org.geonet.metrics.collector;

import nz.org.geonet.metrics.exception.ConnectionException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Creates Collectors.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 12:54 PM
 */
public class CollectorFactory {

    private final static Logger log = Logger.getLogger(CollectorFactory.class.getSimpleName());

    /**
     * Configures a suitable collector.  The configuration is based on properties obtained from
     * defaults, a properties file from the file system, or System properties.
     *
     * @return a collector that can be used to gather metrics about the application server.
     * @throws ConnectionException if a connection to the embedded Jolokia JMX bridge can't be obtained.
     */
    public static Collector getCollector() throws ConnectionException {

        long collectionInterval = 60000;

        String propertiesFile = "/etc/sysconfig/webapp.app-server-metrics.properties";

        if (System.getProperty("webapp.app-server-metrics.properties") != null) {
            propertiesFile = System.getProperty("webapp.app-server-metrics.properties");
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream((propertiesFile)));
            if (properties.getProperty("collection.interval") != null)
                collectionInterval = Long.parseLong(properties.getProperty("collection.interval"));
        } catch (Exception ex) {
            log.warn("Problem reading properties file.");
        }

        if (System.getProperty("webapp.app-server-metrics.collection.interval") != null)
            collectionInterval = Long.parseLong(System.getProperty("webapp.app-server-metrics.collection.interval"));

        // Default port 8080.  If config options are needed do it here from the properties file.
        MetricsClient metricsClient = new MetricsClient("http://localhost:8080/app-server-metrics/jolokia/");

        Collector collector;

        String appServerType = metricsClient.serverType();

        if (ServerType.Jetty.name().equals(appServerType)) {
            collector = new JettyCollector(metricsClient, collectionInterval);
        } else if (ServerType.Tomcat.name().equals(appServerType)) {
            collector = new TomcatCollector(metricsClient, collectionInterval);
        } else {
            log.warn("Can't determine the app server type - using " + ServerType.UnknownJVM.name());
            collector = new JVMCollector(metricsClient, collectionInterval);
        }

        return collector;
    }


}
