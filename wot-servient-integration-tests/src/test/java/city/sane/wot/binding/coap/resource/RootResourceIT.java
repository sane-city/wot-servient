package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.CoapProtocolServer;
import city.sane.wot.binding.coap.WotCoapServer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.ConfigFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class RootResourceIT {
    private CoapServer server;

    @Before
    public void setup() {
        server = new WotCoapServer(new CoapProtocolServer(ConfigFactory.load()));
        server.start();
    }

    @After
    public void teardown() {
        server.stop();

        // TODO: Wait some time after the server has shut down. Apparently the CoAP server reports too early that it was terminated, even though the port is
        //  still in use. This sometimes led to errors during the tests because other CoAP servers were not able to be started because the port was already
        //  in use. This error only occurred in the GitLab CI (in Docker). Instead of waiting, the error should be reported to the maintainer of the CoAP
        //  server and fixed. Because the isolation of the error is so complex, this workaround was chosen.
        try {
            Thread.sleep(1 * 1000L);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void getAllThings() throws ContentCodecException {
        CoapClient client = new CoapClient("coap://localhost:5683");
        CoapResponse response = client.get();

        Assert.assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        Assert.assertEquals(MediaTypeRegistry.APPLICATION_JSON, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new ObjectSchema());
        assertThat(responseValue, instanceOf(Map.class));
    }

    @Test
    public void getAllThingsWithCustomContentType() throws ContentCodecException {
        CoapClient client = new CoapClient("coap://localhost:5683");
        Request request = new Request(CoAP.Code.GET);
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        Assert.assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        Assert.assertEquals(MediaTypeRegistry.APPLICATION_CBOR, response.getOptions().getContentFormat());

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new ObjectSchema());
        assertThat(responseValue, instanceOf(Map.class));
    }
}