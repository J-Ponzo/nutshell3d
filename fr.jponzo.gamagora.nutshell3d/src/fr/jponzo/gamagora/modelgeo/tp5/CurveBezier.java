package fr.jponzo.gamagora.modelgeo.tp5;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractComponent;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class CurveBezier extends AbstractComponent implements ICurve {
	private List<Vec3> controlPts = new ArrayList<Vec3>();
	private List<Vec3> points = new ArrayList<Vec3>();
	private float discrtisation;
	
	IMaterial material;
	
	public CurveBezier(IEntity entity) {
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
		int n = controlPts.size();
		Vec3 pt = new Vec3(0f, 0f, 0f);
		for (int i = 0; i < n; i++) {
			pt = pt.add(controlPts.get(i).multiply(berstein(i, n - 1, t)));
		}
		return pt;
	}

	private float berstein(int i, int n, float t) {
		float result = ((float) fact(n) / ((float) fact(i) * (float) fact(n - i))) * (float) Math.pow(t, i) * (float) Math.pow(1 - t, n - i);
		return result;
	}

	private int fact(int n) {
		int result = 1;
		for (int i = 2; i <= n; i++) {
			result *= i;
		}
		
		return result;
	}

	@Override
	public void moveControlPoint(int index, Vec3 newPos) {
		controlPts.set(index, newPos);
	}
}
