package DSCoinPackage;

public class TransactionQueue {

  public Transaction firstTransaction;
  public Transaction lastTransaction;
  public int numTransactions;

  public void AddTransactions(Transaction transaction) {
    if (firstTransaction == null) {
      firstTransaction = transaction;
    } else {
      lastTransaction.next = transaction;
    }
    lastTransaction = transaction;
    numTransactions++;
  }

  public Transaction RemoveTransaction() throws EmptyQueueException {
    if (firstTransaction == null) {
      throw new EmptyQueueException();
    }
    Transaction removed = firstTransaction;
    firstTransaction = firstTransaction.next;
    removed.next = null;
    numTransactions--;
    return removed;
  }

  public int size() {
    return numTransactions;
  }
}
