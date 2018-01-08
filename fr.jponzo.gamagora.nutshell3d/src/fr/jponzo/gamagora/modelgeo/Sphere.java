package fr.jponzo.gamagora.modelgeo;

import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class Sphere {
	private Vec3 origin;
	private float radius;
	
	public Vec3 getOrigin() {
		return origin;
	}
	public void setOrigin(Vec3 origin) {
		this.origin = origin;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	public Sphere(Vec3 origin, float radius) {
		super();
		this.origin = origin;
		this.radius = radius;
	}
}
