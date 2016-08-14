package week.of.awesome.game;

import java.util.Objects;

public class GridPos {
	public int x;
	public int y;
	
	public GridPos cpy() {
		GridPos cpy = new GridPos();
		cpy.x = x;
		cpy.y = y;
		return cpy;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof GridPos && x == ((GridPos)o).x && y == ((GridPos)o).y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
}
