package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;

public class Mesh extends AbstractComponent implements IMesh {
	private IMaterial material;
	private IMeshDef meshDef;
	private boolean[] layers = new boolean[RenderingSystem.NB_CAM_LAYERS];
	
	public Mesh(IEntity entity, IMeshDef meshDef) {
		super(entity);
		this.meshDef = meshDef;
		for (int i = 0; i < RenderingSystem.NB_CAM_LAYERS; i++) {
			layers[i] = true;
		}
	}

	@Override
	public void enableLayer(int layer) {
		layers[layer] = true;
	}
	
	@Override
	public void disableLayer(int layer) {
		layers[layer] = false;
	}
	
	@Override
	public void setAllLayers(boolean value) {
		for (int i = 0; i < RenderingSystem.NB_CAM_LAYERS; i++) {
			layers[i] = value;
		}
	}
	
	@Override
	public boolean isEnabledLayer(int layer) {
		return layers[layer];
	}
	
	@Override
	public IMaterial getMaterial() {
		return material;
	}

	@Override
	public void setMaterial(IMaterial material) {
		this.material = material;
	}

	@Override
	public void removeMaterial() {
		this.material = null;
	}
	
	@Override
	public IMeshDef getMeshDef() {
		return meshDef;
	}
}
