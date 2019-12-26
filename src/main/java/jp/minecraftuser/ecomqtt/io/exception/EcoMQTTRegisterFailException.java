
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * Subscribeハンドラの登録または解除に失敗した
 * @author ecolight
 */
public class EcoMQTTRegisterFailException extends Exception {
    public EcoMQTTRegisterFailException() {}
    public EcoMQTTRegisterFailException(String message) { super(message); }
    public EcoMQTTRegisterFailException(Throwable cause) { super(cause); }
    public EcoMQTTRegisterFailException(String message, Throwable cause) { super(message, cause); }
}
