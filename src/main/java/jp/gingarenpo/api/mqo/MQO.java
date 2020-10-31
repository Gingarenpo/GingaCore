package jp.gingarenpo.api.mqo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import jp.gingarenpo.api.annotation.NeedlessMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * このクラスは、MQOオブジェクトを作成します。このオブジェクト自体は基本的に使わず、サブオブジェクトを頻繁に使う
 * ことになるでしょう。<strike>ちなみに、オブジェクトによる分別は今のところ考えていません。</strike>考えざるを
 * えなくなってしまいました。
 *
 * @author 銀河連邦
 */
public class MQO {

	/**
	 * オブジェクトの名前をキーとして格納しています
	 */
	private final HashMap<String, MQOObject> object = new HashMap<String, MQOObject>();


	/**
	 * モデルがある場所を指定することで、そのモデルを読み込んだ新しいMQOオブジェクトを作成します。
	 *
	 * @param r モデルがある場所のリソースロケーション。
	 * @throws IOException 存在しなかった時。
	 */
	public MQO(ResourceLocation r) throws IOException {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			throw new RuntimeException("Server side startup!!!");
		}
		// getMinecraft()が使用できない環境が存在するらしい
		parse(Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream());
	}

	/**
	 * モデルがある場所を文字列でパスとして指定します。主にMinecraft以外の用途で使用する場合に使うことを想定しています。
	 * このコンストラクタから呼び出した場合は、InputStreamはこちら側で閉じます（多分）。
	 * @param r パス文字列。パスに関しては、リソースフォルダーからの相対パスで指定する必要があります。
	 * 例えば、「src/main/resources」をソースフォルダとしている場合、「src/main/resources/test/abc.mqo」を
	 * 読み込むには、rに「test/abc.mqo」を指定します。
	 *
	 * @throws IOException 指定されたファイルが存在しなかった時
	 */
	@NeedlessMinecraft
	public MQO(String r) throws IOException {
		// Minecraftのリソースに頼らないやつ
		final InputStream is = ClassLoader.getSystemResourceAsStream(r);
		if (is == null) throw new IOException(r + " is not found!");
		parse(is);
		is.close();
	}

	/**
	 * InputStreamからファイルを読み込み、MQOフォーマットとして解釈してインスタンス内の値を設定します。
	 *
	 * @param is InputStreamを指定します。勝手に閉じないので2回目の呼び出しをする際はご注意
	 */
	private void parse(InputStream is) {
		try (Scanner s = new Scanner(is)) {
			// ということで読み込んでいきます。
			// まずは「Object "~~" {」を探します

			// 番号一覧
			//String o = ""; // オブジェクト名
			MQOObject obj = null; // 一時的に格納するオブジェクト（これを最後に追加するため）
			//int v = 0; // 頂点番号
			//int f = 0; // 面番号
			int col = 0; // 行番号

			// 正規表現一覧
			final String regexO = "Object \\\"(.+)\\\" \\{";
			final String regexVN = "[\t]*vertex [0-9]+ \\{";
			final String regexFN = "[\t]*face [0-9]+ \\{";
			final String regexV = "[\t]*[-]?[0-9\\.]+ [-]?[0-9\\.]+ [-]?[0-9\\.]+";
			final String regexF = "[\t]*[34] V\\(([0-9 ]+)\\) M\\(0\\) UV\\(([0123456789\\. ]+)\\)";

			// フラグ一覧
			boolean fo = false;
			boolean fv = false;
			boolean ff = false;

			while (s.hasNextLine()) {
				col++;
				// 形しか見ないのでこの3つ以外見ない
				final String line = s.nextLine(); // 1行取得
				if (!Pattern.matches(regexO, line) && !Pattern.matches(regexV, line)
						&& !Pattern.matches(regexF, line) && !Pattern.matches("[\t]*\\}", line)
						&& !Pattern.matches(regexVN, line)
						&& !Pattern.matches(regexFN, line)) {
					// いずれの正規表現にも一致しなかった
					// System.out.println("何にも一致しなかったです");
					continue; // 次の行へ（スキップ）
				} else if (Pattern.matches("[\t]*\\}", line)) {
					// 終了フラグの場合
					if (ff) {
						ff = false;
					} else if (fv) {
						fv = false;
					} else if (fo) {
						this.object.put(obj.getName(), obj); // 格納
						obj = null; // いったん解放
						fo = false;
					} else {
						continue; // 何もしない（関係ない）
					}
					continue; // 処理終了
				}

				// ここからはいろいろ分かれますああ大変

				if (ff) {
					// 次に求めるのはfであります
					if (!Pattern.matches(regexF, line)) // パターンが見つからないとき（MQOフォーマットが異常）
						throw new MQOException("Invalid mqo format!! (face expected but not.) at line " + col);

					// それを次の面番号に追加
					final Matcher m = Pattern.compile(regexF).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					final String vnum = m.group(1); // 頂点番号の格納
					final String uvnum = m.group(2); // UVの格納（頂点倍）

					// どうなっているかというと、グループ1=頂点番号（1 3 5） グループ2=材質番号（無視）
					// グループ3=UVマッピング（0-1正規化、頂点番号と同じ）
					// これをそれぞれ引数として渡す
					obj.getFaces().add(new MQOFace(obj, vnum, uvnum));
				} else if (fv) {
					// 頂点なので、次はvですね
					if (!Pattern.matches(regexV, line)) // パターンが見つからないとき（MQOフォーマットが異常）
						throw new MQOException("Invalid mqo format!! (vertex expected but not.) at line " + col);

					// それを次の頂点番号に追加
					final Matcher m = Pattern.compile(regexV).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					final String vnum = m.group(); // 頂点座標の格納
					obj.getVertexs().add(new MQOVertex(obj, vnum));
				} else if (Pattern.matches(regexO, line)) {
					// Objectの始まりだった場合
					// System.out.println("オブジェクトの始まりです");
					final Matcher m = Pattern.compile(regexO).matcher(line);
					m.find();
					obj = new MQOObject(this, m.group(1)); // 名前で作成
					fo = true;

				} else if (Pattern.matches(regexVN, line)) {
					// System.out.println("頂点の始まりです");
					fv = true;
				} else if (Pattern.matches(regexFN, line)) {
					// System.out.println("面の始まりです");
					ff = true;
				}

			}

		} catch (final MQOException e) {
			// クラッシュレポートです
			e.printStackTrace();
			final CrashReport c = CrashReport.makeCrashReport(e, "MQO format error");
			c.makeCategory("Model Loading");
			Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(c); // クラレポ表示
			Minecraft.getMinecraft().displayCrashReport(c);
		}
	}

	/**
	 * このMQOファイルが持つオブジェクトを返します。オブジェクト名を指定する形で返します。オブジェクトが存在しない
	 * 場合はNullが返ります。
	 * @param name オブジェクト名。
	 * @return オブジェクトがあればそのMQOObject、なければnull
	 */
	@Nullable
	public MQOObject getObject(String name) {
		return object.get(name);
	}

	/**
	 * このMQOファイルが持つオブジェクトの一覧を返します。あまり使わないでください。ループさせる目的で使用する場合は
	 * それ専用のメソッドを使用してください。
	 * @return オブジェクト一覧
	 */
	public HashMap<String, MQOObject> getObjects() {
		return object;
	}

	/**
	 * MQOオブジェクトをコレクションとして返します。拡張for文にそのまんま使用できるのでループさせたいときはこちらを
	 * ご利用ください。
	 * @return ループできるコレクションとして設定されたMQOObject
	 */
	public Collection<MQOObject> getObjects4Loop() {
		return object.values();
	}

	/**
	 * 各々のオブジェクトに存在する面に対してdrawFace()を呼び出すだけのラッピングメソッドです。
	 */
	public void draw() {
		// ラッピング処理
		for (final MQOObject obj : object.values()) {
			for (final MQOFace face : obj.getFaces()) {
				face.drawFace();
			}
		}
	}

	@SuppressWarnings("serial")
	public class MQOException extends RuntimeException {
		// 例外処理

		public MQOException(String mes) {
			super(mes);
		}
	}
}