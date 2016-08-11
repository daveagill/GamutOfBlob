package week.of.awesome.game;

import com.badlogic.gdx.math.Interpolation;

public class BounceTween {
	private float value = 0f;
	private float tweenSign = 1f;
	private float speed;
	
	public BounceTween(float speed) {
		this.speed = speed;
	}
	
	public void update(float dt) {
		value += dt * tweenSign * speed;
		
		if (value > 1f) {
			value = 2f - value;
			tweenSign = -1f;
		}
		else if (value < 0f) {
			value = -value;
			tweenSign = 1f;
		}
	}
	
	public float interpolate(float range) {
		return Interpolation.pow2.apply(0, range, value);
	}
}
