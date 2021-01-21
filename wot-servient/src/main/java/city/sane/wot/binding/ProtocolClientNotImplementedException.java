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
package city.sane.wot.binding;

/**
 * This exception is thrown when the a {@link ProtocolClient} implementation does not support a
 * requested functionality.
 */
@SuppressWarnings({ "java:S110" })
public class ProtocolClientNotImplementedException extends ProtocolClientException {
    public ProtocolClientNotImplementedException(Class clazz, String operation) {
        super(clazz.getSimpleName() + " does not implement '" + operation + "'");
    }

    public ProtocolClientNotImplementedException(String message) {
        super(message);
    }
}
