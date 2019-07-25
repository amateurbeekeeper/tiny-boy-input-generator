package tinyboycov.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javr.core.AVR;
import javr.core.AvrDecoder;
import javr.core.AvrInstruction;
import javr.io.HexFile;
import javrsim.peripherals.JPeripheral;
import tinyboy.core.TinyBoyEmulator;
import tinyboy.util.AutomatedTester;
import tinyboy.util.CoverageAnalysis;
import tinyboy.views.TinyBoyPeripheral;
import tinyboycov.core.TinyBoyInputGenerator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Part2_Tests {

  @Test
  public void test_01_fader() throws IOException {
    TestUtils.checkFuzzingCoverage("fader.hex");
  }

  @Test
  public void test_02_blocks() throws IOException {
    TestUtils.checkFuzzingCoverage("blocks.hex");
  }

  @Test
  public void test_03_blocks_2() throws IOException {
    TestUtils.checkFuzzingCoverage("blocks_2.hex");
  }

  @Test
  public void test_04_sokoban() throws IOException {
    TestUtils.checkFuzzingCoverage("sokoban.hex");
  }

  @Test
  public void test_05_snake() throws IOException {
    TestUtils.checkFuzzingCoverage("snake.hex");
  }

  @Test
  public void test_06_tetris() throws IOException {
    TestUtils.checkFuzzingCoverage("tetris.hex");
  }
}
