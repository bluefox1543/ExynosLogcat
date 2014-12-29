package com.sec.apps.exynos_logcat;

public class OverlaySize {
	private int width,height;
	private boolean defaultSize;
	
	public OverlaySize(){
		width = 280;
		height = 320;
		defaultSize = true;
	}

	public OverlaySize(int w,int h,boolean defaultSize){
		width = w;
		height = h;
		this.defaultSize = defaultSize;
	}
	
	public boolean isDefaultSize() {
		return defaultSize;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}