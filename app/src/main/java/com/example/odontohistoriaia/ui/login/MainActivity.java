package com.example.odontohistoriaia.ui.login;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.odontohistoriaia.R;
import android.content.Intent;
import com.example.odontohistoriaia.ui.home.HomeActivity;
import com.example.odontohistoriaia.data.local.AppDatabase;
import com.example.odontohistoriaia.data.local.DatabaseProvider;
import com.example.odontohistoriaia.model.Paciente;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText etUsuario = findViewById(R.id.etUsuario);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String password = etPassword.getText().toString();

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.login_error_campos_vacios, Toast.LENGTH_SHORT).show();
            } else {
                AppDatabase db = DatabaseProvider.getDatabase(MainActivity.this);

                DatabaseProvider.databaseExecutor.execute(() -> {
                    if (db.pacienteDao().obtenerPrimero() == null) {
                        Paciente paciente = new Paciente("María Fernanda Ortiz", "Sensibilidad en pieza 26", "20 sept, 10:00 am — Dra. Valeria Soto");
                        db.pacienteDao().insertar(paciente);
                    }
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                    });
                });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}