package fr.jponzo.gamagora.nutshell3d.material.impl;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITexture;

public class Texture implements ITexture {
	private static final long serialVersionUID = 1L;
	
	private String imagePath;
	private float[] pixBuffer;
	private int width;
	private int height;

	Texture() {
	}
	
	@Override
	public String getImagePath() {
		return imagePath;
	}
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	@Override
	public float[] getPixBuffer() {
		return pixBuffer;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public void load() {
		BufferedImage image = null;
		try
		{
			image = ImageIO.read(new File(imagePath));        
			this.width = image.getWidth();
			this.height = image.getHeight();
			
			int ind = 0;
			pixBuffer = new float[this.width * this.height * 4];
			for (int j = 0; j < this.height; j++) {
				for (int i = 0; i < this.width; i++) {
					int rgb = image.getRGB(j, width - i - 1);
					int alpha = (rgb >> 24) & 0xff;
				    int red = (rgb >> 16) & 0xff;
				    int green = (rgb >> 8) & 0xff;
				    int blue = (rgb) & 0xff;
				    pixBuffer[ind++] = (float) red / 255.0f;
				    pixBuffer[ind++] = (float) green / 255.0f;
				    pixBuffer[ind++] = (float) blue / 255.0f;
				    pixBuffer[ind++] = (float) alpha / 255.0f;
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
