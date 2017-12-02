package fr.jponzo.gamagora.modelgeo.tp5;

import java.util.List;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IComponent;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public interface ICurve extends IComponent {

	List<Vec3> getControlPts();

	void setControlPts(List<Vec3> controlPts);

	IMaterial getMaterial();

	void setMaterial(IMaterial material);

	float[][] getControlPtsTable();

	void updateFromControl();

	float getDiscrtisation();

	void setDiscrtisation(float discrtisation);

	float[][] getPtsTable();

	void moveControlPoint(int index, Vec3 newPos);
}
