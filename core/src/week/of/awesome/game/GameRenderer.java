package week.of.awesome.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.RenderService;

public class GameRenderer {
	private static final float TILE_SIZE = 32f;
	private static final float BOUNCE_AMOUNT = 0.2f;
	
	private RenderService gfx;
	
	private Texture blobTex;
	private Texture blueGeneTex;
	private Texture floorTex;
	
	private BounceTween pickupTween = new BounceTween();
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
		blobTex = gfxResources.newTexture("blob.png");
		blueGeneTex = gfxResources.newTexture("blue-gene.png");
		floorTex = gfxResources.newTexture("floor.png");
	}
	
	public void draw(World world, float dt) {
		pickupTween.update(dt);
		
		float mapLeft = gfx.getMidX() - (world.getMapWidth()-1) * TILE_SIZE / 2f;
		float mapBottom = gfx.getMidY() - (world.getMapHeight()-1) * TILE_SIZE / 2f;
		
		gfx.setTransformMatrix(new Matrix4().translate(mapLeft, mapBottom, 0).scale(TILE_SIZE, TILE_SIZE, 1f));
		
		
		drawMap(world);
		
		// draw genes
		for (GridPos geneGridPos : world.getBlueGenes()) {
			drawSprite(blueGeneTex, new Vector2(geneGridPos.x, geneGridPos.y + pickupTween.interpolate(BOUNCE_AMOUNT)));
		}
		
		// draw blobs
		for (Blob blob : world.getBlobs()) {
			drawSprite(blobTex, blob.getPosition());
		}
		//drawSprite(blob, world.getActiveBlobPosition());
	}
	
	
	private void drawMap(World world) {		
		for (int j = world.getMapHeight(); j >= 0 ; --j) {
			for (int i = 0; i < world.getMapWidth(); ++i) {
				float x = i;
				float y = j;
				
				Tile t = world.tileAt(i, j);				
				if  (t != null) {
					drawSprite(tileToTex(t), new Vector2(x,y));
				}
			}
		}
	}
	
	private Texture tileToTex(Tile t) {
		switch (t) {
			case FLOOR: return floorTex;
			case GOAL: return floorTex;
			case BLUE_GATE: return floorTex;
			case LIGHT: return floorTex;
			case LIGHT_SWITCH: return floorTex;
		}
		
		return null;
	}
	
	private void drawSprite(Texture tex, Vector2 pos) {
		if (tex.getWidth() > tex.getHeight()) {
			float ratio = ((float)tex.getHeight()) / tex.getWidth();
			gfx.drawCentered(tex, pos, 1f, ratio, false);
		}
		else {
			float ratio = ((float)tex.getWidth()) / tex.getHeight();
			gfx.drawCentered(tex, pos, ratio, 1f, false);
		}
	}
}
