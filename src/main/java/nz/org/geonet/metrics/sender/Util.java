package nz.org.geonet.metrics.sender;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Geoff Clitheroe
 *         Date: 12/21/13
 *         Time: 11:48 AM
 */
public class Util {

    private final static Logger log = Logger.getLogger(Util.class.getSimpleName());

    /**
     * Find a suitable source name for the metrics.  Uses the server name if possible e.g.,
     * host.net  If this can't be determined returns a random string so as to avoid clashes.
     *
     * @return a source name for the metrics.
     */
    public static String source() {
        String source = null;

        try {
            source = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.debug(e);
        }

        if (source == null || "localhost".equals(source.toLowerCase()) || "127.0.0.1".equals(source)) {
            source = UUID.randomUUID().toString();
            log.warn("Can't find a meaningful hostname - using " + source);
        }

        return source;
    }
}
