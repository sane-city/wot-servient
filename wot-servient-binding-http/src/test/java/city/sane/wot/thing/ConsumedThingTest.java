package city.sane.wot.thing;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.http.HttpProtocolClientFactory;
import city.sane.wot.binding.http.HttpProtocolServer;

public class ConsumedThingTest extends AConsumeThingTest {

    @Override
    public Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>> getServientClass() {
       return new Pair<>(HttpProtocolServer.class, HttpProtocolClientFactory.class);
    }

    
}

