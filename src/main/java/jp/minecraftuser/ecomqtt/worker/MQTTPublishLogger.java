
package jp.minecraftuser.ecomqtt.worker;

import jp.minecraftuser.ecoframework.LoggerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * MQTT publish logger
 * @author ecolight
 */
public class MQTTPublishLogger extends LoggerFrame {
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param filename ファイル名
     * @param name_ ロガー定義名
     */
    public MQTTPublishLogger(PluginFrame plg_, String filename, String name_) {
        super(plg_, filename, name_);
    }
    
}
