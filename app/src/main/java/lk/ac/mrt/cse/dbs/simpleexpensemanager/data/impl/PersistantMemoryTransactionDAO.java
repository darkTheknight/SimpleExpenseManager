package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistantMemoryTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {

    public static final String DATABASE_NAME = "170343T.db";
	
	public static final DATABASE_VERSION=1;
    public static final String EXPENSE_COLUMN_ID = "ID";
    public static final String EXPENSE_COLUMN_NO = "Account_Number";
    public static final String EXPENSE_COLUMN_Date = "Date";
    public static final String EXPENSE_COLUMN_Type = "Type";
    public static final String EXPENSE_COLUMN_Amount = "Amount";

    private List<Transaction> transactions;

    public PersistantMemoryTransactionDAO(Context context) {
        super(context, DATABASE_NAME , null,DATABASE_VERSION);
        transactions = new LinkedList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO Auto-generated method stub
		String CREATE_tbltrans_TABLE = "CREATE TABLE"+ TABLE_tbltrans + "(" +EXPENSE_COLUMN_ID +"INTEGER PRIMARY,"+EXPENSE_COLUMN_NO +"VARCHAR,"+EXPENSE_COLUMN_Date+"DATE," +EXPENSE_COLUMN_Type+ "TEXT,"+EXPENSE_COLUMN_Amount+"DECIMAL"+ ")";
		db.excecSQL(CREATE_tbltrans_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
		// drop if older exist
        db.execSQL("DROP TABLE IF EXISTS tbltrans");
		// create table again
        onCreate(db);
    }


    @Override
    public void logTransaction(Date Date, String Account_Number, ExpenseType expenseType, double Amount) {
        Transaction transaction = new Transaction(Date, Account_Number, expenseType, Amount);
        String accountNumber = transaction.getAccount_Number();
        Date Dates = transaction.getDate();

        byte[] byteDate = Dates.toString().getBytes();
        ExpenseType Types = transaction.getExpenseType();
        String strType = Types.toString();
        byte[] byteType = toString().getBytes();
        Double Amounts = transaction.getAmount();

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        Log.d("Date",formattedDate);
        byte[] timeStamp = formattedDate.getBytes();


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Account_Number", Account_Number);
        contentValues.put("Amount", Amounts);
        contentValues.put("Type",strType);
        contentValues.put("Date", byteDate);


        db.insert("tbltrans", null, contentValues);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        transactions.clear();
        Log.d("creation","starting");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( " select * from tbltrans", null );

        res.moveToFirst();

        while(res.isAfterLast() == false){

            String Account_Number = res.getString(res.getColumnIndex(EXPENSE_COLUMN_NO));
            Double Amount = res.getDouble(res.getColumnIndex(EXPENSE_COLUMN_Amount));
            String transType = res.getString(res.getColumnIndex(EXPENSE_COLUMN_Type));

            ExpenseType Type = ExpenseType.valueOf(transType);
            byte[] Date = res.getBlob(res.getColumnIndex(EXPENSE_COLUMN_Date));


            String str = new String(Date, StandardCharsets.UTF_8);
            Log.d("loadedDate",str);

            Date finalDate;
            try {


                SimpleDateFormat inputFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss 'GMT'z", Locale.ENGLISH);
                finalDate = inputFormat.parse(str);
                transactions.add(new Transaction(finalDate,Account_Number,Type,Amount));
                Log.d("creation","success");
            }catch (java.text.ParseException e){
                Log.d("creation","failed");
                Calendar cal = Calendar.getInstance();

                finalDate = cal.getTime();
                transactions.add(new Transaction(finalDate,Account_Number,Type,Amount));

            }


            res.moveToNext();
        }
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        return transactions.subList(size - limit, size);
    }




}


