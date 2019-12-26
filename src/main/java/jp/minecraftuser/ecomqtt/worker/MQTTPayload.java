
package jp.minecraftuser.ecomqtt.worker;

import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class MQTTPayload extends PayloadFrame {
    public enum Operation { PUBLISH, SUBSCRIBE, RELOAD, FORCERELOAD }
    public Operation operation;
    public Integer qos;
    public String topic;
    public byte[] payload;
    public MqttMessage mm;
    public Throwable thrwbl;
    public IMqttDeliveryToken imdt;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param topic_ 送受信するトピック名
     * @param payload_ 送受信データ
     * @param qos_ QoS指定
     */
    public MQTTPayload(PluginFrame plg_, String topic_, byte[] payload_, Integer qos_) {
        super(plg_);
        operation = Operation.PUBLISH;
        topic = topic_;
        payload = payload_;
        qos = qos_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param topic_ 送受信するトピック名
     * @param payload_ 送受信データ
     */
    public MQTTPayload(PluginFrame plg_, String topic_, byte[] payload_) {
        super(plg_);
        operation = Operation.PUBLISH;
        topic = topic_;
        payload = payload_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param topic_ 送受信するトピック名
     * @param qos_ QoS指定
     */
    public MQTTPayload(PluginFrame plg_, String topic_, Integer qos_) {
        super(plg_);
        operation = Operation.SUBSCRIBE;
        topic = topic_;
        qos = qos_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param topic_ 送受信するトピック名
     */
    public MQTTPayload(PluginFrame plg_, String topic_) {
        super(plg_);
        operation = Operation.SUBSCRIBE;
        topic = topic_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param thrwbl_ 切断事由
     */
    public MQTTPayload(PluginFrame plg_, Throwable thrwbl_) {
        super(plg_);
        thrwbl = thrwbl_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param imdt_ 送信結果情報
     */
    public MQTTPayload(PluginFrame plg_, IMqttDeliveryToken imdt_) {
        super(plg_);
        imdt = imdt_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param topic_ トピック名
     * @param mm_ MqttMessage
     */
    public MQTTPayload(PluginFrame plg_, String topic_, MqttMessage mm_) {
        super(plg_);
        operation = Operation.RELOAD;
        topic = topic_;
        mm = mm_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public MQTTPayload(PluginFrame plg_) {
        super(plg_);
        operation = Operation.RELOAD;
    }
    
    /**
     * コンストラクタ(再起動指定用 メイン => MQTTスレッド用)
     * @param plg_ プラグインインスタンス
     * @param force 強制再起動指定(蓄積データごとクライアントを破棄する)
     */
    public MQTTPayload(PluginFrame plg_, boolean force) {
        super(plg_);
        if (force) {
            operation = Operation.FORCERELOAD;
        } else {
            operation = Operation.RELOAD;
        }
    }
}
