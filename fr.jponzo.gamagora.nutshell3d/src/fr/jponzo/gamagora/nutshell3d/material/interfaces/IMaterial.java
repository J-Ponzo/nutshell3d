package fr.jponzo.gamagora.nutshell3d.material.interfaces;

import java.util.Map.Entry;
import java.io.Serializable;
import java.util.Set;

public interface IMaterial extends Serializable {

	Set<Entry<String, Float>> getAllFloatParams();

	void setFloatParam(String key, float f);

	float getFloatParam(String key);

	Set<Entry<String, ITexture>> getAllTexParams();

	void setTexParam(String key, ITexture texture);

	ITexture getTexParam(String key);

	Set<Entry<String, Float[]>> getAllVec3Params();

	void setVec3Param(String key, float f1, float f2, float f3);

	float[] getVec3Param(String key);

	IMaterialDef getMaterialDef();
}
