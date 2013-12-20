package nz.org.geonet.metrics.sender;

import nz.org.geonet.metrics.exception.SenderException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Creates Senders.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 1:49 PM
 */
public class SenderFactory {

    private final static Logger log = Logger.getLogger(SenderFactory.class.getSimpleName());

    /**
     * Creates Sender configured based on properties.
     *
     * @return a Sender
     * @throws SenderException if can't find enough properties to configure a Sender.
     */
    public static Sender getSender() throws SenderException {

        Sender sender;

        String libratoUser = null;
        String libratoApiKey = null;
        String hostedGraphiteApiKey = null;

        String propertiesFile = "/etc/sysconfig/webapp.app-server-metrics.properties";

        if (System.getProperty("webapp.app-server-metrics.properties") != null) {
            propertiesFile = System.getProperty("webapp.app-server-metrics.properties");
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream((propertiesFile)));
            libratoUser = properties.getProperty("librato.user");
            libratoApiKey = properties.getProperty("librato.api.key");
            hostedGraphiteApiKey = properties.getProperty("hostedgraphite.api.key");
        } catch (Exception ex) {
            log.warn("Problem reading properties file");
        }

        if (System.getProperty("webapp.app-server-metrics.sender.stderr") != null && "true".equals(System.getProperty("webapp.app-server-metrics.sender.stderr"))) {
            sender = new StdErrSender();
            log.info("creating a StdErr sender");
        } else if (libratoUser != null && libratoApiKey != null) {
            sender = new LibratoMetricsSender(libratoUser, libratoApiKey);
            log.info("creating a Librato sender");
        } else if (hostedGraphiteApiKey != null) {
            sender = new HostedGraphiteSender(hostedGraphiteApiKey);
            log.info("creating a Hosted Graphite Sender");
        } else {
            throw new SenderException("Cannot find enough properties to configure a sender.");
        }

        return sender;
    }
}
