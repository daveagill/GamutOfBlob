package week.of.awesome.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.Services;

public class FinalStageState implements GameState {
	
	private static final float SPAWN_DELAY = 0.1f;
	private static final float GLOW_SPEED = 0.05f;
	private static final float MEGA_BLOB_WALK_SPEED = 50f;
	private static final float MEGA_BLOB_WALK_AMOUNT_BEFORE_FADE = 300;
	private static final float FADE_TO_WHITE_SPEED = 0.5f;
	private static final float FADE_FROM_WHITE_SPEED = 0.25f;
	private static final float FADE_TO_BLACK_SPEED = 0.5f;
	private static final float TEXT_FADE_SPEED = 2f;
	
	private static final String TOP_TEXT = "Go...";
	private static final String BOTTOM_TEXT = "... be free!";
	private static final String GAME_OVER_TEXT = "Game Over";
	
	private Random rand = new Random(TimeUtils.millis());
	
	private GameState restartGameState;
	
	private Services services;
	
	private Texture megaBlobTex;
	private Texture blobTex;
	private Texture blobOverlayTex;
	private Texture glowTex;
	private Texture whiteFadeTex;
	private Texture floorTex;	
	private Texture endGameFadeTex;
	
	private BitmapFont font;
	
	private List<Vector2> blobPositions;
	private List<Float> tweenValues;
	
	private float spawnTimer;
	
	private float glowFade;
	private BounceTween glowBounce;
	
	private boolean isRightPressed;
	private float megaBlobX;
	private BounceTween megaBlobHeightTween;
	private float topTextFade;
	private float bottomTextFade;
	
	private float blackFade;
	private float whiteFade;
	
	private boolean scene1;
	private boolean scene2;
	private boolean gameover;
	private boolean pressedAnyKey;
	
	public void setRestartGameState(GameState restartGameState) {
		this.restartGameState = restartGameState;
	}

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		blobTex = newTexture(services.gfxResources, "blob-base.png");
		blobOverlayTex = newTexture(services.gfxResources, "blob-overlay.png");
		
		megaBlobTex = newTexture(services.gfxResources, "megaBlob.png");
		
		glowTex = newTexture(services.gfxResources, "glow.png");
		
		whiteFadeTex = newTexture(services.gfxResources, "whiteFade.png");
		
		floorTex = newTexture(services.gfxResources, "floor.png");
		
		endGameFadeTex = newTexture(services.gfxResources, "endGameFade.png");
		
		font = newFont(services.gfxResources, "trench.fnt");
		
		blobPositions = new ArrayList<>();
		tweenValues = new ArrayList<>();
		spawnTimer = 0f;
		
		glowFade = 0.1f;
		glowBounce = new BounceTween(1f);
		
		isRightPressed = false;
		megaBlobX = 0f;
		megaBlobHeightTween = new BounceTween(1f);
		topTextFade = 0f;
		bottomTextFade = 0f;
		
		blackFade = 0f;
		whiteFade = 0f;
		
		scene1 = true;
		scene2 = false;
		gameover = false;
		pressedAnyKey = false;
		
