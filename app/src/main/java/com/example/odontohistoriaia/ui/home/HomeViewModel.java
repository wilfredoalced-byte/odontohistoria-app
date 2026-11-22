package com.example.odontohistoriaia.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.odontohistoriaia.data.local.AppDatabase;
import com.example.odontohistoriaia.data.local.DatabaseProvider;
import com.example.odontohistoriaia.data.remote.ApiService;
import com.example.odontohistoriaia.data.remote.RetrofitClient;
import com.example.odontohistoriaia.model.Cita;
import com.example.odontohistoriaia.model.Paciente;
import com.example.odontohistoriaia.model.PacienteRemoto;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Paciente> paciente = new MutableLiveData<>();
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<Paciente> getPaciente() {
        return paciente;
    }

    // Avisos para mostrar al usuario (por ejemplo, "sin conexión con el servidor")
    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public void limpiarMensaje() {
        mensaje.setValue(null);
    }

    // READ (SQLite)
    public void cargarPaciente(AppDatabase db) {
        DatabaseProvider.databaseExecutor.execute(() -> {
            Paciente p = db.pacienteDao().obtenerPrimero();
            paciente.postValue(p);
        });
    }

    // UPDATE (SQLite)
    public void actualizarPaciente(AppDatabase db, Paciente p) {
        DatabaseProvider.databaseExecutor.execute(() -> {
            db.pacienteDao().actualizar(p);
            paciente.postValue(p);
        });
    }

    // DELETE (SQLite)
    public void eliminarPaciente(AppDatabase db, Paciente p, Runnable alTerminar) {
        DatabaseProvider.databaseExecutor.execute(() -> {
            db.pacienteDao().eliminar(p);
            alTerminar.run();
        });
    }

    // Sincroniza el paciente del teléfono con el backend:
    //  - si ya tiene remoteId, trae sus datos actuales del servidor;
    //  - si no, lo busca por nombre y, si no existe, lo crea.
    // El servidor manda: lo que cambie la clínica desde el panel aparece en la app.
    public void sincronizarConServidor(AppDatabase db) {
        RetrofitClient.redExecutor.execute(() -> {
            try {
                Paciente local = db.pacienteDao().obtenerPrimero();
                if (local == null) return;
                ApiService api = RetrofitClient.getApiService();

                PacienteRemoto remoto = null;
                if (local.remoteId > 0) {
                    Response<PacienteRemoto> r = api.obtenerPaciente(local.remoteId).execute();
                    if (r.isSuccessful()) {
                        remoto = r.body();
                    } else if (r.code() != 404) {
                        mensaje.postValue("El servidor no respondió bien. Se muestran los datos guardados en el teléfono.");
                        return;
                    }
                }

                if (remoto == null) {
                    Response<List<PacienteRemoto>> lista = api.obtenerPacientes().execute();
                    if (!lista.isSuccessful() || lista.body() == null) {
                        mensaje.postValue("El servidor no respondió bien. Se muestran los datos guardados en el teléfono.");
                        return;
                    }
                    for (PacienteRemoto p : lista.body()) {
                        if (p.nombre != null && p.nombre.equalsIgnoreCase(local.nombre)) {
                            remoto = p;
                            break;
                        }
                    }
                }

                if (remoto == null) {
                    PacienteRemoto nuevo = new PacienteRemoto(local.nombre, local.motivoConsulta, local.proximaCita);
                    Response<PacienteRemoto> creado = api.crearPaciente(nuevo).execute();
                    if (!creado.isSuccessful() || creado.body() == null) {
                        mensaje.postValue("No se pudo registrar el paciente en el servidor.");
                        return;
                    }
                    remoto = creado.body();
                }

                local.remoteId = remoto.id;
                if (remoto.nombre != null) local.nombre = remoto.nombre;
                if (remoto.motivoConsulta != null) local.motivoConsulta = remoto.motivoConsulta;
                if (remoto.proximaCita != null) local.proximaCita = remoto.proximaCita;
                db.pacienteDao().actualizar(local);
                paciente.postValue(local);
            } catch (IOException e) {
                mensaje.postValue("Sin conexión con el servidor (" + e.getClass().getSimpleName() + ": " + e.getMessage() + "). Se muestran los datos guardados en el teléfono.");
            }
        });
    }

    // PUT (REST): guarda la próxima cita en el paciente del servidor
    public void actualizarPacienteEnServidor(Paciente p) {
        if (p.remoteId <= 0) {
            mensaje.postValue("El cambio se guardó solo en el teléfono: todavía no hay conexión con el servidor.");
            return;
        }
        PacienteRemoto cuerpo = new PacienteRemoto(p.nombre, p.motivoConsulta, p.proximaCita);
        RetrofitClient.getApiService().actualizarPaciente(p.remoteId, cuerpo).enqueue(new Callback<PacienteRemoto>() {
            @Override
            public void onResponse(Call<PacienteRemoto> call, Response<PacienteRemoto> response) {
                if (!response.isSuccessful()) {
                    mensaje.postValue("No se pudo guardar el cambio en el servidor.");
                }
            }

            @Override
            public void onFailure(Call<PacienteRemoto> call, Throwable t) {
                mensaje.postValue("Sin conexión: el cambio se guardó solo en el teléfono.");
            }
        });
    }

    // POST (REST)
    public void enviarCitaAlServidor(Cita cita) {
        RetrofitClient.getApiService().crearCita(cita).enqueue(new Callback<Cita>() {
            @Override
            public void onResponse(Call<Cita> call, Response<Cita> response) { }

            @Override
            public void onFailure(Call<Cita> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
