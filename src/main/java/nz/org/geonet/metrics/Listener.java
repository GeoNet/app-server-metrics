package nz.org.geonet.metrics;

import nz.org.geonet.metrics.exception.ConnectionException;
import nz.org.geonet.metrics.exception.SenderException;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Collects and sends metrics when the webapp is deployed.
 *
 * @author Geoff Clitheroe
 * Date: 8/10/13
 * Time: 6:26 PM
 */
public class Listener implements ServletContextListener {

    private final static Logger log = Logger.getLogger(Listener.class.getSimpleName());


    public void contextInitialized(ServletContextEvent sce) {
        gatherMetrics();
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    void gatherMetrics() {

        new Thread() {

            public void run() {

                try {
                    Metrics metrics = new Metrics();
                    long sleepInterval = metrics.getSleepInterval();

                    //noinspection InfiniteLoopStatement
                    while (true) {

                        metrics.monitor();

                        try {
                            Thread.sleep(sleepInterval);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } catch (ConnectionException e) {
                    log.error(e);
                } catch (SenderException e) {
                    log.error(e);
                }

            }

        }.start();
    }

}