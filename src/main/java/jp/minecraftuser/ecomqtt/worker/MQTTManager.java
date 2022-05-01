
package jp.minecraftuser.ecomqtt.worker;

import com.amazonaws.services.iot.client.AWSIotDeviceErrorCode;
import com.amazonaws.services.iot.client.AWSIotMessage;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTTマネージャークラス
 * @author ecolight
 */
public class MQTTManager extends AsyncProcessFrame {
    static private EcoMQTTConfig efconf;
    final private MultiValuedMap<String, MQTTReceiver> receiverMap = new ArrayListValuedHashMap();
    final private MQTTConnectionFrame con;
    
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
        if (conf.getString("MQTTType").equalsIgnoreCase("aws")) {
            con = new MQTTConnectionAWS(this, efconf);
        } else {
            con = new MQTTConnection(this, efconf);
        }
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
                con.publish(payload.topic, payload.payload, payload.qos);
                //log.info("publish MQTT complete.");
            }
            // MQTT 非同期受信登録
            if (payload.operation == MQTTPayload.Operation.SUBSCRIBE) {
                con.subscribe(payload.topic, payload.qos);
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
     * Topicのフォーマット変換
     * @param src
     * @param plg
     * @param singleWildcard
     * @return 
     */
    static public String cnv(String src, String plg, String singleWildcard) {
        src = src.replace(singleWildcard, "+");
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
     * 他プラグインからの依頼で送信するMQTTメッセージ(プラグインprefix付き)
     * メインスレッドで動作
     * @param plg
     * @param topic_
     * @param payload
     */
    public void sendPluginMQTT(String plg, String topic_, byte[] payload) {
        sendPluginMQTT(plg, topic_, payload, null);
    }

    /**
     * 他プラグインからの依頼で送信するMQTTメッセージ(プラグインprefix付き)
     * メインスレッドで動作
     * @param plg
     * @param topic_ トピック名
     * @param payload 送信電文
     * @param qos QoS指定
     */
    public void sendPluginMQTT(String plg, String topic_, byte[] payload, Integer qos) {
        StringBuilder sb = new StringBuilder(cnv(conf.getString("Topic.Format.Plugin"), plg));
        if (topic_ != null) {
            sb.append("/");
            sb.append(topic_);
        }
        String topic = sb.toString();
        sendRawMQTT(topic, payload, qos);
    }

    /**
     * 指定トピック名で指定データを送る
     * メインスレッドで動作
     * @param topic トピック名
     * @param payload 送信電文
     */
    public void sendRawMQTT(String topic, byte[] payload) {
        sendRawMQTT(topic, payload, null);
    }
    
    /**
     * 指定トピック名で指定データを送る
     * メインスレッドで動作
     * @param topic トピック名
     * @param payload 送信電文
     * @param qos Qos指定
     */
    public void sendRawMQTT(String topic, byte[] payload, Integer qos) {
        MQTTPayload data = new MQTTPayload(plg, topic, payload, qos);
        if ((conf.getBoolean("Log.Publish.File.Enable")) ||
            (conf.getBoolean("Log.Publish.Console"))) {
            StringBuilder s = new StringBuilder();
            s.append("type=Publish");
            s.append(", topic=");
            s.append(data.topic);
            s.append(", payload=");
            s.append(new String(data.payload));
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
     * @param topic トピック名
     */
    public void subscribe(String topic) {
        subscribe(topic, null);
    }
    
    /**
     * サブスクライブ登録
     * メインスレッドで動作
     * @param topic トピック名
     * @param qos QoS指定
     */
    public void subscribe(String topic, Integer qos) {
        MQTTPayload data = new MQTTPayload(plg, topic, qos);
        sendData(data); // To子スレッド executeProcess
    }
    
    /**
     * MQTT メッセージ受信処理(MQTTコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param topic トピック名
     * @param mm 受信電文
     */
    public void receiveMQTT(String topic, MqttMessage mm) {
        MQTTPayload data = new MQTTPayload(plg, topic, mm);
        receiveData(data);
    }

    /**
     * MQTT メッセージ受信処理(MQTTコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param message 受信電文
     */
    public void receiveMQTT(AWSIotMessage message) {
        MQTTPayload data = new MQTTPayload(plg, message);
        receiveData(data);
    }

    /**
     * MQTT メッセージ受信処理(MQTTコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param thrwbl 切断事由
     */
    public void receiveMQTT(Throwable thrwbl) {
        MQTTPayload data = new MQTTPayload(plg, thrwbl);
        receiveData(data);
    }

    /**
     * MQTT メッセージ送信結果処理(AWSコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param ret 送信結果
     */
    public void receiveMQTT(MQTTAWSIotMessage ret) {
        MQTTPayload data = new MQTTPayload(plg, ret);
        receiveData(data);
    }

    /**
     * MQTT メッセージ受信処理(MQTTコールバックからの呼び出し)
     * MQTTクライアントで動作
     * @param imdt 送信結果情報
     */
    public void receiveMQTT(IMqttDeliveryToken imdt) {
        MQTTPayload data = new MQTTPayload(plg, imdt);
        receiveData(data);
    }

    /**
     * レシーバを登録する(プラグインprefix付き)
     * topic指定があればプラグインprefixの後に /topic の形式で付与して登録する
     * メインスレッドで動作
     * @param p プラグインインスタンス
     * @param topic_ トピック名
     * @param receiver レシーブハンドラ
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(Plugin p, String topic_, MQTTReceiver receiver) throws EcoMQTTAlreadyExistException {
        registerReceiver(p, topic_, receiver, null);
    }
    
    /**
     * レシーバを登録する(プラグインprefix付き)
     * topic指定があればプラグインprefixの後に /topic の形式で付与して登録する
     * メインスレッドで動作
     * @param p プラグインインスタンス
     * @param topic_ トピック名
     * @param receiver レシーブハンドラ
     * @param qos QoS指定
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(Plugin p, String topic_, MQTTReceiver receiver, Integer qos) throws EcoMQTTAlreadyExistException {
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
                // ToDo: QoS指定は最初にそのトピック名で登録するプラグインのQoSを採用している
                //       プラグインごとに同名トピック受信設定でQoS変更する場合はクライアントから分ける必要あるか？
                subscribe(topic, qos);
            }
            receiverMap.put(topic, receiver);
        }
    }
    
    /**
     * レシーバを登録する(プラグインprefixなし)
     * メインスレッドで動作
     * @param topic トピック名
     * @param receiver レシーブハンドラ
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver) throws EcoMQTTAlreadyExistException {
        registerReceiver(topic, receiver, null);
    }
    
    /**
     * レシーバを登録する(プラグインprefixなし)
     * メインスレッドで動作
     * @param topic トピック名
     * @param receiver レシーブハンドラ
     * @param qos QoS指定
     * @throws EcoMQTTAlreadyExistException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver, Integer qos) throws EcoMQTTAlreadyExistException {
        if (topic == null) {
            topic = "#";
        }
        synchronized(receiverMap) {
            if (receiverMap.containsMapping(topic, receiver)) {
                throw new EcoMQTTAlreadyExistException();
            }
            // 既に登録されている同名のトピックがなければ登録する
            if (!receiverMap.containsKey(topic)) {
                // ToDo: QoS指定は最初にそのトピック名で登録するプラグインのQoSを採用している
                //       プラグインごとに同名トピック受信設定でQoS変更する場合はクライアントから分ける必要あるか？
                subscribe(topic, qos);
            }
            receiverMap.put(topic, receiver);
        }
    }
    
    /**
     * レシーバを解除する(プラグインprefix付き)
     * メインスレッドで動作
     * @param p プラグインスタンス
     * @param topic_ トピック名
     * @param receiver レシーブハンドラ
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
     * @param topic トピック名
     * @param receiver レシーブハンドラ
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
            // メッセージ受診時処理(Other type MQTT server)
            if (data.mm != null) {
                s.append("type=Payload");
                s.append(", topic=");
                s.append(data.topic);
                s.append(", id=");
                s.append(data.mm.getId());
                s.append(", QoS=");
                s.append(data.mm.getQos());
                s.append(", duplicate=");
                s.append(data.mm.isDuplicate());
                s.append(", retained=");
                s.append(data.mm.isRetained());
                s.append(", payload=");
                s.append(new String(data.mm.getPayload()));
                // ログ出力処理
                if (conf.getBoolean("Log.Subscribe.File.Enable")) {
                    plg.getPluginLogger("subscribe").log(s);
                }
                if (conf.getBoolean("Log.Subscribe.Console")) {
                    log.info(s.toString());
                }
                // 他プラグイン向けの処理
                synchronized(receiverMap) {
                    for (Entry<String, MQTTReceiver> e : receiverMap.entries()) {
                        StringBuilder sb = new StringBuilder("\\Q");
                        // MQTTのtopic指定の +,# を正規表現のマッチングに展開
                        sb.append(e.getKey().replace("/+/", "/\\E[^/]+\\Q/").replace("#", "\\E.+$\\Q"));
                        sb.append("\\E");
                        //log.log(Level.SEVERE, "check handler topic[" + data.topic + "] match[" + s.toString() + "]");
                        if (data.topic.matches(sb.toString())) {
                            e.getValue().handler(data.topic, data.mm.getPayload());
                        }
                    }
                }
            }
            // メッセージ受診時処理(AWS MQTT server)
            if (data.message != null) {
                s.append("type=AWSIotTopicPayload");
                s.append(", topic=");
                s.append(data.message.getTopic());
                AWSIotDeviceErrorCode err = data.message.getErrorCode();
                if (err != null) {
                    s.append(", error=");
                    s.append(err.toString());
                    s.append(", errMessage=");
                    s.append(data.message.getErrorMessage());
                }
                s.append(", QoS=");
                s.append(data.message.getQos());
                s.append(", payload=");
                s.append(new String(data.message.getPayload()));
                // ログ出力処理
                if (conf.getBoolean("Log.Subscribe.File.Enable")) {
                    plg.getPluginLogger("subscribe").log(s);
                }
                if (conf.getBoolean("Log.Subscribe.Console")) {
                    log.info(s.toString());
                }
                // 他プラグイン向けの処理
                synchronized(receiverMap) {
                    for (Entry<String, MQTTReceiver> e : receiverMap.entries()) {
                        StringBuilder sb = new StringBuilder("\\Q");
                        // MQTTのtopic指定の +,# を正規表現のマッチングに展開
                        sb.append(e.getKey().replace("+", "\\E[a-zA-Z0-9]+\\Q").replace("#", "\\E.+$\\Q"));
                        sb.append("\\E");
                        log.log(Level.INFO, "check handler topic[" + data.message.getTopic() + "] match[" + sb.toString() + "]");
                        if (data.message.getTopic().matches(sb.toString())) {
                            e.getValue().handler(data.message.getTopic(), data.message.getPayload());
                        }
                    }
                }
            }
            // 切断時処理
            if (data.thrwbl != null) {
                s.append("type=connectionLost");
                s.append(", message=");
                s.append(data.thrwbl.getLocalizedMessage());
                log.log(Level.WARNING, "MQTT connection lost:{0}", data.thrwbl.toString());
                // 送受信ログ両方にログする
                if (conf.getBoolean("Log.Subscribe.File.Enable")) {
                    plg.getPluginLogger("subscribe").log(s);
                }
                if (conf.getBoolean("Log.Publish.File.Enable")) {
                    plg.getPluginLogger("publish").log(s);
                }
                if ((conf.getBoolean("Log.Subscribe.Console")) ||
                    (conf.getBoolean("Log.Publish.Console"))) {
                    log.info(s.toString());
                }
            }
            // 送信完了時処理(Other type MQTT server)
            if (data.imdt != null) {
                s.append("type=DeliveryToken");
                s.append(", topic=");
                for (String t : data.imdt.getTopics()) {
                    s.append("[");
                    s.append(t);
                    s.append("]");
                }
                s.append(", id=");
                s.append(data.imdt.getMessageId());
                s.append(", SessionPresent=");
                s.append(data.imdt.getSessionPresent());
                s.append(", isComplete=");
                s.append(data.imdt.isComplete());
                try {
                    String buf = data.imdt.getMessage().toString();
                    s.append(", Message=");
                    s.append(buf);
                } catch (MqttException ex) {
                    Logger.getLogger(MQTTManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                // ログ出力処理
                if (conf.getBoolean("Log.Publish.File.Enable")) {
                    plg.getPluginLogger("publish").log(s);
                }
                if (conf.getBoolean("Log.Publish.Console")) {
                    log.info(s.toString());
                }
            }
            // 送信結果処理(AWS MQTT server)
            if (data.ret != null) {
                s.append("type=AWSIoTMessage");
                s.append(", ret=");
                s.append(data.ret.ret.toString());
                s.append(", topic=");
                s.append(data.ret.getTopic());
                AWSIotDeviceErrorCode err = data.ret.getErrorCode();
                if (err != null) {
                    s.append(", errCode=");
                    s.append(err.toString());
                    s.append(", errMessage=");
                    s.append(data.ret.getErrorMessage());
                }
                s.append(", Message=");
                s.append(new String(data.ret.getPayload()));
                // ログ出力処理
                if (conf.getBoolean("Log.Publish.File.Enable")) {
                    plg.getPluginLogger("publish").log(s);
                }
                if (conf.getBoolean("Log.Publish.Console")) {
                    log.info(s.toString());
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
