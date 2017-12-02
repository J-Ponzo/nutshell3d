package fr.jponzo.gamagora.nutshell3d.scene.interfaces;

import java.awt.Color;

public interface ILight extends IComponent {

	Color getColor();

	void setColor(Color color);

	float getIntensity();

	void setIntensity(float intensity);

}