		services.jukebox.play("music/Finding the Balance.mp3");
	}
	
	@Override
	public InputProcessor getInputProcessor() {
		return new InputAdapter() {
			
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Keys.RIGHT || keycode == Keys.D) {
					isRightPressed = true;
				}
				
				pressedAnyKey = gameover;
				return false;
			}
				
			@Override
			public boolean keyUp(int keycode) {
				if (keycode == Keys.RIGHT || keycode == Keys.D) {
					isRightPressed = false;
				}
				return false;
			}
			
		};
	}

	@Override
	public GameState update(float dt) {
		if (scene1) {
			animateScene1(dt);
		}
		
		if (scene2) {
			animateScene2(dt);
		}
		
		if (pressedAnyKey) {
			return restartGameState;
		}
	
		return null;
	}
	
	private void animateScene1(float dt) {
		spawnTimer += dt;
		glowFade = Math.min(1f, glowFade + dt * GLOW_SPEED);
		glowBounce.update(dt);
		
		if (glowFade >= 1f) {
			whiteFade = Math.min(1f, whiteFade + dt * FADE_TO_WHITE_SPEED);
			
			if (whiteFade >= 1f) {
				scene1 = false;
				scene2 = true;
			}
		}
		
		if (spawnTimer > SPAWN_DELAY) {
			spawnTimer = 0;
			
			boolean side = rand.nextBoolean();
			boolean axis = rand.nextBoolean();
			
			float x;
			float y;
			
			if (axis) {
				x = Interpolation.linear.apply(-10, services.gfx.getWidth() + 10, rand.nextFloat());
				y = side ? -10 : services.gfx.getHeight() + 10;
			}
			else {
				x = side ? -10 : services.gfx.getWidth() + 10;
				y = Interpolation.linear.apply(-10, services.gfx.getHeight() + 10, rand.nextFloat());
			}
			
			blobPositions.add(new Vector2(x, y));
			tweenValues.add(0f);
		}
		
		// tween
		for (int i = 0; i < tweenValues.size(); ++i) {
			float t = tweenValues.get(i);
			t = Math.min(1f, t + dt);
			tweenValues.set(i, t);
		}
	}
	
	private void animateScene2(float dt) {
		megaBlobHeightTween.update(dt);
		
		if (whiteFade > 0f) {
			whiteFade = Math.max(0f, whiteFade -= dt * FADE_FROM_WHITE_SPEED);
			return;
		}
		
		if (topTextFade < 1f) {
			topTextFade = Math.max(0f, topTextFade += dt * TEXT_FADE_SPEED);
			return;
		}
		
		if (bottomTextFade < 1f) {
			bottomTextFade = Math.max(0f, bottomTextFade += dt * TEXT_FADE_SPEED);
			return;
		}
		
		boolean outOfHere = megaBlobX > MEGA_BLOB_WALK_AMOUNT_BEFORE_FADE;
		
		if (isRightPressed || outOfHere) {
			megaBlobX += dt * MEGA_BLOB_WALK_SPEED;
		}
	
		if (outOfHere) {
			blackFade = Math.min(1f, blackFade + dt * FADE_TO_BLACK_SPEED);
			
			if (blackFade >= 1f) {
				scene2 = false;
				gameover = true;
			}
		}
	}

	@Override
	public void render(float dt) {
		services.gfx.setTransformMatrix(new Matrix4());
		
		if (scene1) {
			drawScene1();
		}
		
		if (scene2) {
			drawScene2();
		}
		
		if (scene1 || scene2) { // draw whitefade
			services.gfx.draw(whiteFadeTex, new Vector2(0,0), services.gfx.getWidth(), services.gfx.getHeight(), whiteFade);
		}
		
		if (gameover) {
			drawGameoverScene();
		}
	}
	
	private void drawScene1() {
		List<Vector2> tweenedPositions = new ArrayList<>(blobPositions.size());
		for (int i = 0; i < blobPositions.size(); ++i) {
			Vector2 spawnPos = blobPositions.get(i);
			float t = tweenValues.get(i);
			
			float a = Interpolation.linear.apply(0f, 1f, t);
			Vector2 pos = spawnPos.cpy().lerp(new Vector2(services.gfx.getMidX(), services.gfx.getMidY()), a);
			
			tweenedPositions.add(pos);
		}
		
		drawBlobSwarm(tweenedPositions);
		
		drawGlow();
	}

	private void drawBlobSwarm(Collection<Vector2> smallBlobPositions) {
		services.gfx.setTransformMatrix(new Matrix4());
		
		Random colGen = new Random(1);
		for (Vector2 smallPos : smallBlobPositions) {
			Color c = new Color(colGen.nextFloat(), colGen.nextFloat(), colGen.nextFloat(), 1f);
			services.gfx.drawCenteredTinted(blobTex, smallPos, blobTex.getWidth(), blobTex.getHeight(), false, c);
			services.gfx.drawCentered(blobOverlayTex, smallPos, blobOverlayTex.getWidth(), blobOverlayTex.getHeight(), false);
		}
	}
	
	private void drawGlow() {
		float scale = Interpolation.linear.apply(10, 1000, glowFade) + glowBounce.interpolate(50f);
		services.gfx.drawCentered(glowTex, new Vector2(services.gfx.getMidX(), services.gfx.getMidY()), scale, scale, false);
	}
	
	
	private void drawScene2() {
		float floorX = 50;
		float floorY = 100;
		float floorHeight = 400;
		float floorWidth = services.gfx.getWidth();
		services.gfx.draw(floorTex, new Vector2(floorX, floorY), floorWidth, floorHeight, 1f);
		
		float blobX = services.gfx.getMidX() - megaBlobTex.getWidth()/2 + megaBlobX;
		float blobY = floorY;
		float blobHeight = megaBlobTex.getHeight() + megaBlobHeightTween.interpolate(20);
		services.gfx.draw(megaBlobTex, new Vector2(blobX, blobY), megaBlobTex.getWidth(), blobHeight, 1f);
		
		float textPadding = 30f;
		services.gfx.drawFont(font, TOP_TEXT, 100, services.gfx.getHeight() - textPadding, topTextFade);
		services.gfx.drawFont(font, BOTTOM_TEXT, 400, font.getCapHeight() + textPadding, bottomTextFade);
		
		services.gfx.draw(endGameFadeTex, new Vector2(0,0), services.gfx.getWidth(), services.gfx.getHeight(), blackFade);
	}
	
	private void drawGameoverScene() {
		services.gfx.draw(endGameFadeTex, new Vector2(0,0), services.gfx.getWidth(), services.gfx.getHeight(), 1f);
		
		GlyphLayout layout = new GlyphLayout();
		layout.setText(font, GAME_OVER_TEXT);
		services.gfx.drawFont(font, GAME_OVER_TEXT, services.gfx.getMidX() - layout.width/2, services.gfx.getMidY() - layout.height/2, 1f);
	}
	
	private static Texture newTexture(GraphicsResources gfxResources, String filename) {
		return gfxResources.newTexture("sprites/" + filename);
	}
	
	private static BitmapFont newFont(GraphicsResources gfxResources, String filename) {
		return gfxResources.newFont("fonts/" + filename);
	}
}
