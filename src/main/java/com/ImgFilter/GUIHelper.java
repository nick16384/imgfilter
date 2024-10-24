package com.ImgFilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import filters.Blur101x101;
import filters.FiltersList;
import filters.base.Filter;
import filters.base.ImageRaster;
import filters.base.MultiPassFilterApplicator;
import filters.base.RGBChannel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Simple helper class to remove a big chunk of code
 * from Main (App.java) class and move it aside.
 */
public class GUIHelper extends Application {
	public static final int IMG_TARGET_SCALE_X = 1024;
	public static final int MASK_TARGET_SCALE_X = 1024;
	private Stage primaryStage;
	
	// Which pass group is running (pre, main)?
	private static ProgressBar filterPassGroupProgressBar;
	// Which sub-pass from the group is running?
	private static ProgressBar filterPassNumProgressBar;
	// How far is the currently running sub-pass?
	private static ProgressBar filterPassProgressBar;
	private static volatile StringProperty filterPassGroupName = new SimpleStringProperty();
	private static volatile DoubleProperty filterPassNumCurrent = new SimpleDoubleProperty();
	private static volatile DoubleProperty filterPassNumMax = new SimpleDoubleProperty();
	private static Button undoButton;
	private static Button redoButton;
	private static volatile boolean filterCancelRequested = false;
	private static FileChooser imageExporter;
	
