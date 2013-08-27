package nz.org.geonet;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.lang.management.ManagementFactory;

/**
 * Embedded Jetty for testing.
 *
 * @author Geoff Clitheroe
 * Date: 8/8/13
 * Time: 10:33 AM
 */
public class AppServer {

        public static void main(String[] args) throws Exception {
            Server server = new Server(8080);

            MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
            server.addBean(mbContainer);

            WebAppContext root = new WebAppContext();

            root.setContextPath("/app-server-metrics");
            root.setDescriptor("src/main/webapp/WEB-INF/web.xml");

            root.setResourceBase("src/main/webapp/");

            ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
            contextHandlerCollection.setHandlers(new Handler[] {root});

            server.setHandler(contextHandlerCollection);

            StatisticsHandler statisticsHandler = new StatisticsHandler();
            statisticsHandler.setHandler(server.getHandler());
            server.setHandler(statisticsHandler);

            server.start();
            server.join();
        }

}
