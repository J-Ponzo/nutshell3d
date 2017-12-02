package fr.jponzo.gamagora.modelgeo.tp5;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractComponent;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class CurveHermite extends AbstractComponent implements ICurve {
	private List<Vec3> controlPts = new ArrayList<Vec3>();
	private List<Vec3> points = new ArrayList<Vec3>();
	private float discrtisation;
	private Vec3 p0;
	private Vec3 p1;
	private Vec3 v0;
	private Vec3 v1;
	
	IMaterial material;
	
	public CurveHermite(IEntity entity) {
		super(entity);
	}

	@Override
	public List<Vec3> getControlPts() {
		return controlPts;
	}

	@Override
	public void setControlPts(List<Vec3> controlPts) {
		this.controlPts = controlPts;
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
	public float getDiscrtisation() {
		return discrtisation;
	}

	@Override
	public void setDiscrtisation(float discrtisation) {
		this.discrtisation = discrtisation;
	}

	@Override
	public float[][] getControlPtsTable() {
		float[][] table = new float[controlPts.size()][3];
		
		for (int i = 0; i < controlPts.size(); i++) {
			table[i][0] = controlPts.get(i).getX();
			table[i][1] = controlPts.get(i).getY();
			table[i][2] = controlPts.get(i).getZ();
		}
		
		return table;
	}

	@Override
	public float[][] getPtsTable() {
		float[][] table = new float[points.size()][3];
		
		for (int i = 0; i < points.size(); i++) {
			table[i][0] = points.get(i).getX();
			table[i][1] = points.get(i).getY();
			table[i][2] = points.get(i).getZ();
		}
		
		return table;
	}
	
	@Override
	public void updateFromControl() {
		points.clear();
		Vec3 firstPt = controlPts.get(0);
		Vec3 lastPt = controlPts.get(controlPts.size() - 1);
		points.add(new Vec3(firstPt.getX(), firstPt.getY(), firstPt.getZ()));
		for (float t = 1f / discrtisation; t < 1; t += 1f / discrtisation) {
			Vec3 pt = computePoint(t);
			points.add(pt);
		}
		points.add(new Vec3(lastPt.getX(), lastPt.getY(), lastPt.getZ()));
	}
	
	private Vec3 computePoint(float t) {
		float f1 = 2 * t *t *t - 3 * t * t + 1; 
		float f2 = -2 * t *t *t + 3 * t * t; 
		float f3 = t * t * t - 2 * t * t + t; 
		float f4 = t * t * t - t * t; 
		Vec3 pt = p0.multiply(f1).add(p1.multiply(f2)).add(v0.multiply(f3)).add(v1.multiply(f4));
		
		return pt;
	}

	@Override
	public void moveControlPoint(int index, Vec3 newPos) {
		controlPts.set(index, newPos);
	}

	private void updateControlFromParam() {
		if (p0 != null && p1 != null && v0 != null && v1 != null) {
			controlPts.clear();
			controlPts.add(p0);
			controlPts.add(p0.add(v0));
			controlPts.add(p1.add(v1));
			controlPts.add(p1);
		}
	}
	
	public void setP0(Vec3 vec3) {
		p0 = vec3;
		updateControlFromParam();
	}

	public void setP1(Vec3 vec3) {
		p1 = vec3;
		updateControlFromParam();
	}

	public void setV0(Vec3 vec3) {
		v0 = vec3;
		updateControlFromParam();
	}

	public void setV1(Vec3 vec3) {
		v1 = vec3;
		updateControlFromParam();
	}
}
