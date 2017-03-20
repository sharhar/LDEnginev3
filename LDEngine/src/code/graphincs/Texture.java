package code.graphincs;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

abstract public class Texture {
	public BufferedImage image = null;
	public int width = 0;
	public int height = 0;
	public int[] pixels = null;
	public int[] data = null;
	public Renderer renderer;
	
	public Texture(Renderer renderer, String path) {
		try {
			image = ImageIO.read(new FileInputStream(path));//ImageIO.read(Texture.class.getResourceAsStream(path));
			width = image.getWidth();
			height = image.getHeight();
			data = new int[width*height];
			image.getRGB(0,0,width,height,data,0,width);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		pixels = new int[width*height];
		for(int i = 0;i < width*height;i++) {
			int a = (data[i] & 0xff000000) >> 24;
			int r = (data[i] & 0xff0000) >> 16;
			int g = (data[i] & 0xff00) >> 8;
			int b = (data[i] & 0xff);
			
			pixels[i] = a << 24 | b << 16 | g << 8 | r;
		}
		
		this.renderer = renderer;
		
		init();
	}
	
	abstract protected void init();
}
