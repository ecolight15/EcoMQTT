# EcoMQTT

SpigotプラグインのMQTTインターフェース  
MinecraftサーバーからMQTTブローカーとの通信を行うためのプラグインです。

![MQTTで適当なブローカーとおしゃべり](https://ecolight15.github.io/img/pubsub.png "MQTTで適当なブローカーとおしゃべり")
よき

## 機能

- コマンドによるMQTTメッセージの送受信
- 他のプラグインからのMQTT操作API
- スレッドセーフな非同期MQTT処理
- 通常のMQTTブローカーとAWS IoT Core両対応
- SSL/TLS接続サポート
- 設定可能なトピック形式
- 送受信ログ機能
- QoS設定とRetain機能

## 前提条件

- Spigot 1.18.2以上
- EcoFrameworkプラグイン（v0.16以上）
- Java 8以上

## インストール

1. EcoFrameworkプラグインを先にインストールしてください
2. EcoMQTT.jarをpluginsフォルダに配置
3. サーバーを起動してconfig.ymlを生成
4. MQTTブローカーの設定を行う

## 設定

### 設定ファイル（config.yml）

```yaml
# MQTT受信処理の間隔（tick）
mqtt-receive-interval: 5

# MQTTサーバータイプ（Other または AWS）
MQTTType: Other

# 通常のMQTTブローカー設定
Mqtt:
  Server:
    ConnectionType: "ssl"    # "ssl" または "tcp"
    URL: "broker.example.com:8883"
    CleanSession: true
    UserName: "username"
    Password: "password"
  Publish:
    QoS: 0                   # 0, 1, 2
    retain: false
  Subscribe:
    QoS: 0
  SSL:
    TrustStore: ""
    TrustStorePassword: ""
    TrustStoreType: "JKS"
    # ... その他SSL設定

# AWS IoT Core設定
AWS:
  Server:
    URL: "xxxxxxxxx.iot.ap-northeast-1.amazonaws.com:8883"
    CleanSession: true
  Security:
    RootCaFile: "path/to/root-ca.pem"
    CertificateFile: "path/to/certificate.pem.crt"
    PrivateKeyFile: "path/to/private.pem.key"
  Publish:
    QoS: 0
  Subscribe:
    QoS: 0

# トピック設定
Topic:
  ServerName: "mcserver"
  Format:
    System:
      Info: "{server}/s/info"
      Cmd: "{server}/s/cmd"
    Plugin: "{server}/p/{plugin}"

# ログ設定
Log:
  Publish:
    Console: true
    File:
      Enable: false
      Path: "publish.log"
  Subscribe:
    Console: true
    File:
      Enable: false
      Path: "subscribe.log"
```

## コマンド

### 基本コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/pub <message>` | メッセージをパブリッシュ | `ecomqtt.publish` |
| `/sub <topic>` | トピックをサブスクライブ | `ecomqtt.subscribe` |
| `/unsub <topic>` | サブスクライブを解除 | `ecomqtt.unsubscribe` |
| `/ecm` | メインコマンド | `ecomqtt` |
| `/ecm reload` | 設定をリロード | `ecomqtt.reload` |

### 使用例

```
# メッセージを送信
/pub Hello World!

# トピックをサブスクライブ
/sub test/topic

# サブスクライブを解除
/unsub test/topic

# 設定をリロード
/ecm reload
```

## API - 他のプラグインからの使用方法

### 他のプラグインからこのプラグインを中継してMQTT pub/subする場合

MQTTControllerがpublish実行/subscribeハンドラ登録するクラスなので以下のようなMQTTReceiverインタフェース実装したクラスを作る。

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

### パブリッシュの方法

```java
EcoMQTTCommandController rec = new EcoMQTTCommandController(plg, sender);
rec.publish("topic", "message".getBytes());
```

### サブスクライブの方法

```java
rec.registerReceiver("topic", rec, true);
```

### 詳細なAPI

詳しくは `jp.minecraftuser.ecomqtt.commands` と `jp.minecraftuser.ecomqtt.io` パッケージのソースコード参照

## トピック形式

トピックは設定ファイルで定義された形式に従って自動的に生成されます：

- `{server}` : サーバー名（`Topic.ServerName`で設定）
- `{plugin}` : プラグイン名

例：
- システム情報: `mcserver/s/info`
- システムコマンド: `mcserver/s/cmd`
- プラグイン用: `mcserver/p/YourPlugin`

## 技術的な詳細

### 非同期処理

- MQTTの送受信は非同期で処理されます
- キューに貯めて順次送信するため、大量のメッセージ送信時は遅延が発生する可能性があります
- コネクション数を節約するため順次送信を採用（件数×ping時間程度の遅延が発生）

### QoS（Quality of Service）

- **QoS 0**: 最大1回配信（保証なし）
- **QoS 1**: 最低1回配信（重複の可能性あり）
- **QoS 2**: 確実に1回配信（常に1回のみ送信）

### SSL/TLS接続

SSL/TLS接続を使用する場合は、適切な証明書とキーストアの設定が必要です。

## トラブルシューティング

### よくある問題

1. **MQTTブローカーに接続できない**
   - config.ymlのURL、ユーザー名、パスワードを確認
   - ネットワーク接続を確認
   - SSL証明書の設定を確認

2. **メッセージが受信されない**
   - サブスクライブが正しく設定されているか確認
   - トピック名が正しいか確認
   - QoS設定を確認

3. **プラグインが起動しない**
   - EcoFrameworkプラグインがインストールされているか確認
   - Java 8以上が使用されているか確認

### ログの確認

ログ設定を有効にして、送受信の詳細を確認できます：

```yaml
Log:
  Publish:
    Console: true
    File:
      Enable: true
      Path: "publish.log"
  Subscribe:
    Console: true
    File:
      Enable: true
      Path: "subscribe.log"
```

## 開発情報

- **バージョン**: 0.10
- **対応Minecraft**: 1.18.2+
- **使用ライブラリ**: 
  - Eclipse Paho MQTT Client (1.2.5)
  - AWS IoT Device SDK for Java (1.3.4)
  - Apache Commons Collections4 (4.4)

----

### メモ
- EcoFrameworkプラグイン前提
- キューに貯めて非同期でpublishしてるけど、コネクション数節約のために順次送信なので遅い（件数×ping時間なイメージ）。
- 自分用なので細かいところは割愛
