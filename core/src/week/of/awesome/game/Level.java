package week.of.awesome.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Level {
	public String name;
	public GridPos titlePos = new GridPos();
	
	public String music;
	
	public List<List<Tile>> tiles = new ArrayList<>();
	public int width;
	public int height;
	
	public GridPos blobStartPos;
	
	public Collection<GridPos> waterGenes = new ArrayList<>();
	public Collection<GridPos> lavaGenes = new ArrayList<>();
	public Collection<GridPos> teleGenes = new ArrayList<>();
	public Collection<GridPos> stars = new ArrayList<>();
	
	public Collection<ShadowMask> shadowMasks = new ArrayList<>();
	
	public Collection<DialogConfig> dialogs = new ArrayList<>();
	
	public Collection<TeleportConfig> teleports = new ArrayList<>();

	public Tile tileAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) { return null; }
		return tiles.get(tiles.size()-1 - y).get(x);
	}
}
