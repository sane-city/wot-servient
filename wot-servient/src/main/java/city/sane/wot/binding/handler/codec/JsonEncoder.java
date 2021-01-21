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
package city.sane.wot.binding.handler.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonEncoder<T> extends MessageToMessageEncoder<T> {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(JsonEncoder.class);

    public JsonEncoder(Class<? extends T> clazz) {
        super(clazz);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, T msg, List<Object> out) throws Exception {
        String json = JSON_MAPPER.writeValueAsString(msg);
        log.debug("Serialized message to: {}", json);
        out.add(json);
    }
}
