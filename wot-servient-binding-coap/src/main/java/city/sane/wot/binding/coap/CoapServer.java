package city.sane.wot.binding.coap;

import city.sane.wot.binding.coap.resource.RootResource;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * We have to define our own root resource because we want to dynamically determine the children resources at
 * runtime.
 */
public class CoapServer extends org.eclipse.californium.core.CoapServer {
    private final CoapProtocolServer protocolServer;

    public CoapServer(CoapProtocolServer protocolServer) {
        super(protocolServer.getBindPort());
        this.protocolServer = protocolServer;
    }

    protected Resource createRoot() {
        return new RootResource(this);
    }

    public CoapProtocolServer getProtocolServer() {
        return protocolServer;
    }
}
