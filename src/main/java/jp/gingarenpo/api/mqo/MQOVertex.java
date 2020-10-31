package jp.gingarenpo.api.mqo;

/**
 * 面の描画で使用する頂点情報を格納したクラスです。Vectorと同じだけど独自機能追加していくつもりなので一応…
 *
 * @author 銀河連邦
 */
public class MQOVertex {

	private final float x;
	private final float y;
	private final float z; // 以上、座標数値

	private final MQOObject mqo; // 親オブジェクト

	public MQOVertex(MQOObject mqo, String vnum) {
		// MQOの頂点記述方式に従って格納
		// 0.12345 0.23456 0.34567と3つの数値がスペースで区切られている
		// 正規化してあること前提での処理
		this.mqo = mqo; // 代入

		final String[] v = vnum.split(" "); // 分割して…
		//System.out.println("Vertex["+v.length+"]");
		if (v.length != 3) // MQOとして不適切
			throw mqo.getParent().new MQOException("Illegal Vertex Position!!");

		// 代入していく
		x = Float.parseFloat(v[0]);
		y = Float.parseFloat(v[1]);
		z = Float.parseFloat(v[2]);

		// 終わり
	}

	// ポジションは後からセットする必要がないのでreadonly

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	/**
	 * この頂点の属するMQOオブジェクトを返します。
	 *
	 * @return MQOオブジェクト
	 */
	public MQOObject getObject() {
		return mqo;
	}
}
