package code.engine.math;

public class Vector2f {
	public float x, y;
	
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2f() {
		this.x = 0;
		this.y = 0;
	}
	
	public Vector2f add(Vector2f other) {
		return new Vector2f(other.x + x, other.y + y);
	}
}
