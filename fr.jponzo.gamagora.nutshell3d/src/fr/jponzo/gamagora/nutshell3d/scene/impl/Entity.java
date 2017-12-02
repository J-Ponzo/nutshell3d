package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.util.ArrayList;
import java.util.List;

import fr.jponzo.gamagora.modelgeo.tp5.ICurve;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IComponent;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMirror;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IUpdator;

public class Entity implements IEntity {
	IEntity parent;
	List<IEntity> childs = new ArrayList<IEntity>();
	List<IComponent> components = new ArrayList<IComponent>();
	
	@Override
	public IEntity getParent() {
		return parent;
	}

	@Override
	public IComponent getComponent(int index) {
		return components.get(index);
	}
	
	@Override
	public List<IMirror> getMirrors() {
		List<IMirror> componentsList = new ArrayList<IMirror>();
		for (IComponent component : components) {
			if (component instanceof IMirror) {
				componentsList.add((IMirror) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<ICamera> getCameras() {
		List<ICamera> componentsList = new ArrayList<ICamera>();
		for (IComponent component : components) {
			if (component instanceof ICamera) {
				componentsList.add((ICamera) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<ILight> getLights() {
		List<ILight> componentsList = new ArrayList<ILight>();
		for (IComponent component : components) {
			if (component instanceof ILight) {
				componentsList.add((ILight) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<IMesh> getMeshes() {
		List<IMesh> componentsList = new ArrayList<IMesh>();
		for (IComponent component : components) {
			if (component instanceof IMesh) {
				componentsList.add((IMesh) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<ITransform> getTransforms() {
		List<ITransform> componentsList = new ArrayList<ITransform>();
		for (IComponent component : components) {
			if (component instanceof ITransform) {
				componentsList.add((ITransform) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<IUpdator> getUpdators() {
		List<IUpdator> componentsList = new ArrayList<IUpdator>();
		for (IComponent component : components) {
			if (component instanceof IUpdator) {
				componentsList.add((IUpdator) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public List<ICurve> getCurves() {
		List<ICurve> componentsList = new ArrayList<ICurve>();
		for (IComponent component : components) {
			if (component instanceof ICurve) {
				componentsList.add((ICurve) component);
			}
		}
		return componentsList;
	}
	
	@Override
	public void addComponent(IComponent component) {
		components.add(component);
	}
	
	@Override
	public void removeComponent(int index) {
		components.remove(index);
	}
	
	@Override
	public int indexOfComponent(IComponent component) {
		return components.indexOf(component);
	}
	
	@Override
	public int getComponentsCount() {
		return components.size();
	}
	
	@Override
	public IEntity getChild(int index) {
		return childs.get(index);
	}
	
	@Override
	public void addChild(IEntity child) {
		childs.add(child);
		((Entity)child).parent = this;
	}
	
	@Override
	public void removeChild(int index) {
		childs.remove(index);
	}
	
	@Override
	public int indexOfChild(IEntity child) {
		return childs.indexOf(child);
	}
	
	@Override
	public int getChildsCount() {
		return childs.size();
	}
}
