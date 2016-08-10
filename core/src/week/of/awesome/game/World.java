package week.of.awesome.game;

import com.badlogic.gdx.math.Vector2;

public class World {
	public static final float TILE_SIZE = 16f;
	
	private static final float BLOB_SPEED = 8f;
	
	public static enum Direction {
		UP, DOWN, RIGHT, LEFT
	}
	
	private Level level;
	private int blobTilePosX, blobTilePosY;
	private int blobNextTilePosX, blobNextTilePosY;
	private float blobTween = 1f;
	private Direction lastDirection;
	
	private Direction queuedInput;
	
	public World() {
		level = LevelLoader.load("level1.txt");
		blobTilePosX = blobNextTilePosX = level.getBlobStartX();
		blobTilePosY = blobNextTilePosY = level.getBlobStartY();
		
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Vector2 getBlobPosition() {
		return new Vector2(blobTilePosX, blobTilePosY).lerp(new Vector2(blobNextTilePosX, blobNextTilePosY), blobTween).scl(TILE_SIZE);
	}
	
	public void moveBlob(Direction d) {
		if (queuedInput == null && d != lastDirection) {
			queuedInput = d;
		}
	}
	
	public void update(float dt) {
	
		boolean transitionBeginning = blobTween == 1f;
		if (transitionBeginning) {
			// dequeue the input
			Direction d = queuedInput;
			queuedInput = null;
			
			if (d == null) { return; }
			
			int blobNextTilePosX = blobTilePosX;
			int blobNextTilePosY = blobTilePosY;

			if (d == Direction.RIGHT) {
				blobNextTilePosX += 1;
			}
			else if (d == Direction.LEFT) {
				blobNextTilePosX -= 1;
			}
			else if (d == Direction.UP) {
				blobNextTilePosY += 1;
			}
			else if (d == Direction.DOWN) {
				blobNextTilePosY -= 1;
			}
			
			
			// validate the transition (collision detection)
			Tile t = level.tileAt(blobNextTilePosX, blobNextTilePosY);
			boolean passable = t != null;
			
			// if not passable then rollback the transition
			lastDirection = null;
			if (passable) {
				this.blobNextTilePosX = blobNextTilePosX;
				this.blobNextTilePosY = blobNextTilePosY;
				lastDirection = d;
				blobTween = 0f;
			}
		}
		
		if (blobTween < 1) {
			blobTween = Math.min(1f, blobTween + BLOB_SPEED * dt);
			
			// commit to the new location when the transition completes
			boolean completingTransition = blobTween == 1f;
			if (completingTransition) {
				blobTilePosX = blobNextTilePosX;
				blobTilePosY = blobNextTilePosY;
				lastDirection = null;
			}
		}
	}
}
