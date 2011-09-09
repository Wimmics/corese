package wsimport.KgramWS;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.MTOMFeature;

/**
 * <p>This class overrides the RemoteProducerService in order to look for the WSDL file whithin the classpath (jar),
 * rather than in the file system which is the default jax-ws code.</p>
 * <p>The server URL is then simply overriden by a property.</p>
 */
public class RemoteProducerServiceClient extends RemoteProducerService {

    // service endpoint URL
    private static String endpoint;

    // Create a client, parsing the given WSDL file
    protected RemoteProducerServiceClient(URL serviceURL) {
        super(serviceURL, new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "RemoteProducerService"));
        endpoint = serviceURL.toString();
    }

    /**
     * Create a client connecting to the SiteServer Web Service which URL is given.
     * @param serviceUrl Web Service URL.
     * @return web service client stub
     */
    public static RemoteProducer getPort(URL serviceUrl) {
        RemoteProducer rp = new RemoteProducerServiceClient(serviceUrl).getRemoteProducerPort(new MTOMFeature(true, 1024));
        rp.setEndpoint(serviceUrl.toString());
        return rp;
    }

    /**
     * Create a client connecting to the SiteServer Web Service which URL is given.
     * @param serviceUrl Web Service URL.
     * @return web service client stub
     */
    public static RemoteProducer getPort(String serviceUrl) throws MalformedURLException {
        RemoteProducer rp =  new RemoteProducerServiceClient(new URL(serviceUrl)).getRemoteProducerPort(new MTOMFeature(true, 1024));
        rp.setEndpoint(serviceUrl);
        return rp;
    }

    /**
     * Returns service endpoint URL (only works once it has been connected).
     * @return service endpoint URL or null before connection
     */
    public static String getEndpoint() {
        return endpoint;
    }
}
