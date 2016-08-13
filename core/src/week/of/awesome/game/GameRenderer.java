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
	
	private static final float DIALOG_LINEHEIGHT = 60f;
	
	public static final Color GREEN = new Color(170/255f, 212/255f, 0, 1);
	public static final Color BLUE = new Color(0, 102/255f, 255/255f, 1);
	public static final Color RED = new Color(255/255f, 0, 0, 1);
	
	private RenderService gfx;
	
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
	
	private BounceTween blobHeightTween = new BounceTween(2.2f);
	private BounceTween pickupTween = new BounceTween(1f);
	private BounceTween indicatorTween = new BounceTween(1f);
	
	public GameRenderer(GraphicsResources gfxResources, RenderService gfx) {
		this.gfx = gfx;
		
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
		int yOffset = 100;
		
		float bgBottomY = yOffset;
		float textStartY = yOffset;
		if (atTop) {
			bgBottomY = gfx.getHeight() - bgBottomY - dialogHeight;
			textStartY = gfx.getHeight() - textStartY - dialogHeight;
		}
		
		float fudge = 10f; // a bit of upward offset to counter-act the fact that lineheight == fontsize + fudge*2
		float padding = 25;
		float bgMaxAlpha = 0.5f;
		float bgAlpha = dialog.isFadingOut() ? Interpolation.linear.apply(0, bgMaxAlpha, dialog.getFade()) : bgMaxAlpha;
		gfx.draw(dialogBgTex, new Vector2(0, bgBottomY + fudge - padding), gfx.getWidth(), dialogHeight + padding*2, bgAlpha);
		
		float x = xOffset;
		float y = textStartY + dialogHeight;
		boolean indentX = false;
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
				
				String str = words[w] + (w < words.length-1 ? " " : "");
				
				x += gfx.drawFont(dialogFont, str, x, y, alpha).width;
			}
			
			if (dialog.isLineBreak(i)) {
				indentX = !indentX;
				y -= DIALOG_LINEHEIGHT;
				x  = xOffset + (indentX ? 40f : 0f);
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
		boolean dialogAtTop = world.getActiveBlobPosition().y < world.getMapHeight() / 2f;
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
