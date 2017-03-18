package code.graphincs.gl;

import code.graphincs.Model;
import code.graphincs.Renderer;

public class GLModel extends Model{
	public int vao;
	
	public GLModel(float[] data, Renderer renderer) {
		super(data, renderer);
	}

	protected void init(Renderer renderer) {
		
	}
}
