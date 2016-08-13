package week.of.awesome.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input.Keys;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.Services;

public class StartScreenState implements GameState {
	
	private Services services;
	
	private GameState playGameState;
	private boolean starting;
	
	private Texture backgroundTex;
	
	public void setPlayGameState(GameState playGameState) {
		this.playGameState = playGameState;
	}

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		starting = false;
		backgroundTex = services.gfxResources.newTexture("startScreen.png");
	}

	@Override
	public GameState update(float dt) {
		if (starting) {
			return playGameState;
		}
		return null;
	}

	@Override
	public void render(float dt) {
		services.gfx.draw(backgroundTex, new Vector2(0,0), services.gfx.getWidth(), services.gfx.getHeight(), 1f);
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
}