	public static void launchGUI() {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		ImageView img = new ImageView();
		img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
    	//img.setImage(new Image("file:////" + App.filterFrontend.getImageFile().getAbsolutePath()));
    	// If image loading fails, print a stacktrace
    	if (img.getImage().isError())
    		img.getImage().getException().printStackTrace();
    	rescaleImageView(img, IMG_TARGET_SCALE_X);
    	
    	ImageView maskImg = new ImageView();
    	if (App.filterFrontend.getMaskImageFile() != null) {
    		maskImg.setImage(new Image("file:////" + App.filterFrontend.getMaskImageFile().getAbsolutePath()));
    		if (maskImg.getImage().isError())
        		maskImg.getImage().getException().printStackTrace();
        	maskImg.setOpacity(0.5);
        	rescaleImageView(maskImg, MASK_TARGET_SCALE_X, img.getFitHeight());
    	}
    	
    	// ==================================== ELEMENT INITIALIZATION ====================================
    	Button applyFilterButton = new Button("Apply filters");
    	
    	CheckBox multiPassEnableCheckbox = new CheckBox("Multi-Pass");
    	// Min, Max, Initial, Increment
    	Spinner<Integer> multiPassCountSpinner = new Spinner<>(1, 50, 1, 1);
    	multiPassCountSpinner.setDisable(true);
    	ProgressBar multiPassProgressBar = new ProgressBar(0);
    	multiPassProgressBar.setDisable(true);
    	multiPassProgressBar.setMinWidth(300);
    	multiPassProgressBar.setStyle("-fx-accent: green");
    	Label multiPassLabel = new Label("00.00 %");
    	multiPassLabel.setDisable(true);
    	GridPane multiPassOptionsGridPane = new GridPane();
    	multiPassOptionsGridPane.setVgap(10);
    	multiPassOptionsGridPane.setHgap(10);
    	multiPassOptionsGridPane.setAlignment(Pos.CENTER_LEFT);
    	multiPassOptionsGridPane.add(multiPassEnableCheckbox, 0, 0);
    	multiPassOptionsGridPane.add(multiPassCountSpinner, 1, 0);
    	
    	undoButton = new Button("Undo");
    	redoButton = new Button("Redo");
    	
    	TextField imagePathField = new TextField(App.getArgs()[App.ARGS_INDEX_INPUT_IMAGE_FILE]);
    	imagePathField.setMinWidth(300);
    	imagePathField.setEditable(false);
    	TextField imageMaskPathField = new TextField(App.getArgs()[App.ARGS_INDEX_MASK_IMAGE_FILE]);
    	imageMaskPathField.setMinWidth(300);
    	imageMaskPathField.setEditable(false);
    	TextField imageOutPathField = new TextField(App.getArgs()[App.ARGS_INDEX_OUTPUT_IMAGE_FILE]);
    	imageOutPathField.setMinWidth(300);
    	imageOutPathField.setEditable(false);
    	
    	Button importImageButton = new Button("Import Img.");
    	FileChooser imageImporter = new FileChooser();
    	imageImporter.setTitle("Select image file.");
    	Button importMaskButton = new Button("Import Mask");
    	FileChooser maskImporter = new FileChooser();
    	maskImporter.setTitle("Select mask (image) file.");
    	Button saveImageButton = new Button("Export (Save)");
    	imageExporter = new FileChooser();
    	imageExporter.setTitle("Select output file.");
    	
    	Label filterStrengthLabel = new Label("Filter strength / sensitivity");
    	Slider filterStrengthSlider = new Slider(0.0, 1.0, 0.5);
    	
    	ComboBox<Filter<ImageRaster>> filterSelectionDropdown = new ComboBox<>();
    	filterSelectionDropdown.getItems().addAll(FiltersList.FILTERS_LIST.values());
    	StringConverter<Filter<ImageRaster>> filterStringConverter = new StringConverter<>() {
    		@Override
    		public Filter<ImageRaster> fromString(String str) {
    			return FiltersList.fromString(str);
    		}
    		@Override
    		public String toString(Filter<ImageRaster> filter) {
    			return FiltersList.toString(filter);
    		}
    	};
    	filterSelectionDropdown.setConverter(filterStringConverter);
    	filterSelectionDropdown.setValue(FiltersList.DEFAULT_FILTER);
    	
    	CheckBox showMaskCheckbox = new CheckBox("Show mask overlay");
    	showMaskCheckbox.setSelected(true);
    	CheckBox preApplyMaskCheckbox = new CheckBox("Pre-apply mask");
    	preApplyMaskCheckbox.setSelected(false);
    	ComboBox<Filter<ImageRaster>> preMaskSelectionDropdown = new ComboBox<>();
    	preMaskSelectionDropdown.getItems().addAll(FiltersList.MASK_FILTER_LIST.values());
    	preMaskSelectionDropdown.setConverter(filterStringConverter);
    	preMaskSelectionDropdown.setValue(FiltersList.DEFAULT_MASK_FILTER);
    	preMaskSelectionDropdown.setDisable(true);
    	GridPane maskOptionsPane = new GridPane();
    	maskOptionsPane.setAlignment(Pos.CENTER_LEFT);
    	maskOptionsPane.add(showMaskCheckbox, 0, 0);
    	maskOptionsPane.add(preApplyMaskCheckbox, 0, 1);
    	maskOptionsPane.add(preMaskSelectionDropdown, 1, 1);
    	if (App.isMaskDisabled()) {
    		imageMaskPathField.setDisable(true);
    		maskOptionsPane.setDisable(true);
    	}
    	
    	ComboBox<RGBChannel> channelSelectionDropdown =
    			new ComboBox<>(FXCollections.observableArrayList(RGBChannel.values()));
    	channelSelectionDropdown.setValue(RGBChannel.ALL);
    	
    	filterPassGroupProgressBar = new ProgressBar(0.0);
    	filterPassGroupProgressBar.setMinWidth(300);
    	filterPassNumProgressBar = new ProgressBar(0.0);
    	filterPassNumProgressBar.setMinWidth(300);
    	filterPassProgressBar = new ProgressBar(0.0);
    	filterPassProgressBar.setMinWidth(300);
    	
    	Label filterGroupProgressLabel = new Label("00.00 %");
    	Label filterNumProgressLabel = new Label("00.00 %");
    	Label filterProgressLabel = new Label("00.00 %");
    	
    	CheckBox saveOnExitCheckbox = new CheckBox("Save on exit");
    	
    	double minThreads = 1;
    	double maxThreads = Runtime.getRuntime().availableProcessors();
    	double curThreads = (int)maxThreads / 2;
    	Slider executorThreadSelectionSlider = new Slider(minThreads, maxThreads, curThreads);
    	Label executorThreadSelectionLabel = new Label("Parallel Executor Threads: 0");
    	
    	Button cancelButton = new Button("Cancel");
    	
    	// ==================================== EVENT LISTENERS ====================================
    	applyFilterButton.setOnAction(event -> {
    		System.out.println("Applying filter");
    		
    		// Separate threads required, so JavaFX GUI can update it's graphics concurrently.
    		new Thread(() -> {
    			
    			int multiPassCount = multiPassCountSpinner.getValue();
    			for (int i = 1; i <= multiPassCount; i++) {
    				if (filterCancelRequested) {
    					System.err.println("Multi-Pass cancel requested.");
    					filterCancelRequested = false;
    					break;
    				}
    				if (multiPassEnableCheckbox.isSelected()) {
    					System.out.println("Multi-Pass " + i + " / " + multiPassCount);
        				multiPassProgressBar.setProgress((double)i / multiPassCount);
        				final int iFin = i;
        				Platform.runLater(() -> {
        					// Label change only possible from JavaFX thread.
        					multiPassLabel.setText("MP: " + iFin + " / " + multiPassCount);
        				});
    				}
    				if (preApplyMaskCheckbox.isSelected()) {
    					System.out.println("Applying pre-mask");
    					App.filterFrontend.applyFilter(filterSelectionDropdown.getValue(),
    							preMaskSelectionDropdown.getValue(),
            					channelSelectionDropdown.getValue(),
            					filterStrengthSlider.getValue(),
            					executorThreadSelectionSlider.valueProperty().intValue());
    				} else {
    					App.filterFrontend.applyFilter(filterSelectionDropdown.getValue(),
            					channelSelectionDropdown.getValue(),
            					filterStrengthSlider.getValue(),
            					executorThreadSelectionSlider.valueProperty().intValue());
    				}
    			}
    			multiPassProgressBar.setProgress(0);
    			Platform.runLater(() -> {
    				multiPassLabel.setText("MP: 0 / 0");
    			});
    			img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
    		}).start();
    		new Thread(() -> {
    			do {
        			try {
        				Thread.sleep(1000);
        			} catch (InterruptedException ie) {
        				ie.printStackTrace();
        			}
        			img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
        		} while (App.filterFrontend.isProcessorRunning());
    		}).start();
    	});
    	
    	multiPassEnableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
    		multiPassCountSpinner.setDisable(!newValue);
    		multiPassProgressBar.setDisable(!newValue);
    		multiPassLabel.setDisable(!newValue);
    		if (!newValue)
    			multiPassCountSpinner.getValueFactory().setValue(1);
    	});
    	
    	undoButton.setOnAction(event -> {
    		App.filterFrontend.undoLastAction();
    		img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
    	});
    	redoButton.setOnAction(event -> {
    		App.filterFrontend.redoLastAction();
    		img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
    	});
    	
    	importImageButton.setOnAction(event -> {
    		System.out.println("Importing new image and mask...");
    		File newImageFile = imageImporter.showOpenDialog(primaryStage);
    		imagePathField.setText(newImageFile.getAbsolutePath());
    		App.importImage(newImageFile.getAbsolutePath(), null);
    		img.setImage(convertToFxImage(App.filterFrontend.getLiveImage()));
    		rescaleImageView(img, IMG_TARGET_SCALE_X);
    		if (maskImg != null)
    			rescaleImageView(maskImg, MASK_TARGET_SCALE_X, img.getFitHeight());
    	});
    	importMaskButton.setOnAction(event -> {
    		System.out.println("Importing new mask...");
    		File newMaskFile = maskImporter.showOpenDialog(primaryStage);
    		imageMaskPathField.setText(newMaskFile.getAbsolutePath());
    		try { App.importMaskImage(newMaskFile.getAbsolutePath()); }
    		catch (IOException ioe) {
    			App.setMaskDisabled(true);
    			imageMaskPathField.setDisable(true);
	    		maskOptionsPane.setDisable(true);
	    		
    			System.err.println("IOException importing mask.");
    			ioe.printStackTrace();
    		}
    		App.setMaskDisabled(false);
    		imageMaskPathField.setDisable(false);
    		maskOptionsPane.setDisable(false);
    		maskImg.setImage(convertToFxImage(App.filterFrontend.getMaskImage()));
    		rescaleImageView(maskImg, MASK_TARGET_SCALE_X, img.getFitHeight());
    	});
    	
    	filterStrengthLabel.textProperty().bind(
    			Bindings.format("Filter strength / sensitivity (%.2f",
    					filterStrengthSlider.valueProperty().multiply(100.0)).concat("%)"));
    	
    	saveImageButton.setOnAction(event -> {
    		try {
    			File imageSaveFile = imageExporter.showSaveDialog(primaryStage);
    			imageOutPathField.setText(imageSaveFile.getAbsolutePath());
    			App.filterFrontend.saveFileTo(imageSaveFile);
    		} catch (IOException ioe) {
    			System.err.println("Error saving file. Does the process have sufficient permissions?");
    			ioe.printStackTrace();
    		}
    	});
    	
    	showMaskCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
    		maskImg.setVisible(newValue);
    	});
    	preApplyMaskCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
    		preMaskSelectionDropdown.setDisable(!newValue);
    		if (newValue)
    			preMaskSelectionDropdown.setValue(FiltersList.DEFAULT_MASK_FILTER);
    	});
    	
    	saveOnExitCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
    		App.setSaveOnExit(newValue);
    	});
    	
    	filterGroupProgressLabel.textProperty().bind(
    			Bindings.format("[Group] %s", filterPassGroupName));
    	filterNumProgressLabel.textProperty().bind(
    			Bindings.format("[Pass No.] %.0f / %.0f", filterPassNumCurrent, filterPassNumMax));
    	filterProgressLabel.textProperty().bind(
    			Bindings.format("[Prog.] %.2f ",
    					filterPassProgressBar.progressProperty().multiply(100.0)).concat("%"));
    	
    	// Only allow integer values for thread selection slider
    	executorThreadSelectionSlider.valueProperty().addListener((obs, oldval, newVal) -> {
        	executorThreadSelectionSlider.setValue(newVal.intValue());
    	});
    	executorThreadSelectionLabel.textProperty().bind(
    			Bindings.format("Parallel Executor Threads: %.0f", executorThreadSelectionSlider.valueProperty()));
    	
    	cancelButton.setOnAction(event -> {
    		filterCancelRequested = true;
    		App.filterFrontend.cancelProcessing();
    	});
    	
    	// =============================== WINDOW INITIALIZATION / LAYOUT ===============================
    	StackPane root = new StackPane();
    	GridPane gridPane = new GridPane();
    	gridPane.setPadding(new Insets(20, 20, 20, 20));
    	gridPane.setVgap(10);
    	gridPane.setHgap(10);
    	gridPane.setAlignment(Pos.CENTER);
    	//gridPane.setGridLinesVisible(true);
    	
    	StackPane imagePane = new StackPane();
    	imagePane.setAlignment(Pos.BOTTOM_RIGHT);
    	imagePane.getChildren().add(img);
    	imagePane.getChildren().add(maskImg);
    	
    	GridPane controlsPane = new GridPane();
    	controlsPane.setPadding(new Insets(20, 20, 20, 20));
    	controlsPane.setVgap(10);
    	controlsPane.setHgap(10);
    	controlsPane.setAlignment(Pos.CENTER_LEFT);
    	
    	controlsPane.add(imagePathField, 0, 0);
    	controlsPane.add(importImageButton, 1, 0);
    	controlsPane.add(imageMaskPathField, 0, 1);
    	controlsPane.add(importMaskButton, 1, 1);
    	controlsPane.add(imageOutPathField, 0, 2);
    	controlsPane.add(saveImageButton, 1, 2);
    	controlsPane.add(saveOnExitCheckbox, 1, 3);
    	controlsPane.add(filterSelectionDropdown, 0, 4);
    	controlsPane.add(channelSelectionDropdown, 1, 4);
    	controlsPane.add(maskOptionsPane, 0, 5);
    	controlsPane.add(executorThreadSelectionLabel, 0, 6);
    	controlsPane.add(executorThreadSelectionSlider, 0, 7);
    	controlsPane.add(filterStrengthLabel, 0, 8);
    	controlsPane.add(filterStrengthSlider, 0, 9);
    	controlsPane.add(multiPassOptionsGridPane, 0, 10);
    	controlsPane.add(applyFilterButton, 0, 11);
    	controlsPane.add(multiPassProgressBar, 0, 12);
    	controlsPane.add(multiPassLabel, 1, 12);
    	controlsPane.add(filterPassGroupProgressBar, 0, 13);
    	controlsPane.add(filterGroupProgressLabel, 1, 13);
    	controlsPane.add(filterPassNumProgressBar, 0, 14);
    	controlsPane.add(filterNumProgressLabel, 1, 14);
    	controlsPane.add(filterPassProgressBar, 0, 15);
    	controlsPane.add(filterProgressLabel, 1, 15);
    	controlsPane.add(undoButton, 0, 16);
    	controlsPane.add(cancelButton, 1, 16);
    	controlsPane.add(redoButton, 0, 17);
    	
    	gridPane.add(imagePane, 0, 0);
    	gridPane.add(controlsPane, 1, 0);
    	
    	applyFilterButton.setVisible(true);
    	root.getChildren().add(gridPane);
    	
    	double imgScale = (double)IMG_TARGET_SCALE_X / App.filterFrontend.imgWidth;
    	double scaledWidth = App.filterFrontend.imgWidth * imgScale;
    	double scaledHeight = App.filterFrontend.imgHeight * imgScale;
        Scene scene = new Scene(root, scaledWidth + 500, scaledHeight + 50);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image filter " + App.VERSION_STRING);
        primaryStage.show();
        
        // TODO: Future GUI elements to add:
        // TODO: Enable / disable value clamping button
        // TODO: Add batch processing
        // TODO: RGB Histogram graph (enable / disable)
	}
	
	@Override
	public void stop() {
		if (App.isSaveOnExit()) {
			try {
    			File imageSaveFile = imageExporter.showSaveDialog(primaryStage);
    			App.filterFrontend.saveFileTo(imageSaveFile);
    		} catch (IOException ioe) {
    			System.err.println("Error saving file. Does the process have sufficient permissions?");
    			ioe.printStackTrace();
    		}
		}
	}
	
	// Proudly stolen from:
    // https://stackoverflow.com/questions/30970005/bufferedimage-to-javafx-image
	// (modified)
	// FIXME: Test if this method is 100% correctly working
	// TODO: Add multithreading
    private static Image convertToFxImage(BufferedImage image) {
    	int sourceBitsPerChannel =
    			Integer.divideUnsigned(
    					image.getColorModel().getPixelSize(), image.getColorModel().getNumComponents());
    	int targetBitsPerChannel = 8;
    	int channelBitsDivisor = (int)Math.pow(2, sourceBitsPerChannel - targetBitsPerChannel);
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                	int[] rgb = image.getRaster().getPixel(x, y, (int[])(null));
                	// Only the last 8 bits of the color components are relevant
                	int red = (Integer.divideUnsigned(rgb[0], channelBitsDivisor)) & 0x000000FF;
                	int green = (Integer.divideUnsigned(rgb[1], channelBitsDivisor)) & 0x000000FF;
                	int blue = (Integer.divideUnsigned(rgb[2], channelBitsDivisor)) & 0x000000FF;
                	int fxARGB = 0xFF000000 | (red << 16) | (green << 8) | blue;
                    pw.setArgb(x, y, fxARGB);
                }
            }
        }
        return new ImageView(wr).getImage();
    }
	
    private static void rescaleImageView(ImageView img, double targetScaleX) {
    	double imgWidth = img.getImage().getWidth();
		double imgHeight = img.getImage().getHeight();
    	double imgScale = (double)targetScaleX / imgWidth;
    	double scaledWidth = imgWidth * imgScale;
    	double scaledHeight = imgHeight * imgScale;
    	img.setFitHeight(scaledHeight);
    	img.setFitWidth(scaledWidth);
    }
	private static void rescaleImageView(ImageView img, double targetScaleX, double targetScaleY) {
		if (img == null || img.getImage() == null) {
			System.err.println("FX Image is null, cannot transform its size.");
			return;
		}
		double imgWidth = img.getImage().getWidth();
		double imgHeight = img.getImage().getHeight();
    	double imgScaleX = (double)targetScaleX / imgWidth;
    	double imgScaleY = (double)targetScaleY / imgHeight;
    	System.out.println(
    			"Image width: " + imgWidth + ", Target: " + targetScaleX + ", Scale: " + imgScaleX + "\n"
    			+ "Image height: " + imgHeight + ", Target: " + targetScaleY + ", Scale: " + imgScaleY);
    	double scaledWidth = imgWidth * imgScaleX;
    	double scaledHeight = imgHeight * imgScaleY;
    	img.setFitHeight(scaledHeight);
    	img.setFitWidth(scaledWidth);
    }
	
	public static void setFilterProgress(int passGroup, int passNum, int maxPassNum,
			double passProgressPercentage) {
		Platform.runLater(() -> {
			if (filterPassGroupProgressBar == null
					|| filterPassNumProgressBar == null
					|| filterPassProgressBar == null)
				return;
			switch (passGroup) {
			case (MultiPassFilterApplicator.PASS_GROUP_NONE): {
				filterPassGroupName.set("NONE");
				filterPassGroupProgressBar.setProgress(0.0 / 2.0);
				break;
			}
			case (MultiPassFilterApplicator.PASS_GROUP_PRE): {
				filterPassGroupName.set("PRE");
				filterPassGroupProgressBar.setProgress(1.0 / 2.0);
				break;
			}
			case (MultiPassFilterApplicator.PASS_GROUP_MAIN): {
				filterPassGroupName.set("MAIN");
				filterPassGroupProgressBar.setProgress(2.0 / 2.0);
				break;
			}
			default: filterPassGroupName.set("UNKNOWN");
			}
			
			filterPassNumCurrent.set(passNum);
			filterPassNumMax.set(maxPassNum);
			if (passNum != -1)
				/* This +0.0001 seems to be absolutely necessary to solve some floating point shenanigans, which
				would otherwise indicate a progress of 0%, EVEN THOUGH 1.0 / 1.0 is 1.0 when
				printed with printf.
				Idk whats wrong at this point. It works, so I won't change it until it stops working again.*/
				filterPassNumProgressBar.setProgress((double)passNum / maxPassNum + 0.0001);
			else
				filterPassNumProgressBar.setProgress(0.0);
			
			filterPassProgressBar.setProgress(passProgressPercentage / 100);
		});
	}
	
	public static void setActionsCount(int undoLeft, int redoLeft) {
		Platform.runLater(() -> {
			undoButton.setText("Undo (" + undoLeft + ")");
			redoButton.setText("Redo (" + redoLeft + ")");
		});
	}
}
