package jadeCW;

import java.io.Serializable;

public class SwapInfo implements Serializable {
	
	public int currentSlot;
	public int swapSlot;
	
	public SwapInfo(int currentSlot, int swapSlot){
		this.currentSlot = currentSlot;
		this.swapSlot = swapSlot;
	}
	
	public int getCurrentSlot() {
		return currentSlot;
	}
	
	public int getSwapSlot() {
		return swapSlot;
	}
	

}
