package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public interface ITransform extends IComponent {
	
	void setWorldTransform(Mat4 worldTransform);

	void setWorldTranslate(Mat4 worldTranslate);

	void setWorldRotate(Mat4 worldRotate);

	void setWorldScale(Mat4 worldScale);
	
	Mat4 getWorldTransform();

	Mat4 getWorldTranslate();

	Mat4 getWorldRotate();

	Mat4 getWorldScale();

	Mat4 getLocalTranslate();

	Mat4 getLocalRotate();

	Mat4 getLocalScale();
	
	void setLocalTransform(Mat4 localTransform);

	void setLocalTranslate(Mat4 localTranslate);

	void setLocalRotate(Mat4 localRotate);

	void setLocalScale(Mat4 localScale);

	void composeLocalMatrix();

	void computeWorldMatrix();

	void decomposeWorldMatrix();

	Vec3 getFwd();

	Vec3 getRight();

	Vec3 getUp();

	void decomposeLocalMatrix();

}
