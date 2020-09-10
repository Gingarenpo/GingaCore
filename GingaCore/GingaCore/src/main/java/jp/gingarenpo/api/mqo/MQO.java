package jp.gingarenpo.api.mqo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

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
	private HashMap<String, MQOObject> object = new HashMap<String, MQOObject>();


	/**
	 * モデルがある場所を指定することで、そのモデルを読み込んだ新しいMQOオブジェクトを作成します。
	 *
	 * @param r
	 */
	public MQO(ResourceLocation r) {
		// FIXED Client001: noSuchMethodErrorで止まるし不必要なので削除
		// String path = "assets/" + r.getResourceDomain() + "/" + r.getResourcePath();
		parse(r);
	}

	public MQO(String r) {
		// 無理やりパスを生成します
	}

	/**
	 * ファイルを読み込んでその中身を解析します。
	 *
	 * @param path
	 */
	private void parse(ResourceLocation r) {
		try (Scanner s = new Scanner(Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream())) {
			// ということで読み込んでいきます。
			// まずは「Object "~~" {」を探します

			// 番号一覧
			//String o = ""; // オブジェクト名
			MQOObject obj = null; // 一時的に格納するオブジェクト（これを最後に追加するため）
			//int v = 0; // 頂点番号
			//int f = 0; // 面番号
			int col = 0; // 行番号

			// 正規表現一覧
			String regexO = "Object \\\"(.+)\\\" \\{";
			String regexVN = "[\t]*vertex [0-9]+ \\{";
			String regexFN = "[\t]*face [0-9]+ \\{";
			String regexV = "[\t]*[-]?[0-9\\.]+ [-]?[0-9\\.]+ [-]?[0-9\\.]+";
			String regexF = "[\t]*[34] V\\(([0-9 ]+)\\) M\\(0\\) UV\\(([0123456789\\. ]+)\\)";

			// フラグ一覧
			boolean fo = false;
			boolean fv = false;
			boolean ff = false;

			while (s.hasNextLine()) {
				col++;
				// 形しか見ないのでこの3つ以外見ない
				String line = s.nextLine(); // 1行取得
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
					Matcher m = Pattern.compile(regexF).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					String vnum = m.group(1); // 頂点番号の格納
					String uvnum = m.group(2); // UVの格納（頂点倍）

					// どうなっているかというと、グループ1=頂点番号（1 3 5） グループ2=材質番号（無視）
					// グループ3=UVマッピング（0-1正規化、頂点番号と同じ）
					// これをそれぞれ引数として渡す
					obj.getFaces().add(new MQOFace(obj, vnum, uvnum));
				} else if (fv) {
					// 頂点なので、次はvですね
					if (!Pattern.matches(regexV, line)) // パターンが見つからないとき（MQOフォーマットが異常）
						throw new MQOException("Invalid mqo format!! (vertex expected but not.) at line " + col);

					// それを次の頂点番号に追加
					Matcher m = Pattern.compile(regexV).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					String vnum = m.group(); // 頂点座標の格納

					obj.getVertexs().add(new MQOVertex(obj, vnum));
				} else if (Pattern.matches(regexO, line)) {
					// Objectの始まりだった場合
					// System.out.println("オブジェクトの始まりです");
					Matcher m = Pattern.compile(regexO).matcher(line);
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

		} catch (IOException e) {
			// クラッシュレポートです
			e.printStackTrace();
			CrashReport c = CrashReport.makeCrashReport(e, "No model found.");
			c.makeCategory("Model Loading");
			Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(c); // クラレポ表示
			Minecraft.getMinecraft().displayCrashReport(c);
		} catch (MQOException e) {
			// クラッシュレポートです
			e.printStackTrace();
			CrashReport c = CrashReport.makeCrashReport(e, "MQO format error");
			c.makeCategory("Model Loading");
			Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(c); // クラレポ表示
			Minecraft.getMinecraft().displayCrashReport(c);
		}
	}

	@Nullable
	public MQOObject getObject(String name) {
		return object.get(name);
	}

	/**
	 * 各々のオブジェクトに存在する面に対してdrawFace()を呼び出すだけのラッピングメソッドです。
	 */
	public void draw() {
		// ラッピング処理
		for (MQOObject obj : object.values()) {
			for (MQOFace face : obj.getFaces()) {
				face.drawFace();
			}
		}
	}

	public class MQOException extends RuntimeException {
		// 例外処理

		public MQOException(String mes) {
			super(mes);
		}
	}
}