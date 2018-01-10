package fr.jponzo.gamagora.modelgeo;

import java.util.ArrayList;
import java.util.List;

import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class LODMeshDef extends MutableMeshDef {
	private int disc = 64;
	private boolean isMerge = false;
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
	
	public boolean isMerge() {
		return isMerge;
	}



	public void setMerge(boolean isMerge) {
		this.isMerge = isMerge;
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
		initNorTable =  new float[norTable.length][3];
		for (int i = 0; i < norTable.length; i++) {
			initNorTable[i][0] = norTable[i][0];
			initNorTable[i][1] = norTable[i][1];
			initNorTable[i][2] = norTable[i][2];
		}
		initIdxTable =  new int[idxTable.length][3];
		for (int i = 0; i < idxTable.length; i++) {
			initIdxTable[i][0] = idxTable[i][0];
			initIdxTable[i][1] = idxTable[i][1];
			initIdxTable[i][2] = idxTable[i][2];
		}
		
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
						if (isMerge) {
							mergeVertices(grid[i][j][k]);
						}
					}
				}
			}
		}
		if (isMerge) {
			generateNormalsOnLoadMesh();
		}
		
		System.out.println("Update to " + disc);
	}

	private void mergeVertices(ArrayList<Integer> mergedVecs) {
		for (int i = 0; i < initIdxTable.length; i++) {
			for (int j = 0; j < mergedVecs.size(); j++) {
				if (initIdxTable[i][0] == mergedVecs.get(j)) {
					initIdxTable[i][0] = mergedVecs.get(0);
				}
				if (initIdxTable[i][1] == mergedVecs.get(j)) {
					initIdxTable[i][1] = mergedVecs.get(0);
				}
				if (initIdxTable[i][2] == mergedVecs.get(j)) {
					initIdxTable[i][2] = mergedVecs.get(0);
				}
			}
		}
		
		int[] mergedVecCount = new int[initIdxTable.length];
		for (int i = 0; i < mergedVecs.size(); i++) {
			int[] faces = findFace(mergedVecs.get(i));
			for (int j = 0; i < faces.length; i++) {
				mergedVecCount[faces[j]]++;
			}
		}
		
		for (int i = 0; i < mergedVecCount.length; i++) {
			if (mergedVecCount[i] == 3) {
				deleteFaceOnLodMesh(i);
			}
		}
	}

	/**
	 * Generate position normals from topology
	 */
	private void generateNormalsOnLoadMesh() {
		//Init tris normals
		Vec3[] trisNor = new Vec3[initIdxTable.length];
		for (int i = 0; i < initIdxTable.length; i++) {
			int p1 = initIdxTable[i][0];
			int p2 = initIdxTable[i][1];
			int p3 = initIdxTable[i][2];

			Vec3 v1 = new Vec3(initPosTable[p1][0], initPosTable[p1][1], initPosTable[p1][2]);
			Vec3 v2 = new Vec3(initPosTable[p2][0], initPosTable[p2][1], initPosTable[p2][2]);
			Vec3 v3 = new Vec3(initPosTable[p3][0], initPosTable[p3][1], initPosTable[p3][2]);

			Vec3 v1v2 = v2.subtract(v1);
			Vec3 v1v3 = v3.subtract(v1);

			Vec3 n = v1v2.cross(v1v3).getUnitVector();
			trisNor[i] = n;
		}

		//Init position adjacences table (pos => faces)
		List<Integer>[] posAdj = new List[initPosTable.length];
		for (int i = 0; i < initPosTable.length; i++) {
			posAdj[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < initIdxTable.length; i++) {
			int p1 = initIdxTable[i][0];
			int p2 = initIdxTable[i][1];
			int p3 = initIdxTable[i][2];

			posAdj[p1].add(i);
			posAdj[p2].add(i);
			posAdj[p3].add(i);
		}

		//Compute normals
		initNorTable = new float[initPosTable.length][3];
		for (int i = 0; i < initPosTable.length; i++) {
			Vec3 n = new Vec3(0f, 0f, 0f);
			for (int j = 0; j < posAdj[i].size(); j++) {
				int triNorInd = posAdj[i].get(j);
				n = n.add(trisNor[triNorInd]);
			}
			n = n.getUnitVector();
			initNorTable[i][0] = n.getX();
			initNorTable[i][1] = n.getY();
			initNorTable[i][2] = n.getZ();
		}
	}
	
	public void deleteFaceOnLodMesh(int faceInd) {
		//save pos indices
		int p1 = initIdxTable[faceInd][0];
		int p2 = initIdxTable[faceInd][1];
		int p3 = initIdxTable[faceInd][2];

		//Remove face
		int[][] newIdxTable = new int[initIdxTable.length - 1][3];
		int ind = 0;
		for (int i = 0; i < faceInd; i++) {
			newIdxTable[ind][0] = initIdxTable[i][0];
			newIdxTable[ind][1] = initIdxTable[i][1];
			newIdxTable[ind][2] = initIdxTable[i][2];
			ind++;
		}
		for (int i = faceInd + 1; i < initIdxTable.length; i++) {
			newIdxTable[ind][0] = initIdxTable[i][0];
			newIdxTable[ind][1] = initIdxTable[i][1];
			newIdxTable[ind][2] = initIdxTable[i][2];
			ind++;
		}
		initIdxTable = newIdxTable;
	}
	
	private int[] findFace(int v) {
		List<Integer> faces = new ArrayList<Integer>();
		for (int i = 0; i < initIdxTable.length; i++) {
			if (initIdxTable[i][0] == v || initIdxTable[i][1] == v ||initIdxTable[i][2] == v) {
				faces.add(i);
			}
		}
		
		int[] tab = new int[faces.size()];
		for (int i = 0; i < faces.size(); i++) {
			tab[i] = faces.get(i);
		}
		return tab;
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
