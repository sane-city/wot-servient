package city.sane.wot.thing;

import org.junit.Ignore;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.jadex.JadexProtocolClientFactory;
import city.sane.wot.binding.jadex.JadexProtocolServer;

@Ignore("Jadex platform discovery is unstable")
public class ConsumedThingTest extends AConsumeThingTest {

    @Override
    public Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>> getServientClass() {
        return new Pair(JadexProtocolServer.class, JadexProtocolClientFactory.class);
    }

}
