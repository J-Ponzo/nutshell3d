package fr.jponzo.gamagora.modelgeo;

import java.util.ArrayList;

public class LODMeshDef extends MutableMeshDef {
	private int disc = 64;
	private float[][] initPosTable = new float[0][3];
	private float[][] initColTable = new float[0][3];
	private float[][] initOffTable = new float[0][3];
	private float[][] initNorTable = new float[0][3];
	private int[][] initIdxTable = new int[0][3];

	@Override
	public void load() {
		super.load();
		update();
	}

	private void update() {
		//Init
		initPosTable =  new float[posTable.length][3];
		for (int i = 0; i < posTable.length; i++) {
			initPosTable[i][0] = posTable[i][0];
			initPosTable[i][1] = posTable[i][1];
			initPosTable[i][2] = posTable[i][2];
		}
		initColTable = colTable;
		initOffTable = offTable;
		initNorTable = norTable;
		initIdxTable = idxTable;
		
		//Sort
		ArrayList<Integer>[][][] grid = (ArrayList<Integer>[][][])new ArrayList[disc][disc][disc];
		for (int i = 0; i < disc; i++) {
			for (int j = 0; j < disc; j++) {
				for (int k = 0; k < disc; k++) {
					grid[i][j][k] = new ArrayList<Integer>();
					for (int v = 0; v < posTable.length; v++) {
						if (vectorIsInCase(v, i, j, k)) {
							grid[i][j][k].add(v);
						}
					}
				}
			}
		}

		//Merge
		for (int i = 0; i < disc; i++) {
			for (int j = 0; j < disc; j++) {
				for (int k = 0; k < disc; k++) {
					if (grid[i][j][k].size() > 0) {
						int ind = grid[i][j][k].get(0);
						float x = posTable[ind][0];
						float y = posTable[ind][1];
						float z = posTable[ind][2];
						for (int v = 1; v < grid[i][j][k].size(); v++) {
							ind = grid[i][j][k].get(v);
							x += posTable[ind][0];
							y += posTable[ind][1];
							z += posTable[ind][2];
						}
						x /= grid[i][j][k].size();
						y /= grid[i][j][k].size();
						z /= grid[i][j][k].size();
						for (int v = 0; v < grid[i][j][k].size(); v++) {
							ind = grid[i][j][k].get(v);
							initPosTable[ind][0] = x;
							initPosTable[ind][1] = y;
							initPosTable[ind][2] = z;
						}
					}
				}
			}
		}
		
		System.out.println("Update to " + disc);
	}

	private boolean vectorIsInCase(int v, int i, int j, int k) {
		float grideSize = 2f;
		float caseSize = grideSize / (float) disc;
		float posX = i * caseSize - (grideSize/2f);
		float posY = j * caseSize - (grideSize/2f);
		float posZ = k * caseSize - (grideSize/2f);
		float left = posX - caseSize / 2;
		float right = posX + caseSize / 2;
		float top = posY + caseSize / 2;
		float down = posY - caseSize / 2;
		float fwd = posZ + caseSize / 2;
		float bck = posZ - caseSize / 2;

		if (posTable[v][0] < left || posTable[v][0] > right
				|| posTable[v][1] > top || posTable[v][1] < down
				|| posTable[v][2] > fwd || posTable[v][2] < bck) {
			return false;
		}
		return true;
	}

	public int getDisc() {
		return disc;
	}

	public void setDisc(int disc) {
		this.disc = disc;
		update();
	}

	public float[][] getPosTable() {
		return initPosTable;
	}

	@Override
	public int[][] getIdxTable() {
		return initIdxTable;
	}

	@Override
	public float[][] getColTable() {
		return initColTable;
	}

	@Override
	public float[][] getOffTable() {
		return initOffTable;
	}

	@Override
	public float[][] getNorTable() {
		return initNorTable;
	}
}
