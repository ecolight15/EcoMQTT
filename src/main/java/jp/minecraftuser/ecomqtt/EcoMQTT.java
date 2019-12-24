
package jp.minecraftuser.ecomqtt;

import java.util.logging.Level;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTCommand;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTPublishCommand;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTReloadCommand;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTSubscribeCommand;
import jp.minecraftuser.ecomqtt.commands.EcoMQTTUnsubscribeCommand;
import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqtt.worker.MQTTPublishLogger;
import jp.minecraftuser.ecomqtt.worker.MQTTSubscribeLogger;

/**
 * EcoMQTTプラグインクラス
 * コマンドでのMQTT送受信、ユーザー
 * 及び他プラグインへのスレッドセーフな非同期MQTT送受信機能を提供する
 * @author ecolight
 */
public class EcoMQTT  extends PluginFrame{

    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
        disable();
    }

    /**
     * 設定初期化
     */
    @Override
    public void initializeConfig() {
        EcoMQTTConfig conf = new EcoMQTTConfig(this);

        // Mqtt manager interval timer
        conf.registerInt("mqtt-receive-interval");
        
        // Mqtt settings
        conf.registerString("Mqtt.Server.ConnectionType");
        conf.registerString("Mqtt.Server.URL");
        conf.registerString("Mqtt.Server.UserName");
        conf.registerString("Mqtt.Server.Password");
        conf.registerString("Mqtt.SSL.TrustStore");
        conf.registerString("Mqtt.SSL.TrustStorePassword");
        conf.registerString("Mqtt.SSL.TrustStoreType");
        conf.registerString("Mqtt.SSL.KeyStore");
        conf.registerString("Mqtt.SSL.KeyStorePassword");
        conf.registerString("Mqtt.SSL.KeyStoreType");
        conf.registerInt("Mqtt.Publish.QoS");
        conf.registerBoolean("Mqtt.Publish.retain");
        conf.registerInt("Mqtt.Subscribe.QoS");
        
        // Topic settings
        conf.registerString("Topic.ServerName");
        conf.registerString("Topic.Format.System.Info");
        conf.registerString("Topic.Format.System.Cmd");
        conf.registerString("Topic.Format.Plugin");

        // Log settings
        conf.registerBoolean("Log.Publish.Console");
        conf.registerBoolean("Log.Subscribe.Console");
        conf.registerBoolean("Log.Publish.File.Enable");
        conf.registerBoolean("Log.Subscribe.File.Enable");
        conf.registerString("Log.Publish.File.Path");
        conf.registerString("Log.Subscribe.File.Path");

        registerPluginConfig(conf);
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcoMQTTCommand(this, "ecm");
        cmd.addCommand(new EcoMQTTReloadCommand(this, "reload"));
        registerPluginCommand(cmd);
        
        // MQTT
        registerPluginCommand(new EcoMQTTPublishCommand(this, "pub"));
        registerPluginCommand(new EcoMQTTSubscribeCommand(this, "sub"));
        registerPluginCommand(new EcoMQTTUnsubscribeCommand(this, "unsub"));
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        //registerPluginListener(new CreatureListener(this, "creature"));
    }

    /**
     * ロガークラス初期化
     */
    @Override
    protected void initializeLogger() {
        ConfigFrame conf = getDefaultConfig();
        log.log(Level.INFO, "Publish logger {0}", conf.getBoolean("Log.Publish.File.Enable") ? "Enabled" : "Disabled");
        if (conf.getBoolean("Log.Publish.File.Enable")) {
            registerPluginLogger(new MQTTPublishLogger(this, conf.getString("Log.Publish.File.Path"), "publish"));
        }
        log.log(Level.INFO, "Subscribe logger {0}", conf.getBoolean("Log.Subscribe.File.Enable") ? "Enabled" : "Disabled");
        if (conf.getBoolean("Log.Subscribe.File.Enable")) {
            registerPluginLogger(new MQTTSubscribeLogger(this, conf.getString("Log.Subscribe.File.Path"), "subscribe"));
        }
    }

    /**
     * タイマー初期化処理
     */
    @Override
    protected void initializeTimer() {
        // 設定ファイル編集がされていれば起動する
        ConfigFrame conf = getDefaultConfig();
        if (!conf.getString("Mqtt.Server.URL").equals("serverip:port")) {
            MQTTManager m = new MQTTManager(this, "mqtt");
            registerPluginTimer(m);
            // 設定変更時に通知対象とする
            getDefaultConfig().registerNotifiable(m);
            // タイマーを起動する
            m.runTaskTimer(this, 0, conf.getInt("mqtt-receive-interval"));
        } else {
            log.warning("*** Please require MQTT settings ***");
        }
    }
    
}
