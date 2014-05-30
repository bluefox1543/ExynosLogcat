package com.sec.apps.exynos_logcat;

public class OverlaySize {
	private int width,height;
	
	public OverlaySize(){
		width = 280;
		height = 320;
	}
	
	public OverlaySize(int w,int h){
		width = w;
		height = h;
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