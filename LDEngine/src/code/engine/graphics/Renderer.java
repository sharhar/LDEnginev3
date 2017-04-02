package code.engine.graphics;

import code.engine.math.Vector2f;
import code.engine.math.Vector4f;
import code.engine.window.Window;

public abstract class Renderer {
	public long window;
	public int height;
	public int width;
	
	abstract public Window createWindowandContext(int width, int height, String title, boolean debug, boolean vSync);
	abstract public void clear(Vector4f color);
	abstract public void swap();
	abstract public void destory();
	
	abstract public Model createModel(float[] data);
	abstract public Shader createShader(String vertPath, String fragPath);
	abstract public Texture createTexture(String path);
	abstract public Renderable createRenderable(Model model, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture);
	
	public Renderable createText(Shader shader, Texture texture, String text, float xOff, float yOff, float size) {
		int tsz = text.length();
		float[] verts = new float[tsz * 4 * 6];
		
		for (int i = 0; i < tsz; i++) {
			int chid = (int)text.charAt(i);

			float sx = chid % 16;
			float sy = (chid - sx) / 16;

			float x1 = xOff + size * i;
			float y1 = yOff + size;
			float x2 = xOff + size * (i + 1);
			float y2 = yOff;

			float tx1 = (sx + 0.0f) / 16.0f;
			float ty1 = (sy + 0.0f) / 16.0f;
			float tx2 = (sx + 1.0f) / 16.0f;
			float ty2 = (sy + 1.0f) / 16.0f;

			verts[i * 24 + 0] = x1;
			verts[i * 24 + 1] = y1;

			verts[i * 24 + 2] = tx1;
			verts[i * 24 + 3] = ty1;

			verts[i * 24 + 4] = x1;
			verts[i * 24 + 5] = y2;

			verts[i * 24 + 6] = tx1;
			verts[i * 24 + 7] = ty2;

			verts[i * 24 + 8] = x2;
			verts[i * 24 + 9] = y1;

			verts[i * 24 + 10] = tx2;
			verts[i * 24 + 11] = ty1;

			verts[i * 24 + 12] = x1;
			verts[i * 24 + 13] = y2;

			verts[i * 24 + 14] = tx1;
			verts[i * 24 + 15] = ty2;

			verts[i * 24 + 16] = x2;
			verts[i * 24 + 17] = y1;

			verts[i * 24 + 18] = tx2;
			verts[i * 24 + 19] = ty1;

			verts[i * 24 + 20] = x2;
			verts[i * 24 + 21] = y2;

			verts[i * 24 + 22] = tx2;
			verts[i * 24 + 23] = ty2;
		}
		
		Model model = createModel(verts);
		
		return createRenderable(model, shader, new Vector2f(0, 0), 0, new Vector2f(1, 1), texture);
	}
}
