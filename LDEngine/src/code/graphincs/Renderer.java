package code.graphincs;

import code.math.Vector4f;

public abstract class Renderer {
	abstract public void init(long window);
	abstract public void clear(Vector4f color);
	abstract public void swap();
	abstract public void destory();
}
