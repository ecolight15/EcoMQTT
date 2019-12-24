
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * EcoMQTTプラグイン内のMQTTマネージャクラスが起動していない
 * @author ecolight
 */
public class EcoMQTTReceiverNotFoundException extends EcoMQTTException {
    public EcoMQTTReceiverNotFoundException() {}
    public EcoMQTTReceiverNotFoundException(String message) { super(message); }
    public EcoMQTTReceiverNotFoundException(Throwable cause) { super(cause); }
    public EcoMQTTReceiverNotFoundException(String message, Throwable cause) { super(message, cause); }
}
