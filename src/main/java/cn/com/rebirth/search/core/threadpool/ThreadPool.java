/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThreadPool.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.threadpool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.concurrent.EsThreadPoolExecutor;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.SizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;


/**
 * The Class ThreadPool.
 *
 * @author l.xue.nong
 */
public class ThreadPool extends AbstractComponent {

	
	/**
	 * The Class Names.
	 *
	 * @author l.xue.nong
	 */
	public static class Names {

		
		/** The Constant SAME. */
		public static final String SAME = "same";

		
		/** The Constant GENERIC. */
		public static final String GENERIC = "generic";

		
		/** The Constant GET. */
		public static final String GET = "get";

		
		/** The Constant INDEX. */
		public static final String INDEX = "index";

		
		/** The Constant BULK. */
		public static final String BULK = "bulk";

		
		/** The Constant SEARCH. */
		public static final String SEARCH = "search";

		
		/** The Constant PERCOLATE. */
		public static final String PERCOLATE = "percolate";

		
		/** The Constant MANAGEMENT. */
		public static final String MANAGEMENT = "management";

		
		/** The Constant FLUSH. */
		public static final String FLUSH = "flush";

		
		/** The Constant MERGE. */
		public static final String MERGE = "merge";

		
		/** The Constant CACHE. */
		public static final String CACHE = "cache";

		
		/** The Constant REFRESH. */
		public static final String REFRESH = "refresh";

		
		/** The Constant SNAPSHOT. */
		public static final String SNAPSHOT = "snapshot";
	}

	
	/** The executors. */
	private final ImmutableMap<String, ExecutorHolder> executors;

	
	/** The scheduler. */
	private final ScheduledThreadPoolExecutor scheduler;

	
	/** The estimated time thread. */
	private final EstimatedTimeThread estimatedTimeThread;

	
	/**
	 * Instantiates a new thread pool.
	 */
	public ThreadPool() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	
	/**
	 * Instantiates a new thread pool.
	 *
	 * @param settings the settings
	 */
	@Inject
	public ThreadPool(Settings settings) {
		super(settings);

		Map<String, Settings> groupSettings = settings.getGroups("threadpool");

		Map<String, ExecutorHolder> executors = Maps.newHashMap();
		executors.put(
				Names.GENERIC,
				build(Names.GENERIC, "cached", groupSettings.get(Names.GENERIC), ImmutableSettings.settingsBuilder()
						.put("keep_alive", "30s").build()));
		executors.put(Names.INDEX,
				build(Names.INDEX, "cached", groupSettings.get(Names.INDEX), ImmutableSettings.Builder.EMPTY_SETTINGS));
		executors.put(Names.BULK,
				build(Names.BULK, "cached", groupSettings.get(Names.BULK), ImmutableSettings.Builder.EMPTY_SETTINGS));
		executors.put(Names.GET,
				build(Names.GET, "cached", groupSettings.get(Names.GET), ImmutableSettings.Builder.EMPTY_SETTINGS));
		executors
				.put(Names.SEARCH,
						build(Names.SEARCH, "cached", groupSettings.get(Names.SEARCH),
								ImmutableSettings.Builder.EMPTY_SETTINGS));
		executors.put(
				Names.PERCOLATE,
				build(Names.PERCOLATE, "cached", groupSettings.get(Names.PERCOLATE),
						ImmutableSettings.Builder.EMPTY_SETTINGS));
		executors.put(
				Names.MANAGEMENT,
				build(Names.MANAGEMENT, "scaling", groupSettings.get(Names.MANAGEMENT), ImmutableSettings
						.settingsBuilder().put("keep_alive", "5m").put("size", 5).build()));
		executors.put(
				Names.FLUSH,
				build(Names.FLUSH, "scaling", groupSettings.get(Names.FLUSH),
						ImmutableSettings.settingsBuilder().put("keep_alive", "5m").put("size", 10).build()));
		executors.put(
				Names.MERGE,
				build(Names.MERGE, "scaling", groupSettings.get(Names.MERGE),
						ImmutableSettings.settingsBuilder().put("keep_alive", "5m").put("size", 20).build()));
		executors.put(
				Names.REFRESH,
				build(Names.REFRESH, "cached", groupSettings.get(Names.REFRESH), ImmutableSettings.settingsBuilder()
						.put("keep_alive", "1m").build()));
		executors.put(
				Names.CACHE,
				build(Names.CACHE, "scaling", groupSettings.get(Names.CACHE),
						ImmutableSettings.settingsBuilder().put("keep_alive", "5m").put("size", 4).build()));
		executors.put(
				Names.SNAPSHOT,
				build(Names.SNAPSHOT, "scaling", groupSettings.get(Names.SNAPSHOT), ImmutableSettings.settingsBuilder()
						.put("keep_alive", "5m").put("size", 5).build()));
		executors.put(Names.SAME, new ExecutorHolder(MoreExecutors.sameThreadExecutor(), new Info(Names.SAME, "same")));
		this.executors = ImmutableMap.copyOf(executors);
		this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,
				EsExecutors.daemonThreadFactory(settings, "[scheduler]"));
		this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

