package fr.jponzo.gamagora.modelgeo.tpVol;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.modelgeo.MutableMeshDef;
import fr.jponzo.gamagora.modelgeo.Sphere;
import fr.jponzo.gamagora.modelgeo.tp5.ICurve;
import fr.jponzo.gamagora.nutshell3d.input.InputManager;
import fr.jponzo.gamagora.nutshell3d.input.KeyCode;
import fr.jponzo.gamagora.nutshell3d.material.impl.MaterialManager;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.runtime.RuntimeSystem;
import fr.jponzo.gamagora.nutshell3d.scene.SceneManager;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractUpdator;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Camera;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Entity;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Light;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Mesh;
import fr.jponzo.gamagora.nutshell3d.scene.impl.MeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Transform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.io.IOUtils;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class MGTPVolEx1App {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 800;
	private static int height = 600;

	private static float cubeSize = 1;
	private static float discW = 1f;
	private static float discH = 1f;
	private static float discD = 1f;
	private static int xMinBound = -10;
	private static int xMaxBound = 10;
	private static int yMinBound = -10;
	private static int yMaxBound = 10;
	private static int zMinBound = -10;
	private static int zMaxBound = 10;
	private static float[][][] voxGrid;
	private static List<Sphere> spheres = new ArrayList<Sphere>();
	private static int selectedSphere = 0;
	private static boolean isInterOp = false;

	public static void main(String[] args) throws InterruptedException, OperationNotSupportedException {

		//creating frame
		final Frame frame = new Frame (APP_NAME);

		//adding canvas to frame
		GLCanvas glCanvas = RenderingSystem.getInstance().getGlcanvas();
		frame.add(glCanvas);

		//Configure frame
		frame.setSize( width, height);
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {				
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						System.exit(0);
					}
				}.start();
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				width = e.getComponent().getWidth();
				height = e.getComponent().getHeight();

				ICamera camera = SceneManager.getInstance().getActiveCamera();
				if (camera != null) {
					float w = ((float) camera.getViewport().width) / 100f;
					float h = ((float) camera.getViewport().height) / 100f;
					camera.setWidth((int) ((float)width * w));
					camera.setHeight((int) ((float)height * h));
				}
			}
		});

		configureScene();

		SceneManager.getInstance().initPass();

		RenderingSystem.getInstance().setBatchDrawCalls(false);

		RuntimeSystem.getInstance().start();

		RuntimeSystem.getInstance().play();
	}

	private static void createVoxels(IEntity rootEntity) throws OperationNotSupportedException {
		//Compute disc
		discW = 1f / cubeSize;
		discH = 1f / cubeSize;
		discD = 1f / cubeSize;

		//Create voxel space
		initVoxGrid(spheres);

		//Create meshes from voxel space
		IEntity cubesRoot = cubesFromGrid();
		if (rootEntity.getChildsCount() > 1) {
			rootEntity.removeChild(1);
		}
		rootEntity.addChild(cubesRoot);
	}

	private static IEntity cubesFromGrid() throws OperationNotSupportedException {
		ITransform transform;

		IEntity cubesRoot = new Entity();
		new Transform(cubesRoot);

		IMeshDef cubeMeshDef = new MeshDef();
		cubeMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Cube.obj");
		cubeMeshDef.load();

		IMaterial meshMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLight.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLight.frag");
		meshMat.setVec3Param("mat_diffuseColor", 0.5f, 0.5f, 0.5f);

		int w = (int) ((xMaxBound - xMinBound) * discW);
		int h = (int) ((yMaxBound - yMinBound) * discH);
		int d = (int) ((zMaxBound - zMinBound) * discD);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				for (int k = 0; k < d; k++) {
					if (voxGrid[i][j][k] > 0) {
						if (i == 0 || j == 0 || k == 0 || i == w - 1 || j == h - 1 || k == d - 1 
								|| !(voxGrid[i + 1][j][k] > 0 
										&& voxGrid[i - 1][j][k] > 0 
										&& voxGrid[i][j + 1][k] > 0 
										&& voxGrid[i][j - 1][k] > 0 
										&& voxGrid[i][j][k + 1] > 0 
										&& voxGrid[i][j][k - 1] > 0)) {
							IEntity cubeEntity = new Entity();
							transform = new Transform(cubeEntity);
							transform.setLocalTranslate(Matrices.translation(
									(float) i / (float) discW + xMinBound, 
									(float) j / (float) discH + yMinBound, 
									(float) k / (float) discD + zMinBound));
							transform.setLocalScale(Matrices.scale(
									1f / (float) discW, 1f / (float) discH, 1f / (float) discD));
							IMesh cubeMesh = new Mesh(cubeEntity, cubeMeshDef);
							cubesRoot.addChild(cubeEntity);
							cubeMesh.setMaterial(meshMat);
						}
					}
				}
			}
		}

		return cubesRoot;
	}

	private static void initVoxGrid(List<Sphere> spheres) {
		Sphere firstSphere = spheres.get(0);
		xMinBound = (int) (firstSphere.getOrigin().getX() - firstSphere.getRadius()) - 1;
		xMaxBound = (int) (firstSphere.getOrigin().getX() + firstSphere.getRadius()) + 1;
		yMinBound = (int) (firstSphere.getOrigin().getY() - firstSphere.getRadius()) - 1;
		yMaxBound = (int) (firstSphere.getOrigin().getY() + firstSphere.getRadius()) + 1;
		zMinBound = (int) (firstSphere.getOrigin().getZ() - firstSphere.getRadius()) - 1;
		zMaxBound = (int) (firstSphere.getOrigin().getZ() + firstSphere.getRadius()) + 1;
		for (int i = 1; i < spheres.size(); i++) {
			Sphere sphere = spheres.get(i);
			int curXMinBound = (int) (sphere.getOrigin().getX() - firstSphere.getRadius()) - 1;
			int curXMaxBound = (int) (sphere.getOrigin().getX() + firstSphere.getRadius()) + 1;
			int curYMinBound = (int) (sphere.getOrigin().getY() - firstSphere.getRadius()) - 1;
			int curYMaxBound = (int) (sphere.getOrigin().getY() + firstSphere.getRadius()) + 1;
			int curZMinBound = (int) (sphere.getOrigin().getZ() - firstSphere.getRadius()) - 1;
			int curZMaxBound = (int) (sphere.getOrigin().getZ() + firstSphere.getRadius()) + 1;

			if (xMinBound > curXMinBound) {
				xMinBound = curXMinBound;
			}
			if (xMaxBound < curXMaxBound) {
				xMaxBound = curXMaxBound;
			}
			if (yMinBound > curYMinBound) {
				yMinBound = curYMinBound;
			}
			if (yMaxBound < curYMaxBound) {
				yMaxBound = curYMaxBound;
			}
			if (zMinBound > curZMinBound) {
				zMinBound = curZMinBound;
			}
			if (zMaxBound < curZMaxBound) {
				zMaxBound = curZMaxBound;
			}
		}

		int w = (int) ((xMaxBound - xMinBound) * discW);
		int h = (int) ((yMaxBound - yMinBound) * discH);
		int d = (int) ((zMaxBound - zMinBound) * discD);
		voxGrid = new float[w][h][d];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				for (int k = 0; k < d; k++) {
					if (isInterOp) {
						voxGrid[i][j][k] = 0f;
						for (Sphere sphere : spheres) {
							Vec3 voxelPos = new Vec3((float) i / (float) discW + xMinBound, 
									(float) j / (float) discH + yMinBound, 
									(float) k / (float) discD + zMinBound);
							float dist = voxelPos.subtract(sphere.getOrigin()).getLength();
							if (dist < sphere.getRadius()) {
								voxGrid[i][j][k]++;
							}
						}
						if (voxGrid[i][j][k] == spheres.size()) {
							voxGrid[i][j][k] = 1;
						} else {
							voxGrid[i][j][k] = 0;
						}
					} else {
						voxGrid[i][j][k] = 0f;
						for (Sphere sphere : spheres) {
							Vec3 voxelPos = new Vec3((float) i / (float) discW + xMinBound, 
									(float) j / (float) discH + yMinBound, 
									(float) k / (float) discD + zMinBound);
							float dist = voxelPos.subtract(sphere.getOrigin()).getLength();
							if (dist < sphere.getRadius()) {
								voxGrid[i][j][k] = 1f;
							}
						}
					}
				}
			}
		}
	}

	private static void configureScene() throws OperationNotSupportedException {
		//Create root
		IEntity rootEntity = new Entity();
		ITransform transform = new Transform(rootEntity);
		SceneManager.getInstance().setRoot(rootEntity);

		//Create Camera
		IEntity cameraEntity = new Entity();
		transform = new Transform(cameraEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, -30));
		ICamera camera = new Camera(cameraEntity);
		camera.setWidth(width);
		camera.setHeight(height);
		camera.setNear(0.001f);
		camera.setFar(100);
		camera.setFov(60f);
		camera.setViewport(
				new Rectangle(0, 0, 100, 100)
				);
		camera.setOrtho(false);
		IMaterial camMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\idPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\idPostEffect.frag");
		camera.setMaterial(camMat);
		rootEntity.addChild(cameraEntity);
		new AbstractUpdator(cameraEntity) {
			private float moveSpeed = 10f;
			private float rotSpeed = (float) Math.PI / 3f;
			private float offset = 0.1f;

			@Override
			public void update(long deltaTime) {
				//Move
				ITransform transform = entity.getTransforms().get(0);
				if (InputManager.getInstance().getKey(KeyCode.R)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getUp().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.F)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getUp().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.Z)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getFwd().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.S)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getFwd().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.Q)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getRight().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.D)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getRight().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}

				//Turn
				Mat4 localRotation = transform.getLocalRotate();
				Mat4 offsetHRotation = Mat4.MAT4_IDENTITY;
				Mat4 offsetVRotation = Mat4.MAT4_IDENTITY;
				if (InputManager.getInstance().getKey(KeyCode.Left)) {
					offsetHRotation = Matrices.yRotation(rotSpeed * ((float) deltaTime / 1000f));
				}
				if (InputManager.getInstance().getKey(KeyCode.Right)) {
					offsetHRotation = Matrices.yRotation(-rotSpeed * ((float) deltaTime / 1000f));
				}
				if (InputManager.getInstance().getKey(KeyCode.Up)) {
					offsetVRotation = Matrices.rotate(-rotSpeed * ((float) deltaTime / 1000f), transform.getRight());
				}
				if (InputManager.getInstance().getKey(KeyCode.Down)) {
					offsetVRotation = Matrices.rotate(rotSpeed * ((float) deltaTime / 1000f), transform.getRight());
				}
				transform.setLocalRotate(offsetHRotation.multiply(offsetVRotation).multiply(localRotation));

				//Discretisation
				if (InputManager.getInstance().getKeyDown(KeyCode.PageUp)) {
					cubeSize *= 1f / 0.9f;
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.PageDown)) {
					cubeSize *= 0.9f;
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				//Select Sphere
				if (InputManager.getInstance().getKeyDown(KeyCode.W)) {
					if (selectedSphere < spheres.size() - 1) {
						selectedSphere += 1;
					}
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.X)) {
					if (selectedSphere > 0) {
						selectedSphere -= 1;
					}
				}

				//Move sphere
				//FWD
				if (InputManager.getInstance().getKeyDown(KeyCode.I)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX(), selected.getOrigin().getY(), selected.getOrigin().getZ() + offset);
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//BCK
				if (InputManager.getInstance().getKeyDown(KeyCode.K)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX(), selected.getOrigin().getY(), selected.getOrigin().getZ() - offset);
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//LEFT
				if (InputManager.getInstance().getKeyDown(KeyCode.J)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX() - offset, selected.getOrigin().getY(), selected.getOrigin().getZ());
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//RIGHT
				if (InputManager.getInstance().getKeyDown(KeyCode.L)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX() + offset, selected.getOrigin().getY(), selected.getOrigin().getZ());
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//UP
				if (InputManager.getInstance().getKeyDown(KeyCode.P)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX(), selected.getOrigin().getY() + offset, selected.getOrigin().getZ());
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//DOWN
				if (InputManager.getInstance().getKeyDown(KeyCode.M)) {
					Sphere selected = spheres.get(selectedSphere);
					Vec3 newOrigin = new Vec3(selected.getOrigin().getX(), selected.getOrigin().getY() - offset, selected.getOrigin().getZ());
					selected.setOrigin(newOrigin);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				//SCALE SPHERE
				if (InputManager.getInstance().getKeyDown(KeyCode.Y)) {
					Sphere selected = spheres.get(selectedSphere);
					selected.setRadius(selected.getRadius() + 0.1f);
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.H)) {
					Sphere selected = spheres.get(selectedSphere);
					if (selected.getRadius() > 0) {
						selected.setRadius(selected.getRadius() - 0.1f);
					}
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				//TOGGLE OPERATOR
				if (InputManager.getInstance().getKeyDown(KeyCode.T)) {
					isInterOp = !isInterOp;
					try {
						createVoxels(rootEntity);
					} catch (OperationNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void init() {
			}
		};
		SceneManager.getInstance().setActiveCamera(camera);

		//Create Light
		IEntity lightEntity = new Entity();
		transform = new Transform(lightEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
		ILight light = new Light(lightEntity);
		light.setAlbedo(
				new Color(255, 255, 255, 255)
				);
		light.setIntensity(100f);
		cameraEntity.addChild(lightEntity);

		//Create spheres
		Sphere s1 = new Sphere(new Vec3(2.0f, 0.0f, 0.0f), 3f);
		spheres.add(s1);
		Sphere s2 = new Sphere(new Vec3(-2.0f, 0.0f, 0.0f), 3f);
		spheres.add(s2);

		createVoxels(rootEntity);
	}
}
