
package jp.minecraftuser.ecomqtt.worker;

import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class MQTTPayload extends PayloadFrame {
    public enum Operation { PUBLISH, SUBSCRIBE, RELOAD, FORCERELOAD }
    public Operation operation;
    public String topic;
    public byte[] data;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param topic_ 送受信するトピック名
     * @param data_ 送受信データ
     */
    public MQTTPayload(PluginFrame plg_, String topic_, byte[] data_) {
        super(plg_);
        operation = Operation.PUBLISH;
        topic = topic_;
        data = data_;
    }
    public MQTTPayload(PluginFrame plg_, String topic_) {
        super(plg_);
        operation = Operation.SUBSCRIBE;
        topic = topic_;
    }
    public MQTTPayload(PluginFrame plg_) {
        super(plg_);
        operation = Operation.RELOAD;
    }
    public MQTTPayload(PluginFrame plg_, boolean force) {
        super(plg_);
        if (force) {
            operation = Operation.FORCERELOAD;
        } else {
            operation = Operation.RELOAD;
        }
    }
}
