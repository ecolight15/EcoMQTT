
package jp.minecraftuser.ecomqtt.worker;

import jp.minecraftuser.ecoframework.LoggerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * MQTT Subscribe logger
 * @author ecolight
 */
public class MQTTSubscribeLogger extends LoggerFrame {
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param filename ファイル名
     * @param name_ ロガー定義名
     */
    public MQTTSubscribeLogger(PluginFrame plg_, String filename, String name_) {
        super(plg_, filename, name_);
    }
    
}
