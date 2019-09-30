package city.sane.wot.thing.property;

import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.form.Form;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ConsumedThingPropertyTest {
    @Test
    public void normalizeAbsoluteHref() {
        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(null, thing);
        Form form = new Form.Builder().setHref("http://example.com/properties/count").build();
        ThingProperty property = new ThingProperty.Builder().setForms(Arrays.asList(form)).build();
        ConsumedThingProperty consumedProperty = new ConsumedThingProperty("count", property, consumedThing);

        assertEquals("http://example.com/properties/count", consumedProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeRelativeHref() {
        Thing thing = new Thing.Builder().setBase("http://example.com").build();
        ConsumedThing consumedThing = new ConsumedThing(null, thing);
        Form form = new Form.Builder().setHref("/properties/count").build();
        ThingProperty property = new ThingProperty.Builder().setForms(Arrays.asList(form)).build();
        ConsumedThingProperty consumedProperty = new ConsumedThingProperty("count", property, consumedThing);

        assertEquals("http://example.com/properties/count", consumedProperty.getForms().get(0).getHref());
    }
}