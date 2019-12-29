
package jp.minecraftuser.ecomqtt.worker;

import com.amazonaws.services.iot.client.AWSIotDeviceErrorCode;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;

/**
 * MQTT 接続クラス
 * @author ecolight
 */
public final class MQTTConnectionAWS implements MQTTConnectionFrame {
    private final MQTTManager manager;
    private static final Logger LOG = Logger.getLogger(MQTTConnectionAWS.class.getName());
    private boolean abort = false;
    private AWSIotMqttClient client;
    private int pubQOS = 0;
    private int subQOS = 0;
    
    /**
     * コンストラクタ
     * @param manager_ マネージャクラスインスタンス
     * @param conf_ 設定情報
     */
    public MQTTConnectionAWS(MQTTManager manager_, EcoMQTTConfig conf_) {
        manager = manager_;
        connect(conf_, true);
    }
    
    /**
     * 接続処理
     * @param conf 設定情報
     * @param force 強制再起動指定(蓄積データごとクライアントを破棄する)
     */
    @Override
    public void connect(EcoMQTTConfig conf, boolean force) {
        // AWS settings
        pubQOS = conf.getInt("AWS.Publish.QoS");
        subQOS = conf.getInt("AWS.Subscribe.QoS");

        // 接続処理
        if (client == null || force) {
            // クライアント作成 MemoryPersistenceの役割が不明瞭
            String url = conf.getString("AWS.Server.URL");
            String clid = UUID.randomUUID().toString(); // Todo:接続ごとに異なる好きな名前が振れる様なのでそのうち仕様検討する
            LOG.log(Level.INFO, "create MQTT client [{0}] cliendId[{1}]", new Object[]{url, clid});
            KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(
                    conf.getString("AWS.Security.CertificateFile"),
                    conf.getString("AWS.Security.PrivateKeyFile"));
            client = new AWSIotMqttClient(url, clid, pair.keyStore, pair.keyPassword);
        }
        if (client == null) {
            LOG.log(Level.SEVERE, "failed MQTT client create");
            return;
        }
        LOG.log(Level.INFO, "start MQTT connection");
        
        // 設定
        LOG.log(Level.INFO, "AWS connection settings. BaseRetryDelay={0}, CleanSession={1}"
                + ", ConnectionTimeout={2}, KeepAliveInterval={3}, MaxConnectionRetries={4}"
                + ", MaxOfflineQueueSize={5}, MaxRetryDelay={6}, NumOfClientThreads={7}"
                + ", Port={8}, ServerAckTimeout={9}, WillMessage={10}", new Object[] {
            client.getBaseRetryDelay(),
            client.isCleanSession(),
            client.getConnectionTimeout(),
            client.getKeepAliveInterval(),
            client.getMaxConnectionRetries(),
            client.getMaxOfflineQueueSize(),
            client.getMaxRetryDelay(),
            client.getNumOfClientThreads(),
            client.getPort(),
            client.getServerAckTimeout(),
            client.getWillMessage(),
        });
//        client.setBaseRetryDelay(0);
//        client.setCleanSession(conf.getBoolean("AWS.Server.CleanSession"));
//        client.setConnectionTimeout(pubQOS);
//        client.setKeepAliveInterval(pubQOS);
//        client.setMaxConnectionRetries(pubQOS);
//        client.setMaxOfflineQueueSize(pubQOS);
//        client.setMaxRetryDelay(pubQOS);
//        client.setNumOfClientThreads(pubQOS);
//        client.setPort(pubQOS);
//        client.setServerAckTimeout(subQOS);
//        client.setWillMessage(new AWSIotMessage(topic, AWSIotQos.QOS0, payload));

        // 接続
        try {
            client.connect();
            LOG.log(Level.INFO, "MQTT server connected.");
        } catch (AWSIotException ex) {
            if (!abort) {
                AWSIotDeviceErrorCode err = ex.getErrorCode();
                if (err != null) {
                    LOG.log(Level.WARNING, "reason: {0}", err.toString());
                }
                LOG.log(Level.WARNING, "message: {0}", ex.getMessage());
                LOG.log(Level.WARNING, "localize: {0}", ex.getLocalizedMessage());
                LOG.log(Level.WARNING, "cause: {0}", ex.getCause());
                LOG.log(Level.WARNING, null, ex);
            }
            abort = true;
        }
    }

    /**
     * メッセージ送信処理
     * @param topic トピック名
     * @param payload 送信電文
     */
    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, null);
    }
    
    /**
     * メッセージ送信処理
     * @param topic トピック名
     * @param payload 送信電文
     * @param qos QoS指定
     */
    @Override
    public void publish(String topic, byte[] payload, Integer qos) {
        try {
            if (qos == null) {
                qos = pubQOS;
            }
            client.publish(new MQTTAWSIotMessage(this, topic, AWSIotQos.valueOf(qos), payload));
            abort = false;
        } catch (AWSIotException ex) {
            if (!abort) {
                LOG.log(Level.WARNING, "message: {}", ex.getMessage());
                LOG.log(Level.WARNING, "localize: {}", ex.getLocalizedMessage());
                LOG.log(Level.WARNING, "cause: {}", ex.getCause());
                LOG.log(Level.WARNING, null, ex);
            }
            abort = true;
        }
    }
    
    /**
     * 購読登録処理
     * @param topic トピック名
     */
    @Override
    public void subscribe(String topic) {
        subscribe(topic, null);
    }
    
    /**
     * 購読登録処理
     * @param topic トピック名
     * @param qos QoS指定
     */
    @Override
    public void subscribe(String topic, Integer qos) {
        try {
            if (qos == null) {
                qos = subQOS;
            }
            client.subscribe(new MQTTAWSIotTopic(this, topic, AWSIotQos.valueOf(qos)));
        } catch (AWSIotException ex) {
            AWSIotDeviceErrorCode err = ex.getErrorCode();
            if (err != null) {
                LOG.log(Level.WARNING, "reason: {0}", err.toString());
            }
            LOG.log(Level.WARNING, "message: {0}", ex.getMessage());
            LOG.log(Level.WARNING, "localize: {0}", ex.getLocalizedMessage());
            LOG.log(Level.WARNING, "cause: {0}", ex.getCause());
            LOG.log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * 切断処理 ※disconnectは最大30秒程度待機する場合がある
     */
    @Override
    public void disconnect() {
        if (client != null) {
            try {
                LOG.log(Level.INFO, "start MQTT disconnect");
                client.disconnect();
                LOG.log(Level.INFO, "MQTT disconnected");
            } catch (AWSIotException ex) {
                AWSIotDeviceErrorCode err = ex.getErrorCode();
                if (err != null) {
                    LOG.log(Level.WARNING, "reason: {0}", err.toString());
                }
                LOG.log(Level.WARNING, "message: {0}", ex.getMessage());
                LOG.log(Level.WARNING, "localize: {0}", ex.getLocalizedMessage());
                LOG.log(Level.WARNING, "cause: {0}", ex.getCause());
                LOG.log(Level.WARNING, null, ex);
            }
        }
    }
    
    /**
     * 接続状態を示す
     * @return 接続状態
     */
    @Override
    public boolean isConnected() {
        return (client != null);
    }
    
    /**
     * 送信結果
     * @param ret 送信結果通知
     */
    public void onMessage(MQTTAWSIotMessage ret) {
        manager.receiveMQTT(ret);
    }

    /**
     * メッセージ受信時に呼び出し
     * @param message 受信電文
     */
    public void messageArrived(AWSIotMessage message) {
        manager.receiveMQTT(message);
    }
    
}
