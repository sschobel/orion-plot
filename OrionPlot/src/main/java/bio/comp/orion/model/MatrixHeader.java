package bio.comp.orion.model;

import java.awt.Color;

public interface MatrixHeader {
	public String getName();
	public Color getColor(Color defaultColor);
	public Double getWidth(Double defaultWidth);
	public Color getColor();
	public Double getWidth();
	public Double getHeight();

}
