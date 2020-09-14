package jp.gingarenpo.api.helper;

/**
 * ゲーム内の座標軸や、ゲーム内におけるブロックの位置などに関するお役立ちメソッドです。
 * もちろんインスタンスを生成することはできません。
 *
 * 1.7.10対応版のため、当Modは1.12.2と若干仕様が異なります。
 *
 * @author 銀河連邦
 *
 */
public class GPosHelper {

	private GPosHelper() {}; // コンストラクターは起動不可能

	/**
	 *	指定された二つの座標の距離を求めます。絶対値で返却されます。BlockPosの指定のため、座標精度は整数値ですが、
	 *	XYZを考慮した最短距離による距離を導き出します。やっていることは三平方の定理を2回使っているだけです。
	 *	辺経由の距離（所謂道のり）ではないのでご注意ください。
	 * @param p1 1つ目の座標。
	 * @param p2 2つ目の座標。
	 * @return 距離。
	 */
	public static double getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		// 3平方の定理を使用する（斜辺^2 = 他の辺の2乗の和）
		// まずそれぞれの長さを出す
		int x = Math.abs(x1 - x2);
		int y = Math.abs(y1 - y2);
		int z = Math.abs(z1 - z2);

		// 次に、XとZに対する直線距離を算出する
		double xz = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // 平面上の長さ

		// その出した座標に対して、Yと直線距離を算出する
		double xyz = Math.sqrt(Math.pow(xz, 2) + Math.pow(y, 2)); // これが距離

		return xyz;
	}

}
