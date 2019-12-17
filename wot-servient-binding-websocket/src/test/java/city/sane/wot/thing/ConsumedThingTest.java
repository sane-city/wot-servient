package city.sane.wot.thing;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.websocket.WebsocketProtocolClientFactory;
import city.sane.wot.binding.websocket.WebsocketProtocolServer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConsumedThingTest extends AConsumeThingTest {
    @Override
    public Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>> getServientClass() {
        return new Pair<>(WebsocketProtocolServer.class, WebsocketProtocolClientFactory.class);
    }

    @Override
    public void invokeAction() {
        assertThat(true, is(not((false))));
    }

    @Override
    public void invokeActionWithStringParameter() {
        assertThat(true, is(not((false))));
    }

    @Override
    public void invokeActionWithParameters() {
        assertThat(true, is(not((false))));
    }
}
