
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
    public EcoMQTTCommandController(Plugin plg_, CommandSender sender_) {
        super(plg_);
        sender = sender_;
    }

    @Override
    public void handler(String topic, byte[] payload) {
        sender.sendMessage("topic[" + topic + "] payload[" + new String(payload) + "]");
    }
}
