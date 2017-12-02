package fr.jponzo.gamagora.nutshell3d.material.interfaces;

import java.io.Serializable;
import java.util.HashMap;

import javax.naming.OperationNotSupportedException;

public interface IMaterialDef extends Serializable {

	String getVertexShaderSource();

	String getFragmentShaderSource();

	void load() throws OperationNotSupportedException;

	HashMap<String, String> getVertUnif();

	HashMap<String, String> getFragUnif();

	String getVertexShaderPath();

	String getFragmentShaderPath();
	
	void setVertexShaderPath(String vertexShaderPath);

	void setFragmentShaderPath(String pixelShaderPath);
}
