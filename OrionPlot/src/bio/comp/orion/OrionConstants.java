package bio.comp.orion;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.eventbus.EventBus;

import java.util.logging.Logger;

public interface OrionConstants {

	static class LoggerSupplier implements Supplier<Logger>{
		
		private String logName;
		public LoggerSupplier(String logname){
			this.logName = logname;
		}
		@Override
		public Logger get() {
			// TODO Auto-generated method stub
			return Logger.getLogger(this.logName);
		}
	}
	static class EventBusSupplier implements Supplier<EventBus>{
		private String busName;
		public EventBusSupplier(String _busName){
			this.busName = _busName;
		}

		@Override
		public EventBus get() {
			// TODO Auto-generated method stub
			return new EventBus(busName);
		}
		
	}
	public final static String PROBLEM_LOGGER_NAME="orion.problem.log";
	public final static String DEBUG_LOGGER_NAME="orion.debug.log";
	public final static Supplier<Logger> PROBLEM_LOGGER = Suppliers.memoize(new LoggerSupplier(PROBLEM_LOGGER_NAME));
	public final static Supplier<Logger> DEBUG_LOGGER = Suppliers.memoize(new LoggerSupplier(DEBUG_LOGGER_NAME));
	public final static String STATUS_EVENT_BUS_NAME = "orion.bus.problem";
	public final static String STATUS_EVENT_EXCEPTION_KEY = "orion.bus.problem.exception";
	public final static String STATUS_EVENT_SOURCE_KEY = "orion.bus.problem.source";
	public final static String STATUS_EVENT_DESCRIPTION_KEY = "orion.bus.problem.desc";
	public final static Supplier<EventBus> STATUS_EVENT_BUS = Suppliers.memoize(new EventBusSupplier(STATUS_EVENT_BUS_NAME));
}
