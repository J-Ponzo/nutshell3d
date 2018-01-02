package fr.jponzo.gamagora.nutshell3d.scene.impl;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IPortal;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class PortalUpdator extends AbstractUpdator {

	public PortalUpdator(IEntity entity, IEntity cameraEntity) {
		super(entity);
		this.cameraEntity = cameraEntity;
	}

	private float lastDot = 0;
	private boolean crossedThisFrame = false;
	private long totalTime;
	private IEntity cameraEntity;
	
	@Override
	public void init() {
		
	}

	@Override
	public void update(long deltaTime) {
		totalTime += deltaTime;
		
		IEntity source = entity.getPortals().get(0).getEntity();
		IEntity target = entity.getPortals().get(0).getTarget().getEntity();
		
		IMaterial portalMat = target.getPortals().get(0).getMaterial();
		portalMat.setFloatParam("mat_time", (float) totalTime / 1000f);
		portalMat.setFloatParam("mat_periode", 500);
		portalMat.setFloatParam("mat_amplitude", 50f);
		
		Vec4 mirPos4 = entity.getTransforms().get(0).getWorldTranslate().getColumn(3);
		Vec4 camPos4 = cameraEntity.getTransforms().get(0).getWorldTranslate().getColumn(3);
		
		Vec3 M = new Vec3(mirPos4.getX(), mirPos4.getY(), mirPos4.getZ());
		Vec3 A = new Vec3(camPos4.getX(), camPos4.getY(), camPos4.getZ());
		Vec3 n = entity.getTransforms().get(0).getFwd();
		Vec3 MA = A.subtract(M);
	
		float newDot = n.dot(MA);
		if (lastDot != 0 && lastDot * newDot < 0 && MA.getLength() < 0.5) {
			//Disable target
			PortalUpdator trgUpdator = (PortalUpdator) target.getUpdators().get(0);
			trgUpdator.lastDot = 0;
			trgUpdator.crossedThisFrame = true;
			this.lastDot = 0;
			
			//Set mirror cam transform
			IPortal portal = source.getPortals().get(0);
			ITransform camTransform = cameraEntity.getTransforms().get(0);
			camTransform.setLocalTransform(portal.getViewMatrix(camTransform.getWorldTransform()));
			
			return;
		}
		if (crossedThisFrame) {
			crossedThisFrame = false;
		} else {
			lastDot = newDot;
		}
	}
}
