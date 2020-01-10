package city.sane.wot.binding.handler.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonEncoder<T> extends MessageToMessageEncoder<T> {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(JsonDecoder.class);

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
