package code.graphincs;

abstract public class Model {
	public float[] data;
	
	public Model(float[] data, Renderer renderer) {
		this.data = data;
		init(renderer);
	}
	
	abstract protected void init(Renderer renderer);
}
