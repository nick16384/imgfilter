package com.ImgFilter;

import java.io.File;

public enum ImageFileExtension {
	
	JPEG("[WIP] JPEG", ".jpeg", ".jpg"),
	PNG("PNG", ".png"),
	BMP("[WIP] BMP", ".bmp"),
	TIFF("TIFF", ".tiff", ".tif");
	
	private String name;
	private String[] allFileExtensions;
	private ImageFileExtension(String name, String... fileExtensions) {
		this.name = name;
		this.allFileExtensions = fileExtensions;
	}
	
	public static boolean isValidImageFileExtension(File file) {
		// Check for valid file name suffix
		boolean isValidSuffix = false;
		ImageFileExtension[] possibleFileExtensions = ImageFileExtension.values();
		for (ImageFileExtension extension : possibleFileExtensions) {
			for (String suffix : extension.allFileExtensions) {
				if (file.getAbsolutePath().endsWith(suffix)) {
					isValidSuffix = true;
					break;
				}
			}
		}
		return isValidSuffix;
	}
	
	public String[] getAllFileExtensions() {
		return this.allFileExtensions;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
