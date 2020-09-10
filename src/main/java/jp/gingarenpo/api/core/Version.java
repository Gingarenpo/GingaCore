package jp.gingarenpo.api.core;

/**
 * バージョン管理する際にどれが大きいのか小さいのか判断するクラス（インスタンスが必要です）
 * @author 銀河連邦
 *
 */
public class Version {

	private int[] version; // バージョン番号左から順番に
	public String root; // そのまま返すためのもの

	/**
	 * 1.2.3.4みたいな感じでバージョンを入力
	 * @param version
	 */
	public Version(String version) {
		// 普通に分解しようか
		this.version = new int[version.split("\\.").length];
		for (int i = 0; i < this.version.length; i++) {
			this.version[i] = Integer.valueOf(version.split("\\.")[i].trim());
		}
		this.root = version;
	}

	/**
	 * このバージョンが指定したバージョンよりも新しければtrue、古いか同じならばfalseを返す
	 * @param other
	 * @return
	 */
	public boolean isLatestThan(Version other) {
		// 多いほうに合わせるので
		int count = (this.version.length > other.version.length) ? this.version.length : other.version.length;

		for (int i = 0; i < count; i++) {
			// 左から順に比較していく。足りない分は0とする
			int me = (this.version.length <= i) ? 0 : this.version[i];
			int you = (other.version.length <= i) ? 0 : other.version[i]; // こうなるね


			if (me > you) return true;
		}
		return false;
	}

	public boolean equals(Object others) {
		// 同じかどうか
		if (!(others instanceof Version)) {
			return false; // そもそもインスタンスが違う
		}

		Version other = (Version) others; // 安心してダウンキャストできる


		int count = (this.version.length > other.version.length) ? this.version.length : other.version.length;

		for (int i = 0; i < count; i++) {
			// 左から順に比較していく。足りない分は0とする
			int me = (this.version.length <= i) ? 0 : this.version[i];
			int you = (other.version.length <= i) ? 0 : other.version[i]; // こうなるね


			if (me != you) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return root;
	}
}
