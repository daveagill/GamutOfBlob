package week.of.awesome.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import week.of.awesome.framework.JukeboxService;

public class AmbientMusic {
	public static enum Mood {
		UNEASY,
		COMFORTABLE,
		SPOOKY,
		TENSE
	}
	
	private JukeboxService jukebox;
	private Map<Mood, List<String>> moodStack = new HashMap<>();
	
	public AmbientMusic(JukeboxService jukebox) {
		this.jukebox = jukebox;
		
		for (Mood mood : Mood.values()) {
			moodStack.put(mood, new ArrayList<>());
		}
		
		queue(Mood.UNEASY, "Lightless Dawn.mp3");
		
		queue(Mood.COMFORTABLE, "Danse Morialta.mp3");
		queue(Mood.COMFORTABLE, "Cattails.mp3");
		queue(Mood.COMFORTABLE, "Cipher2.mp3");
		queue(Mood.COMFORTABLE, "Marty Gots a Plan.mp3");
		queue(Mood.COMFORTABLE, "Bit Quest.mp3");
		queue(Mood.COMFORTABLE, "Disco con Tutti.mp3");
		queue(Mood.COMFORTABLE, "Electrodoodle.mp3");
		queue(Mood.COMFORTABLE, "Finding the Balance.mp3");
		
		queue(Mood.SPOOKY, "8bit Dungeon Level.mp3");
		
		queue(Mood.TENSE, "Spellbound.mp3");
	}
	
	public void playNext(Mood mood) {
		List<String> stack = moodStack.get(mood);
		jukebox.play(stack.get(0));
		stack.add(stack.remove(0));
	}
	
	private void queue(Mood mood, String file) {
		moodStack.get(mood).add("music/" + file);
	}
}
