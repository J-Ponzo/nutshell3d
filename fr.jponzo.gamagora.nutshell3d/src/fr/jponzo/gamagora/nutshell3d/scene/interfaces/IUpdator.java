package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

public interface IUpdator extends IComponent {
	public void init();
	public void update(long deltaTime);
}
