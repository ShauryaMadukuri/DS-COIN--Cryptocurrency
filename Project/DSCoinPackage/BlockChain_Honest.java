package DSCoinPackage;

import HelperClasses.CRF;

public class BlockChain_Honest {

  public int tr_count;
  public static final String start_string = "DSCoin";
  public TransactionBlock lastBlock;

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

  public void InsertBlock_Honest(TransactionBlock newBlock) {
    if (lastBlock == null) {
      setNonceDgst(newBlock, start_string);
    } else {
      setNonceDgst(newBlock, lastBlock.dgst);
    }

    newBlock.previous = lastBlock;
    if (lastBlock != null) {
      lastBlock.next = newBlock;
    }
    lastBlock = newBlock;
  }
}
