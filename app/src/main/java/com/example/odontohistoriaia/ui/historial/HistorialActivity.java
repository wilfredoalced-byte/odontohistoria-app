package com.example.odontohistoriaia.ui.historial;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.odontohistoriaia.R;
import com.example.odontohistoriaia.data.remote.RetrofitClient;
import com.example.odontohistoriaia.model.Tratamiento;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class HistorialActivity extends AppCompatActivity {

    public static final String EXTRA_PACIENTE_ID = "pacienteRemotoId";

    private LinearLayout contenedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        contenedor = findViewById(R.id.contenedorTratamientos);

        int pacienteId = getIntent().getIntExtra(EXTRA_PACIENTE_ID, 0);
        if (pacienteId <= 0) {
            mostrarMensaje("Todavía no se sincronizó el paciente con el servidor. Vuelve a la pantalla anterior, espera un momento y abre el historial de nuevo.");
            return;
        }
        mostrarMensaje("Cargando tratamientos...");
        cargarDesdeServidor(pacienteId);
    }

    // GET /tratamientos?pacienteId=...
    private void cargarDesdeServidor(int pacienteId) {
        RetrofitClient.redExecutor.execute(() -> {
            try {
                Response<List<Tratamiento>> r = RetrofitClient.getApiService().obtenerTratamientos(pacienteId).execute();
                if (!r.isSuccessful() || r.body() == null) {
                    runOnUiThread(() -> mostrarMensaje("No se pudo cargar el historial del servidor."));
                    return;
                }
                runOnUiThread(() -> mostrarLista(r.body()));
            } catch (IOException e) {
                runOnUiThread(() -> mostrarMensaje("Sin conexión con el servidor (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")."));
            }
        });
    }

    private void mostrarLista(List<Tratamiento> tratamientos) {
        contenedor.removeAllViews();
        if (tratamientos.isEmpty()) {
            mostrarMensaje("Todavía no tienes tratamientos registrados.");
            return;
        }
        for (Tratamiento t : tratamientos) {
            contenedor.addView(crearFilaTratamiento(t));
        }
    }

    private void mostrarMensaje(String texto) {
        contenedor.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(14);
        tv.setTextColor(0xFF888888);
        contenedor.addView(tv);
    }

    private LinearLayout crearFilaTratamiento(Tratamiento t) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);
        card.setRadius(32);
        card.setCardElevation(0);
        card.setStrokeWidth(2);
        card.setStrokeColor(0xFFE0E0E0);

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.VERTICAL);
        fila.setPadding(40, 32, 40, 32);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(t.nombre);
        tvNombre.setTextSize(16);
        tvNombre.setTypeface(null, Typeface.BOLD);

        TextView tvFecha = new TextView(this);
        String fecha = (t.fecha == null || t.fecha.isEmpty()) ? "Sin fecha" : t.fecha;
        tvFecha.setText(fecha + " · " + (t.estado == null ? "" : t.estado));
        tvFecha.setTextSize(12.5f);
        tvFecha.setTextColor(0xFF888888);
        LinearLayout.LayoutParams fechaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fechaParams.topMargin = 6;
        tvFecha.setLayoutParams(fechaParams);

        fila.addView(tvNombre);
        fila.addView(tvFecha);
        card.addView(fila);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.addView(card);
        return wrapper;
    }
}
