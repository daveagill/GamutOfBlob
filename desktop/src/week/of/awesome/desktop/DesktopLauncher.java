package week.of.awesome.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import week.of.awesome.game.WoAGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Week of Awesome IV :: The Game";
		config.width = 800;
		config.height = 600;
		config.resizable = true;
		new LwjglApplication(new WoAGame(), config);
	}
}