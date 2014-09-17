package nz.org.geonet.metrics.collector;

import java.util.HashMap;

/**
 * Collects basic JVM metrics.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 12:30 PM
 */
public class JVMCollector implements Collector {

    private final MetricsClient metricsClient;
    private final long collectionInterval;

    public JVMCollector(MetricsClient metricsClient, long collectionInterval) {
        this.metricsClient = metricsClient;
        this.collectionInterval = collectionInterval;
    }

    public HashMap<String, Number> gather() {
        HashMap<String, Number> metrics = jvmHeapMemoryUsage();
        metrics.putAll(jvmPermGen());
        metrics.putAll(jvmNonHeapMemoryUsage());
        metrics.putAll(jvmCodeCache());
        metrics.putAll(jvmEdenSpace());
        metrics.putAll(jvmSurvivorSpace());
        metrics.putAll(jvmOldGen());
        metrics.putAll(jvmThreads());
        metrics.putAll(classLoading());
        metrics.putAll(cpuLoad());

        return metrics;
    }

    public long collectionInterval() {
        return collectionInterval;
    }

    public String serverType() {
        return ServerType.UnknownJVM.name();
    }

    HashMap<String, Number> cpuLoad() {
        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=OperatingSystem", "ProcessCpuLoad,SystemCpuLoad");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("ProcessCpuLoad")) {
            result.put("ProcessCpuLoad", responseMap.get("ProcessCpuLoad"));
        }

        if (responseMap.containsKey("SystemCpuLoad")) {
            result.put("SystemCpuLoad", responseMap.get("SystemCpuLoad"));
        }

        return result;
    }

    HashMap<String, Number> jvmHeapMemoryUsage() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=Memory", "HeapMemoryUsage");

        HashMap<String, Number> result = new HashMap<String, Number>();

            if (responseMap.containsKey("used")) {
                result.put("HeapMemoryUsage.used", responseMap.get("used"));
            }

            if (responseMap.containsKey("max")) {
                result.put("HeapMemoryUsage.max", responseMap.get("max"));
            }

        return result;
    }

    HashMap<String, Number> jvmNonHeapMemoryUsage() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=Memory", "NonHeapMemoryUsage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("NonHeapMemoryUsage.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("NonHeapMemoryUsage.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmPermGen() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=MemoryPool,name=PS Perm Gen", "Usage");

        HashMap<String, Number> result = new HashMap<String, Number>();

            if (responseMap.containsKey("used")) {
                result.put("PermGen.used", responseMap.get("used"));
            }

            if (responseMap.containsKey("max")) {
                result.put("PermGen.max", responseMap.get("max"));
            }

        return result;
    }

    HashMap<String, Number> jvmEdenSpace() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=MemoryPool,name=PS Eden Space", "Usage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("EdenSpace.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("EdenSpace.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmSurvivorSpace() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=MemoryPool,name=PS Survivor Space", "Usage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("SurvivorSpace.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("SurvivorSpace.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmOldGen() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=MemoryPool,name=PS Old Gen", "Usage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("OldGen.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("OldGen.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmCodeCache() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=MemoryPool,name=Code Cache", "Usage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("CodeCache.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("CodeCache.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmThreads() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=Threading", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("ThreadCount")) {
            result.put("Threading.used", responseMap.get("ThreadCount"));
        }

        if (responseMap.containsKey("DaemonThreadCount")) {
            result.put("Threading.max", responseMap.get("DaemonThreadCount"));
        }

        return result;
    }

    HashMap<String, Number> classLoading() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=ClassLoading", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("TotalLoadedClassCount")) {
            result.put("Classes.totalLoaded", responseMap.get("TotalLoadedClassCount"));
        }

        return result;
    }

}
