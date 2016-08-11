package week.of.awesome.game;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.StandardGameApp;

public class WoAGame extends StandardGameApp {

	@Override
	protected GameState setupGameStates() {
		PlayGameState playGameState = new PlayGameState();
		GameOverState gameOverState = new GameOverState();
		
		playGameState.setGameOverState(gameOverState);
		
		return playGameState;
	}

}
