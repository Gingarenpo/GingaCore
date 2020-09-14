package jp.gingarenpo.api.mqo;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

/**
 * MQOの面を格納するクラスです。この中にはさらに頂点も格納しているため、このクラスからOpenGL描画を行うこともできます
 *
 * @author 銀河連邦
 */
public class MQOFace {

	private MQOObject mqo; // 親オブジェクト
	private int[] v; // 頂点番号を格納（固定なのでプリミティブ配列で）
	private ArrayList<float[]> uv = new ArrayList<float[]>(); // 頂点対応のUV座標を格納

	/**
	 * 指定した親オブジェクト内に存在する面として、新規に面オブジェクトを作成します。面オブジェクトは三角形か四角形
	 * じゃないとめんどくさいことになります。チェックしないので自力で確認してください。
	 *
	 * @param mqo 親元となるMQOオブジェクト。
	 * @param vnum 頂点が記されている対応番号を記したもの。1 3 5とかそんな感じ
	 * @param uvnum それぞれの頂点に対応したUVを記録する。必ずvnumの倍の引数を持つ
	 */
	public MQOFace(MQOObject mqo, String vnum, String uvnum) {
		this.mqo = mqo; // 親オブジェクトを代入
		String[] vs = vnum.split(" "); // 空白で
		String[] uvs = uvnum.split(" "); // これも
		// System.out.println("obj("+mqo.getName()+") : v["+vs.length+"] uv["+uvs.length+"]");

		if (vs.length * 2 != uvs.length) // 座標数が一致しない
			throw mqo.getParent().new MQOException("Illegal UV or Vertex parameter!!");

		// これを代入していく
		v = new int[vs.length]; // 指定した数で初期化
		for (int i = 0; i < vs.length; i++) {
			v[i] = Integer.parseInt(vs[i]); // 頂点番号を代入
			uv.add(i, new float[] {Float.parseFloat(uvs[i*2]), Float.parseFloat(uvs[i*2+1])}); // UVをね
		}
	}

	/**
	 * この面を実際に描画します。直接OpenGLを使用するので、レンダーメソッドで呼び出す必要があります。そうでない場合の
	 * チェックは一切しないのでご了承ください。
	 */
	public void drawFace() {
		// 実際にこの面を描きます
		GL11.glBegin((v.length == 3) ? GL11.GL_TRIANGLES : GL11.GL_QUADS); // 三角形か四角形か
		for (int i = 0;  i < v.length; i++) {
			GL11.glTexCoord2f(uv.get(i)[0], uv.get(i)[1]); // テクスチャUV座標を指定（Vは反転する！！）
			GL11.glVertex3f(mqo.getVertexs().get(v[i]).getX(),
				mqo.getVertexs().get(v[i]).getY(), mqo.getVertexs().get(v[i]).getZ()); // 頂点を指定
		}

		GL11.glEnd(); // 終了
	}
}
