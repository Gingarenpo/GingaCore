package jp.gingarenpo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.gingarenpo.api.helper.GFileHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * <p>拙作Mod内で使用するヘルパーメソッドならびにお役立ちメソッドが並んでいます。
 * このMod内のメソッドはコモンメソッドとして誰でも使用することができます。使用する場合は、
 * 当ModのJarファイルをEclipseならば「クラスパス」として追加してください。開発者として使用することが大半だろうと
 * 思いますので、その際はソースJarをダウンロードしないとここが見えないと思います。</p>
 *
 * <p>当Modを開発者以外の方が使用する場合、ソースコード内に埋め込むのは禁止とします。使用する場合は、この
 * Modが配布されているURL（ここでは記しません）を伝え、ダウンロードしてもらうように指示してください。</p>
 *
 * <p>1.0世代と比べて、<b>下位互換性のない</b>変更が行われています（MQO関連をパッケージごと移動させました）。
 * 使用される場合はご注意ください。なるべくパッケージはわかりやすく分けていますが、細かいことはそこまで
 * 記さない（わかっていること前提）なのでご注意ください。JavaDocは作成するつもりありません。</p>
 *
 * <p>簡易的な使用方法だけ載せているのでそれをご利用ください。</p>
 *
 * <ul>
 * 	<li>methodパッケージ内の内容は下位互換性を保つために残していますが、apiパッケージにあった「ColorInt」クラスに
 * 関しては、中身は消していませんが場所が変わっています。</li>
 * 	<li>mqoパッケージは「jp.gingarenpo.mqo」から「jp.gingarenpo.<b>api</b>.mqo」に移動しています。</li>
 * 	<li>基本的にすべて「api」パッケージに入っています。重複を防ぐためなのでご了承ください。（というか今まで
 * 全く重複に関して適当にごまかしていたのがバレる）</li>
 * </ul>
 *
 * @author 銀河連邦
 * @since 2020-09-06（Ver2.0）
 * @version 2.0
 */

@Mod(modid = GingaCore.MODID, name = "GingaCore", version = GingaCore.version)
@EventBusSubscriber
public class GingaCore {


	public static Logger log;
	public static final int majorVersion = 2; // バージョン番号の上位
	public static final int minorVersion = 0; // 下位（全部格納するため小数点にしていますが3つ以上増やしません）
	public static final String version = majorVersion + "." + minorVersion; // 起動時に実行するバージョンの取得
	public static final String MODID = "gingacore";

	// インスタンス生成してすぐ
	@EventHandler
	public void preInit(FMLConstructionEvent event) {
	}

	// おおもとの準備（ほぼここ）
	@EventHandler
	public void init(FMLPreInitializationEvent event) {
		log = LogManager.getLogger("GingaCore-"+GingaCore.version); // ログ
		GFileHelper.isUpdateOnServer(); // バージョンチェック
	}

	// 準備できたものを処理する
	@EventHandler
	public void prepare(FMLInitializationEvent event) {
	}

	// 最終実行準備
	@EventHandler
	public void ready(FMLPostInitializationEvent event) {
		// レシピ作成
	}

}
