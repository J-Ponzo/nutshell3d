package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IPortal;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class Portal extends AbstractComponent implements IPortal {
	private IMaterial material;
	private IPortal target;

	public Portal(IEntity entity) {
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
	public IPortal getTarget() {
		return target;
	}

	@Override
	public void setTarget(IPortal target) {
		this.target = target;
	}
	
	@Override
	public Mat4 getViewMatrix(Mat4 eyeMat) {
		//		//Compute source portal view matrix and inverse
		//		ITransform srcTrans = entity.getTransforms().get(0);
		//		Mat4 srcT =  new Mat4(srcTrans.getWorldTranslate());
		//		Mat4 srcR = new Mat4(srcTrans.getWorldRotate());
		//		Mat4 srcTInv = Matrices.invert(srcT);
		//		Mat4 srcRInv = Matrices.invert(srcR);
		//
		//		//Compute target portal view matrix and inverse
		//		ITransform trgTrans = target.getEntity().getTransforms().get(0);;
		//		Mat4 trgT =  new Mat4(trgTrans.getWorldTranslate());
		//		Mat4 trgR =  new Mat4(trgTrans.getWorldRotate());
		//
		//		//Compute switch matrix
		//		Mat4 switchT = srcTInv.multiply(trgT);
		//		Mat4 switchR = srcRInv.multiply(trgR);
		//		Mat4 switchMat = switchT.multiply(switchR);

		//Compute source portal view matrix and inverse
		ITransform srcTrans = entity.getTransforms().get(0);

		Vec4 col3 = srcTrans.getWorldTranslate().getColumn(3);
		Vec3 eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
		Vec3 center = eye.add(srcTrans.getFwd());
		Vec3 up = new Vec3(0f, 1f, 0f);
		Mat4 srcViewMat = Matrices.lookAt(eye, center, up);
		Mat4 srcInvViewMat = Matrices.invert(srcViewMat);

		//Compute target portal view matrix and inverse
		ITransform trgTrans = target.getEntity().getTransforms().get(0);

		col3 = trgTrans.getWorldTranslate().getColumn(3);
		eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
		center = eye.add(trgTrans.getFwd());
		up = new Vec3(0f, 1f, 0f);
		Mat4 trgViewMat = Matrices.lookAt(eye, center, up);
		Mat4 trgInvViewMat = Matrices.invert(srcViewMat);

		
		
		//Compute basis change
		Mat4 switchMat = srcInvViewMat.multiply(trgViewMat);
		Mat4 trgEyeMat = switchMat.multiply(eyeMat);
		
		Vec3 correction = trgTrans.getFwd().multiply(3f);
		trgEyeMat = Matrices.translation(correction.getX(), correction.getY(), correction.getZ()).multiply(trgEyeMat);

		return trgEyeMat;
	}

	//	@Override
	//	public Mat4 getViewMatrix(Mat4 eyeMat) {
	//		//		//Compute source portal view matrix and inverse
	//		//		ITransform srcTrans = entity.getTransforms().get(0);
	//		//
	//		//		Vec4 col3 = srcTrans.getWorldTranslate().getColumn(3);
	//		//		Vec3 eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
	//		//		Vec3 center = eye.add(srcTrans.getFwd());
	//		//		Vec3 up = srcTrans.getUp();
	//		//		Mat4 srcViewMat = Matrices.lookAt(eye, center, up);
	//		//		Mat4 srcInvViewMat = Matrices.invert(srcViewMat);
	//		//
	//		//		//Compute target portal view matrix and inverse
	//		//		ITransform trgTrans = target.getEntity().getTransforms().get(0);
	//		//
	//		//		col3 = trgTrans.getWorldTranslate().getColumn(3);
	//		//		eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
	//		//		center = eye.add(trgTrans.getFwd());
	//		//		up = trgTrans.getUp();
	//		//		Mat4 trgViewMat = Matrices.lookAt(eye, center, up);
	//		//		Mat4 trgInvViewMat = Matrices.invert(srcViewMat);
	//
	//		//Compute source portal view matrix and inverse
	//		ITransform srcTrans = entity.getTransforms().get(0);
	//		Mat4 srcViewMat = srcTrans.getWorldTransform();
	//		Mat4 srcInvViewMat = Matrices.invert(srcViewMat);
	//
	//		//Compute target portal view matrix and inverse
	//		ITransform trgTrans = target.getEntity().getTransforms().get(0);;
	//		Mat4 trgViewMat = trgTrans.getWorldTransform();
	//		Mat4 trgInvViewMat = Matrices.invert(trgViewMat);
	//
	//		//Compute basis change
	//		Mat4 trgEyeMat = eyeMat
	//				.multiply(srcViewMat)
	//				.multiply(Matrices.rotate((float)Math.PI, new Vec3(0f, 1f, 0f)))
	//				.multiply(trgInvViewMat);
	//
	//		return trgEyeMat;
	//	}
}
