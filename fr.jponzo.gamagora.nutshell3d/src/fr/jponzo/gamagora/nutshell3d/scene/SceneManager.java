package fr.jponzo.gamagora.nutshell3d.scene;

import java.util.List;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IUpdator;

public class SceneManager {
	private static class SingletonWrapper {
		private final static SceneManager instance = new SceneManager();
	}

	public static SceneManager getInstance() {
		return SingletonWrapper.instance;
	}

	private IEntity root;

	private ICamera activeCamera;

	public IEntity getRoot() {
		return root;
	}

	public void setRoot(IEntity root) {
		this.root = root;
	}

	public ICamera getActiveCamera() {
		return activeCamera;
	}

	public void setActiveCamera(ICamera activeCamera) {
		this.activeCamera = activeCamera;
	}

	public void initPass() {
		initSubtree(root);
	}

	public void computeWorldMatricesPass(long deltaTime) {
		computeWorldMatrices();
	}

	public void updatePass(long deltaTime) {
		updateSubtree(root, deltaTime);
	}

	private void computeWorldMatrices() {
		//Compose all local transform matrices
		composeSubTreeLocalTransforms(root);

		//Compute world Matrix
		computeSubTreeWorldTransform(root);

		//Decompose all World Matrices
		decomposeSubTreeWorldTransforms(root);
	}

	private void computeSubTreeWorldTransform(IEntity entity) {
		//Processing current Entity
		ITransform transform = entity.getTransforms().get(0);
		transform.computeWorldMatrix();

		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				computeSubTreeWorldTransform(entity.getChild(i));
			}
		}
	}

	private void decomposeSubTreeWorldTransforms(IEntity entity) {
		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				decomposeSubTreeWorldTransforms(entity.getChild(i));
			}
		}

		//Processing current Entity
		ITransform transform = entity.getTransforms().get(0);
		transform.decomposeWorldMatrix();
	}

	private void composeSubTreeLocalTransforms(IEntity entity) {
		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				composeSubTreeLocalTransforms(entity.getChild(i));
			}
		}

		//Processing current Entity
		ITransform transform = entity.getTransforms().get(0);
		transform.composeLocalMatrix();
	}

	private void initSubtree(IEntity entity) {
		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				initSubtree(entity.getChild(i));
			}
		}

		//Processing current Entity
		List<IUpdator> updators = entity.getUpdators();
		for (IUpdator updator : updators) {
			updator.init();
		}
	}

	private void updateSubtree(IEntity entity, long deltaTime) {
		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				updateSubtree(entity.getChild(i), deltaTime);
			}
		}

		//Processing current Entity
		List<IUpdator> updators = entity.getUpdators();
		for (IUpdator updator : updators) {
			updator.update(deltaTime);
		}
	}
}
