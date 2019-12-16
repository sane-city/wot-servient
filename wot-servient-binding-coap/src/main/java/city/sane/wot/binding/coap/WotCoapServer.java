package city.sane.wot.binding.coap;

import city.sane.wot.binding.coap.resource.RootResource;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * We have to define our own root resource because we want to dynamically determine the children resources at
 * runtime.
 */
public class WotCoapServer extends org.eclipse.californium.core.CoapServer {
    private final CoapProtocolServer protocolServer;

    public WotCoapServer(CoapProtocolServer protocolServer) {
        super(protocolServer.getBindPort());
        this.protocolServer = protocolServer;
    }

    @Override
    protected Resource createRoot() {
        return new RootResource(this);
    }

    public CoapProtocolServer getProtocolServer() {
        return protocolServer;
    }
}
