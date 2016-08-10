package week.of.awesome.game;

import java.util.List;

public class Level {
	private String name;
	
	private List<List<Tile>> tiles;
	private int width;
	private int height;
	
	private int blobStartX;
	private int blobStartY;
	
	public Level(String name, List<List<Tile>> tiles, int blobStartX, int blobStartY) {
		this.name = name;
		this.tiles = tiles;
		this.width = tiles.get(0).size();
		this.height = tiles.size();
		this.blobStartX = blobStartX;
		this.blobStartY = blobStartY;
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Tile tileAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) { return null; }
		return tiles.get(tiles.size()-1 - y).get(x);
	}
	
	public int getBlobStartX() {
		return blobStartX;
	}
	
	public int getBlobStartY() {
		return blobStartY;
	}
}
