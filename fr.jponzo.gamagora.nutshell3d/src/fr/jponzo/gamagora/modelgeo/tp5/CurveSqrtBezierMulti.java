package fr.jponzo.gamagora.modelgeo.tp5;

import java.util.ArrayList;
import java.util.List;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractComponent;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class CurveSqrtBezierMulti extends AbstractComponent implements ICurve {
	private List<Vec3> controlPts = new ArrayList<Vec3>();
	private List<Vec3> points = new ArrayList<Vec3>();
	private float discrtisation;

	IMaterial material;

	public CurveSqrtBezierMulti(IEntity entity) {
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
		//Compute parts
		List<List<Vec3>> pointsParts = new ArrayList<List<Vec3>>();
		for (int i = 0; i < controlPts.size() - 3; i += 3) {
			//Build part
			List<Vec3> controlPart = new ArrayList<Vec3>();
			controlPart = controlPts.subList(i, i + 4);
			pointsParts.add(computePartFromControl(controlPart, discrtisation));
		}

		//Concat parts
		points.clear();
		points.add(controlPts.get(0));
		for (List<Vec3> part : pointsParts) {
			points.addAll(part.subList(1, part.size()));
		}
	}

	@Override
	public void moveControlPoint(int index, Vec3 newPos) {
		Vec3 odlPos = controlPts.get(index);
		controlPts.set(index, newPos);

		//Move merged point
		if (index == 0 || index == 3 || index == 6) {
			int leftInd = index - 1;
			int rightInd = index + 1;
			Vec3 v = newPos.subtract(odlPos);
			if (leftInd > 0) {
				Vec3 leftPos = controlPts.get(leftInd);
				controlPts.set(leftInd, leftPos.add(v));
			}
			if (rightInd < controlPts.size()) {
				Vec3 rightPos = controlPts.get(rightInd);
				controlPts.set(rightInd, rightPos.add(v));
			}
		} 
		//Move right point
		else if (index == 1 || index == 4) {
			int leftInd = index - 2;
			int pivotInd = index - 1;
			if (leftInd > 0) {
				Vec3 pos = controlPts.get(index);
				Vec3 pivot = controlPts.get(pivotInd);
				Vec3 v = pivot.subtract(pos);
				controlPts.set(leftInd, pivot.add(v));
				
			}
		}
		//Move left point
		else {
			int rightInd = index + 2;
			int pivotInd = index + 1;
			if (rightInd < controlPts.size()) {
				Vec3 pos = controlPts.get(index);
				Vec3 pivot = controlPts.get(pivotInd);
				Vec3 v = pivot.subtract(pos);
				controlPts.set(rightInd, pivot.add(v));
			}
		}
	}

	private List<Vec3> computePartFromControl (List<Vec3> partControlPts, float partDisc) {
		List<Vec3> partPoints = new ArrayList<Vec3>();

		Vec3 firstPt = partControlPts.get(0);
		Vec3 lastPt = partControlPts.get(partControlPts.size() - 1);
		partPoints.add(new Vec3(firstPt.getX(), firstPt.getY(), firstPt.getZ()));
		for (float t = 1f / partDisc; t < 1; t += 1f / partDisc) {
			Vec3 pt = computePoint(t, partControlPts);
			partPoints.add(pt);
		}
		partPoints.add(new Vec3(lastPt.getX(), lastPt.getY(), lastPt.getZ()));

		return partPoints;
	}

	private Vec3 computePoint(float t, List<Vec3> partControlPts) {
		int n = partControlPts.size();
		Vec3 pt = new Vec3(0f, 0f, 0f);
		for (int i = 0; i < n; i++) {
			pt = pt.add(partControlPts.get(i).multiply(berstein(i, n - 1, t)));
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
}
