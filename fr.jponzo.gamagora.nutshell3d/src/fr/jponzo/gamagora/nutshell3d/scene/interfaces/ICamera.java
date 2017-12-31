package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import java.awt.Rectangle;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;

public interface ICamera extends IComponent {

	void setWidth(float width);

	float getWidth();

	float getHeight();

	void setHeight(float height);

	float getNear();

	void setNear(float near);

	float getFar();

	void setFar(float far);

	float getFov();

	void setFov(float fov);

	Rectangle getViewport();

	void setViewport(Rectangle viewPort);

	boolean isOrtho();

	void setOrtho(boolean isOrtho);

	Mat4 getViewMatrix();

	Mat4 getProjMatrix();

	IMaterial getMaterial();

	void setMaterial(IMaterial material);

	void enableLayer(int layer);

	void disableLayer(int layer);

	boolean isEnabledLayer(int layer);

}
