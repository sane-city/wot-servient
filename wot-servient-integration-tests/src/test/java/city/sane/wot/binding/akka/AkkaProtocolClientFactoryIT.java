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
package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNull;

public class AkkaProtocolClientFactoryIT {
    private AkkaProtocolClientFactory factory = null;

    @AfterEach
    public void tearDown() {
        if (factory != null) {
            factory.destroy().join();
        }
    }

    @Test
    public void initShouldNotFail() throws ExecutionException, InterruptedException {
        factory = new AkkaProtocolClientFactory(ConfigFactory.load());

        assertNull(factory.init().get());
    }

    @Test
    public void destroyShouldNotFail() throws ExecutionException, InterruptedException {
        factory = new AkkaProtocolClientFactory(ConfigFactory.load());
        factory.init().join();

        assertNull(factory.destroy().get());
    }
}