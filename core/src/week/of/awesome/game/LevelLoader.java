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
		
		for (Element shadowMaskXml : xml.getChildrenByName("shadow-mask")) {
			ShadowMask mask = new ShadowMask();
			parseShadowMask(shadowMaskXml.getText(), mask);
			level.shadowMasks.add(mask);
		}
		
		for (Element teleportXml : xml.getChildrenByName("teleport")) {
			TeleportConfig teleport = new TeleportConfig();
			parseTeleport(teleportXml, teleport, level.height);
			level.teleports.add(teleport);
		}
		
		for (Element dialogXml : xml.getChildrenByName("dialog")) {
			DialogConfig dialog = new DialogConfig();
			parseDialog(dialogXml, dialog);
			level.dialogs.add(dialog);
		}
		
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
					
				// title tile
				case 'T':
					currRow.add(null);
					level.titlePos = currGridPosition;
					break;
					
				// floor tile
				case 'o':
					currRow.add(Tile.FLOOR);
					break;
					
				// water tile
				case 'w':
					currRow.add(Tile.WATER);
					break;
					
				// water tile
				case 'l':
					currRow.add(Tile.LAVA);
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
					level.waterGenes.add(currGridPosition);
					break;
					
				// red gene
				case 'r':
					currRow.add(Tile.FLOOR);
					level.lavaGenes.add(currGridPosition);
					break;
					
				// yellow gene
				case 'g':
					currRow.add(Tile.FLOOR);
					level.teleGenes.add(currGridPosition);
					break;
					
				default:
					throw new RuntimeException("Unknown tile token: " + token);
			}
		}
		
		level.width = currRow.size();
		level.height = level.tiles.size();
		
		// since the level is loaded from top-to-bottom need to invert all y-coords
		invertY(level.titlePos, level.height);
		invertY(level.blobStartPos, level.height);
		level.waterGenes.forEach(pos -> invertY(pos, level.height));
		level.lavaGenes.forEach(pos -> invertY(pos, level.height));
		level.teleGenes.forEach(pos -> invertY(pos, level.height));
		level.stars.forEach(pos -> invertY(pos, level.height));
	}
	
	private static void parseShadowMask(String shadowData, ShadowMask mask) {
		int x = 0;
		int y = 0;
	
		// parse tokens
		int tokenIdx = 0;
		while (tokenIdx < shadowData.length()) {
			char token = shadowData.charAt(tokenIdx);
			++tokenIdx;
			
			GridPos currGridPosition = new GridPos();
			currGridPosition.x = x;
			currGridPosition.y = y;
			
			switch (token) {
				// newlines are a new row
				case '\n':
				case '\r':
					if (x > 0) {
						++y;
						x = 0;
					}
					continue;
					
				// button tile
				case 'o':
					mask.buttons.add(currGridPosition);
					break;
				
				// shadow tile
				case 'x':
					mask.shadows.add(currGridPosition);
					break;
					
					// whitespace is irrelevant
				case '\t':
				case ' ':
					continue;
				
				// empty tile
				case '.':
					break;
				
				default:
					throw new RuntimeException("Unknown shadowmask token: " + token);
			}
			
			++x;
		}
		
		// since the level is loaded from top-to-bottom need to invert all y-coords
		final int height = y+1;
		mask.buttons.forEach(pos -> invertY(pos, height));
		mask.shadows.forEach(pos -> invertY(pos, height));
	}
	
	private static void parseTeleport(Element teleportXml, TeleportConfig teleport, int levelHeight) {
		teleport.padPos = parseGridPos( teleportXml.get("from") );
		teleport.targetPos = parseGridPos( teleportXml.get("to") );
	}
	
	private static void parseDialog(Element dialogXml, DialogConfig dialog) {
		String triggerGridPosText = dialogXml.get("trigger");
		dialog.trigger = parseGridPos(triggerGridPosText);
		
		dialog.blocksGameplay = dialogXml.getBoolean("blocksGameplay", false);
		
		for (Element textXml : dialogXml.getChildrenByName("text")) {
			float wait = textXml.getFloatAttribute("wait");
			String text = textXml.getText() == null ? "" : textXml.getText();
			boolean hasNewline = text.endsWith("\\n");
			if (hasNewline) {
				text = text.substring(0, text.length()-2);
			}
			int indents = 0;
			while (text.startsWith("\\t")) {
				text = text.substring(2);
				++indents;
			}
			dialog.text.add(text);
			dialog.lineBreaks.add(hasNewline);
			dialog.indents.add(indents);
			dialog.timings.add(wait);
		}
	}
	
	private static GridPos parseGridPos(String gridPosText) {
		String[] xy = gridPosText.split(",");
		GridPos gridPos = new GridPos();
		gridPos.x = Integer.parseInt(xy[0].trim());
		gridPos.y = Integer.parseInt(xy[1].trim());
		return gridPos;
	}
	
	private static void invertY(GridPos pos, int height) {
		pos.y = height-1 - pos.y;
	}
}
