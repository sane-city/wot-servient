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
package city.sane.wot.thing.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ThingFilterTest {
    @Test
    public void testEquals() {
        ThingFilter filterA = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterB = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterC = new ThingFilter().setMethod(DiscoveryMethod.LOCAL);

        assertEquals(filterA, filterB);
        assertEquals(filterB, filterA);
        assertNotEquals(filterA, filterC);
        assertNotEquals(filterC, filterA);
    }
}