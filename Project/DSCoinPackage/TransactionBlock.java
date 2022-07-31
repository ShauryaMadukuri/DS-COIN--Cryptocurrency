package DSCoinPackage;

import HelperClasses.MerkleTree;

public class TransactionBlock {

  public Transaction[] trarray;
  public TransactionBlock previous;
  public TransactionBlock next;
  public MerkleTree Tree;
  public String trsummary;
  public String nonce;
  public String dgst;

  TransactionBlock(Transaction[] t) {
    trarray = t.clone();
    previous = null;
    next = null;
    Tree = new MerkleTree();
    trsummary = Tree.Build(trarray);
    dgst = null;
  }

  public boolean checkTransaction(Transaction t) {
    boolean valid = false;
    if (t.coinsrc_block == null) {
      return true;
    }

    for (Transaction currt : t.coinsrc_block.trarray) {
      if (currt.coinID.equals(t.coinID) && currt.Destination.equals(t.Source)) {
        valid = true;
        break;
      }
    }

    if (this != t.coinsrc_block) {
      TransactionBlock currBlock = previous;
      checkvalid: {
        while (!currBlock.equals(t.coinsrc_block)) {
          for (Transaction currTr : currBlock.trarray) {
            if (currTr.coinID.equals(t.coinID)) {
              valid = false;
              break checkvalid;
            }
          }
          currBlock = currBlock.previous;
        }
      }
    }

    return valid;
  }
}