package fr.jponzo.gamagora.nutshell3d.material.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterialDef;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITexture;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITextureLocation;

public class MaterialManager {

	private static class SingletonWrapper {
		private final static MaterialManager instance = new MaterialManager();
	}

	public static MaterialManager getInstance() {
		return SingletonWrapper.instance;
	}

	public static final ITexture DEFAULT_TEXTURE = new Texture();

	//ATLASSING VARS
	private int activeAtlasId = -1;
	private int atlasNumber = 8;
	private int lastAtlasIdAssigned = -1;
	private Map<ITexture, Boolean[]> atlasRefTable = new HashMap<ITexture, Boolean[]>();

	//MATERIAL MANAGEMENT VARS
	private Map<String, IMaterialDef> materialDefsTable = new HashMap<String, IMaterialDef>();

	//TEXTURES MANAGEMENT VARS
	private Map<String, ITexture> texturesTable = new HashMap<String, ITexture>();

	public IMaterial createMaterial(String vertPath, String fragPath) throws OperationNotSupportedException {
		String key = buildMaterialDefKey(vertPath, fragPath);
		IMaterialDef materialDef = materialDefsTable.get(key);
		if (materialDef == null) {
			materialDef = new MaterialDef();
			materialDef.setVertexShaderPath(vertPath);
			materialDef.setFragmentShaderPath(fragPath);
			materialDef.load();
			materialDefsTable.put(key, materialDef);
		}
		IMaterial material = new Material(materialDef);
		return material;
	}

	public void registerMaterial(IMaterial material) throws OperationNotSupportedException {
		IMaterialDef materialDef = material.getMaterialDef();
		String vertPath = materialDef.getVertexShaderPath();
		String fragPath = materialDef.getFragmentShaderPath();
		String key = buildMaterialDefKey(vertPath, fragPath);
		materialDef.load();
		materialDefsTable.put(key, materialDef);
	}
	
	public String buildMaterialDefKey(String vertPath, String fragPath) {
		return vertPath +"->"+fragPath;
	}

	public ITexture createTexture(String imgPath) {
		ITexture texture = texturesTable.get(imgPath);
		if (texture != null) {
			return texture;
		}
		Texture newTexture = new Texture();
		newTexture.setImagePath(imgPath);
		newTexture.load();
		texturesTable.put(imgPath, newTexture);
		return newTexture;
	}
	
	public void registerTexture(ITexture texture) {
		texture.load();
		texturesTable.put(texture.getImagePath(), texture);
	}

	public List<ITextureLocation> getTextureLocations(ITexture texture) {
		List<ITextureLocation> textureLocations = new ArrayList<ITextureLocation>();
		Boolean[] atlasPresences = atlasRefTable.get(texture);
		if (atlasPresences == null) {
			return textureLocations;
		}
		for (int i = 0; i < atlasNumber; i++) {
			if (atlasPresences[i]) {
				ITextureLocation textureLocation = new TextureLocation();
				textureLocation.setAtlasId(i);
				textureLocation.setOx(0);
				textureLocation.setOy(0);
				textureLocations.add(textureLocation);
			}
		}
		return textureLocations;
	}

	public int assignTextureLocation(ITexture textureData) {
		lastAtlasIdAssigned =  (lastAtlasIdAssigned + 1) % atlasNumber;
		//Create new texture presence entry
		Boolean[] atlasPersences = new Boolean[atlasNumber];
		for (int i = 0; i < atlasNumber; i++) {
			if (i == lastAtlasIdAssigned) {
				atlasPersences[i] = true;
			} else {
				atlasPersences[i] = false;
			}
		}
		
		//Remove all resources entries present on the assigned atlas
		List<ITexture> keysToRemove = new ArrayList<ITexture>();
		for (Entry<ITexture, Boolean[]> entry : atlasRefTable.entrySet()) {
			if (entry.getValue()[lastAtlasIdAssigned] ) {
				keysToRemove.add(entry.getKey());
			}
		}
		for (ITexture key : keysToRemove) {
			atlasRefTable.remove(key);
		}
		
		//Put new texture presence entry
		atlasRefTable.put(textureData, atlasPersences);
		
		return lastAtlasIdAssigned;
	}
}
