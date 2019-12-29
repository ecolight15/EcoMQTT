
package jp.minecraftuser.ecomqtt.worker;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

/**
 * AWS サブスクライブ用ハンドラ
 * @author ecolight
 */
public class MQTTAWSIotTopic extends AWSIotTopic {
    MQTTConnectionAWS con;
    
    /**
     * コンストラクタ
     * @param con_ AWSコネクションインスタンス
     * @param topic トピック名
     */
    public MQTTAWSIotTopic(MQTTConnectionAWS con_, String topic) {
        super(topic);
        con = con_;
    }
    
    /**
     * コンストラクタ
     * @param con_ AWSコネクションインスタンス
     * @param topic トピック名
     * @param qos QoS指定
     */
    public MQTTAWSIotTopic(MQTTConnectionAWS con_, String topic, AWSIotQos qos) {
        super(topic, qos);
        con = con_;
    }

    /**
     * メッセージハンドラ
     * @param message サブスクライブ受信メッセージ
     */
    @Override
    public void onMessage(AWSIotMessage message) {
        super.onMessage(message); //To change body of generated methods, choose Tools | Templates.
        con.messageArrived(message);
    }

}
