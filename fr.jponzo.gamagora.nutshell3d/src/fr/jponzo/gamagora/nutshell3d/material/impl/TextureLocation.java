package fr.jponzo.gamagora.nutshell3d.material.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITextureLocation;

public class TextureLocation implements ITextureLocation {
	private int atlasId;
	private int ox;
	private int oy;
	
	TextureLocation() {
	}
	
	@Override
	public int getAtlasId() {
		return atlasId;
	}
	
	@Override
	public void setAtlasId(int atlasId) {
		this.atlasId = atlasId;
	}
	
	@Override
	public int getOx() {
		return ox;
	}
	
	@Override
	public void setOx(int ox) {
		this.ox = ox;
	}
	
	@Override
	public int getOy() {
		return oy;
	}
	
	@Override
	public void setOy(int oy) {
		this.oy = oy;
	}
}
