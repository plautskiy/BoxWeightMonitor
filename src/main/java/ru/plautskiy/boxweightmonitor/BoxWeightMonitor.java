package ru.plautskiy.boxweightmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class BoxWeightMonitor extends Application {
    final static String SETTINGSFILE = "settings.conf";
    public final static String VERSION = "1.21";
    public static Properties appProperties = new Properties();
    private static MainSceneFXMLController controller;
    public final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScene.fxml"));
    
    private WatchService watchService;
    private WatchEventsReader watcher;
    public ExecutorService executor;
    public Future<?> future;

    @Override
    public void start(Stage stage) {
        try {
            readSettingFromFile(SETTINGSFILE);
        } catch (IOException ex) {
            Logger.getLogger(BoxWeightMonitor.class.getName()).log(Level.SEVERE, null, ex);
            showDialog(Alert.AlertType.ERROR,"Ошибка","Файл настроек setting.conf не найден или поврежен. Завершение программы.");
            System.exit(0);
        }
        buildLogFromFile(TextFileReader.readFromCurrentCAS(), true);
        buildArchLog();
        
        try {
            runWatcherEvents(stage);
        } catch (InterruptedException ex) {
            Logger.getLogger(BoxWeightMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void stop() {
        stopWatcher();
        try {
            super.stop();
        } catch (Exception ex) {
            Logger.getLogger(BoxWeightMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showScene(Stage stage){
        Parent root = null;
        try {
            root = (Parent) loader.load();
        } catch (IOException ex) {
            showDialog(Alert.AlertType.ERROR, "Ошибка", "не удалось загрузить fxml файл корневого окна. Завершение программы.");
            stopWatcher();
            System.exit(0);
        }
        controller = (MainSceneFXMLController)loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("Box Weight Monitor "+VERSION);
        stage.getIcons().add(new Image(getClass().getResource("/img/scale.png").toExternalForm()));
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main()
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private void runWatcherEvents(Stage stage) throws InterruptedException{
        Path dir = Paths.get(BoxWeightMonitor.appProperties.getProperty("raw-path"));
        watcher = new WatchEventsReader(dir);
        executor = Executors.newCachedThreadPool();
        future = executor.submit(watcher);
        executor.shutdown();
        
        showScene(stage); 
    }
    
    public void stopWatcher(){
        future.cancel(true);
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainSceneFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        executor.shutdownNow();
    }
    
    private static void readSettingFromFile(String fromFile) throws FileNotFoundException, IOException{
        try (FileInputStream file = new FileInputStream(fromFile)) {
            appProperties.load(file);
        }
    }
    
    public void buildLogFromFile(List<String> rawList, boolean isCurrentLog){
        String[] tokens = null;
        if(!rawList.isEmpty()){
            for(String str:rawList){
                String strmod= str.replace("\"", "");
                tokens=strmod.split(",");
                if (tokens.length<3){
                    showDialog(Alert.AlertType.ERROR,"Ошибка","Не корректный формат CAS-файла . Завершение программы.");
                    System.exit(0);
                }
                if(isGoodEvent(tokens[2], tokens[3])){
                    if(isCurrentLog){
                        WeightListsSingleton.INSTANCE.addEventCommonLog(new WeightEvent(tokens[0], tokens[1], getBoxType(tokens[3]), Double.valueOf(tokens[3])/10));
                    }else{
                        WeightListsSingleton.INSTANCE.addEventArchCommonLog(new WeightEvent(tokens[0], tokens[1], getBoxType(tokens[3]), Double.valueOf(tokens[3])/10,true));
                    }
                }
            }
        }else{
            showDialog(Alert.AlertType.ERROR,"Ошибка","Не удалось загрузить журнал взвешиваний. Завершение программы.");
            System.exit(0);
        }
    }
    
    public void buildArchLog(){
        File currentCasFile = TextFileReader.findLastCASFile();
        Path dir = Paths.get(BoxWeightMonitor.appProperties.getProperty("raw-path"));

        Optional<Path> FilePath = Optional.empty();
        List<File> filesInFolder = new ArrayList<>();
        try {
            filesInFolder = Files.walk(dir)
                    .filter(f -> !Files.isDirectory(f) && f.toFile().toString().endsWith(".txt"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            showDialog(Alert.AlertType.ERROR,"Ошибка","Не удалось загрузить файлы взвешиваний. Завершение программы.");
            System.exit(0);
        }
        if(filesInFolder.size()>1){
            List<File> filesInFolderNoCurrent = filesInFolder.stream().filter(f->!f.equals(currentCasFile)).collect(Collectors.toList());
            filesInFolderNoCurrent.stream().forEach(f->{
                buildLogFromFile(TextFileReader.read(f), false);
            });
        }else{
            filesInFolder.stream().forEach(f->{
                buildLogFromFile(TextFileReader.read(f), false);
            });
        }
    }
    
    private boolean isGoodEvent(String flag, String weight){
        double w = Double.valueOf(weight);
        double thr = Double.valueOf(appProperties.getProperty("weight-threshold", "100"));
        return "St".equals(flag) && !"0".equals(weight) && w > thr;
    }
    
    private String getBoxType(String weight){
        double limit = Double.valueOf(appProperties.getProperty("a1box-weight-limit", "240"));
        return (limit<Double.valueOf(weight)/10)? "A2":"A1";
    }
    
    private static void showDialog(Alert.AlertType type, String header, String text){
        Alert alert = new Alert(type);
        alert.setTitle(header);
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private class WatchEventsReader implements Runnable {
        private WatchService watcher= null;
        private Path casPath=null;
        private WatchKey keyWatch;

        @SuppressWarnings("unchecked")
        <T> WatchEvent<T> cast(WatchEvent<?> event) {
            return (WatchEvent<T>) event;
        }

        public WatchEventsReader(Path path) {
            try {
                this.watcher = FileSystems.getDefault().newWatchService();
                this.casPath=path;
                this.keyWatch = casPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException ex) {
                Logger.getLogger(BoxWeightMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
        try {
            for (;;) {
                // wait for key to be signalled
                WatchKey key = watcher.take();
                if (this.keyWatch != key) {
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> ev = cast(event);
                    //System.out.format("%s: %s\n", ev.kind(), casPath.resolve(ev.context()));
                    controller.newWeightEvent(ev.kind().toString()+ " "+casPath.resolve(ev.context()));
                }
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException x) {
            return;
        }
        }
    }
}
