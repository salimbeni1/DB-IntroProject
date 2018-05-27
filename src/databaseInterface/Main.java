package databaseInterface;

import javafx.application.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;

public class Main extends Application {

	//TABLE VIEW AND DATA AND SQL PREDEFINED QUEERIES
    private ObservableList<ObservableList> data;
    private TableView tableview;
    
    private String q0 = "SELECT cl.CLIPTITLE, rt.RUNNINGTIME " + 
      		"FROM CLIPS cl, RELEASEDDATES rl, RUNNINGTIMES rt " + 
      		"where rl.RELEASECOUNTRY = 'France' AND rl.CLIPID = cl.CLIPID AND rt.CLIPID = cl.CLIPID and ROWNUM <= 10 " + 
      		"ORDER BY rt.RUNNINGTIME DESC";
    
    private String q1 = "SELECT S.* FROM ACTED S WHERE S.ACTEDID = '2' OR S.actedid = 10 OR S.actedid = 30";
    
    private String q2 = "SELECT S.* FROM ACTED S WHERE S.ACTEDID = '2'";
    
    private String querries[] = {q0, q1, q2};

    //MAIN EXECUTOR
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    
    
    
  //CONNECTION DATABASE
    public void buildData(String sql){
          
          data = FXCollections.observableArrayList();
          try{
        	  System.out.println("	- INTRO TO DATABSE -");
        	  Class.forName("oracle.jdbc.driver.OracleDriver");
        	  System.out.print("Database Connection : ");
        	  Connection con = DriverManager.getConnection(
  					"jdbc:oracle:thin:@//diassrv2.epfl.ch:1521/orcldias.epfl.ch",
  					"DB2018_G35",
  					"DB2018_G35");
        	  System.out.println("OK");
        	  
        	  
            
            //ResultSet
            ResultSet rs = con.createStatement().executeQuery(sql);

            /**********************************
             * TABLE COLUMN ADDED DYNAMICALLY *
             **********************************/
            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                //We are using non property style for making dynamic table
                final int j = i;                
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){                    
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {                                                                                              
                        return new SimpleStringProperty(param.getValue().get(j).toString());                        
                    }                    
                });

                tableview.getColumns().addAll(col);
            }

            /********************************
             * Data added to ObservableList *
             ********************************/
            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);

            }
            
            //FINALLY ADDED TO TableView
            tableview.setItems(data);
          }catch(Exception e){
              System.out.println("Error : "+e.getMessage());             
          }
      }
          
    
    

	@SuppressWarnings("unchecked")
	@Override
    public void start(Stage mainStage) throws Exception {
		
		mainStage.setTitle("DB-P Team35");
		
		ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(
			    "A", "B", "C")
			);
		
		
        
        cb.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                Stage resultStage = new Stage();
                resultStage.setTitle("Result");
                resultStage.setX(mainStage.getX()+mainStage.getWidth());
                //TableView
                tableview = new TableView();
                System.out.println(cb.getSelectionModel().getSelectedIndex());
                buildData(querries[cb.getSelectionModel().getSelectedIndex()]);

                //Main Scene
                Scene scene = new Scene(tableview);        

                resultStage.setScene(scene);
                resultStage.show();
            }
        });
		
        GridPane grid = new GridPane();
        grid.add(new Label("Predefined Querries : "), 0, 0, 4 , 1);
        grid.add(cb, 0, 1);
        
        /*
        grid.add(new Label("Personalised Querries :"), 0, 3,4,1);
        grid.add(new Label(" FROM : "), 0, 4);
        grid.add(new ChoiceBox(), 1, 4);
        grid.add(new Label(" WITH : "), 2, 4);
        grid.add(new ChoiceBox(), 3, 4);*/
        
        
        grid.add(new Label("Insert or delete :"), 0, 5,4,1);
        grid.add(new Label(" TABLE: "), 0, 6);
        grid.add(new ChoiceBox(), 1, 6);
        grid.add(new Label(" WITH : "), 2, 6);
        grid.add(new ChoiceBox(), 3, 6);
        
        grid.setHgap(3); 
        grid.setVgap(10); 
        grid.setPadding(new Insets(10, 10, 10, 10));
        
	    mainStage.setScene(new Scene(grid));
	    mainStage.show();
      
    }

}
