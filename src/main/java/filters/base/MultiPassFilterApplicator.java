package filters.base;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import filters.FiltersList;
import filters.Inverted;
import filters.Mask_Add;
import filters.Mask_Multiply;

public class MultiPassFilterApplicator {
	private static int THREAD_POOL_SIZE = 6;
	private static volatile ExecutorService parallelProcessor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	private static volatile boolean processingCancelled = false;
	
	public static final BufferedImage NULL_MASK = null;
	
	private static volatile BufferedImage sourceLive;
	public static Filter<?> currentFilter;
	// Number of image pixel columns, which have been edited
	// by the filter already. For status information only.
	private static volatile AtomicInteger finishedPixelColumns = new AtomicInteger();
	public static final double FILTER_PASS_PROGRESS_NOT_RUNNING = -1.0;
	
	private static volatile int currentPassGroup = -1;
	// Note: last finished pass group is never PASS_GROUP_POST,
	// since it will be reset to NONE after POST finishes.
	private static volatile int lastFinishedPassGroup = -1;
	public static final int PASS_GROUP_NONE = -1;
	public static final int PASS_GROUP_PRE = 0;
	public static final int PASS_GROUP_MAIN = 1;
	public static final int PASS_GROUP_POST = 2;
	private static volatile int currentPassNum = -1;
	
	// Only true when automatic applyFilter(...) method is running.
	// Will not be set when using manual pass execution.
	private static volatile boolean isAutoFilterRunning = false;
	
	/**
	 * Makes sure a list and its elements are not null.
	 * @param <T> List type
	 * @param <L> List element type
	 * @param list
	 * @throws NullPointerException If the list or any of its elements are null.
	 * @return
	 */
	private static <T extends List<L>, L extends Object> T requireListNotNull(T list) {
		Objects.requireNonNull(list);
		for (L element : list)
			Objects.requireNonNull(element, "At least one list element is null.");
		return list;
	}
	
	/**
	 * Applies a filter without masking.
	 * Uses a null mask.
	 * @param source
	 * @param channel
	 * @param strength
	 * @param threadPoolSize
	 * @return
	 */
	
	// Application of filter without mask:
	// In: original
	// Out: output
	// filter(original) = output
	
	public static <ImgType extends BufferedImage> ImgType applyFilter(Filter<ImgType> filter,
			ImgType source, RGBAChannel channel,
			double strength, int threadPoolSize) {
		currentFilter = filter;
		return applyFilter(filter, source, (ImgType)NULL_MASK, channel, strength, threadPoolSize);
	}
	
	/**
	 * Applies a filter with pre-masking.
	 * In simple terms: The mask changes which parts of the image are being modified.
	 * @param source
	 * @param mask
	 * @param maskOp
	 * @param channel
	 * @param strength
	 * @param threadPoolSize
	 * @return
	 */
	
	// Application of filter with mask:
	// In: original, mask, mask_op
	// Out: output
	// filter(original) = modified
	// modified * mask = modified_masked
	// original * (-mask) = original_antimasked
	// original_antimasked + modified_masked = output
	
	public static <ImgType extends BufferedImage> ImgType applyFilter(Filter<ImgType> filter,
			ImgType source, ImgType mask, Filter<ImgType> maskOp,
			RGBAChannel channel, double strength, int threadPoolSize) {
		// FIXME: Make pre-masking only change parts in which mask is brighter (pre-multiply?)
		// TODO: First apply filter, then "cut out" with multipy filter
		currentFilter = filter;
		
		isAutoFilterRunning = true;
		
		// TODO: Remake this.
		
		isAutoFilterRunning = false;
		currentFilter = null;
		throw new UnsupportedOperationException("TODO: Remake this.");
	}
	
	/**
	 * Applies a filter directly without pre-masking.
	 * Filters however can use their mask implementation.
	 * @param source
	 * @param mask
	 * @param channel
	 * @param strength
	 * @param threadPoolSize
	 * @return
	 */
	
