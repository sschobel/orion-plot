package bio.comp.orion.model;

public interface MatrixReader {
	DataLine[] getMatrix();
	MatrixHeader[] getHeaders();
	String[] getHeaderNames();
}
