package databaseInterface;

import javafx.application.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.sql.*;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

// my version 01

public class Main extends Application {

	// TABLE VIEW AND DATA AND SQL PREDEFINED QUEERIES
	@SuppressWarnings("rawtypes")
	private ObservableList<ObservableList> data;
	@SuppressWarnings("rawtypes")
	private TableView tableview;

	private String qa = "SELECT cl.CLIPTITLE, rt.RUNNINGTIME " + "FROM  CLIPS cl, RUNNINGTIMES rt "
			+ "where rt.RELEASECOUNTRY = 'France' AND rt.CLIPID = cl.CLIPID " + "ORDER BY rt.RUNNINGTIME DESC "
			+ "FETCH FIRST 10 ROWS ONLY ";

	private String qb = "SELECT COUNT(*) " + "FROM RELEASEDDATES rl "
			+ "WHERE EXTRACT(YEAR FROM rl.RELEASEDATE) = 2001 " + "GROUP BY rl.RELEASECOUNTRY ";

	private String qc = "SELECT COUNT(*), gr.GENRE " + "FROM RELEASEDDATES rl, GENRES gr "
			+ "WHERE rl.RELEASECOUNTRY = 'USA' and EXTRACT(YEAR FROM rl.RELEASEDATE) >= 2013 and gr.CLIPID = rl.CLIPID "
			+ "GROUP BY gr.GENRE ";

	private String qd = "WITH TEMP as (SELECT ac.PERSONID, COUNT(DISTINCT(ac.CLIPID)) as CNT "
			+ "              FROM ACTED ac " + "              GROUP BY ac.PERSONID "
			+ "              ORDER BY CNT DESC " + "              FETCH FIRST 1 ROW ONLY) "
			+ "SELECT pp.FULLNAME, tmp.CNT " + "FROM TEMP tmp, PEOPLE pp " + "WHERE pp.PERSONID = tmp.PERSONID ";

	private String qe = "SELECT MAX(COUNT(DISTINCT(di.CLIPID))) AS CNT " + "FROM DIRECTED di "
			+ "GROUP BY di.PERSONID ";

	private String qf = "WITH TEMP AS (SELECT DISTINCT(PERSONID)\r\n"
			+ "              FROM ((SELECT DISTINCT PRODUCED.PERSONID, PRODUCED.CLIPID FROM PRODUCED \r\n"
			+ "                    INNER JOIN ACTED ON (ACTED.PERSONID = PRODUCED.PERSONID) AND (ACTED.CLIPID = PRODUCED.CLIPID))\r\n"
			+ "                    UNION \r\n"
			+ "                    (SELECT DISTINCT PRODUCED.PERSONID, PRODUCED.CLIPID FROM PRODUCED\r\n"
			+ "                    INNER JOIN WROTE ON (WROTE.PERSONID = PRODUCED.PERSONID) AND (WROTE.CLIPID = PRODUCED.CLIPID))\r\n"
			+ "                    UNION\r\n"
			+ "                    (SELECT DISTINCT PRODUCED.PERSONID, PRODUCED.CLIPID FROM PRODUCED\r\n"
			+ "                    INNER JOIN DIRECTED ON (DIRECTED.PERSONID = PRODUCED.PERSONID) AND (DIRECTED.CLIPID = PRODUCED.CLIPID))\r\n"
			+ "                    UNION\r\n"
			+ "                    (SELECT DISTINCT WROTE.PERSONID, WROTE.CLIPID FROM WROTE\r\n"
			+ "                    INNER JOIN ACTED ON (ACTED.PERSONID = WROTE.PERSONID) AND (ACTED.CLIPID = WROTE.CLIPID))\r\n"
			+ "                    UNION\r\n"
			+ "                    (SELECT DISTINCT WROTE.PERSONID, WROTE.CLIPID FROM WROTE\r\n"
			+ "                    INNER JOIN DIRECTED ON (DIRECTED.PERSONID = WROTE.PERSONID) AND (DIRECTED.CLIPID = WROTE.CLIPID))\r\n"
			+ "                    UNION\r\n"
			+ "                    (SELECT DISTINCT DIRECTED.PERSONID, DIRECTED.CLIPID FROM DIRECTED\r\n"
			+ "                    INNER JOIN ACTED ON (ACTED.PERSONID = DIRECTED.PERSONID) AND (ACTED.CLIPID = DIRECTED.CLIPID))))\r\n"
			+ "SELECT pp.FULLNAME\r\n" + "FROM TEMP tmp, PEOPLE pp\r\n" + "WHERE pp.PERSONID = tmp.PERSONID\r\n"
			+ "FETCH FIRST 10 ROWS ONLY";

	private String qg = "SELECT ln.LANGUAGE, COUNT(ln.LANGUAGE) " + "FROM LANGUAGES ln " + "GROUP BY ln.LANGUAGE "
			+ "ORDER BY COUNT(ln.LANGUAGE) DESC " + "FETCH FIRST 10 ROWS ONLY ";

	private String qh = "WITH TEMP AS (SELECT ac.PERSONID, COUNT(DISTINCT(ac.CLIPID)) as CNT "
			+ "              FROM ACTED ac, CLIPS cl "
			+ "              WHERE ac.CLIPID = cl.CLIPID AND cl.CLIPTYPE = 'TV' "
			+ "              GROUP BY ac.PERSONID " + "              ORDER BY CNT DESC "
			+ "              FETCH FIRST 1 ROW ONLY) " + "SELECT pp.FULLNAME, tmp.CNT " + "FROM TEMP tmp, PEOPLE pp "
			+ "WHERE tmp.PERSONID = pp.PERSONID ";

	private String querries[] = { qa, qb, qc, qd, qe, qf, qg, qh };
	private String queries2[] = Queries2.queries2;

	// MAIN EXECUTOR
	public static void main(String[] args) {
		launch(args);
	}

