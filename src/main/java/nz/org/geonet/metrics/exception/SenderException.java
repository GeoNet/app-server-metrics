package nz.org.geonet.metrics.exception;

/**
 * Sender Exception
 *
 * @author Geoff Clitheroe
 * Date: 8/16/13
 * Time: 8:52 PM
 */
public class SenderException extends Exception {

    @SuppressWarnings("SameParameterValue")
    public SenderException(String message) {
        super(message);
    }
}
