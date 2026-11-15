package cl.duoc.viviendas.repository;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ViviendaRepository {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path casas = Path.of("casas.json");
    private final Path departamentos = Path.of("departamentos.json");

    public List<Vivienda> cargar() throws IOException {
        List<Vivienda> resultado = new ArrayList<>();
        resultado.addAll(leerCasas());
        resultado.addAll(leerDepartamentos());
        return resultado;
    }

    private List<Casa> leerCasas() throws IOException {
        if (Files.notExists(casas)) return new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(casas, StandardCharsets.UTF_8)) {
            List<Casa> lista = gson.fromJson(reader, new TypeToken<List<Casa>>() {}.getType());
            return lista == null ? new ArrayList<>() : lista;
        } catch (RuntimeException e) {
            throw new IOException("No se pudo leer casas.json: " + e.getMessage(), e);
        }
    }

    private List<Departamento> leerDepartamentos() throws IOException {
        if (Files.notExists(departamentos)) return new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(departamentos, StandardCharsets.UTF_8)) {
            List<Departamento> lista = gson.fromJson(reader, new TypeToken<List<Departamento>>() {}.getType());
            return lista == null ? new ArrayList<>() : lista;
        } catch (RuntimeException e) {
            throw new IOException("No se pudo leer departamentos.json: " + e.getMessage(), e);
        }
    }

    public void guardar(List<Vivienda> viviendas) throws IOException {
        List<Casa> listaCasas = new ArrayList<>();
        List<Departamento> listaDepartamentos = new ArrayList<>();
        for (Vivienda vivienda : viviendas) {
            if (vivienda instanceof Casa casa) listaCasas.add(casa);
            else if (vivienda instanceof Departamento departamento) listaDepartamentos.add(departamento);
        }
        escribir(casas, listaCasas);
        escribir(departamentos, listaDepartamentos);
    }

    private void escribir(Path archivo, Object datos) throws IOException {
        try (Writer writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            gson.toJson(datos, writer);
        }
    }
}
