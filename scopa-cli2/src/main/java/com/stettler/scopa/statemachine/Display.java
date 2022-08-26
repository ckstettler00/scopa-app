package com.stettler.scopa.statemachine;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.model.Suit;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Display {

     Logger logger = LoggerFactory.getLogger(getClass().getName());
     private final static char VERTICAL = (char)0x2502;
     private final static char HORIZ = (char)0x2500;
     private final static int WIDTH = 95;
     private final static int HEIGHT = 25;
     private char[][] screen = new char[HEIGHT][WIDTH];

     private static char BOX_TOP_LEFT = (char)0x250c;
     private static char BOX_TOP_RIGHT = (char)0x2510;
     private static char BOX_BOT_LEFT = (char)0x2514;
     private static char BOX_BOT_RIGHT = (char)0x2518;


     private static final int HAND_COL_POS = 34;
     private static final int HAND_ROW_POS = 19;
     private static final int CARD_HEIGHT = 5;
     private static final int CARD_WIDTH = 7;
     private static final int HAND_2_ROW = 1;
     private static final int TABLE_COL = 24;
     private static final int TABLE_ROW = 7;


     List<String> playerIds = new ArrayList<>();
     Map<String, GameStatus> gameStatus = new ConcurrentHashMap<>();

     public void clearScreen() {
          for (int i =0; i < HEIGHT; i++) {
               for (int j=0; j < WIDTH; j++) {
                    screen[i][j] = ' ';
               }
          }
          drawBox(0,0, HEIGHT, WIDTH);
          drawLabel();
     }

     public void drawBox(int row, int col, int height, int width) {
          screen[row][col] = BOX_TOP_LEFT;
          screen[row][col+width-1] = BOX_TOP_RIGHT;
          screen[row+height-1][col] = BOX_BOT_LEFT;
          screen[row+height-1][col+width-1] = BOX_BOT_RIGHT;
          for (int i = col+1; i < col+width-1; i++) {
               screen[row][i] = HORIZ;
          }
          for (int i = row+1; i < row+height-1; i++) {
               screen[i][col] = VERTICAL;
          }
          for (int i = row+1; i < row+height-1; i++) {
               screen[i][col+width-1] = VERTICAL;
          }
          for (int i = col+1; i < col+width-1; i++) {
               screen[row+height-1][i] = HORIZ;
          }
     }

     synchronized public void println(String message) {
          System.out.println(message);
     }

     synchronized public void render() {
          for (int i = 0; i < HEIGHT; i++) {
               StringBuilder line = new StringBuilder();
               for (int j=0;j<WIDTH; j++) {
                    line.append(screen[i][j]);
               }
               System.out.println(line.toString());
          }
     }
     public void makeCard(int row, int col, Card card){
          drawBox(row, col, CARD_HEIGHT, CARD_WIDTH);
          if (card.getVal() < 10){
               screen[row+1][col+3] = String.valueOf(card.getVal()).charAt(0);
               screen[row+3][col+3] = String.valueOf(card.getVal()).charAt(0);
          }
          else{
               screen[row+1][col+3] = '1';
               screen[row+1][col+4] = '0';
               screen[row+3][col+3] = '1';
               screen[row+3][col+4] = '0';
          }
          screen[row+2][col+3] = Suit.symbol(card.getSuit());

     }
     public void makeTable(List<Card> table){
          int R = TABLE_ROW;
          int C = TABLE_COL;
          int count = 0;

          for(Card card: table ) {
               makeCard(R, C, card);
               count++;
               C = C + 10;
               if (count > 4) {
                    R = 13;
                    C = 24;
                    count =0; // start of new row.
               }

          }

     }
     public void makeHand(int P, List<Card> hand){
          int HRP = HAND_COL_POS;
          int num;
          if(P == 0)
               num = 3;
          else if(P == 1 || P == 2)
               num = 2;
          else if(P == 3 || P == 4)
               num = 1;
          else
               num = 0;
          for(int i = 0; i<num; i++){
               opponentCard(HRP);
               HRP = HRP + 10;
          }
          HRP = HAND_COL_POS;
          num = 0;
          for(Card card: hand ) {
               makeCard(HAND_ROW_POS, HRP, hand.get(num));
               HRP = HRP + 10;
               num++;
          }

     }

     public void updateGameStatus(GameStatus status) {
          String playerId = status.getPlayer().getDetails().getPlayerId();
          String currentPlayerId = status.getCurrentPlayerId();

          if (!playerIds.contains(playerId)) {
               playerIds.add(playerId);
          }
          gameStatus.put(playerId, status);

          // Create a temp array to flip flop the display order based
          // on whose turn it is.
          List<String> tmpIds = new ArrayList<>();
          if (playerIds.size() > 0 && playerIds.get(0).equals(currentPlayerId)) {
               tmpIds.addAll(playerIds);
          } else if (playerIds.size() > 1) {
               tmpIds.add(playerIds.get(1));
               tmpIds.add(playerIds.get(0));
          } else {
               return;
          }

          logger.info("Display current player:{} order: {}", currentPlayerId, tmpIds);

          if (tmpIds.size() > 1) {
               String pid = tmpIds.get(1);
               writePlayerDetails(2, gameStatus.get(pid).getPlayer().getDetails());
               writePlayerScore(3, gameStatus.get(pid).getPlayer().getScore());
          }
          if (tmpIds.size() > 0) {
               String pid = tmpIds.get(0);
               writePlayerDetails(21, gameStatus.get(pid).getPlayer().getDetails());
               writePlayerScore(22, gameStatus.get(pid).getPlayer().getScore());
          }

          for (int i =0; i < tmpIds.size(); i++)
          {
               int idx = tmpIds.size() - i - 1;
               GameStatus tmpStatus = this.gameStatus.get(tmpIds.get(idx));
               makeHand(idx, tmpStatus.getPlayer().getHand());
               makeTable(tmpStatus.getTable());
               remainingDeck("" + tmpStatus.getCardsRemaining());
          }
     }
     protected void writePlayerDetails(int startRow, PlayerDetails details) {
          char[] nameArr = details.getScreenHandle().toCharArray();
          for(int i = 13; i < 13+nameArr.length; i++){
               screen[startRow][i] = nameArr[i-13];
          }
     }

     protected void writePlayerScore(int startRow, int score) {
          char[] scoreArr = (score+"").toCharArray();
          for(int i = 13; i < 13+scoreArr.length; i++){
               screen[startRow][i] = scoreArr[i-13];
          }
     }


     public void drawLabel() {
          screen[21][5] = 'P';
          screen[21][6] = 'l';
          screen[21][7] = 'a';
          screen[21][8] = 'y';
          screen[21][9] = 'e';
          screen[21][10] = 'r';
          screen[21][11] = ':';
          screen[21][12] = ' ';

          screen[22][5] = 'S';
          screen[22][6] = 'c';
          screen[22][7] = 'o';
          screen[22][8] = 'r';
          screen[22][9] = 'e';
          screen[22][10] = ':';
          screen[22][11] = ' ';

          screen[2][5] = 'P';
          screen[2][6] = 'l';
          screen[2][7] = 'a';
          screen[2][8] = 'y';
          screen[2][9] = 'e';
          screen[2][10] = 'r';
          screen[2][11] = ':';
          screen[2][12] = ' ';

          screen[3][5] = 'S';
          screen[3][6] = 'c';
          screen[3][7] = 'o';
          screen[3][8] = 'r';
          screen[3][9] = 'e';
          screen[3][10] = ':';
          screen[3][11] = ' ';

          screen[8][77] = 'S';
          screen[8][78] = 'u';
          screen[8][79] = 'i';
          screen[8][80] = 't';
          screen[8][81] = ' ';
          screen[8][82] = 'i';
          screen[8][83] = 'n';
          screen[8][84] = 'd';
          screen[8][85] = 'e';
          screen[8][86] = 'x';
          screen[8][87] = ':';
          screen[8][88] = ' ';


          screen[10][77] = 'S';
          screen[10][78] = 'w';
          screen[10][79] = 'o';
          screen[10][80] = 'r';
          screen[10][81] = 'd';
          screen[10][82] = 's';
          screen[10][83] = ' ';
          screen[10][84] = '=';
          screen[10][85] = ' ';
          screen[10][86] = (char) (0x03c4);

          screen[11][77] = 'S';
          screen[11][78] = 'c';
          screen[11][79] = 'e';
          screen[11][80] = 'p';
          screen[11][81] = 't';
          screen[11][82] = 'e';
          screen[11][83] = 'r';
          screen[11][84] = 's';
          screen[11][85] = ' ';
          screen[11][86] = '=';
          screen[11][87] = ' ';
          screen[11][88] = (char) 0x00a1;

          screen[12][77] = 'C';
          screen[12][78] = 'u';
          screen[12][79] = 'p';
          screen[12][80] = 's';
          screen[12][81] = ' ';
          screen[12][82] = '=';
          screen[12][83] = ' ';
          screen[12][84] = (char) (0xfc);


          screen[13][77] = 'C';
          screen[13][78] = 'o';
          screen[13][79] = 'i';
          screen[13][80] = 'n';
          screen[13][81] = 's';
          screen[13][82] = ' ';
          screen[13][83] = '=';
          screen[13][84] = ' ';
          screen[13][85] = (char) 0xa2;

          screen[1][33] = '1';
          screen[1][43] = '2';
          screen[1][53] = '3';

          screen[7][23] = '0';
          screen[7][33] = '1';
          screen[7][43] = '2';
          screen[7][53] = '3';
          screen[7][63] = '4';
          screen[13][23] = '5';
          screen[13][33] = '6';
          screen[13][43] = '7';
          screen[13][53] = '8';
          screen[13][63] = '9';

          screen[19][33] = '1';
          screen[19][43] = '2';
          screen[19][53] = '3';

     }
     public void opponentCard(int hand2Col){
          drawBox(HAND_2_ROW,hand2Col , CARD_HEIGHT, CARD_WIDTH);
          screen[HAND_2_ROW+2][hand2Col+1] = 'S';
          screen[HAND_2_ROW+2][hand2Col+2] = 'c';
          screen[HAND_2_ROW+2][hand2Col+3] = 'o';
          screen[HAND_2_ROW+2][hand2Col+4] = 'p';
          screen[HAND_2_ROW+2][hand2Col+5] = 'a';
     }

     public void remainingDeck(String num){
          char[] numArr = num.toCharArray();
          drawBox(TABLE_ROW +3,TABLE_COL - 15 ,CARD_HEIGHT, CARD_WIDTH);
          screen[TABLE_ROW + 5][TABLE_COL-14] = 'S';
          screen[TABLE_ROW + 5][TABLE_COL-13] = 'c';
          screen[TABLE_ROW + 5][TABLE_COL-12] = 'o';
          screen[TABLE_ROW + 5][TABLE_COL-11] = 'p';
          screen[TABLE_ROW + 5][TABLE_COL-10] = 'a';
          for(int i = 0; i < numArr.length; i++)
          screen[TABLE_ROW + 4][TABLE_COL-12+i] = numArr[i];
     }
}

