package nz.org.geonet.metrics.collector;

import nz.org.geonet.metrics.exception.ConnectionException;
import org.apache.log4j.Logger;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.*;
import org.json.simple.JSONObject;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wraps the connection to Jolokia.
 *
 * @author Geoff Clitheroe
 *         Date: 8/22/13
 *         Time: 2:34 PM
 */
class MetricsClient {

    private final J4pClient client;
    private final static Logger log = Logger.getLogger(MetricsClient.class.getSimpleName());
    private final String URL;

    /**
     * Wraps the connection to Jolokia.
     *
     * @param URL the URL to connect to the Jolokia agent on.
     */
    @SuppressWarnings("SameParameterValue")
    public MetricsClient(String URL) {
        client = J4pClient.url(URL)
                .socketTimeout(5000)
                .build();

        this.URL = URL;
    }

    /**
     * Check if an mbean is enabled (can be queried).
     *
     * @param mbeanName the name of the bean to query for.
     * @return true if the mbean can be connected to.
     */
    boolean enabled(String mbeanName) {
        boolean result = true;

        try {
            J4pReadRequest request = new J4pReadRequest(mbeanName);
            client.execute(request);
        } catch (J4pException e) {
            result = false;
            log.error("Could not connect to Jetty JMX are mbeans enabled?");
        } catch (MalformedObjectNameException e) {
            log.debug(e);
        }

        return result;
    }


    /**
     * Read attributes from an mbean.  Only includes mbean attributes that are numbers in the return.
     *
     * @param mbeanName the mbean to query
     * @param attributeName attributes of the bean to query.  Can be null.
     * @return map of bean attribute names and values.
     */
    HashMap<String, Number> read(String mbeanName, String attributeName) {

        HashMap<String, Number> responseMap = new HashMap<String, Number>();

        try {
            J4pReadRequest request;

            if (attributeName != null) {
                request = new J4pReadRequest(mbeanName, attributeName);
            } else {
                request = new J4pReadRequest(mbeanName);
            }
            J4pReadResponse response = client.execute(request);

            // Oh for type inference.
            for (ObjectName objectName : response.getObjectNames()) {
                for (String attribute : response.getAttributes(objectName)) {

                    if (response.getValue(objectName, attribute) instanceof Number) {
                        responseMap.put(attribute, (Number) response.getValue(objectName, attribute));
                    } else if (response.getValue(objectName, attribute) instanceof JSONObject) {
                        JSONObject jsonObject = response.getValue(objectName, attribute);

                        for (Object key : jsonObject.keySet()) {

                            if (jsonObject.get(key) instanceof Number) {
                                responseMap.put((String) key, (Number) jsonObject.get(key));
                            }
                        }
                    }
                }
            }

        } catch (J4pException e) {
            log.debug(e);
        } catch (MalformedObjectNameException e) {
            log.debug(e);
        }

        return responseMap;
    }

    /**
     * Execute a method on an mbean.
     *
     * @param mbeanName the name of the bean.
     * @param method the name of the method to execute.
     */
    @SuppressWarnings("SameParameterValue")
    void exec(String mbeanName, String method) {
        try {
            J4pExecRequest request = new J4pExecRequest(mbeanName, method);
            client.execute(request);
        } catch (MalformedObjectNameException e) {
            log.debug(e);
        } catch (J4pException e) {
            log.debug(e.getStackTrace());
        }
    }

    /**
     * Connects to Jolokia and tries to introspect the server type e.g., Jetty, Tomcat etc.
     *
     * @return the server type
     * @throws nz.org.geonet.metrics.exception.ConnectionException
     *
     * @see ServerType
     */
    String serverType() throws ConnectionException {
        String result = ServerType.UnknownJVM.name();

        try {
            J4pVersionRequest request = new J4pVersionRequest();

            J4pVersionResponse response = client.execute(request);

            if (response != null && response.getValue() != null) {
                HashMap responseMap = response.getValue();

                if (responseMap.containsKey("info")) {
                    HashMap info = (HashMap) responseMap.get("info");

                    // TODO - don't check the version at the moment.  Don't know how important this is yet.
                    // e.g., {"product":"tomcat","vendor":"Apache","version":"6.0.37"}
                    if (info.containsKey("product") && ServerType.Jetty.name().toLowerCase().matches(info.get("product").toString().toLowerCase())) {
                        result = ServerType.Jetty.name();
                    } else if (info.containsKey("product") && ServerType.Tomcat.name().toLowerCase().matches(info.get("product").toString().toLowerCase())) {
                        result = ServerType.Tomcat.name();
                    }
                }
            }

        } catch (J4pException e) {
            throw new ConnectionException("problem connecting to the Jolokia agent " + URL);
        }

        return result;
    }

    /**
     * Search for names of mbeans matching a pattern.
     *
     * @param mbeanName
     * @return
     */
    @SuppressWarnings("SameParameterValue")
    List<String> search(String mbeanName) {
        List<String> result = new ArrayList<String>();

        try {
            J4pSearchRequest request = new J4pSearchRequest(mbeanName);

            J4pSearchResponse response = client.execute(request);

            if (response != null && response.getMBeanNames() != null && response.getMBeanNames().size() > 0) {
                result.addAll(response.getMBeanNames());
            }

        } catch (MalformedObjectNameException e) {
            log.debug(e);
        } catch (J4pException e) {
            log.debug(e);
        }

        return result;
    }
}
