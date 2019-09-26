package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.ServiceScope;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This Agent is started together with {@link city.sane.wot.binding.jadex.JadexProtocolServer} and is responsible for exposing things. For each exposed Thing
 * a {@link ThingAgent} is created, which is responsible for the interaction with the Thing.
 */
@Agent
@ProvidedServices({
        @ProvidedService(type = ThingsService.class, scope = ServiceScope.PLATFORM)
})
public class ThingsAgent implements ThingsService {
    final static Logger log = LoggerFactory.getLogger(ThingsAgent.class);
    private final Map<String, IExternalAccess> children = new HashMap<>();
    @Agent
    private IInternalAccess agent;
    @AgentArgument("things")
    private Map<String, ExposedThing> things;

    @AgentCreated
    public IFuture<Void> created() {
        log.debug("Agent created");

        return Future.DONE;
    }

    @Override
    public IFuture<IExternalAccess> expose(String id) {
        CreationInfo info = new CreationInfo()
                .setFilenameClass(ThingAgent.class)
                .addArgument("thing", things.get(id));
        IFuture<IExternalAccess> component = agent.createComponent(info);
        component.addResultListener(thingAgent -> children.put(id, thingAgent));

        return component;
    }

    @Override
    public IFuture<Void> destroy(String id) {
        IExternalAccess thingAgent = children.get(id);

        Future<Void> result = new Future();
        thingAgent.killComponent().addResultListener(r -> result.setResult(null), result::setException);

        return result;
    }
}
