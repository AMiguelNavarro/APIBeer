package com.svalero.beers.controller;

import com.svalero.beers.domain.Beer;
import com.svalero.beers.service.BeersService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.Observable;
import rx.schedulers.Schedulers; // OJO IMPORTAR ESTE SINO DA ERROR

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppController implements Initializable {

    public TableView tvAllBeers;
    public ListView lvBeersFecha,lvFiltroNombre;
    public Button btBuscar, btFiltroNombre, btZIP, btCSV;
    public TextField tfFechaFabricacion, tfFiltroNombre;
    public ComboBox<String> cbFechaFabricacion;
    public Text tvNombre, tvEslogan, tvFechaFabricacion, tvUnit, tvValue;
    public WebView wvImagen;
    public TextArea taDescripcion;

    private ObservableList<Beer> listadoGeneralBeers;
    private ObservableList<String> listadoFechasFabricacion;
    private List<Beer> listadoCompleto;
    private BeersService apiService;
    private Beer cervezaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new BeersService();
        fijarColumnas();

        listadoCompleto = new ArrayList<>();

        listadoGeneralBeers = FXCollections.observableArrayList();
        tvAllBeers.setItems(listadoGeneralBeers);
        getAllBeers();



    }

    @FXML
    private void seleccionarCervezaListadoGeneral(Event event) {
        cervezaSeleccionada = (Beer) tvAllBeers.getSelectionModel().getSelectedItem();
        if (!validarCervezaSeleccionada()) {
            return;
        }

        cargarCervezaSeleccionada(cervezaSeleccionada);
    }

    @FXML
    private void seleccionarCervezaListadoFecha(Event event) {

    }

    @FXML
    private void buscar(Event event) {
        String fechaSeleccionada = cbFechaFabricacion.getSelectionModel().getSelectedItem();
        lvBeersFecha.setItems(listadoCompleto.stream().filter(beer -> beer.getFirstBrewed().contains(fechaSeleccionada)).collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    @FXML
    private void filtrarNombre(Event event) {
        String nombreTextField = tfFiltroNombre.getText();
        // equalsIgnoreCase = que sea igual pero da igual mayuscula que minuscula
        lvFiltroNombre.setItems(listadoCompleto.stream().filter(beer -> beer.getName().contains(nombreTextField)).collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    @FXML
    private void seleccionarBeerFiltroNombre(Event event) {
        cervezaSeleccionada = (Beer) lvFiltroNombre.getSelectionModel().getSelectedItem();
        if (!validarCervezaSeleccionada()) {
            return;
        }

        cargarCervezaSeleccionada(cervezaSeleccionada);
    }

    @FXML
    private void exportarZIP(Event event) {
        File file = export();
        if (file != null) {
            CompletableFuture.runAsync(() -> comprimirZIP(file));
        }
    }

    @FXML
    private void exportarCSV(Event event) {
        File file = export();
        if (file != null) {
            System.out.println("EXPORTADO EN " + file.getAbsolutePath());
        }
    }


    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */


    private File export() {
        File file = null;
        try {
        FileChooser fileChooser = new FileChooser();
        file = fileChooser.showSaveDialog(null);
        FileWriter fileWriter = new FileWriter(file + ".csv");
        CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Nombre", "Grados", "Fecha de fabricación"));

        for (Beer elemento : listadoCompleto) {
            printer.printRecord(
                    elemento.getName(),
                    elemento.getDegrees(),
                    elemento.getFirstBrewed()
            );
        }

        printer.close();

        } catch (IOException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return file;
    }

    private void comprimirZIP(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().concat(".zip"));
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath().concat(".csv"));
            ZipEntry zipEntry = new ZipEntry(file.getName().concat(".csv"));

            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >=0){
                zos.write(bytes, 0, length);
            }
            zos.close();
            fis.close();
            fos.close();

            // Se borra el archivo csv para evitar duplicidades
            Files.delete(Path.of(file.getAbsolutePath().concat(".csv")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean validarCervezaSeleccionada() {
        if (cervezaSeleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("ERROR");
            alerta.setContentText("Debes seleccionar una cerveza");
            alerta.showAndWait();
            return false;
        } else {
            return true;
        }
    }



    private void cargarCervezaSeleccionada(Beer seleccionada) {
        tvNombre.setText(seleccionada.getName());
        tvEslogan.setText(seleccionada.getTagLine());
        tvFechaFabricacion.setText(seleccionada.getFirstBrewed());
        wvImagen.getEngine().load(seleccionada.getImageUrl());
        taDescripcion.setWrapText(true);
        taDescripcion.setText(seleccionada.getDescription());
        tvValue.setText(String.valueOf(seleccionada.getVolume().getValue()));
        tvUnit.setText(seleccionada.getVolume().getUnit());
    }

//    private void getFechasFabricacion() {
//        apiService.getAllBeers()
//                .flatMap(Observable::from)
//                .distinct(Beer::getFirstBrewed)
//                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
//                .subscribe(beer -> {
//                    listadoFechasFabricacion.add(beer.getFirstBrewed());
//                });
//
//        for(String fecha : listadoFechasFabricacion) {
//            System.out.println(fecha);
//        }
//    }

    /**
     * Traer todas las cervezas de la API
     */
    private void getAllBeers() {
        tvAllBeers.getItems().clear();
        listadoGeneralBeers.clear();

        apiService.getAllBeers()
                .flatMap(Observable::from)
                .doOnCompleted(() -> {
                    System.out.println("FIN");
//                    listadoFechasFabricacion = listadoCompleto.stream().map(Beer::getFirstBrewed).collect(Collectors.toCollection(FXCollections::observableArrayList)); SE PUEDE HACER DIRECTAMENTE SIN CREAR OBSERVABLE LIST
                    cbFechaFabricacion.setItems(listadoCompleto.stream()
                            .map(beer -> beer.getFirstBrewed().substring(3,7))
                            .sorted()
                            .distinct()
                            .collect(Collectors.toCollection(FXCollections::observableArrayList))); // TRansforma lista a observable LIST
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(beer -> {
                    listadoGeneralBeers.add(beer);
                    listadoCompleto.add(beer);
                });

    }

    /**
     * Fija las columnas del Table View
     */
    private void fijarColumnas() {
        Field[] columnas = Beer.class.getDeclaredFields();

        for(Field columna : columnas) {
            if (columna.getName().equals("id") || columna.getName().equals("description") || columna.getName().equals("imageUrl") || columna.getName().equals("volume") || columna.getName().equals("tagLine")) {
                continue;
            }

            TableColumn<Beer, String> col = new TableColumn<>(columna.getName());
            col.setCellValueFactory(new PropertyValueFactory<>(columna.getName()));
            tvAllBeers.getColumns().add(col);
        }

        tvAllBeers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }



}
