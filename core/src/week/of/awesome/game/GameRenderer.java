package week.of.awesome.game;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.RenderService;
import week.of.awesome.game.Blob.Kind;

public class GameRenderer {
	private static final float OPAQUE = 1f;
	private static final float SHADOW = 0.8f;
	
	
	private static final float TILE_SIZE = 32f;
	private static final float BOUNCE_AMOUNT = 0.2f;
	private static final float BLOB_PULSATE_AMOUNT = 0.15f;
	
	private static final float TITLE_SLIDE_IN_HEIGHT = 15f;
	private static final float TITLE_ENTRANCE_SPEED = 1f;
	
	private static final float DIALOG_LINEHEIGHT = 60f;
	
	public static final Color GREEN = new Color(170/255f, 212/255f, 0, 1);
	public static final Color BLUE = new Color(0, 102/255f, 255/255f, 1);
	public static final Color RED = new Color(255/255f, 0, 0, 1);
	
	private RenderService gfx;
	
	private float titleFade = 0f;
	
	private Texture mainBgTex;
	
	private Texture fadeTex;
	
	private Texture blobTex;
	private Texture blobOverlayTex;
	private Texture activeIndicatorTex;
	
	private Texture geneTex;
	private Texture starTex;
	
	private Texture shadowTex;
	private Texture lightButtonTex;
	
	private Texture floorTex;
	private Texture waterTex;
	private Texture lavaTex;
	
	private BitmapFont dialogFont;
	private Texture dialogBgTex;
	
	private BitmapFont levelTitleFont;
	
	private BounceTween blobHeightTween = new BounceTween(2.2f);
	private BounceTween pickupTween = new BounceTween(1f);
	private BounceTween indicatorTween = new BounceTween(1f);
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
		mainBgTex = newTexture(gfxResources, "mainbg.png");
		mainBgTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		fadeTex = newTexture(gfxResources, "whiteFade.png");
		
		blobTex = newTexture(gfxResources, "blob-base.png");
		blobOverlayTex = newTexture(gfxResources, "blob-overlay.png");
		activeIndicatorTex = newTexture(gfxResources, "indicator.png");
		
		geneTex = newTexture(gfxResources, "gene.png");
		starTex = newTexture(gfxResources, "star.png");
		
		shadowTex = newTexture(gfxResources, "shadow.png");
		lightButtonTex = newTexture(gfxResources, "lightButton.png");
		
		floorTex = newTexture(gfxResources, "floor.png");
		waterTex = newTexture(gfxResources, "water.png");
		lavaTex = newTexture(gfxResources, "lava.png");
		
		dialogFont = newFont(gfxResources, "trench.fnt");
		dialogBgTex = newTexture(gfxResources, "dialogbg.png");
		
