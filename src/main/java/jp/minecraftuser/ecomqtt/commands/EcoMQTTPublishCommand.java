
package jp.minecraftuser.ecomqtt.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;
import static jp.minecraftuser.ecomqtt.worker.MQTTManager.cnv;
import org.bukkit.command.CommandSender;

/**
 * publishコマンドクラス
 * @author ecolight
 */
public class EcoMQTTPublishCommand extends CommandFrame {
    EcoMQTTConfig config;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoMQTTPublishCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
        config = (EcoMQTTConfig) conf;
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecomqtt.publish";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            EcoMQTTCommandController rec;
            try {
                // プレイヤーごとにハンドラ兼コントローラを生成して管理する
                // コンソールからも使えるようにとりあえず名前で管理する
                if (config.con.containsKey(sender.getName())) {
                    rec = config.con.get(sender.getName());
                } else {
                    rec = new EcoMQTTCommandController(plg, sender);
                    config.con.put(sender.getName(), rec);
                }
                rec.publish(cnv(conf.getString("Topic.Format.System.Cmd"), plg.toString()), args[0].getBytes());
                Utl.sendPluginMessage(plg, sender, "パブリッシュしました[" + args[0] + "]");
            } catch (Exception ex) {
                Logger.getLogger(EcoMQTTPublishCommand.class.getName()).log(Level.SEVERE, null, ex);
                Utl.sendPluginMessage(plg, sender, "パブリッシュに失敗しました:" + ex.getLocalizedMessage());
            }
        } else {
            Utl.sendPluginMessage(plg, sender, "パラメータが不足しています");
        }
        return true;
    }
    
}
