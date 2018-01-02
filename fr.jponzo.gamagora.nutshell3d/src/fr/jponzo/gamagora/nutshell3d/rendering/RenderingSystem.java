package fr.jponzo.gamagora.nutshell3d.rendering;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.GLBuffers;

import fr.jponzo.gamagora.modelgeo.tp5.ICurve;
import fr.jponzo.gamagora.nutshell3d.material.impl.MaterialManager;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterialDef;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITexture;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITextureLocation;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Camera;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Entity;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Transform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMirror;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IPortal;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec4;

public class RenderingSystem extends AbstractRenderingSystem {
	public static final int NB_CAM_LAYERS = 32;

	private static class SingletonWrapper {
		private final static RenderingSystem instance = new RenderingSystem();
	}

	public static RenderingSystem getInstance() {
		return SingletonWrapper.instance;
	}

	/** 
	 * The definition of the displayed white triangle vertices
	 */
	private float verticesData[] = new float[0]; 

	/**
	 * The definition of the triangles composed square
	 */
	private int elementsData[] = new int[0];

	private int[] vbo = new int[1];
	private int[] ebo = new int[1];
	private int[] tex = new int[8];

	private int[] osFb = new int[2];
	private int[] osTex = new int[2];

	private int shaderProgram;

	private String loadedMeshPath = null;
	private String loadedMaterialPath = null;

	private List<IEntity> meshQueue = new ArrayList<IEntity>();
	private List<IEntity> cameraQueue = new ArrayList<IEntity>();
	private List<IEntity> lightQueue = new ArrayList<IEntity>();
	private List<IEntity> mirrorQueue = new ArrayList<IEntity>();
	private List<IEntity> portalQueue = new ArrayList<IEntity>();
	private List<IEntity> curveQueue = new ArrayList<IEntity>();

	private boolean debug = true;

	private IEntity mirrorCamEntity = new Entity();

	public GLCanvas getGlcanvas() {
		return this.glcanvas;
	}

	public void renderPass(IEntity root) {
		//Push entities in rendering queue
		meshQueue.clear();
		lightQueue.clear();
		cameraQueue.clear();
		mirrorQueue.clear();
		portalQueue.clear();
		curveQueue.clear();
		updateRenderingQueues(root);
		this.glcanvas.display();
	}

	private void updateRenderingQueues(IEntity entity) {
		//Recursive call (sweeping the tree)
		if (entity.getChildsCount() > 0) {
			for (int i = 0; i < entity.getChildsCount(); i++) {
				updateRenderingQueues(entity.getChild(i));
			}
		}

		//Processing current Entity
		//look for a mesh component
		if (entity.getMeshes().size() > 0 
				&& entity.getMirrors().size() == 0
				&& entity.getPortals().size() == 0) {
			meshQueue.add(entity);
			Collections.sort(meshQueue, new Comparator<IEntity>() {

				@Override
				public int compare(IEntity o1, IEntity o2) {
					String path1 = o1.getMeshes().get(0).getMeshDef().getPath();
					String path2 = o1.getMeshes().get(0).getMeshDef().getPath();
					int hash1 = path1.hashCode();
					int hash2 = path2.hashCode();
					if (hash1 > hash2) {
						return 1;
					} else if (hash1 < hash2) {
						return -1;
					} else {
						return 0;
					}
				}
			});
		}
		if (entity.getLights().size() > 0) {
			lightQueue.add(entity);
		}
		if (entity.getCameras().size() > 0) {
			cameraQueue.add(entity);
		}
		if (entity.getMirrors().size() > 0) {
			mirrorQueue.add(entity);
		}
		if (entity.getPortals().size() > 0) {
			portalQueue.add(entity);
		}
		if (entity.getCurves().size() > 0) {
			curveQueue.add(entity);
		}
	}

