package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.awt.Rectangle;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class Camera extends AbstractComponent implements ICamera {
	//TODO duplicate info with viewport
	private int width;
	private int height;
	private int near;
	private int far;
	private float fov;
	private Rectangle viewport;
	private boolean isOrtho;

	private IMaterial material;

	public Camera(IEntity entity) {
		super(entity);
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public void setWidth(int width) {
		this.width = width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public int getNear() {
		return near;
	}
	
	@Override
	public void setNear(int near) {
		this.near = near;
	}
	
	@Override
	public int getFar() {
		return far;
	}
	
	@Override
	public void setFar(int far) {
		this.far = far;
	}
	
	@Override
	public float getFov() {
		return fov;
	}
	
	@Override
	public void setFov(float fov) {
		this.fov = fov;
	}
	
	@Override
	public Rectangle getViewport() {
		return viewport;
	}
	
	@Override
	public void setViewport(Rectangle viewPort) {
		this.viewport = viewPort;
	}
	
	@Override
	public boolean isOrtho() {
		return isOrtho;
	}
	
	@Override
	public void setOrtho(boolean isOrtho) {
		this.isOrtho = isOrtho;
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
	public Mat4 getViewMatrix() {
		ITransform transform = entity.getTransforms().get(0);
		Vec4 col3 = transform.getWorldTranslate().getColumn(3);
		Vec3 eye = new Vec3(col3.getX(), col3.getY(), col3.getZ());
		Vec3 center = eye.add(transform.getFwd());
		Vec3 up = transform.getUp();
		
		return Matrices.lookAt(eye, center, up);
	}

	@Override
	public Mat4 getProjMatrix() {
		return Matrices.perspective((float) fov, (float) width / (float) height, (float) near, (float) far);
	}
}
