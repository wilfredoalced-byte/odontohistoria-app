package com.example.odontohistoriaia.data.remote;

import com.example.odontohistoriaia.model.Cita;
import com.example.odontohistoriaia.model.EstadoPieza;
import com.example.odontohistoriaia.model.PacienteRemoto;
import com.example.odontohistoriaia.model.PiezaOdontograma;
import com.example.odontohistoriaia.model.Tratamiento;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ---------- Citas ----------
    @GET("citas")
    Call<List<Cita>> obtenerCitas();

    @POST("citas")
    Call<Cita> crearCita(@Body Cita cita);

    // ---------- Pacientes ----------
    @GET("pacientes")
    Call<List<PacienteRemoto>> obtenerPacientes();

    @GET("pacientes/{id}")
    Call<PacienteRemoto> obtenerPaciente(@Path("id") int id);

    @POST("pacientes")
    Call<PacienteRemoto> crearPaciente(@Body PacienteRemoto paciente);

    @PUT("pacientes/{id}")
    Call<PacienteRemoto> actualizarPaciente(@Path("id") int id, @Body PacienteRemoto paciente);

    // ---------- Odontograma ----------
    @GET("pacientes/{id}/odontograma")
    Call<List<PiezaOdontograma>> obtenerOdontograma(@Path("id") int pacienteId);

    @PUT("pacientes/{id}/odontograma/{pieza}")
    Call<PiezaOdontograma> marcarPieza(@Path("id") int pacienteId,
                                       @Path("pieza") int pieza,
                                       @Body EstadoPieza estado);

    @DELETE("pacientes/{id}/odontograma/{pieza}")
    Call<PiezaOdontograma> quitarPieza(@Path("id") int pacienteId, @Path("pieza") int pieza);

    // ---------- Tratamientos (historial) ----------
    @GET("tratamientos")
    Call<List<Tratamiento>> obtenerTratamientos(@Query("pacienteId") int pacienteId);
}