		TimeValue estimatedTimeInterval = componentSettings.getAsTime("estimated_time_interval",
				TimeValue.timeValueMillis(200));
		this.estimatedTimeThread = new EstimatedTimeThread(EsExecutors.threadName(settings, "[timer]"),
				estimatedTimeInterval.millis());
		this.estimatedTimeThread.start();
	}

	
	/**
	 * Estimated time in millis.
	 *
	 * @return the long
	 */
	public long estimatedTimeInMillis() {
		return estimatedTimeThread.estimatedTimeInMillis();
	}

	
	/**
	 * Info.
	 *
	 * @return the thread pool info
	 */
	public ThreadPoolInfo info() {
		List<Info> infos = new ArrayList<Info>();
		for (ExecutorHolder holder : executors.values()) {
			String name = holder.info.name();
			
			if ("same".equals(name)) {
				continue;
			}
			infos.add(holder.info);
		}
		return new ThreadPoolInfo(infos);
	}

	
	/**
	 * Stats.
	 *
	 * @return the thread pool stats
	 */
	public ThreadPoolStats stats() {
		List<ThreadPoolStats.Stats> stats = new ArrayList<ThreadPoolStats.Stats>();
		for (ExecutorHolder holder : executors.values()) {
			String name = holder.info.name();
			
			if ("same".equals(name)) {
				continue;
			}
			int threads = -1;
			int queue = -1;
			int active = -1;
			if (holder.executor instanceof ThreadPoolExecutor) {
				ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) holder.executor;
				threads = threadPoolExecutor.getPoolSize();
				queue = threadPoolExecutor.getQueue().size();
				active = threadPoolExecutor.getActiveCount();
			}
			stats.add(new ThreadPoolStats.Stats(name, threads, queue, active));
		}
		return new ThreadPoolStats(stats);
	}

	
	/**
	 * Generic.
	 *
	 * @return the executor
	 */
	public Executor generic() {
		return executor(Names.GENERIC);
	}

	
	/**
	 * Executor.
	 *
	 * @param name the name
	 * @return the executor
	 */
	public Executor executor(String name) {
		Executor executor = executors.get(name).executor;
		if (executor == null) {
			throw new RestartIllegalArgumentException("No executor found for [" + name + "]");
		}
		return executor;
	}

	
	/**
	 * Scheduler.
	 *
	 * @return the scheduled executor service
	 */
	public ScheduledExecutorService scheduler() {
		return this.scheduler;
	}

	
	/**
	 * Schedule with fixed delay.
	 *
	 * @param command the command
	 * @param interval the interval
	 * @return the scheduled future
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, TimeValue interval) {
		return scheduler.scheduleWithFixedDelay(new LoggingRunnable(command), interval.millis(), interval.millis(),
				TimeUnit.MILLISECONDS);
	}

	
	/**
	 * Schedule.
	 *
	 * @param delay the delay
	 * @param name the name
	 * @param command the command
	 * @return the scheduled future
	 */
	public ScheduledFuture<?> schedule(TimeValue delay, String name, Runnable command) {
		if (!Names.SAME.equals(name)) {
			command = new ThreadedRunnable(command, executor(name));
		}
		return scheduler.schedule(command, delay.millis(), TimeUnit.MILLISECONDS);
	}

	
	/**
	 * Shutdown.
	 */
	public void shutdown() {
		estimatedTimeThread.running = false;
		estimatedTimeThread.interrupt();
		scheduler.shutdown();
		for (ExecutorHolder executor : executors.values()) {
			if (executor.executor instanceof ThreadPoolExecutor) {
				((ThreadPoolExecutor) executor.executor).shutdown();
			}
		}
	}

	
	/**
	 * Shutdown now.
	 */
	public void shutdownNow() {
		estimatedTimeThread.running = false;
		estimatedTimeThread.interrupt();
		scheduler.shutdownNow();
		for (ExecutorHolder executor : executors.values()) {
			if (executor.executor instanceof ThreadPoolExecutor) {
				((ThreadPoolExecutor) executor.executor).shutdownNow();
			}
		}
	}

	
	/**
	 * Await termination.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return true, if successful
	 * @throws InterruptedException the interrupted exception
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		boolean result = scheduler.awaitTermination(timeout, unit);
		for (ExecutorHolder executor : executors.values()) {
			if (executor.executor instanceof ThreadPoolExecutor) {
				result &= ((ThreadPoolExecutor) executor.executor).awaitTermination(timeout, unit);
			}
		}
		return result;
	}

	
	/**
	 * Builds the.
	 *
	 * @param name the name
	 * @param defaultType the default type
	 * @param settings the settings
	 * @param defaultSettings the default settings
	 * @return the executor holder
	 */
	private ExecutorHolder build(String name, String defaultType, @Nullable Settings settings, Settings defaultSettings) {
		if (settings == null) {
			settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
		}
		String type = settings.get("type", defaultType);
		ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings, "[" + name + "]");
		if ("same".equals(type)) {
			logger.debug("creating thread_pool [{}], type [{}]", name, type);
			return new ExecutorHolder(MoreExecutors.sameThreadExecutor(), new Info(name, type));
		} else if ("cached".equals(type)) {
			TimeValue keepAlive = settings.getAsTime("keep_alive",
					defaultSettings.getAsTime("keep_alive", TimeValue.timeValueMinutes(5)));
			Executor executor = new EsThreadPoolExecutor(0, Integer.MAX_VALUE, keepAlive.millis(),
					TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), threadFactory);
			return new ExecutorHolder(executor, new Info(name, type, -1, -1, keepAlive, null));
		} else if ("fixed".equals(type)) {
			int size = settings.getAsInt("size",
					defaultSettings.getAsInt("size", Runtime.getRuntime().availableProcessors() * 5));
			SizeValue capacity = settings.getAsSize("capacity",
					settings.getAsSize("queue_size", defaultSettings.getAsSize("queue_size", null)));
			RejectedExecutionHandler rejectedExecutionHandler;
			String rejectSetting = settings.get("reject_policy", defaultSettings.get("reject_policy", "abort"));
			if ("abort".equals(rejectSetting)) {
				rejectedExecutionHandler = new AbortPolicy();
			} else if ("caller".equals(rejectSetting)) {
				rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
			} else {
				throw new RestartIllegalArgumentException("reject_policy [" + rejectSetting + "] not valid for ["
						+ name + "] thread pool");
			}
			Executor executor = new EsThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
					capacity == null ? new LinkedTransferQueue<Runnable>() : new ArrayBlockingQueue<Runnable>(
							(int) capacity.singles()), threadFactory, rejectedExecutionHandler);
			return new ExecutorHolder(executor, new Info(name, type, size, size, null, capacity));
		} else if ("scaling".equals(type)) {
			TimeValue keepAlive = settings.getAsTime("keep_alive",
					defaultSettings.getAsTime("keep_alive", TimeValue.timeValueMinutes(5)));
			int min = settings.getAsInt("min", defaultSettings.getAsInt("min", 1));
			int size = settings.getAsInt(
					"max",
					settings.getAsInt("size",
							defaultSettings.getAsInt("size", Runtime.getRuntime().availableProcessors() * 5)));
			Executor executor = EsExecutors.newScalingExecutorService(min, size, keepAlive.millis(),
					TimeUnit.MILLISECONDS, threadFactory);
			return new ExecutorHolder(executor, new Info(name, type, min, size, keepAlive, null));
		} else if ("blocking".equals(type)) {
			TimeValue keepAlive = settings.getAsTime("keep_alive",
					defaultSettings.getAsTime("keep_alive", TimeValue.timeValueMinutes(5)));
			int min = settings.getAsInt("min", defaultSettings.getAsInt("min", 1));
			int size = settings.getAsInt(
					"max",
					settings.getAsInt("size",
							defaultSettings.getAsInt("size", Runtime.getRuntime().availableProcessors() * 5)));
			SizeValue capacity = settings.getAsSize("capacity",
					settings.getAsSize("queue_size", defaultSettings.getAsSize("queue_size", new SizeValue(1000))));
			TimeValue waitTime = settings.getAsTime("wait_time",
					defaultSettings.getAsTime("wait_time", TimeValue.timeValueSeconds(60)));
			Executor executor = EsExecutors.newBlockingExecutorService(min, size, keepAlive.millis(),
					TimeUnit.MILLISECONDS, threadFactory, (int) capacity.singles(), waitTime.millis(),
					TimeUnit.MILLISECONDS);
			return new ExecutorHolder(executor, new Info(name, type, min, size, keepAlive, capacity));
		}
		throw new RestartIllegalArgumentException("No type found [" + type + "], for [" + name + "]");
	}

	
	/**
	 * The Class LoggingRunnable.
	 *
	 * @author l.xue.nong
	 */
	class LoggingRunnable implements Runnable {

		
		/** The runnable. */
		private final Runnable runnable;

		
		/**
		 * Instantiates a new logging runnable.
		 *
		 * @param runnable the runnable
		 */
		LoggingRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				runnable.run();
			} catch (Exception e) {
				logger.warn("failed to run {}", e, runnable.toString());
			}
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return runnable.hashCode();
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return runnable.equals(obj);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[threaded] " + runnable.toString();
		}
	}

	
	/**
	 * The Class ThreadedRunnable.
	 *
	 * @author l.xue.nong
	 */
	class ThreadedRunnable implements Runnable {

		
		/** The runnable. */
		private final Runnable runnable;

		
		/** The executor. */
		private final Executor executor;

		
		/**
		 * Instantiates a new threaded runnable.
		 *
		 * @param runnable the runnable
		 * @param executor the executor
		 */
		ThreadedRunnable(Runnable runnable, Executor executor) {
			this.runnable = runnable;
			this.executor = executor;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			executor.execute(runnable);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return runnable.hashCode();
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return runnable.equals(obj);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[threaded] " + runnable.toString();
		}
	}

	
	/**
	 * The Class EstimatedTimeThread.
	 *
	 * @author l.xue.nong
	 */
	static class EstimatedTimeThread extends Thread {

		
		/** The interval. */
		final long interval;

		
		/** The running. */
		volatile boolean running = true;

		
		/** The estimated time in millis. */
		volatile long estimatedTimeInMillis;

		
		/**
		 * Instantiates a new estimated time thread.
		 *
		 * @param name the name
		 * @param interval the interval
		 */
		EstimatedTimeThread(String name, long interval) {
			super(name);
			this.interval = interval;
			setDaemon(true);
		}

		
		/**
		 * Estimated time in millis.
		 *
		 * @return the long
		 */
		public long estimatedTimeInMillis() {
			return this.estimatedTimeInMillis;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (running) {
				estimatedTimeInMillis = System.currentTimeMillis();
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					running = false;
					return;
				}
				try {
					FileSystemUtils.checkMkdirsStall(estimatedTimeInMillis);
				} catch (Exception e) {
					
				}
			}
		}
	}

	
	/**
	 * The Class AbortPolicy.
	 *
	 * @author l.xue.nong
	 */
	public static class AbortPolicy implements RejectedExecutionHandler {

		
		/**
		 * Instantiates a new abort policy.
		 */
		public AbortPolicy() {
		}

		
		/* (non-Javadoc)
		 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
		 */
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			throw new ThreadPoolRejectedException();
		}
	}

	
	/**
	 * The Class ExecutorHolder.
	 *
	 * @author l.xue.nong
	 */
	static class ExecutorHolder {

		
		/** The executor. */
		public final Executor executor;

		
		/** The info. */
		public final Info info;

		
		/**
		 * Instantiates a new executor holder.
		 *
		 * @param executor the executor
		 * @param info the info
		 */
		ExecutorHolder(Executor executor, Info info) {
			this.executor = executor;
			this.info = info;
		}
	}

	
	/**
	 * The Class Info.
	 *
	 * @author l.xue.nong
	 */
	public static class Info implements Streamable, ToXContent {

		
		/** The name. */
		private String name;

		
		/** The type. */
		private String type;

		
		/** The min. */
		private int min;

		
		/** The max. */
		private int max;

		
		/** The keep alive. */
		private TimeValue keepAlive;

		
		/** The capacity. */
		private SizeValue capacity;

		
		/**
		 * Instantiates a new info.
		 */
		Info() {

		}

		
		/**
		 * Instantiates a new info.
		 *
		 * @param name the name
		 * @param type the type
		 */
		public Info(String name, String type) {
			this(name, type, -1);
		}

		
		/**
		 * Instantiates a new info.
		 *
		 * @param name the name
		 * @param type the type
		 * @param size the size
		 */
		public Info(String name, String type, int size) {
			this(name, type, size, size, null, null);
		}

		
		/**
		 * Instantiates a new info.
		 *
		 * @param name the name
		 * @param type the type
		 * @param min the min
		 * @param max the max
		 * @param keepAlive the keep alive
		 * @param capacity the capacity
		 */
		public Info(String name, String type, int min, int max, @Nullable TimeValue keepAlive,
				@Nullable SizeValue capacity) {
			this.name = name;
			this.type = type;
			this.min = min;
			this.max = max;
			this.keepAlive = keepAlive;
			this.capacity = capacity;
		}

		
		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return this.name;
		}

		
		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		
		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return this.type;
		}

		
		/**
		 * Min.
		 *
		 * @return the int
		 */
		public int min() {
			return this.min;
		}

		
		/**
		 * Gets the min.
		 *
		 * @return the min
		 */
		public int getMin() {
			return this.min;
		}

		
		/**
		 * Max.
		 *
		 * @return the int
		 */
		public int max() {
			return this.max;
		}

		
		/**
		 * Gets the max.
		 *
		 * @return the max
		 */
		public int getMax() {
			return this.max;
		}

		
		/**
		 * Keep alive.
		 *
		 * @return the time value
		 */
		@Nullable
		public TimeValue keepAlive() {
			return this.keepAlive;
		}

		
		/**
		 * Gets the keep alive.
		 *
		 * @return the keep alive
		 */
		@Nullable
		public TimeValue getKeepAlive() {
			return this.keepAlive;
		}

		
		/**
		 * Capacity.
		 *
		 * @return the size value
		 */
		@Nullable
		public SizeValue capacity() {
			return this.capacity;
		}

		
		/**
		 * Gets the capacity.
		 *
		 * @return the capacity
		 */
		@Nullable
		public SizeValue getCapacity() {
			return this.capacity;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			name = in.readUTF();
			type = in.readUTF();
			min = in.readInt();
			max = in.readInt();
			if (in.readBoolean()) {
				keepAlive = TimeValue.readTimeValue(in);
			}
			if (in.readBoolean()) {
				capacity = SizeValue.readSizeValue(in);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(name);
			out.writeUTF(type);
			out.writeInt(min);
			out.writeInt(max);
			if (keepAlive == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				keepAlive.writeTo(out);
			}
			if (capacity == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				capacity.writeTo(out);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
		 */
		@Override
		public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
			builder.startObject(name, XContentBuilder.FieldCaseConversion.NONE);
			builder.field(Fields.TYPE, type);
			if (min != -1) {
				builder.field(Fields.MIN, min);
			}
			if (max != -1) {
				builder.field(Fields.MAX, max);
			}
			if (keepAlive != null) {
				builder.field(Fields.KEEP_ALIVE, keepAlive.toString());
			}
			if (capacity != null) {
				builder.field(Fields.CAPACITY, capacity.toString());
			}
			builder.endObject();
			return builder;
		}

		
		/**
		 * The Class Fields.
		 *
		 * @author l.xue.nong
		 */
		static final class Fields {

			
			/** The Constant TYPE. */
			static final XContentBuilderString TYPE = new XContentBuilderString("type");

			
			/** The Constant MIN. */
			static final XContentBuilderString MIN = new XContentBuilderString("min");

			
			/** The Constant MAX. */
			static final XContentBuilderString MAX = new XContentBuilderString("max");

			
			/** The Constant KEEP_ALIVE. */
			static final XContentBuilderString KEEP_ALIVE = new XContentBuilderString("keep_alive");

			
			/** The Constant CAPACITY. */
			static final XContentBuilderString CAPACITY = new XContentBuilderString("capacity");
		}

	}
}
