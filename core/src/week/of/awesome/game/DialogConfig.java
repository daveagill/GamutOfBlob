package week.of.awesome.game;

import java.util.ArrayList;
import java.util.List;

public class DialogConfig {
	public List<String> text = new ArrayList<>();
	public List<Float> timings = new ArrayList<>();
	public List<Boolean> lineBreaks = new ArrayList<>();
	public GridPos trigger;
	public boolean blocksGameplay;
}
