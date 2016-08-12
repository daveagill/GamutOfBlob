package week.of.awesome.game;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.game.World.Direction;

public class Blob {
	
	public static enum Kind {
		GREEN, BLUE, RED
	}
	
	
	private static final float BLOB_SPEED = 8f;
	private static final float SCARED_VIBRATE_AMOUNT = 0.2f;
	
	private GridPos blobPos;
	private GridPos nextBlobPos;
	private float moveTween = 1f;
	
	private Vector2 worldPos;
	
	private BounceTween scareTweener;
	private int scaredCyclesRemaining;
	
	private Direction lastDirection;
	private Direction queuedInput;
	
	private Kind kind;
	
	public Blob(GridPos position, Kind kind) {
		blobPos = position.cpy();
		nextBlobPos = position.cpy();
		worldPos = new Vector2(blobPos.x, blobPos.y);
		this.kind = kind;
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public GridPos getGridPosition() {
		return blobPos.cpy();
	}
	
	public Vector2 getPosition() {
		return worldPos.cpy();
	}
	
	public void enqueueMovement(Direction d) {
		if (queuedInput == null && d != lastDirection) {
			queuedInput = d;
		}
	}
	
	public void becomeScared() {
		if (isScared()) { return; }
		scaredCyclesRemaining = 5;
		scareTweener = new BounceTween(20);
	}
	
	public boolean isScared() {
		return scaredCyclesRemaining > 0;
	}
	
	public boolean update(World tileMap, float dt) {
		
		boolean walkComplete = processTileMovement(tileMap, dt);
		
		// animate for scaredness
		float scaredXOffset = 0f;
		if (isScared()) {
			if (scareTweener.update(dt)) {
				--scaredCyclesRemaining;
			}
			scaredXOffset = scareTweener.interpolate(SCARED_VIBRATE_AMOUNT, 0.5f, Interpolation.linear) - SCARED_VIBRATE_AMOUNT/2;
		}
			
		
		worldPos = new Vector2(blobPos.x, blobPos.y).lerp(new Vector2(nextBlobPos.x, nextBlobPos.y), moveTween).add(scaredXOffset, 0);
		
		return walkComplete;
	}
	
	
	private boolean processTileMovement(World tileMap, float dt) {
		boolean readyToWalk = moveTween == 1f;
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
			Tile t = tileMap.tileAt(nextPos);
			boolean walkable = t != null;
			boolean scary = false;
			if (kind != Kind.BLUE && t == Tile.WATER) { walkable = false; scary = true; }
			if (kind != Kind.RED && t == Tile.LAVA) { walkable = false; scary = true;  }
			if (tileMap.isShadowAt(nextPos)) { walkable = false; scary = true; }
			
			if (scary && t != null) {
				becomeScared();
			}
			
			// if walkable then kick off the transition
			lastDirection = null;
			if (walkable) {
				this.nextBlobPos = nextPos;
				lastDirection = d;
				moveTween = 0f;
			}
		}
		
		if (moveTween < 1) {
			moveTween = Math.min(1f, moveTween + BLOB_SPEED * dt);
			
			// commit to the new location when the transition completes
			boolean walkComplete = moveTween == 1f;
			if (walkComplete) {
				blobPos.set( nextBlobPos );
				lastDirection = null;
				return true;
			}
		}
		
		return false;
	}
}
