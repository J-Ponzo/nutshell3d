package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;

public class Mesh extends AbstractComponent implements IMesh {
	private IMaterial material;
	private IMeshDef meshDef;
	
	public Mesh(IEntity entity, IMeshDef meshDef) {
		super(entity);
		this.meshDef = meshDef;
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
