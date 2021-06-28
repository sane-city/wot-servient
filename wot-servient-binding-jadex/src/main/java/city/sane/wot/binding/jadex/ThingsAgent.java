/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.ServiceScope;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This Agent is started together with {@link city.sane.wot.binding.jadex.JadexProtocolServer} and
 * is responsible for exposing things. For each exposed Thing a {@link ThingAgent} is created, which
 * is responsible for the interaction with the Thing.
 */
@Agent
@ProvidedServices({
        @ProvidedService(type = ThingsService.class, scope = ServiceScope.PLATFORM)
})
public class ThingsAgent implements ThingsService {
    private static final Logger log = LoggerFactory.getLogger(ThingsAgent.class);
    private final Map<String, IExternalAccess> children;
    @Agent
    private IInternalAccess agent;
    @AgentArgument("things")
    private Map<String, ExposedThing> things;

    public ThingsAgent() {
        children = new HashMap<>();
    }

    ThingsAgent(IInternalAccess agent,
                Map<String, ExposedThing> things,
                Map<String, IExternalAccess> children) {
        this.agent = agent;
        this.things = things;
        this.children = children;
    }

    @AgentCreated
    public void created() {
        log.debug("Agent created");
    }

    @Override
    public IFuture<IExternalAccess> expose(String id) {
        CreationInfo info = new CreationInfo()
                .setFilenameClass(ThingAgent.class)
                .addArgument("thing", things.get(id));
        IFuture<IExternalAccess> component = agent.createComponent(info);
        component.addResultListener(new IResultListener<>() {
            @Override
            public void resultAvailable(IExternalAccess thingAgent) {
                children.put(id, thingAgent);
            }

            @Override
            public void exceptionOccurred(Exception e) {
                // do nothing
            }
        });

        return component;
    }

    @Override
    public IFuture<Void> destroy(String id) {
        IExternalAccess thingAgent = children.get(id);

        Future<Void> future = new Future();
        thingAgent.killComponent().addResultListener(new IResultListener<Map<String, Object>>() {
            @Override
            public void resultAvailable(Map<String, Object> result) {
                future.setResult(null);
            }

            @Override
            public void exceptionOccurred(Exception exception) {
                future.setException(exception);
            }
        });

        return future;
    }
}