		levelTitleFont = newFont(gfxResources, "montserrat.fnt");
	}
	
	public void drawDialog(DialogState dialog, boolean atTop) {
		//drawSprite(lavaTex, new Vector2(dialog.config.trigger.x, dialog.config.trigger.y));
		
		if (!dialog.active()) { return; }
		gfx.setTransformMatrix(new Matrix4());
		
		// calculate height
		float dialogHeight = DIALOG_LINEHEIGHT;
		for (int i = 0; i < dialog.size(); ++i) {
			dialogHeight += dialog.isLineBreak(i) ? DIALOG_LINEHEIGHT : 0f;
		}
		
		int xOffset = 100;
		int yOffset = 0;
		float fudge = 10f; // a bit of upward offset to counter-act the fact that lineheight == fontsize + fudge*2
		float padding = 25;
		
		float bgBottomY = yOffset;
		float textStartY = yOffset;
		if (atTop) {
			bgBottomY = gfx.getHeight() - bgBottomY - dialogHeight - padding - fudge;
			textStartY = gfx.getHeight() - textStartY - dialogHeight - padding - fudge;
		}
		

		float bgMaxAlpha = 0.5f;
		float bgAlpha = dialog.isFadingOut() ? Interpolation.linear.apply(0, bgMaxAlpha, dialog.getFade()) : bgMaxAlpha;
		gfx.draw(dialogBgTex, new Vector2(0, bgBottomY + fudge - padding), gfx.getWidth(), dialogHeight + padding*2, bgAlpha);
		
		float x = xOffset;
		float y = textStartY + dialogHeight;
		for (int i = 0; i <= dialog.currentTextIdx(); ++i) {
			
			String text = dialog.getText(i);
			String[] words = text.split(" ");
			
			for (int w = 0; w < words.length; ++w) {
				float initialFadeValue = -w * 1f;
				float alpha = 1f;
				if (dialog.isFadingOut()) {
					alpha = dialog.getFade();
				}
				else if (i == dialog.currentTextIdx()) {
					alpha = Math.max(0f, Interpolation.linear.apply(initialFadeValue, 1f, dialog.getFade()));
				}
				
				String str = words[w] + " ";
				
				x += gfx.drawFont(dialogFont, str, x, y, alpha).width;
			}
			
			if (dialog.isLineBreak(i)) {
				y -= DIALOG_LINEHEIGHT;
				float indent = 0;
				if (i < dialog.currentTextIdx()) {
					indent = dialog.getIndentation(i+1) * 40f;
				}
				x = xOffset + indent;
			}
		}
	}
	
	public void drawFade(float amount) {
		gfx.setTransformMatrix(new Matrix4());
		gfx.draw(fadeTex, new Vector2(0, 0), gfx.getWidth(), gfx.getHeight(), amount);
	}
	
	public void draw(World world, float dt) {
		blobHeightTween.update(dt);
		pickupTween.update(dt);
		indicatorTween.update(dt);
		
		float mapLeft = gfx.getMidX() - (world.getMapWidth()-1) * TILE_SIZE / 2f;
		float mapBottom = gfx.getMidY() - (world.getMapHeight()-1) * TILE_SIZE / 2f;
		
		// move the map to follow the blob
		if (world.isGameplayStarted()) {
			float threshold = 100;
			float leftThreshold = threshold;
			float bottomThreshold = threshold;
			float rightThreshold = gfx.getWidth() - threshold;
			float topThreshold = gfx.getHeight() - threshold;
			Vector2 blobScreenSpacePos = world.getActiveBlobPosition().scl(TILE_SIZE).add(mapLeft, mapBottom);
			if (blobScreenSpacePos.x < leftThreshold) {
				mapLeft += leftThreshold - blobScreenSpacePos.x;
			}
			else if (blobScreenSpacePos.x > rightThreshold) {
				mapLeft -= blobScreenSpacePos.x - rightThreshold;
			}
			if (blobScreenSpacePos.y < bottomThreshold) {
				mapBottom += bottomThreshold - blobScreenSpacePos.y;
			}
			else if (blobScreenSpacePos.y > topThreshold) {
				mapBottom -= blobScreenSpacePos.y - topThreshold;
			}
		}
		
		
		gfx.setTransformMatrix(new Matrix4());
		gfx.drawScreen(mainBgTex, 1f);
		
		// draw title text
		titleFade = Math.min(1f, titleFade + dt * TITLE_ENTRANCE_SPEED);
		Vector2 titlePos = new Vector2(world.getMapNamePosition().x, world.getMapNamePosition().y);
		titlePos.scl(TILE_SIZE).add(mapLeft, mapBottom).sub(TILE_SIZE/3f, 0);
		titlePos.y += Interpolation.linear.apply(TITLE_SLIDE_IN_HEIGHT, 0, titleFade);
		gfx.drawFont(levelTitleFont, world.getMapName(), titlePos.x, titlePos.y, titleFade);

		gfx.setTransformMatrix(new Matrix4().translate(mapLeft, mapBottom, 0).scale(TILE_SIZE, TILE_SIZE, 1f));
		
		drawMap(world);
		
		// draw genes
		for (GridPos geneGridPos : world.getBlueGenes()) {
			drawTintedSprite(geneTex, new Vector2(geneGridPos.x, geneGridPos.y + pickupTween.interpolate(BOUNCE_AMOUNT)), BLUE);
		}
		for (GridPos geneGridPos : world.getRedGenes()) {
			drawTintedSprite(geneTex, new Vector2(geneGridPos.x, geneGridPos.y + pickupTween.interpolate(BOUNCE_AMOUNT)), RED);
		}
		
		// draw light switches
		for (ShadowAndButtonState shadowAndButtons : world.getShadowsAndButtons()) {
			for (GridPos buttonPos : shadowAndButtons.getButtons()) {
				drawSprite(lightButtonTex, new Vector2(buttonPos.x, buttonPos.y));
			}
		}
		
		// draw blobs
		int blobIdx = 0;
		for (Blob blob : world.getBlobs()) {
			++blobIdx;
			float heightDelta = blobHeightTween.interpolate(BLOB_PULSATE_AMOUNT, blobIdx * 0.3f);
			Vector2 position = blob.getPosition().add(0, heightDelta/2);
			drawTintedSprite(blobTex, position, 1f, 1f+heightDelta, kindToColour(blob.getKind()));
			drawSprite(blobOverlayTex, position, 1f, 1f+heightDelta);
		}
		
		// draw active blob indicator
		gfx.drawCentered(activeIndicatorTex, world.getActiveBlobPosition().cpy().add(0, 0.5f + indicatorTween.interpolate(0.5f)), activeIndicatorTex.getWidth() / TILE_SIZE, activeIndicatorTex.getHeight() / TILE_SIZE, false);
	
	
		// draw stars
		for (GridPos starGridPos : world.getStars()) {
			drawSprite(starTex, new Vector2(starGridPos.x, starGridPos.y));
		}
		
		// draw shadows
		for (ShadowAndButtonState shadowAndButtons : world.getShadowsAndButtons()) {
			if (!shadowAndButtons.isLightOn()) {
				for (GridPos shadowPos : shadowAndButtons.getShadows()) {
					drawGridCell(shadowTex, shadowPos, SHADOW);
				}
			}
		}
		
		// draw dialog
		boolean dialogAtTop = false;//world.getActiveBlobPosition().y < world.getMapHeight() / 2f;
		for (DialogState dialog : world.getDialogs()) {
			drawDialog(dialog, dialogAtTop);
		}
	}
	
	
	private void drawMap(World world) {
		for (int j = world.getMapHeight(); j >= 0 ; --j) {
			for (int i = 0; i < world.getMapWidth(); ++i) {		
				GridPos pos = new GridPos();
				pos.x = i;
				pos.y = j;
				
				Tile t = world.tileAt(pos);				
				if  (t != null) {
					drawGridCell(tileToTex(t), pos, OPAQUE);
				}
			}
		}
	}
	
	private Texture tileToTex(Tile t) {
		switch (t) {
			case FLOOR: return floorTex;
			case WATER: return waterTex;
			case LAVA: return lavaTex;
		}
		
		return null;
	}
	
	private Color kindToColour(Kind k) {
		switch (k) {
			case GREEN: return GREEN;
			case BLUE: return BLUE;
			case RED: return RED;
		}
		
		throw new RuntimeException("Unmapped blob kind: " + k);
	}
	
	private void drawGridCell(Texture tex, GridPos pos, float alpha) {
		gfx.drawCenteredTinted(tex, new Vector2(pos.x, pos.y), 1f, 1f, false, new Color(1f, 1f, 1f, alpha));
	}
	
	private void drawSprite(Texture tex, Vector2 pos, float width, float height) {
		drawTintedSprite(tex, pos, width, height, Color.WHITE);
	}
	
	private void drawSprite(Texture tex, Vector2 pos) {
		drawSprite(tex, pos, 1f, 1f);
	}
	
	private void drawTintedSprite(Texture tex, Vector2 pos, float width, float height, Color colour) {
		if (tex.getWidth() > tex.getHeight()) {
			float ratio = ((float)tex.getHeight()) / tex.getWidth();
			gfx.drawCenteredTinted(tex, pos, width, ratio * height, false, colour);
		}
		else {
			float ratio = ((float)tex.getWidth()) / tex.getHeight();
			gfx.drawCenteredTinted(tex, pos, ratio * width, height, false, colour);
		}
	}
	
	private void drawTintedSprite(Texture tex, Vector2 pos, Color colour) {
		drawTintedSprite(tex, pos, 1f, 1f, colour);
	}
	
	private static Texture newTexture(GraphicsResources gfxResources, String filename) {
		return gfxResources.newTexture("sprites/" + filename);
	}
	
	private static BitmapFont newFont(GraphicsResources gfxResources, String filename) {
		return gfxResources.newFont("fonts/" + filename);
	}
}
