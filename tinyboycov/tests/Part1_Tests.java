package tinyboycov.tests;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Part1_Tests {
  /**
   * You can change this number as you see fit. Remember, 8Mhz means 8_000_000
   * cycles per second. Hence, 8_000 cycles in one ms.
   */
  public static final int PULSE_LENGTH = (125 * 8_000);

  @Test
  public void test_01_fader() throws IOException {
    String inputSequence = "__R__L_______U_LDRR______URRLL_R__R__L__LD_U__U_______UU_DU_DU__L___U________U_____D_D______D__LU___";
    TestUtils.checkManualCoverage("fader.hex", inputSequence, PULSE_LENGTH);
  }

  @Test
  public void test_02_blocks() throws IOException {
    String inputSequence = "_RD____U_ULDD___D_LURDD_UL___R____LLR__L_U_D_DD__R";
    TestUtils.checkManualCoverage("blocks.hex", inputSequence, PULSE_LENGTH);
  }

  @Test
  public void test_03_blocks_2() throws IOException {
    String inputSequence = "_RD____U_ULDD___D_LURDD_UL___R____LLR__L_U_D_DD__R";
    TestUtils.checkManualCoverage("blocks_2.hex", inputSequence, PULSE_LENGTH);
  }

  @Test
  public void test_04_sokoban() throws IOException {
    String inputSequence = "LRD_LDLDUDUD_LLLRLLLRRDRUUD_DRULL__L_DUURLRULRRU_RLRLLRL_LLRRLUUURURR_RLLD_D_LDDRULRULLLUUUURDRLDUU_";
    TestUtils.checkManualCoverage("sokoban.hex", inputSequence, PULSE_LENGTH);
  }

  @Test
  public void test_05_snake() throws IOException {
    String inputSequence = "_URLUURDRRLR_UUURULRLU___LRRLUDRURD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD";
    TestUtils.checkManualCoverage("snake.hex", inputSequence, PULSE_LENGTH);
  }

  @Test
  public void test_06_tetris() throws IOException {
    String inputSequence = "RLDUDLDDUURRUDURLRUULLLLLULULDLLUDDRURLDLLDLLULDLRURUDRURRRUULUDDDDULLLRLLDDULUDURRULRDRRUUDURLLULDU";
    TestUtils.checkManualCoverage("tetris.hex", inputSequence, PULSE_LENGTH);
  }
}
