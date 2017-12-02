package fr.jponzo.gamagora.nutshell3d.material.interfaces;

import java.io.Serializable;

public interface ITexture extends Serializable {

	int getHeight();

	int getWidth();

	float[] getPixBuffer();

	String getImagePath();

	void load();

}
