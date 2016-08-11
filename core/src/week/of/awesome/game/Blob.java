package week.of.awesome.game;

import com.badlogic.gdx.math.Vector2;

import week.of.awesome.game.World.Direction;

public class Blob {
	
	public static enum Kind {
		GREEN, BLUE
	}
	
	
	private static final float BLOB_SPEED = 8f;
	
	private GridPos blobPos;
	private GridPos nextBlobPos;
	private float blobTween = 1f;
	
	private Direction lastDirection;
	private Direction queuedInput;
	
	private Kind kind;
	
	public Blob(GridPos position, Kind kind) {
		blobPos = position.cpy();
		nextBlobPos = position.cpy();
		this.kind = kind;
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public GridPos getGridPosition() {
		return blobPos;
	}
	
	public Vector2 getPosition() {
		return new Vector2(blobPos.x, blobPos.y).lerp(new Vector2(nextBlobPos.x, nextBlobPos.y), blobTween);
	}
	
	public void enqueueMovement(Direction d) {
		if (queuedInput == null && d != lastDirection) {
			queuedInput = d;
		}
	}
	
	public boolean update(World tileMap, float dt) {
		boolean readyToWalk = blobTween == 1f;
		if (readyToWalk) {
			// dequeue the input
			Direction d = queuedInput;
			queuedInput = null;
			
			if (d == null) { return false; }
			

			GridPos nextPos = blobPos.cpy();

			if (d == Direction.RIGHT) {
				nextPos.x += 1;
			}
			else if (d == Direction.LEFT) {
				nextPos.x -= 1;
			}
			else if (d == Direction.UP) {
				nextPos.y += 1;
			}
			else if (d == Direction.DOWN) {
				nextPos.y -= 1;
			}
			
			
			// validate the transition (collision detection)
			Tile t = tileMap.tileAt(nextPos.x, nextPos.y);
			boolean walkable = t != null;
			
			// if walkable then kick off the transition
			lastDirection = null;
			if (walkable) {
				this.nextBlobPos = nextPos;
				lastDirection = d;
				blobTween = 0f;
			}
		}
		
		if (blobTween < 1) {
			blobTween = Math.min(1f, blobTween + BLOB_SPEED * dt);
			
			// commit to the new location when the transition completes
			boolean walkComplete = blobTween == 1f;
			if (walkComplete) {
				blobPos.set( nextBlobPos );
				lastDirection = null;
				return true;
			}
		}
		
		return false;
	}
}
