package nz.org.geonet.metrics.sender;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Geoff Clitheroe
 *         Date: 8/26/13
 *         Time: 9:11 AM
 */
public class StdErrSender implements Sender {

    String source;

    StdErrSender() {
        source = Util.source();
    }

    public void send(String serverType, Map<String, Number> metrics) {
        for (String key : metrics.keySet()) {
            System.err.println(source + "." + serverType + "." + key + " " + metrics.get(key));
        }
    }
}
