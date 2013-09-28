/*
 * This is class for joining and splitting bit into MSB and LSB.
 * needs to bw improved:
 * -case if joining 16 bit to 64bit
 */
package xbeegateway;
import java.lang.Math;

/**
 *
 * @author A.R. Dzulqarnain
 */
public class Bitplay {
    public void Bitplay(){};
    //bitval in every method tells how long the source or target bit .ex:16bit,8bit,64bit
    public int joinBit(int valMSB,int valLSB, int bitlength){
        bitlength=bitlength/2;
	int val=(valMSB<<bitlength) | valLSB;	
	return val;
    }
    
    public int getMSB(int value, int bitlength){
	bitlength=bitlength/2;
	int mask=getMask(bitlength);
        int msb= (value>>>bitlength) & mask;
	return msb;
    }
	
    public int getLSB(int value, int bitlength){
	bitlength=bitlength/2;
	int mask=getMask(bitlength);
        int lsb= value & mask;
	return lsb;
    }
	
    public int getMask(int length){
	int num=0;
	for(int i=0;i<length;i++){
            num=num+((int)Math.pow(2,i));
	}
	return num;
    }
}