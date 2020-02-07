package city.sane.wot.binding.handler.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonDecoder<T> extends MessageToMessageDecoder<String> implements ChannelInboundHandler {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(JsonDecoder.class);
    private final Class<? extends T> clazz;

    public JsonDecoder(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          String json,
                          List<Object> out) throws Exception {
        T message = JSON_MAPPER.readValue(json, clazz);
        log.debug("Deserialized message to: {}", message);
        out.add(message);
    }
}
