package week.of.awesome.game;

import java.util.ArrayList;
import java.util.List;

public class DialogState {
	private static final float FADE_SPEED = 4f;
	
	private DialogConfig config;
	private List<Float> timings;
	private float fade = 0f;
	private boolean fadingIn = true;
	private boolean fadingOut = false;
	private int currText = 0;
	
	private boolean triggered;
	private boolean active;
	private boolean complete;

	public DialogState(DialogConfig dialog) {
		this.config = dialog;
		timings = new ArrayList<>(dialog.timings);
	}
	
	public void update(float dt) {
		if (!pending()) { return; }
		active = true;
		
		if (fadingIn) {
			fade = Math.min(1, fade + dt * FADE_SPEED);
			fadingIn = fade < 1f;
		}
		if (fadingOut) {
			fade = Math.max(0f, fade - dt * FADE_SPEED);
			active = fade > 0f;
			complete = !active;
		}
		if (!fadingIn && !fadingOut) {
			float t = timings.get(currText) - dt;
			timings.set(currText, t);
			
			if (t <= 0) {
				if (currText == config.text.size()-1) {
					fadingOut = true;
					fade = 1f;
				}
				else {
					++currText;
					fadingIn = true;
					fade = 0f;
				}
			}
		}
	}
	
	public void trigger(GridPos pos) {
		if (config.trigger.equals(pos)) {
			triggered = true;
		}
	}
	
	public int size() {
		return config.text.size();
	}
	
	public int currentTextIdx() {
		return currText;
	}
	
	public String getText(int i) {
		return config.text.get(i);
	}
	
	public boolean isLineBreak(int i) {
		return config.lineBreaks.get(i);
	}
	
	public int getIndentation(int i) {
		return config.indents.get(i);
	}
	
	public float getFade() {
		return fade;
	}
	
	public boolean isFadingOut() {
		return fadingOut;
	}
	
	public boolean pending() {
		return triggered && !complete;
	}
	
	public boolean active() {
		return active;
	}
	
	public boolean isBlockingGameplay() {
		return triggered && currText < config.text.size()-1 && config.blocksGameplay;
	}
}
