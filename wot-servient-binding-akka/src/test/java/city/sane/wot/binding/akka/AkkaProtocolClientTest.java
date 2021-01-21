package city.sane.wot.binding.akka;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.akka.actor.ThingActor.GetThingDescription;
import city.sane.wot.binding.akka.actor.ThingActor.InvokeAction;
import city.sane.wot.binding.akka.actor.ThingActor.ReadAllProperties;
import city.sane.wot.binding.akka.actor.ThingActor.ReadProperty;
import city.sane.wot.binding.akka.actor.ThingActor.WriteProperty;
import city.sane.wot.binding.akka.actor.ThingsActor.GetThings;
import city.sane.wot.content.Content;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AkkaProtocolClientTest {
    private ActorSystem system;
    private Form form;
    private Duration askTimeout;
    private Duration discoverTimeout;
    private AkkaProtocolPattern pattern;
    private Message message;
    private ActorSelection actorSelection;
    private Content content;
    private ThingFilter filter;

    @BeforeEach
    public void setUp() {
        system = mock(ActorSystem.class, RETURNS_DEEP_STUBS);
        form = mock(Form.class);
        askTimeout = Duration.ofSeconds(60);
        discoverTimeout = Duration.ofSeconds(5);
        pattern = mock(AkkaProtocolPattern.class);
        message = mock(Message.class);
        actorSelection = mock(ActorSelection.class);
        content = mock(Content.class);
        filter = mock(ThingFilter.class);
    }

    @Test
    public void readResourceShouldUseCorrectMessageToReadProperty() {
        when(form.getHref()).thenReturn("akka://foo/bar#properties/count");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.readResource(form);

        verify(pattern).ask(any(ActorSelection.class), any(ReadProperty.class), any());
    }

    @Test
    public void writeResourceShouldUseCorrectMessageToWriteProperty() {
        when(form.getHref()).thenReturn("akka://foo/bar#properties/count");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.writeResource(form, content);

        verify(pattern).ask(any(ActorSelection.class), any(WriteProperty.class), any());
    }

    @Test
    public void observeResourceShouldCreateCorrectActorForProperty() {
        when(form.getHref()).thenReturn("akka://foo/bar#properties/count");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.observeResource(form).subscribe();

        verify(system).actorOf(any());
    }

    @Test
    public void readResourceShouldUseCorrectMessageToReadAllProperties() {
        when(form.getHref()).thenReturn("akka://foo/bar#all/properties");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.readResource(form);

        verify(pattern).ask(any(ActorSelection.class), any(ReadAllProperties.class), any());
    }

    @Test
    public void readResourceShouldUseCorrectMessageToGetThingDescription() {
        when(form.getHref()).thenReturn("akka://foo/bar#thing");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.readResource(form);

        verify(pattern).ask(any(ActorSelection.class), any(GetThingDescription.class), any());
    }

    @Test
    public void readResourceShouldUseCorrectMessageToGetThings() {
        when(form.getHref()).thenReturn("akka://foo/bar#thing-directory");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.readResource(form);

        verify(pattern).ask(any(ActorSelection.class), any(GetThings.class), any());
    }

    @Test
    public void invokeResourceShouldUseCorrectMessageToInvokeAction() {
        when(form.getHref()).thenReturn("akka://foo/bar#actions/reset");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.invokeResource(form, content);

        verify(pattern).ask(any(ActorSelection.class), any(InvokeAction.class), any());
    }

    @Test
    public void observeResourceShouldCreateCorrectActorForEvent() {
        when(form.getHref()).thenReturn("akka://foo/bar#events/change");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.observeResource(form).subscribe();

        verify(system).actorOf(any());
    }

    @Test
    public void subscribeResourceShouldStopActorWhenObserverIsDone() {
        when(form.getHref()).thenReturn("akka://foo/bar#events/change");

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        client.observeResource(form).subscribe().dispose();

        verify(system).stop(any());
    }

    @Test
    public void discoverShouldThrowException() throws ProtocolClientNotImplementedException {
        when(form.getHref()).thenReturn("akka://foo/bar#properties/count");
        when(system.actorSelection(anyString())).thenReturn(actorSelection);
        when(pattern.ask(any(ActorSelection.class), any(), any())).thenReturn(completedFuture(message));

        AkkaProtocolClient client = new AkkaProtocolClient(system, askTimeout, discoverTimeout, pattern);
        assertThrows(ProtocolClientNotImplementedException.class, () -> client.discover(filter));
    }
}