	public static <ImgType extends BufferedImage> ImgType
	applyFilter(Filter<ImgType> filter,
			ImgType source, ImgType mask, RGBAChannel channel,
			double strength, int threadPoolSize) {
		if (threadPoolSize < 1)
			throw new IllegalArgumentException("Thread pool size cannot be smaller than 1");
		THREAD_POOL_SIZE = threadPoolSize;
		Filter<ImgType> prevFilter = (Filter<ImgType>)currentFilter;
		currentFilter = filter;
		boolean autoFilterWasRunning = isAutoFilterRunning;
		isAutoFilterRunning = true;
		List<Future<?>> returnData = new ArrayList<>();
		try {
			HashMap<PrePass<ImgType>, List<?>> preProcessingData =
					runPreProcessingPasses(filter, source, mask, strength);
			
			ImgType afterMainPasses =
					applyMainPasses(filter, preProcessingData, source, mask, channel, strength);
			
			ImgType afterPostProcessing =
					applyPostProcessingPasses(
							filter, preProcessingData, source, afterMainPasses, mask, channel, strength);
			
			isAutoFilterRunning = false;
			return afterPostProcessing;
		} catch (UnfinishedOperationException uoe) {
			uoe.printStackTrace();
		} catch (RequiredOperationSkipAttemptedException rosae) {
			rosae.printStackTrace();
		} catch (InterruptedException ie) {
			System.err.println("Pass executor was interrupted.");
			ie.printStackTrace();
		}
		// Keep previous filter running state
		isAutoFilterRunning = autoFilterWasRunning;
		currentFilter = prevFilter;
		throw new RuntimeException("Internal exception executing passes in order. See other stacktraces.");
	}
	
	public static <ImgType extends BufferedImage> HashMap<PrePass<ImgType>, List<?>>
	runPreProcessingPasses(Filter<ImgType> filter, ImgType source, ImgType maskUnscaled, double strength)
			throws UnfinishedOperationException, RequiredOperationSkipAttemptedException, InterruptedException {
		checkPassGroupEligibleToRun(PASS_GROUP_PRE);
		currentPassGroup = PASS_GROUP_PRE;
		currentPassNum = 0;
		sourceLive = source;
		final ImgType mask =
				(ImgType)FilterUtils.scaleImageToSize(maskUnscaled, source.getWidth(), source.getHeight());
		
		HashMap<PrePass<ImgType>, List<?>> preProcessingData = new HashMap<>();
		reinitializeProcessor(THREAD_POOL_SIZE);
		List<Future<List<?>>> futureList = parallelProcessor.invokeAll(
				filter.getPrePasses().stream()
				.<Callable<List<?>>> map(prePass -> { return () -> {
					// TODO: May cause concurrency issues, since non-atomic. Solve when necessary only.
					currentPassNum++;
					finishedPixelColumns.set(0);
					List<?> returnData = prePass.runPreProcessing(source, mask, strength);
					preProcessingData.put(prePass, returnData);
					finishedPixelColumns.set(source.getWidth());
					return returnData;
				}; })
				.toList());
		
		
		// For future "FilterStatusFuture" implementation
		/*return new HashMap<PrePass<ImgType>, Future<?>>(
				IntStream.range(0, futureList.size())
				.boxed()
				.collect(Collectors.toMap(filter.getPrePasses()::get, futureList::get)));*/
		/*for (PrePass<ImgType> prePass : filter.getPrePasses()) {
			parallelProcessor.submit(() -> {
				// TODO: May cause concurrency issues, since non-atomic. Solve when necessary only.
				currentPassNum++;
				finishedPixelColumns.set(0);
				preProcessingData.put(prePass, prePass.runPreProcessing(source, mask, strength));
				finishedPixelColumns.set(source.getWidth());
			});
		}*/
		waitForProcessorShutdown();
		lastFinishedPassGroup = PASS_GROUP_PRE;
		resetStatus(false);
		return preProcessingData;
	}
	// Developer's note: "NF" at the end of source and mask means "non-final".
	// This is required, because lambdas require final inputs for enclosed-scope variables.
	// For readability: NF variables are used outside of lambdas, final ones only inside.
	public static <ImgType extends BufferedImage> ImgType applyMainPasses(
			Filter<ImgType> filter,
			HashMap<PrePass<ImgType>, List<?>> preProcessingData,
			ImgType sourceNF, ImgType maskNF, RGBAChannel channel, double strength)
					throws UnfinishedOperationException, RequiredOperationSkipAttemptedException  {
		checkPassGroupEligibleToRun(PASS_GROUP_MAIN);
		currentPassGroup = PASS_GROUP_MAIN;
		currentPassNum = 0;
		sourceLive = FilterUtils.deepCopy(sourceNF);
		final ImgType mask = (ImgType)FilterUtils.scaleImageToSize(
				maskNF, sourceNF.getWidth(), sourceNF.getHeight());
		final ImgType source = FilterUtils.deepCopy(sourceNF);
		
		// TODO: Return a Future<ImgType>, which can report status (live img, group, pass, progress)
		
		for (PixelTransformer<ImgType> mainPass : filter.getMainPassTransformers()) {
			if (processingCancelled) break;
			reinitializeProcessor(THREAD_POOL_SIZE);
			currentPassNum++;
			finishedPixelColumns.set(0);
			for (int xNonFinal = 0; xNonFinal < sourceNF.getWidth(); xNonFinal++) {
				final int x = xNonFinal;
				parallelProcessor.submit(() -> {
					for (int y = 0; y < source.getHeight(); y++) {
						int sourceARGB = source.getRGB(x, y);
						int newARGB = mainPass.apply(
								x, y, sourceARGB,
								preProcessingData,
								source, mask,
								strength);
						sourceLive.setRGB(x, y,
								getRGBAWithChannelFilter(
										sourceARGB,
										newARGB,
										channel));
					}
					finishedPixelColumns.addAndGet(1);
				});
			}
			waitForProcessorShutdown();
		}
		lastFinishedPassGroup = PASS_GROUP_MAIN;
		resetStatus(false);
		sourceNF = (ImgType)sourceLive;
		return sourceNF;
	}
	public static <ImgType extends BufferedImage> ImgType applyPostProcessingPasses(
			Filter<ImgType> filter,
			HashMap<PrePass<ImgType>, List<?>> preProcessingData,
			ImgType originalNF, ImgType sourceNF, ImgType maskNF, RGBAChannel channel, double strength)
					throws UnfinishedOperationException, RequiredOperationSkipAttemptedException {
		checkPassGroupEligibleToRun(PASS_GROUP_POST);
		currentPassGroup = PASS_GROUP_POST;
		currentPassNum = 0;
		sourceLive = FilterUtils.deepCopy(sourceNF);
		final ImgType mask = (ImgType)FilterUtils.scaleImageToSize(
				maskNF, sourceNF.getWidth(), sourceNF.getHeight());
		final ImgType source = FilterUtils.deepCopy(sourceNF);
		final ImgType original = FilterUtils.deepCopy(originalNF);
		
		for (PostProcessPixelTransformer<ImgType> postPass : filter.getPostPassTransformers()) {
			if (processingCancelled) break;
			reinitializeProcessor(THREAD_POOL_SIZE);
			currentPassNum++;
			finishedPixelColumns.set(0);
			for (int xNonFinal = 0; xNonFinal < sourceNF.getWidth(); xNonFinal++) {
				final int x = xNonFinal;
				parallelProcessor.submit(() -> {
					for (int y = 0; y < source.getHeight(); y++) {
						int sourceARGB = source.getRGB(x, y);
						int newARGB = postPass.apply(
								x, y, sourceARGB,
								preProcessingData,
								original, source, mask,
								strength);
						sourceLive.setRGB(x, y,
								getRGBAWithChannelFilter(
										sourceARGB,
										newARGB,
										channel));
					}
					finishedPixelColumns.addAndGet(1);
				});
			}
			waitForProcessorShutdown();
		}
		resetStatus(true);
		sourceNF = (ImgType)sourceLive;
		return sourceNF;
	}
	
