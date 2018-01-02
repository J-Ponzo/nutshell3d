package fr.jponzo.gamagora.nutshell3d.scene.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class MeshDef implements IMeshDef {
	private String meshPath;
	private float[][] posTable;
	private float[][] colTable;
	private float[][] offTable;
	private float[][] norTable;
	private int[][] idxTable;

	private enum DataType{
		Position,
		Color,
		Offset,
		Normals,
		Index
	};

	@Override
	public void load() {
		String[] tokens = meshPath.split("\\.");
		if(tokens.length > 2
				&& tokens[tokens.length - 2].equals("mesh")
				&& tokens[tokens.length - 1].equals("csv")) {
			loadCsvDataFromFile();
		} else if (tokens.length > 1
				&& tokens[tokens.length - 1].equals("stl")) {
			loadStlDataFromFile();
		} else if (tokens.length > 1
				&& tokens[tokens.length - 1].equals("obj")) {
			loadObjDataFromFile();
		} else if (tokens.length > 1
				&& tokens[tokens.length - 1].equals("off")) {
			loadOffDataFromFile();
		}

		normalizePositions();
	}

	/**
	 * Normalize positions on a unit cube 
	 */
	private void normalizePositions() {
		float xMin = posTable[0][0];
		float xMax = posTable[0][0];
		float yMin = posTable[0][1];
		float yMax = posTable[0][1];
		float zMin = posTable[0][2];
		float zMax = posTable[0][2];

		float xSum = posTable[0][0];
		float ySum = posTable[0][1];
		float zSum = posTable[0][2];

		for (int i = 1; i < posTable.length; i++) {
			//Update bounds
			if (posTable[i][0] < xMin) {
				xMin = posTable[i][0];
			}
			if (posTable[i][0] > xMax) {
				xMax = posTable[i][0];
			}
			if (posTable[i][1] < yMin) {
				yMin = posTable[i][1];
			}
			if (posTable[i][1] > yMax) {
				yMax = posTable[i][1];
			}
			if (posTable[i][2] < zMin) {
				zMin = posTable[i][2];
			}
			if (posTable[i][2] > zMax) {
				zMax = posTable[i][2];
			}

			//Update sums
			xSum += posTable[i][0];
			ySum += posTable[i][1];
			zSum += posTable[i][2];
		}

		//Center on origin && scale to unit
		float xAvg = xSum / (float) posTable.length;
		float yAvg = ySum / (float) posTable.length;
		float zAvg = zSum / (float) posTable.length;

		float deltaX = xMax - xMin;
		float deltaY = yMax - yMin;
		float deltaZ = zMax - zMin;
		float scalFactor = 1f / deltaX;
		if (deltaY > deltaX && deltaY > deltaZ) {
			scalFactor = 1f / deltaY;
		} else if (deltaZ > deltaX && deltaZ > deltaY) {
			scalFactor = 1f / deltaZ;
		}

		for (int i = 0; i < posTable.length; i++) {
			posTable[i][0] -= xAvg;
			posTable[i][1] -= yAvg;
			posTable[i][2] -= zAvg;

			posTable[i][0] *= scalFactor;
			posTable[i][1] *= scalFactor;
			posTable[i][2] *= scalFactor;
		}
	}
	
	/**
	 * Generate position normals from topology
	 */
	private void generateNormals() {
		//Init tris normals
		Vec3[] trisNor = new Vec3[idxTable.length];
		for (int i = 0; i < idxTable.length; i++) {
			int p1 = idxTable[i][0];
			int p2 = idxTable[i][1];
			int p3 = idxTable[i][2];
			
			Vec3 v1 = new Vec3(posTable[p1][0], posTable[p1][1], posTable[p1][2]);
			Vec3 v2 = new Vec3(posTable[p2][0], posTable[p2][1], posTable[p2][2]);
			Vec3 v3 = new Vec3(posTable[p3][0], posTable[p3][1], posTable[p3][2]);
			
			Vec3 v1v2 = v2.subtract(v1);
			Vec3 v1v3 = v3.subtract(v1);
			
			Vec3 n = v1v2.cross(v1v3).getUnitVector();
			trisNor[i] = n;
		}
		
		//Init position adjacences table (pos => faces)
		List<Integer>[] posAdj = new List[posTable.length];
		for (int i = 0; i < posTable.length; i++) {
			posAdj[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < idxTable.length; i++) {
			int p1 = idxTable[i][0];
			int p2 = idxTable[i][1];
			int p3 = idxTable[i][2];
			
			posAdj[p1].add(i);
			posAdj[p2].add(i);
			posAdj[p3].add(i);
		}
		
		//Compute normals
		norTable = new float[posTable.length][3];
		for (int i = 0; i < posTable.length; i++) {
			Vec3 n = new Vec3(0f, 0f, 0f);
			for (int j = 0; j < posAdj[i].size(); j++) {
				int triNorInd = posAdj[i].get(j);
				n = n.add(trisNor[triNorInd]);
			}
			n = n.getUnitVector();
			norTable[i][0] = n.getX();
			norTable[i][1] = n.getY();
			norTable[i][2] = n.getZ();
		}
	}

	private void loadObjDataFromFile() {
		//Reading the .mesh.csv file and sorting data lines
		List<String> posLines = new ArrayList<String>();
		List<String> colLines = new ArrayList<String>();
		List<String> offLines = new ArrayList<String>();
		List<String> norLines = new ArrayList<String>();
		List<String> idxLines = new ArrayList<String>();
		loadObjData(posLines, colLines, offLines, norLines, idxLines);

		float[][] posTableTmp;
		float[][] colTableTmp;
		float[][] offTableTmp;
		float[][] norTableTmp;
		//Init positions
		posTableTmp = new float[posLines.size()][3];
		for (int i = 0; i < posLines.size(); i++) {
			String[] tokens = posLines.get(i).split(" ");
			tokens = cleanTokens(tokens);
			posTableTmp[i][0] = Float.parseFloat(tokens[1]);
			posTableTmp[i][1] = Float.parseFloat(tokens[2]);
			posTableTmp[i][2] = Float.parseFloat(tokens[3]);
		}

		//Init color attrs
		colTableTmp = new float[colLines.size()][3];
		for (int i = 0; i < colLines.size(); i++) {
			String[] tokens = colLines.get(i).split(" ");
			colTableTmp[i][0] = Float.parseFloat(tokens[1]);
			colTableTmp[i][1] = Float.parseFloat(tokens[2]);
			colTableTmp[i][2] = Float.parseFloat(tokens[3]);
		}

		//Init texture offsets attrs
		offTableTmp = new float[offLines.size()][2];
		for (int i = 0; i < offLines.size(); i++) {
			String[] tokens = offLines.get(i).split(" ");
			offTableTmp[i][0] = Float.parseFloat(tokens[2]);
			offTableTmp[i][1] = Float.parseFloat(tokens[1]);
		}

		//Init normal attrs
		norTableTmp = new float[norLines.size()][3];
		for (int i = 0; i < norLines.size(); i++) {
			String[] tokens = norLines.get(i).split(" ");
			norTableTmp[i][0] = Float.parseFloat(tokens[1]);
			norTableTmp[i][1] = Float.parseFloat(tokens[2]);
			norTableTmp[i][2] = Float.parseFloat(tokens[3]);
		}

		//Init triangles
		idxTable = new int[idxLines.size() * 2][3];
		posTable = new float[idxLines.size() * 6][3];
		colTable = new float[idxLines.size() * 6][3];
		offTable = new float[idxLines.size() * 6][2];
		norTable = new float[idxLines.size() * 6][3];
		int index = 0;
		int dataIndex = 0;
		for (int i = 0; i < idxLines.size() ; i++) {
			String[] indices = idxLines.get(i).split(" ");
			indices = cleanTokens(indices);
			String ind1 = indices[1];
			String ind2 = indices[2];
			String ind3 = indices[3];
			String ind4 = ind3;
			if (indices.length == 5) {
				ind4 = indices[4];
			}

			String[] indToks = ind1.split("/");
			int ind1V = resolveIndex(Integer.parseInt(indToks[0]), posTable.length, DataType.Position) - 1;
			int ind1Vt = resolveIndex(Integer.parseInt(indToks[1]), posTable.length, DataType.Offset) - 1;
			int ind1Vn = resolveIndex(Integer.parseInt(indToks[2]), posTable.length, DataType.Normals) - 1;
			indToks = ind2.split("/");
			int ind2V = resolveIndex(Integer.parseInt(indToks[0]), posTable.length, DataType.Position) - 1;
			int ind2Vt = resolveIndex(Integer.parseInt(indToks[1]), posTable.length, DataType.Offset) - 1;
			int ind2Vn = resolveIndex(Integer.parseInt(indToks[2]), posTable.length, DataType.Normals) - 1;
			indToks = ind3.split("/");
			int ind3V = resolveIndex(Integer.parseInt(indToks[0]), posTable.length, DataType.Position) - 1;
			int ind3Vt = resolveIndex(Integer.parseInt(indToks[1]), posTable.length, DataType.Offset) - 1;
			int ind3Vn = resolveIndex(Integer.parseInt(indToks[2]), posTable.length, DataType.Normals) - 1;
			indToks = ind4.split("/");
			int ind4V = resolveIndex(Integer.parseInt(indToks[0]), posTable.length, DataType.Position) - 1;
			int ind4Vt = resolveIndex(Integer.parseInt(indToks[1]), posTable.length, DataType.Offset) - 1;
			int ind4Vn = resolveIndex(Integer.parseInt(indToks[2]), posTable.length, DataType.Normals) - 1;

			//p1
			idxTable[index][0] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind1V][0];
			posTable[dataIndex][1] = posTableTmp[ind1V][1];
			posTable[dataIndex][2] = posTableTmp[ind1V][2];

			offTable[dataIndex][0] = offTableTmp[ind1Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind1Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind1Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind1Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind1Vn][2];

			dataIndex++;

			//p2
			idxTable[index][1] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind2V][0];
			posTable[dataIndex][1] = posTableTmp[ind2V][1];
			posTable[dataIndex][2] = posTableTmp[ind2V][2];

			offTable[dataIndex][0] = offTableTmp[ind2Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind2Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind2Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind2Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind2Vn][2];

			dataIndex++;

			//p3
			idxTable[index][2] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind3V][0];
			posTable[dataIndex][1] = posTableTmp[ind3V][1];
			posTable[dataIndex][2] = posTableTmp[ind3V][2];

			offTable[dataIndex][0] = offTableTmp[ind3Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind3Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind3Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind3Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind3Vn][2];

			dataIndex++;
			index++;

			//p1
			idxTable[index][0] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind1V][0];
			posTable[dataIndex][1] = posTableTmp[ind1V][1];
			posTable[dataIndex][2] = posTableTmp[ind1V][2];

			colTable[dataIndex][0] = 1f;
			colTable[dataIndex][1] = 1f;
			colTable[dataIndex][2] = 1f;

			offTable[dataIndex][0] = offTableTmp[ind1Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind1Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind1Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind1Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind1Vn][2];

			dataIndex++;

			//p3
			idxTable[index][1] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind3V][0];
			posTable[dataIndex][1] = posTableTmp[ind3V][1];
			posTable[dataIndex][2] = posTableTmp[ind3V][2];

			colTable[dataIndex][0] = 1f;
			colTable[dataIndex][1] = 1f;
			colTable[dataIndex][2] = 1f;

			offTable[dataIndex][0] = offTableTmp[ind3Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind3Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind3Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind3Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind3Vn][2];

			dataIndex++;

			//p4
			idxTable[index][2] = dataIndex;

			posTable[dataIndex][0] = posTableTmp[ind4V][0];
			posTable[dataIndex][1] = posTableTmp[ind4V][1];
			posTable[dataIndex][2] = posTableTmp[ind4V][2];

			colTable[dataIndex][0] = 1f;
			colTable[dataIndex][1] = 1f;
			colTable[dataIndex][2] = 1f;

			offTable[dataIndex][0] = offTableTmp[ind4Vt][0];
			offTable[dataIndex][1] = offTableTmp[ind4Vt][1];

			norTable[dataIndex][0] = norTableTmp[ind4Vn][0];
			norTable[dataIndex][1] = norTableTmp[ind4Vn][1];
			norTable[dataIndex][2] = norTableTmp[ind4Vn][2];

			dataIndex++;
			index++;
		}
	}

	private int resolveIndex(int ind, int nbVertices, DataType dataType) {
		if (ind > 0) {
			return ind;
		} else {
			throw new UnsupportedOperationException();
			//			switch (dataType) {
			//				case Position :
			//				return nbVertices + ind;
			//				case Offset :
			//					return nbVertices + ind;
			//				case Normals :
			//					return nbVertices + ind;
			//			}
		}
	}

	private void loadObjData(List<String> posLines, List<String> colLines, List<String> offLines, List<String> norLines,
			List<String> idxLines) {
		File file = new File(meshPath);
		DataType dataType = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replace(',', '.');			//Ensuring international notation
				String[] tokens = inputLine.split(" ");
				tokens = cleanTokens(tokens);
				if (tokens.length == 0) {
					continue;
				}

				if (tokens[0].equals("v")) {
					dataType = DataType.Position;
				} else if (tokens[0].equals("vc")) {
					dataType = DataType.Color;
				} else if (tokens[0].equals("vt")) {
					dataType = DataType.Offset;
				} else if (tokens[0].equals("vn")) {
					dataType = DataType.Normals;
				} else if (tokens[0].equals("f")) {
					dataType = DataType.Index;
				} else {
					dataType = null;
				}

				if (dataType != null) {
					switch (dataType) {
					case Position:
						posLines.add(inputLine);
						break;
					case Color:
						colLines.add(inputLine);
						break;
					case Offset:
						offLines.add(inputLine);
						break;
					case Normals:
						norLines.add(inputLine);
						break;
					case Index:
						idxLines.add(inputLine);
						break;
					}
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String[] cleanTokens(String[] tokens) {
		//Count blanks
		int blanks = 0;
		for (String token : tokens) {
			if (token.equals("")) {
				blanks++;
			}
		}

		//Build clean tokens tab
		int ind = 0;
		String[] cleanTokens = new String[tokens.length - blanks];
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (!token.equals("")) {
				cleanTokens[ind++] = token;
			}
		}

		return cleanTokens;
	}

	private void loadStlDataFromFile() {
		throw new UnsupportedOperationException();
	}

	private void loadOffDataFromFile() {
		//Reading the .mesh.csv file and sorting data lines
		List<String> posLines = new ArrayList<String>();
		List<String> colLines = new ArrayList<String>();
		List<String> offLines = new ArrayList<String>();
		List<String> norLines = new ArrayList<String>();
		List<String> idxLines = new ArrayList<String>();
		loadOffData(posLines, colLines, offLines, norLines, idxLines);

		//Init positions
		posTable = new float[posLines.size()][3];
		for (int i = 0; i < posLines.size(); i++) {
			String[] tokens = posLines.get(i).split(" ");
			posTable[i][0] = Float.parseFloat(tokens[0]);
			posTable[i][1] = Float.parseFloat(tokens[1]);
			posTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init color attrs
		colTable = new float[colLines.size()][3];
		for (int i = 0; i < colLines.size(); i++) {
			String[] tokens = colLines.get(i).split(" ");
			colTable[i][0] = Float.parseFloat(tokens[0]);
			colTable[i][1] = Float.parseFloat(tokens[1]);
			colTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init texture offsets attrs
		offTable = new float[offLines.size()][2];
		for (int i = 0; i < offLines.size(); i++) {
			String[] tokens = offLines.get(i).split(" ");
			offTable[i][0] = Float.parseFloat(tokens[0]);
			offTable[i][1] = Float.parseFloat(tokens[1]);
		}

		//Init normal attrs
		norTable = new float[norLines.size()][3];
		for (int i = 0; i < norLines.size(); i++) {
			String[] tokens = norLines.get(i).split(" ");
			norTable[i][0] = Float.parseFloat(tokens[0]);
			norTable[i][1] = Float.parseFloat(tokens[1]);
			norTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init triangles
		idxTable = new int[idxLines.size()][3];
		for (int i = 0; i < idxLines.size(); i++) {
			String[] tokens = idxLines.get(i).split(" ");
			idxTable[i][0] = (int) Float.parseFloat(tokens[1]);
			idxTable[i][1] = (int) Float.parseFloat(tokens[2]);
			idxTable[i][2] = (int) Float.parseFloat(tokens[3]);
		}
		
		//Generate normals
		generateNormals();
	}

	private void loadOffData(List<String> posLines, List<String> colLines, List<String> offLines, List<String> norLines,
			List<String> idxLines) {
		File file = new File(meshPath);
		DataType dataType = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String inputLine;

			//Skip first 2 lines
			in.readLine();
			in.readLine();

			//Parse body
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replace(',', '.');			//Ensuring international notation
				String[] tokens = inputLine.split(" ");
				tokens = cleanTokens(tokens);
				if (tokens.length == 3) {
					dataType = DataType.Position;
				} else if (tokens.length == 4) {
					dataType = DataType.Index;
				} else {
					dataType = null;
				}

				if (dataType != null) {
					switch (dataType) {
					case Position:
						posLines.add(inputLine);
						break;
					case Index:
						idxLines.add(inputLine);
						break;
					}
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadCsvDataFromFile() {
		//Reading the .mesh.csv file and sorting data lines
		List<String> posLines = new ArrayList<String>();
		List<String> colLines = new ArrayList<String>();
		List<String> offLines = new ArrayList<String>();
		List<String> norLines = new ArrayList<String>();
		List<String> idxLines = new ArrayList<String>();
		loadCsvData(posLines, colLines, offLines, norLines, idxLines);

		//Init positions
		posTable = new float[posLines.size()][3];
		for (int i = 0; i < posLines.size(); i++) {
			String[] tokens = posLines.get(i).split(";");
			posTable[i][0] = Float.parseFloat(tokens[0]);
			posTable[i][1] = Float.parseFloat(tokens[1]);
			posTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init color attrs
		colTable = new float[colLines.size()][3];
		for (int i = 0; i < colLines.size(); i++) {
			String[] tokens = colLines.get(i).split(";");
			colTable[i][0] = Float.parseFloat(tokens[0]);
			colTable[i][1] = Float.parseFloat(tokens[1]);
			colTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init texture offsets attrs
		offTable = new float[offLines.size()][2];
		for (int i = 0; i < offLines.size(); i++) {
			String[] tokens = offLines.get(i).split(";");
			offTable[i][0] = Float.parseFloat(tokens[0]);
			offTable[i][1] = Float.parseFloat(tokens[1]);
		}

		//Init normal attrs
		norTable = new float[norLines.size()][3];
		for (int i = 0; i < norLines.size(); i++) {
			String[] tokens = norLines.get(i).split(";");
			norTable[i][0] = Float.parseFloat(tokens[0]);
			norTable[i][1] = Float.parseFloat(tokens[1]);
			norTable[i][2] = Float.parseFloat(tokens[2]);
		}

		//Init triangles
		idxTable = new int[idxLines.size()][3];
		for (int i = 0; i < idxLines.size(); i++) {
			String[] tokens = idxLines.get(i).split(";");
			idxTable[i][0] = (int) Float.parseFloat(tokens[0]);
			idxTable[i][1] = (int) Float.parseFloat(tokens[1]);
			idxTable[i][2] = (int) Float.parseFloat(tokens[2]);
		}
	}

	private void loadCsvData(List<String> posLines, List<String> colLines, List<String> offLines, List<String> norLines, List<String> idxLines) {
		File file = new File(meshPath);
		DataType dataType = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replace(',', '.');			//Ensuring international notation
				String[] tokens = inputLine.split(";");
				if (tokens[0].equals("Positions")) {
					dataType = DataType.Position;
				} else if (tokens[0].equals("Colors")) {
					dataType = DataType.Color;
				} else if (tokens[0].equals("Offsets")) {
					dataType = DataType.Offset;
				} else if (tokens[0].equals("Normals")) {
					dataType = DataType.Normals;
				} else if (tokens[0].equals("Indices")) {
					dataType = DataType.Index;
				} else {
					switch (dataType) {
					case Position:
						posLines.add(inputLine);
						break;
					case Color:
						colLines.add(inputLine);
						break;
					case Offset:
						offLines.add(inputLine);
						break;
					case Normals:
						norLines.add(inputLine);
						break;
					case Index:
						idxLines.add(inputLine);
						break;
					}
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPath() {
		return meshPath;
	}

	public void setPath(String path) {
		this.meshPath = path;
	}

	public float[][] getPosTable() {
		return posTable;
	}

	@Override
	public int[][] getIdxTable() {
		return idxTable;
	}

	@Override
	public float[][] getColTable() {
		return colTable;
	}

	@Override
	public float[][] getOffTable() {
		return offTable;
	}

	@Override
	public float[][] getNorTable() {
		return norTable;
	}
}
