package jp.gingarenpo.api.helper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
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
	 * solvePathの検索モードで、modsフォルダを検索します。
	 */
	public static final int SEARCH_MODS = 1;

	/**
	 * solvePathの検索モードで、Minecraftの実行フォルダを検索します。
	 */
	public static final int SEARCH_RUN = 2;

	/**
	 * solvePathの検索モードで、自身のリソースを検索します。
	 */
	public static final int SEARCH_RESOURCE = 4;

	/**
	 * solvePathの検索モードで、ドライブルートフォルダを検索します。
	 */
	public static final int SEARCH_DRIVE = 8;

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
	 * バージョンチェック時の例外です。ラップしているだけなので中身はありません。
	 * @author 銀河連邦
	 *
	 */
	public class VersionCheckException extends Exception {

		/**
		 * シリアルナンバー
		 */
		private static final long serialVersionUID = 1L;

		public VersionCheckException(String mes) {
			super(mes);
		}
	}

	// -----------------------------------------------------------------------------------------------------------
	// Ver2.1 Added
	// -----------------------------------------------------------------------------------------------------------

	/**
	 * <p>指定した文字列をパスとして認識して、そのパスを解決し、Mod内で読み込めるロケーションの形で読み込みを試みます。</p>
	 * <hr>
	 * <p>相対パスでも絶対パスでも構いません。パスを絶対パスとして認識した場合は、絶対パスとした際のファイルを作成して返します。ただし、
	 * そのファイルが存在しない場合はnullを返します。したがって、nullチェックを行うだけで判断できます。</p>
	 * <p>相対パスが指定された場合（相対パスと認識された場合）は、以下の優先度で存在を確認します。存在が確認できた時点でそのファイルの
	 * インスタンスを作成し、返却します。いずれでも解決できない場合はnullを返します。したがって、相対パスで指定した結果がFileインスタンス
	 * だった場合は、必ずその場所にファイルが存在していることになります（ただしその後削除された場合は動作未定です）。</p>
	 * <p>相対パスの解決優先度は次の順番となっています。例で示す際、指定したstr引数（パス）は「abc.txt」だったとします。</p>
	 * <ol>
	 * <li>Modsディレクトリをルートとした相対パスとみなし、その中から探します。例→「C:\minecraft\mods\abc.txt」。</li>
	 * <li>Minecraftが実行されているディレクトリをルートとした相対パスとみなします。例→「C:\minecraft\abc.txt」。</li>
	 * <li>それでも見つからない場合、このクラスがロードされたクラスローダーからシステムリソースを指定しているものとみなし、当Mod内から
	 * リソースを検出します。他Modの検出は行えません。例→「C:\minecraft\mods\GTC.jar\abc.txt」。</li>
	 * <li>最終手段として、Minecraftを実行しているシステムドライブの直下をルートとした検索を行います。例→「C:\abc.txt」。ただし、これは
	 * 管理者権限がない状態でシステムドライブを参照した場合などに読み込めない場合があります。</li>
	 * </ol>
	 * <p>なお、複数発見はされず、上記の優先度で最初に見つかったファイルを返します。優先度を変更することはできませんが、オーバーロードされた
	 * メソッドを使用することで、特定の処理のみを実行させることができます。</p>
	 * <hr>
	 * <p>優先度の概念は将来変更される恐れがあります。優先度に依存した呼び出しを行わないで、なるべく絶対パスで記述することをお勧めします。</p>
	 *
	 * @param str 取得したいファイルを指定したパス。絶対パスを指定する場合は「C:\abc.txt」のようにドライブ名から入力してください。
	 * 内部ではFileオブジェクトのパス取得結果によって判断しています。
	 * @return 絶対パスでファイルが存在する場合、または相対パスで上記優先度をもとにファイルが存在したらそのファイルのインスタンスが返り、
	 * いかなる検索をしてもファイルが見つからない場合はnullを返します。
	 */
	public static File solvePath(String str) {
		// 絶対パスかそうでないかの判断を行う（簡易的なチェックですが）
		File test = new File(str);
		if (test.getPath().contentEquals(test.getAbsolutePath())) {
			// 絶対パスの場合
			return test;
		}

		// 相対パスの場合
		File file = solvePathAtMods(str);
		if (file != null) return file;
		file = solvePathAtMinecraft(str);
		if (file != null) return file;
		file = solvePathAsResource(str);
		if (file != null) return file;
		file = solvePathAtDrive(str);
		if (file != null) return file;
		return null;
	}

	/**
	 * <p>指定した文字列をパスと認識して、そのパスを指定された検索方法を使用して読み込みを試みます。</p>
	 * <hr>
	 * <p>このオーバーロードメソッドは、相対パスの解決に使用します。通常は優先度の高いもので検出された場合にそこで処理が終了するため
	 * それより低いところのものが欲しい場合には取得できなくなります。その際に、場所が分かっている場合はこちらを利用できます。</p>
	 * <p>ただ、場所が分かっているならこのメソッド使わなくてもいいと思うので、実質開発者専用のメソッドです。</p>
	 * @param str 取得したいファイルを指定したパス。
	 * @param flug 定数として指定します。「SEARCH_」から始まる定数を利用します。OR演算で複数指定することができますが、複数指定した
	 * 場合は上記の優先度順に検索されます。
	 * @return 指定されたファイルがあればFileインスタンス、なければnull。
	 */
	public static File solvePath(String str, int flug) {
		File file = null; // まずは宣言しておく

		if ((flug & SEARCH_MODS) != 0) {
			file = solvePathAtMods(str);
			if (file != null) return file;
		}
		if ((flug & SEARCH_RUN) != 0) {
			file = solvePathAtMinecraft(str);
			if (file != null) return file;
		}
		if ((flug & SEARCH_RESOURCE) != 0) {
			file = solvePathAsResource(str);
			if (file != null) return file;
		}
		if ((flug & SEARCH_DRIVE) != 0) {
			file = solvePathAtDrive(str);
			if (file != null) return file;
		}

		return file; // =null
	}

	/**
	 * パスを解決する際に使用するもので、「Mods」ディレクトリを起点としたファイルオブジェクトを返します。内部でしか使用しない
	 * ため使う必要はありません。
	 *
	 * @param str 文字列
	 * @return あればFileインスタンス、なければnull
	 */
	private static File solvePathAtMods(String str) {
		// Modsディレクトリを取得する
		File mods = GFileHelper.getModsDir(); // Modsフォルダ
		File file = new File(mods.getAbsolutePath() + "/" + str);
		return (file.exists()) ? file : null;
	}

	/**
	 * パスを解決する際に使用するもので、「Minecraft」ディレクトリを起点としたファイルオブジェクトを返します。内部でしか使用しない
	 * ため使う必要はありません。
	 *
	 * @param str 文字列
	 * @return あればFileインスタンス、なければnull
	 */
	private static File solvePathAtMinecraft(String str) {
		// Modsディレクトリを取得する
		File mods = Minecraft.getMinecraft().mcDataDir.getAbsoluteFile(); // Minecraftフォルダー
		File file = new File(mods.getAbsolutePath() + "/" + str);
		return (file.exists()) ? file : null;
	}

	/**
	 * パスを解決するために使用するもので、このクラスが呼び出されているクラスローダーからリソースとして参照した際のファイルオブジェクトを
	 * 返します。内部でしか使用しないため使う必要はありません。
	 * @param str 文字列
	 * @return
	 */
	private static File solvePathAsResource(String str) {
		java.net.URL res = ClassLoader.getSystemResource(str);
		return (res == null) ? null : new File(res.getPath());
	}

	/**
	 * パスを解決する際に使用するもので、Minecraftのデータが格納してあるドライブ（Cがほとんどでしょう）から検索を試みます。
	 * セキュリティ的にに怪しいところがあるため、フラグ指定の場合は使用すべきではありません。
	 *
	 * @param str 文字列
	 * @return あればFileインスタンス、なければnull
	 */
	private static File solvePathAtDrive(String str) {
		// Minecraftのフォルダを取得する
		File mods = Minecraft.getMinecraft().mcDataDir.getAbsoluteFile(); // Minecraftフォルダー
		// このフォルダの最初の文字がドライブレターになっている
		File file = new File(mods.getAbsolutePath().substring(0, 3) + str); // C:\とか
		return (file.exists()) ? file : null;
	}

	// -----------------------------------------------------------------------------------------------------------
	// Ver2.2 Added
	// -----------------------------------------------------------------------------------------------------------

	/**
	 * このメソッドは、URLなどで生成されたパスを無理くり変換したものを、正規化した絶対パスに直すことができるメソッドです。例えば、
	 * JarファイルのリソースURLを取得すると「file:/C:/abc…」と言った余分な部分が出てしまいます。本来の絶対パスは、Windowsなら
	 * ドライブレターから始まるはず。その是正をするメソッドとなります。返される文字列をFileなりFilesなりで使用すれば
	 * まともに動くはずです。
	 * <hr>
	 * <span style='color: red; font-weight: bold;'>Windowsでしか動作確認をしていないため、Macなどでは壊れる可能性があります。</span>
	 *
	 * @param path 変換したいパス。
	 * @return 変換したパス。
	 */
	public static String getAbsoluteNormalizePath(String path) {
		String res = null;
		if (path.startsWith("file:/")) {
			res = path.substring(6);
		}
		else if (path.startsWith("/")) {
			res = path.substring(1);
		}
		return res;
	}

	/**
	 * 指定されたクラスが存在するURLを返却します。Jarファイルの場合は自身の実行Jarファイルが返ってきます。というかもっぱらJarファイル用に最適化
	 * しています。
	 * @param clazz パスを取得したいクラス。
	 * @return パスクラス。
	 * @throws UnsupportedEncodingException エンコードが存在しない場合
	 */
	public static String getAbsoluteClassPath(Class<?> clazz) throws UnsupportedEncodingException {
		final ProtectionDomain pd = clazz.getProtectionDomain();
		final CodeSource cs = pd.getCodeSource(); // コードソース
		final String path = getAbsoluteNormalizePath(URLDecoder.decode(cs.getLocation().getPath(), "UTF-8")); // クラス自体があるそこまでの絶対パスでJarなら.jar!以降要らない
		return path.substring(0, path.indexOf("!")); // これでよし
	}


}
