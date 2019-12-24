
package jp.minecraftuser.ecomqtt.worker;

import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecomqtt.config.EcoMQTTConfig;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT 接続クラス
 * @author ecolight
 */
public final class MQTTConnection implements MqttCallback {
    private final MQTTManager manager;
    private static final Logger LOG = Logger.getLogger(MQTTConnection.class.getName());
    private boolean abort = false;
    private MqttClient client;
    private int pubQOS = 0;
    private int subQOS = 0;
    private boolean retain = false;
    
    /**
     * コンストラクタ
     * @param manager_
     * @param conf_ 
     */
    public MQTTConnection(MQTTManager manager_, EcoMQTTConfig conf_) {
        manager = manager_;
        connect(conf_, true);
    }
    
    /**
     * 接続処理
     * @param conf
     * @param force 
     */
    public void connect(EcoMQTTConfig conf, boolean force) {
        // 接続設定読み込み
        String type = conf.getString("Mqtt.Server.ConnectionType");
        String name = conf.getString("Mqtt.Server.UserName");
        String pass = conf.getString("Mqtt.Server.Password");
        String sslTrustStore = conf.getString("Mqtt.SSL.TrustStore");
        
        // 送受信設定読み込み
        pubQOS = conf.getInt("Mqtt.Publish.QoS");
        subQOS = conf.getInt("Mqtt.Subscribe.QoS");
        retain = conf.getBoolean("Mqtt.Publish.retain");
        
        // 接続処理
        try {
            if (client != null && client.isConnected()) {
                LOG.log(Level.INFO, "start MQTT disconnect");
                client.disconnect();
                LOG.log(Level.INFO, "MQTT disconnected");
            }
            if (client == null || force) {
                // クライアント作成 MemoryPersistenceの役割が不明瞭
                String url = conf.getString("Mqtt.Server.URL");
                String clid = UUID.randomUUID().toString(); // Todo:接続ごとに異なる好きな名前が振れる様なのでそのうち仕様検討する
                LOG.log(Level.INFO, "create MQTT client [{0}] cliendId[{1}]", new Object[]{url, clid});
                client = new MqttClient((type.equalsIgnoreCase("ssl") ? "ssl://" : "tcp://") + url, clid, new MemoryPersistence());
            }
            LOG.log(Level.INFO, "start MQTT connection");
            MqttConnectOptions conOpt = new MqttConnectOptions();
            
            // 接続ユーザー情報
            conOpt.setUserName(name);
            conOpt.setPassword(pass.toCharArray());

            // 接続時間設定
            conOpt.setConnectionTimeout(60);
            conOpt.setKeepAliveInterval(60);
            conOpt.setAutomaticReconnect(true);
            
            // SSL設定
            if (type.equalsIgnoreCase("ssl") && sslTrustStore.length() != 0) {
                Properties sslProp = new Properties();
                sslProp.put(SSLSocketFactoryFactory.TRUSTSTORE, sslTrustStore);
                sslProp.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, conf.getString("Mqtt.SSL.TrustStorePassword"));
                sslProp.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, conf.getString("Mqtt.SSL.TrustStoreType"));
                sslProp.put(SSLSocketFactoryFactory.CLIENTAUTH, false);
                String sslKeyStore = conf.getString("Mqtt.SSL.KeyStore");
                if (sslKeyStore.length() != 0) {
                    sslProp.put(SSLSocketFactoryFactory.KEYSTORE, sslKeyStore);
                    sslProp.put(SSLSocketFactoryFactory.KEYSTOREPWD, conf.getString("Mqtt.SSL.KeyStorePassword"));
                    sslProp.put(SSLSocketFactoryFactory.KEYSTORETYPE, conf.getString("Mqtt.SSL.KeyStoreType"));
                }
                conOpt.setSSLProperties(sslProp);
            }

            // 受信用コールバック設定
            client.setCallback(this);
            
            // 接続
            conOpt.setCleanSession(true);
            client.connect(conOpt);
            LOG.log(Level.INFO, "MQTT server connected.");
        } catch (MqttException ex) {
            if (!abort) {
                LOG.log(Level.WARNING, "reason: {}", ex.getReasonCode());
                LOG.log(Level.WARNING, "message: {}", ex.getMessage());
                LOG.log(Level.WARNING, "localize: {}", ex.getLocalizedMessage());
                LOG.log(Level.WARNING, "cause: {}", ex.getCause());
                LOG.log(Level.WARNING, null, ex);
            }
            abort = true;
        }
    }
    
    /**
     * メッセージ送信処理
     * @param topic
     * @param data 
     */
    public void publish(String topic, byte[] data) {
        try {
            client.publish(topic, data, pubQOS, retain);
            abort = false;
        } catch (MqttException ex) {
            if (!abort) {
                LOG.log(Level.WARNING, "reason: {}", ex.getReasonCode());
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
     * @param topic 
     */
    public void subscribe(String topic) {
        try {
            client.subscribe(topic, subQOS);
        } catch (MqttException ex) {
            LOG.log(Level.WARNING, "reason: {}", ex.getReasonCode());
            LOG.log(Level.WARNING, "message: {}", ex.getMessage());
            LOG.log(Level.WARNING, "localize: {}", ex.getLocalizedMessage());
            LOG.log(Level.WARNING, "cause: {}", ex.getCause());
            LOG.log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * メッセージ送信処理
     * @param topic
     * @param message 
     */
    public void send(String topic, String message) {
        publish(topic, message.getBytes());
    }
    
    /**
     * 切断処理 ※disconnectは最大30秒程度待機する場合がある
     */
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                LOG.log(Level.INFO, "start MQTT disconnect");
                client.disconnect();
                LOG.log(Level.INFO, "MQTT disconnected");
            } catch (MqttException ex) {
                LOG.log(Level.WARNING, "reason: {}", ex.getReasonCode());
                LOG.log(Level.WARNING, "message: {}", ex.getMessage());
                LOG.log(Level.WARNING, "localize: {}", ex.getLocalizedMessage());
                LOG.log(Level.WARNING, "cause: {}", ex.getCause());
                LOG.log(Level.WARNING, null, ex);
            }
        }
    }
    
    /**
     * 接続状態を示す
     * @return 
     */
    public boolean isConnected() {
        boolean ret = (client != null);
        if (ret) {
            ret = client.isConnected();
        }
        return ret;
    }
    
    /**
     * ブローカーとの切断処理でのコールバック呼び出し
     * @param thrwbl 
     */
    @Override
    public void connectionLost(Throwable thrwbl) {
        // 再接続必要であればする
        //isReady = false;
        
        // マネージャーになにかする
        LOG.log(Level.INFO, "MQTT connection lost");
        manager.receiveMQTT("topic", "connectionLost".getBytes());
    }

    /**
     * 送信完了時に呼び出し
     * @param imdt 
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        // パブリッシャ専用？
        LOG.log(Level.INFO, "MQTT delivery complete.");
    }

    /**
     * メッセージ受信時に呼び出し
     * @param topic
     * @param mm
     * @throws Exception 
     */
    @Override
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        manager.receiveMQTT(topic, mm.getPayload());
    }
    
}
