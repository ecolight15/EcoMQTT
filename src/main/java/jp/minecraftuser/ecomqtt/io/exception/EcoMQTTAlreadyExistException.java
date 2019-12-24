
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * EcoMQTTプラグイン内のMQTTマネージャクラスが起動していない
 * @author ecolight
 */
public class EcoMQTTAlreadyExistException extends EcoMQTTException {
    public EcoMQTTAlreadyExistException() {}
    public EcoMQTTAlreadyExistException(String message) { super(message); }
    public EcoMQTTAlreadyExistException(Throwable cause) { super(cause); }
    public EcoMQTTAlreadyExistException(String message, Throwable cause) { super(message, cause); }
}
