# EcoMQTT
spigot plugin mqtt interface

![MQTTで適当なブローカーとおしゃべり](https://ecolight15.github.io/img/pubsub.png "MQTTで適当なブローカーとおしゃべり")
よき


### 他のプラグインからこのプラグインを中継してMQTT pub/subする場合
- MQTTControllerがpublish実行/subscribeハンドラ登録するクラスなので以下のようなMQTTReceiverインタフェース実装したクラスを作る。

```java
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
```

- publishするとき
```java
EcoMQTTCommandController rec = new EcoMQTTCommandController(plg);
rec.publish("topic", "message".getBytes());
```

- subscribeするとき
```java
rec.registerReceiver("topic", rec, true);
```

詳しくは jp.minecraftuser.ecomqtt.commands あたりのソースコード参照

----
### メモ
- EcoFrameworkプラグイン前提
- キューに貯めて非同期でpublishしてるけど、コネクション数節約のために順次送信なので遅い（件数×ping時間なイメージ）。
- 自分用なので細かいところは割愛
