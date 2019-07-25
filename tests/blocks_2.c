#include <avr/io.h>
#include <util/delay.h>
#include "tinyboy.h"

void display_ul() {
  for(int y=0;y<64;++y) {
    for(int x=0;x<8;++x) {
      if(x < 4 && y < 32) {
	display_write(0xFF);
      } else {
	display_write(0x0);
      }
    }
  }
}


void display_ll() {
  for(int y=0;y<64;++y) {
    for(int x=0;x<8;++x) {
      if(x < 4 && y >= 32) {
	display_write(0xFF);
      } else {
	display_write(0x0);
      }
    }
  }
}

void display_ur() {
  for(int y=0;y<64;++y) {
    for(int x=0;x<8;++x) {
      if(x >= 4 && y < 32) {
	display_write(0xFF);
      } else {
	display_write(0x0);
      }
    }
  }
}

void display_lr() {
  for(int y=0;y<64;++y) {
    for(int x=0;x<8;++x) {
      if(x >= 4 && y >= 32) {
	display_write(0xFF);
      } else {
	display_write(0x0);
      }
    }
  }
}


void display_clear() {
  for(int y=0;y<64;++y) {
    for(int x=0;x<8;++x) {
      display_write(0x0);
    }
  }
}

int main (void){
  DDRB = 0b00001111;
  PORTB = 0b00000000;
  //
  int button;
  //
  while(1) { // loop forever
    // Wait for ANY key press
    display_ul();
    while((button = read_buttons()) == 0) {

    }
    // Then, depending on that press do something.
    if(button == BUTTON_DOWN) {
      display_ll();
      while((read_buttons() & BUTTON_RIGHT) == 0) {
	// Keep looping :)
      }
      display_lr();    
    } else if(button == BUTTON_RIGHT) {
      display_ur();
      while((read_buttons() & BUTTON_DOWN) == 0) {
	// Keep looping :)
      }
      display_lr();
    }
    // Done
  }
}
