package week.of.awesome.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class LevelLoader {
	
	private static class MapParseResult {
		public List<List<Tile>> tiles = new ArrayList<>();
		public int blobX, blobY;
	}
	
	public static Level load(String file) {
		Element xml;
		try {
			xml = new XmlReader().parse(Gdx.files.internal(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		String levelName = xml.getAttribute("name");
		
		String mapData = xml.getChildByName("map").getText();
		MapParseResult parsedMap = parseMap(mapData);
		
		return new Level(levelName, parsedMap.tiles, parsedMap.blobX, parsedMap.blobY);
	}
	
	private static MapParseResult parseMap(String mapData) {
		
		MapParseResult result = new MapParseResult();
		
		List<Tile> currRow = new ArrayList<>();
		result.tiles.add(currRow);
		
		// parse tokens
		int tokenIdx = 0;
		while (tokenIdx < mapData.length()) {
			char token = mapData.charAt(tokenIdx);
			++tokenIdx;
			
			switch (token) {
				// newlines are a new row
				case '\n':
				case '\r':
					if (!currRow.isEmpty()) {
						currRow = new ArrayList<>();
						result.tiles.add(currRow);
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
					
				// blob spawner
				case '@':
					currRow.add(Tile.FLOOR);
					result.blobX = currRow.size()-1;
					result.blobY = result.tiles.size()-1;
					break;
					
				// goal (x marks the spot!)
				case 'x':
					currRow.add(Tile.GOAL);
					break;
					
				default:
					throw new RuntimeException("Unknown tile token: " + token);
			}
		}
		
		// since the file is loaded from top-to-bottom we need to invert the Y coordinates
		result.blobY = result.tiles.size()-1 - result.blobY;
		
		return result;
	}
}
