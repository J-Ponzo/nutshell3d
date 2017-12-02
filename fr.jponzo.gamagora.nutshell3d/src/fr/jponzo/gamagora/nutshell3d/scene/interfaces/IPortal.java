package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;

public interface IPortal extends IComponent {

	void setMaterial(IMaterial material);

	IMaterial getMaterial();

	Mat4 getViewMatrix(Mat4 eye);

	IPortal getTarget();

	void setTarget(IPortal target);

}