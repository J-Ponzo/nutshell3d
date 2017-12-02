package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMirror;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class Mirror extends AbstractComponent implements IMirror {
	private IMaterial material;

	public Mirror(IEntity entity) {
		super(entity);
	}

	@Override
	public IMaterial getMaterial() {
		return material;
	}

	@Override
	public void setMaterial(IMaterial material) {
		this.material = material;
	}

	@Override
	public Mat4 getViewMatrix(Mat4 eyeTransform) {
		//Compute mirror view matrix and inverse
		ITransform mirrorTrans = entity.getTransforms().get(0);

		Vec4 col3 = mirrorTrans.getWorldTranslate().getColumn(3);
		Vec3 eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
		Vec3 center = eye.add(mirrorTrans.getFwd());
		Vec3 up = mirrorTrans.getUp();
		Mat4 mirrorViewMat = Matrices.lookAt(eye, center, up);
		Mat4 mirrorInvViewMat = Matrices.invert(mirrorViewMat);
		
		//Compute basis change
		Mat4 mirrorCamViewMat = mirrorInvViewMat
		.multiply(Matrices.scale(1f, 1f, -1f))
		.multiply(mirrorViewMat)
		.multiply(eyeTransform);
		
		return mirrorCamViewMat;
	}
}
