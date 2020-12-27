package jp.gingarenpo.api.helper;

import net.minecraft.util.math.BlockPos;

/**
 * ゲーム内の座標軸や、ゲーム内におけるブロックの位置などに関するお役立ちメソッドです。
 * もちろんインスタンスを生成することはできません。
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
	public static double getDistance(BlockPos p1, BlockPos p2) {
		// 3平方の定理を使用する（斜辺^2 = 他の辺の2乗の和）
		// まずそれぞれの長さを出す
		final int x = Math.abs(p1.getX() - p2.getX());
		final int y = Math.abs(p1.getY() - p2.getY());
		final int z = Math.abs(p1.getZ() - p2.getZ());

		// 次に、XとZに対する直線距離を算出する
		final double xz = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // 平面上の長さ

		// その出した座標に対して、Yと直線距離を算出する
		final double xyz = Math.sqrt(Math.pow(xz, 2) + Math.pow(y, 2)); // これが距離

		return xyz;
	}

	/**
	 * 指定された二つの座標の距離を求めます。こちらはBlockPosと座標値の2つを使用できます。
	 * @param p1 1つ目の座標。
	 * @param x2 2つ目の座標のX座標。
	 * @param y2 2つ目の座標のY座標。
	 * @param z2 2つ目の座標のZ座標。
	 * @return 距離。
	 */
	public static double getDistance(BlockPos p1, int x2, int y2, int z2) {
		return getDistance(p1, new BlockPos(x2, y2, z2));
	}

	/**
	 * 指定された二つの座標の距離を求めます。こちらはすべて座標値で指定します。パラメーターはもう自明なので省略。
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return 距離。
	 */
	public static double getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return getDistance(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
	}

	/**
	 * 指定された全数値において、すべてが一致していればtrueを返します。1つでも異なればfalseを返します。
	 * @param pos 調べたい数値。floatでいくらでも指定できます。
	 * @return 全てが一致していればtrue、1つでも異なればfalse
	 */
	public static boolean areSameNumber(double ...pos) {
		// 全ての数値が一致していたらtrue
		for (int i = 0; i < pos.length - 1; i++) {
			if (pos[i] != pos[i+1]) return false; // 1個でも違えば残念ながら不一致
		}
		return true;
	}

}
