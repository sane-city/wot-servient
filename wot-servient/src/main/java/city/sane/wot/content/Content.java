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
package city.sane.wot.content;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents any serialized content. Enables the transfer of arbitrary data structures.
 */
public class Content {
    public static final Content EMPTY_CONTENT = new Content(ContentManager.DEFAULT, new byte[0]);
    private final String type;
    private final byte[] body;

    private Content() {
        type = null;
        body = null;
    }

    public Content(byte[] body) {
        this(ContentManager.DEFAULT, body);
    }

    public Content(String type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), Arrays.hashCode(getBody()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Content)) {
            return false;
        }
        return Objects.equals(getType(), ((Content) obj).getType()) && Arrays.equals(getBody(), ((Content) obj).getBody());
    }

    @Override
    public String toString() {
        return "Content{" +
                "type='" + type + '\'' +
                ", body=" + Arrays.toString(body) +
                ", new String(body)=" + new String(body) +
                '}';
    }

    public String getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }
}
