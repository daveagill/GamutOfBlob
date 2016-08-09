package week.of.awesome.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.RenderService;

public class GameRenderer {
	
	private static final float TILE_SIZE = 16f;

	private RenderService gfx;
	
	private Texture img;
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
		img = gfxResources.newTexture("floor.png");
	}
	
	public void draw(Level level) {
		
		float mapLeft = gfx.getMidX() - (level.getWidth()-1) * TILE_SIZE / 2f;
		float mapBottom = gfx.getMidY() - (level.getHeight()-1) * TILE_SIZE / 2f;
		
		gfx.setTransformMatrix(new Matrix4().translate(mapLeft, mapBottom, 0));
		
		for (int j = 0; j < level.getHeight(); ++j) {
			for (int i = 0; i < level.getWidth(); ++i) {
				float x = i * TILE_SIZE;
				float y = j * TILE_SIZE;
				
				Tile t = level.tileAt(i, j);
				if  (t == Tile.FLOOR || t == Tile.GOAL) {
					gfx.drawCentered(img, new Vector2(x,y), TILE_SIZE, TILE_SIZE, false);
				}
			}
		}
	}
}
