package week.of.awesome.framework;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

public interface GameState {
	public void onEnter(Services services);
	public default void onExit() { }

	public GameState update(float dt);
	public void render(float dt);
	
	public default InputProcessor getInputProcessor() {
		return new InputAdapter() { };
	}
}