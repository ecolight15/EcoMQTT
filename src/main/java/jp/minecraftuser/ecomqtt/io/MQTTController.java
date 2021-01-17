
package jp.minecraftuser.ecomqtt.io;

import jp.minecraftuser.ecomqtt.EcoMQTT;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTAlreadyExistException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTReceiverNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTRegisterFailException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * EcoMQTT制御クラス（外部プラグイン用）
 * @author ecolight
 */
public class MQTTController {
    protected final Plugin plg;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public MQTTController(Plugin plg_) {
        plg = plg_;
    }

    /**
     * MQTT publish 処理(デフォルトtopic)
     * {server}/p/{plugin} 等
     * @param payload 送信電文
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(byte[] payload) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        publish(null, payload, false);
    }

    /**
     * MQTT publish 処理(デフォルトtopic)
     * {server}/p/{plugin} 等
     * @param payload 送信電文
     * @param qos QoS指定
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(byte[] payload, Integer qos) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        publish(null, payload, false, qos);
    }

    /**
     * MQTT publish 処理(プラグインprefix付き)
     * {server}/p/{plugin}/topic 等
     * @param topic 送信トピック指定
     * @param payload 送信電文
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(String topic, byte[] payload) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        publish(topic, payload, false);
    }

    /**
     * MQTT publish 処理(プラグインprefix付き)
     * {server}/p/{plugin}/topic 等
     * @param topic 送信トピック指定
     * @param payload 送信電文
     * @param qos QoS指定
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(String topic, byte[] payload, Integer qos) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        publish(topic, payload, false, qos);
    }

    /**
     * MQTT publish 処理(プラグインprefix付き or 無し)
     * +/p/{plugin}/topic 等
     * @param topic 送信トピック指定
     * @param payload 送信電文
     * @param raw topicのプラグインprefix解除指定
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(String topic, byte[] payload, boolean raw) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        publish(topic, payload, raw, null);
    }
    
    /**
     * MQTT publish 処理(プラグインprefix付き or 無し)
     * +/p/{plugin}/topic 等
     * @param topic 送信トピック指定
     * @param payload 送信電文
     * @param raw topicのプラグインprefix解除指定
     * @param qos QoS指定
     * @throws EcoMQTTManagerNotFoundException
     * @throws EcoMQTTPluginNotFoundException
     */
    public void publish(String topic, byte[] payload, boolean raw, Integer qos) throws EcoMQTTManagerNotFoundException, EcoMQTTPluginNotFoundException {
        // EcoMQTT プラグインの取得とチェック
        Plugin p = Bukkit.getPluginManager().getPlugin("EcoMQTT");
        if (p == null) {
            throw new EcoMQTTPluginNotFoundException();
        }
        EcoMQTT plg = (EcoMQTT) p;
        
        // マネージャクラスインスタンスの取得とチェック
        MQTTManager m = (MQTTManager) plg.getPluginTimer("mqtt");
        if (m == null) {
            throw new EcoMQTTManagerNotFoundException();
        }

        // publish要求送信
        if (raw) {
            m.sendRawMQTT(topic, payload, qos);
        } else {
            m.sendPluginMQTT(this.plg.getName(), topic, payload, qos);
        }
    }
    
