package bio.comp.orion.model;

public class Preconditions {
	public interface Precondition{
		public boolean met();
		public boolean notMet();
		public void enforce(); //Throws RuntimeException if notMet() returns true;
	}
	static class PreconditionUnmetException extends RuntimeException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 106659409943150608L;
		Precondition _unmet;
		public PreconditionUnmetException(Precondition unmet) {
			// TODO Auto-generated constructor stub
			super(String.format("Unmet precondition : %s", unmet));
			_unmet = unmet;
		}
	}
	static abstract class AbstractPrecondition implements Precondition{

		public abstract boolean met(); 

		@Override
		public boolean notMet() {
			
			return !met();
		}
		
		@Override
		public void enforce(){
			if(notMet()){
				throw new PreconditionUnmetException(this);
			}
		}
		
	}
	static class NotNull extends AbstractPrecondition{
		Object[] _objects;
		NotNull(Object... objects){
			_objects = objects;
		}
		@Override
		public boolean met() {
			
			for(Object obj : _objects){
				if(obj == null){
					return false;
				}
			}
			return true;
		}
		@Override
		public String toString(){
			return String.format("Precondition (Not-Null) : %s", _objects); 
		}
	}
	static class Not extends AbstractPrecondition{
		Precondition _precon;
		Not(Precondition precon){
			_precon = precon;
		}

		@Override
		public boolean met() {
			
			return _precon.notMet();
		}
	}
	static class MetAlways extends AbstractPrecondition{

		@Override
		public boolean met() {
			
			return true;
		}
		
	}
	static class MetNever extends AbstractPrecondition{

		@Override
		public boolean met() {
			
			return false;
		}
		
	}
	static Precondition notNull(Object... objects){
		return objects != null ? new NotNull(objects) : new MetAlways();
	}
	static Precondition not(Precondition precon){
		return precon != null ? new Not(precon) : new MetNever();
	}
}
