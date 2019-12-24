
package jp.minecraftuser.ecomqtt.io.exception;

/**
 * EcoMQTTプラグインが読み込まれていない
 * @author ecolight
 */
public class EcoMQTTPluginNotFoundException extends EcoMQTTException {
    public EcoMQTTPluginNotFoundException() {}
    public EcoMQTTPluginNotFoundException(String message) { super(message); }
    public EcoMQTTPluginNotFoundException(Throwable cause) { super(cause); }
    public EcoMQTTPluginNotFoundException(String message, Throwable cause) { super(message, cause); }
}
