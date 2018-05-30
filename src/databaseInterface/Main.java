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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.sql.*;
import java.util.ArrayList;

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
                
                insDel.add(new Text("insert the data u want to insert"),0,0,2,1);
                
                // -- Clip Table
                TextField clipTitle = new TextField();clipTitle.setPromptText("Forest Gump");
                insDel.add(new Label("Clip Title "), 0, 2);
                insDel.add(clipTitle, 1, 2);
                
                TextField clipYear = new TextField();clipYear.setPromptText("2001");
                insDel.add(new Label("Clip Year "), 0, 3);
                insDel.add(clipYear, 1, 3);
                
                TextField clipType = new TextField();clipType.setPromptText("Comedy");
                insDel.add(new Label("Clip Type "), 0, 4);
                insDel.add(clipType, 1, 4);
                
                // -- Genres Table
                ArrayList<TextField> allGenres = new ArrayList();
                
                insDel.add(new Label("nb Genres "), 0, 6);
                TextField genreNb = new TextField("0");genreNb.setPromptText("3");
                genreNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage genreStage = new Stage();
                        	genreStage.setTitle("Genres");
                        	
                        	GridPane genreGrid = new GridPane();
                        	int nbGenres = Integer.parseInt(genreNb.getText());
                            for(int i = 0; i < nbGenres;++i) {
                            	allGenres.add(new TextField(""));
                            	
                            	genreGrid.add(new Label("Genres "), 0, 6+i);
                            	genreGrid.add(allGenres.get(i), 1, 6+i);
                            	
                            }
                            Scene genreScene = new Scene(genreGrid);        
                            genreStage.setScene(genreScene);
                            genreStage.show();
                        }
                    }
                });
                insDel.add(genreNb, 1, 6);
                
                
                // -- Language Table
                
                ArrayList<TextField> allLanguage = new ArrayList();
                
                insDel.add(new Label("nb Language "), 0, 7);
                TextField languageNb = new TextField("0");languageNb.setPromptText("1");
                languageNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage languageStage = new Stage();
                        	languageStage.setTitle("Language");
                        	
                        	GridPane languageGrid = new GridPane();
                        	int nbLanguage = Integer.parseInt(languageNb.getText());
                            for(int i = 0; i < nbLanguage;++i) {
                            	allLanguage.add(new TextField(""));
                            	
                            	languageGrid.add(new Label("Language "), 0, 6+i);
                            	languageGrid.add(allLanguage.get(i), 1, 6+i);
                            	
                            }
                            Scene languageScene = new Scene(languageGrid);        
                            languageStage.setScene(languageScene);
                            languageStage.show();
                        }
                    }
                });
                insDel.add(languageNb, 1, 7);
                
                // -- Running Time
                
                ArrayList<TextField> allRTCountryNames = new ArrayList();
                ArrayList<TextField> allRTRunningTime = new ArrayList();
                
                
                insDel.add(new Label("nb Run. Time "), 0, 8);
                TextField RTNb = new TextField("0");RTNb.setPromptText("1");
                RTNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage RTStage = new Stage();
                        	RTStage.setTitle("Run. Time");
                        	
                        	GridPane RTGrid = new GridPane();
                        	int nbRT = Integer.parseInt(RTNb.getText());
                            for(int i = 0; i < nbRT;++i) {
                            	allRTCountryNames.add(new TextField(""));
                            	allRTRunningTime.add(new TextField(""));
                            	
                            	RTGrid.add(new Label("Country N. "), 0, 6+i*3);
                            	RTGrid.add(allRTCountryNames.get(i), 1, 6+i*3);
                            	
                            	RTGrid.add(new Label("Running T. "), 0, 6+i*3+1);
                            	RTGrid.add(allRTRunningTime.get(i), 1, 6+i*3+1);
                            	
                            	RTGrid.add(new Label("- - - -"), 0, 6+i*3+2);
                            	
                            }
                            Scene RTScene = new Scene(RTGrid);        
                            RTStage.setScene(RTScene);
                            RTStage.show();
                        }
                    }
                });
                insDel.add(RTNb, 1, 8);
                
                // -- Released in Table
                
                ArrayList<TextField> allRICountryNames = new ArrayList();
                ArrayList<TextField> allRIReleaseDate = new ArrayList();
                
                
                insDel.add(new Label("nb Released in "), 0, 9);
                TextField RINb = new TextField("0");RINb.setPromptText("1");
                RINb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage RIStage = new Stage();
                        	RIStage.setTitle("RELEASED_IN");
                        	
                        	GridPane RIGrid = new GridPane();
                        	int nbRI = Integer.parseInt(RINb.getText());
                            for(int i = 0; i < nbRI;++i) {
                            	allRICountryNames.add(new TextField(""));
                            	allRIReleaseDate.add(new TextField(""));
                            	
                            	RIGrid.add(new Label("Country N. "), 0, 6+i*3);
                            	RIGrid.add(allRICountryNames.get(i), 1, 6+i*3);
                            	
                            	RIGrid.add(new Label("Release Date "), 0, 6+i*3+1);
                            	RIGrid.add(allRIReleaseDate.get(i), 1, 6+i*3+1);
                            	
                            	RIGrid.add(new Label("- - - -"), 0, 6+i*3+2);
                            	
                            }
                            Scene RIScene = new Scene(RIGrid);        
                            RIStage.setScene(RIScene);
                            RIStage.show();
                        }
                    }
                });
                insDel.add(RINb, 1, 9);
                
                // -- Link Table
                
                ArrayList<TextField> allLinkClip = new ArrayList();
                ArrayList<TextField> allLinkType = new ArrayList();
                
                
                insDel.add(new Label("nb links "), 0, 10);
                TextField linkNb = new TextField("0");RINb.setPromptText("1");
                linkNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage linkStage = new Stage();
                        	linkStage.setTitle("links Clip");
                        	
                        	GridPane linkGrid = new GridPane();
                        	int nbLink = Integer.parseInt(linkNb.getText());
                            for(int i = 0; i < nbLink;++i) {
                            	allLinkClip.add(new TextField(""));
                            	allLinkType.add(new TextField(""));
                            	
                            	linkGrid.add(new Label("Clip id. "), 0, 6+i*3);
                            	linkGrid.add(allLinkClip.get(i), 1, 6+i*3);
                            	
                            	linkGrid.add(new Label("link Type "), 0, 6+i*3+1);
                            	linkGrid.add(allLinkType.get(i), 1, 6+i*3+1);
                            	
                            	linkGrid.add(new Label("- - - -"), 0, 6+i*3+2);
                            	
                            }
                            Scene linkScene = new Scene(linkGrid);        
                            linkStage.setScene(linkScene);
                            linkStage.show();
                        }
                    }
                });
                insDel.add(linkNb, 1, 10);
                
                // -- Country Table
                
                ArrayList<TextField> allCountries = new ArrayList();
                
                insDel.add(new Label("nb Country "), 0, 11);
                TextField countryNb = new TextField("0");countryNb.setPromptText("1");
                countryNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	
                        	Stage countryStage = new Stage();
                        	countryStage.setTitle("country");
                        	
                        	GridPane countryGrid = new GridPane();
                        	int nbcountry = Integer.parseInt(countryNb.getText());
                            for(int i = 0; i < nbcountry;++i) {
                            	allCountries.add(new TextField(""));
                            	
                            	countryGrid.add(new Label("Countries "), 0, 6+i);
                            	countryGrid.add(allCountries.get(i), 1, 6+i);
                            	
                            }
                            Scene countryScene = new Scene(countryGrid);        
                            countryStage.setScene(countryScene);
                            countryStage.show();
                        }
                    }
                });
                insDel.add(countryNb, 1, 11);
                
                // -- People
                
                ArrayList<TextField> allPNames = new ArrayList();
                ArrayList<TextField> allPRealName = new ArrayList();
                ArrayList<TextField> allPDPofBirth = new ArrayList();
                ArrayList<TextField> allPHeight = new ArrayList();
                ArrayList<TextField> allPBiography = new ArrayList();
                ArrayList<TextField> allPBiographer = new ArrayList();
                ArrayList<TextField> allPDCofDeath = new ArrayList();
                ArrayList<TextField> allPTrivia = new ArrayList();
                ArrayList<TextField> allPQuotes = new ArrayList();
                ArrayList<TextField> allPTradeMark = new ArrayList();
                ArrayList<TextField> allPWRTfrom = new ArrayList();
                
                
                insDel.add(new Label("nb People "), 0, 12);
                TextField peopleNb = new TextField("0");peopleNb.setPromptText("1");
                peopleNb.setOnKeyPressed(new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent ke)
                    {
                        if (ke.getCode().equals(KeyCode.ENTER))
                        {
                        	int nbPeople = Integer.parseInt(peopleNb.getText());
                            for(int i = 0; i < nbPeople;++i) {
	                        	Stage peopleStage = new Stage();
	                        	peopleStage.setTitle("PEOPLE");
	                        	
	                        	GridPane peopleGrid = new GridPane();
	                        	
	                            allPNames.add(new TextField(""));
	                            peopleGrid.add(new Label("Full Name "), 0, 1);
	                            peopleGrid.add(allPNames.get(i), 1, 1);
	                            
	                            allPRealName.add(new TextField(""));
	                            peopleGrid.add(new Label("Real Name "), 0, 2);
	                            peopleGrid.add(allPRealName.get(i), 1, 2);
	                            
	                            allPDPofBirth.add(new TextField(""));
	                            peopleGrid.add(new Label("D&P of birth "), 0, 3);
	                            peopleGrid.add(allPDPofBirth.get(i), 1, 3);
	                            
	                            allPHeight.add(new TextField(""));
	                            peopleGrid.add(new Label("Height "), 0, 4);
	                            peopleGrid.add(allPHeight.get(i), 1, 4);
	                            
	                            allPBiography.add(new TextField(""));
	                            peopleGrid.add(new Label("Biography "), 0, 5);
	                            peopleGrid.add(allPBiography.get(i), 1, 5);
	                            
	                            allPBiographer.add(new TextField(""));
	                            peopleGrid.add(new Label("Biographer "), 0, 6);
	                            peopleGrid.add(allPBiographer.get(i), 1, 6);
	                            
	                            allPDCofDeath.add(new TextField(""));
	                            peopleGrid.add(new Label("D&C of death "), 0, 7);
	                            peopleGrid.add(allPDCofDeath.get(i), 1, 7);
	                            
	                            allPTrivia.add(new TextField(""));
	                            peopleGrid.add(new Label("Trivia "), 0, 8);
	                            peopleGrid.add(allPTrivia.get(i), 1, 8);
	                            
	                            allPQuotes.add(new TextField(""));
	                            peopleGrid.add(new Label("Quotes "), 0, 9);
	                            peopleGrid.add(allPQuotes.get(i), 1, 9);
	                            
	                            allPTradeMark.add(new TextField(""));
	                            peopleGrid.add(new Label("Trade Mark "), 0, 10);
	                            peopleGrid.add(allPTradeMark.get(i), 1, 10);
	                            
	                            allPWRTfrom.add(new TextField(""));
	                            peopleGrid.add(new Label("from ? "), 0, 11);
	                            peopleGrid.add(allPWRTfrom.get(i), 1, 11);
	                            

      	
	                            
	                            Scene peopleScene = new Scene(peopleGrid);        
	                            peopleStage.setScene(peopleScene);
	                            peopleStage.show();
                            }
                        }
                    }
                });
                insDel.add(peopleNb, 1, 12);
                
                
                
                
                
                
                
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
