package bio.comp.orion.ui;

import java.awt.Color;

public interface ColorIndexConstants {
    static final int COLOR_INDEXES_ARE_PAIRS = 2;
    static final int INDEX_TABLE_COLUMN = 0;
    static final int COLOR_TABLE_COLUMN = 1;
    static final String INDEX_TABLE_COLUMN_NAME = "Value";
    static final String COLOR_TABLE_COLUMN_NAME = "Color";
    static final Class<?> INDEX_TABLE_COLUMN_CLASS = Integer.class;
    static final Class<?> COLOR_TABLE_COLUMN_CLASS = Color.class;
    static final Class<?>[] TABLE_COLUMN_CLASSES = new Class<?>[] { INDEX_TABLE_COLUMN_CLASS, COLOR_TABLE_COLUMN_CLASS };
    static final String[] TABLE_COLUMN_NAMES = new String[] { INDEX_TABLE_COLUMN_NAME, COLOR_TABLE_COLUMN_NAME };
}
