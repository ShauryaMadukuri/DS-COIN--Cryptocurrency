package DSCoinPackage;

import java.util.*;

import HelperClasses.CRF;
import HelperClasses.MerkleTree;
import HelperClasses.Pair;
import HelperClasses.TreeNode;

public class Members {

  public String UID;
  public List<Pair<String, TransactionBlock>> mycoins = new ArrayList<>();
  public Transaction[] in_process_trans;

  public void addCoin(Pair<String, TransactionBlock> coin) {
    for (int i = 0; i < mycoins.size(); i++) {
      String curr = mycoins.get(i).get_first();
      if (Integer.parseInt(curr) > Integer.parseInt(coin.first)) {
        mycoins.add(i, coin);
        return;
      }
    }
    mycoins.add(coin);
  }

  public void initiateCoinsend(String destUID, DSCoin_Honest DSobj) {
    Pair<String, TransactionBlock> giveCoin = mycoins.get(0);
    mycoins.remove(0);

    Transaction trans = new Transaction();
    for (Members m : DSobj.memberlist) {
      if (m.UID.equals(destUID)) {
        trans.Destination = m;
        break;
      }
    }
    trans.Source = this;
    trans.coinID = giveCoin.get_first();
    trans.coinsrc_block = giveCoin.get_second();

    for (int i = 0; i < in_process_trans.length; i++) {
      if (in_process_trans[i] == null) {
        in_process_trans[i] = trans;
      }
    }

    DSobj.pendingTransactions.AddTransactions(trans);
  }

  public Pair<TransactionBlock, Integer> findTrans(Transaction tobj, DSCoin_Honest DSObj) {

    TransactionBlock tB = DSObj.bChain.lastBlock;
    int i = 0;
    findTr: {
      while (tB != null) {
        for (i = 0; i < DSObj.bChain.tr_count; i++) {
          Transaction t = tB.trarray[i];
          if (t.equals(tobj)) {
            break findTr;
          }
        }
        tB = tB.previous;
      }
    }

    return new Pair<TransactionBlock, Integer>(tB, i);
  }

  public List<Pair<String, String>> findDgstList(TransactionBlock tB) {
    TransactionBlock currBlock = tB;
    List<Pair<String, String>> dgstList = new ArrayList<Pair<String, String>>();

    if (currBlock == null) {
      dgstList = null;
    } else {
      dgstList.add(new Pair<String, String>(currBlock.previous.dgst, null));
      while (currBlock != null) {
        Pair<String, String> dgstPair = new Pair<String, String>(currBlock.dgst,
            currBlock.previous.dgst + "#" + currBlock.trsummary + "#" + currBlock.nonce);
        dgstList.add(dgstPair);
        currBlock = currBlock.next;
      }
    }
    return dgstList;
  }

  public List<Pair<String, String>> findSibPath(TransactionBlock tB, int index) {
    List<Pair<String, String>> sibPath = new ArrayList<Pair<String, String>>();
    TreeNode curr = tB.Tree.rootnode;
    int numleaves = tB.Tree.numdocs;
    index++;
    while (curr.left != null) {
      if (index > numleaves / 2) {
        curr = curr.right;
        index -= numleaves / 2;
      } else {
        curr = curr.left;
      }
      numleaves /= 2;
    }

    while (curr.parent != null) {
      sibPath.add(new Pair<String, String>(curr.parent.left.val, curr.parent.right.val));
      curr = curr.parent;
    }
    sibPath.add(new Pair<String, String>(curr.val, null));
    return sibPath;
  }

  public Pair<List<Pair<String, String>>, List<Pair<String, String>>> finalizeCoinsend(Transaction tobj,
      DSCoin_Honest DSObj) throws MissingTransactionException {
    Pair<TransactionBlock, Integer> trans = findTrans(tobj, DSObj);
    TransactionBlock tB = trans.get_first();
    int index = trans.get_second();
    if (tB == null) {
      throw new MissingTransactionException();
    }

    List<Pair<String, String>> sibPath = findSibPath(tB, index);
    List<Pair<String, String>> dgstList = findDgstList(tB);

    for (int j = 0; j < in_process_trans.length; j++) {
      if (tobj.equals(in_process_trans[j])) {
        in_process_trans[j] = null;
      }
    }

    tobj.Destination.addCoin(new Pair<String, TransactionBlock>(tobj.coinID, tB));

    return new Pair<List<Pair<String, String>>, List<Pair<String, String>>>(sibPath, dgstList);
  }

