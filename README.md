# GingaCore
![Logo](https://github.com/Gingarenpo/GingaCore/logo.png)
Minecraft Mod 1.12.2におけるModお役立ちメソッドを提供するものとなっております。基本的に配布は https://ginren.info/G-factory/ で行っております。

## 中身について
このModは、導入するだけではMinecraft内に何か追加・変更を加えることはありません。前提Modとして、ほかのModで使用することができるヘルパーメソッドなどを追加したModとなっています。基本的には開発者である銀河連邦自身が適当に「こういうメソッド使いたいんだよな」って思ったものを適宜付け加えています。

## 対応バージョンについて
残念ながら、このバージョンは「1.12.2」のみ対応しています。既に過去の産物となりかけている1.12.2ですが、1.13はメソッドの仕様が変わっているため、一部のメソッドは動作しません。ただし、「`@NeedlessMinecraft`」アノテーションがついているメソッドは、Minecraftの機能に依存しないため、Minecraftを読み込んでいなくても使用できます。なお、Minecraft内のお役立ちメソッドとして提供していますが、その他の用途にも使用することができます。

## JavaDocについて
作っていますが適当です。詳しいことはソースコード見るなりなんなりしてください。大体直感的に操作できるようにしているはずです。

## ライセンスについて
ライセンスに関してです。このModというかソースコードは著作権法上で定められた範囲での使用を認めています。特にApacheLicenseとかつけていません。Minecraft以外の利用も構いません。ただし、Minecraft1.12.2以外では動作確認していません。拙作Javaゲーム「数字ルーレット」にもこれを使用しています。
ちなみに埋め込みに関してですが、当ライブラリをJarファイルにバインドするのは許可しています。ただし、バインドする場合は「release」内にある完成品のjarファイルとしてください。当ソースコードをエンドユーザー側で再コンパイルしてバインドすることは禁止とさせていただきます。

## 改造について
パラメーター名の変更など内部動作に全く関係ない程度の改造は許可しますが、メソッドの可視性を変えたりするのは禁止します。丸パクで自身のものとして公開するのも禁止です（ただアルゴリズムを参考にすることは全く持って問題ありません。コピペが禁止なだけです）。もし、追加で実装したいものがあれば「アルゴリズムを参考にして」自分で勝手にクラスでもなんでも作成してください。

## 要望について
要望は受け付けております（GitHubの使い方まだよくわかっていませんが、どこかでできないのかな？）。プルリクは違うと思うので、まあ銀河連邦までご相談ください。

## 共同開発したい方向け
いないと思いますがIssuesの問題などを解決してくださる方がいらっしゃれば確認したうえで採用させていただくかもしれません。コミットは必要な方にしか許可しないつもりです。

## その他
詳細は変更になることもあるので定期的にここ読んでいってください。少しずつ加筆・修正をしていく予定です。多分。
