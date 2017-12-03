package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.awt.Color;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;

public class Light extends AbstractComponent implements ILight {
	private Color albedo;
	private float intensity;
	
	public Light(IEntity entity) {
		super(entity);
	}
	
	@Override
	public Color getAlbedo() {
		return albedo;
	}
	
	@Override
	public void setAlbedo(Color color) {
		this.albedo = color;
	}
	
	@Override
	public float getIntensity() {
		return intensity;
	}
	
	@Override
	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}
}
