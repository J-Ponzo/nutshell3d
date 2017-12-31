package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.awt.Rectangle;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class Camera extends AbstractComponent implements ICamera {
	//TODO duplicate info with viewport
	private float width;
	private float height;
	private float near;
	private float far;
	private float fov;
	private Rectangle viewport;
	private boolean isOrtho;
	private boolean[] layers = new boolean[RenderingSystem.NB_CAM_LAYERS];

	private IMaterial material;

	public Camera(IEntity entity) {
		super(entity);
		for (int i = 0; i < RenderingSystem.NB_CAM_LAYERS; i++) {
			layers[i] = true;
		}
	}
	
	@Override
	public void enableLayer(int layer) {
		layers[layer] = true;
	}
	
	@Override
	public void disableLayer(int layer) {
		layers[layer] = false;
	}
	
	@Override
	public boolean isEnabledLayer(int layer) {
		return layers[layer];
	}
	
	@Override
	public float getWidth() {
		return width;
	}
	
	@Override
	public void setWidth(float width) {
		this.width = width;
	}
	
	@Override
	public float getHeight() {
		return height;
	}
	
	@Override
	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public float getNear() {
		return near;
	}
	
	@Override
	public void setNear(float near) {
		this.near = near;
	}
	
	@Override
	public float getFar() {
		return far;
	}
	
	@Override
	public void setFar(float far) {
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
		return Matrices.perspective(fov, width / height, near, far);
	}
}
