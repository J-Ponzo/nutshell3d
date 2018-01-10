package fr.jponzo.gamagora.modelgeo.tpsub;

import java.util.ArrayList;
import java.util.List;

import fr.jponzo.gamagora.modelgeo.tp5.CurveBezier;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class ChaikinCurve extends CurveBezier {

	public ChaikinCurve(IEntity entity) {
		super(entity);
	}

	@Override
	public void updateFromControl() {
		List<Vec3> ci;
		List<Vec3> cim1 = new ArrayList<Vec3>(controlPts);
		for (int i = 0; i < discrtisation; i++) {
			ci = new ArrayList<Vec3>();
			for (int k = 0; k < cim1.size() - 1; k++) {
				Vec3 a = cim1.get(k);
				Vec3 b = cim1.get(k + 1);
				Vec3 p1 = a.multiply(3f / 4f).add(b.multiply(1f / 4f));
				Vec3 p2 = a.multiply(1f / 4f).add(b.multiply(3f / 4f));
				ci.add(p1);
				ci.add(p2);
			}
			Vec3 a = cim1.get(cim1.size() - 1);
			Vec3 b = cim1.get(0);
			Vec3 p1 = a.multiply(3f / 4f).add(b.multiply(1f / 4f));
			Vec3 p2 = a.multiply(1f / 4f).add(b.multiply(3f / 4f));
			ci.add(p1);
			ci.add(p2);
			
			cim1 = ci;
		}
		
		points = new ArrayList<Vec3>(cim1);
	}
	
	@Override
	public float[][] getControlPtsTable() {
		float[][] table = new float[controlPts.size() + 1][3];
		
		for (int i = 0; i < controlPts.size(); i++) {
			table[i][0] = controlPts.get(i).getX();
			table[i][1] = controlPts.get(i).getY();
			table[i][2] = controlPts.get(i).getZ();
		}
		table[controlPts.size()][0] = controlPts.get(0).getX();
		table[controlPts.size()][1] = controlPts.get(0).getY();
		table[controlPts.size()][2] = controlPts.get(0).getZ();
		
		return table;
	}
	
	@Override
	public float[][] getPtsTable() {
		float[][] table = new float[points.size() + 1][3];
		
		for (int i = 0; i < points.size(); i++) {
			table[i][0] = points.get(i).getX();
			table[i][1] = points.get(i).getY();
			table[i][2] = points.get(i).getZ();
		}
		table[points.size()][0] = points.get(0).getX();
		table[points.size()][1] = points.get(0).getY();
		table[points.size()][2] = points.get(0).getZ();
		
		return table;
	}
}
