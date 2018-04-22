package ru.plautskiy.boxweightmonitor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainSceneFXMLController implements Initializable {
    private Label label;
    @FXML
    private TreeView<String> treeModes;
    @FXML
    private Font x1;
    @FXML
    private Font x3;
    @FXML
    private Label lblStatusLeft;
    @FXML
    private Label lblStatusRight;
    @FXML
    private MenuItem menuDataUpdate;
    @FXML
    private Button btnUpdate;
    @FXML
    private MenuItem menuDataExit;
    @FXML
    private MenuItem helpMenuAbout;
    @FXML
    private Label lblDetails;
    @FXML
    private ListView<String> listViewMain;
    @FXML
    private Label lblDetailsA1Weight;
    @FXML
    private Label lblDetailsA2Weight;
    @FXML
    private Label lblDetails1;
    @FXML
    private Font x11;
    @FXML
    private Label lblDetailsA1LastWeight;
    @FXML
    private Label lblDetailsA2LastWeight;
    @FXML
    private MenuItem menuDataPrefs;
            
    private final WeightListsSingleton storage = WeightListsSingleton.INSTANCE;
    private final BoxWeightMonitor appInstance = new BoxWeightMonitor();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        treeModes.setTooltip(new Tooltip("Выберите данные для просмотра"));
        lblDetailsA1Weight.setTooltip(new Tooltip("Усредненное значение веса последних 4 коробов"));
        lblDetailsA2Weight.setTooltip(new Tooltip("Усредненное значение веса последних 4 коробов"));
        lblDetailsA1LastWeight.setTooltip(new Tooltip("Последнее измеренное значение"));
        lblDetailsA2LastWeight.setTooltip(new Tooltip("Последнее измеренное значение"));
        createTreeItems();
        treeModes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            showData(newValue.getValue());
        });
        listViewMain.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                storage.calculate();
                lblDetailsA1Weight.setText(String.valueOf(storage.getLastAvrWeight()[0]));
                lblDetailsA2Weight.setText(String.valueOf(storage.getLastAvrWeight()[1]));
                lblDetailsA1LastWeight.setText(String.valueOf(storage.getLastWeight()[0]));
                lblDetailsA2LastWeight.setText(String.valueOf(storage.getLastWeight()[1]));
            }
        });
        
 }
  
    private void createTreeItems(){
        TreeItem<String> rootItem = new TreeItem<> ("Данные");
        rootItem.setExpanded(true);
        TreeItem<String> casLeaf = new TreeItem<>("Текущий CAS-файл");
        TreeItem<String> scaleMeasuresNode = new TreeItem<>("Текущая сессия");
        TreeItem<String> commonJournalLeaf = new TreeItem<>("Общий журнал");
        TreeItem<String> a1JournalLeaf = new TreeItem<>("Короба А1");
        TreeItem<String> a2JournalLeaf = new TreeItem<>("Короба А2");
        TreeItem<String> archScaleMeasuresNode = new TreeItem<>("Архив");
        TreeItem<String> archCommonJournalLeaf = new TreeItem<>("Общий журнал (арх)");
        TreeItem<String> archA1JournalLeaf = new TreeItem<>("Короба А1 (арх)");
        TreeItem<String> archA2JournalLeaf = new TreeItem<>("Короба А2 (арх)");
        
        rootItem.getChildren().add(casLeaf);
        rootItem.getChildren().add(scaleMeasuresNode);
        rootItem.getChildren().add(archScaleMeasuresNode);
        
        scaleMeasuresNode.getChildren().add(commonJournalLeaf);
        scaleMeasuresNode.getChildren().add(a1JournalLeaf);
        scaleMeasuresNode.getChildren().add(a2JournalLeaf);
        
        archScaleMeasuresNode.getChildren().add(archCommonJournalLeaf);
        archScaleMeasuresNode.getChildren().add(archA1JournalLeaf);
        archScaleMeasuresNode.getChildren().add(archA2JournalLeaf);
        
        treeModes.setRoot(rootItem);
    }
    
    private void showData(String mode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<String>> future;
    
        future = executorService.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> result = new ArrayList<>();
                switch (mode) {
                    case "Текущий CAS-файл":    
                        result = TextFileReader.readFromCurrentCAS();
                        break;
                    case "Общий журнал":
                        result = convListToText(storage.getwEventsCommonLog(),false);
                        break;
                    case "Короба А1":
                        result = convListToText(filterListByType(storage.getwEventsCommonLog(), "A1"),false);
                        break;
                    case "Короба А2":
                        result = convListToText(filterListByType(storage.getwEventsCommonLog(), "A2"),false);
                        break;
                    case "Общий журнал (арх)":
                        result = convListToText(storage.getwEventsArchCommonLog(),true);
                        break;
                    case "Короба А1 (арх)":
                        result = convListToText(filterListByType(storage.getwEventsArchCommonLog(), "A1"),true);
                        break;
                    case "Короба А2 (арх)":
                        result = convListToText(filterListByType(storage.getwEventsArchCommonLog(), "A2"),true);
                        break;
                }
                return result;
            }
        });
        
        Optional<List<String>> optreadLines = Optional.empty();
        try {
            optreadLines = Optional.ofNullable(future.get());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainSceneFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        executorService.shutdownNow();
        ObservableList<String> items = FXCollections.observableList(optreadLines.orElse(Collections.emptyList()));

        listViewMain.setItems(items);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                listViewMain.scrollTo(items.size() - 1);
                listViewMain.getSelectionModel().select(items.size()-1);
                listViewMain.requestFocus();
            }
        });
    }
    
    private static void showDialog(AlertType type, String header, String text){
        Alert alert = new Alert(type);
        alert.setTitle("Box Weight Monitor");
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }
    
    private List<String> convListToText(List<WeightEvent> list, boolean isArchive){
        List<String> result = new ArrayList<>();
        for(WeightEvent we:list){
            if(isArchive){
                result.add(we.toString(true));
            }else{
                result.add(we.toString());
            }
        }
        return result;
    }
    
    private List<WeightEvent> filterListByType(List<WeightEvent> list, String type){
        return list.stream().filter(t->type.equals(t.getBoxType())).collect(Collectors.toList());
    }
   
    public void newWeightEvent(String event){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lblStatusRight.setText("Новое событие от весов: "+event);
                fetchData();
            }
        });
        
    }
    
    @FXML
    public void fetchData(){
        lblStatusLeft.setText("Загрузка файлов из каталога CAS...");
        storage.clearwEventsCommonLog();
        storage.clearwEventsArchCommonLog();
        appInstance.buildLogFromFile(TextFileReader.readFromCurrentCAS(), true);
        appInstance.buildArchLog();
        lblStatusLeft.setText("Данные успешно обновлены");
        treeModes.getSelectionModel().select(0);
        
        storage.calculate();
        
        lblDetailsA1Weight.setText(String.valueOf(storage.getLastAvrWeight()[0]));
        lblDetailsA2Weight.setText(String.valueOf(storage.getLastAvrWeight()[1]));
        lblDetailsA1LastWeight.setText(String.valueOf(storage.getLastWeight()[0]));
        lblDetailsA2LastWeight.setText(String.valueOf(storage.getLastWeight()[1]));    
    }
    
    private void fetchData(ActionEvent event) {
        fetchData();
    }

    @FXML
    private void menuDataExitAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void helpMenuAboutAction(ActionEvent event) {
        showDialog(AlertType.INFORMATION, "Монитор проведенных взвешиваний коробов"+"\n"+"Версия: "+BoxWeightMonitor.VERSION, "\u00a9 2018 Плаутский П.Г. Все права защищены.");
    }
}
