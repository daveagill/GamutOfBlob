package week.of.awesome.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input.Keys;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.GraphicsResources;
import week.of.awesome.framework.Services;

public class StartScreenState implements GameState {
	
	private static final String TITLE_TEXT = "Gamut of Blob";
	private static final String START_TEXT = "Press SPACE to start";
	
	private Services services;
	
	private GameState playGameState;
	private boolean starting;
	
	private BounceTween megaBlobHeightTween;
	
	private Texture mainBgTex;
	private Texture megaBlobTex;
	
	private BitmapFont font;
	
	public void setPlayGameState(GameState playGameState) {
		this.playGameState = playGameState;
	}

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		starting = false;
		megaBlobHeightTween = new BounceTween(1f);
		
		
		
		mainBgTex = newTexture(services.gfxResources, "mainbg.png");
		mainBgTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		megaBlobTex = services.gfxResources.newTexture("startScreenBlob.png");
		
		font = newFont(services.gfxResources, "trench.fnt");
	}

	@Override
	public GameState update(float dt) {
		megaBlobHeightTween.update(dt);
		
		if (starting) {
			return playGameState;
		}
		return null;
	}

	@Override
	public void render(float dt) {
		services.gfx.setTransformMatrix(new Matrix4());
		services.gfx.drawScreen(mainBgTex, 1f);
		
		float blobX = services.gfx.getMidX() - megaBlobTex.getWidth()/2;
		float blobY = 150;
		float blobHeight = megaBlobTex.getHeight() + megaBlobHeightTween.interpolate(20);
		services.gfx.draw(megaBlobTex, new Vector2(blobX, blobY), megaBlobTex.getWidth(), blobHeight, 1f);
		
		
		GlyphLayout layout = new GlyphLayout();
		layout.setText(font, START_TEXT);
		services.gfx.drawFont(font, START_TEXT, services.gfx.getMidX() - layout.width/2, 100, 1f);
		
		
		GlyphLayout titleLayout = new GlyphLayout();
		titleLayout.setText(font, TITLE_TEXT);
		services.gfx.drawFont(font, TITLE_TEXT, services.gfx.getMidX() - titleLayout.width/2, services.gfx.getHeight() - 50, 1f);
	}
	
	@Override
	public InputProcessor getInputProcessor() {
		return new InputAdapter() {
			
			@Override
			public boolean keyDown(int keycode) {
				starting = keycode == Keys.SPACE;
				return false;
			}
			
		};
	}
	
	private static Texture newTexture(GraphicsResources gfxResources, String filename) {
		return gfxResources.newTexture("sprites/" + filename);
	}
	
	private static BitmapFont newFont(GraphicsResources gfxResources, String filename) {
		return gfxResources.newFont("fonts/" + filename);
	}
}
