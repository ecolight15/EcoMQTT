
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * EcoMQTTプラグイン内のMQTTマネージャクラスが起動していない
 * @author ecolight
 */
public class EcoMQTTManagerNotFoundException extends EcoMQTTException {
    public EcoMQTTManagerNotFoundException() {}
    public EcoMQTTManagerNotFoundException(String message) { super(message); }
    public EcoMQTTManagerNotFoundException(Throwable cause) { super(cause); }
    public EcoMQTTManagerNotFoundException(String message, Throwable cause) { super(message, cause); }
}
