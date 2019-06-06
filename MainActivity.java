package com.kproject.filemanager.activities;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.kproject.filemanager.R;
import com.kproject.filemanager.activities.MainActivity;
import com.kproject.filemanager.adapter.ManagerAdapter;
import com.kproject.filemanager.model.Manager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final String RESTORE_CURRENT_PATH = "restoreCurrentPath";
	private static final String HOME_PATH = Environment.getExternalStorageDirectory().toString();
	
	private List<Manager> listFolders;
	private List<Manager> listFiles;
	private String currentPath;
	private String backDir;
	
	private ListView lvListManager;
	private TextView tvListEmpty;

	private static final int PERMISSION_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lvListManager = findViewById(R.id.lvListManager);
		tvListEmpty = findViewById(R.id.tvListViewEmpty);
		
		// Para Android 6.0 Marshmallow e superior
		if (Build.VERSION.SDK_INT >= 23) {
			if (permissionGranted()) {
				if (savedInstanceState != null) {
					currentPath = savedInstanceState.getString(RESTORE_CURRENT_PATH);
					fileManager(currentPath);
					listviewClicks();
				}
			} else {
				requestPermissionInfo();
			}
		// Para Android 5.1 Lollipop e inferior
		} else {
			if (savedInstanceState != null) {
				currentPath = savedInstanceState.getString(RESTORE_CURRENT_PATH);
				fileManager(currentPath);
				listviewClicks();
			} else {
				fileManager(HOME_PATH);
				listviewClicks();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(RESTORE_CURRENT_PATH, currentPath);
		super.onSaveInstanceState(outState);
	}
	
	private void fileManager(String dirProject) {
		listFolders = new ArrayList<>();
		listFiles = new ArrayList<>();
		currentPath = dirProject;
		File file = new File(dirProject);
		backDir = file.getParent();
		File[] files = file.listFiles();
		
		if (!dirProject.equals(HOME_PATH)) {
			listFolders.add(new Manager("...", backDir));
		}
		
		for (File f : files) {
			if (!f.isHidden() && f.canRead()) {
				if (f.isDirectory()) {
					listFolders.add(new Manager(f.getName(), f.getPath()));
				} else {
					listFiles.add(new Manager(f.getName(), f.getPath()));
				}
			}
		}

		Collections.sort(listFolders);
		Collections.sort(listFiles);
		listFolders.addAll(listFiles);
		
		ArrayAdapter listCustomAdapter = new ManagerAdapter(this, listFolders);
		lvListManager.setAdapter(listCustomAdapter);
		lvListManager.setEmptyView(tvListEmpty);
	}
	
	private void listviewClicks() {
		lvListManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p1, View p2, int position, long p4) {
				File file = new File(listFolders.get(position).getDirPath());
				if (file.isDirectory()) {
					fileManager(listFolders.get(position).getDirPath());
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (currentPath.equals(HOME_PATH)) {
			super.onBackPressed();
		} else{
			fileManager(backDir);
			listviewClicks();
		}
	}
	
	private void requestPermissionInfo() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			// Mensagem de informação para caso o usuário já tenha negado as permissões pelo menos uma vez
			showAlertDialog();
		} else {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
		}
	}
	
	// Método chamado assim que o usuário concede ou nega uma permissão
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					fileManager(HOME_PATH);
					listviewClicks();
				} else {
					requestPermissionInfo();
				}
				break;
		}
	}

	private boolean permissionGranted() {
		int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			return false;
		}
	}
	
	private void showAlertDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle("Permissão não concedida");
		dialog.setMessage("Por favor, conceda a permissão para acessar o armazenamento externo, pois é necessário para que o aplicativo funcione corretamente.");
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				// Pede a permissão novamente
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton("Cancelar", null);
		dialog.show();
	}
	
}
