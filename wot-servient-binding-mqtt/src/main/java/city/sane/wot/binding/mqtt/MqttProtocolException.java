package city.sane.wot.binding.mqtt;

/**
 * A MqttProtocolException is thrown by mqtt binding when errors occur.
 */
class MqttProtocolException extends Exception {
    public MqttProtocolException(String message) {
        super(message);
    }

    public MqttProtocolException(Throwable cause) {
        super(cause);
    }
}
