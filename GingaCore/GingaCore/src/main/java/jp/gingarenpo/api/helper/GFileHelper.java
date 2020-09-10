package jp.gingarenpo.api.helper;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import jp.gingarenpo.GingaCore;
import jp.gingarenpo.api.annotation.NeedlessMinecraft;
import jp.gingarenpo.api.interfaces.IModCore;
import net.minecraft.client.Minecraft;

/**
 * ファイル操作（I/O）に関するお役立ちメソッドを提供します。インスタンスを生成することはできません。
 * ほとんどのメソッドでIOException関連の例外をスローしますので、try - catch構文内で使用することをおすすめします。
 *
 * @author 銀河連邦
 *
 */
public class GFileHelper {

	private GFileHelper() {}; // コンストラクタは使用不可能

	/**
	 * このModの更新URL
	 */
	private static String URL = "https://ginren.info/G-Factory/Data/Addon/minecraft_new.txt";

	/**
	 * 最後に実行したアップデートバージョン。
	 */
	private static String version = null;

	/**
	 * 起動しているMinecraftのModフォルダを返します。
	 * @return
	 */
	public static File getModsDir() {
		// まずMinecraft自体の場所を取得する
		File mc = Minecraft.getMinecraft().mcDataDir.getAbsoluteFile(); // 絶対パスで取得する
		// Modsフォルダはその中の直下にある（末尾にピリオドがつくので削除して読み込む）
		mc = new File(mc.getAbsolutePath().substring(0, mc.getAbsolutePath().length()-1) + "mods");
		GingaCore.log.log(Level.INFO, "Mods Directory = " + mc.toString());
		return mc;
	}

	/**
	 * GingaCore（当Mod）において、新しいバージョンのModが公開されているかを判断します。サーバーに接続します。
	 * インターネットに接続できない場合や、サーバーがダウンしている場合、開発者が更新をサボっている場合は
	 * falseを返します。更新できるバージョンがある場合にのみtrueを返します。
	 *
	 * @return
	 */
	@NeedlessMinecraft
	public static boolean isUpdateOnServer() {
		boolean b;
		try {
			b = isUpdateOnServer(GFileHelper.URL, GingaCore.MODID, GingaCore.majorVersion, GingaCore.minorVersion);
			if (b) {
				GingaCore.log.info("GingaCoreに更新があります！　最新版→"+version+", 使用中→"+GingaCore.version);
			}
			else {
				GingaCore.log.info("お使いのGingaCoreは最新です。");
			}
			return b;
		} catch (VersionCheckException e) {
			// 何らかのエラーになったとき
			GingaCore.log.fatal(e.getMessage());
			return false;
		}
	}

	/**
	 * 内部で使用するロジックのため意味がありませんが、一応説明。指定したURLにアクセスしてチェックして、
	 * 更新があればtrue、なければfalseを返します。例外はすべてVersionCheckExceptionとして返します。
	 * このメソッドは直接呼び出すことができません。ラップされたメソッドをご利用ください。
	 *
	 * @param path 更新に必要な情報が記載されているファイル。書式に関してはエンドユーザー呼び出しメソッド参照。
	 * @param modid そのModのModID。歴史的経緯から小文字大文字どちらでもOKだが小文字推奨。
	 * @param majorVersion 実行しているModのメジャーバージョン。
	 * @param minorVersion 実行しているModのマイナーバージョン。複数指定することができます、一応。
	 * @return
	 */
	@NeedlessMinecraft
	private static boolean isUpdateOnServer
		(String path, String modid, int majorVersion, int ...minorVersion) throws VersionCheckException {
		// サーバーに接続する（配布元＝開発者自身のサーバーね）
				java.net.URL url = null; // こうしないと参照できない
				HttpURLConnection  c = null; // コネクション

				try {
					url = new java.net.URL(path); // URLを読み込む
				} catch (MalformedURLException e) {
					// URLがおかしい場合（本来あり得ないが）
					throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]URLが不正です。");
				}

