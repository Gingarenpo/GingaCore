package jp.gingarenpo.old;

/**
 * ※2.0以降使わなくなりましたが互換性の為に残しています
 *
 * @author 銀河連邦
 */
public class ColorInt {

	// 16色の固定色
	public static final int BLACK = 0;
	public static final int RED = 0xff0000;
	public static final int BLUE = 0x0000ff;
	public static final int GREEN = 0x00ff00;
	public static final int YELLOW = 0xffff00;
	public static final int PARPLE = 0xff00ff;
	public static final int WATER = 0x00ffff;
	public static final int WHITE = 0xffffff;

	// メソッド
	/**
	 * RGB値により整数に変換した色を返します。
	 *
	 * @param r
	 *            R要素
	 * @param g
	 *            G要素
	 * @param b
	 *            B要素
	 * @return 0～16777215までのどれかの数値。
	 */
	public static int getColor(int r, int g, int b) {
		return r * 65536 + g * 256 + b; // 整数化したもの
	}

	public static int[] getRGB(int i) {
		// 256で割ってみる
		int r, g, b;
		if (i / 65536 >= 255) {
			r = 255;
		} else {
			// 商がR
			r = i / 65536;
		}
		i -= r * 65536;
		if (i / 256 >= 255) {
			g = 255;
		} else {
			g = i / 256;
		}
		i -= g * 256;
		b = i;

		return new int[] { r, g, b };
	}
}
