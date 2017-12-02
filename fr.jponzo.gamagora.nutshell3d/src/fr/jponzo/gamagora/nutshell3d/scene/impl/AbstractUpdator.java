package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IUpdator;

public abstract class AbstractUpdator extends AbstractComponent implements IUpdator {

	public AbstractUpdator(IEntity entity) {
		super(entity);
	}

}
