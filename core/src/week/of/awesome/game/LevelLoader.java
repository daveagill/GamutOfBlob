package week.of.awesome.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class LevelLoader {

	public static Level load(String file) {
		Element xml;
		try {
			xml = new XmlReader().parse(Gdx.files.internal(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Level level = new Level();
		
		level.name = xml.getAttribute("name");
		
		String mapData = xml.getChildByName("map").getText();
		parseMap(mapData, level);
		
		return level;
	}
	
	private static void parseMap(String mapData, Level level) {
		
		List<Tile> currRow = new ArrayList<>();
		level.tiles.add(currRow);
		
		// parse tokens
		int tokenIdx = 0;
		while (tokenIdx < mapData.length()) {
			char token = mapData.charAt(tokenIdx);
			++tokenIdx;
			
			GridPos currGridPosition = new GridPos();
			currGridPosition.x = currRow.size();
			currGridPosition.y = level.tiles.size()-1;
			
			switch (token) {
				// newlines are a new row
				case '\n':
				case '\r':
					if (!currRow.isEmpty()) {
						currRow = new ArrayList<>();
						level.tiles.add(currRow);
					}
					break;
					
				// whitespace is irrelevant
				case '\t':
				case ' ':
					continue;
				
				// empty tile
				case '.':
					currRow.add(null);
					break;
					
				// floor tile
				case 'o':
					currRow.add(Tile.FLOOR);
					break;
					
				// water tile
				case 'w':
					currRow.add(Tile.WATER);
					break;
					
				// blob spawner
				case '@':
					currRow.add(Tile.FLOOR);
					level.blobStartPos = currGridPosition;
					break;
					
				// star collectible
				case '*':
					currRow.add(Tile.FLOOR);
					level.stars.add(currGridPosition);
					break;
					
				// blue gene
				case 'b':
					currRow.add(Tile.FLOOR);
					level.blueGenes.add(currGridPosition);
					break;
					
				default:
					throw new RuntimeException("Unknown tile token: " + token);
			}
		}
		
		level.width = currRow.size();
		level.height = level.tiles.size();
		
		// sicne the level is loaded from top-to-bottom need to invert all y-coords
		invertY(level.blobStartPos, level);
		level.blueGenes.forEach(pos -> invertY(pos, level));
		level.stars.forEach(pos -> invertY(pos, level));
	}
	
	private static void invertY(GridPos pos, Level level) {
		pos.y = level.height-1 - pos.y;
	}
}
