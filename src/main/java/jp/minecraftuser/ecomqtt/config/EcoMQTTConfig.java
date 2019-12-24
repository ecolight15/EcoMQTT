
package jp.minecraftuser.ecomqtt.config;

import java.util.HashMap;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTCommandController;
import jp.minecraftuser.ecomqtt.io.MQTTController;

/**
 * デフォルトコンフィグクラス
 * @author ecolight
 */
public class EcoMQTTConfig extends ConfigFrame{
    // コマンドからの制御用
    public HashMap<String, EcoMQTTCommandController> con;

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public EcoMQTTConfig(PluginFrame plg_) {
        super(plg_);
        con = new HashMap<>();
    }
}
