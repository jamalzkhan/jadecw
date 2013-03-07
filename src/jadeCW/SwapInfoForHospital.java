package jadeCW;

import java.io.Serializable;

import jade.core.AID;

public class SwapInfoForHospital implements Serializable {
	
	public int currentSlot;
	public int swapSlot;
	public AID swapWith;
	
	public SwapInfoForHospital(int currentSlot, int swapSlot, AID swapWith){
		this.currentSlot = currentSlot;
		this.swapSlot = swapSlot;
		this.swapWith = swapWith;
	}
	
	public int getCurrentSlot() {
		return currentSlot;
	}
	
	public int getSwapSlot() {
		return swapSlot;
	}
	
	public AID getSwapWith() {
		return swapWith;
	}

}
