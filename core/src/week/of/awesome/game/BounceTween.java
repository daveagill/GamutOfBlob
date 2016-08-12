package week.of.awesome.game;

import com.badlogic.gdx.math.Interpolation;

public class BounceTween {
	private float t = 0f;
	private float initialValue;
	private float speed;
	
	public BounceTween(float speed) {
		this.speed = speed;
	}
	
	public BounceTween(float speed, float initialValue) {
		this.speed = speed;
		this.initialValue = initialValue;
	}
	
	public boolean update(float dt) {
		t += (dt * speed);
		if (t > 2) {
			t = t-2;
			return true;
		}
		return false;
	}
	
	public float interpolate(float range) {
		return interpolate(range, Interpolation.pow2);
	}
	
	public float interpolate(float range, Interpolation interpolation) {
		float value = initialValue + t;
		
		while (value > 1f) {
			value = value - 2;
		}
		if (value < 0f) {
			value = -value;
		}
		
		return interpolation.apply(0, range, value);
	}
}
