package bio.comp.orion.generator;

import bio.comp.orion.generator.Generator.Result;

public interface Pipeline{
	public interface Pipe<I,O> {
		void prepare(I input);
		Result<O> process();
		public Result<O> process(I input);
	}
	static class Combined<I,IO,O> implements Pipe<I, O>{
		private Pipe<I, IO>_first;
		private Pipe<IO, O>_second;
		public Combined(Pipe<I, IO>first, Pipe<IO, O>next){
			_first = first;
			_second = next;
		}
		
		@Override
		public void prepare(I input) {
			// TODO Auto-generated method stub
			_first.prepare(input);
		}
		@Override
		public Result<O> process() {
			// TODO Auto-generated method stub
			Result<IO> resultOne = _first.process();
			if(resultOne.isValid()){
				return _second.process(resultOne.get());
			}
			return Generator.Error.ErrorForResultOfType(null, 1, "error in pipe");
		}

		public  Result<O> process(I input) {
			// TODO Auto-generated method stub'
			prepare(input);
			return  process();
		}
	}
	static abstract class AbstractPipe<II, OO> implements Pipe<II,OO>{
		private II _input;
		public void prepare(II input){
			_input = input;
		}
		public Result<OO> process(){
			return this.process(_input);
		}
		public abstract Result<OO> process(II input);
	}
    static class Builder{
    	public static <I, O> Pipe<I,O> Create(AbstractPipe<I,O> lambda){
    		return lambda;
    	}
    	public static <I, II, OO> Pipe<I, OO> Compose(Pipe<I, II>front, Pipe<II, OO>back){
    		return new Combined<I, II, OO>(front, back);
    	}
    	
    }
}
