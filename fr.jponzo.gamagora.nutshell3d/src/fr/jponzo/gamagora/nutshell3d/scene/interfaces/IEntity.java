package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import java.awt.Component;
import java.util.List;

import fr.jponzo.gamagora.modelgeo.tp5.ICurve;

public interface IEntity {
	IEntity getParent();
	
	IComponent getComponent(int index);
	
	List<IMirror> getMirrors();	

	List<ICamera> getCameras();

	List<ILight> getLights();
	
	List<IMesh> getMeshes();

	List<ITransform> getTransforms();

	List<IUpdator> getUpdators();
	
	List<ICurve> getCurves();
	
	void addComponent(IComponent component);

	void removeComponent(int index);

	int indexOfComponent(IComponent component);

	int getComponentsCount();

	IEntity getChild(int index);

	void addChild(IEntity child);

	void removeChild(int index);

	int indexOfChild(IEntity child);

	int getChildsCount();
}
