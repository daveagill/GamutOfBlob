package week.of.awesome.framework;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class RenderService implements Disposable {
	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	
	private OrthographicCamera camera  = new OrthographicCamera();
	private int midX, midY;

	public RenderService() {
		gl = Gdx.gl;
		gl.glClearColor(0, 0, 0, 1);
	}
	
	public void resizeViewport(int width, int height) {
		camera.setToOrtho(false, width, height);
		camera.update();
		
		midX = width / 2;
		midY = height / 2;
	}
	
	public int getMidX() {
		return midX;
	}
	
	public int getMidY() {
		return midY;
	}
	
	public void beginFrame() {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
	}
	
	public void endFrame() {
		batch.end();
	}
	
	public void setTransformMatrix(Matrix4 m) {
		batch.setTransformMatrix(m);
	}
	
	public void drawCentered(Texture t, Vector2 pos, float width, float height, boolean flipX) {
		float actualWidth = flipX ? -width : width;
		batch.draw(t, pos.x - actualWidth/2, pos.y - height/2, actualWidth, height);
	}
	
	public void drawCenteredTinted(Texture t, Vector2 pos, float width, float height, boolean flipX, Color colour) {
		float actualWidth = flipX ? -width : width;
		batch.setColor(colour);
		batch.draw(t, pos.x - actualWidth/2, pos.y - height/2, actualWidth, height);
		batch.setColor(Color.WHITE);
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}
