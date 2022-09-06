package izbirkom.magadan.povestka;

import android.Manifest;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ListActivity {
    private String headerStr = "ПОВЕСТКА ДНЯ\nзаседания Избирательной комиссии Магаданской области";
    private ArrayList<String> s = new ArrayList<>();
    private String fileName;
    View header;
    final String DIR_SD = "povestka";
    final String FILENAME_SD = "povestka.txt";

    void readFile (){
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Ошибка доступа к хранилищу", Toast.LENGTH_LONG).show();
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                s.add(str.replace("::","\n"));
            }
            br.close();
            s.removeAll(Arrays.asList("", null));
            headerStr = headerStr + "\n\n" + s.get(0);
            s.remove(0);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Toast.makeText(this, "Не найден файл с вопросами povestka.txt", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка чтения файла povestka.txt", Toast.LENGTH_LONG).show();
        }
    }
    View createHeader(String text) {
        View view = getLayoutInflater().inflate(R.layout.list_header, null);
        ((TextView)view.findViewById(R.id.textViewHeaderText)).setText(text);
        return view;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(android.R.id.list);
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            //done
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        readFile();
        header = createHeader(headerStr);
        listView.addHeaderView(header);
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this, R.layout.list_item, s);
        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, s);
        // Привяжем массив через адаптер к ListView
        listView.setAdapter(adapter);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        fileName = Environment.getExternalStorageDirectory().toString() +"/" + DIR_SD + "/" + (position) + ".pdf";
        File f = new File(fileName);
        if (position > 0) {
            if (f.exists() && !f.isDirectory()) {
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(Uri.fromFile(f), "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    startActivity(target);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Отсутствует приложение для просмотра PDF файлов.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Отсутствует вложение", Toast.LENGTH_SHORT).show();
            }
        }
    }
}