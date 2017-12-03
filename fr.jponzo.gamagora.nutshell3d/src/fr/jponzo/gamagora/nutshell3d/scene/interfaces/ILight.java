package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import java.awt.Color;

public interface ILight extends IComponent {

	Color getAlbedo();

	void setAlbedo(Color color);

	float getIntensity();

	void setIntensity(float intensity);

}
