uint16_t crcTable[256];
uint16_t poly=0x8005; //polynom based on CRC-16 Standard


void crcTableInit(){
	uint16_t remain; //remainder variable
	for(int div = 0; div <256 ; ++div){ //divident index
		remain = div << 8;
		
		for(uint16_t bit = 0; bit<8; ++bit){
			uint16_t topbit = remain & (1<<15);
			remain <<= 1;
			if (topbit)
            {
                remain ^= poly;
            }
		}
		
		crcTable[div] = remain;
	}
}

uint16_t getCRC(uint8_t *payload, int length){
	uint8_t data;
	uint16_t remain = 0; //remainder initial value
	
	for(int byte=0; byte<length ;++byte){
		data = *payload++ ^ (remain >> 8);
		remain = crcTable[data] ^ (remain<<8);
		
	}
	
	return remain;
}