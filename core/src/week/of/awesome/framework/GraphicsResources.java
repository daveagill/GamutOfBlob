package week.of.awesome.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

public class GraphicsResources implements Disposable {

	private Map<String, Texture> textureCache = new HashMap<>();
	private Map<String, BitmapFont> fontCache = new HashMap<>();
	
	public Texture newTexture(String path) {
		Texture t = textureCache.get(path);
		if (t == null) {
			t = new Texture(path);
			textureCache.put(path, t);
		}
		return t;
	}
	
	public BitmapFont newFont(String path) {
		BitmapFont f = fontCache.get(path);
		if (f == null) {
			f = new BitmapFont(Gdx.files.internal(path));
			fontCache.put(path, f);
		}
		return f;
	}

	
	@Override
	public void dispose() {
		textureCache.values().forEach(Disposable::dispose);
		fontCache.values().forEach(Disposable::dispose);
	}
}
