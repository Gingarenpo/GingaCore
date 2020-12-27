package jp.gingarenpo.api.helper;

/**
 * 数学的な計算や、算出を必要とする場合のお役立ちメソッド一覧となります。基本的にMinecraftの要素は使用しないので、
 * どのメソッドも独立して使用することができます。
 *
 * なお開発者が数学に疎いので回りくどいことばかりしていますが気になさらずに。
 *
 * @since 2020-12-28
 * @version 2.3～
 */
public class GMathHelper {

	private GMathHelper() {
		// コンストラクタによるインスタンスの生成は禁止します
	}

	/**
	 * 2つの数値の絶対値を加算した結果を返します。ようは、2点間の距離を求めるメソッドです。
	 * @param a 一つ目の値。
	 * @param b 二つ目の値。
	 * @return 2つの値の距離。
	 */
	public static double distance(double a, double b) {
		return Math.abs(a) + Math.abs(b);
	}
}
