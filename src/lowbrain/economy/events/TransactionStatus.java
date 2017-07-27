package lowbrain.economy.events;

public enum TransactionStatus {
    VALID, // transaction is valid
    INVALID_DATA, // transaction data is missing from config file
    BANK_STOCK_MAXED, // bank can no longer buy this item because it has reach is maximum stock quantity
    BANK_STOCK_LOW, // bank can no longer sell this item because it has reach is minimum stock quantity
    PLAYER_MISSING_INVENTORY, // player doesn't have enough of the items in his inventory
    BANK_CAPACITY_MAXED, // bank can no longer sell because it has reach is maximum bank account amount
    BANK_CAPACITY_LOW // bank can no longer buy because it has reach is minimum bank account amount
}
