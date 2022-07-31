package DSCoinPackage;

import HelperClasses.Pair;

import java.util.ArrayList;
import java.util.List;

public class Moderator {

  public void initializeDSCoin(DSCoin_Honest DSObj, int coinCount) {

    Members moderator = new Members();
    moderator.UID = "Moderator";

    int currCoinID = 99999;
    int currMember = 0;
    Transaction t;
    Transaction[] trarray;
    TransactionBlock tB;

    for (int i = 0; i < coinCount / DSObj.bChain.tr_count; i++) {
      trarray = new Transaction[DSObj.bChain.tr_count];
      for (int j = 0; j < DSObj.bChain.tr_count; j++) {
        t = new Transaction();
        t.Source = moderator;
        t.Destination = DSObj.memberlist[currMember];
        t.coinsrc_block = null;
        t.coinID = String.valueOf(++currCoinID);
        if (currMember == DSObj.memberlist.length - 1) {
          currMember = 0;
        } else {
          currMember++;
        }
        trarray[j] = t;
      }
      tB = new TransactionBlock(trarray);

      int count = DSObj.bChain.tr_count;
      int mem;
      if (currMember == 0) {
        mem = DSObj.memberlist.length - 1;
      } else {
        mem = currMember - 1;
      }

      int coinID = currCoinID;
      while (count > 0) {
        DSObj.memberlist[mem].addCoin(new Pair<String, TransactionBlock>(String.valueOf(coinID), tB));
        coinID--;
        count--;
        if (mem == 0) {
          mem = DSObj.memberlist.length - 1;
        } else {
          mem--;
        }
      }
      DSObj.bChain.InsertBlock_Honest(tB);
    }
    DSObj.latestCoinID = String.valueOf(currCoinID);
  }

  public void initializeDSCoin(DSCoin_Malicious DSObj, int coinCount) {

    Members moderator = new Members();
    moderator.UID = "Moderator";

    int currCoinID = 99999;
    int currMember = 0;
    Transaction t;
    Transaction[] trarray;
    TransactionBlock tB;

    for (int i = 0; i < coinCount / DSObj.bChain.tr_count; i++) {
      trarray = new Transaction[DSObj.bChain.tr_count];
      for (int j = 0; j < DSObj.bChain.tr_count; j++) {
        t = new Transaction();
        t.Source = moderator;
        t.Destination = DSObj.memberlist[currMember];
        t.coinsrc_block = null;
        t.coinID = String.valueOf(++currCoinID);
        if (currMember == DSObj.memberlist.length - 1) {
          currMember = 0;
        } else {
          currMember++;
        }
        trarray[j] = t;
      }
      tB = new TransactionBlock(trarray);

      int count = DSObj.bChain.tr_count;
      int mem;
      if (currMember == 0) {
        mem = DSObj.memberlist.length - 1;
      } else {
        mem = currMember - 1;
      }

      int coinID = currCoinID;
      while (count > 0) {
        DSObj.memberlist[mem].addCoin(new Pair<String, TransactionBlock>(String.valueOf(coinID), tB));
        coinID--;
        count--;
        if (mem == 0) {
          mem = DSObj.memberlist.length - 1;
        } else {
          mem--;
        }
      }
      DSObj.bChain.InsertBlock_Malicious(tB);
    }
    DSObj.latestCoinID = String.valueOf(currCoinID);
  }

}