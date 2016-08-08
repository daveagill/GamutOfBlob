package week.of.awesome.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.Services;

public class PlayGameState implements GameState {
	
	private Services services;
	Texture img;

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		img = services.gfxResources.newTexture("badlogic.jpg");
	}

	@Override
	public GameState update(float dt) {

		return null;
	}

	@Override
	public void render(float dt) {
		services.gfx.drawCentered(img, new Vector2(100,100), 200, 200, false);
	}

}
