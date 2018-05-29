package databaseInterface;

import javafx.application.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.sql.*;

public class Main extends Application {

	//TABLE VIEW AND DATA AND SQL PREDEFINED QUEERIES
    private ObservableList<ObservableList> data;
    private TableView tableview;
    
    private String qa = "SELECT cl.CLIPTITLE, rt.RUNNINGTIME " + 
    		"FROM  CLIPS cl, RUNNINGTIMES rt " + 
    		"where rt.RELEASECOUNTRY = 'France' AND rt.CLIPID = cl.CLIPID " + 
    		"ORDER BY rt.RUNNINGTIME DESC " + 
    		"FETCH FIRST 10 ROWS ONLY ";
    
    private String qb = "SELECT COUNT(*) " + 
    		"FROM RELEASEDDATES rl " + 
    		"WHERE EXTRACT(YEAR FROM rl.RELEASEDATE) = 2001 " + 
    		"GROUP BY rl.RELEASECOUNTRY ";
    
    private String qc = "SELECT COUNT(*), gr.GENRE " + 
    		"FROM RELEASEDDATES rl, GENRES gr " + 
    		"WHERE rl.RELEASECOUNTRY = 'USA' and EXTRACT(YEAR FROM rl.RELEASEDATE) >= 2013 and gr.CLIPID = rl.CLIPID " + 
    		"GROUP BY gr.GENRE ";
    
    private String qd = "WITH TEMP as (SELECT ac.PERSONID, COUNT(DISTINCT(ac.CLIPID)) as CNT " + 
    		"              FROM ACTED ac " + 
    		"              GROUP BY ac.PERSONID " + 
    		"              ORDER BY CNT DESC " + 
    		"              FETCH FIRST 1 ROW ONLY) " + 
    		"SELECT pp.FULLNAME, tmp.CNT " + 
    		"FROM TEMP tmp, PEOPLE pp " + 
    		"WHERE pp.PERSONID = tmp.PERSONID ";
    
    private String qe = "SELECT MAX(COUNT(DISTINCT(di.CLIPID))) AS CNT " + 
    		"FROM DIRECTED di " + 
    		"GROUP BY di.PERSONID ";
    
    private String qf = "WITH TEMP AS (SELECT uni.PERSONID, uni.CLIPID, COUNT(uni.PERSONID) AS CNT " + 
    		"              FROM (SELECT DISTINCT PERSONID, CLIPID FROM PRODUCED " + 
    		"                    UNION ALL " + 
    		"                    SELECT DISTINCT PERSONID, CLIPID FROM WROTE " + 
    		"                    UNION ALL " + 
    		"                    SELECT DISTINCT PERSONID, CLIPID FROM ACTED " + 
    		"                    UNION ALL" + 
    		"                    SELECT DISTINCT PERSONID, CLIPID FROM DIRECTED) uni " + 
    		"              GROUP BY uni.PERSONID, uni.CLIPID)" + 
    		"SELECT DISTINCT(pp.FULLNAME) " + 
    		"FROM TEMP tmp, PEOPLE pp " + 
    		"WHERE pp.PERSONID = tmp.PERSONID AND tmp.CNT >= 2 " + 
    		"FETCH FIRST 10 ROWS ONLY ";
    
    private String qg = "SELECT ln.LANGUAGE, COUNT(ln.LANGUAGE) " + 
    		"FROM LANGUAGES ln " + 
    		"GROUP BY ln.LANGUAGE " + 
    		"ORDER BY COUNT(ln.LANGUAGE) DESC " + 
    		"FETCH FIRST 10 ROWS ONLY ";
    
    private String qh = "WITH TEMP AS (SELECT ac.PERSONID, COUNT(DISTINCT(ac.CLIPID)) as CNT " + 
    		"              FROM ACTED ac, CLIPS cl " + 
    		"              WHERE ac.CLIPID = cl.CLIPID AND cl.CLIPTYPE = 'TV' " + 
    		"              GROUP BY ac.PERSONID " + 
    		"              ORDER BY CNT DESC " + 
    		"              FETCH FIRST 1 ROW ONLY) " + 
    		"SELECT pp.FULLNAME, tmp.CNT " + 
    		"FROM TEMP tmp, PEOPLE pp " + 
    		"WHERE tmp.PERSONID = pp.PERSONID ";
    
    
    private String querries[] = {qa, qb, qc, qd, qe, qf , qg , qh};

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
                    @Override
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
		
		
		
		// allow to close all windows from the main window
		mainStage.setOnCloseRequest((new EventHandler<WindowEvent>(){

	        @Override
	        public void handle(WindowEvent arg0) {
	        	Platform.exit();
	        }
		}));
		
		
		
		
		
		
		mainStage.setTitle("DB-P Team35");
		GridPane grid = new GridPane();
		
		try {
		Image emoji = new Image(getClass().getResourceAsStream("pervertedEmoji.png"));
		
		
		Label intro = new Label("  this is our wonderful GUI");
		ImageView ivIntro = new ImageView(emoji);
		ivIntro.setFitWidth(30);ivIntro.setFitHeight(30);
		intro.setGraphic(ivIntro);
		
		grid.add(intro, 0, 1);
		Text intro2 = new Text(" - first make sure u r conneted\n to our EPFL network :)");
		grid.add(intro2, 0, 3);
		
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		// ---------PREDEFINED QUERRIES-------------------------------------------
		
		ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(
			    "A", "B", "C", "D", "E", "F", "G", "H")
			);
		
		
        
        cb.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                Stage resultStage = new Stage();
                resultStage.setTitle("Result");
                resultStage.setX(mainStage.getX()+mainStage.getWidth());
                //TableView
                tableview = new TableView();
                buildData(querries[cb.getSelectionModel().getSelectedIndex()]);

                //Main Scene
                Scene scene = new Scene(tableview);        

                resultStage.setScene(scene);
                resultStage.show();
            }
        });
		
        
        grid.add(new Label("Predefined Querries : "), 0, 10, 4 , 1);
        grid.add(cb, 0, 11);
        
        // ----------------------------------------------------------------------
        
        
        // ----------------- INSERT ---------------------------------------------
        
        
        Button bt = new Button("Insert or Delete");
        
        bt.setOnAction(new EventHandler<ActionEvent>() {
        	
        	@Override
            public void handle(ActionEvent event) {
                Stage resultStage = new Stage();
                resultStage.setTitle("Insert or Delete");
                resultStage.setX(mainStage.getX()+mainStage.getWidth());
                
                GridPane insDel = new GridPane();
                
                insDel.add(new Text("insert the data u want to insert"),0,0);
                
                TextField entryTitle = new TextField();entryTitle.setPromptText("Forest Gump");
                insDel.add(new Label("Clip Name"), 0, 2);
                insDel.add(entryTitle, 1, 2);
                
                
                
                
                
                insDel.setHgap(3); 
                insDel.setVgap(10); 
                insDel.setPadding(new Insets(10, 10, 10, 10));
                //Main Scene
                Scene scene = new Scene(insDel);        
                resultStage.setScene(scene);
                resultStage.show();
            }
        	
        });
        
        
        grid.add(new Label("Advanced settings : "), 0, 15,4,1);
        grid.add(bt, 0,16,4,1);
        
        // ----------------------------------------------------------------------
        
        grid.setHgap(3); 
        grid.setVgap(10); 
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        
        
	    mainStage.setScene(new Scene(grid));
	    mainStage.show();
      
    }

}
