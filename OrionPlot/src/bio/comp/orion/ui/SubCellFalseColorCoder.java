package bio.comp.orion.ui;

import java.awt.Color;

public interface SubCellFalseColorCoder{
	Color colorForSubCell(int row, int cell, int subCell, int subCellValue);
}
