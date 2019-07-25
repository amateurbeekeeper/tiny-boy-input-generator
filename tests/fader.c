#include <avr/io.h>
#include <util/delay.h>
#include "tinyboy.h"

int main (void){
  DDRB = 0b00001111;
  PORTB = 0b00000000;
  //
  for(int i=7;i>=0;i=i-1) {
    int pixel = 0;
    for(int j=0;j!=512;++j) {
      int byte = 0;
      for(int k=0;k!=8;++k) {
	if(pixel == 0) {
	  byte |= 1 << k;
	}
	pixel = pixel + i;
	if(pixel >= 8) {
	  pixel = pixel - 8;
	}
      }
      display_write(byte);
    }
    _delay_ms(50);
  }
  // Done
}
