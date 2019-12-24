
package jp.minecraftuser.ecomqtt.worker;

import java.util.Map.Entry;
import java.util.logging.Level;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;
import jp.minecraftuser.ecomqtt.io.MQTTReceiver;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTAlreadyExistException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTReceiverNotFoundException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * MQTTマネージャークラス
 * @author ecolight
 */
public class MQTTManager extends AsyncProcessFrame {
    static private EcoMQTTConfig efconf;
    final private MultiValuedMap<String, MQTTReceiver> receiverMap = new ArrayListValuedHashMap();
    final private MQTTConnection con;
    
    /**
     * 親スレッド用コンストラクタ
     * メインスレッドにて生成
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public MQTTManager(PluginFrame plg_, String name_) {
        super(plg_, name_);
        efconf = (EcoMQTTConfig) conf;
        con = null;
    }

    /**
     * 子スレッド用コンストラクタ
     * メインスレッドにて生成 (※親スレッドの初期化と同時に初期化)
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     * @param frame_ 子スレッド用フレーム
     */
    public MQTTManager(PluginFrame plg_, String name_, AsyncFrame frame_) {
        super(plg_, name_, frame_);
        efconf = (EcoMQTTConfig) plg.getDefaultConfig();
        // MQTT 接続処理
        con = new MQTTConnection(this, efconf);
    }

    /**
     * MQTTスレッド用実行依頼エントリ処理
     * MQTTスレッドで動作
     * @param payload_ ペイロードインスタンス
     */
    @Override
    protected void executeProcess(PayloadFrame payload_) {
        MQTTPayload payload = (MQTTPayload) payload_;
        
        if (payload.operation == MQTTPayload.Operation.RELOAD) {
            log.info("Start reconnect MQTT");
            con.connect(efconf, false);
            log.info("MQTT reconnected");
            return;
        }
        if (payload.operation == MQTTPayload.Operation.FORCERELOAD) {
            log.info("Start force reconnect MQTT");
            con.connect(efconf, true);
            log.info("MQTT reconnected");
            return;
        }
        
        // 接続失敗あるいは切断状態の場合には再接続処理する
        if (!con.isConnected()) {
            log.info("Start send reconnect MQTT");
            con.connect(efconf, false);
            log.info("MQTT send reconnected");
        }
        if (con.isConnected()) {
            // MQTT 非同期送信
            if (payload.operation == MQTTPayload.Operation.PUBLISH) {
                //log.info("publish MQTT topic[" + payload.topic + "] data[" + new String(payload.data) +"]");
                con.publish(payload.topic, payload.data);
                //log.info("publish MQTT complete.");
            }
            // MQTT 非同期受信登録
            if (payload.operation == MQTTPayload.Operation.SUBSCRIBE) {
                con.subscribe(payload.topic);
            }
        }
        // 必要があればメインスレッド側に結果を返却することも可
        //receiveData(data);
    }

    /**
     * 設定再読み込み時処理
     * メインスレッドで動作
     */
    @Override
    public void reloadNotify() {
        reconnect();
    }
    
    /**
     * クライアント再生成しての再接続
     * メインスレッドで動作
     */
    public void reconnect() {
        MQTTPayload data = new MQTTPayload(plg, false);
        sendData(data); // To子スレッド executeProcess
    }

    /**
     * クライアント再生成しての再接続
     * メインスレッドで動作
     */
    public void forceReconnect() {
        MQTTPayload data = new MQTTPayload(plg, true);
        sendData(data); // To子スレッド executeProcess
    }

    /**
     * Topicのフォーマット変換
     * @param src
     * @param plg
     * @return 
     */
    static public String cnv(String src, String plg) {
        src = src.replace("{server}", efconf.getString("Topic.ServerName"));
        if (plg != null) {
            src = src.replace("{plugin}", plg);
        }
        return src;
    }
    
    /**
     * このプラグインが主体で送信するMQTTメッセージ
     * メインスレッドで動作
     * @param payload
     */
    public void sendSysInfoMQTT(byte[] payload) {
        String format = cnv(conf.getString("Topic.Format.System.Info"), null);
        sendRawMQTT(format, payload);
    }
    
    /**
     * このプラグインがコマンド経由で送信するMQTTメッセージ
     * メインスレッドで動作
     * @param p
     * @param payload
     */
    public void sendSysCmdMQTT(CommandSender p, byte[] payload) {
        String format = cnv(conf.getString("Topic.Format.System.Cmd"), null);
        sendRawMQTT(format, payload);
    }
    
    /**
     * 他プラグインからの依頼で送信するMQTTメッセージ
     * メインスレッドで動作
     * @param plg
     * @param payload
     */
    public void sendPluginMQTT(String plg, byte[] payload) {
        String format = cnv(conf.getString("Topic.Format.Plugin"), plg);
        sendRawMQTT(format, payload);
    }
    
    /**
     * 指定トピック名で指定データを送る
     * メインスレッドで動作
     * @param topic
     * @param payload 
     */
    public void sendRawMQTT(String topic, byte[] payload) {
        MQTTPayload data = new MQTTPayload(plg, topic, payload);
        if ((conf.getBoolean("Log.Publish.File.Enable")) ||
            (conf.getBoolean("Log.Publish.Console"))) {
            StringBuilder s = new StringBuilder();
            s.append("topic[");
            s.append(data.topic);
            s.append("] data[");
            s.append(new String(data.data));
            s.append("]");
            if (conf.getBoolean("Log.Publish.Console")) {
                log.info(s.toString());
            }
            if (conf.getBoolean("Log.Publish.File.Enable")) {
                plg.getPluginLogger("publish").log(s);
            }
        }
        sendData(data); // To子スレッド executeProcess
    }
    
