package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketProtocolClient implements ProtocolClient {
    final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);

    public WebsocketProtocolClient(Config config) throws ProtocolClientException {

    }
}