  public void MineCoin(DSCoin_Honest DSObj) {

    Transaction[] _trarray = new Transaction[DSObj.bChain.tr_count];
    int validcount = 0;
    Transaction currTr;
    try {
      boolean valid;
      while (validcount != DSObj.bChain.tr_count - 1) {
        currTr = DSObj.pendingTransactions.RemoveTransaction();
        valid = DSObj.bChain.lastBlock.checkTransaction(currTr);

        if (valid) {
          for (Transaction t : DSObj.bChain.lastBlock.trarray) {
            if (t.coinID.equals(currTr.coinID) && currTr.Source.equals(t.Source)) {
              valid = false;

              break;
            }
          }
        }

        if (valid) {
          for (Transaction t : _trarray) {
            if (t == null) {
              break;
            }
            if (t.coinID.equals(currTr.coinID)) {

              valid = false;
              break;
            }
          }
        }

        if (valid) {
          _trarray[validcount] = currTr;
          validcount++;
        }

      }
    } catch (Exception e) {
    }

    Transaction minerRewardTransaction = new Transaction();

    int latestCoinID = Integer.valueOf(DSObj.latestCoinID);
    minerRewardTransaction.coinID = String.valueOf(++latestCoinID);
    DSObj.latestCoinID = String.valueOf(latestCoinID);

    minerRewardTransaction.Source = null;
    minerRewardTransaction.Destination = this;
    minerRewardTransaction.coinsrc_block = null;

    _trarray[_trarray.length - 1] = minerRewardTransaction;

    TransactionBlock tB = new TransactionBlock(_trarray);
    DSObj.bChain.InsertBlock_Honest(tB);

    addCoin(new Pair<String, TransactionBlock>(minerRewardTransaction.coinID, tB));

  }

  public void MineCoin(DSCoin_Malicious DSObj) {
    Transaction[] _trarray = new Transaction[DSObj.bChain.tr_count];
    int validcount = 0;
    Transaction currTr;
    try {
      boolean valid;
      TransactionBlock lastBlock = DSObj.bChain.FindLongestValidChain();
      while (validcount != DSObj.bChain.tr_count - 1) {

        currTr = DSObj.pendingTransactions.RemoveTransaction();
        valid = lastBlock.checkTransaction(currTr);

        if (valid) {
          for (Transaction t : lastBlock.trarray) {
            if (t.coinID.equals(currTr.coinID) && currTr.Source.equals(t.Source)) {
              valid = false;

              break;
            }
          }
        }

        if (valid) {
          for (Transaction t : _trarray) {
            if (t == null) {
              break;
            }
            if (t.coinID.equals(currTr.coinID)) {

              valid = false;
              break;
            }
          }
        }

        if (valid) {
          _trarray[validcount] = currTr;
          validcount++;
        }

      }
    } catch (Exception e) {
    }

    Transaction minerRewardTransaction = new Transaction();

    int latestCoinID = Integer.valueOf(DSObj.latestCoinID);
    minerRewardTransaction.coinID = String.valueOf(++latestCoinID);
    DSObj.latestCoinID = String.valueOf(latestCoinID);

    minerRewardTransaction.Source = null;
    minerRewardTransaction.Destination = this;
    minerRewardTransaction.coinsrc_block = null;

    _trarray[_trarray.length - 1] = minerRewardTransaction;

    TransactionBlock tB = new TransactionBlock(_trarray);
    DSObj.bChain.InsertBlock_Malicious(tB);

    addCoin(new Pair<String, TransactionBlock>(minerRewardTransaction.coinID, tB));

  }

}