	private static void reinitializeProcessor(int numThreads) {
		if (!parallelProcessor.isTerminated())
			parallelProcessor.shutdownNow();
		parallelProcessor = Executors.newFixedThreadPool(numThreads);
	}
	private static void waitForProcessorShutdown() {
		parallelProcessor.shutdown();
		boolean terminationWaitResult = true;
		try {
			terminationWaitResult = parallelProcessor.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException ie) {
			System.err.println("Processor wait has been interrupted.");
			parallelProcessor.shutdownNow();
		}
		if (!terminationWaitResult) {
			System.err.println("Processor timeout has been reached.");
			parallelProcessor.shutdownNow();
		}
	}
	
	/**
	 * Applies filter to a specific channel only and keeps original values on
	 * other channels.
	 */
	// TODO: Implement channel specific filter
	private static int getRGBAWithChannelFilter(int originalRGBA, int newRGBA, RGBAChannel channel) {
		int originalRed = FilterUtils.getRed(originalRGBA);
		int originalGreen = FilterUtils.getGreen(originalRGBA);
		int originalBlue = FilterUtils.getBlue(originalRGBA);
		int originalAlpha = FilterUtils.getAlpha(originalRGBA);
		
		int newRed = FilterUtils.getRed(newRGBA);
		int newGreen = FilterUtils.getGreen(newRGBA);
		int newBlue = FilterUtils.getBlue(newRGBA);
		int newAlpha = FilterUtils.getAlpha(newRGBA);
		
		switch (channel) {
			case ALL_EXCEPT_ALPHA: { return FilterUtils.toARGB(newRed, newGreen, newBlue, originalAlpha); }
			case ALL: { return FilterUtils.toARGB(newRed, newGreen, newBlue, newAlpha); }
			case RED: { return FilterUtils.toARGB(newRed, originalGreen, originalBlue, originalAlpha); }
			case GREEN: { return FilterUtils.toARGB(originalRed, newGreen, originalBlue, originalAlpha); }
			case BLUE: { return FilterUtils.toARGB(originalRed, originalGreen, newBlue, originalAlpha); }
			case ALPHA: { return FilterUtils.toARGB(originalRed, originalGreen, originalBlue, newAlpha); }
			default: {
				System.err.println("Filter channel selection not supported. Using default \"ALL_EXCEPT_ALPHA\"");
				return getRGBAWithChannelFilter(originalRGBA, newRGBA, RGBAChannel.ALL_EXCEPT_ALPHA);
			}
		}
	}
	
