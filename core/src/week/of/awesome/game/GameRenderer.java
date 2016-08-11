package week.of.awesome.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.RenderService;
import week.of.awesome.game.Blob.Kind;

public class GameRenderer {
	private static final float TILE_SIZE = 32f;
	private static final float BOUNCE_AMOUNT = 0.2f;
	
	private RenderService gfx;
	
	private Texture fadeTex;
	
	private Texture blobTex;
	private Texture blobOverlayTex;
	private Texture activeIndicatorTex;
	
	private Texture blueGeneTex;
	private Texture starTex;
	
	private Texture floorTex;
	private Texture waterTex;
	
	private Color green = new Color(170/255f, 212/255f, 0, 1);
	private Color blue = new Color(0, 102/255f, 255/255f, 1);
	
	private BounceTween pickupTween = new BounceTween(1f);
	private BounceTween indicatorTween = new BounceTween(1f);
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
		fadeTex = gfxResources.newTexture("water.png");
		fadeTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		blobTex = gfxResources.newTexture("blob-base.png");
		blobOverlayTex = gfxResources.newTexture("blob-overlay.png");
		activeIndicatorTex = gfxResources.newTexture("indicator.png");
		
		blueGeneTex = gfxResources.newTexture("blue-gene.png");
		starTex = gfxResources.newTexture("star.png");
		
		floorTex = gfxResources.newTexture("floor.png");
		waterTex = gfxResources.newTexture("water.png");
	}
	
	public void drawFade(float amount) {
		gfx.setTransformMatrix(new Matrix4());
		gfx.draw(fadeTex, new Vector2(0, 0), gfx.getWidth(), gfx.getHeight(), amount);
	}
	
	public void draw(World world, float dt) {
		pickupTween.update(dt);
		indicatorTween.update(dt);
		
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
			gfx.drawCenteredTinted(blobTex, blob.getPosition(), 1f, 1f, false, kindToColour(blob.getKind()));
			gfx.drawCentered(blobOverlayTex, blob.getPosition(), 1f, 1f, false);
		}
		
		// draw active blob indicator
		gfx.drawCentered(activeIndicatorTex, world.getActiveBlobPosition().cpy().add(0, 0.5f + indicatorTween.interpolate(0.5f)), activeIndicatorTex.getWidth() / TILE_SIZE, activeIndicatorTex.getHeight() / TILE_SIZE, false);
	
	
		// draw stars
		for (GridPos starGridPos : world.getStars()) {
			drawSprite(starTex, new Vector2(starGridPos.x, starGridPos.y));
		}
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
			case WATER: return waterTex;
			case LIGHT: return floorTex;
			case LIGHT_SWITCH: return floorTex;
		}
		
		return null;
	}
	
	private Color kindToColour(Kind k) {
		switch (k) {
			case GREEN: return green;
			case BLUE: return blue;
		}
		
		throw new RuntimeException("Unmapped blob kind: " + k);
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
