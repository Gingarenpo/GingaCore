package jp.gingarenpo.method;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * お役立ちメソッドを集めたものです。staticなので、関数のように使うことを前提としています。
 *
 * @author 銀河連邦
 */
public final class Method {
	// インスタンス生成不可能、全部staticでアクセスすること
	
	/**
	 * 指定したNBTタグから、指定したキーをもとに整数値を返します。キーが存在しない場合はデフォルトの値としてvalueを返します。
	 *
	 * @param compound
	 *            NBTタグ。
	 * @param key
	 *            キー。
	 * @param value
	 *            存在しないときに返すデフォルトの値。
	 * @return
	 */
	public static int getIntWithValue(NBTTagCompound compound, String key, int value) {
		// 規定値を設定した版（互換性維持のため）
		if (compound.hasKey(key))
			return compound.getInteger(key);
		else return value;
	}
	
	/**
	 * 指定したNBTタグから、指定したキーをもとに論理値を返します。キーが存在しない場合はデフォルトの値としてvalueを返します。
	 *
	 * @param compound
	 *            NBTタグ。
	 * @param key
	 *            キー。
	 * @param value
	 *            存在しないときに返すデフォルトの値。
	 * @return
	 */
	public static boolean getBooleanWithValue(NBTTagCompound compound, String key, boolean value) {
		// 規定値を設定した版（互換性維持のため）
		if (compound.hasKey(key))
			return compound.getBoolean(key);
		else return value;
	}
	
	/**
	 * 指定したNBTタグから、指定したキーをもとに文字列値を返します。キーが存在しない場合はデフォルトの値としてvalueを返します。
	 *
	 * @param compound
	 *            NBTタグ。
	 * @param key
	 *            キー。
	 * @param value
	 *            存在しないときに返すデフォルトの値。
	 * @return
	 */
	public static String getStringWithValue(NBTTagCompound compound, String key, String value) {
		// 規定値を設定した版（互換性維持のため）
		if (compound.hasKey(key))
			return compound.getString(key);
		else return value;
	}
	
	/**
	 * 指定したNBTタグから、指定したキーをもとに整数配列を返します。キーが存在しない場合はデフォルトの値としてvalueを返します。
	 *
	 * @param compound
	 *            NBTタグ。
	 * @param key
	 *            キー。
	 * @param value
	 *            存在しないときに返すデフォルトの値。
	 * @return
	 */
	public static int[] getIntArrayWithValue(NBTTagCompound compound, String key, int[] value) {
		// 規定値を設定した版（互換性維持のため）
		if (compound.hasKey(key))
			return compound.getIntArray(key);
		else return value;
	}
	
	/**
	 *	二つの座標について、XYZを考慮した直線距離を算出します。ブロック1マスを1とした実数で返却します。
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double getDistance(BlockPos p1, BlockPos p2) {
		// 3平方の定理を使用する（斜辺^2 = 他の辺の2乗の和）
		// まずそれぞれの長さを出す
		int x = Math.abs(p1.getX() - p2.getX());
		int y = Math.abs(p1.getY() - p2.getY());
		int z = Math.abs(p1.getZ() - p2.getZ());
		
		// 次に、XとZに対する直線距離を算出する
		double xz = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // 平面上の長さ
		
		// その出した座標に対して、Yと直線距離を算出する
		double xyz = Math.sqrt(Math.pow(xz, 2) + Math.pow(y, 2)); // これが距離
		
		return xyz;
	}
	
	/**
	 * 二つの座標を指定しますが、片方をXYZの3つの座標で表したものとなります。所謂オーバーロード。
	 * @param p1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public static double getDistance(BlockPos p1, double x2, double y2, double z2) {
		return getDistance(p1, new BlockPos(x2, y2, z2));
	}
	
	/**
	 * 二つの座標の距離を求めますが、すべてXYZによる座標で算出します。
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return getDistance(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
	}
}
