package city.sane.wot.thing.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

/**
 * (De)serializes data in CBOR format.
 */
public class CborCodec extends JsonCodec {
    private final ObjectMapper mapper = new ObjectMapper(new CBORFactory());

    @Override
    public String getMediaType() {
        return "application/cbor";
    }

    protected ObjectMapper getMapper() {
        return mapper;
    }
}
