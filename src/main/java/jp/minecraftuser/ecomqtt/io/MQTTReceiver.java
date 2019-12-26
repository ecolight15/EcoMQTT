
package jp.minecraftuser.ecomqtt.io;

/**
 * EcoMQTTプラグインから特定のtopicの受信を行う受信インタフェース
 * @author ecolight
 */
public abstract interface MQTTReceiver {
    /**
     * MQTT topic受信ハンドラ
     * @param topic 受信トピック
     * @param payload 受信電文
     */
    public abstract void handler(String topic, byte[] payload);
}
