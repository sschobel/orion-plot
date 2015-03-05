package bio.comp.orion.model;

import java.awt.Color;

public interface MatrixHeader {
	public String getLineName();
	public Color getLineColor(Color defaultColor);
	public Double getLineWidth(Double defaultWidth);
	public Color getLineColor();
	public Double getLineWidth();

}
