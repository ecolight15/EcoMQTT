
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * 解除指定したトピックとSubscribeレシーブハンドラの対は登録されていない
 * @author ecolight
 */
public class EcoMQTTReceiverNotFoundException extends EcoMQTTException {
    public EcoMQTTReceiverNotFoundException() {}
    public EcoMQTTReceiverNotFoundException(String message) { super(message); }
    public EcoMQTTReceiverNotFoundException(Throwable cause) { super(cause); }
    public EcoMQTTReceiverNotFoundException(String message, Throwable cause) { super(message, cause); }
}
