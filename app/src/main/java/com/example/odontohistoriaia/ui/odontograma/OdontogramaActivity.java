package com.example.odontohistoriaia.ui.odontograma;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.odontohistoriaia.R;
import com.example.odontohistoriaia.data.remote.RetrofitClient;
import com.example.odontohistoriaia.model.EstadoPieza;
import com.example.odontohistoriaia.model.PiezaOdontograma;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class OdontogramaActivity extends AppCompatActivity {

    public static final String EXTRA_PACIENTE_ID = "pacienteRemotoId";

    // 0 = sano, 1 = caries, 2 = tratado (el estado verdadero vive en el servidor)
    private final int[] estadoDientes = new int[8];
    private final String[] numerosDientes = {"18", "17", "16", "15", "14", "13", "12", "11"};
    private static final String[] NOMBRES_ESTADO = {"Sano", "Caries", "Tratado"};

    private int pacienteId;
    private final FrameLayout[] halos = new FrameLayout[8];
    private final TextView[] iconos = new TextView[8];
    private final TextView[] etiquetas = new TextView[8];
    private final TextView[] badges = new TextView[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odontograma);

        pacienteId = getIntent().getIntExtra(EXTRA_PACIENTE_ID, 0);

        LinearLayout rowDientes = findViewById(R.id.rowDientes);

        for (int i = 0; i < estadoDientes.length; i++) {
            LinearLayout diente = crearDiente(i);
            rowDientes.addView(diente);
        }

        if (pacienteId > 0) {
            cargarDesdeServidor();
        } else {
            Toast.makeText(this, "Sin conexión con el servidor: los cambios no se guardarán.", Toast.LENGTH_LONG).show();
        }
    }

    // GET /pacientes/:id/odontograma
    private void cargarDesdeServidor() {
        RetrofitClient.redExecutor.execute(() -> {
            try {
                Response<List<PiezaOdontograma>> r = RetrofitClient.getApiService().obtenerOdontograma(pacienteId).execute();
                if (!r.isSuccessful() || r.body() == null) {
                    avisar("No se pudo cargar el odontograma del servidor.");
                    return;
                }
                runOnUiThread(() -> {
                    for (PiezaOdontograma pieza : r.body()) {
                        for (int i = 0; i < numerosDientes.length; i++) {
                            if (numerosDientes[i].equals(String.valueOf(pieza.pieza))) {
                                estadoDientes[i] = indiceEstado(pieza.estado);
                                pintar(i);
                            }
                        }
                    }
                });
            } catch (IOException e) {
                avisar("Sin conexión con el servidor. No se pudo cargar el odontograma.");
            }
        });
    }

    // PUT o DELETE /pacientes/:id/odontograma/:pieza. Si falla, la pieza vuelve a su estado anterior.
    private void guardarEnServidor(int index, int anterior, int nuevo) {
        if (pacienteId <= 0) {
            avisar("Sin conexión con el servidor: el cambio no se guardó.");
            return;
        }
        int pieza = Integer.parseInt(numerosDientes[index]);
        RetrofitClient.redExecutor.execute(() -> {
            boolean correcto;
            try {
                if (nuevo == 0) {
                    Response<PiezaOdontograma> r = RetrofitClient.getApiService().quitarPieza(pacienteId, pieza).execute();
                    correcto = r.isSuccessful() || r.code() == 404; // 404 = ya estaba sin marca
                } else {
                    Response<PiezaOdontograma> r = RetrofitClient.getApiService()
                            .marcarPieza(pacienteId, pieza, new EstadoPieza(NOMBRES_ESTADO[nuevo])).execute();
                    correcto = r.isSuccessful();
                }
            } catch (IOException e) {
                correcto = false;
            }
            if (!correcto) {
                runOnUiThread(() -> {
                    estadoDientes[index] = anterior;
                    pintar(index);
                });
                avisar("No se pudo guardar la pieza " + pieza + " en el servidor.");
            }
        });
    }

    private void avisar(String texto) {
        runOnUiThread(() -> Toast.makeText(this, texto, Toast.LENGTH_LONG).show());
    }

    private int indiceEstado(String estado) {
        for (int i = 0; i < NOMBRES_ESTADO.length; i++) {
            if (NOMBRES_ESTADO[i].equals(estado)) return i;
        }
        return 0;
    }

    // Aplica el estado actual de una pieza a sus vistas
    private void pintar(int index) {
        int estado = estadoDientes[index];
        iconos[index].setBackground(crearFondoRedondeado(estado));
        halos[index].setBackground(crearHalo(estado));
        etiquetas[index].setText(textoEstado(estado));
        etiquetas[index].setTextColor(colorTextoEstado(estado));
        actualizarBadge(badges[index], estado);
    }

    private LinearLayout crearDiente(int index) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsContenedor = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsContenedor.setMargins(dpToPx(3), 0, dpToPx(3), 0);
        contenedor.setLayoutParams(paramsContenedor);

        // Halo exterior (profundidad simulada con dos capas de color)
        FrameLayout halo = new FrameLayout(this);
        halo.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60)));
        halo.setBackground(crearHalo(estadoDientes[index]));

        // Círculo del diente
        TextView icono = new TextView(this);
        FrameLayout.LayoutParams paramsIcono = new FrameLayout.LayoutParams(dpToPx(46), dpToPx(46));
        paramsIcono.gravity = Gravity.CENTER;
        icono.setLayoutParams(paramsIcono);
        icono.setGravity(Gravity.CENTER);
        icono.setText("🦷");
        icono.setTextSize(19);
        icono.setBackground(crearFondoRedondeado(estadoDientes[index]));
        icono.setElevation(dpToPx(2));

        // Badge pequeño con check al estar "tratado" (detalle extra de pulido)
        TextView badge = new TextView(this);
        FrameLayout.LayoutParams paramsBadge = new FrameLayout.LayoutParams(dpToPx(16), dpToPx(16));
        paramsBadge.gravity = Gravity.TOP | Gravity.END;
        badge.setLayoutParams(paramsBadge);
        badge.setGravity(Gravity.CENTER);
        badge.setTextSize(9);
        badge.setTextColor(Color.WHITE);
        actualizarBadge(badge, estadoDientes[index]);

        halo.addView(icono);
        halo.addView(badge);

        TextView numero = new TextView(this);
        LinearLayout.LayoutParams paramsNumero = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsNumero.topMargin = dpToPx(6);
        numero.setLayoutParams(paramsNumero);
        numero.setText(numerosDientes[index]);
        numero.setTextSize(11);
        numero.setTypeface(null, Typeface.BOLD);
        numero.setGravity(Gravity.CENTER);
        numero.setTextColor(Color.parseColor("#8A8A8A"));

        TextView etiqueta = new TextView(this);
        etiqueta.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        etiqueta.setText(textoEstado(estadoDientes[index]));
        etiqueta.setTextSize(8.5f);
        etiqueta.setGravity(Gravity.CENTER);
        etiqueta.setTypeface(null, Typeface.BOLD);
        etiqueta.setTextColor(colorTextoEstado(estadoDientes[index]));

        halos[index] = halo;
        iconos[index] = icono;
        etiquetas[index] = etiqueta;
        badges[index] = badge;

        contenedor.addView(halo);
        contenedor.addView(numero);
        contenedor.addView(etiqueta);

        contenedor.setOnClickListener(v -> {
            int anterior = estadoDientes[index];
            int nuevoEstado = (anterior + 1) % 3;
            estadoDientes[index] = nuevoEstado;
            pintar(index);

            icono.setScaleX(0.6f);
            icono.setScaleY(0.6f);
            icono.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(260)
                    .setInterpolator(new OvershootInterpolator(3f))
                    .start();

            guardarEnServidor(index, anterior, nuevoEstado);
        });

        return contenedor;
    }

    private void actualizarBadge(TextView badge, int estado) {
        if (estado == 2) {
            badge.setText("✓");
            GradientDrawable fondoBadge = new GradientDrawable();
            fondoBadge.setShape(GradientDrawable.OVAL);
            fondoBadge.setColor(Color.parseColor("#2E6E9E"));
            fondoBadge.setStroke(dpToPx(1), Color.WHITE);
            badge.setBackground(fondoBadge);
            badge.setVisibility(TextView.VISIBLE);
        } else if (estado == 1) {
            badge.setText("!");
            GradientDrawable fondoBadge = new GradientDrawable();
            fondoBadge.setShape(GradientDrawable.OVAL);
            fondoBadge.setColor(Color.parseColor("#C1443D"));
            fondoBadge.setStroke(dpToPx(1), Color.WHITE);
            badge.setBackground(fondoBadge);
            badge.setVisibility(TextView.VISIBLE);
        } else {
            badge.setVisibility(TextView.GONE);
        }
    }

    private GradientDrawable crearFondoRedondeado(int estado) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setShape(GradientDrawable.OVAL);
        switch (estado) {
            case 1:
                fondo.setColor(Color.parseColor("#EFA9A2"));
                break;
            case 2:
                fondo.setColor(Color.parseColor("#8FBEE0"));
                break;
            default:
                fondo.setColor(Color.parseColor("#7FC8AC"));
        }
        return fondo;
    }

    private GradientDrawable crearHalo(int estado) {
        GradientDrawable halo = new GradientDrawable();
        halo.setShape(GradientDrawable.OVAL);
        switch (estado) {
            case 1:
                halo.setColor(Color.parseColor("#FBEBE9"));
                break;
            case 2:
                halo.setColor(Color.parseColor("#E8F2FA"));
                break;
            default:
                halo.setColor(Color.parseColor("#E7F5EF"));
        }
        return halo;
    }

    private String textoEstado(int estado) {
        switch (estado) {
            case 1: return "Caries";
            case 2: return "Tratado";
            default: return "Sano";
        }
    }

    private int colorTextoEstado(int estado) {
        switch (estado) {
            case 1: return Color.parseColor("#C1443D");
            case 2: return Color.parseColor("#2E6E9E");
            default: return Color.parseColor("#2E8B65");
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}