	// Methods for live status updates
	/**
	 * @return The latest image also including current live updates
	 * from unfinished filter operations.
	 * @apiNote Returns a deep copy of the image. The returned image cannot be modified
	 * so that the filter is affected in any way.
	 */
	public static BufferedImage getLiveImage() {
		return FilterUtils.deepCopy(sourceLive);
	}
	
	public static Filter<?> getCurrentFilter() {
		return currentFilter;
	}
	/**
	 * @return The current pass progress between 0.0 (just started) and 1.0 (finished)
	 * If no filter is actively running, returns -1.0;
	 */
	public static double getCurrentPassProgress() {
		int finishedPixelColumns = MultiPassFilterApplicator.finishedPixelColumns.get();
		if (finishedPixelColumns == -1 || sourceLive == null)
			return FILTER_PASS_PROGRESS_NOT_RUNNING;
		// (double) cast required to avoid integer division (would return 0 or 1)
		return (double)finishedPixelColumns / sourceLive.getWidth();
	}
	public static int getCurrentPassGroup() {
		return currentPassGroup;
	}
	public static int getCurrentPassNum() {
		return currentPassNum;
	}
	public static int getMaxPassesForCurrentGroup() {
		switch (currentPassGroup) {
		case (PASS_GROUP_NONE): return -1;
		case (PASS_GROUP_PRE): return currentFilter.getPrePasses().size();
		case (PASS_GROUP_MAIN): return currentFilter.getMainPassTransformers().size();
		case (PASS_GROUP_POST): return currentFilter.getPostPassTransformers().size();
		default: throw new RuntimeException("Wrong internal pass group " + currentPassGroup);
		}
	}
	/**
	 * Only returns true when the automatic filtering process via {@code applyFilter(...)}
	 * has been invoked. Manual filtering methods ({@code runPreProcessing(...)}, 
	 * {@code applyMainPasses(...)} and {@code applyPostProcessingPasses(...)}) do NOT
	 * change this status to true.
	 * @return Whether the automatic filter is running or not.
	 */
	public static boolean isAutoFilterRunning() {
		return isAutoFilterRunning;
	}
	
	private static void checkPassGroupEligibleToRun(int passGroup)
			throws UnfinishedOperationException, RequiredOperationSkipAttemptedException {
		String passGroupStr = "";
		switch (passGroup) {
		case (PASS_GROUP_NONE): passGroupStr = "NONE"; break;
		case (PASS_GROUP_PRE): passGroupStr = "PRE"; break;
		case (PASS_GROUP_MAIN): passGroupStr = "MAIN"; break;
		case (PASS_GROUP_POST): passGroupStr = "POST"; break;
		default: throw new IllegalArgumentException("Pass group specified not valid: " + passGroup);
		}
		if (lastFinishedPassGroup != passGroup - 1)
			throw new RequiredOperationSkipAttemptedException(
					"Pass " + passGroupStr + " cannot run, because a previous pass has not run yet.");
		
		if (currentPassGroup != PASS_GROUP_NONE)
			throw new UnfinishedOperationException(
					"Another pass is still running.");
	}
	
	/**
	 * Full reset includes lastFinishedPassGroup and processingCancelled
	 * @param fullReset
	 */
	private static synchronized void resetStatus(boolean fullReset) {
		if (fullReset) {
			lastFinishedPassGroup = PASS_GROUP_NONE;
			processingCancelled = false;
		}
		currentPassGroup = PASS_GROUP_NONE;
		currentPassNum = -1;
		finishedPixelColumns.set(0);
	}
	
	// TODO: Implement processing cancelling
	public static void cancelProcessing() {
		System.err.println("Forcing shutdown of processor");
		parallelProcessor.shutdownNow();
		processingCancelled = true;
	}
}
