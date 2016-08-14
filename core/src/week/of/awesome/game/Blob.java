package week.of.awesome.game;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.game.World.Direction;

public class Blob {
	
	public static enum Kind {
		BASIC, WATER, LAVA, TELEPORT
	}
	
	
	private static final float FALL_SPEED = 3f;
	private static final float WALK_SPEED = 8f;
	private static final float SCARED_VIBRATE_AMOUNT = 0.2f;
	private static final float TELEPORT_SPEED = 2f;
	
	private GridPos blobPos;
	private GridPos nextBlobPos;
	private float moveTween = 0f;
	private float speed;
	
	private Vector2 worldPos;
	
	private BounceTween scareTweener;
	private int scaredCyclesRemaining;
	
	private GridPos teleportTo;
	private float teleportOutTween;
	private float teleportInTween;
	
	private Direction lastDirection;
	private Direction queuedInput;
	
	private Kind kind;
	
	public Blob(GridPos position, Kind kind) {
		blobPos = position.cpy();
		blobPos.y += 5;
		nextBlobPos = position.cpy();
		worldPos = new Vector2(blobPos.x, blobPos.y);
		this.kind = kind;
		this.speed = FALL_SPEED;
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
	
	public void teleportTo(GridPos destination) {
		queuedInput = null;
		
		teleportTo = destination.cpy();
		teleportOutTween = 1f;
		teleportInTween = 0f;
	}
	
	public float getTeleportFade() {
		return teleportTo == null ? 1f : Math.max(teleportInTween, teleportOutTween);
	}
	
	public boolean update(World tileMap, float dt, boolean gameNotReadyToMove) {
		
		boolean isTeleporting = teleportTo != null;
		
		boolean walkComplete = processTileMovement(tileMap, dt, gameNotReadyToMove || isTeleporting);
		
		// animate for scaredness
		float scaredXOffset = 0f;
		if (isScared()) {
			if (scareTweener.update(dt)) {
				--scaredCyclesRemaining;
			}
			scaredXOffset = scareTweener.interpolate(SCARED_VIBRATE_AMOUNT, 0.5f, Interpolation.linear) - SCARED_VIBRATE_AMOUNT/2;
		}
		
		// animate teleportation
		if (isTeleporting) {
			if (teleportOutTween > 0f) {
				teleportOutTween = Math.max(0f, teleportOutTween - dt * TELEPORT_SPEED);
				
				// move blob to the destination
				if (teleportOutTween == 0) {
					blobPos = teleportTo.cpy();
					nextBlobPos = teleportTo.cpy();
				}
			}
			else if (teleportInTween < 1f) {
				teleportInTween = Math.min(1f, teleportInTween + dt * TELEPORT_SPEED);
			}
			else {
				teleportTo = null; // disable teleportation
				walkComplete = true;
			}
		}
		
		worldPos = new Vector2(blobPos.x, blobPos.y).lerp(new Vector2(nextBlobPos.x, nextBlobPos.y), moveTween).add(scaredXOffset, 0);
		
		return walkComplete;
	}
	
	
	private boolean processTileMovement(World tileMap, float dt, boolean movementBlocked) {
		if (movementBlocked) { // scrap enqueued input
			queuedInput = null;
		}
		
		boolean readyToWalk = moveTween == 1f;
		if (readyToWalk) {
			speed = WALK_SPEED;
			
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
			if (kind != Kind.WATER && t == Tile.WATER) { walkable = false; scary = true; }
			if (kind != Kind.LAVA && t == Tile.LAVA) { walkable = false; scary = true;  }
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
			moveTween = Math.min(1f, moveTween + speed * dt);
			
			// commit to the new location when the transition completes
			boolean walkComplete = moveTween == 1f;
			if (walkComplete) {
				blobPos = nextBlobPos.cpy();
				lastDirection = null;
				return true;
			}
		}
		
		return false;
	}
}
