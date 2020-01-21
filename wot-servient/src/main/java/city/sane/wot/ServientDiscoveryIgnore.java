package city.sane.wot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If no {@link city.sane.wot.binding.ProtocolServer} or {@link city.sane.wot.binding.ProtocolClientFactory} are specified in the config, {@link ServientConfig}
 * searches the classpath for suitable classes. This annotation excludes the respective class from the search.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServientDiscoveryIgnore {

}
