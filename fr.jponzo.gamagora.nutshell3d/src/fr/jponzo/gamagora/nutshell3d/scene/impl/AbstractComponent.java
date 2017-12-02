package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IComponent;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;

public abstract class AbstractComponent implements IComponent {
	protected IEntity entity;

	public AbstractComponent(IEntity entity) {
		super();
		this.entity = entity;
		this.entity.addComponent(this);
	}	
}
