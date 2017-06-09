package org.openecomp.sdc.common.jsongraph.util;

import org.slf4j.Logger;

/**
 * Provides common utility: functionality, common fields and enumerations
 *
 */
public class CommonUtility {
	/**
	 * Provides list of logging level names
	 *
	 */
	public enum LogLevelEnum {
		ERROR,
		WARNING,
		INFO,
		DEBUG,
		TRACE,
	}
	/**
	 * Adds received message to log according to level in case if specified level is enabled
	 * @param logger
	 * @param logLevel
	 * @param format
	 * @param arguments
	 */
	public static void addRecordToLog(Logger logger, LogLevelEnum logLevel, String format, Object... arguments ){
		switch(logLevel){
		case ERROR:
			if(logger.isErrorEnabled()){
				logger.error(format, arguments);
			}
			break;
		case WARNING:
			if(logger.isWarnEnabled()){
				logger.warn(format, arguments);
			}
			break;
		case INFO:
			if(logger.isInfoEnabled()){
				logger.info(format, arguments);
			}
			break;
		case DEBUG:
			if(logger.isDebugEnabled()){
				logger.debug(format, arguments);
			}
			break;
		case TRACE:
			if(logger.isTraceEnabled()){
				logger.trace(format, arguments);
			}
			break;
		default:
			break;
		}
	}
	
}
