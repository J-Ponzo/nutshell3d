package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.awt.Color;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;

public class Light extends AbstractComponent implements ILight {
	private Color color;
	private float intensity;
	
	public Light(IEntity entity) {
		super(entity);
	}
	
	@Override
	public Color getColor() {
		return color;
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
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
