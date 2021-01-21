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
package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public abstract class ThingInteractionWithContent extends ThingInteraction {
    protected Content value;

    public ThingInteractionWithContent(String thingId, String name, Content value) {
        super(thingId, name);
        this.value = Objects.requireNonNull(value);
    }

    public ThingInteractionWithContent() {
        super();
        this.value = null;
    }

    public Content getValue() {
        return value;
    }

    public void setValue(Content value) {
        this.value = value;
    }
}
