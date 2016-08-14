package week.of.awesome.game;

import week.of.awesome.framework.JukeboxService;

public class AmbientMusic {

	private JukeboxService jukebox;
	private String previousTrack;
	
	public AmbientMusic(JukeboxService jukebox) {
		this.jukebox = jukebox;
	}
	
	public void playNext(String filename) {
		if (filename.equals(previousTrack)) { return; }
		jukebox.play("music/" + filename);
		previousTrack = filename;
	}

}
