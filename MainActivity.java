package com.example.bt_sqlite;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button btnCreateDatabase=null;
    Button btnInsertCategory=null;
    Button btnShowCategoryList=null;
    Button btnShowCategoryList2=null;
    Button btnTransaction=null;
    Button btnShowDetail=null;
    Button btnInsertComputer=null;
    public static final int OPEN_CATEGORY_DIALOG=1;
    public static final int SEND_DATA_FROM_CATEGORY_ACTIVITY=2;
    SQLiteDatabase database=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnInsertCategory=(Button) findViewById(R.id.btnInsertCategory);
        btnInsertCategory.setOnClickListener(new MyEvent());
        btnShowCategoryList=(Button) findViewById(R.id.buttonShowCategoryList);
        btnShowCategoryList.setOnClickListener(new MyEvent());
        btnInsertCategory=(Button) findViewById(R.id.buttonInsertCategory);
        btnInsertCategory.setOnClickListener(new MyEvent());
        getDatabase();
    }

    public boolean isTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public SQLiteDatabase getDatabase()
    {
        try
        {
            database=openOrCreateDatabase("mydata.db", Context.MODE_PRIVATE, null);
            if(database!=null)
            {
                if(isTableExists(database,"tblCategories"))
                    return database;
                database.setLocale(Locale.getDefault());
                database.setVersion(1);
                String sqlCategory="create table tblCategories ("
                        +"id integer primary key autoincrement,"
                        +"firstname text, "
                        +"lastname text)";
                database.execSQL(sqlCategory);
                String sqlComputer="create table tblComputers ("
                        +"id integer primary key autoincrement,"
                        +"title text, "
                        +"dateadded date,"
                        +"authorid integer not null constraint categoryid references tblCategories(id) on delete cascade)";
                database.execSQL(sqlComputer);
                //Cách tạo trigger khi nhập dữ liệu sai ràng buộc quan hệ
                String sqlTrigger="create trigger fk_insert_computer before insert on tblCoputers "
                        +" for each row "
                        +" begin "
                        +" 	select raise(rollback,'them du lieu tren bang tblComputers bi sai') "
                        +" 	where (select id from tblCategories where id=new.categoryid) is null ;"
                        +" end;";
                database.execSQL(sqlTrigger);
                Toast.makeText(MainActivity.this, "OK OK", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return database;
    }
    public void createDatabaseAndTrigger()
    {
        if(database==null)
        {
            getDatabase();
            Toast.makeText(MainActivity.this, "OK OK", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * hàm mở màn hình nhập Tác giả
     */
    public void showInsertCategoryDialog()
    {
        Intent intent=new Intent(MainActivity.this, CreateCategoryActivity.this);
        startActivityForResult(intent, OPEN_CATEGORY_DIALOG);
    }
    /**
     * hàm xem danh sách tác giả dùng Activity
     * Tôi làm 2 cách để các bạn ôn tập lại ListView
     * bạn gọi hàm nào thì gọi 1 thôi showAuthorList1 hoặc showAuthorList2
     */
    public void showCategoryList1()
    {
        Intent intent=new Intent(MainActivity.this, ShowListCategoryActivity.class);
        startActivity(intent);
    }
    /**
     * hàm xem danh sách tác giả dùng ListActivity
     * Tôi làm 2 cách để các bạn ôn tập lại ListView
     * bạn gọi hàm nào thì gọi 1 thôi showAuthorList1 hoặc showAuthorList2
     */
    public void showCategoryList2()
    {
        Intent intent=new Intent(MainActivity.this, ShowListCategoryActivity2.class);
        startActivity(intent);
    }
    /**
     * Tôi cung cấp thêm hàm này để các bạn nghiên cứu thêm về transaction
     */
    public void interactDBWithTransaction()
    {
        if(database!=null)
        {
            database.beginTransaction();
            try
            {
                //làm cái gì đó tùm lum ở đây,
                //chỉ cần có lỗi sảy ra thì sẽ kết thúc transaction
                ContentValues values=new ContentValues();
                values.put("firstname", "xx");
                values.put("lastname", "yyy");
                database.insert("tblCategories", null, values);
                database.delete("tblCategories", "ma=?", new String[]{"x"});
                //Khi nào hàm này được gọi thì các thao tác bên trên mới thực hiện được
                //Nếu nó không được gọi thì mọi thao tác bên trên đều bị hủy
                database.setTransactionSuccessful();
            }
            catch(Exception ex)
            {
                Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally
            {
                database.endTransaction();
            }
        }
    }
    /**
     * hàm xử lý kết quả trả về
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==SEND_DATA_FROM_CATEGORY_ACTIVITY)
        {
            Bundle bundle= data.getBundleExtra("DATA_CATEGORY");
            String firstname=bundle.getString("firstname");
            String lastname=bundle.getString("lastname");
            ContentValues content=new ContentValues();
            content.put("firstname", firstname);
            content.put("lastname", lastname);
            if(database!=null)
            {
                long categoryid=database.insert("tblCategories", null, content);
                if(categoryid==-1)
                {
                    Toast.makeText(MainActivity.this,categoryid+" - "+ firstname +" - "+lastname +" ==> insert error!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, categoryid+" - "+firstname +" - "+lastname +" ==>insert OK!", Toast.LENGTH_LONG).show();
                }
            }

        }
    }
    /**
     * class xử lý sự kiện
     * @author drthanh
     *
     */
    private class MyEvent implements OnClickListener
    {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(v.getId()==R.id.btnInsertCategory)
            {
                showInsertCategoryDialog();
            }
            else if(v.getId()==R.id.buttonShowCategoryList)
            {
                showCategoryList1();
            }

            else if(v.getId()==R.id.buttonInsertComputer)
            {
                Intent intent=new Intent(MainActivity.this, InsertComputerActivity.class);
                startActivity(intent);
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_simple_database_main, menu);
        return true;
    }
}