
package jp.minecraftuser.ecomqtt.commands;

import jp.minecraftuser.ecomqtt.io.MQTTController;
import jp.minecraftuser.ecomqtt.io.MQTTReceiver;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * MQTTサブスクライブ受信ハンドラ/パブリッシュ制御クラス
 * @author ecolight
 */
public class EcoMQTTCommandController extends MQTTController implements MQTTReceiver {
    CommandSender sender;
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param sender_ コマンド送信者情報
     */
    public EcoMQTTCommandController(Plugin plg_, CommandSender sender_) {
        super(plg_);
        sender = sender_;
    }

    /**
     * コマンド受信登録ハンドラ
     * @param topic 受信トピック
     * @param payload 受信電文
     */
    @Override
    public void handler(String topic, byte[] payload) {
        sender.sendMessage("topic[" + topic + "] payload[" + new String(payload) + "]");
    }
}
