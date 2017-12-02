package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import java.io.Serializable;

public interface IMeshDef extends Serializable {
	float[][] getPosTable();

	float[][] getColTable();

	float[][] getOffTable();
	
	float[][] getNorTable();

	int[][] getIdxTable();

	void setPath(String meshPath);
	
	String getPath();

	void load();
}
