package org.codetwisted.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;

public final class DebugUtils {

	public interface TraceSection {

		long timeBeginAtMillis();

		long timeEndedAtMillis();


		long durationNanos();


		String callerNameInitial();

		String callerNameFinal();


		StackTraceElement[] getStackTraceInitial();

		StackTraceElement[] getStackTraceFinal();
	}

	public interface TraceSectionToken {
		/* Marker interface */
	}


	@NotNull
	public static TraceSectionToken beginTraceSection() {
		TraceSectionImpl traceSection = new TraceSectionImpl();
		{
			StackTraceElement stackTrace[] = Thread.currentThread().getStackTrace();

			traceSection.stackTraceInitial = Arrays.copyOfRange(stackTrace, STACK_TRACE_DEPTH_BASE + 1, stackTrace.length);
			traceSection.callerInitial = getCallerMethodName(0, false);
			traceSection.timeBegan = System.currentTimeMillis();
			traceSection.time = System.nanoTime();
		}
		return traceSection;
	}

	public static TraceSection endTraceSection(@NotNull TraceSectionToken sectionToken) {
		long time = System.nanoTime();
		long timeEnded = System.currentTimeMillis();

		TraceSectionImpl traceSection = (TraceSectionImpl) sectionToken;
		{
			StackTraceElement stackTrace[] = Thread.currentThread().getStackTrace();

			traceSection.timeEnded = timeEnded;
			traceSection.time = time - traceSection.time;
			traceSection.callerFinal = getCallerMethodName(0, false);
			traceSection.stackTraceFinal = Arrays.copyOfRange(stackTrace, STACK_TRACE_DEPTH_BASE + 1, stackTrace.length);

			traceSection.isComplete = true;
		}
		return traceSection;
	}

	private static class TraceSectionImpl implements TraceSection, TraceSectionToken {

		private long timeBegan;
		private long timeEnded;

		private long time;

		private String callerInitial;
		private String callerFinal;


		private StackTraceElement stackTraceInitial[];
		private StackTraceElement stackTraceFinal[];


		private boolean isComplete;

		@Override
		public long timeBeginAtMillis() {
			return timeBegan;
		}

		@Override
		public long timeEndedAtMillis() {
			return timeEnded;
		}

		@Override
		public long durationNanos() {
			return time;
		}

		@Override
		public String callerNameInitial() {
			return callerInitial;
		}

		@Override
		public String callerNameFinal() {
			return callerFinal;
		}

		@Override
		public StackTraceElement[] getStackTraceInitial() {
			return stackTraceInitial;
		}

		@Override
		public StackTraceElement[] getStackTraceFinal() {
			return stackTraceFinal;
		}


		@Override
		public String toString() {
			return isComplete ? String.format("%s, duration: %.3fs (initiator: %s@%d, %s)", TraceSection.class.getSimpleName(), time / 1000000000f,
						stackTraceInitial[0].getMethodName(), stackTraceInitial[0].getLineNumber(), stackTraceInitial[0].getFileName())
					: String.format("%s, began at: %s (initiator: %s)", TraceSection.class.getSimpleName(), new Date(timeBegan), callerInitial);
		}
	}


	private static final int STACK_TRACE_DEPTH_BASE;

	static {
		// Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
		int i = 0;

		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			++i;

			if (ste.getClassName().equals(DebugUtils.class.getName())) {
				break;
			}
		}
		STACK_TRACE_DEPTH_BASE = i;
	}


	public static String getCallerMethodName(int offsetUp, boolean withLineNumber) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (stackTrace == null
				|| stackTrace.length <= STACK_TRACE_DEPTH_BASE)
			return "corrupted stack!";

		final int level = STACK_TRACE_DEPTH_BASE + offsetUp;
		if (0 <= level && level < stackTrace.length) {
			StackTraceElement callerElement = stackTrace[level];

			StringBuilder callerName = new StringBuilder(callerElement.getClassName())
				.append('#').append(callerElement.getMethodName());

			if (withLineNumber) {
				callerName.append(String.format(
					" @ line %d", callerElement.getLineNumber()));
			}
			return callerName.toString() ;
		} else {
			return String.format("Out of stack bounds! (index: %d)", level);
		}
	}

	public static String getCallerMethodName(boolean withLineNumber) {
		return getCallerMethodName(1, withLineNumber);
	}

	public static String getCallerMethodName() {
		return getCallerMethodName(1, false);
	}


	public static String getCallerClassName(int offsetUp) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (stackTrace == null
			|| stackTrace.length <= STACK_TRACE_DEPTH_BASE)
			return "corrupted stack!";

		final int level = STACK_TRACE_DEPTH_BASE + offsetUp;
		if (0 <= level && level < stackTrace.length) {
			StackTraceElement callerElement = stackTrace[level];

			return callerElement.getClassName();
		} else {
			return String.format("Out of stack bounds! (index: %d)", level);
		}
	}

	public static String getCallerClassName() {
		return getCallerClassName(1);
	}


	public static String getStackTrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (stackTrace == null
				|| stackTrace.length <= STACK_TRACE_DEPTH_BASE) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		for (int i = STACK_TRACE_DEPTH_BASE; i < stackTrace.length; ++i) {
			sb.append(stackTrace[i].toString())
					.append(System.getProperty("line.separator") )
					.append('\t');
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}


	private DebugUtils() {
		/* Prevent instantiating */
	}
}
