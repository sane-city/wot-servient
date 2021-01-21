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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AkkaProtocolServerIT {
    private AkkaProtocolServer server;

    @BeforeEach
    public void setUp() {
        server = new AkkaProtocolServer(ConfigFactory.load());
        server.start(null).join();
    }

    @AfterEach
    public void tearDown() {
        server.stop().join();
    }

    @Test
    public void getDirectoryUrlShouldReturnCorredUrl() throws URISyntaxException {
        assertEquals(new URI("akka://wot@127.0.0.1:25520/user/things#thing-directory"), server.getDirectoryUrl());
    }
}