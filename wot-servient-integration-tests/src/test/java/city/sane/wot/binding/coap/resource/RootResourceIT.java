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
    private WotCoapServer server;

    @Before
    public void setup() {
        server = new WotCoapServer(new CoapProtocolServer(ConfigFactory.load()));
        server.start();
    }

    @After
    public void teardown() {
        server.stop();
    }

    @Test
    public void getAllThings() throws ContentCodecException {
        CoapClient client = new CoapClient("coap://localhost:" + server.getPort());
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
        CoapClient client = new CoapClient("coap://localhost:" + server.getPort());
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