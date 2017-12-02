package fr.jponzo.gamagora.nutshell3d.material.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterialDef;

public class MaterialDef implements IMaterialDef {
	private static final long serialVersionUID = 1L;
	
	private String vertexShaderPath;
	private String fragmentShaderPath;
	private String vertexShaderSource;
	private String fragmentShaderSource;
	
	private HashMap<String, String> vertIn = new HashMap<String, String>();
	private HashMap<String, String> vertOut = new HashMap<String, String>();
	private HashMap<String, String> vertUnif = new HashMap<String, String>();
	private HashMap<String, String> fragIn = new HashMap<String, String>();
	private HashMap<String, String> fragOut = new HashMap<String, String>();
	private HashMap<String, String> fragUnif = new HashMap<String, String>();
	
	MaterialDef() {
	}

	@Override
	public String getVertexShaderPath() {
		return this.vertexShaderPath;
	}

	@Override
	public String getFragmentShaderPath() {
		return this.fragmentShaderPath;
	}
	
	@Override
	public void setVertexShaderPath(String vertexShaderPath) {
		this.vertexShaderPath = vertexShaderPath;
	}

	@Override
	public void setFragmentShaderPath(String pixelShaderPath) {
		this.fragmentShaderPath = pixelShaderPath;
	}

	@Override
	public String getVertexShaderSource() {
		return vertexShaderSource;
	}

	@Override
	public String getFragmentShaderSource() {
		return fragmentShaderSource;
	}
	
	@Override
	public HashMap<String, String> getVertUnif() {
		return vertUnif;
	}

	@Override
	public HashMap<String, String> getFragUnif() {
		return fragUnif;
	}

	@Override
	public void load() {
		readSources();
		extractShaderParams();
	}
	
	private void extractShaderParams() {
		String cleanLine;
		String[] vertLines = vertexShaderSource.split("\n");
		for (int l = 0; l < vertLines.length; l++) {
			cleanLine = vertLines[l];
			cleanLine = cleanLine(cleanLine);
			String[] tokens = cleanLine.split(" ");
			if (tokens[0].equals("in")) {
				vertIn.put(tokens[2], tokens[1]);
			} else if (tokens[0].equals("out")) {
				vertOut.put(tokens[2], tokens[1]);
			} else if (tokens[0].equals("uniform")) {
				vertUnif.put(tokens[2], tokens[1]);
			}
		}

		String[] fragLines = fragmentShaderSource.split("\n");
		for (int l = 0; l < fragLines.length; l++) {
			cleanLine = fragLines[l];
			cleanLine = cleanLine(cleanLine);
			String[] tokens = cleanLine.split(" ");
			if (tokens[0].equals("in")) {
				fragIn.put(tokens[2], tokens[1]);
			} else if (tokens[0].equals("out")) {
				fragOut.put(tokens[2], tokens[1]);
			} else if (tokens[0].equals("uniform")) {
				fragUnif.put(tokens[2], tokens[1]);
			}
		}
	}
	
	private void readSources() {
		//Reading the Vertex Shader Source code file
		File file = new File(this.vertexShaderPath);
		this.vertexShaderSource = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				this.vertexShaderSource += inputLine + "\n";
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Reading the Vertex Shader Source code file
		file = new File(this.fragmentShaderPath);
		this.fragmentShaderSource = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				this.fragmentShaderSource += inputLine + "\n";
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String cleanLine(String line) {
		line = line.trim();
		line = line.replaceAll("//.*", " ");
		line = line.trim();
		line = line.replaceAll("\t+", " ");
		line = line.trim();
		line = line.replaceAll(" +", " ");
		line = line.trim();
		line = line.replaceAll(";", "");
		line = line.trim();

		return line;
	}
}
