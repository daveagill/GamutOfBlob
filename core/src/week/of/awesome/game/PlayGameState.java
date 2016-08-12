package week.of.awesome.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayDeque;
import java.util.Deque;
import com.badlogic.gdx.Input.Keys;

import week.of.awesome.framework.GameState;
import week.of.awesome.framework.Services;
import week.of.awesome.game.World.Direction;

public class PlayGameState implements GameState {
	
	private static final float FADE_IN_SPEED = 2f;
	private static final float FADE_OUT_SPEED = 4f;
	
	private Services services;
	
	private GameState gameOverState;
	
	private int levelNum = 1;
	private boolean levelComplete;
	private boolean gameComplete;
	
	private float fadeIn;
	private float fadeOut;
	
	private Deque<Direction> directionStack = new ArrayDeque<>();
	private boolean ignoreInputs;
	
	private GameRenderer renderer;
	private World world;
	
	private Sound blobMovedSound;
	private Sound blobTreadWaterSound;
	private Sound collectedGeneSound;
	private Sound collectedStarSound;
	private Sound switchBlobSound;
	private Sound buttonActivatedSound;
	private Sound buttonDeactivatedSound;
	
	
	private WorldEvents eventHandler = new WorldEvents() {

		@Override
		public void onBlobMoved(Tile tile) {
			if (tile == Tile.WATER) {
				blobTreadWaterSound.play();
			}
			else {
				blobMovedSound.play(0.5f);
			}
		}

		@Override
		public void onCollectedGene() {
			collectedGeneSound.play();
		}
		
		@Override
		public void onCollectedStar() {
			collectedStarSound.play();
		}
		
		@Override
		public void onLevelComplete() {
			String nextLevelFilename = filenameForLevel(levelNum+1);
			gameComplete = !Gdx.files.internal(nextLevelFilename).exists();
			levelComplete = true;
		}

		@Override
		public void onButtonActivated() {
			buttonActivatedSound.play();
		}
		
		@Override
		public void onButtonDeactivated() {
			buttonDeactivatedSound.play();
		}
	};
	
	public void setGameOverState(GameState gameOverState) {
		this.gameOverState = gameOverState;
	}

	@Override
	public void onEnter(Services services) {
		this.services = services;
		
		levelComplete = false;
		gameComplete = false;
		
		fadeIn = 1f;
		fadeOut = 0f;
				
		directionStack.clear();
		ignoreInputs = true;
		
		renderer = new GameRenderer(services.gfxResources, services.gfx);
		world = new World(filenameForLevel(levelNum));
		
		blobMovedSound = services.sfxResources.newSound("blobMoved.wav");
		blobTreadWaterSound = services.sfxResources.newSound("treadWater.wav");
		collectedGeneSound = services.sfxResources.newSound("collectedGene.wav");
		collectedStarSound = services.sfxResources.newSound("collectedStar.wav");
		switchBlobSound = services.sfxResources.newSound("switchBlob.wav");
		buttonActivatedSound = services.sfxResources.newSound("buttonActivated.wav");
		buttonDeactivatedSound = services.sfxResources.newSound("buttonDeactivated.wav");
	}

	@Override
	public GameState update(float dt) {
		if (!ignoreInputs && !directionStack.isEmpty()) {
			world.moveBlob(directionStack.getLast());
		}
		
		// enable control after the fadeIn
		if (fadeIn == 0f) { ignoreInputs = false; }
		
		world.update(dt, eventHandler);
		
		// decide which gamestate to advance to
		if (levelComplete && fadeOut == 1f) {
			if (gameComplete) { return gameOverState; }
			
			++levelNum;
			return this; // next level
		}
		return null;
	}

	@Override
	public void render(float dt) {
		fadeIn = Math.max(0f, fadeIn - dt * FADE_IN_SPEED);
		fadeOut = levelComplete ? Math.min(1f, fadeOut + dt * FADE_OUT_SPEED) : 0f;
				
		renderer.draw(world, dt);
		
		if (fadeIn > 0) {
			renderer.drawFade(fadeIn);
		}
		if (fadeOut > 0) {
			renderer.drawFade(fadeOut);
		}
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
				else if (keycode == Keys.SHIFT_RIGHT || keycode == Keys.SHIFT_LEFT) {
					world.switchBlob(keycode == Keys.SHIFT_RIGHT);
					switchBlobSound.play();
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
	
	private String filenameForLevel(int idx) {
		return "level" + idx + ".txt";
	}
}
