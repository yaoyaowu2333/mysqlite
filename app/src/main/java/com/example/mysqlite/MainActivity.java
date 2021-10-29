package com.example.mysqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    private MyDAO myDAO;  //数据库访问对象
    private ListView listView;
    private List<Map<String,Object>> listData;
    private List<Map<String,String>> nameData;
    private Map<String,Object> listItem;
    private SimpleAdapter listAdapter;

    private EditText et_name;  //数据表包含3个字段，第1字段为自增长类型
    private EditText et_price;
    private EditText et_money;
    private EditText et_flag;

    private  String selId=null;  //选择项id
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_add= (Button) findViewById(R.id.bt_add);bt_add.setOnClickListener(this);
        Button bt_modify=(Button)findViewById(R.id.bt_modify);bt_modify.setOnClickListener(this);
        Button bt_del=(Button)findViewById(R.id.bt_del);bt_del.setOnClickListener(this);

        et_name=(EditText)findViewById(R.id.et_name);
        et_price=(EditText)findViewById(R.id.et_price);
        et_money=(EditText)findViewById(R.id.et_money);
        et_flag=(EditText)findViewById(R.id.et_flag);

        myDAO = new MyDAO(this);  //创建数据库访问对象
        if(myDAO.getRecordsNumber()==0) {  //防止重复运行时重复插入记录
            myDAO.insertInfo("Jay", 18.3, 18.3, "买");   //插入记录
        }

        displayRecords();   //显示记录
    }
    public void displayRecords(){  //显示记录方法定义
        listView = (ListView)findViewById(R.id.listView);
        listData = new ArrayList<Map<String,Object>>();
        nameData = new ArrayList<Map<String,String>>();

        Cursor cursor = myDAO.allQuery();//查出全部记录
        while (cursor.moveToNext()){
            int id=cursor.getInt(0);  //获取字段值
            String name=cursor.getString(cursor.getColumnIndex("name"));
            double price=cursor.getDouble(cursor.getColumnIndex("price"));//推荐此种方式
            double money=cursor.getDouble(cursor.getColumnIndex("money"));
            String flag=cursor.getString(cursor.getColumnIndex("flag"));

            listItem=new HashMap<String,Object>(); //必须在循环体里新建
            listItem.put("_id", id);  //第1参数为键名，第2参数为键值
            listItem.put("name", name);
            listItem.put("price", price);
            listItem.put("money", money);
            listItem.put("flag", flag);
            listData.add(listItem);   //添加一条记录
        }
        //列表显示listData的数据
        listAdapter = new SimpleAdapter(this,
                listData,
                R.layout.list_item, //自行创建的列表项布局
                new String[]{"_id","name","price","money","flag"},
                new int[]{R.id.tv_id,R.id.tvname,R.id.tvprice,R.id.tvmoney,R.id.tvflag});
        listView.setAdapter(listAdapter);  //应用适配器

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //列表项监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> rec= (Map<String, Object>) listAdapter.getItem(position);  //从适配器取记录

                et_name.setText(rec.get("name").toString());  //刷新文本框
                et_price.setText(rec.get("price").toString());
                et_money.setText(rec.get("money").toString());
                et_flag.setText(rec.get("flag").toString());
                Log.i("ly",rec.get("_id").toString());
                selId=rec.get("_id").toString();  //供修改和删除时使用
            }
        });
    }
    public void onClick(View v) {  //实现的接口方法
        if(selId!=null) {  //选择了列表项后，可以增加/删除/修改
            String p1 = et_name.getText().toString().trim();
            double p2 = Double.parseDouble(et_price.getText().toString());
            double p3 = Double.parseDouble(et_money.getText().toString());
            String p4 = et_flag.getText().toString().trim();
            switch (v.getId()){
                case  R.id.bt_add://感觉没啥用，因为插入时没有id，所以在插入情况下走不到这一步
                    myDAO.insertInfo(p1,p2,p3,p4);
                    break;
                case  R.id.bt_modify:
                    myDAO.updateInfo(p1,p2,p3,p4,selId);
                    Toast.makeText(getApplicationContext(),"更新成功！",Toast.LENGTH_SHORT).show();
                    break;
                case  R.id.bt_del:
                    myDAO.deleteInfo(selId);
                    Toast.makeText(getApplicationContext(),"删除成功！",Toast.LENGTH_SHORT).show();
                    et_name.setText(null);et_price.setText(null);et_money.setText(null);et_flag.setText(null); selId=null; //提示
            }
        }else{  //未选择列表项
            if(v.getId()==R.id.bt_add) {  //单击添加按钮
                String p1 = et_name.getText().toString();
                String p2 = et_price.getText().toString();
                String p3 = et_money.getText().toString();
                String p4 = et_flag.getText().toString();
                if(p1.equals("")||p2.equals("")||p3.equals("")||p4.equals("")){  //要求输入了信息
                    Toast.makeText(getApplicationContext(),"任一元素都不能空！",Toast.LENGTH_SHORT).show();
                }else{
                    myDAO.insertInfo(p1, Double.parseDouble(p2),Double.parseDouble(p3),p4);  //第2参数转型
                }
            } else{   //单击了修改或删除按钮
                Toast.makeText(getApplicationContext(),"请先选择记录！",Toast.LENGTH_SHORT).show();
            }
        }
        displayRecords();//刷新ListView对象
    }
}