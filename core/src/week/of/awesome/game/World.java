package week.of.awesome.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class World {
	
	public static enum Direction {
		UP, DOWN, RIGHT, LEFT
	}
	
	private Level level;
	
	int activeBlobIdx;
	private List<Blob> blobs;
	private Collection<GridPos> activeBlueGenes;
	
	public World() {
		level = LevelLoader.load("level1.txt");
		blobs = new ArrayList<>();
		blobs.add(new Blob(level.blobStartPos));
		activeBlueGenes = new HashSet<>(level.blueGenes);
		
		activeBlobIdx = 0;
	}
	
	public int getMapWidth() {
		return level.width;
	}
	
	public int getMapHeight() {
		return level.height;
	}
	
	public Tile tileAt(int i, int j) {
		return level.tileAt(i, j);
	}
	
	public Collection<GridPos> getBlueGenes() {
		return activeBlueGenes;
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
		activeBlobIdx += forward ? 1 : -1;
		if (activeBlobIdx >= blobs.size()) { activeBlobIdx = 0; }
		if (activeBlobIdx < 0) { activeBlobIdx = blobs.size()-1; }
	}
	
	public void update(float dt, WorldEvents events) {
		boolean onNewTile = activeBlob().update(this, dt);
		
		if (onNewTile) {
			events.onBlobMoved();
			
			GridPos blobGridPos = activeBlob().getGridPosition();
			
			if (activeBlueGenes.remove(blobGridPos)) {
				events.onCollectedGene();
				
				// generate a new blob offset to the left
				GridPos blueBlobPos = blobGridPos.cpy();
				blueBlobPos.x -= 1;
				blobs.add(new Blob(blueBlobPos));
			}
		}
	}

	private Blob activeBlob() {
		return blobs.get(activeBlobIdx);
	}
}