package cl.duoc.viviendas.controller;

import javafx.fxml.FXML;
import cl.duoc.viviendas.repository.ViviendaRepository;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.ReadOnlyStringWrapper;

public class MainController {
    private final ViviendaRepository repository = new ViviendaRepository();
    private final ObservableList<Vivienda> viviendas =
            FXCollections.observableArrayList();
    @FXML
    private TableView<Vivienda> tablaViviendas;
    @FXML
    private TableColumn<Vivienda, String> colTipo;

    @FXML
    private TableColumn<Vivienda, String> colCodigo;

    @FXML
    private TableColumn<Vivienda, String> colSuperficie;

    @FXML
    private TableColumn<Vivienda, String> colHabitaciones;

    @FXML
    private TableColumn<Vivienda, String> colCosto;
    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtSuperficie;

    @FXML
    private TextField txtHabitaciones;

    @FXML
    private Button btnRegistrar;

    @FXML
    private Label lblMensaje;



    @FXML
    private TextField txtPiso;

    @FXML
    private CheckBox chkGastoComun;

    @FXML
    private CheckBox chkEstacionamiento;

    @FXML
    private CheckBox chkPatio;

    @FXML
    public void initialize() {

        cmbTipo.getItems().addAll("Departamento", "Casa");

        txtPiso.setDisable(true);
        chkGastoComun.setDisable(true);
        chkEstacionamiento.setDisable(true);
        chkPatio.setDisable(true);

        cmbTipo.setOnAction(event -> cambiarTipo());
        tablaViviendas.setItems(viviendas);
        colTipo.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(dato.getValue().getTipo()));

        colCodigo.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(dato.getValue().getCodigo()));

        colSuperficie.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        String.valueOf(dato.getValue().getSuperficie())));

        colHabitaciones.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        String.valueOf(dato.getValue().getHabitaciones())));

        colCosto.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        String.format("$%,.0f",
                                dato.getValue().calcularCostoArriendo())));
        try {
            viviendas.setAll(repository.cargar());
        } catch (IOException e) {
            lblMensaje.setText("Error al leer los archivos: " + e.getMessage());
        }
    }

    private void cambiarTipo() {

        String tipo = cmbTipo.getValue();

        boolean departamento = "Departamento".equals(tipo);
        boolean casa = "Casa".equals(tipo);

        txtPiso.setDisable(!departamento);
        chkGastoComun.setDisable(!departamento);
        chkEstacionamiento.setDisable(!departamento);

        chkPatio.setDisable(!casa);
    }
    @FXML
    private void registrarVivienda() {

        String tipo = cmbTipo.getValue();

        if (tipo == null) {
            lblMensaje.setText("Debes seleccionar un tipo de vivienda.");
            return;
        }

        String codigo = txtCodigo.getText().trim();

        if (codigo.isEmpty()) {
            lblMensaje.setText("El código de propiedad no puede estar vacío.");
            return;
        }

        try {
            double superficie = Double.parseDouble(txtSuperficie.getText().trim());

            if (!Double.isFinite(superficie) || superficie < 20 || superficie > 500) {
                lblMensaje.setText("La superficie debe estar entre 20 y 500 m².");
                return;
            }

            int habitaciones = Integer.parseInt(txtHabitaciones.getText().trim());

            if (habitaciones <= 0) {
                lblMensaje.setText("Las habitaciones deben ser mayores que cero.");
                return;
            }

            if (tipo.equals("Departamento")) {

                int piso = Integer.parseInt(txtPiso.getText().trim());

                if (piso <= 0) {
                    lblMensaje.setText("El piso debe ser mayor que cero.");
                    return;
                }
            }

            Vivienda nuevaVivienda;

            if (tipo.equals("Casa")) {

                nuevaVivienda = new Casa(
                        codigo,
                        superficie,
                        habitaciones,
                        chkPatio.isSelected()
                );

            } else {

                int piso = Integer.parseInt(txtPiso.getText().trim());

                Departamento departamento = new Departamento(
                        codigo,
                        superficie,
                        habitaciones,
                        piso,
                        chkGastoComun.isSelected()
                );

                if (chkEstacionamiento.isSelected()) {
                    departamento.asignarEstacionamiento();
                }

                nuevaVivienda = departamento;
            }

            viviendas.add(nuevaVivienda);
            try {
                repository.guardar(new ArrayList<>(viviendas));
            } catch (IOException e) {
                viviendas.remove(nuevaVivienda);
                lblMensaje.setText("Error al guardar los archivos: " + e.getMessage());
                return;
            }

            lblMensaje.setText(
                    codigo + " (" + tipo + ") registrada correctamente."
            );
            cmbTipo.getSelectionModel().clearSelection();

            txtCodigo.clear();
            txtSuperficie.clear();
            txtHabitaciones.clear();
            txtPiso.clear();

            chkGastoComun.setSelected(false);
            chkEstacionamiento.setSelected(false);
            chkPatio.setSelected(false);

            cambiarTipo();

        } catch (NumberFormatException e) {
            lblMensaje.setText("Revisa los datos numéricos: superficie, habitaciones o piso.");
        }

    }



@FXML
private void eliminarVivienda() {

    Vivienda seleccionada =
            tablaViviendas.getSelectionModel().getSelectedItem();

    if (seleccionada == null) {
        lblMensaje.setText("Debes seleccionar una vivienda para eliminar.");
        return;
    }

    int indice = viviendas.indexOf(seleccionada);
    viviendas.remove(seleccionada);
    try {
        repository.guardar(new ArrayList<>(viviendas));
    } catch (IOException e) {
        viviendas.add(indice, seleccionada);
        lblMensaje.setText("Error al guardar los archivos: " + e.getMessage());
        return;
    }

    lblMensaje.setText(
            seleccionada.getCodigo() + " eliminada correctamente."
    );
}


}