	// CONNECTION DATABASE
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildData(String sql) {

		data = FXCollections.observableArrayList();
		try {
			System.out.println("	- INTRO TO DATABSE -");
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.print("Database Connection : ");
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@//diassrv2.epfl.ch:1521/orcldias.epfl.ch",
					"DB2018_G35", "DB2018_G35");
			System.out.println("OK");

			// ResultSet
			ResultSet rs = con.createStatement().executeQuery(sql);

			/**********************************
			 * TABLE COLUMN ADDED DYNAMICALLY *
			 **********************************/
			for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				// We are using non property style for making dynamic table
				int j = i;
				TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				col.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							@Override
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								try {
									return new SimpleStringProperty(param.getValue().get(j).toString());
								}catch (Exception e) {
									return new SimpleStringProperty("Null");
								}
							}
						});

				tableview.getColumns().addAll(col);
			}

			/********************************
			 * Data added to ObservableList *
			 ********************************/
			while (rs.next()) {
				// Iterate Row
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// Iterate Column
					row.add(rs.getString(i));
				}
				data.add(row);

			}

			// FINALLY ADDED TO TableView
			tableview.setItems(data);
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void start(Stage mainStage) throws Exception {

		// allow to close all windows from the main window
		mainStage.setOnCloseRequest((new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
			}
		}));
		
		Image emoji = new Image(getClass().getResourceAsStream("pervertedEmoji.png"));
		
		mainStage.getIcons().add(emoji);

		mainStage.setTitle("DB-P Team35");
		
		GridPane grid = new GridPane();
		grid.getColumnConstraints().add(new ColumnConstraints(150));
		grid.getColumnConstraints().add(new ColumnConstraints(100));
		try {
			

			Label intro = new Label("  this is our wonderful\n GUI");
			ImageView ivIntro = new ImageView(emoji);
			ivIntro.setFitWidth(30);
			ivIntro.setFitHeight(30);
			intro.setGraphic(ivIntro);

			grid.add(intro, 0, 1, 2, 1);
			Text intro2 = new Text(" - first make sure you are \nconnected to our EPFL\n network :)");
			grid.add(intro2, 0, 3);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// ---------PREDEFINED QUERRIES-------------------------------------------

		ChoiceBox<?> cb = new ChoiceBox<String>(
				FXCollections.observableArrayList("A", "B", "C", "D", "E", "F", "G", "H"));

		ChoiceBox<?> cb2 = new ChoiceBox<String>(
				FXCollections.observableArrayList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"));

		Button queryBtn = new Button("GO");
		Button queryBtn2 = new Button("GO");
		
		TextField queryHText = new TextField();
		queryHText.setPrefWidth(60);
		queryHText.setVisible(false);
		

		cb.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
		    if(!obs.getValue()) {
		    	if(cb.getSelectionModel().getSelectedIndex()==7) {
		    		queryHText.setVisible(true);
		    	} else {
		    		queryHText.setVisible(false);
		    	}
		    }
		});
		

		queryBtn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				Stage resultStage = new Stage();
				resultStage.setTitle("Result");
				resultStage.setX(mainStage.getX() + mainStage.getWidth());
				// TableView
				tableview = new TableView();
				
				if(cb.getSelectionModel().getSelectedIndex()==7 && !queryHText.getText().isEmpty()) {
					String modifiedQueryH = querries[cb.getSelectionModel().getSelectedIndex()].replace("TV", queryHText.getText());
					buildData(modifiedQueryH);
				} else {
					buildData(querries[cb.getSelectionModel().getSelectedIndex()]);
				}
				
				// Main Scene
				Scene scene = new Scene(tableview);

				resultStage.setScene(scene);
				resultStage.getIcons().add(emoji);
				resultStage.show();
			}
		});

		queryBtn2.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				Stage resultStage = new Stage();
				resultStage.setTitle("Result");
				resultStage.setX(mainStage.getX() + mainStage.getWidth());
				// TableView

				tableview = new TableView();
				buildData(queries2[cb2.getSelectionModel().getSelectedIndex()]);

				// Main Scene
				Scene scene = new Scene(tableview);

				resultStage.setScene(scene);
				resultStage.getIcons().add(emoji);
				resultStage.show();
			}
		});

		grid.add(new Label("Predefined Querries Part 2 : "), 0, 7, 2, 1);
		
		GridPane group1 = new GridPane();group1.setHgap(10);
		group1.add(queryHText, 2, 0);
		group1.add(cb, 0, 0);
		group1.add(queryBtn, 1, 0);
		grid.add(group1, 0, 8, 2, 1);
		
		grid.add(new Label("Predefined Querries Part 3 : "), 0, 11, 2, 1);
		GridPane group2 = new GridPane();group2.setHgap(10);
		group2.add(cb2, 0, 0);
		group2.add(queryBtn2, 1, 0);
		grid.add(group2, 0, 12, 2, 1);

		// ----------------------------------------------------------------------

		// ----------------- INSERT ---------------------------------------------

		Button bt = new Button("Insert Delete");

		bt.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Stage resultStage = new Stage();
				resultStage.setTitle("Insert or Delete");
				resultStage.setX(mainStage.getX() + mainStage.getWidth());

				GridPane insDel = new GridPane();

				insDel.add(new Text("Insert or delete the data you want"), 0, 0, 2, 1);
				CheckBox deleteClip = new CheckBox("Delete clip");
				deleteClip.selectedProperty().set(false);
				
				// -- Clip Table
				TextField clipTitle = new TextField();
				clipTitle.setPromptText("Forest Gump");
				insDel.add(new Label("Clip Title * "), 0, 2);
				insDel.add(clipTitle, 1, 2);

				TextField clipYear = new TextField();
				clipYear.setPromptText("2001");
				insDel.add(new Label("Clip Year "), 0, 3);
				insDel.add(clipYear, 1, 3);

				TextField clipType = new TextField();
				clipType.setPromptText("TV");
				insDel.add(new Label("Clip Type "), 0, 4);
				insDel.add(clipType, 1, 4);
				
				TextField rating = new TextField();
				rating.setPromptText("5.5");
				insDel.add(new Label("Rating "), 0, 5);
				insDel.add(rating, 1, 5);
				
				TextField votes = new TextField();
				votes.setPromptText("100");
				insDel.add(new Label("Votes "), 0, 6);
				insDel.add(votes, 1, 6);

				// -- Genres Table
				ArrayList<TextField> allGenres = new ArrayList<TextField>();

				insDel.add(new Label("nb Genres "), 0, 7);
				TextField genreNb = new TextField("0");
				genreNb.setPromptText("3");
				genreNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage genreStage = new Stage();
							genreStage.setTitle("Genres");

							GridPane genreGrid = new GridPane();
							int nbGenres = Integer.parseInt(genreNb.getText());
							for (int i = 0; i < nbGenres; ++i) {
								allGenres.add(new TextField(""));

								genreGrid.add(new Label("Genres "), 0, 6 + i);
								genreGrid.add(allGenres.get(i), 1, 6 + i);

							}
							Scene genreScene = new Scene(genreGrid);
							genreStage.setScene(genreScene);
							genreStage.getIcons().add(emoji);
							genreStage.show();
						}
					}
				});
				insDel.add(genreNb, 1, 7);

				// -- Language Table

				ArrayList<TextField> allLanguage = new ArrayList<TextField>();

				insDel.add(new Label("nb Language "), 0, 8);
				TextField languageNb = new TextField("0");
				languageNb.setPromptText("1");
				languageNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage languageStage = new Stage();
							languageStage.setTitle("Language");

							GridPane languageGrid = new GridPane();
							int nbLanguage = Integer.parseInt(languageNb.getText());
							for (int i = 0; i < nbLanguage; ++i) {
								allLanguage.add(new TextField(""));

								languageGrid.add(new Label("Language "), 0, 6 + i);
								languageGrid.add(allLanguage.get(i), 1, 6 + i);

							}
							Scene languageScene = new Scene(languageGrid);
							languageStage.setScene(languageScene);
							languageStage.getIcons().add(emoji);
							languageStage.show();
						}
					}
				});
				insDel.add(languageNb, 1, 8);

				// -- Running Time

				ArrayList<TextField> allRTCountryNames = new ArrayList<TextField>();
				ArrayList<TextField> allRTRunningTime = new ArrayList<TextField>();

				insDel.add(new Label("nb Run. Time "), 0, 9);
				TextField RTNb = new TextField("0");
				RTNb.setPromptText("1");
				RTNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage RTStage = new Stage();
							RTStage.setTitle("Run. Time");

							GridPane RTGrid = new GridPane();
							int nbRT = Integer.parseInt(RTNb.getText());
							for (int i = 0; i < nbRT; ++i) {
								allRTCountryNames.add(new TextField(""));
								allRTRunningTime.add(new TextField(""));

								RTGrid.add(new Label("Country N. "), 0, 6 + i * 3);
								RTGrid.add(allRTCountryNames.get(i), 1, 6 + i * 3);

								RTGrid.add(new Label("Running T. "), 0, 6 + i * 3 + 1);
								RTGrid.add(allRTRunningTime.get(i), 1, 6 + i * 3 + 1);

								RTGrid.add(new Label("- - - -"), 0, 6 + i * 3 + 2);

							}
							Scene RTScene = new Scene(RTGrid);
							RTStage.setScene(RTScene);
							RTStage.getIcons().add(emoji);
							RTStage.show();
						}
					}
				});
				insDel.add(RTNb, 1, 9);

				// -- Released in Table

				ArrayList<TextField> allRICountryNames = new ArrayList<TextField>();
				ArrayList<TextField> allRIReleaseDate = new ArrayList<TextField>();

				insDel.add(new Label("nb Released in "), 0, 10);
				TextField RINb = new TextField("0");
				RINb.setPromptText("1");
				RINb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage RIStage = new Stage();
							RIStage.setTitle("RELEASED_IN");

							GridPane RIGrid = new GridPane();
							int nbRI = Integer.parseInt(RINb.getText());
							for (int i = 0; i < nbRI; ++i) {
								allRICountryNames.add(new TextField(""));
								allRIReleaseDate.add(new TextField(""));

								RIGrid.add(new Label("Country N. "), 0, 6 + i * 3);
								RIGrid.add(allRICountryNames.get(i), 1, 6 + i * 3);

								RIGrid.add(new Label("Release Date "), 0, 6 + i * 3 + 1);
								RIGrid.add(allRIReleaseDate.get(i), 1, 6 + i * 3 + 1);

								RIGrid.add(new Label("- - - -"), 0, 6 + i * 3 + 2);

							}
							Scene RIScene = new Scene(RIGrid);
							RIStage.setScene(RIScene);
							RIStage.getIcons().add(emoji);
							RIStage.show();
						}
					}
				});
				insDel.add(RINb, 1, 10);

				// -- Link Table

				ArrayList<TextField> allLinkClip = new ArrayList<TextField>();
				ArrayList<TextField> allLinkType = new ArrayList<TextField>();

				insDel.add(new Label("nb links "), 0, 11);
				TextField linkNb = new TextField("0");
				RINb.setPromptText("1");
				linkNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage linkStage = new Stage();
							linkStage.setTitle("links Clip");

							GridPane linkGrid = new GridPane();
							int nbLink = Integer.parseInt(linkNb.getText());
							for (int i = 0; i < nbLink; ++i) {
								allLinkClip.add(new TextField(""));
								allLinkType.add(new TextField(""));

								linkGrid.add(new Label("Clip id. "), 0, 6 + i * 3);
								linkGrid.add(allLinkClip.get(i), 1, 6 + i * 3);

								linkGrid.add(new Label("link Type "), 0, 6 + i * 3 + 1);
								linkGrid.add(allLinkType.get(i), 1, 6 + i * 3 + 1);

								linkGrid.add(new Label("- - - -"), 0, 6 + i * 3 + 2);

							}
							Scene linkScene = new Scene(linkGrid);
							linkStage.setScene(linkScene);
							linkStage.getIcons().add(emoji);
							linkStage.show();
						}
					}
				});
				insDel.add(linkNb, 1, 11);

				// -- Country Table

				ArrayList<TextField> allCountries = new ArrayList<TextField>();

				insDel.add(new Label("nb parti. from "), 0, 12);
				TextField countryNb = new TextField("0");
				countryNb.setPromptText("1");
				countryNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {

							Stage countryStage = new Stage();
							countryStage.setTitle("country");

							GridPane countryGrid = new GridPane();
							int nbcountry = Integer.parseInt(countryNb.getText());
							for (int i = 0; i < nbcountry; ++i) {
								allCountries.add(new TextField(""));

								countryGrid.add(new Label("Countries "), 0, 6 + i);
								countryGrid.add(allCountries.get(i), 1, 6 + i);

							}
							Scene countryScene = new Scene(countryGrid);
							countryStage.setScene(countryScene);
							countryStage.getIcons().add(emoji);
							countryStage.show();
						}
					}
				});
				insDel.add(countryNb, 1, 12);

				// -- People

				ArrayList<TextField> allPNames = new ArrayList<TextField>();
				ArrayList<TextField> allPRealName = new ArrayList<TextField>();
				ArrayList<TextField> allPDPofBirth = new ArrayList<TextField>();
				ArrayList<TextField> allPHeight = new ArrayList<TextField>();
				ArrayList<TextField> allPBiography = new ArrayList<TextField>();
				ArrayList<TextField> allPBiographer = new ArrayList<TextField>();
				ArrayList<TextField> allPDCofDeath = new ArrayList<TextField>();
				ArrayList<TextField> allPTrivia = new ArrayList<TextField>();
				ArrayList<TextField> allPQuotes = new ArrayList<TextField>();
				ArrayList<TextField> allPTradeMark = new ArrayList<TextField>();
				ArrayList<TextField> allPWRTfrom = new ArrayList<TextField>();

				ArrayList<ArrayList<TextField>> allPDirectedRoles = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPDirectedAddInfo = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPDirectedClip = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPproducedRoles = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPproducedAddInfo = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPproducedClip = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPactedChar = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPactedAddInfo = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPactedOrderCredit = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPactedClip = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPwriterWT = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPwriterAddInfo = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPwriterRole = new ArrayList<ArrayList<TextField>>();
				ArrayList<ArrayList<TextField>> allPwriterClip = new ArrayList<ArrayList<TextField>>();
				
				ArrayList<CheckBox> allDeletePerson = new ArrayList<>();

				TextField directedNb = new TextField("0");
				directedNb.setPromptText("1");
				TextField actedNb = new TextField("0");
				actedNb.setPromptText("1");
				TextField writerNb = new TextField("0");
				writerNb.setPromptText("1");
				TextField producedNb = new TextField("0");
				producedNb.setPromptText("1");

				insDel.add(new Label("nb People "), 0, 13);
				TextField peopleNb = new TextField("0");
				peopleNb.setPromptText("1");
				peopleNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {
							int nbPeople = Integer.parseInt(peopleNb.getText());
							for (int i = 0; i < nbPeople; ++i) {
								Stage peopleStage = new Stage();
								peopleStage.setTitle("PEOPLE");
									
								GridPane peopleGrid = new GridPane();
								
								CheckBox deletePerson = new CheckBox("Delete person");
								deletePerson.selectedProperty().set(false);
								peopleGrid.add(deletePerson,1,16);
								allDeletePerson.add(deletePerson);
								
								allPNames.add(new TextField(""));
								peopleGrid.add(new Label("Full Name *"), 0, 1);
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

								peopleGrid.add(new Label("nb Directed "), 0, 12);

								directedNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
									@Override
									public void handle(KeyEvent ke) {
										if (ke.getCode().equals(KeyCode.ENTER)) {

											Stage directedStage = new Stage();
											directedStage.setTitle("directed");

											GridPane directedGrid = new GridPane();
											int nbdirected = Integer.parseInt(directedNb.getText());

											allPDirectedRoles.add(new ArrayList<TextField>());
											allPDirectedAddInfo.add(new ArrayList<TextField>());
											allPDirectedClip.add(new ArrayList<TextField>());

											for (int k = 0; k < nbdirected; ++k) {
												allPDirectedAddInfo.get(allPDirectedAddInfo.size() - 1)
														.add(new TextField(""));
												allPDirectedRoles.get(allPDirectedRoles.size() - 1)
														.add(new TextField(""));
												allPDirectedClip.get(allPDirectedClip.size() - 1)
														.add(new TextField(""));

												directedGrid.add(new Label("AddInfo "), 0, 6 + k * 4);
												directedGrid.add(
														allPDirectedAddInfo.get(allPDirectedAddInfo.size() - 1).get(k),
														1, 6 + k * 4);

												directedGrid.add(new Label("Role "), 0, 6 + k * 4 + 1);
												directedGrid.add(
														allPDirectedRoles.get(allPDirectedRoles.size() - 1).get(k), 1,
														6 + k * 4 + 1);

												// directedGrid.add(new Label("CLip "), 0, 6+k*4+2);
												// directedGrid.add(allPDirectedClip.get(allPDirectedClip.size()-1).get(k),
												// 1, 6+k*4+2);

												directedGrid.add(new Label("- - - -"), 0, 6 + k * 4 + 3);

											}
											Scene directedScene = new Scene(directedGrid);
											directedStage.setScene(directedScene);
											directedStage.getIcons().add(emoji);
											directedStage.show();
										}
									}
								});
								peopleGrid.add(directedNb, 1, 12);

								peopleGrid.add(new Label("nb Produced "), 0, 13);

								producedNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
									@Override
									public void handle(KeyEvent ke) {
										if (ke.getCode().equals(KeyCode.ENTER)) {

											Stage producedStage = new Stage();
											producedStage.setTitle("produced");

											GridPane producedGrid = new GridPane();
											int nbproduced = Integer.parseInt(producedNb.getText());

											allPproducedRoles.add(new ArrayList<TextField>());
											allPproducedAddInfo.add(new ArrayList<TextField>());
											allPproducedClip.add(new ArrayList<TextField>());

											for (int k = 0; k < nbproduced; ++k) {
												allPproducedAddInfo.get(allPproducedAddInfo.size() - 1)
														.add(new TextField(""));
												allPproducedRoles.get(allPproducedRoles.size() - 1)
														.add(new TextField(""));
												allPproducedClip.get(allPproducedClip.size() - 1)
														.add(new TextField(""));

												producedGrid.add(new Label("AddInfo "), 0, 6 + k * 4);
												producedGrid.add(
														allPproducedAddInfo.get(allPproducedAddInfo.size() - 1).get(k),
														1, 6 + k * 4);

												producedGrid.add(new Label("Role "), 0, 6 + k * 4 + 1);
												producedGrid.add(
														allPproducedRoles.get(allPproducedRoles.size() - 1).get(k), 1,
														6 + k * 4 + 1);

												// producedGrid.add(new Label("Clip "), 0, 6+k*4+2);
												// producedGrid.add(allPproducedClip.get(allPproducedClip.size()-1).get(k),
												// 1, 6+k*4+2);

												producedGrid.add(new Label("- - - -"), 0, 6 + k * 4 + 3);

											}
											Scene producedScene = new Scene(producedGrid);
											producedStage.setScene(producedScene);
											producedStage.getIcons().add(emoji);
											producedStage.show();
										}
									}
								});
								peopleGrid.add(producedNb, 1, 13);

								peopleGrid.add(new Label("nb acted "), 0, 14);

								actedNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
									@Override
									public void handle(KeyEvent ke) {
										if (ke.getCode().equals(KeyCode.ENTER)) {

											Stage actedStage = new Stage();
											actedStage.setTitle("acted");

											GridPane actedGrid = new GridPane();
											int nbacted = Integer.parseInt(actedNb.getText());

											allPactedChar.add(new ArrayList<TextField>());
											allPactedAddInfo.add(new ArrayList<TextField>());
											allPactedOrderCredit.add(new ArrayList<TextField>());
											allPactedClip.add(new ArrayList<TextField>());

											for (int k = 0; k < nbacted; ++k) {
												allPactedAddInfo.get(allPactedAddInfo.size() - 1)
														.add(new TextField(""));
												allPactedChar.get(allPactedChar.size() - 1).add(new TextField(""));
												allPactedOrderCredit.get(allPactedOrderCredit.size() - 1)
														.add(new TextField(""));
												allPactedClip.get(allPactedClip.size() - 1).add(new TextField(""));

												actedGrid.add(new Label("AddInfo "), 0, 6 + k * 5);
												actedGrid.add(allPactedAddInfo.get(allPactedAddInfo.size() - 1).get(k),
														1, 6 + k * 5);

												actedGrid.add(new Label("Role "), 0, 6 + k * 5 + 1);
												actedGrid.add(allPactedChar.get(allPactedChar.size() - 1).get(k), 1,
														6 + k * 5 + 1);

												actedGrid.add(new Label("Order Credit "), 0, 6 + k * 5 + 2);
												actedGrid.add(allPactedOrderCredit.get(allPactedOrderCredit.size() - 1)
														.get(k), 1, 6 + k * 5 + 2);

												// actedGrid.add(new Label("Clip "), 0, 6+k*5+3);
												// actedGrid.add(allPactedClip.get(allPactedClip.size()-1).get(k), 1,
												// 6+k*5+3);

												actedGrid.add(new Label("- - - -"), 0, 6 + k * 5 + 4);

											}
											Scene actedScene = new Scene(actedGrid);
											actedStage.setScene(actedScene);
											actedStage.getIcons().add(emoji);
											actedStage.show();
										}
									}
								});
								peopleGrid.add(actedNb, 1, 14);

								peopleGrid.add(new Label("nb writer "), 0, 15);

								writerNb.setOnKeyPressed(new EventHandler<KeyEvent>() {
									@Override
									public void handle(KeyEvent ke) {
										if (ke.getCode().equals(KeyCode.ENTER)) {

											Stage writerStage = new Stage();
											writerStage.setTitle("writer");

											GridPane writerGrid = new GridPane();
											int nbwriter = Integer.parseInt(writerNb.getText());

											allPwriterWT.add(new ArrayList<TextField>());
											allPwriterAddInfo.add(new ArrayList<TextField>());
											allPwriterRole.add(new ArrayList<TextField>());
											allPwriterClip.add(new ArrayList<TextField>());

											for (int k = 0; k < nbwriter; ++k) {
												allPwriterWT.get(allPwriterWT.size() - 1).add(new TextField(""));
												allPwriterAddInfo.get(allPwriterAddInfo.size() - 1)
														.add(new TextField(""));
												allPwriterRole.get(allPwriterRole.size() - 1).add(new TextField(""));
												allPwriterClip.get(allPwriterClip.size() - 1).add(new TextField(""));

												writerGrid.add(new Label("Work Types "), 0, 6 + k * 5);
												writerGrid.add(allPwriterWT.get(allPwriterWT.size() - 1).get(k), 1,
														6 + k * 5);

												writerGrid.add(new Label("Add Info "), 0, 6 + k * 5 + 1);
												writerGrid.add(
														allPwriterAddInfo.get(allPwriterAddInfo.size() - 1).get(k), 1,
														6 + k * 5 + 1);

												writerGrid.add(new Label("Roles "), 0, 6 + k * 5 + 2);
												writerGrid.add(allPwriterRole.get(allPwriterRole.size() - 1).get(k), 1,
														6 + k * 5 + 2);

												// writerGrid.add(new Label("Clip "), 0, 6+k*5+3);
												// writerGrid.add(allPwriterClip.get(allPwriterClip.size()-1).get(k), 1,
												// 6+k*5+3);

												writerGrid.add(new Label("- - - -"), 0, 6 + k * 5 + 4);

											}
											Scene writerScene = new Scene(writerGrid);
											writerStage.setScene(writerScene);
											writerStage.getIcons().add(emoji);
											writerStage.show();
										}
									}
								});
								peopleGrid.add(writerNb, 1, 15);

								Scene peopleScene = new Scene(peopleGrid);
								peopleStage.setScene(peopleScene);
								peopleStage.getIcons().add(emoji);
								peopleStage.show();
							}
						}
					}
				});
				insDel.add(peopleNb, 1, 13);

				Button insertBtn = new Button("INSERT");
				insertBtn.setOnAction((e) -> {

					try {
						System.out.println("	- INTRO TO DATABASE -");
						Class.forName("oracle.jdbc.driver.OracleDriver");
						System.out.print("Database Connection : ");
						Connection con = DriverManager.getConnection(
								"jdbc:oracle:thin:@//diassrv2.epfl.ch:1521/orcldias.epfl.ch", "DB2018_G35",
								"DB2018_G35");
						System.out.println("OK");
						String insertSql = "";
						String clipID_query = " (SELECT MAX ( ClipID ) AS ClipMax From Clips ) ";
						String clipID = "";
						if(!clipTitle.getText().equals("")) {
							String queryClip = "Select CLIPID from clips where cliptitle = '" + clipTitle.getText().replace("'", "''") +"'";
							ResultSet rscl =con.createStatement().executeQuery(queryClip);
							if(!rscl.isBeforeFirst()) {
								
								String insertSql0 = " INSERT INTO Clips (ClipID,ClipYear,ClipTitle,ClipType) ";
								if(!clipYear.getText().equals("")) {
									insertSql0 += " VALUES ((SELECT MAX ( ClipID ) From Clips ) + 1 ,TO_DATE(" + clipYear.getText()
											+ ",'YYYY') , '" + clipTitle.getText().replace("'", "''") + "' , '" + clipType.getText().replace("'", "''") + "' ) ";
									System.out.println(insertSql0);
								} else {
									insertSql0 += " VALUES ((SELECT MAX ( ClipID ) From Clips ) + 1,'','"+ clipTitle.getText().replace("'", "''") +"','" 
											+ clipType.getText().replace("'", "''") + "' ) ";
									System.out.println(insertSql0);
								}
								con.createStatement().executeQuery(insertSql0);
								
								ResultSet rs_clipid = con.createStatement().executeQuery(clipID_query);
								while (rs_clipid.next()) {
									clipID = rs_clipid.getString("ClipMax");
								}
								
							} else {
								while(rscl.next()) {
									clipID = rscl.getString("CLIPID");
								}
							}
						}
						
						
						
						
						if(!rating.getText().isEmpty() || !votes.getText().isEmpty()) {
							insertSql += " INSERT INTO Ratings ( rank , ClipID , Votes) ";
							insertSql += " VALUES ( '" + rating.getText() + "' , " + clipID + ",'"+votes.getText()+"') "; 
						
						}

						for (int a = 0; a < allGenres.size(); ++a) {
							insertSql += " INSERT INTO Genres ( Genre , ClipID ) ";
							insertSql += " VALUES ( '" + allGenres.get(a).getText().replace("'", "''") + "' , " + clipID + " ) ";
							
							con.createStatement().executeQuery(insertSql);
							insertSql = "";
						}

						for (int a = 0; a < allLanguage.size(); ++a) {
							insertSql += " INSERT INTO Languages (Language,ClipID) ";
							insertSql += " VALUES ( '" + allLanguage.get(a).getText().replace("'", "''") + "' ," + clipID + " ) ";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";
						}

						for (int a = 0; a < allRTCountryNames.size(); ++a) {

							insertSql += "INSERT INTO countries (countryname) " + "	SELECT * FROM (SELECT '"
									+ allRTCountryNames.get(a).getText().replace("'", "''") + "' from dual) tmp "
									+ "WHERE NOT EXISTS (SELECT countryname " + "					FROM countries "
									+ "					WHERE countryname = '" + allRTCountryNames.get(a).getText().replace("'", "''")
									+ "')" + "FETCH FIRST ROW ONLY";

							con.createStatement().executeQuery(insertSql);
							insertSql = "";

						}

						for (int a = 0; a < allRTCountryNames.size(); ++a) {

							insertSql += " INSERT INTO RunningTimes ( RunningTime , ReleaseCountry , ClipID ) ";
							insertSql += " VALUES ( " + allRTRunningTime.get(a).getText() + ",'"
									+ allRTCountryNames.get(a).getText().replace("'", "''") + "' ," + clipID + " ) ";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";

						}

						for (int a = 0; a < allRICountryNames.size(); ++a) {

							insertSql += "INSERT INTO countries (countryname) " + "	SELECT * FROM (SELECT '"
									+ allRICountryNames.get(a).getText().replace("'", "''") + "' from dual) tmp "
									+ "WHERE NOT EXISTS (SELECT countryname " + "					FROM countries "
									+ "					WHERE countryname = '" + allRICountryNames.get(a).getText().replace("'", "''")
									+ "')" + "FETCH FIRST ROW ONLY";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";
						}
						for (int a = 0; a < allRICountryNames.size(); ++a) {
							insertSql += " INSERT INTO Releaseddates ( ReleaseCountry , ReleaseDate , ClipID ) ";
							insertSql += " VALUES ( '" + allRICountryNames.get(a).getText().replace("'", "''") + "', TO_DATE('"
									+ allRIReleaseDate.get(a).getText().replace("'", "''") + "','dd.MM.YYYY') ," + clipID + " ) ";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";

						}

						for (int a = 0; a < allLinkClip.size(); ++a) {
							insertSql += " INSERT INTO Link ( linkType , ClipTo , ClipFrom ) ";
							insertSql += " VALUES ( '" + allLinkType.get(a).getText().replace("'", "''") + "' , "
									+ allLinkClip.get(a).getText().replace("'", "''") + " , " + clipID + " ) ";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";
						}

						for (int a = 0; a < allCountries.size(); ++a) {

							insertSql += "INSERT INTO countries (countryname) " + "	SELECT * FROM (SELECT '"
									+ allCountries.get(a).getText().replace("'", "''") + "' from dual) tmp "
									+ "WHERE NOT EXISTS (SELECT countryname " + "					FROM countries "
									+ "					WHERE countryname = '" + allCountries.get(a).getText().replace("'", "''") + "')"
									+ "FETCH FIRST ROW ONLY";
							con.createStatement().executeQuery(insertSql);
							insertSql = "";

						}

						for (int a = 0; a < allCountries.size(); ++a) {
							insertSql += " INSERT INTO ReceivedParticipationFrom ( CountryName , ClipID ) ";
							insertSql += " VALUES ( '" + allCountries.get(a).getText().replace("'", "''") + "' ," + clipID + " ) ";

						}

						System.out.println(insertSql);
						if(!insertSql.equals(""))
							con.createStatement().executeQuery(insertSql);

						// People tables

						for (int i = 0; i < Integer.parseInt(peopleNb.getText()); ++i) {
							String personID = "";
							if(!allPNames.get(i).getText().equals("")) {
								String queryClip = "Select PERSONID from PEOPLE where fullname = '" + allPNames.get(i).getText().replace("'", "''") +"'";
								ResultSet rspid =con.createStatement().executeQuery(queryClip);
								if(!rspid.isBeforeFirst()) {
									String insertSql2 = " INSERT INTO People (PersonID,FullName) ";
									insertSql2 += " VALUES ( (SELECT MAX ( PersonID ) From People ) + 1 , '"
											+ allPNames.get(i).getText().replace("'", "''") + "' ) ";
									con.createStatement().executeQuery(insertSql2);
		
									System.out.println(insertSql2);
									String personIDQuery = "(SELECT MAX ( PersonID ) AS MAXID From People )";
									ResultSet rsPerson = con.createStatement().executeQuery(personIDQuery);		
									while (rsPerson.next()) {
										personID = String.valueOf(rsPerson.getInt("MAXID"));
									}
								} else {
									while(rspid.next()) {
										personID=rspid.getString("PERSONID");
									}
									
								}
							}

							// DirectedRole insert
							for (int j = 0; j < Integer.parseInt(directedNb.getText()); ++j) {

								int directedRoleID = -1;
								String queryDirected = "SELECT DIRECTEDID FROM DIRECTEDROLE WHERE ";
								String queryDirectedInsert = "";

								if (allPDirectedAddInfo.get(i).get(j).getText().isEmpty()) {
									queryDirected += "AddInfos IS NULL AND ";
								} else {
									queryDirected += "AddInfos = '" + allPDirectedAddInfo.get(i).get(j).getText().replace("'", "''")
											+ "' AND ";
								}

								if (allPDirectedRoles.get(i).get(j).getText().isEmpty()) {
									queryDirected += "Roles IS NULL";
								} else {
									queryDirected += "Roles = '" + allPDirectedRoles.get(i).get(j).getText().replace("'", "''") + "'";
								}
								System.out.println(queryDirected);
								ResultSet rsD = con.createStatement().executeQuery(queryDirected);
								if (!rsD.isBeforeFirst()) {
									ResultSet rsNew = con.createStatement()
											.executeQuery("SELECT MAX ( DirectedID )+1 AS MAXID From DirectedRole");
									while (rsNew.next()) {
										directedRoleID = rsNew.getInt("MAXID");
									}
									
									queryDirectedInsert += " INSERT INTO DIRECTEDROLE (Roles,AddInfos,DirectedID) ";
									queryDirectedInsert += " VALUES ('" + allPDirectedRoles.get(i).get(j).getText().replace("'", "''") + "','"
											+ allPDirectedAddInfo.get(i).get(j).getText().replace("'", "''") + "'," + directedRoleID + ")";
									System.out.println(queryDirectedInsert);
									con.createStatement().executeQuery(queryDirectedInsert);
									
								} else {
									while (rsD.next()) {
										directedRoleID = rsD.getInt("DIRECTEDID");
									}
								}

								String queryAllDirected = "";
								queryAllDirected += " INSERT INTO DIRECTED (ClipID,DirectedID,PersonID) ";
								queryAllDirected += "VALUES (" + clipID + "," + directedRoleID + "," + personID + ")";

								System.out.println(queryAllDirected);
								con.createStatement().executeQuery(queryAllDirected);
							}

							// Actedchars insert
							for (int j = 0; j < Integer.parseInt(actedNb.getText()); ++j) {
								int actedCharsID = -1;
								String queryActed = "SELECT ACTEDID FROM ACTEDCHARS WHERE ";
								String queryActedInsert = "";

								if (allPactedAddInfo.get(i).get(j).getText().isEmpty()) {
									queryActed += "AddInfos IS NULL AND ";
								} else {
									queryActed += "AddInfos = '" + allPactedAddInfo.get(i).get(j).getText().replace("'", "''") + "' AND ";
								}

								if (allPactedOrderCredit.get(i).get(j).getText().isEmpty()) {
									queryActed += "OrdersCredit IS NULL AND ";
								} else {
									queryActed += "OrdersCredit = '" + allPactedOrderCredit.get(i).get(j).getText().replace("'", "''")
											+ "' AND ";
								}

								if (allPactedChar.get(i).get(j).getText().isEmpty()) {
									queryActed += "Chars IS NULL";
								} else {
									queryActed += "Chars = '" + allPactedChar.get(i).get(j).getText().replace("'", "''") + "'";
								}
								System.out.println(queryActed);
								ResultSet rsA = con.createStatement().executeQuery(queryActed);
								if (!rsA.isBeforeFirst()) {
									ResultSet rsNew = con.createStatement()
											.executeQuery("(SELECT MAX ( ActedID )+1 AS MAXID From ActedChars)");
									while (rsNew.next()) {
										actedCharsID = rsNew.getInt("MAXID");
									}
									
									queryActedInsert += " INSERT INTO ACTEDCHARS (Chars,AddInfos,OrdersCredit, ActedID) ";
									queryActedInsert += " VALUES ('" + allPactedChar.get(i).get(j).getText() + "','"
											+ allPactedAddInfo.get(i).get(j).getText().replace("'", "''") + "','"
											+ allPactedOrderCredit.get(i).get(j).getText().replace("'", "''") + "'," + actedCharsID + ")";
									System.out.println(queryActedInsert);
									con.createStatement().executeQuery(queryActedInsert);
								} else {
									while (rsA.next()) {
										actedCharsID = rsA.getInt("ACTEDID");
									}
								}

								String queryAllActed = "";
								queryAllActed += " INSERT INTO ACTED (ClipID,DirectedID,PersonID) ";
								queryAllActed += "VALUES (" + clipID + "," + actedCharsID + "," + personID + ")";
								System.out.println(queryAllActed);

							}

							// ProducedRole insert
							for (int j = 0; j < Integer.parseInt(producedNb.getText()); ++j) {

								int producedRoleID = -1;
								String queryProduced = "SELECT PRODUCEDID FROM PRODUCEDROLE WHERE ";
								String queryProducedInsert = "";

								if (allPproducedAddInfo.get(i).get(j).getText().isEmpty()) {
									queryProduced += "AddInfos IS NULL AND ";
								} else {
									queryProduced += "AddInfos = '" + allPproducedAddInfo.get(i).get(j).getText().replace("'", "''")
											+ "' AND ";
								}

								if (allPproducedRoles.get(i).get(j).getText().isEmpty()) {
									queryProduced += "Roles IS NULL";
								} else {
									queryProduced += "Roles = '" + allPproducedRoles.get(i).get(j).getText().replace("'", "''") + "'";
								}
								ResultSet rsD = con.createStatement().executeQuery(queryProduced);
								if (!rsD.isBeforeFirst()) {
									ResultSet rsNew = con.createStatement()
											.executeQuery("(SELECT MAX ( ProducedID ) +1 AS MAXID From ProducedRole)");
									while (rsNew.next()) {
										producedRoleID = rsNew.getInt("MAXID");
									}
									
									queryProducedInsert += " INSERT INTO PRODUCEDROLE (Roles,AddInfos,ProducedID) ";
									queryProducedInsert += " VALUES ('" + allPproducedRoles.get(i).get(j).getText().replace("'", "''") + "','"
											+ allPproducedAddInfo.get(i).get(j).getText().replace("'", "''") + "'," + producedRoleID + ")";
									System.out.println(queryProducedInsert);
									con.createStatement().executeQuery(queryProducedInsert);
									
								} else {
									while (rsD.next()) {
										producedRoleID = rsD.getInt("PRODUCEDID");
									}
								}

								String queryAllProduced = "";
								queryAllProduced += " INSERT INTO PRODUCED(ClipID,ProducedID,PersonID) ";
								queryAllProduced += "VALUES (" + clipID + "," + producedRoleID + "," + personID + ")";
								System.out.println(queryAllProduced);
								con.createStatement().executeQuery(queryAllProduced);
							}

							// Wrote insert
							for (int j = 0; j < Integer.parseInt(writerNb.getText()); ++j) {
								int wroteRolesID = -1;
								String queryWrote = "SELECT WROTEID FROM WROTEROLE WHERE ";
								String queryWroteInsert = "";

								if (allPwriterAddInfo.get(i).get(j).getText().isEmpty()) {
									queryWrote += "AddInfos IS NULL AND ";
								} else {
									queryWrote += "AddInfos = '" + allPwriterAddInfo.get(i).get(j).getText().replace("'", "''") + "' AND ";
								}

								if (allPwriterRole.get(i).get(j).getText().isEmpty()) {
									queryWrote += "Roles IS NULL AND ";
								} else {
									queryWrote += "Roles = '" + allPwriterRole.get(i).get(j).getText().replace("'", "''") + "' AND ";
								}

								if (allPwriterWT.get(i).get(j).getText().isEmpty()) {
									queryWrote += "WorkTypes IS NULL";
								} else {
									queryWrote += "WorkTypes = '" + allPwriterWT.get(i).get(j).getText().replace("'", "''") + "'";
								}
								System.out.println(queryWrote);
								ResultSet rsA = con.createStatement().executeQuery(queryWrote);
								if (!rsA.isBeforeFirst()) {
									ResultSet rsNew = con.createStatement()
											.executeQuery("(SELECT MAX ( WroteID ) +1 AS MAXID From WroteRole)");
									while (rsNew.next()) {
										wroteRolesID = rsNew.getInt("MAXID");
									}
									
									queryWroteInsert += " INSERT INTO WROTEROLE (Roles,AddInfos,WorkTypes, WroteID) ";
									queryWroteInsert += " VALUES ('" + allPwriterRole.get(i).get(j).getText().replace("'", "''") + "','"
											+ allPwriterAddInfo.get(i).get(j).getText().replace("'", "''") + "','"
											+ allPwriterWT.get(i).get(j).getText().replace("'", "''") + "'," + wroteRolesID + ")";
									System.out.println(queryWroteInsert);

									con.createStatement().executeQuery(queryWroteInsert);
									
								} else {
									while (rsA.next()) {
										wroteRolesID = rsA.getInt("WROTEID");
									}
								}

								String queryAllWrote = "";
								queryAllWrote += " INSERT INTO WROTE (ClipID,WroteID,PersonID) ";
								queryAllWrote += "VALUES (" + clipID + "," + wroteRolesID + "," + personID + ")";
								System.out.println(queryAllWrote);
								con.createStatement().executeQuery(queryAllWrote);

							}
							
							if((!allPHeight.get(i).getText().equals("") || !allPDPofBirth.get(i).getText().equals("") || !allPBiography.get(i).getText().equals("") ||
							!allPBiographer.get(i).getText().equals("") || !allPDCofDeath.get(i).getText().equals("") || !allPTrivia.get(i).getText().equals("") ||
							!allPQuotes.get(i).getText().equals("") || !allPTradeMark.get(i).getText().equals(""))) {
								String insertSql3 = "";
	
								insertSql3 += "INSERT INTO Biography (PersonID,DateAndPlaceOfBirth,Height,Biography, biographer,"
										+ "dateandcauseofdeath,trivia,personalQuotes,tradeMArk,wherearetheynow) ";
								insertSql3 += "VALUES (" + personID + ",'" + allPDPofBirth.get(i).getText().replace("'", "''") + "','"
										+ allPHeight.get(i).getText().replace("'", "''") + "','" + allPBiography.get(i).getText().replace("'", "''") + "','"
										+ allPBiographer.get(i).getText().replace("'", "''") + "','" + allPDCofDeath.get(i).getText().replace("'", "''") + "','"
										+ allPTrivia.get(i).getText().replace("'", "''") + "','" + allPQuotes.get(i).getText().replace("'", "''") + "','"
										+ allPTradeMark.get(i).getText().replace("'", "''") + "','" + allPWRTfrom.get(i).getText().replace("'", "''") + "') ";
	
								System.out.println(insertSql3);
								con.createStatement().executeQuery(insertSql3);
							}

						}

						System.out.println("Insert done");

					} catch (Exception f) {
						System.out.println("Error : " + f.getMessage());
					}

				});

				Button deleteBtn = new Button("DELETE");
				deleteBtn.setOnAction((e) -> {

					try {
						
						 System.out.println("	- INTRO TO DATABASE -");
						 Class.forName("oracle.jdbc.driver.OracleDriver");
						 System.out.print("Database Connection : "); Connection con =
						 DriverManager.getConnection(
						 "jdbc:oracle:thin:@//diassrv2.epfl.ch:1521/orcldias.epfl.ch", "DB2018_G35",
						 "DB2018_G35"); System.out.println("OK");
						 
						String clipID = "";
						System.out.println(clipTitle.getText());
						if(!clipTitle.getText().equals("")) {
							ResultSet rsCl = con.createStatement().executeQuery("SELECT CLIPID FROM CLIPS WHERE CLIPS.CLIPTITLE = '"+ clipTitle.getText().replace("'", "''") + "'");
							while(rsCl.next()) {
								clipID = rsCl.getString("CLIPID");
								break;
							}
						}

						String deleteSql = "";
						Boolean needAnd = false;

						for (int a = 0; a < Integer.parseInt(genreNb.getText()) ; ++a) {
							if (!allGenres.get(a).getText().equals("") || !clipID.equals("")) {
								deleteSql += "DELETE FROM Genres ";
								deleteSql += "WHERE ";
								if (!allGenres.get(a).getText().equals("")) {
									deleteSql += "Genre = '" + allGenres.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipID = '" + clipID + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}

						for (int a = 0; a < Integer.parseInt(languageNb.getText()); ++a) {
							if (!allLanguage.get(a).getText().equals("") || !clipID.equals("")) {
								deleteSql += "DELETE FROM Languages ";
								deleteSql += "WHERE ";
								if (!allLanguage.get(a).getText().equals("")) {
									deleteSql += "Language = '" + allLanguage.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipID = '" + clipID + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}

						for (int a = 0; a < Integer.parseInt(RTNb.getText()); ++a) {
							if (!allRTRunningTime.get(a).getText().equals("") || !clipID.equals("")
									|| !allRTCountryNames.get(a).getText().equals("")) {
								deleteSql += "DELETE FROM RunningTimes ";
								deleteSql += "WHERE ";
								if (!allRTRunningTime.get(a).getText().equals("")) {
									deleteSql += "RunningTime = '" + allRTRunningTime.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipID = '" + clipID + "' ";
									needAnd = true;
								}
								if (!allRTCountryNames.get(a).getText().equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ReleaseCountry = '" + allRTCountryNames.get(a).getText() + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}

						for (int a = 0; a < Integer.parseInt(RINb.getText()); ++a) {
							if (!allRICountryNames.get(a).getText().equals("") || !clipID.equals("")
									|| !allRIReleaseDate.get(a).getText().equals("")) {
								deleteSql += "DELETE FROM Releaseddates ";
								deleteSql += "WHERE ";
								if (!allRICountryNames.get(a).getText().equals("")) {
									deleteSql += "CountryName = '" + allRICountryNames.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipID = '" + clipID + "' ";
									needAnd = true;
								}
								if (!allRIReleaseDate.get(a).getText().equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ReleaseDate = '" + allRIReleaseDate.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}

						for (int a = 0; a < Integer.parseInt(linkNb.getText()); ++a) {
							if (!allLinkType.get(a).getText().equals("") || !clipID.equals("")
									|| !allLinkClip.get(a).getText().equals("")) {
								deleteSql += "DELETE FROM Link ";
								deleteSql += "WHERE ";
								if (!allLinkType.get(a).getText().equals("")) {
									deleteSql += "linkType = '" + allLinkType.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipFrom = '" + clipID + "' ";
									needAnd = true;
								}
								if (!allLinkClip.get(a).getText().equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipTo = '" + allLinkClip.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}

						for (int a = 0; a < Integer.parseInt(countryNb.getText()); ++a) {
							if (!allCountries.get(a).getText().equals("") || !clipID.equals("")) {
								deleteSql += "DELETE FROM ReceivedPArticipationFrom ";
								deleteSql += "WHERE ";
								if (!allCountries.get(a).getText().equals("")) {
									deleteSql += "ReleaseCountry = '" + allCountries.get(a).getText().replace("'", "''") + "' ";
									needAnd = true;
								}
								if (!clipID.equals("")) {
									if (needAnd)
										deleteSql += " AND ";
									deleteSql += "ClipID = '" + clipID + "' ";
									needAnd = true;
								}
								needAnd = false;
							}
						}
						
						if (!rating.getText().equals("") || !votes.getText().equals("")) {
							deleteSql += "DELETE FROM Ratings ";
							deleteSql += "WHERE ";
							if (!rating.getText().equals("")) {
								deleteSql += "Rank = '" + rating.getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!clipID.equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "ClipID = '" + clipID + "' ";
								needAnd = true;
							}
							if (!votes.getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "Votes = '" + votes.getText() + "' ";
								needAnd = true;
							}
							needAnd = false;
						}
						
						System.out.println(deleteSql);
						if (!deleteSql.equals(""))
							con.createStatement().executeQuery(deleteSql);
						deleteSql = "";
						
						// -- people
						deleteSql = "";
						
						String personID = "";
						for (int i = 0; i < Integer.parseInt(peopleNb.getText()); ++i) {
							if(!allPNames.get(i).getText().equals("")) {
								ResultSet rsPi = con.createStatement().executeQuery("SELECT PERSONID FROM PEOPLE WHERE FULLNAME = '"+allPNames.get(i).getText().replace("'", "''") + "'");
								while(rsPi.next()) {
									personID = rsPi.getString("PERSONID");
								}
						}
						
						for(int j = 0; j < Integer.parseInt(producedNb.getText()); ++j) {
							if(!allPproducedAddInfo.get(i).get(j).getText().isEmpty() || !allPproducedRoles.get(i).get(j).getText().isEmpty()) {
								String deleteJobID = "SELECT from producedrole where";
								String producedID = "";
								
								if (!allPproducedAddInfo.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPproducedAddInfo.get(i).get(j).getText().replace("'", "''") + " AND ";
								}
								if (!allPproducedAddInfo.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPproducedRoles.get(i).get(j).getText().replace("'", "''");
								}
								ResultSet rs = con.createStatement().executeQuery(deleteJobID);
								
								while(rs.next()) {
									producedID = rs.getString("PRODUCEDID");
								}
								deleteSql = "DELETE FROM PRODUCED WHERE producedID = "+producedID+ " AND clipid = "+ clipID +" AND personid = " + personID;
								deleteJobID=deleteJobID.replace("SELECT","DELETE");
								
								con.createStatement().executeQuery(deleteSql);
								
								con.createStatement().executeQuery(deleteJobID);
								System.out.println(deleteJobID);
								System.out.println(deleteSql);
								deleteJobID = "";
							} else {
								deleteSql = "DELETE FROM PRODUCED WHERE personid = "+personID+ " AND clipid = "+ clipID;
								con.createStatement().executeQuery(deleteSql);
								

								System.out.println(deleteSql);		
							}
							deleteSql = "";
						}
						
						for(int j = 0; j < Integer.parseInt(directedNb.getText()); ++j) {
							if(!allPDirectedAddInfo.get(i).get(j).getText().isEmpty() || !allPDirectedRoles.get(i).get(j).getText().isEmpty()) {
								String deleteJobID = "SELECT from directedrole where";
								String directedID = "";
								
								if (!allPDirectedAddInfo.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPDirectedAddInfo.get(i).get(j).getText().replace("'", "''") + " AND ";
								}
								if (!allPDirectedRoles.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPDirectedRoles.get(i).get(j).getText().replace("'", "''");
								}
								ResultSet rs = con.createStatement().executeQuery(deleteJobID);
								
								while(rs.next()) {
									directedID = rs.getString("DIRECTEDID");
								}
								deleteSql = "DELETE FROM DIRECTED WHERE DIRECTEDID = "+ directedID + " AND clipid = "+ clipID +" AND personid = " + personID;
								deleteJobID=deleteJobID.replace("SELECT","DELETE");
								
								con.createStatement().executeQuery(deleteSql);
								
								con.createStatement().executeQuery(deleteJobID);
								System.out.println(deleteJobID);
								System.out.println(deleteSql);
								deleteJobID = "";
							} else {
								deleteSql = "DELETE FROM DIRECTED WHERE personid = "+ personID + " AND clipid = "+ clipID;
								con.createStatement().executeQuery(deleteSql);
								
								System.out.println(deleteSql);		
							}
							deleteSql = "";
						}
						
						for(int j = 0; j < Integer.parseInt(actedNb.getText()); ++j) {
							if(!allPactedAddInfo.get(i).get(j).getText().isEmpty() || !allPactedChar.get(i).get(j).getText().isEmpty()
								|| !allPactedOrderCredit.get(i).get(j).getText().isEmpty()) {
								String deleteJobID = "SELECT from actedchars where";
								String actedID = "";
								
								if (!allPactedAddInfo.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPactedAddInfo.get(i).get(j).getText().replace("'", "''") + " AND ";
								}
								if (!allPactedChar.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPactedChar.get(i).get(j).getText().replace("'", "''");
								}
								if (!allPactedOrderCredit.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPactedOrderCredit.get(i).get(j).getText().replace("'", "''");
								}
								ResultSet rs = con.createStatement().executeQuery(deleteJobID);
								
								while(rs.next()) {
									actedID = rs.getString("ACTEDID");
								}
								deleteSql = "DELETE FROM ACTED WHERE ACTEDID = "+ actedID + " AND clipid = "+ clipID +" AND personid = " + personID;
								deleteJobID=deleteJobID.replace("SELECT","DELETE");
								
								con.createStatement().executeQuery(deleteSql);
								con.createStatement().executeQuery(deleteJobID);
								System.out.println(deleteJobID);
								System.out.println(deleteSql);
								deleteJobID = "";
							} else {
								deleteSql = "DELETE FROM ACTED WHERE personid = "+ personID + " AND clipid = "+ clipID;
								con.createStatement().executeQuery(deleteSql);
								
								System.out.println(deleteSql);		
							}
							deleteSql = "";
						}
						
						for(int j = 0; j < Integer.parseInt(writerNb.getText()); ++j) {
							if(!allPwriterAddInfo.get(i).get(j).getText().isEmpty() || !allPwriterWT.get(i).get(j).getText().isEmpty()
								|| !allPwriterRole.get(i).get(j).getText().isEmpty()) {
								String deleteJobID = "SELECT from wroterole where";
								String wroteID = "";
								
								if (!allPwriterAddInfo.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPwriterAddInfo.get(i).get(j).getText() + " AND ";
								}
								if (!allPwriterWT.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPwriterWT.get(i).get(j).getText();
								}
								if (!allPwriterRole.get(i).get(j).getText().isEmpty()) {
									deleteJobID += allPwriterRole.get(i).get(j).getText();
								}
								ResultSet rs = con.createStatement().executeQuery(deleteJobID);
								
								while(rs.next()) {
									wroteID = rs.getString("WROTEID");
								}
								deleteSql = "DELETE FROM WROTE WHERE WROTEID = "+ wroteID + " AND clipid = "+ clipID +" AND personid = " + personID;
								deleteJobID=deleteJobID.replace("SELECT","DELETE");
								
								System.out.println(deleteJobID);
								System.out.println(deleteSql);
								
								con.createStatement().executeQuery(deleteSql);
								con.createStatement().executeQuery(deleteJobID);

								deleteJobID = "";
							} else {
								deleteSql = "DELETE FROM WROTE WHERE personid = "+ personID + " AND clipid = "+ clipID;
								System.out.println(deleteSql);
								con.createStatement().executeQuery(deleteSql);				
							}
							deleteSql = "";
						}
							
						if(!allPHeight.get(i).getText().equals("") || !allPDPofBirth.get(i).getText().equals("") || !allPBiography.get(i).getText().equals("") ||
							!allPBiographer.get(i).getText().equals("") || !allPDCofDeath.get(i).getText().equals("") || !allPTrivia.get(i).getText().equals("") ||
							!allPQuotes.get(i).getText().equals("") || !allPTradeMark.get(i).getText().equals("")) { 
							deleteSql += "DELETE FROM Biography ";
							deleteSql += "WHERE ";
							if (!allPHeight.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "Height = '" + allPHeight.get(i).getText() + "' ";
								needAnd = true;
							}
							if (!allPDPofBirth.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "DateAndPlaceOfBirth = '" + allPDPofBirth.get(i).getText() + "' ";
								needAnd = true;
							}
							if (!allPBiography.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "dbms_lob.substr(Biography,4000) = '" + allPBiography.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!allPBiographer.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "Biographer = '" + allPBiographer.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!allPDCofDeath.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "DateAndCauseOfDeath = '" +  allPDCofDeath.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!allPTrivia.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "dbms_lob.substr(Trivia,4000) = '" + allPTrivia.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!allPQuotes.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "dbms_lob.substr(PersonalQuotes,4000) = '" + allPQuotes.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if (!allPTradeMark.get(i).getText().equals("")) {
								if (needAnd)
									deleteSql += " AND ";
								deleteSql += "dbms_lob.substr(Trademark,4000) = '" + allPTradeMark.get(i).getText().replace("'", "''") + "' ";
								needAnd = true;
							}
							if(!allPNames.get(i).getText().equals("")) {
								if(needAnd)
									deleteSql += " AND ";
								deleteSql += "PersonID = " + personID; 
							}
							System.out.println(deleteSql);
							con.createStatement().executeQuery(deleteSql);
						}
						
						if(allDeletePerson.get(i).isSelected()) {
							if (!allPNames.get(i).getText().equals("")) {
								deleteSql += "DELETE FROM People WHERE FULLNAME = '" + allPNames.get(i).getText() +"'";
								con.createStatement().executeQuery(deleteSql);
								deleteSql = "";
							}
						}
						
					}
					
					if ((!clipTitle.getText().equals("") || !clipType.getText().equals("")
						|| !clipYear.getText().equals("")) && deleteClip.isSelected()) {
						deleteSql += "DELETE FROM Clips ";
						deleteSql += "WHERE ";
						if (!clipTitle.getText().equals("")) {
							deleteSql += "ClipTitle = '" + clipTitle.getText().replaceAll("'", "''") + "' ";
							needAnd = true;
						}
						if (!clipType.getText().equals("")) {
							if (needAnd)
								deleteSql += " AND ";
							deleteSql += "ClipType = '" + clipType.getText() + "' ";
							needAnd = true;
						}
						if (!clipYear.getText().equals("")) {
							if (needAnd)
								deleteSql += " AND ";
							deleteSql += "ClipYear = TO_DATE('" + clipYear.getText() + "','YYYY') ";
							needAnd = true;
						}
						needAnd = false;
					}
					
					System.out.println(deleteSql);
					if (!deleteSql.equals(""))
						con.createStatement().executeQuery(deleteSql);
						
					System.out.println("Deletion done");
						
					} catch (Exception f) {
						System.out.println("Error : " + f.getMessage());
					}

				});

				GridPane grouper = new GridPane();
				grouper.add(insertBtn, 0, 1);
				grouper.add(deleteBtn, 1, 1);
				// deleteClip defined in top of function
				grouper.add(deleteClip, 2, 1);
				grouper.setHgap(10);

				insDel.add(grouper, 0, 15, 2, 1);

				insDel.setHgap(3);
				insDel.setVgap(10);
				insDel.setPadding(new Insets(10, 10, 10, 10));
				// Main Scene
				Scene scene = new Scene(insDel);
				resultStage.setScene(scene);
				resultStage.getIcons().add(emoji);
				resultStage.show();
			}

		});

		grid.add(new Label("Advanced settings : "), 0, 15, 2, 1);
		
		
		Button gSearch = new Button("SEARCH");
		gSearch.setOnAction(e -> {
			
			Stage searchStage = new Stage();
			
			HBox divider = new HBox();
			
			
			// ---------- clips
			GridPane clipsPane = new GridPane();
			clipsPane.setHgap(3);
			clipsPane.setVgap(10);
			clipsPane.setPadding(new Insets(10, 10, 10, 10));
			
			
			Button clipSearch = new Button("SEARCH");
			TextField clipText = new TextField(); clipText.setPromptText("Clip Title");
			
			clipsPane.add(clipText, 0, 0);
			clipsPane.add(clipSearch, 0, 1);
			
			CheckBox genreC = new CheckBox("genre");
			CheckBox languageC = new CheckBox("language");
			CheckBox RunningTimeC = new CheckBox("runningT");
			CheckBox ReleasedDatesC = new CheckBox("releaseD");
			CheckBox directorC = new CheckBox("director");
			CheckBox writerC = new CheckBox("writer");
			CheckBox actorC = new CheckBox("actor");
			CheckBox producerC = new CheckBox("producer");
			CheckBox ratingsC = new CheckBox("ratings");
			CheckBox linkC = new CheckBox("links");
			
			TilePane tClip = new TilePane();tClip.setMaxWidth(170); 
			tClip.setTileAlignment(Pos.TOP_LEFT);tClip.setHgap(8);
			tClip.getChildren().addAll(genreC,languageC,RunningTimeC,ReleasedDatesC
					,directorC,writerC,actorC,producerC,ratingsC,linkC);
			clipsPane.add(tClip,0,2);
			
			clipSearch.setOnAction(clipEventSearch -> {						 
				String sqlQueryClips1 = "WITH TEMP AS (select * "
										+ "from clips "
										+ "where cliptitle LIKE '" + clipText.getText() + "%') ";
				String sqlQueryClips2 = "select TEMP.clipid, TEMP.cliptitle, TEMP.clipyear, TEMP.cliptype ";
				String sqlQueryClips3 = "";
				if(genreC.isSelected()) {
					sqlQueryClips2 += ",genres.genre";
					sqlQueryClips3 += " LEFT JOIN genres ON genres.clipid = TEMP.clipid";
				}
				if(languageC.isSelected()) {
					sqlQueryClips2 += ",languages.language";
					sqlQueryClips3 += " LEFT JOIN languages ON languages.clipid = TEMP.clipid";
				}
				if(RunningTimeC.isSelected()) {
					sqlQueryClips2 += ",runningtimes.releasecountry,runningtimes.runningtime";
					sqlQueryClips3 += " LEFT JOIN runningtimes ON runningtimes.clipid = TEMP.clipid";
				}
				if(ReleasedDatesC.isSelected()) {
					sqlQueryClips2 += ",releaseddates.releasecountry,releaseddates.releasedate";
					sqlQueryClips3 += " LEFT JOIN releaseddates ON releaseddates.clipid = TEMP.clipid";
				}
				if(producerC.isSelected()) {
					sqlQueryClips2 += ",people.fullname,producedrole.roles, producedrole.addinfos";
					sqlQueryClips3 += " left join produced on produced.clipid = temp.clipid"
									+ " left join people on produced.personid = people.personid"
									+ " left join producedrole on produced.producedid = producedrole.producedid";
				}
				if(writerC.isSelected()) {
					sqlQueryClips2 += ",people.fullname,wroterole.roles,wroterole.worktypes,wroterole.addinfos";
					sqlQueryClips3 += " left join wrote on wrote.clipid = temp.clipid"
									+ " left join people on wrote.personid = people.personid"
									+ " left join wroterole on wrote.wroteid = wroterole.wroteid";
				}
				if(directorC.isSelected()) {
					sqlQueryClips2 += ",people.fullname,directedrole.roles,directedrole.addinfos";
					sqlQueryClips3 += " left join directed on directed.clipid = temp.clipid"
									+ " left join people on directed.personid = people.personid"
									+ " left join directedrole on directed.directedid = directedrole.directedid";
				}
				if(actorC.isSelected()) {
					sqlQueryClips2 += ",people.fullname,actedchars.chars,actedchars.addinfos,actedchars.orderscredit";
					sqlQueryClips3 += " left join acted on acted.clipid = temp.clipid"
									+ " left join people on acted.personid = people.personid"
									+ " left join actedchars on acted.actedid = actedchars.actedid";
				}
				if(ratingsC.isSelected()) {
					sqlQueryClips2 += ",ratings.rank,ratings.votes";
					sqlQueryClips3 += " LEFT JOIN releaseddates ON releaseddates.clipid = TEMP.clipid";
				}
				if(linkC.isSelected()) {
					sqlQueryClips2 += ",link.clipto,link.linktype";
					sqlQueryClips3 += " LEFT JOIN link ON link.clipfrom = TEMP.clipid";
				}
				
				
				String sqlQueryClips = sqlQueryClips1 + sqlQueryClips2+ " FROM TEMP " + sqlQueryClips3 + " order by clipid asc";
				
				System.out.println(sqlQueryClips);
				
				Stage resultStage = new Stage();
				resultStage.setTitle("Result");
				resultStage.setX(mainStage.getX() + mainStage.getWidth());
				
				// TableView
				tableview = new TableView();
				buildData(sqlQueryClips);

				// Main Scene
				Scene scene = new Scene(tableview);

				resultStage.setScene(scene);
				resultStage.getIcons().add(emoji);
				resultStage.show();
			});	
			// ----------- person
			
			GridPane personPane = new GridPane();
			personPane.setHgap(3);
			personPane.setVgap(10);
			personPane.setPadding(new Insets(10, 10, 10, 10));
			
			Button personSearch = new Button("SEARCH");
			TextField personText = new TextField();personText.setPromptText("Person Name");
			
			personPane.add(personText, 0, 0);
			personPane.add(personSearch, 0, 1);
			
			TilePane tPerson = new TilePane();tPerson.setMaxWidth(170); 
			tPerson.setTileAlignment(Pos.TOP_LEFT);tPerson.setHgap(8);
			CheckBox biographyP = new CheckBox("biography");
			CheckBox nickNameP = new CheckBox("nickName");
			CheckBox spouseP = new CheckBox("spouse");
			CheckBox bioBooksP = new CheckBox("bioBooks");
			CheckBox salaryP = new CheckBox("salary");
			CheckBox directorP = new CheckBox("director");
			CheckBox writerP = new CheckBox("writer");
			CheckBox actorP = new CheckBox("actor");
			CheckBox producerP = new CheckBox("producer");
			
			tPerson.getChildren().addAll(biographyP,nickNameP,spouseP,bioBooksP,salaryP
					,directorP,writerP,actorP,producerP);
			
			personSearch.setOnAction( personEventSearch  -> {
				
				String sqlQueryPerson1 = "WITH TEMP AS (select * "
										+ "from people "
										+ "where fullname LIKE '" + personText.getText() + "%') ";
				String sqlQueryPerson2 = "select TEMP.personid,TEMP.fullname";
				String sqlQueryPerson3 = "";

				if (biographyP.isSelected()) {
					sqlQueryPerson2 += ",biography.realname,biography.biography,biography.dateandplaceofbirth,biography.height,biography.biographer,"
							+ "biography.dateandcauseofdeath,biography.trivia,biography.personalquotes,biography.wherearetheynow,biography.trademark";
					sqlQueryPerson3 += " LEFT JOIN biography ON biography.personid = TEMP.personid";
				}
				
				if (nickNameP.isSelected()) {
					sqlQueryPerson2 += ",nickname.nickname";
					sqlQueryPerson3 += " LEFT JOIN nickname ON nickname.personid = TEMP.personid";
				}
				
				if (spouseP.isSelected()) {
					sqlQueryPerson2 += ",spouse.spouse";
					sqlQueryPerson3 += " LEFT JOIN spouse ON spouse.personid = TEMP.personid";
				}
				
				if (bioBooksP.isSelected()) {
					sqlQueryPerson2 += ",biographicalbooks.biographicalbooks";
					sqlQueryPerson3 += " LEFT JOIN biographicalbooks ON biographicalbooks.personid = TEMP.personid";
				}
				
				if (salaryP.isSelected()) {
					sqlQueryPerson2 += ",salary.clipname,salary.salary,salary.year";
					sqlQueryPerson3 += " LEFT JOIN salary ON salary.personid = TEMP.personid";
				}
				
				if(actorP.isSelected()) {
					sqlQueryPerson2 += ",clips.cliptitle,actedchars.chars,actedchars.addinfos,actedchars.orderscredit";
					sqlQueryPerson3 += " left join acted on acted.personid = temp.personid"
									+ " left join clips on clips.clipid = acted.clipid"
									+ " left join actedchars on acted.actedid = actedchars.actedid";
				}
				
				if(producerP.isSelected()) {
					sqlQueryPerson2 += ",clips.cliptitle,producedrole.roles, producedrole.addinfos";
					sqlQueryPerson3 += " left join produced on produced.personid = temp.personid"
									+ " left join clips on clips.clipid = produced.personid"
									+ " left join producedrole on produced.producedid = producedrole.producedid";
				}
				if(writerP.isSelected()) {
					sqlQueryPerson2 += ",clips.cliptitle,wroterole.roles,wroterole.worktypes,wroterole.addinfos";
					sqlQueryPerson3 += " left join wrote on wrote.personid = temp.personid"
									+ " left join clips on clips.clipid = wrote.personid"
									+ " left join wroterole on wrote.wroteid = wroterole.wroteid";
				}
				if(directorP.isSelected()) {
					sqlQueryPerson2 += ",clips.cliptitle,directedrole.roles,directedrole.addinfos";
					sqlQueryPerson3 += " left join directed on directed.personid = temp.personid"
									+ " left join clips on clips.clipid = directed.personid"
									+ " left join directedrole on directed.directedid = directedrole.directedid";
				}
				
				String sqlQueryPerson = sqlQueryPerson1 + sqlQueryPerson2 + " FROM TEMP " + sqlQueryPerson3 + " order by temp.personid ASC";
				System.out.println(sqlQueryPerson);
				
				Stage resultStage = new Stage();
				resultStage.setTitle("Result");
				resultStage.setX(mainStage.getX() + mainStage.getWidth());
				// TableView

				tableview = new TableView();
				buildData(sqlQueryPerson);

				// Main Scene
				Scene scene = new Scene(tableview);

				resultStage.setScene(scene);
				resultStage.getIcons().add(emoji);
				resultStage.show();

				
			});
			
			personPane.add(tPerson,0,2);
			
			
			
			
			
			
			divider.getChildren().addAll(clipsPane,personPane);
			searchStage.setScene(new Scene(divider));
			searchStage.getIcons().add(emoji);
			searchStage.show();
			
		});
		
		
		
		GridPane grouper = new GridPane();
		grouper.add(bt, 0, 0);
		grouper.add(gSearch, 1, 0);
		grouper.setHgap(10);
		
		grid.add(grouper, 0, 16, 2, 1);

		// ----------------------------------------------------------------------

		grid.setHgap(3);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		mainStage.setScene(new Scene(grid));
		mainStage.setWidth(230);
		//mainStage.getIcons().add(emoji);
		mainStage.show();

	}

}
