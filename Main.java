import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import java.util.ArrayList;

public class Main extends Application {
    // Textfield constraints
    public static void digitsTxtFld(TextField field) {
        field.setTextFormatter(new TextFormatter<Integer>(change -> {
            String newText = change.getControlNewText();

            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    @Override
    public void start(Stage stage) {
        //From port
        Label lab1=new Label("From port");
        ComboBox<String> combo = new ComboBox<>();
        MyDB db = new MyDB();
        ArrayList<String> harbourNames = db.query("select name from harbour;","name");

        for (String name: harbourNames) {
            combo.getItems().add(name);
        }
        //To port
        Label lab2=new Label("To port");
        ComboBox<String> combo1 = new ComboBox<>();

        ArrayList<String> Destinations = db.query("select name from harbour;","name");
        for (String name: Destinations)
            combo1.getItems().add(name);

        //Textfield
        Label lab3=new Label("Number of containers");
        TextField fld = new TextField();
        digitsTxtFld(fld);

        //Buttons
        Button srch = new Button("Search");
        Button update = new Button("Update");

        //Text output
        TextArea res = new TextArea();

        //Search button
        srch.setOnAction(e ->{
            update.setOnAction(o ->{ res.setText("Not active");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
            });

            if (combo.getValue() == null && combo1.getValue() == null){
                res.setText("Please fill both ports");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            if (combo.getValue() != null && combo1.getValue() == null){
                res.setText("Please fill both ports");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            if (combo.getValue() == null && combo1.getValue() != null){
                res.setText("Please fill both ports");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }

            if (combo.getValue().equals(combo1.getValue())) {
                res.setText("Source and destination ports must be different");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            if (fld.getText() == ("")){
                res.setText("Number must be filled");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            int numb = Integer.parseInt(fld.getText());
            if (numb <= 0 ){
                res.setText("Input must be greater than 0");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            if (numb >=100000){
                res.setText("Input can't be higher than 99999");
                res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                return;
            }
            //Reset colour
            res.setStyle("-fx-text-fill: rgb(0,0,0);");

            // Query for available vessels
            ArrayList<String> vesselNames = db.query("select v.name as vessel, capacity from transport t " +
                    "inner join vessel v on t.vessel = v.id " +
                    "inner join harbour h1 on t.fromharbour = h1.id " +
                    "inner join harbour h2 on t.toharbour = h2.id " +
                    "left outer join flow f on t.id = f.transport " +
                    "where h1.name = '"+combo.getValue()+"' and h2.name = '"+combo1.getValue() +
                    "' group by t.id " +
                    "having " + numb + " <= v.capacity;","vessel");

            for (String name2: vesselNames) {
                res.setText(name2 + "\n" + ("There is " +vesselNames.size() + " available vessel to send " +numb+ " containers to " + combo1.getValue()));
            }
            if (vesselNames.isEmpty()) {
                res.setText("There is no available vessel for " + numb + " containers" + " between " + combo.getValue() + " and " + combo1.getValue());
                return;
            }

            //Query for vessel IDs
            ArrayList<String> vesselIds = db.query("select vessel.id from vessel" +
                    " left outer join transport t on vessel.id = t.vessel" +
                    " group by vessel.id" +
                    " having vessel.name = '" + vesselNames.get(0) + "';", "id");

            //Button for updating database
            update.setOnAction(o ->{
            putContainersOnShip(vesselIds,db,numb);
            res.setText(vesselNames.get(0) + " has been updated with " +numb+" containers from " +combo.getValue()+ " to " +combo1.getValue());

            //Setting update button inactive
            update.setOnAction(o1 ->{ res.setText("Not active");
            res.setStyle(" -fx-text-fill: rgb(255,0,0);");
                });
            });
        });
        //User view - Interface design
        GridPane pane1 = new GridPane();
        BorderPane root=new BorderPane();
        lab1.setPrefSize(150,30);
        lab2.setPrefSize(150,30);
        lab3.setPrefSize(150,30);
        srch.setStyle("-fx-font: 22 arial; -fx-base: rgb(0,250,0);"+
                        " -fx-text-fill: rgb(255,255,255);");
        res.setStyle( "-fx-font: 16 arial;");
        pane1.add(lab1,1,1);
        pane1.add(combo,1,2);
        pane1.add(lab2,2,1);
        pane1.add(combo1,2,2);
        pane1.add(lab3,3,1);
        pane1.add(fld,3,2);
        root.setTop(pane1);
        root.setCenter(srch);
        root.setBottom(res);
        root.setLeft(update);
        Scene scene = new Scene(root, 500, 500, Color.BLUE);
        stage.setTitle("Containers");
        stage.setScene(scene);
        stage.show();
    }
    //method for updating
    public void putContainersOnShip(ArrayList <String> vesselIds, MyDB db, int containers){
        if(!vesselIds.isEmpty()){
            db.cmd("insert into flow(transport, containers) values ("+vesselIds.get(0)+","+containers+");");
            db.cmd("update vessel set capacity = capacity -"+containers+" where vessel.id ="+vesselIds.get(0)+";");
        }
    }
    //Launching the program
    public static void main(String[] args) {
        launch(args);
    }
}