	@Override
	protected void initialize(GL4 gl) {
		gl.glEnable(GL.GL_DEPTH_TEST);

		//Create and Bind Texture Buffers
		gl.glGenTextures(8, IntBuffer.wrap(tex));

		//Create and Bind VBO
		gl.glGenBuffers(1, IntBuffer.wrap(vbo));

		//Create and Bind EBO
		gl.glGenBuffers(1, IntBuffer.wrap(ebo));

		//Create out screen texture Beffers on last text offset
		gl.glGenTextures(2, IntBuffer.wrap(osTex));
		gl.glBindTexture(GL4.GL_TEXTURE_2D, osTex[0]);
		gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 1, GL4.GL_RGB8, glcanvas.getWidth(), glcanvas.getHeight());
		gl.glBindTexture(GL4.GL_TEXTURE_2D, osTex[1]);
		gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 1, GL4.GL_RGB8, glcanvas.getWidth(), glcanvas.getHeight());

		//Create out screen FBO and provide it with an out screen texture
		gl.glGenFramebuffers(2, IntBuffer.wrap(osFb));

		//Configure fb0
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[0]);
		gl.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, GL4.GL_TEXTURE_2D, osTex[0], 0);

		IntBuffer rbIdPtr1 = GLBuffers.newDirectIntBuffer(1);
		gl.glGenRenderbuffers(1, rbIdPtr1);
		int rbId = rbIdPtr1.get(0);
		gl.glBindRenderbuffer(GL4.GL_RENDERBUFFER, rbId);
		gl.glRenderbufferStorage(GL4.GL_RENDERBUFFER, GL4.GL_DEPTH24_STENCIL8, glcanvas.getWidth(), glcanvas.getHeight());
		gl.glFramebufferRenderbuffer(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, GL4.GL_RENDERBUFFER, rbId);
		gl.glFramebufferRenderbuffer(GL4.GL_FRAMEBUFFER, GL4.GL_STENCIL_ATTACHMENT, GL4.GL_RENDERBUFFER, rbId);

		//Configure fb1
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[1]);
		gl.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, GL4.GL_TEXTURE_2D, osTex[1], 0);

		IntBuffer rbIdPtr2 = GLBuffers.newDirectIntBuffer(1);
		gl.glGenRenderbuffers(1, rbIdPtr2);
		rbId = rbIdPtr2.get(0);
		gl.glBindRenderbuffer(GL4.GL_RENDERBUFFER, rbId);
		gl.glRenderbufferStorage(GL4.GL_RENDERBUFFER, GL4.GL_DEPTH24_STENCIL8, glcanvas.getWidth(), glcanvas.getHeight());
		gl.glFramebufferRenderbuffer(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, GL4.GL_RENDERBUFFER, rbId);
		gl.glFramebufferRenderbuffer(GL4.GL_FRAMEBUFFER, GL4.GL_STENCIL_ATTACHMENT, GL4.GL_RENDERBUFFER, rbId);
	}

	@Override
	protected void render(GL4 gl) {
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClearStencil(0);

		for (IEntity cameraEntity : cameraQueue) {
			ICamera camera = cameraEntity.getCameras().get(0);

			//Set fb0 active
			gl.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
			gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[0]);
			gl.glEnable(GL4.GL_DEPTH_TEST);

			//Draw Scene
			gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
			gl.glDisable(GL4.GL_STENCIL_TEST);
			gl.glStencilMask(0x00);
			meshesRenderingPass(gl, camera);
			curvesRenderingPass(gl, camera);
			gl.glStencilMask(0xFF);
			gl.glEnable(GL4.GL_STENCIL_TEST);

			//Mirrors Rendering
			//Init stencil
			gl.glClear(GL4.GL_STENCIL_BUFFER_BIT);
			gl.glStencilOp(GL4.GL_KEEP, GL4.GL_KEEP, GL4.GL_REPLACE);
			for (int i = 0; i < mirrorQueue.size(); i++) {
				gl.glStencilFunc(GL4.GL_ALWAYS, i + 1, 0xFF);
				IEntity mirrorEntity = mirrorQueue.get(i);
				mirrorRenderingPass(gl, camera, mirrorEntity);
			}

			for (int i = 0; i < mirrorQueue.size(); i++) {
				//Draw mirror content on fb1
				//				gl.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
				gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[1]);
				gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
				ICamera mirrorCam = createMirrorCam(mirrorQueue.get(i), cameraEntity);
				gl.glDisable(GL4.GL_STENCIL_TEST);
				gl.glStencilMask(0x00);
				meshesRenderingPass(gl, mirrorCam);
				gl.glStencilMask(0xFF);
				gl.glEnable(GL4.GL_STENCIL_TEST);

				//Draw mirror content on corresponding hole
				//				gl.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
				gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[0]);
				gl.glStencilFunc(GL4.GL_EQUAL, i + 1, 0xFF);
				gl.glDepthMask(false);
				postEffectRendering(gl, mirrorCam, osTex[1]);
				gl.glDepthMask(true);
			}

			//Portals Rendering
			//Init stencil
			gl.glClear(GL4.GL_STENCIL_BUFFER_BIT);
			gl.glStencilOp(GL4.GL_KEEP, GL4.GL_KEEP, GL4.GL_REPLACE);
			for (int i = 0; i < portalQueue.size(); i++) {
				gl.glStencilFunc(GL4.GL_ALWAYS, i + 1, 0xFF);
				IEntity portalEntity = portalQueue.get(i);
				portalRenderingPass(gl, camera, portalEntity);
			}

			for (int i = 0; i < portalQueue.size(); i++) {
				//Draw mirror content on fb1
				//				gl.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
				gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[1]);
				gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
				ICamera portalCam = createPortalCam(portalQueue.get(i), cameraEntity);
				gl.glDisable(GL4.GL_STENCIL_TEST);
				gl.glStencilMask(0x00);
				meshesRenderingPass(gl, portalCam);
				gl.glStencilMask(0xFF);
				gl.glEnable(GL4.GL_STENCIL_TEST);

				//Draw mirror content on corresponding hole
				//				gl.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
				gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, osFb[0]);
				gl.glStencilFunc(GL4.GL_EQUAL, i + 1, 0xFF);
				gl.glDepthMask(false);
				postEffectRendering(gl, portalCam, osTex[1]);
				gl.glDepthMask(true);
			}

			//Post Effect rendering pass
			Rectangle viewPort = camera.getViewport();
			gl.glViewport(
					(int) (camera.getWidth() * (viewPort.getX() / 100f)), 
					(int) (camera.getHeight() * (viewPort.getY() / 100f)),
					(int) (camera.getWidth() * (viewPort.getWidth() / 100f)), 
					(int) (camera.getHeight() * (viewPort.getHeight() / 100f))); 
			gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
			gl.glDisable(GL4.GL_DEPTH_TEST);
			gl.glDisable(GL4.GL_STENCIL_TEST);
			gl.glStencilMask(0x00);
			postEffectRendering(gl, camera, osTex[0]);
			gl.glStencilMask(0xFF);
			gl.glEnable(GL4.GL_STENCIL_TEST);
			gl.glEnable(GL4.GL_DEPTH_TEST);
		}
	}

	//TODO refactoring 
	private ICamera createMirrorCam(IEntity mirrorEntity, IEntity cameraEntity) {
		// Crate camera copy. The camera is docked on mirrorCamEntity attribute
		// Not clear, TODO refactoring
		ICamera mirrorCam = copyCameraComponent(cameraEntity.getCameras().get(0));

		//Set mirror cam transform
		IMirror mirror = mirrorEntity.getMirrors().get(0);
		ITransform camTransform = cameraEntity.getTransforms().get(0);
		ITransform mirrorCamTransform = mirrorCamEntity.getTransforms().get(0);
		mirrorCamTransform.setWorldTransform(mirror.getViewMatrix(camTransform.getWorldTransform()));

		//Set mirror material
		mirrorCam.setMaterial(mirror.getMaterial());

		return mirrorCam;
	}

	//TODO refactoring 
	private ICamera createPortalCam(IEntity portalEntity, IEntity cameraEntity) {
		// Crate camera copy. The camera is docked on mirrorCamEntity attribute
		// Not clear, TODO refactoring
		ICamera portalCam = copyCameraComponent(cameraEntity.getCameras().get(0));

		//Set portal cam transform
		IPortal portal = portalEntity.getPortals().get(0);
		ITransform camTransform = cameraEntity.getTransforms().get(0);
		ITransform portalCamTransform = mirrorCamEntity.getTransforms().get(0);
		portalCamTransform.setWorldTransform(portal.getViewMatrix(camTransform.getWorldTransform()));
		
		//Set mirror material
		portalCam.setMaterial(portal.getMaterial());

		return portalCam;
	}

	private void mirrorRenderingPass(GL4 gl, ICamera camera, IEntity mirrorEntity) {
		loadedMeshPath = null;
		loadedMaterialPath = null;
		List<IMesh> meshes = mirrorEntity.getMeshes();
		for (IMesh mesh : meshes) {
			if (mesh != null && mesh.getMaterial() != null) {
				if (!isAlradyLoaded(mesh.getMeshDef())) {
					//Push Vertices
					fillVBO(gl, 
							mesh.getMeshDef().getPosTable(), 
							mesh.getMeshDef().getColTable(), 
							mesh.getMeshDef().getOffTable(), 
							mesh.getMeshDef().getNorTable());

					//Push Elements
					fillEBO(gl, mesh.getMeshDef().getIdxTable());

					loadedMeshPath = mesh.getMeshDef().getPath();
				}

				//Set the Material Active
				setUpMaterial(gl, mirrorEntity, mesh.getMaterial(), camera);

				//Draw elements
				gl.glDrawElements(GL4.GL_TRIANGLES, elementsData.length, GL4.GL_UNSIGNED_INT, 0);
			}
		}
	}

	private void portalRenderingPass(GL4 gl, ICamera camera, IEntity portalEntity) {
		loadedMeshPath = null;
		loadedMaterialPath = null;
		List<IMesh> meshes = portalEntity.getMeshes();
		for (IMesh mesh : meshes) {
			if (mesh != null && mesh.getMaterial() != null) {
				if (!isAlradyLoaded(mesh.getMeshDef())) {
					//Push Vertices
					fillVBO(gl, 
							mesh.getMeshDef().getPosTable(), 
							mesh.getMeshDef().getColTable(), 
							mesh.getMeshDef().getOffTable(), 
							mesh.getMeshDef().getNorTable());

					//Push Elements
					fillEBO(gl, mesh.getMeshDef().getIdxTable());
					
					loadedMeshPath = mesh.getMeshDef().getPath();
				}

				//Set the Material Active
				setUpMaterial(gl, portalEntity, mesh.getMaterial(), camera);

				//Draw elements
				gl.glDrawElements(GL4.GL_TRIANGLES, elementsData.length, GL4.GL_UNSIGNED_INT, 0);
			}
		}
	}

	private void postEffectRendering(GL4 gl, ICamera camera, int osTexId) {
		fillScreenVBO(gl);
		fillScreenEBO(gl);

		IMaterial mat = camera.getMaterial();

		//Create a program Shader
		int vertexShader = loadShaderFromSource(gl, mat.getMaterialDef().getVertexShaderSource(), GL4.GL_VERTEX_SHADER);
		int fragmentShader = loadShaderFromSource(gl, mat.getMaterialDef().getFragmentShaderSource(), GL4.GL_FRAGMENT_SHADER);
		int shaderProgram = buildShaderProgram(gl, vertexShader, fragmentShader);

		//Make program Shader the active program
		gl.glUseProgram(shaderProgram);

		//Bind Shader program attributes
		bindVBOAttributes(gl, shaderProgram);

		gl.glActiveTexture(GL4.GL_TEXTURE0 + osTexId);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, osTexId);
		int texParamId = gl.glGetUniformLocation(shaderProgram, "pst_screenTexture");
		gl.glUniform1i(texParamId, osTexId);

		bindMaterialUniforms(gl, shaderProgram, mat);

		//Draw elements
		gl.glDrawElements(GL4.GL_TRIANGLES, elementsData.length, GL4.GL_UNSIGNED_INT, 0);
	}

	private void fillScreenVBO(GL4 gl) {
		//Meshes Vertices Buffer
		verticesData = new float[] {
				-1f, -1f, 0f, 	0f, 0f, 0f,		0f, 0f, 	0f, 0f, 0f,
				-1f, 1f, 0f, 	0f, 0f, 0f,		0f, 1f, 	0f, 0f, 0f,
				1f, 1f, 0f, 	0f, 0f, 0f,		1f, 1f, 	0f, 0f, 0f,
				1f, -1f, 0f, 	0f, 0f, 0f,		1f, 0f, 	0f, 0f, 0f
		};

		//Make vbo the active array buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]);
		//Copies vertices into the active buffer (vbo)
		FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(verticesData); 
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, verticesData.length * 4, floatBuffer, GL4.GL_STATIC_DRAW);
	}

	private void fillScreenEBO(GL4 gl) {
		//Meshes Elements Buffer
		elementsData = new int[] {
				0, 1, 2,
				0, 2, 3
		};

		//Make ebo the active element buffer
		gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
		//Copies vertices into the active buffer (ebo)
		IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(elementsData); 
		gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, elementsData.length * 4, intBuffer, GL4.GL_STATIC_DRAW);
	}

	private ICamera copyCameraComponent(ICamera camera) {
		ICamera mirrorCam = new Camera(mirrorCamEntity);
		ITransform mirrorCamTransform = new Transform(mirrorCamEntity);	
		mirrorCam.setWidth(camera.getWidth());
		mirrorCam.setHeight(camera.getHeight());
		mirrorCam.setNear(camera.getNear());
		mirrorCam.setFar(camera.getFar());
		mirrorCam.setFov(camera.getFov());
		mirrorCam.setViewport(camera.getViewport());
		mirrorCam.setOrtho(camera.isOrtho());
		mirrorCam.setMaterial(camera.getMaterial());
		return mirrorCam;
	}

	private void meshesRenderingPass(GL4 gl, ICamera camera) {
		loadedMeshPath = null;
		loadedMaterialPath = null;
		for (IEntity entity : meshQueue) {
			List<IMesh> meshes = entity.getMeshes();
			for (IMesh mesh : meshes) {
				if (mesh != null && mesh.getMaterial() != null && matchCameraLayers(mesh, camera)) {
					if (!isAlradyLoaded(mesh.getMeshDef())) {
						//Push Vertices
						fillVBO(gl, 
								mesh.getMeshDef().getPosTable(), 
								mesh.getMeshDef().getColTable(), 
								mesh.getMeshDef().getOffTable(), 
								mesh.getMeshDef().getNorTable());

						//Push Elements
						fillEBO(gl, mesh.getMeshDef().getIdxTable());

						loadedMeshPath = mesh.getMeshDef().getPath();
					}
					//Set the Material Active
					setUpMaterial(gl, entity, mesh.getMaterial(), camera);

					//Draw elements
					gl.glDrawElements(GL4.GL_TRIANGLES, elementsData.length, GL4.GL_UNSIGNED_INT, 0);
				}
			}
		}
	}

	private boolean isAlradyLoaded(IMeshDef meshDef) {
		if (loadedMeshPath!= null && meshDef.getPath().equals(loadedMeshPath)) {
			return true;
		}
		return false;
	}

	private boolean isAlradyLoaded(IMaterialDef materialDef) {
		String curMaterialPath = MaterialManager.getInstance().buildMaterialDefKey(
				materialDef.getVertexShaderPath(), 
				materialDef.getFragmentShaderPath());
		if (loadedMaterialPath != null && curMaterialPath.equals(loadedMaterialPath)) {
			return true;
		}
		return false;
	}

	private boolean matchCameraLayers(IMesh mesh, ICamera camera) {
		for (int i = 0; i < NB_CAM_LAYERS; i++) {
			if (mesh.isEnabledLayer(i) && camera.isEnabledLayer(i)) {
				return true;
			}
		}
		return false;
	}

	private void curvesRenderingPass(GL4 gl, ICamera camera) {
		for (IEntity entity : curveQueue) {
			List<ICurve> curves = entity.getCurves();
			for (ICurve curve : curves) {
				if (curve != null && curve.getMaterial() != null) {
					float[][] emptyTable = new float[0][0];
					IMaterial curveMat = curve.getMaterial();

					//Draw Controls
					//Push Vertices
					fillVBO(gl, 
							curve.getControlPtsTable(), 
							emptyTable, 
							emptyTable, 
							emptyTable);

					//Set the Material Active
					curveMat.setVec3Param("mat_color", 0.2f, 0.2f, 0.2f);
					setUpMaterial(gl, entity, curveMat, camera);

					//Draw elements
					gl.glDrawArrays(GL4.GL_LINE_STRIP, 0, curve.getControlPtsTable().length);

					//Draw points
					//Push Vertices
					fillVBO(gl, 
							curve.getPtsTable(), 
							emptyTable, 
							emptyTable, 
							emptyTable);

					//Set the Material Active
					curveMat.setVec3Param("mat_color", 0.8f, 0.8f, 0.8f);
					setUpMaterial(gl, entity, curveMat, camera);

					//Draw elements
					gl.glDrawArrays(GL4.GL_LINE_STRIP, 0, curve.getPtsTable().length);
				}
			}
		}
	}

	/**
	 * Create an atomic pipeline program composed by a vertex and a fragment Shader.
	 * The only output variable "outColor" is bound to the default buffer (0)
	 * @param gl : the OpenGL Context
	 * @param vertexShader : the id used by OpenGL referencing the given vertex shader.
	 * @param fragmentShader : the id used by OpenGL referencing the given fragment shader.
	 * @return the id used by OpenGL referencing the program
	 */
	private int buildShaderProgram(GL4 gl, int vertexShader, int fragmentShader) {
		//Create the shader program
		int shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertexShader);
		gl.glAttachShader(shaderProgram, fragmentShader);

		//Bind output variable of fragment shader to the default buffer (0)
		gl.glBindFragDataLocation(shaderProgram, 0, "outColor");

		//Effective build program shader from previously attached shader
		gl.glLinkProgram(shaderProgram);

		return shaderProgram;
	}

	private int loadShaderFromSource(final GL4 gl, String vertexSource, final int shaderType) {
		String[] vertexSources = {vertexSource};

		//Creating the Shader
		int vertexShader = gl.glCreateShader(shaderType);
		gl.glShaderSource(vertexShader, 1, vertexSources, null);

		//Compiling the Shader
		gl.glCompileShader(vertexShader);

		byte[] infoLog = new byte[512];
		gl.glGetShaderInfoLog(vertexShader, 512, null, 0, infoLog, 0);
		if (debug) {
			String infoLogStr = new String(infoLog);
			infoLogStr = infoLogStr.trim();
			if (!infoLogStr.equals("")) {
				System.out.println(infoLogStr);
			}
		}

		return vertexShader;
	}

	private void setUpMaterial(GL4 gl, IEntity entity, IMaterial mat, ICamera cam) {
		if (!isAlradyLoaded(mat.getMaterialDef())) {
			//Create a program Shader
			int vertexShader = loadShaderFromSource(gl, mat.getMaterialDef().getVertexShaderSource(), GL4.GL_VERTEX_SHADER);
			int fragmentShader = loadShaderFromSource(gl, mat.getMaterialDef().getFragmentShaderSource(), GL4.GL_FRAGMENT_SHADER);
			shaderProgram = buildShaderProgram(gl, vertexShader, fragmentShader);

			//Make program Shader the active program
			gl.glUseProgram(shaderProgram);

			String curMaterialPath = MaterialManager.getInstance().buildMaterialDefKey(
					mat.getMaterialDef().getVertexShaderPath(), 
					mat.getMaterialDef().getFragmentShaderPath());
			loadedMaterialPath = curMaterialPath;
		}

		//Bind Shader program attributes
		bindVBOAttributes(gl, shaderProgram);

		//Bind model Matrix Components
		ITransform modelTransform = entity.getTransforms().get(0);
		float[] worldTMatrix = Matrices.flat(modelTransform.getWorldTranslate());
		float[] worldRMatrix = Matrices.flat(modelTransform.getWorldRotate());
		float[] worldSMatrix = Matrices.flat(modelTransform.getWorldScale());
		int paramId  = gl.glGetUniformLocation(shaderProgram, "mod_TMatrix");
		gl.glUniformMatrix4fv(paramId, 1, false, worldTMatrix, 0);
		paramId  = gl.glGetUniformLocation(shaderProgram, "mod_RMatrix");
		gl.glUniformMatrix4fv(paramId, 1, false, worldRMatrix, 0);
		paramId  = gl.glGetUniformLocation(shaderProgram, "mod_SMatrix");
		gl.glUniformMatrix4fv(paramId, 1, false, worldSMatrix, 0);

		//Bind Camera attributes
		bindCameraUniforms(gl, shaderProgram, cam);

		bindLightUniforms(gl, shaderProgram);

		bindMaterialUniforms(gl, shaderProgram, mat);
	}

	/**
	 * Bound vertices datas to the Shader program attributes
	 * @param gl
	 * @param shaderProgram
	 */
	private void bindVBOAttributes(GL4 gl, int shaderProgram) {
		//Bind the "position" attribute according to the schema
		int posAttr = gl.glGetAttribLocation(shaderProgram, "position");
		gl.glEnableVertexAttribArray(posAttr);
		gl.glVertexAttribPointer(posAttr, 3, GL4.GL_FLOAT, false, 11 * 4, 0);

		//Bind the "color" attribute according to the schema
		int colAttrib = gl.glGetAttribLocation(shaderProgram, "color");
		gl.glEnableVertexAttribArray(colAttrib);
		gl.glVertexAttribPointer(colAttrib, 3, GL4.GL_FLOAT, false, 11 * 4, 3 * 4);

		//Bind the "texcoord" attribute according to the schema
		int texcoordAttrib = gl.glGetAttribLocation(shaderProgram, "texcoord");
		gl.glEnableVertexAttribArray(texcoordAttrib);
		gl.glVertexAttribPointer(texcoordAttrib, 2, GL4.GL_FLOAT, false, 11 * 4, 6 * 4);

		//Bind the "normal" attribute according to the schema
		int normalAttrib = gl.glGetAttribLocation(shaderProgram, "normal");
		gl.glEnableVertexAttribArray(normalAttrib);
		gl.glVertexAttribPointer(normalAttrib, 3, GL4.GL_FLOAT, false, 11 * 4, 8 * 4);
	}

	private void bindMaterialUniforms(GL4 gl, int shaderProgram, IMaterial mat) {
		//Bind float attrs
		for (Entry<String, Float[]> entry : mat.getAllVec3Params()) {
			String paramName = entry.getKey();
			Float[] paramData = entry.getValue();
			int paramId  = gl.glGetUniformLocation(shaderProgram, paramName);
			gl.glUniform3f(paramId, paramData[0], paramData[1], paramData[2]);
		}

		//Bind vec3 attrs
		for (Entry<String, Float> entry : mat.getAllFloatParams()) {
			String paramName = entry.getKey();
			Float paramData = entry.getValue();
			int paramId  = gl.glGetUniformLocation(shaderProgram, paramName);
			if (paramData != null) {
				gl.glUniform1f(paramId, paramData);
			}
		}

		//Bind sampler2D attrs
		int texInd = 0;
		for (Entry<String, ITexture> entry : mat.getAllTexParams()) {
			String textureName = entry.getKey();
			ITexture textureData = entry.getValue();
			if (textureData.getPixBuffer() != null) {
				MaterialManager materialManager = MaterialManager.getInstance();
				List<ITextureLocation> textureLocations = materialManager.getTextureLocations(textureData);
				if (textureLocations.size() > 0) {
					texInd = textureLocations.get(0).getAtlasId();

					//Create and Bind Texture
					gl.glActiveTexture(GL4.GL_TEXTURE0 + texInd);
					gl.glBindTexture(GL4.GL_TEXTURE_2D, tex[texInd]);
				} else {
					texInd = materialManager.assignTextureLocation(textureData);

					//Create and Bind Texture
					gl.glActiveTexture(GL4.GL_TEXTURE0 + texInd);
					gl.glBindTexture(GL4.GL_TEXTURE_2D, tex[texInd]);
					gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 1, GL4.GL_RGB8, textureData.getWidth(), textureData.getHeight());
					FloatBuffer imgBuffer = GLBuffers.newDirectFloatBuffer(textureData.getPixBuffer());
					gl.glTexSubImage2D(GL4.GL_TEXTURE_2D, 0, 0, 0, 
							textureData.getWidth(), textureData.getHeight(), 
							GL4.GL_RGBA, GL4.GL_FLOAT, imgBuffer);
				}
				int texParamId = gl.glGetUniformLocation(shaderProgram, textureName);
				gl.glUniform1i(texParamId, texInd);
			}
		}
	}

	private void bindLightUniforms(GL4 gl, int shaderProgram) {
		//Bind Light attrs
		for (int i = 0; i < lightQueue.size(); i++) {
			IEntity lightEntity = lightQueue.get(i);
			float[] lightPosition = new float[3];
			ITransform transform = lightEntity.getTransforms().get(0);
			lightPosition[0] = ((Vec4) transform.getWorldTranslate().getColumn(3)).getX();
			lightPosition[1] = ((Vec4) transform.getWorldTranslate().getColumn(3)).getY();
			lightPosition[2] = ((Vec4) transform.getWorldTranslate().getColumn(3)).getZ();

			ILight light = lightEntity.getLights().get(0);
			float r = ((float) light.getAlbedo().getRed()) / 255f;
			float g = ((float) light.getAlbedo().getGreen()) / 255f;
			float b = ((float) light.getAlbedo().getBlue()) / 255f;
			float[] lightAlbedo = {r, g, b};
			float lightIntensity = light.getIntensity();

			int lightParamId2  = gl.glGetUniformLocation(shaderProgram, "lgt_albedo[" + i + "]");
			gl.glUniform3f(lightParamId2, lightAlbedo[0], lightAlbedo[1], lightAlbedo[2]);
			int lightParamId1  = gl.glGetUniformLocation(shaderProgram, "lgt_position[" + i + "]");
			gl.glUniform3f(lightParamId1, lightPosition[0], lightPosition[1], lightPosition[2]);
			int lightParamId3  = gl.glGetUniformLocation(shaderProgram, "lgt_intensity[" + i + "]");
			gl.glUniform1f(lightParamId3, lightIntensity);
		}
	}

	private void bindCameraUniforms(GL4 gl, int shaderProgram, ICamera cam) {
		//Bind Camera attrs
		int viewMatrixParamId  = gl.glGetUniformLocation(shaderProgram, "cam_viewMatrix");
		float[] flatViewMatrix = Matrices.flat(cam.getViewMatrix());
		gl.glUniformMatrix4fv(viewMatrixParamId, 1, false, flatViewMatrix, 0);

		int projMatrixParamId  = gl.glGetUniformLocation(shaderProgram, "cam_projMatrix");
		float[] flatProjMatrix = Matrices.flat(cam.getProjMatrix());
		gl.glUniformMatrix4fv(projMatrixParamId, 1, false, flatProjMatrix, 0);
	}

	private void fillVBO(GL4 gl, float[][] worldPosTable, float[][] colTable, float[][] offTable, float[][] norTable) {
		List<Float> verticesDataLst = new ArrayList<Float>();
		for (int i = 0; i < worldPosTable.length; i++) {
			verticesDataLst.add(worldPosTable[i][0]);
			verticesDataLst.add(worldPosTable[i][1]);
			verticesDataLst.add(worldPosTable[i][2]);

			if (colTable.length > 0) {
				verticesDataLst.add(colTable[i][0]);
				verticesDataLst.add(colTable[i][1]);
				verticesDataLst.add(colTable[i][2]);
			} else {
				verticesDataLst.add(1f);
				verticesDataLst.add(1f);
				verticesDataLst.add(1f);
			}

			if (offTable.length > 0) {
				verticesDataLst.add(offTable[i][0]);
				verticesDataLst.add(offTable[i][1]);
			} else {
				verticesDataLst.add(0f);
				verticesDataLst.add(0f);
			}

			if (norTable.length > 0) {
				verticesDataLst.add(norTable[i][0]);
				verticesDataLst.add(norTable[i][1]);
				verticesDataLst.add(norTable[i][2]);
			} else {
				verticesDataLst.add(0f);
				verticesDataLst.add(0f);
				verticesDataLst.add(1f);
			}
		}

		//Meshes Vertices Buffer
		verticesData = new float[verticesDataLst.size()];
		for (int i = 0; i < verticesDataLst.size(); i++) {
			verticesData[i] = verticesDataLst.get(i);
		}

		//Make vbo the active array buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]);
		//Copies vertices into the active buffer (vbo)
		FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(verticesData); 
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, verticesData.length * 4, floatBuffer, GL4.GL_STATIC_DRAW);
	}

	private void fillEBO(GL4 gl, int[][] idxTable) {
		List<Integer> elementsDataLst = new ArrayList<Integer>();
		for (int i = 0; i < idxTable.length; i++) {
			elementsDataLst.add(idxTable[i][0]);
			elementsDataLst.add(idxTable[i][1]);
			elementsDataLst.add(idxTable[i][2]);
		}

		//Meshes Elements Buffer
		elementsData = new int[elementsDataLst.size()];
		for (int i = 0; i < elementsDataLst.size(); i++) {
			elementsData[i] = elementsDataLst.get(i);
		}

		//Make ebo the active element buffer
		gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
		//Copies vertices into the active buffer (ebo)
		IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(elementsData); 
		gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, elementsData.length * 4, intBuffer, GL4.GL_STATIC_DRAW);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
		gl.glViewport(0, 0, width, height);
	}
}
