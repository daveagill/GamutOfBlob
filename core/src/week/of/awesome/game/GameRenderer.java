package week.of.awesome.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.RenderService;

public class GameRenderer {
	private RenderService gfx;
	
	private Texture blob;
	private Texture floor;
	
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
		blob = gfxResources.newTexture("blob.png");
		floor = gfxResources.newTexture("floor.png");
	}
	
	public void draw(World world) {
		Level level = world.getLevel();
		
		float mapLeft = gfx.getMidX() - (level.getWidth()-1) * World.TILE_SIZE / 2f;
		float mapBottom = gfx.getMidY() - (level.getHeight()-1) * World.TILE_SIZE / 2f;
		
		gfx.setTransformMatrix(new Matrix4().translate(mapLeft, mapBottom, 0));
		
		
		drawMap(level);
		
		gfx.drawCentered(blob, world.getBlobPosition(), World.TILE_SIZE, World.TILE_SIZE, false);
	}
	
	
	private void drawMap(Level level) {		
		for (int j = 0; j < level.getHeight(); ++j) {
			for (int i = 0; i < level.getWidth(); ++i) {
				float x = i * World.TILE_SIZE;
				float y = j * World.TILE_SIZE;
				
				Tile t = level.tileAt(i, j);
				if  (t != null) {
					gfx.drawCentered(tileToTex(t), new Vector2(x,y), World.TILE_SIZE, World.TILE_SIZE, false);
				}
			}
		}
	}
	
	private Texture tileToTex(Tile t) {
		switch (t) {
			case FLOOR: return floor;
			case GOAL: return floor;
			case BLUE_GATE: return floor;
			case BLUE_GENE: return floor;
			case LIGHT: return floor;
			case LIGHT_SWITCH: return floor;
		}
		
		return null;
	}
}
