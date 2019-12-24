
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * EcoMQTTプラグイン関連のExceptionクラス
 * @author ecolight
 */
public class EcoMQTTException extends Exception {
    public EcoMQTTException() {}
    public EcoMQTTException(String message) { super(message); }
    public EcoMQTTException(Throwable cause) { super(cause); }
    public EcoMQTTException(String message, Throwable cause) { super(message, cause); }
}