    /**
     * サブスクライブ登録
     * メインスレッドで動作
     * @param topic 
     */
    public void subscribe(String topic) {
        MQTTPayload data = new MQTTPayload(plg, topic);
        sendData(data); // To子スレッド executeProcess
    }
    
    /**
     * MQTT メッセージ受信処理(MQTTコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param topic
     * @param data 
     */
    public void receiveMQTT(String topic, byte[] data) {
        MQTTPayload payload = new MQTTPayload(plg, topic, data);
        receiveData(payload);
    }

    /**
     * レシーバを登録する(プラグインprefix付き)
     * topic指定があればプラグインprefixの後に /topic の形式で付与して登録する
     * メインスレッドで動作
     * @param p
     * @param topic_
     * @param receiver 
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(Plugin p, String topic_, MQTTReceiver receiver) throws EcoMQTTAlreadyExistException {
        StringBuilder sb = new StringBuilder(cnv(conf.getString("Topic.Format.Plugin"), p.getName()));
        if (topic_ != null) {
            sb.append("/");
            sb.append(topic_);
        }
        String topic = sb.toString();
        synchronized(receiverMap) {
            if (receiverMap.containsMapping(topic, receiver)) {
                throw new EcoMQTTAlreadyExistException();
            }
            // 既に登録されている同名のトピックがなければ登録する
            if (!receiverMap.containsKey(topic)) {
                subscribe(topic);
            }
            receiverMap.put(topic, receiver);
        }
    }
    
    /**
     * レシーバを登録する(プラグインprefixなし)
     * メインスレッドで動作
     * @param topic
     * @param receiver 
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver) throws EcoMQTTAlreadyExistException {
        if (topic == null) {
            topic = "#";
        }
        synchronized(receiverMap) {
            if (receiverMap.containsMapping(topic, receiver)) {
                throw new EcoMQTTAlreadyExistException();
            }
            // 既に登録されている同名のトピックがなければ登録する
            if (!receiverMap.containsKey(topic)) {
                subscribe(topic);
            }
            receiverMap.put(topic, receiver);
        }
    }
    
    /**
     * レシーバを解除する(プラグインprefix付き)
     * メインスレッドで動作
     * @param p
     * @param topic_
     * @param receiver 
     * @throws EcoMQTTReceiverNotFoundException 
     */
    public void unregisterReceiver(Plugin p, String topic_, MQTTReceiver receiver) throws EcoMQTTReceiverNotFoundException {
        StringBuilder sb = new StringBuilder(cnv(conf.getString("Topic.Format.Plugin"), p.getName()));
        if (topic_ != null) {
            sb.append("/");
            sb.append(topic_);
        }
        String topic = sb.toString();
        synchronized(receiverMap) {
            if (!receiverMap.containsMapping(topic, receiver)) {
                throw new EcoMQTTReceiverNotFoundException();
            }
            receiverMap.removeMapping(topic, receiver);
        }
    }
    
    /**
     * レシーバを解除する(プラグインprefixなし)
     * メインスレッドで動作
     * @param topic
     * @param receiver 
     * @throws EcoMQTTReceiverNotFoundException 
     */
    public void unregisterReceiver(String topic, MQTTReceiver receiver) throws EcoMQTTReceiverNotFoundException {
        if (topic == null) {
            topic = "#";
        }
        synchronized(receiverMap) {
            if (!receiverMap.containsMapping(topic, receiver)) {
                throw new EcoMQTTReceiverNotFoundException();
            }
            receiverMap.removeMapping(topic, receiver);
        }
    }

    /**
     * メインスレッド側受信処理
     * Spigotの同期タイマー呼び出しで実行されるためbukkit/spigot APIをコールする場合
     * ここで呼び出しを行うこと。
     * メインスレッドで動作
     * @param data_ ペイロードインスタンス
     */
    @Override
    protected void executeReceive(PayloadFrame data_) {
        MQTTPayload data = (MQTTPayload) data_;
        if ((conf.getBoolean("Log.Subscribe.File.Enable")) ||
            (conf.getBoolean("Log.Subscribe.Console"))){
            StringBuilder s = new StringBuilder();
            s.append("topic[");
            s.append(data.topic);
            s.append("] data[");
            s.append(new String(data.data));
            s.append("]");
            if (conf.getBoolean("Log.Subscribe.Console")) {
                log.info(s.toString());
            }
            if (conf.getBoolean("Log.Subscribe.File.Enable")) {
                plg.getPluginLogger("subscribe").log(s);
            }
        }
        // 他プラグイン向けの処理
        synchronized(receiverMap) {
            for (Entry<String, MQTTReceiver> e : receiverMap.entries()) {
                StringBuilder s = new StringBuilder("\\Q");
                // MQTTのtopic指定の +,# を正規表現のマッチングに展開
                s.append(e.getKey().replace("/+/", "/\\E[^/]+\\Q/").replace("#", "\\E.+$\\Q"));
                s.append("\\E");
                //log.log(Level.SEVERE, "check handler topic[" + data.topic + "] match[" + s.toString() + "]");
                if (data.topic.matches(s.toString())) {
                    e.getValue().handler(data.topic, data.data);
                }
            }
        }
    }

    /**
     * 継承クラスの子スレッド用インスタンス生成
     * 親子間で共有リソースがある場合、マルチスレッドセーフな作りにすること
     * synchronizedにする、スレッドセーフ対応クラスを使用するなど
     * メインスレッドで動作
     * @return AsyncFrame継承クラスのインスタンス
     */
    @Override
    protected AsyncFrame clone() {
        return new MQTTManager(plg, name, this);
    }

    /**
     * 子プロセス終了処理
     * MQTTスレッドで動作
     */
    @Override
    protected void finalizeProcess() {
        con.disconnect();
    }
}
