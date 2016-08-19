package week.of.awesome.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.math.Vector2;

import week.of.awesome.game.Blob.Kind;

public class World {
	
	public static enum Direction {
		UP, DOWN, RIGHT, LEFT
	}
	
	private Level level;
	
	private Vector2 cameraPos;
	
	private int activeBlobIdx;
	private List<Blob> blobs;
	private Collection<GridPos> activeWaterGenes;
	private Collection<GridPos> activeLavaGenes;
	private Collection<GridPos> activeTeleGenes;
	private Collection<GridPos> activeStars;
	private Collection<ShadowAndButtonState> shadowsAndButtons;
	
	private Collection<DialogState> dialogs;
	private boolean isMovementBlocked = false;
	
	private boolean isGameplayStarted = false;
	private int numStarsCollected;
	
	public World(String levelFilename) {
		this.level = LevelLoader.load(levelFilename);
		cameraPos = new Vector2(level.width/2f, level.height/2f);
		
		blobs = new ArrayList<>();
		blobs.add(new Blob(level.blobStartPos, Kind.BASIC));
		activeWaterGenes = new HashSet<>(level.waterGenes);
		activeLavaGenes = new HashSet<>(level.lavaGenes);
		activeTeleGenes = new HashSet<>(level.teleGenes);
		activeStars = new HashSet<>(level.stars);
		shadowsAndButtons = level.shadowMasks.stream().map(ShadowAndButtonState::new).collect(Collectors.toList());

		dialogs = level.dialogs.stream().map(DialogState::new).collect(Collectors.toList());
		
		activeBlobIdx = 0;
		numStarsCollected = 0;
	}
	
	public String getSoundtrack() {
		return level.music;
	}
	
	public String getMapName() {
		return level.name;
	}
	
	public GridPos getMapNamePosition() {
		return level.titlePos;
	}
	
	public int getMapWidth() {
		return level.width;
	}
	
	public int getMapHeight() {
		return level.height;
	}
	
	public Vector2 getCameraPosition() {
		return cameraPos.cpy();
	}
	
	public Tile tileAt(GridPos pos) {
		return level.tileAt(pos.x, pos.y);
	}
	
