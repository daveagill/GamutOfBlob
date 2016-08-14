package week.of.awesome.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ShadowAndButtonState {
	private Collection<GridPos> shadows;
	private Map<GridPos, Integer> buttonsPushed;
	private int numPushed = 0;
	
	public ShadowAndButtonState(ShadowMask mask) {
		shadows = new HashSet<>(mask.shadows);
		buttonsPushed = new HashMap<>();
		mask.buttons.forEach(b -> buttonsPushed.put(b, 0));
	}
	
	public boolean activateButton(GridPos pos) {
		Integer pushCount = buttonsPushed.get(pos);
		if (pushCount != null) {
			buttonsPushed.put(pos.cpy(), pushCount+1);
			numPushed += pushCount == 0 ? 1 : 0;
			return true;
		}
		return false;
	}
	
	public boolean deactivateButton(GridPos pos) {
		Integer pushCount = buttonsPushed.get(pos);
		if (pushCount != null) {
			buttonsPushed.put(pos.cpy(), pushCount-1);
			numPushed -= pushCount == 1 ? 1 : 0;
			return true;
		}
		return false;
	}
	
	public boolean isLightOn() {
		return buttonsPushed.size() == numPushed && !buttonsPushed.isEmpty();
	}
	
	public boolean isShadowAt(GridPos pos) {
		return !isLightOn() && shadows.contains(pos);
	}
	
	public Collection<GridPos> getShadows() {
		return shadows;
	}
	
	public Collection<GridPos> getButtons() {
		return buttonsPushed.keySet();
	}
}
