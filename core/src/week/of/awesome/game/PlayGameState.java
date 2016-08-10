package week.of.awesome.game;


import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

import java.util.ArrayDeque;
import java.util.Deque;
import com.badlogic.gdx.Input.Keys;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.Services;
import week.of.awesome.game.World.Direction;

public class PlayGameState implements GameState {
	
	private Services services;
	
	private GameRenderer renderer;
	private World world;
	
	private Deque<Direction> directionStack = new ArrayDeque<>();

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		renderer = new GameRenderer(services.gfxResources, services.gfx);
		world = new World();
	}

	@Override
	public GameState update(float dt) {
		if (!directionStack.isEmpty()) {
			world.moveBlob(directionStack.getLast());
		} else {
			//world.moveBlob(null);
		}
		
		world.update(dt);
		return null;
	}

	@Override
	public void render(float dt) {
		renderer.draw(world);
	}

	@Override
	public InputProcessor getInputProcessor() {
		return new InputAdapter() {
			
			@Override
			public boolean keyDown(int keycode) {
				Direction direction = mapToDirection(keycode);
				if (direction != null) {
					directionStack.addLast(direction);
				}
				return false;
			}

			@Override
			public boolean keyUp(int keycode) {
				Direction direction = mapToDirection(keycode);
				if (direction != null) {
					directionStack.remove(direction);
				}
				return false;
			}
			
			private Direction mapToDirection(int keycode) {
				if (keycode == Keys.RIGHT || keycode == Keys.D) {
					return Direction.RIGHT;
				}
				if (keycode == Keys.LEFT || keycode == Keys.A) {
					return Direction.LEFT;
				}
				if (keycode == Keys.UP || keycode == Keys.W) {
					return Direction.UP;
				}
				if (keycode == Keys.DOWN || keycode == Keys.S) {
					return Direction.DOWN;
				}
				
				return null;
			}
			
		};
	}
}
