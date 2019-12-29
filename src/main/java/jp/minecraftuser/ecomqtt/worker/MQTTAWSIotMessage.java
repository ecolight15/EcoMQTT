
package jp.minecraftuser.ecomqtt.worker;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

/**
 * AWSパブリッシュメッセージクラス
 * @author ecolight
 */
public class MQTTAWSIotMessage extends AWSIotMessage {
    private final MQTTConnectionAWS con;
    enum RET { SUCCESS, FAILURE, TIMEOUT }
    public RET ret;
    
    /**
     * コンストラクタ
     * @param con_ AWSコネクションインスタンス
     * @param topic トピック名
     * @param qos QoS指定
     * @param payload 送信電文
     */
    public MQTTAWSIotMessage(MQTTConnectionAWS con_, String topic, AWSIotQos qos, byte[] payload) {
        super(topic, qos, payload);
        con = con_;
    }

    /**
     * 送信成功ハンドラ
     */
    @Override
    public void onSuccess() {
        ret = RET.SUCCESS;
        con.onMessage(this);
    }

    /**
     * 送信失敗ハンドラ
     */
    @Override
    public void onFailure() {
        ret = RET.FAILURE;
        con.onMessage(this);
    }

    /**
     * 送信タイムアウトハンドラ
     */
    @Override
    public void onTimeout() {
        ret = RET.TIMEOUT;
        con.onMessage(this);
    }

}
