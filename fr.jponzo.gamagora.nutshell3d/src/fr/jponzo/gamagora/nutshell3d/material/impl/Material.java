package fr.jponzo.gamagora.nutshell3d.material.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterialDef;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITexture;

public class Material implements IMaterial {
	private static final long serialVersionUID = 1L;

	private IMaterialDef materialDef = null;

	private Map<String, ITexture> texParams = new HashMap<String, ITexture>();
	private Map<String, Float[]> vec3Params = new HashMap<String, Float[]>();
	private Map<String, Float> floatParams = new HashMap<String, Float>();

	Material(IMaterialDef materialDef) throws OperationNotSupportedException {
		this.materialDef = materialDef;
		initParamsFromMaterialDef();
	}

	@Override
	public IMaterialDef getMaterialDef() {
		return materialDef;
	}

	@Override
	public float[] getVec3Param(String key) {
		Float[] param3f = vec3Params.get(key);
		if (param3f == null) {
			return null;
		}

		float[] castParam3f = new float[3];
		castParam3f[0] = param3f[0];
		castParam3f[1] = param3f[1];
		castParam3f[2] = param3f[2];
		return castParam3f;
	}

	@Override
	public void setVec3Param(String key, float f1, float f2, float f3) {
		if (vec3Params.get(key) == null) {
			return;
		}
		Float[] param3f = new Float[3];
		param3f[0] = f1;
		param3f[1] = f2;
		param3f[2] = f3;
		vec3Params.put(key, param3f);
	}

	@Override
	public Set<Entry<String, Float[]>> getAllVec3Params() {
		return vec3Params.entrySet();
	}

	@Override
	public ITexture getTexParam(String key) {
		return texParams.get(key);
	}

	@Override
	public void setTexParam(String key, ITexture texture) {
		if (texParams.get(key) == null) {
			return;
		}
		texParams.put(key, texture);
	}

	@Override
	public Set<Entry<String, ITexture>> getAllTexParams() {
		return texParams.entrySet();
	}

	@Override
	public float getFloatParam(String key) {
		return floatParams.get(key);
	}

	@Override
	public void setFloatParam(String key, float f) {
		if (floatParams.get(key) == null) {
			return;
		}
		floatParams.put(key, f);
	}

	@Override
	public Set<Entry<String, Float>> getAllFloatParams() {
		return floatParams.entrySet();
	}

	public void initParamsFromMaterialDef() throws OperationNotSupportedException {
		for (Entry<String, String> param : materialDef.getVertUnif().entrySet()) {
			String prefix = param.getKey().split("_")[0];
			if (prefix.equals("mat")) {
				switch (param.getValue()) {
				case "float":
					floatParams.put(param.getKey(), 0f);
					break;
				case "vec2":

					break;
				case "vec3":
					vec3Params.put(param.getKey(), new Float[]{0f, 0f, 0f});
					break;
				case "sampler2D":
					texParams.put(param.getKey(), MaterialManager.DEFAULT_TEXTURE);
					break;
				default:
					throw new OperationNotSupportedException("The uniform GLSL type '" + param.getValue() + "' is not supported yet");
				}
			}
		}

		for (Entry<String, String> param : materialDef.getFragUnif().entrySet()) {
			String prefix = param.getKey().split("_")[0];
			if (prefix.equals("mat")) {
				switch (param.getValue()) {
				case "float":
					floatParams.put(param.getKey(), 0f);
					break;
				case "vec2":

					break;
				case "vec3":
					vec3Params.put(param.getKey(), new Float[]{0f, 0f, 0f});
					break;
				case "sampler2D":
					texParams.put(param.getKey(), MaterialManager.DEFAULT_TEXTURE);
					break;
				default:
					throw new OperationNotSupportedException("The uniform GLSL type '" + param.getValue() + "' is not supported yet");
				}
			}
		}
	}
}
