
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * 指定のtopicとSubscribeレシーブハンドラの対は既に登録済み
 * @author ecolight
 */
public class EcoMQTTAlreadyExistException extends EcoMQTTException {
    public EcoMQTTAlreadyExistException() {}
    public EcoMQTTAlreadyExistException(String message) { super(message); }
    public EcoMQTTAlreadyExistException(Throwable cause) { super(cause); }
    public EcoMQTTAlreadyExistException(String message, Throwable cause) { super(message, cause); }
}
