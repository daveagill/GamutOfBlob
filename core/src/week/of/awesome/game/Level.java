package week.of.awesome.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Level {
	public String name;
	
	public List<List<Tile>> tiles = new ArrayList<>();
	public int width;
	public int height;
	
	public GridPos blobStartPos;
	
	public Collection<GridPos> blueGenes = new ArrayList<>();
	

	public Tile tileAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) { return null; }
		return tiles.get(tiles.size()-1 - y).get(x);
	}
}