package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;

public interface IMesh extends IComponent {
	void setMaterial(IMaterial mat);

	IMaterial getMaterial();

	IMeshDef getMeshDef();
	
	public void removeMaterial();

	void enableLayer(int layer);

	void disableLayer(int layer);

	boolean isEnabledLayer(int layer);

	void setAllLayers(boolean value);
}
