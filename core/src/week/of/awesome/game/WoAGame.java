package week.of.awesome.game;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.StandardGameApp;

public class WoAGame extends StandardGameApp {

	@Override
	protected GameState setupGameStates() {
		StartScreenState startScreenState = new StartScreenState();
		PlayGameState playGameState = new PlayGameState();
		FinalStageState finalStageState = new FinalStageState();
		
		startScreenState.setPlayGameState(playGameState);
		playGameState.setGameOverState(finalStageState);
		finalStageState.setRestartGameState(startScreenState);
		
		return startScreenState;
	}

}
