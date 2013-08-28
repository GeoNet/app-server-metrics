# App Server Metrics

Collect JVM and other metrics for your Java web application server (e.g., Jetty, Tomcat) with a simple war install.

## Quick Start

### Librato Metrics

Send metrics to Librato Metrics (https://metrics.librato.com/) every sixty seconds.  See the dashboard images under `screen-shots`.

* Create a Librato Metrics account https://metrics.librato.com/
* Create a key with Record Access for the Librato Metrics account.
* Put the Librato user name and token in a file `/etc/sysconfig/webapp.app-server-metrics.properties`:

```
  librato.user=some.user@mail.com
  librato.api.key=XXX
```

* Download a war from http://geonet.artifactoryonline.com/geonet/public-releases/nz/org/geonet/app-server-metrics/
* Deploy the app-server-metrics war to you app server at `/app-server-metrics`.
* Log into Librato Metrics and create dashboards.  The metrics are sent with the host short name as the source and the app
server name added to the metric name.  This makes them very suitable for using with dynamic instruments and dashboards.

### Other Metrics Targets

There is Sender to output to stderr which is most useful for development.  If you want other targets see Adding Other Senders below.
 You only need to implement one method and add to the SenderFactory.

## General Options

### Properties file

The location of the properties file can be configured with a JVM property:

```
-Dwebapp.app-server-metrics.properties=/path/to/properties.file
```

### Collection Interval

The collection interval (in millis) can be set in the properties file.  The default is 60000 (60 seconds).

```
...
collection.interval=60000
```

## Security

Jolokia is used for the HTTP-JMX bridge.  Access to the embedded Jolokia servlet is restricted to 127.0.0.1 only by the
file `src/main/resources/jolokia-access.xml

See also http://www.jolokia.org/reference/html/security.html

The application has no other ServletContexts.  A ServletContextListener is used for collecting
metrics (see `nz.org.geonet.metrics.Listener`).

## Supported Servers

The application will collect basic JVM metrics for any server that will run the war.
Where it knows how the application will collect additional metrics about the server.

The application has been tested on (and collects additional metrics for)

* Jetty 9.0.3.v20130506
* Tomcat 6.0.37

If the server is not Jetty or Tomcat then the name 'UnknownJVM' is used for identifying the server in metrics.

The app server that the application is deployed into must have an http listener on 8080 that accepts connections from localhost
and the application must be deployed at the context /app-server-metrics

### Jetty Specifics

* Jetty needs to have JMX enabled.
* If the Jetty statistics handler is enabled then more metrics such as requests per second and response codes are collected.

## Database Connection Pools

### Tomcat JNDI

If there is are database connection pool(s) specified in Tomcat via JNDI then metrics for these will be gathered.

## Under the Covers.

Jolokia (http://www.jolokia.org/) is used for the HTTP-JMX bridge and client.  Jolokia is awesome!

## Adding other Senders

A Sender is used to get the metrics to the visualisation end point.  Additional Senders can be added by;

* implementing `nz.org.geonet.metrics.sender.Sender`
* and creating that Sender from `nz.org.geonet.metrics.sender.SenderFactory` when appropriate configuration is available.

## Adding Collectors for other Application Servers

Collectors collect JVM and other app server metrics.  Additional Collectors can be added by;

* Add the application server type to `nz.org.geonet.metrics.collector.ServerType`
* Implementing `nz.org.geonet.metrics.collector.Collector`
* Adding config to `nz.org.geonet.metrics.collector.CollectorFactory` to create the Collector based on the result of
calling `nz.org.geonet.metrics.collector.MetricsClient.serverType()`

***Note:*** the application does not currently consider application server version and is only unique across application server name.
If you run multiple versions of the same application server on the same host then you will need to do more work.

## Development

Run the war using the embedded Jetty class with a Sender that outputs to std err every 2000 millis with:

```
 mvn test-compile exec:java \
   -Dwebapp.app-server-metrics.sender.stderr=true \
   -Dwebapp.app-server-metrics.collection.interval=2000
```

The same properties can be set for developing with an external Tomcat install.
