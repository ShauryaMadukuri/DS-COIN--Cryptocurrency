package DSCoinPackage;

import HelperClasses.CRF;
import HelperClasses.MerkleTree;

public class BlockChain_Malicious {

  public int tr_count;
  public static final String start_string = "DSCoin";
  public TransactionBlock[] lastBlocksList;

  public static boolean checkTransactionBlock(TransactionBlock tB) {
    if (!tB.dgst.substring(0, 4).equals("0000")) {
      return false;
    }

    CRF obj = new CRF(64);
    if (tB.previous == null) {
      if (!tB.dgst.equals(obj.Fn(BlockChain_Malicious.start_string + "#" + tB.trsummary + "#" + tB.nonce))) {
        return false;
      }
    } else {
      if (!tB.dgst.equals(obj.Fn(tB.previous.dgst + "#" + tB.trsummary + "#" + tB.nonce))) {
        return false;
      }
    }

    MerkleTree test = new MerkleTree();
    if (!tB.trsummary.equals(test.Build(tB.trarray))) {
      return false;
    }

    for (Transaction t : tB.trarray) {
      if (!tB.checkTransaction(t)) {
        return false;
      }

    }
    return true;
  }

  public TransactionBlock FindLongestValidChain() {
    int longest = 0;
    TransactionBlock longestlast = null;
    if (lastBlocksList == null) {
      return null;
    }
    for (TransactionBlock tB : lastBlocksList) {
      int currlen = 0;
      TransactionBlock curr = tB;
      TransactionBlock currLast = tB;
      while (curr != null) {
        if (checkTransactionBlock(curr)) {
          currlen++;
        } else {
          currlen = 0;
          currLast = curr.previous;
        }
        curr = curr.previous;
      }
      if (currlen > longest) {
        longest = currlen;
        longestlast = currLast;
      }
    }
    return longestlast;
  }

  public void setNonceDgst(TransactionBlock tB, String prevdgst) {

    int nonce = 1000000001;
    CRF obj = new CRF(64);
    String dgst;
    while (true) {
      dgst = obj.Fn(prevdgst + "#" + tB.trsummary + "#" + nonce);

      if (dgst.substring(0, 4).equals("0000")) {
        tB.nonce = Integer.toString(nonce);
        tB.dgst = dgst;
        break;
      }
      nonce++;
    }
  }

  public void InsertBlock_Malicious(TransactionBlock newBlock) {
    boolean isEnd = true;
    TransactionBlock lastBlock = FindLongestValidChain();
    if (lastBlock == null) {
      isEnd = false;
      newBlock.next = null;
      newBlock.previous = null;
      setNonceDgst(newBlock, start_string);
    }

    else {
      newBlock.previous = lastBlock;
      if (lastBlock.next != null) {
        isEnd = false;
      }
      lastBlock.next = newBlock;
      newBlock.next = null;
      setNonceDgst(newBlock, lastBlock.dgst);
    }

    if (isEnd) {
      for (int i = 0; i < lastBlocksList.length; i++) {
        if (lastBlocksList[i].equals(lastBlock)) {
          lastBlocksList[i] = newBlock;
          break;
        }
      }
    } else {
      for (int i = 0; i < lastBlocksList.length; i++) {
        if (lastBlocksList[i] == null) {
          lastBlocksList[i] = newBlock;
          break;
        }
      }
    }
  }

}