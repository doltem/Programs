/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xbeegateway;

/**
 *
 * @author A.R. Dzulqarnain
 */
public class CRC {
    int[] crcTable=new int[256];
    int poly=0x8005; //polynom based on CRC-16 Standard


    public void CRC(){
        int remain; //remainder variable
        for(int div = 0; div <256 ; ++div){ //divident index
            remain = div << 8;

            for(short bit = 0; bit<8; ++bit){
                int topbit = remain & (1<<15);
                remain <<= 1;
                if (topbit!=0){
                        remain ^= poly;
                }
            }

            crcTable[div] = remain;
        }
    }

    public int getCRC(int[] payload, int length){
        int data;
        int remain = 0; //remainder initial value

        for(short index=0; index<length ;++index){
                data = payload[index] ^ (remain >>> 8);
                remain = crcTable[data] ^ (remain<<8);
        }

        return remain;
    }
    
}