    /**
     * MQTTレシーバ登録処理(デフォルトtopic)
     * {server}/p/{plugin} 等
     * @param receiver レシーブハンドラ
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(MQTTReceiver receiver) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        registerReceiver(null, receiver);
    }

    /**
     * MQTTレシーバ登録処理(デフォルトtopic)
     * {server}/p/{plugin} 等
     * @param receiver レシーブハンドラ
     * @param qos QoS指定
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(MQTTReceiver receiver, Integer qos) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        registerReceiver(null, receiver, false, qos);
    }

    /**
     * MQTTレシーバ登録処理(プラグインprefix付き)
     * {server}/p/{plugin}/topic 等
     * @param topic 受信トピック指定(基本的にMQTTのSubscribe指定と同等)
     * @param receiver QoS指定
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        registerReceiver(topic, receiver, false);
    }
    
    /**
     * MQTTレシーバ登録処理(プラグインprefix付き)
     * {server}/p/{plugin}/topic 等
     * @param topic 受信トピック指定(基本的にMQTTのSubscribe指定と同等)
     * @param receiver レシーブハンドラ
     * @param qos QoS指定
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver, Integer qos) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        registerReceiver(topic, receiver, false, qos);
    }
    
    /**
     * MQTTレシーバ登録処理(プラグインprefix付き or 無し)
     * +/p/{plugin}/topic 等
     * @param topic 受信トピック指定(基本的にMQTTのSubscribe指定と同等)
     * @param receiver レシーブハンドラ
     * @param raw topicのプラグインprefix解除指定
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver, boolean raw) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        registerReceiver(topic, receiver, raw, null);
    }

    /**
     * MQTTレシーバ登録処理(プラグインprefix付き or 無し)
     * +/p/{plugin}/topic 等
     * @param topic 受信トピック指定(基本的にMQTTのSubscribe指定と同等)
     * @param receiver レシーブハンドラ
     * @param raw topicのプラグインprefix解除指定
     * @param qos QoS指定
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void registerReceiver(String topic, MQTTReceiver receiver, boolean raw, Integer qos) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        // EcoMQTT プラグインの取得とチェック
        Plugin p = Bukkit.getPluginManager().getPlugin("EcoMQTT");
        if (p == null) {
            throw new EcoMQTTPluginNotFoundException();
        }
        EcoMQTT plg = (EcoMQTT) p;
        
        // マネージャクラスインスタンスの取得とチェック
        MQTTManager m = (MQTTManager) plg.getPluginTimer("mqtt");
        if (m == null) {
            throw new EcoMQTTManagerNotFoundException();
        }

        try {
            // 登録関数呼び出し
            if (raw) {
                m.registerReceiver(topic, receiver);
            } else {
                m.registerReceiver(this.plg, topic, receiver);
            }
        } catch (EcoMQTTAlreadyExistException ex) {
            throw new EcoMQTTRegisterFailException(ex);
        }
    }

    /**
     * MQTTレシーバ登録解除処理(デフォルトtopic)
     * {server}/p/{plugin} 等
     * @param receiver
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void unregisterReceiver(MQTTReceiver receiver) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        unregisterReceiver(null, receiver);
    }

    /**
     * MQTTレシーバ登録解除処理(プラグインprefix付き)
     * {server}/p/{plugin}/topic 等
     * @param topic
     * @param receiver
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void unregisterReceiver(String topic, MQTTReceiver receiver) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        unregisterReceiver(topic, receiver, false);
    }

    /**
     * MQTTレシーバ登録解除処理(プラグインprefix付き or 無し)
     * +/p/{plugin}/topic 等
     * @param topic
     * @param receiver
     * @param raw
     * @throws EcoMQTTPluginNotFoundException
     * @throws EcoMQTTManagerNotFoundException 
     * @throws EcoMQTTRegisterFailException 
     */
    public void unregisterReceiver(String topic, MQTTReceiver receiver, boolean raw) throws EcoMQTTPluginNotFoundException, EcoMQTTManagerNotFoundException, EcoMQTTRegisterFailException {
        // EcoMQTT プラグインの取得とチェック
        Plugin p = Bukkit.getPluginManager().getPlugin("EcoMQTT");
        if (p == null) {
            throw new EcoMQTTPluginNotFoundException();
        }
        EcoMQTT plg = (EcoMQTT) p;
        
        // マネージャクラスインスタンスの取得とチェック
        MQTTManager m = (MQTTManager) plg.getPluginTimer("mqtt");
        if (m == null) {
            throw new EcoMQTTManagerNotFoundException();
        }

        try {
            // 登録解除関数呼び出し
            if (raw) {
                m.unregisterReceiver(topic, receiver);
            } else {
                m.unregisterReceiver(this.plg, topic, receiver);
            }
        } catch (EcoMQTTReceiverNotFoundException ex) {
            throw new EcoMQTTRegisterFailException(ex);
        }
    }
    
}
