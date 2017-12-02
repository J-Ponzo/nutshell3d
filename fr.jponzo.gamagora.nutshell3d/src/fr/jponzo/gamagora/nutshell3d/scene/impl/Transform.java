package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class Transform extends AbstractComponent implements ITransform {

	private Mat4 localTransform = Mat4.MAT4_IDENTITY;
	private Mat4 localTranslate = Mat4.MAT4_IDENTITY;
	private Mat4 localRotate = Mat4.MAT4_IDENTITY;
	private Mat4 localScale = Mat4.MAT4_IDENTITY;

	private Mat4 worldTransform = Mat4.MAT4_IDENTITY;
	private Mat4 worldTranslate = Mat4.MAT4_IDENTITY;
	private Mat4 worldRotate = Mat4.MAT4_IDENTITY;
	private Mat4 worldScale = Mat4.MAT4_IDENTITY;

	public Transform(IEntity entity) {
		super(entity);
	}

	@Override
	public Vec3 getFwd() {
		Vec4 k = new Vec4(0f, 0f, 1f, 0f);
		Vec4 fwd = worldRotate.multiply(k);
		return new Vec3(fwd.getX(), fwd.getY(), fwd.getZ());
	}
	
	@Override
	public Vec3 getRight() {
		Vec4 i = new Vec4(1f, 0f, 0f, 0f);
		Vec4 right = worldRotate.multiply(i);
		return new Vec3(right.getX(), right.getY(), right.getZ());
	}
	
	@Override
	public Vec3 getUp() {
		Vec4 j = new Vec4(0f, 1f, 0f, 0f);
		Vec4 up = worldRotate.multiply(j);
		return new Vec3(up.getX(), up.getY(), up.getZ());
	}
	
	@Override
	public void setWorldTransform(Mat4 worldTransform) {
		this.worldTransform = worldTransform;
		decomposeWorldMatrix();
	}

	@Override
	public void setWorldTranslate(Mat4 worldTranslate) {
		this.worldTranslate = worldTranslate;
	}

	@Override
	public void setWorldRotate(Mat4 worldRotate) {
		this.worldRotate = worldRotate;
	}

	@Override
	public void setWorldScale(Mat4 worldScale) {
		this.worldScale = worldScale;
	}
	
	@Override
	public Mat4 getWorldTransform() {
		return worldTransform;
	}

	@Override
	public Mat4 getWorldTranslate() {
		return worldTranslate;
	}

	@Override
	public Mat4 getWorldRotate() {
		return worldRotate;
	}

	@Override
	public Mat4 getWorldScale() {
		return worldScale;
	}

	@Override
	public void setLocalTransform(Mat4 localTransform) {
		this.localTransform = localTransform;
		decomposeLocalMatrix();
	}

	@Override
	public void setLocalTranslate(Mat4 localTranslate) {
		this.localTranslate = localTranslate;
	}

	@Override
	public void setLocalRotate(Mat4 localRotate) {
		this.localRotate = localRotate;
	}

	@Override
	public void setLocalScale(Mat4 localScale) {
		this.localScale = localScale;
	}
	
	@Override
	public Mat4 getLocalTranslate() {
		return localTranslate;
	}

	@Override
	public Mat4 getLocalRotate() {
		return localRotate;
	}

	@Override
	public Mat4 getLocalScale() {
		return localScale;
	}

	/**
	 * Compute local transform matrix from local translate, rotate and scale matrices 
	 */
	@Override
	public void composeLocalMatrix() {
		localTransform = Mat4.MAT4_IDENTITY;

		localTransform = localTransform.multiply(localTranslate);
		localTransform = localTransform.multiply(localRotate);
		localTransform = localTransform.multiply(localScale);
	}

	/**
	 * Compute world transform matrix from parent
	 */
	@Override
	public void computeWorldMatrix() {
		if (entity.getParent() != null) {
			ITransform parentTransform = entity.getParent().getTransforms().get(0);
			worldTransform = parentTransform.getWorldTransform().multiply(localTransform);
		}
	}

	@Override
	public void decomposeLocalMatrix() {
		//Compute Translate
		float tx = ((Vec4)localTransform.getColumn(3)).getX();
		float ty = ((Vec4)localTransform.getColumn(3)).getY();
		float tz = ((Vec4)localTransform.getColumn(3)).getZ();
		localTranslate = Matrices.translation(tx, ty, tz);

		//Compute Scale
		Vec3 vec = new Vec3(
				((Vec4)localTransform.getColumn(0)).getX(),
				((Vec4)localTransform.getColumn(0)).getY(), 
				((Vec4)localTransform.getColumn(0)).getZ());
		float sx = vec.getLength();
		vec = new Vec3(
				((Vec4)localTransform.getColumn(1)).getX(),
				((Vec4)localTransform.getColumn(1)).getY(), 
				((Vec4)localTransform.getColumn(1)).getZ());
		float sy = vec.getLength();
		vec = new Vec3(
				((Vec4)localTransform.getColumn(2)).getX(),
				((Vec4)localTransform.getColumn(2)).getY(), 
				((Vec4)localTransform.getColumn(2)).getZ());
		float sz = vec.getLength();
		localScale = Matrices.scale(sx, sy, sz);

		//Compute Rotation
		float a = ((Vec4)localTransform.getColumn(0)).getX() / sx;
		float e = ((Vec4)localTransform.getColumn(0)).getY() / sx;
		float i = ((Vec4)localTransform.getColumn(0)).getZ() / sx;

		float b = ((Vec4)localTransform.getColumn(1)).getX() / sy;
		float f = ((Vec4)localTransform.getColumn(1)).getY() / sy;
		float j = ((Vec4)localTransform.getColumn(1)).getZ() / sy;

		float c = ((Vec4)localTransform.getColumn(2)).getX() / sz;
		float g = ((Vec4)localTransform.getColumn(2)).getY() / sz;
		float k = ((Vec4)localTransform.getColumn(2)).getZ() / sz;

		float[] buffer3 = {
				a, e, i, 0,
				b, f, j, 0,
				c, g, k, 0,
				0, 0, 0, 1
		};
		localRotate = new Mat4(buffer3);
	}
	
	/**
	 * Compute the world translate, rotate and scale matrices from the world transform
	 */
	@Override
	public void decomposeWorldMatrix() {
		//Compute Translate
		float tx = ((Vec4)worldTransform.getColumn(3)).getX();
		float ty = ((Vec4)worldTransform.getColumn(3)).getY();
		float tz = ((Vec4)worldTransform.getColumn(3)).getZ();
		worldTranslate = Matrices.translation(tx, ty, tz);

		//Compute Scale
		Vec3 vec = new Vec3(
				((Vec4)worldTransform.getColumn(0)).getX(),
				((Vec4)worldTransform.getColumn(0)).getY(), 
				((Vec4)worldTransform.getColumn(0)).getZ());
		float sx = vec.getLength();
		vec = new Vec3(
				((Vec4)worldTransform.getColumn(1)).getX(),
				((Vec4)worldTransform.getColumn(1)).getY(), 
				((Vec4)worldTransform.getColumn(1)).getZ());
		float sy = vec.getLength();
		vec = new Vec3(
				((Vec4)worldTransform.getColumn(2)).getX(),
				((Vec4)worldTransform.getColumn(2)).getY(), 
				((Vec4)worldTransform.getColumn(2)).getZ());
		float sz = vec.getLength();
		worldScale = Matrices.scale(sx, sy, sz);

		//Compute Rotation
		float a = ((Vec4)worldTransform.getColumn(0)).getX() / sx;
		float e = ((Vec4)worldTransform.getColumn(0)).getY() / sx;
		float i = ((Vec4)worldTransform.getColumn(0)).getZ() / sx;

		float b = ((Vec4)worldTransform.getColumn(1)).getX() / sy;
		float f = ((Vec4)worldTransform.getColumn(1)).getY() / sy;
		float j = ((Vec4)worldTransform.getColumn(1)).getZ() / sy;

		float c = ((Vec4)worldTransform.getColumn(2)).getX() / sz;
		float g = ((Vec4)worldTransform.getColumn(2)).getY() / sz;
		float k = ((Vec4)worldTransform.getColumn(2)).getZ() / sz;

		float[] buffer3 = {
				a, e, i, 0,
				b, f, j, 0,
				c, g, k, 0,
				0, 0, 0, 1
		};
		worldRotate = new Mat4(buffer3);
	}
}
