
package jp.minecraftuser.ecomqtt.worker;

import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;

/**
 * MQTTConnection向けインタフェース
 * @author ecolight
 */
public interface MQTTConnectionFrame {
     /**
     * 接続処理
     * @param conf 設定情報
     * @param force 強制再起動指定(蓄積データごとクライアントを破棄する)
     */
    public void connect(EcoMQTTConfig conf, boolean force);
    
    /**
     * メッセージ送信処理
     * @param topic トピック名
     * @param payload 送信電文
     */
    public void publish(String topic, byte[] payload);

    /**
     * メッセージ送信処理
     * @param topic トピック名
     * @param payload 送信電文
     * @param qos QoS指定
     */
    public void publish(String topic, byte[] payload, Integer qos);
    
    /**
     * 購読登録処理
     * @param topic トピック名
     */
    public void subscribe(String topic);
    
    /**
     * 購読登録処理
     * @param topic トピック名
     * @param qos QoS指定
     */
    public void subscribe(String topic, Integer qos);
    
    /**
     * 切断処理 ※disconnectは最大30秒程度待機する場合がある
     */
    public void disconnect();
    
    /**
     * 接続状態を示す
     * @return 接続状態
     */
    public boolean isConnected();
    
}