				// コネクションを作成する
				try {
					c = (HttpURLConnection) url.openConnection(); // コネクションを確立させる
				} catch (IOException e) {
					throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]コネクションを確立できませんでした。");
				}

				// 設定する
				c.setDoInput(true); // 入力は受け付ける
				c.setReadTimeout(2000); // リクエストボディ取得は2秒以内（そこまで待っていられない）
				c.setConnectTimeout(2000); // 通信確立タイムアウトは2秒（同じく待っていられない）
				try {
					c.setRequestMethod("GET"); // GET通信に固定する
				} catch (ProtocolException e) {
					// これも到達することはまずないが、通信方法が変な場合
					throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]通信プロトコルが異常です。");
				}

				// 接続する
				try {
					c.connect(); // 接続開始
				} catch (IOException e) {
					// 接続に失敗した場合
					throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]最新情報の取得に失敗しました。");
				}

				// 中身を取得する
				try (Scanner s = new Scanner(c.getInputStream())) {
					if (c.getResponseCode() != 200) throw new IOException("Bad status code " + c.getResponseCode());
					// MODID=バージョン　として格納されている
					while (s.hasNextLine()) {
						// 1行取得する
						String str = s.nextLine(); // 1行
						Matcher m = Pattern.compile("([^=]+)=(.+)").matcher(str); // 該当書式かどうかを判定
						if (!m.find()) {
							// System.out.println("[範囲外]" + str);
							continue;
						}; // 見つからない場合はあきらめる
						// 見つかっているため取り出す
						try {
							version = m.group(2); // 再代入
							if (m.group(1).equalsIgnoreCase(modid)) {
								// 一致した場合
								// System.out.println("[発見！]" + str);
								// バージョン番号を確かめる（X.Y.Z.…）
								String[] serverVersion = m.group(2).split("\\.", minorVersion.length + 1); // 2分割すると「1.2.3」なら「1」と「2.3」
								if (Integer.parseInt(serverVersion[0]) > majorVersion) {
									// サーバーのほうがメジャーバージョン大きいのは明らかに更新があるので
									return true;
								}

								// 次の下のところで比べる
								for (int i = 0; i < minorVersion.length; i++) {
									if (Integer.parseInt(serverVersion[i+1]) > minorVersion[i]) {
										// 更新がありますね
										return true;
									}
								}
							}
						} catch (IllegalStateException e) {
							// グループマッチに失敗した場合
							throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]内部エラーが発生しました。");
						}
					}

					// ここまで来ちゃったら見つからないので
					return false;

				} catch (IOException e) {
					// インプットストリームが取得できない場合
					throw new GFileHelper().new VersionCheckException("[isUpdateOnServer]取得時エラー: " + e.getLocalizedMessage());
				}
	}

	/**
	 * <p>IModCoreインターフェースを実装したクラスを指定することで、そのModの更新の有無を代わりに取得してくれます。
	 * やっていることは当Mod専用メソッドと同じもので、更新がある場合にtrue、更新がない場合はfalse、
	 * エラーが発生した場合はログに出力して例外を返しています。</p>
	 * <p>プライベートメソッドにパラメーターの設定あるんだからそれを利用すればいいじゃんと思われるかもしれませんが、
	 * あくまでインターフェースなどから取得したい開発者の探求心、チェックメソッドをあっちでは実行できない、将来の
	 * アノテーション対応を考えてこのような形をとっています。ご了承ください。</p>
	 * <hr>
	 * <h2>更新データの場所</h2>
	 * <p>更新ファイルは、文字列として読み取れるテキストファイルとします。拡張子は何であっても構いません。
	 * まずWebに置かれると思いますが、その際には<b>ブラウザでそのファイルを表示できる</b>ことが条件です。</p>
	 * <p>そのデータファイルを適切な場所に置いたら、そのURLをインターフェース内のメソッドで指定します。</p>
	 * <h2>書式について</h2>
	 * <p>更新データは複数のModを記述することもできます。簡易的な対応ですが、「Javaプロパティ書式」に若干近い感じです。</p>
	 * <p>ForgeのConfigとも近い感じですが、概ね以下のような感じです。</p>
	 * <ul>
	 * <li>「modid=バージョン」が1行です。modidは、そのModのIDです。名前ではありません。</li>
	 * <li>それ以外の行は空行含めすべて無視されます。ただし、簡易チェックしかしていないので、行内に「=」が含まれると
	 * エラーを起こします。日本語はあっても構いませんがUTF-8以外での文字コードはお勧めしていません。</li>
	 * <li>バージョンは「1.2.3.4」のように記述します。使用できる文字は「.」と数字だけです。半角で入力しましょう。</li>
	 * <li>バージョン番号のチェックは甘いため、変な数字入れるとクラッシュします（いずれチェック厳密にしますが）</li>
	 * </ul>
	 * <p>以下は、例となります。「gingacore」のバージョン「2.4」がリリースされているとします。</p>
	 * <hr>
	 * <pre># ここは無視されます
	 * こんな感じで書いても無視されます
	 * ああああああああああああああああああああああ　←無視されます
	 * gtc=9.9.9 ←MODIDが違うため無視されます
	 * gingacore=89L ←<b>絶対に書かないでください。使用不可能な文字があるためクラッシュします。</b>
	 * gingacore=2.4 ←このように入力します。
	 *
	 * ↑空行も無視されますしこの行も無視されます
	 * </pre>
	 * <hr>
	 * <p>詳しいことや最新の情報は配布サイトかなんかで掲載する予定なので暇があればチェックしてみてください。</p>
	 * @param mod インターフェース「IModCore」を実装したModメインクラスのインスタンス。
	 * @return 上記参照
	 * @throws VersionCheckException エラーが発生したとき。getMessageで詳細を取得することができます。
	 */
	@NeedlessMinecraft
	public static boolean isUpdateOnServer(IModCore mod) throws VersionCheckException {
		// まだチェック入れていないのでそのままラップしちゃいますが…（）
		return isUpdateOnServer(mod.getURL(), mod.getModId(), mod.getMajorVersion(), mod.getMinorVersion());
	}

	/**
	 * 最後に実行したアップデートチェックのバージョンを取得します。一度「isUpdateOnServer」メソッドを実行する必要があり、
	 * その実行が成功しないとnullが返されます。また実行して成功するたびに上書きされます。ログを取るときとかに使って
	 * 見てはいかがでしょうか。
	 * @return
	 */
	@Nullable
	public static String getLastCheckedVersion() {
		return version;
	}

	/**
	 * バージョンチェック時の例外です。
	 * @author 銀河連邦
	 *
	 */
	public class VersionCheckException extends Exception {

		public VersionCheckException(String mes) {
			super(mes);
		}
	}
}
