package week.of.awesome.game;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.Services;

public class PlayGameState implements GameState {
	
	private Services services;
	
	private GameRenderer renderer;
	private Level level;

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		renderer = new GameRenderer(services.gfxResources, services.gfx);
		level = LevelLoader.load("level1.txt");
	}

	@Override
	public GameState update(float dt) {
		return null;
	}

	@Override
	public void render(float dt) {
		renderer.draw(level);
	}

}