	public boolean isShadowAt(GridPos pos) {
		for (ShadowAndButtonState shadowsAndButton : shadowsAndButtons) {
			if (shadowsAndButton.isShadowAt(pos)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Collection<DialogState> getDialogs() {
		return dialogs;
	}
	
	public Collection<TeleportConfig> getTeleports() {
		return level.teleports;
	}
	
	public Collection<ShadowAndButtonState> getShadowsAndButtons() {
		return shadowsAndButtons;
	}
	
	public Collection<GridPos> getStars() {
		return activeStars;
	}
	
	public Collection<GridPos> getWaterGenes() {
		return activeWaterGenes;
	}
	
	public Collection<GridPos> getLavaGenes() {
		return activeLavaGenes;
	}
	
	public Collection<GridPos> getTeleGenes() {
		return activeTeleGenes;
	}
	
	public Collection<Blob> getBlobs() {
		return blobs;
	}
	
	public Vector2 getActiveBlobPosition() {
		return activeBlob().getPosition();
	}
	
	public void moveBlob(Direction d) {
		activeBlob().enqueueMovement(d);
	}
	
	public void switchBlob(boolean forward) {
		int attemptsRemaining = blobs.size();
		while (attemptsRemaining > 0) {
			activeBlobIdx += forward ? 1 : -1;
			if (activeBlobIdx >= blobs.size()) { activeBlobIdx = 0; }
			if (activeBlobIdx < 0) { activeBlobIdx = blobs.size()-1; }
			if (isShadowAt(activeBlob().getGridPosition())) { // skip shadowed blobs
				--attemptsRemaining;
				continue;
			}
			break;
		}
	}
	
	public int getNumStarsCollected() {
		return numStarsCollected;
	}
	
	public boolean isGameplayStarted() {
		return isGameplayStarted;
	}
	
	public void update(float dt, WorldEvents events) {
		isMovementBlocked = !isGameplayStarted; // prevent movement until gameplay starts
		for (DialogState d : dialogs) {
			isMovementBlocked |= d.isBlockingGameplay(); // prevent movement when dialog is open
		}
		for (DialogState d : dialogs) {
			if (d.pending()) {
				d.update(dt);
				break; // one at at time!
			}
		}
		
		for (int i = 0; i < blobs.size(); ++i) {
			processBlob(dt, blobs.get(i), events);
		}
		
		// update camera
		float xThreshold = 6;
		float yThreshold = 7;
		float rightThreshold = cameraPos.x + xThreshold;
		float leftThreshold = cameraPos.x - xThreshold;
		float topThreshold = cameraPos.y + yThreshold;
		float bottomThreshold = cameraPos.y - yThreshold;
		Vector2 blobTrackingPosition = activeBlob().getPosition();
		if (blobTrackingPosition.x > rightThreshold) {
			cameraPos.x += blobTrackingPosition.x - rightThreshold;
		}
		else if (blobTrackingPosition.x < leftThreshold) {
			cameraPos.x -= leftThreshold - blobTrackingPosition.x;
		}
		if (blobTrackingPosition.y > topThreshold) {
			cameraPos.y += blobTrackingPosition.y - topThreshold;
		}
		else if (blobTrackingPosition.y < bottomThreshold) {
			cameraPos.y -= bottomThreshold - blobTrackingPosition.y;
		}
	}
	
	private void processBlob(float dt, Blob blob, WorldEvents events) {
		GridPos oldBlobPos = blob.getGridPosition();
		boolean onNewTile = blob.update(this, dt, isMovementBlocked);
		
		if (onNewTile) {
			isGameplayStarted = true; // gameplay starts once the blob arrives at its first cell
			
			GridPos blobGridPos = blob.getGridPosition();
			Tile tile = tileAt(blobGridPos);
			
			// gene pickups
			boolean geneCollected = false;
			geneCollected |= pickupGenes(activeWaterGenes, Kind.WATER, blobGridPos);
			geneCollected |= pickupGenes(activeLavaGenes, Kind.LAVA, blobGridPos);
			geneCollected |= pickupGenes(activeTeleGenes, Kind.TELEPORT, blobGridPos);
			if (geneCollected) {
				events.onCollectedGene();
			}
			
			// star pickups
			if (activeStars.remove(blobGridPos)) {
				events.onCollectedStar();
				++numStarsCollected;
				
				if (activeStars.isEmpty()) {
					events.onLevelComplete();
				}
			}
			
			// activate and deactivate buttons
			for (ShadowAndButtonState shadowsAndButton : shadowsAndButtons) {
				if (shadowsAndButton.activateButton(blobGridPos)) {
					events.onButtonActivated();
				}
			}
			if (!oldBlobPos.equals(blobGridPos)) {
				for (ShadowAndButtonState shadowsAndButton : shadowsAndButtons) {
					if (shadowsAndButton.deactivateButton(oldBlobPos)) {
						events.onButtonDeactivated();
					}
				}
			}
			
			// teleport pads
			for (TeleportConfig teleport : getTeleports()) {
				if (blob.getKind() == Kind.TELEPORT && blobGridPos.equals(teleport.padPos)) {
					blob.teleportTo(teleport.targetPos);
					events.onTeleport();
				}
			}
			
			// activate dialog
			for (DialogState d : dialogs) {
				d.trigger(blobGridPos);
			}
			
			events.onBlobMoved(tile);
		}
	}
	
	private boolean pickupGenes(Collection<GridPos> activeGenes, Kind kind, GridPos at) {
		if (activeGenes.remove(at)) {
			// generate a new blob offset to the left
			GridPos newBlobPos = at.cpy();
			newBlobPos.x -= 1;
			blobs.add(activeBlobIdx+1, new Blob(newBlobPos, kind));
			return true;
		}
		return false;
	}

	private Blob activeBlob() {
		return blobs.get(activeBlobIdx);
	}
}
