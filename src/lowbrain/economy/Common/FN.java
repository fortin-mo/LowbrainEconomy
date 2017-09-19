package lowbrain.economy.Common;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FN {
    public final static NumberFormat MONEY_FORMAT = new DecimalFormat("#0.00");
    public static String toMoney(double value) {
        return MONEY_FORMAT.format(value);
    }
}
