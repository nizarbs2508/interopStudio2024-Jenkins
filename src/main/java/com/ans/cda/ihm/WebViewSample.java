package com.ans.cda.ihm;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.ValueSet;
import org.ini4j.Profile.Section;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.ans.cda.fhir.CodeSystemEntity;
import com.ans.cda.fhir.FhirUtilities;
import com.ans.cda.fhir.ItemEntity;
import com.ans.cda.service.artdecor.ArtDecorService;
import com.ans.cda.service.artdecor.Mutualisation;
import com.ans.cda.service.artdecor.RemoveSch;
import com.ans.cda.service.bom.BomService;
import com.ans.cda.service.control.ControlCdaService;
import com.ans.cda.service.control.XPathLineEvaluator;
import com.ans.cda.service.crossvalidation.CrossValidationService;
import com.ans.cda.service.crossvalidation.SaxonCrossValidator;
import com.ans.cda.service.parametrage.IniFile;
import com.ans.cda.service.parametrage.ParamEntity;
import com.ans.cda.service.parametrage.ParametrageService;
import com.ans.cda.service.parametrage.XmlToGraph;
import com.ans.cda.service.validation.SaxonValidator;
import com.ans.cda.service.validation.ValidationService;
import com.ans.cda.service.xdm.IheXdmService;
import com.ans.cda.service.xdm.IheXdmUtilities;
import com.ans.cda.service.xdm.XdmService;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.ConvertPdfToPdfAB1;
import com.ans.cda.utilities.general.Inutility2;
import com.ans.cda.utilities.general.LocalUtility;
import com.ans.cda.utilities.general.PdfUtility;
import com.ans.cda.utilities.general.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.jfoenix.controls.JFXButton;
import com.spire.xls.CellRange;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;

import ca.uhn.fhir.context.FhirContext;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 * WebViewSample api with JavaFX
 * 
 * @author bensalem Nizar
 */
public class WebViewSample extends Application {
	/**
	 * FILEP
	 */
	private static final String FILEP = "file:/";
	/**
	 * CONTROL
	 */
	private static final String CONTROL = "message.menu.fhir.control";
	/**
	 * FOLDERFOP
	 */
	private static final String FOLDERFOP = "folder.fop";
	/**
	 * ERRORMETA
	 */
	private static final String ERRORMETA = "message.errormeta";
	/**
	 * ERRORCDA
	 */
	private static final String ERRORCDA = "message.errorcda";
	/**
	 * SUCCES
	 */
	private static final String SUCCES = "message.succes";
	/**
	 * TERMINO
	 */
	private static final String TERMINO = "message.menu.fhir.termino";
	/**
	 * SERVER
	 */
	private static final String SERVER = "message.menu.fhir.server";
	/**
	 * FHIRLOG
	 */
	private static final String FHIRLOG = "message.menu.fhir.log";
	/**
	 * FHIRCODESYS
	 */
	private static final String FHIRCODESYS = "message.menu.fhir.code.sys";
	/**
	 * LASTDSELECTED
	 */
	private static final String LASTDSELECTED = "LAST_DIRECTORY_SELECTED";
	/**
	 * REPORTHTML
	 */
	private static final String REPORTHTML = "document_last_report_HTML.html";
	/**
	 * LASTPUSED
	 */
	private static final String LASTPUSED = "LAST-PATH-USED";
	/**
	 * LASTJDVPUSED
	 */
	private static final String LASTJDVPUSED = "LAST-JDV-PATH-USED";
	/**
	 * textAr
	 */
	private final TextArea textAr = new TextArea();
	/**
	 * textArMeta
	 */
	private final TextArea textArMeta = new TextArea();
	/**
	 * textFieldFhir
	 */
	private final TextField textFieldFhir = new TextField();
	/**
	 * SCHCLEANER
	 */
	private static final String SCHCLEANER = "SCH-CLEANER";
	/**
	 * FHIR
	 */
	private static final String FHIR = "FHIR";
	/**
	 * SCHCLEANER
	 */
	private static final String TOOLS = "TOOLS";
	/**
	 * LASTCDAFILE
	 */
	private static final String LASTCDAFILE = "LAST-CDA-FILE";
	/**
	 * MEMORY
	 */
	private static final String MEMORY = "MEMORY";
	/**
	 * API
	 */
	private static final String API = "API";
	/**
	 * NOS
	 */
	private static final String NOS = "NOS";
	/**
	 * LASTMAFILE
	 */
	private static final String LASTMAFILE = "LAST-META-FILE";
	/**
	 * FONTTIME
	 */
	private static final String FONTTIME = "-fx-font-size: 14px;";
	/**
	 * FONTIME2
	 */
	private static final String FONTIME2 = "-fx-background-color: transparent";
	/**
	 * FONTTIME1
	 */
	private static final String FONTTIME1 = "-fx-font-size: 15;-fx-underline: true;";
	/**
	 * EXTXML
	 */
	private static final String EXTXML = "*.xml";
	/**
	 * API-MAPPING
	 */
	private static final String APIMAPPING = "API-MAPPING";
	/**
	 * browser
	 */
	private final WebView browserEngine = new WebView();
	/**
	 * browser
	 */
	private final WebView browserEngine1 = new WebView();
	/**
	 * webEngine
	 */
	private final WebEngine webEngine = Utility.getWebEngine(browserEngine);
	/**
	 * webEngine
	 */
	private final WebEngine webEngine1 = Utility.getWebEngine(browserEngine1);
	/**
	 * textFieldCda
	 */
	private final TextField textFieldCda = new TextField();
	/**
	 * labelCda
	 */
	private final Label labelCda = LocalUtility.labelForValue(() -> LocalUtility.get("message.selectcda"));
	/**
	 * labelMeta
	 */
	private final Label labelMeta = LocalUtility.labelForValue(() -> LocalUtility.get("message.selectmeta"));
	/**
	 * textFieldMeta
	 */
	public TextField textFieldMeta = new TextField();
	/**
	 * view57
	 */
	private final ImageView view57 = new ImageView(Constant.POINT);
	/**
	 * view57
	 */
	private final ImageView view58 = new ImageView(Constant.CIRCLE);
	/**
	 * accordion
	 */
	private final Accordion accordion = new Accordion();
	/**
	 * area
	 */
	private final TextArea area = new TextArea();
	/**
	 * paneArea
	 */
	private final TitledPane paneArea = new TitledPane();
	/**
	 * BORDERSTYLE
	 */
	private static final String BORDERSTYLE = "-fx-box-border: 0px;";
	/**
	 * comboBox
	 */
	private final MFXComboBox<String> comboBox = new MFXComboBox<>();
	/**
	 * buttonFrensh
	 */
	private final Button buttonFrensh = LocalUtility.buttonForKey("button.frensh");
	/**
	 * buttonEnglish
	 */
	private final Button buttonEnglish = LocalUtility.buttonForKey("button.english");
	/**
	 * buttonEspagnol
	 */
	private final Button buttonEspagnol = LocalUtility.buttonForKey("button.espagnol");
	/**
	 * shadow
	 */
	private final InnerShadow shadow = new InnerShadow(8, Color.DARKKHAKI);
	/**
	 * button
	 */
	private final JFXButton button = new JFXButton("");
	/**
	 * filePath
	 */
	private String filePath;
	/**
	 * scaleTransition
	 */
	private final ScaleTransition scaleTransition = new ScaleTransition();
	/**
	 * fadeTransition
	 */
	private final FadeTransition fadeTransition = new FadeTransition();
	/**
	 * listConverted
	 */
	private static List<String> listConverted = new ArrayList<>();
	/**
	 * listSize
	 */
	private static int listSize = 0;
	/**
	 * treeView
	 */
	private TreeView<String> treeView;
	/**
	 * treeViewMeta
	 */
	private TreeView<String> treeViewMeta;
	/**
	 * thirdStage
	 */
	private final Stage thirdStage = new Stage();
	/**
	 * itemsFiltred
	 */
	private static ObservableList<String> itemsFiltred = FXCollections.observableArrayList();
	/**
	 * filteredItems
	 */
	private static FilteredList<String> filteredItems = new FilteredList<>(itemsFiltred, s -> true);
	/**
	 * listViewItems
	 */
	public static ListView<String> listViewItems;
	/**
	 * textAreaInput
	 */
	private static TextArea textAreaInput;
	/**
	 * taskUpdateStage
	 */
	private static Stage taskUpdateStage;
	/**
	 * progress
	 */
	private static ProgressIndicator progress;
	/**
	 * textFieldF
	 */
	private static TextField textFieldF;
	/**
	 * listViewCq
	 */
	private static ListView<String> listViewCq;
	/**
	 * textAreaStat
	 */
	private static TextArea textAreaStat;
	/**
	 * textAreaS
	 */
	private static TextArea textAreaS;
	/**
	 * terStage
	 */
	private static Stage terStage;
	/**
	 * logStage
	 */
	private static Stage logStage;
	/**
	 * servStage
	 */
	private static Stage servStage;
	/**
	 * conStage
	 */
	private static Stage conStage;
	/**
	 * fieldTermino
	 */
	private static String fieldTermino;
	/**
	 * name
	 */
	private static TextField name;
	/**
	 * uri
	 */
	private static TextField uri;
	/**
	 * oid
	 */
	private static TextField oid;
	/**
	 * content
	 */
	private static TextField content;
	/**
	 * numberOfCheckedItems
	 */
	private static int numberOfCheckedItems = 0;
	/**
	 * sDestinationDirectory
	 */
	private String sDestinationDirectory;
	/**
	 * iErrorLevel
	 */
	private int iErrorLevel;
	/**
	 * bAllValid
	 */
	private boolean bAllValid;
	/**
	 * yesClicked
	 */
	private static AtomicBoolean yesClicked = new AtomicBoolean(false);
	/**
	 * /** Logger
	 */
	private static final Logger LOG = Logger.getLogger(WebViewSample.class);

	/**
	 * void main for Javafx launcher Main secondaire de l'application de javaFX
	 * 
	 * @param args
	 */

	public static void main(final String args[]) {
		try {
			// create ingteropStudio folder
			final String interopFolder = Constant.INTEROPFOLDER;
			if (!new File(interopFolder).exists()) {
				new File(interopFolder).mkdirs();
			}
			// create FHIR folder
			final String fhirFolder = Constant.FHIRFOLDER;
			if (!new File(fhirFolder).exists()) {
				new File(fhirFolder).mkdirs();
			}

			final String jsonFolder = Constant.JSONFHIRFOLDER;
			if (!new File(jsonFolder).exists()) {
				new File(jsonFolder).mkdirs();
			}
			// create LOG folder
			final File logDir = new File(Constant.LOGFOLDFER);
			if (!logDir.exists()) {
				logDir.mkdirs();
			}
			final File file = new File(logDir + "\\log.log");
			final long fileSizeInBytes = file.length();
			final double fileSizeInKB = (double) fileSizeInBytes / 1024;
			final double fileSizeInMB = fileSizeInKB / 1024;
			final double fileSizeInGB = fileSizeInMB / 1024;
			if (fileSizeInGB >= 1) {
				file.delete();
				file.createNewFile();
			}
			launch(args);
		} catch (final SecurityException | IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * loading in api
	 */
	public static void runTask(final Stage taskUpdateStage, final ProgressIndicator progress) {
		final Task<Void> longTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				final int max = 100;
				for (int i = 1; i <= max; i++) {
					if (isCancelled()) {
						break;
					}
					updateProgress(i, max);
				}
				return null;
			}
		};
		longTask.setOnSucceeded(new EventHandler<>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				taskUpdateStage.hide();
			}
		});
		progress.progressProperty().bind(longTask.progressProperty());
		taskUpdateStage.show();
		new Thread(longTask).start();
	}

	/**
	 * start stage
	 * 
	 * @param stage
	 */
	@Override
	public void start(final Stage stage) {
		ParametrageService.init();
		if (!Constant.INTEROPINIFILE.exists()) {
			IniFile.init();
		}
		final TextField fieldUrl1 = new TextField();
		final TextField fieldUrl2 = new TextField();
		final TextField fieldUrl3 = new TextField();
		final TextField fieldUrl4 = new TextField();
		final TextField fieldUrl5 = new TextField();
		final TextField fieldUrl6 = new TextField();
		fieldUrl1.setText(ParametrageService.readValueInPropFile("url.valueset"));
		fieldUrl2.setText(ParametrageService.readValueInPropFile("url.codesystem1"));
		fieldUrl3.setText(ParametrageService.readValueInPropFile("url.codesystem2"));
		fieldUrl4.setText(ParametrageService.readValueInPropFile("url.a11"));
		fieldUrl5.setText(ParametrageService.readValueInPropFile("url.x04"));
		fieldUrl6.setText(ParametrageService.readValueInPropFile("url.a04"));
		final DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
		dropShadow.setRadius(15);
		dropShadow.setOffsetX(10);
		dropShadow.setOffsetY(10);
		browserEngine.setOnMouseEntered(e -> browserEngine.setEffect(shadow));
		browserEngine.setOnMouseExited(e -> browserEngine.setEffect(null));
		browserEngine.setOnMouseEntered(e -> browserEngine.setEffect(shadow));
		browserEngine.setOnMouseExited(e -> browserEngine.setEffect(null));
		browserEngine.setOnMouseEntered(e -> browserEngine.setEffect(shadow));
		browserEngine.setOnMouseExited(e -> browserEngine.setEffect(null));
		textFieldCda.setOnMouseEntered(e -> textFieldCda.setEffect(shadow));
		textFieldCda.setOnMouseExited(e -> textFieldCda.setEffect(null));
		textFieldMeta.setOnMouseEntered(e -> textFieldMeta.setEffect(shadow));
		textFieldMeta.setOnMouseExited(e -> textFieldMeta.setEffect(null));

		textFieldCda.promptTextProperty().bind(LocalUtility.createStringBinding("message.prompt.cda"));
		textFieldCda.setFocusTraversable(false);

		textFieldMeta.promptTextProperty().bind(LocalUtility.createStringBinding("message.prompt.meta"));
		textFieldMeta.setFocusTraversable(false);

		final Tooltip tooltipCda = LocalUtility.createBoundTooltip("message.choose.cda");
		textFieldCda.setTooltip(tooltipCda);

		final Tooltip tooltipMeta = LocalUtility.createBoundTooltip("message.choose.meta");
		textFieldMeta.setTooltip(tooltipMeta);

		final Region spacer101 = new Region();
		spacer101.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		HBox.setHgrow(spacer101, Priority.ALWAYS);

		final TextArea textAreaConsole = new TextArea();

		final BackgroundImage backgroundImageFr = new BackgroundImage(
				new Image(WebViewSample.class.getResource("/images/fr.png").toExternalForm()),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		final Background backgroundFr = new Background(backgroundImageFr);
		final DropShadow shadow = new DropShadow(10, Color.GRAY);
		final Glow glow = new Glow(0.5);
		shadow.setInput(glow);
		buttonFrensh.setEffect(shadow);
		buttonFrensh.setOnMouseEntered(e -> buttonFrensh.setEffect(new DropShadow(20, Color.DARKGRAY)));
		buttonFrensh.setOnMouseExited(e -> buttonFrensh.setEffect(shadow));
		buttonFrensh.setBackground(backgroundFr);
		buttonFrensh.setPrefWidth(30);
		buttonFrensh.setPrefHeight(20);
		buttonFrensh.setMinHeight(20);
		buttonFrensh.setMaxHeight(20);
		buttonFrensh.setStyle(Constant.STYLE);
		buttonFrensh.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
		// Tooltip Frensh flag('
		buttonFrensh.setTooltip(LocalUtility.createBoundTooltip("button.tooltip.frensh"));

		buttonFrensh.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					LocalUtility.switchLanguage(Locale.FRENCH);
					if (Constant.BIENVENIDOS.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEFR.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEEN.equals(textAreaConsole.getText())) {
						textAreaConsole.setText(Constant.BIENVENUEFR);
					}
					final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
							.toExternalForm();
					if (Utility.getEngine(browserEngine).getDocument().getBaseURI().endsWith(Constant.INTEROPHTML)) {
						webEngine.load(url);
					} else if (Utility.getEngine(browserEngine).getDocument().getBaseURI()
							.contains("DocumentationIS2022")) {
						final String url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILE)
								.toExternalForm();
						webEngine.load(url1);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
					}
					spacer101.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
				});
			}
		});

		final BackgroundImage backgroundImageEn = new BackgroundImage(
				new Image(WebViewSample.class.getResource("/images/en.png").toExternalForm()),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		final Background backgroundEn = new Background(backgroundImageEn);
		buttonEnglish.setEffect(shadow);
		buttonEnglish.setOnMouseEntered(e -> buttonEnglish.setEffect(new DropShadow(20, Color.DARKGRAY)));
		buttonEnglish.setOnMouseExited(e -> buttonEnglish.setEffect(shadow));
		buttonEnglish.setBackground(backgroundEn);
		buttonEnglish.setPrefWidth(30);
		buttonEnglish.setPrefHeight(20);
		buttonEnglish.setMinHeight(20);
		buttonEnglish.setMaxHeight(20);
		buttonEnglish.setStyle(Constant.STYLE);
		buttonEnglish.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
		// Tooltip Frensh flag
		buttonEnglish.setTooltip(LocalUtility.createBoundTooltip("button.tooltip.english"));
		buttonEnglish.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					LocalUtility.switchLanguage(Locale.ENGLISH);
					if (Constant.BIENVENIDOS.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEFR.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEEN.equals(textAreaConsole.getText())) {
						textAreaConsole.setText(Constant.BIENVENUEEN);
					}
					final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
							.toExternalForm();
					if (Utility.getEngine(browserEngine).getDocument().getBaseURI().endsWith(Constant.INTEROPHTML)) {
						webEngine.load(url);
					} else if (Utility.getEngine(browserEngine).getDocument().getBaseURI()
							.contains("DocumentationIS2022")) {
						final String url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILEEN)
								.toExternalForm();
						webEngine.load(url1);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
					}
					spacer101.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
				});
			}
		});

		final BackgroundImage backgroundImageEs = new BackgroundImage(
				new Image(WebViewSample.class.getResource("/images/es.jpg").toExternalForm()),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(1.0, 1.0, true, true, false, false));
		final Background backgroundEs = new Background(backgroundImageEs);
		buttonEspagnol.setEffect(shadow);
		buttonEspagnol.setOnMouseEntered(e -> buttonEspagnol.setEffect(new DropShadow(20, Color.DARKGRAY)));
		buttonEspagnol.setOnMouseExited(e -> buttonEspagnol.setEffect(shadow));
		buttonEspagnol.setBackground(backgroundEs);
		buttonEspagnol.setPrefWidth(30);
		buttonEspagnol.setPrefHeight(20);
		buttonEspagnol.setMinHeight(20);
		buttonEspagnol.setMaxHeight(20);
		buttonEspagnol.setStyle(Constant.STYLE);
		buttonEspagnol.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
		// Tooltip Espagnol flag
		buttonEspagnol.setTooltip(LocalUtility.createBoundTooltip("button.tooltip.espagnol"));
		buttonEspagnol.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					LocalUtility.switchLanguage(Locale.forLanguageTag("es-ES"));
					if (Constant.BIENVENIDOS.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEFR.equals(textAreaConsole.getText())
							|| Constant.BIENVENUEEN.equals(textAreaConsole.getText())) {
						textAreaConsole.setText(Constant.BIENVENIDOS);
					}
					final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
							.toExternalForm();
					if (Utility.getEngine(browserEngine).getDocument().getBaseURI().endsWith(Constant.INTEROPHTML)) {
						webEngine.load(url);
					} else if (Utility.getEngine(browserEngine).getDocument().getBaseURI()
							.contains("DocumentationIS2022")) {
						final String url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILEES)
								.toExternalForm();
						webEngine.load(url1);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
					}
					spacer101.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
				});
			}
		});

		area.setPrefHeight(400);
		final ImageView imageViewAns = new ImageView();
		final Image ansImage = new Image(WebViewSample.class.getResource("/images/ans001.png").toExternalForm());
		final Glow sepiaTone = new Glow();
		sepiaTone.setLevel(0);
		imageViewAns.setImage(ansImage);
		imageViewAns.setFitHeight(100);
		imageViewAns.setPreserveRatio(true);
		imageViewAns.setSmooth(true);
		imageViewAns.setCursor(Cursor.HAND);
		final Tooltip tooltip = LocalUtility.createBoundTooltip("message.go.to");
		Tooltip.install(imageViewAns, tooltip);
		imageViewAns.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<>() {
			@Override
			public void handle(final MouseEvent event) {
				LocalUtility.browser(Constant.URL);
				event.consume();
			}
		});

		webEngine.setJavaScriptEnabled(true);
		final double wndwWidth = 150.0d;
		final double wndhHeigth = 150.0d;
		progress = new ProgressIndicator();
		progress.setMinWidth(wndwWidth);
		progress.setMinHeight(wndhHeigth);
		progress.setProgress(0.25F);
		final VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10));
		updatePane.setSpacing(5.0d);
		updatePane.setAlignment(Pos.CENTER);
		Utility.getChildrenNode(updatePane).addAll(progress);
		updatePane.setStyle(FONTIME2);
		taskUpdateStage = new Stage(StageStyle.UNDECORATED);
		taskUpdateStage.setScene(new Scene(updatePane, 170, 170));
		// End progressBar
		final VBox vBox = new VBox(10);
		vBox.setMinSize(stage.getMinWidth(), stage.getMinHeight());
		vBox.setStyle("-fx-background-color: transparent;");
		final MenuBar menuBar = new MenuBar();
		final Menu xdmMenu = LocalUtility.menuForKey("message.fonction.xdm");
		final MenuItem xdmMeta = LocalUtility.menuBarForKey("message.fonction.xdm.gen");
		final MenuItem xdmMulti = LocalUtility.menuBarForKey("message.fonction.xdm.allgen");
		final MenuItem xdmArchive = LocalUtility.menuBarForKey("message.fonction.xdm.archive");
		final MenuItem arboItem = LocalUtility.menuBarForKey("message.param.arborescence");
		Utility.getItems(xdmMenu).addAll(xdmMeta, xdmMulti, xdmArchive, arboItem);

		final Menu cdaMenu = LocalUtility.menuForKey("message.control.cda");
		final MenuItem uuidItem = LocalUtility.menuBarForKey("message.control.cda.uuid");
		final MenuItem calculItem = LocalUtility.menuBarForKey("message.control.cda.loinc");
		final MenuItem bioItem = LocalUtility.menuBarForKey("message.control.cda.bio");
		final MenuItem bomItem = LocalUtility.menuBarForKey("message.bom.delete");
		Utility.getItems(cdaMenu).addAll(uuidItem, calculItem, bioItem, bomItem);

		final Menu xpathMenu = LocalUtility.menuForKey("message.xpath");
		final MenuItem xpathItem = LocalUtility.menuBarForKey("message.xpath.cda");
		final MenuItem xpathSItem = LocalUtility.menuBarForKey("message.xpath.cdameta");
		final MenuItem xpathMItem = LocalUtility.menuBarForKey("message.xpath.meta");

		Utility.getItems(xpathMenu).addAll(xpathItem, xpathSItem, xpathMItem);

		final Menu validationMenu = LocalUtility.menuForKey("message.validation");
		final MenuItem validationCdaItem = LocalUtility.menuBarForKey("message.validation.api");
		final MenuItem validAllCdaItem = LocalUtility.menuBarForKey("message.validation.allapi");
		final MenuItem valMetaItem = LocalUtility.menuBarForKey("message.validation.meta");
		final MenuItem crossValItem = LocalUtility.menuBarForKey("message.validation.cross");
		final MenuItem displayItem = LocalUtility.menuBarForKey("message.validation.rapport");
		final MenuItem openItem = LocalUtility.menuBarForKey("message.validation.rapport.open");
		final MenuItem exportItem = LocalUtility.menuBarForKey("message.export.pdf");
		Utility.getItems(validationMenu).addAll(validationCdaItem, validAllCdaItem, valMetaItem, crossValItem,
				displayItem, openItem, exportItem);

		final Menu paramMenu = LocalUtility.menuForKey("message.param");
		final MenuItem paramItem = LocalUtility.menuBarForKey("message.param.path");
		final MenuItem paramMItem = LocalUtility.menuBarForKey("message.param.mapping");
		final MenuItem paramIItem = LocalUtility.menuBarForKey("message.param.ini");
		final MenuItem logItem = LocalUtility.menuBarForKey("message.param.log");
		Utility.getItems(paramMenu).addAll(paramItem, paramMItem, paramIItem, logItem);

		final Menu docMenu = LocalUtility.menuForKey("message.documentation");
		final MenuItem paramcItem = LocalUtility.menuBarForKey("message.documentation.api");
		final MenuItem docItem = LocalUtility.menuBarForKey("message.documentation.readme");
		Utility.getItems(docMenu).addAll(paramcItem, docItem);

		final Menu artDecorMenu = LocalUtility.menuForKey("message.menu.artdecor");
		final MenuItem artDecorCItem = LocalUtility.menuBarForKey("message.artdecor.cleaner");
		final MenuItem statDecorItem = LocalUtility.menuBarForKey("message.artdecor.statistique");
		final MenuItem schItem = LocalUtility.menuBarForKey("message.artdecor.sch");
		Utility.getItems(artDecorMenu).addAll(schItem, artDecorCItem, statDecorItem);

		final Menu linkMenu = LocalUtility.menuForKey("message.menu.link");
		final MenuItem jarItem = LocalUtility.menuBarForKey("message.menu.execute");
		final MenuItem outilItem = LocalUtility.menuBarForKey("message.menu.outil.ans");

		final Menu fhirMenu = LocalUtility.menuForKey("message.menu.fhir");
		final MenuItem moduleFhirItem = LocalUtility.menuBarForKey("message.module.fhir");
		final MenuItem genItem = LocalUtility.menuBarForKey("message.menu.fhir.generate");
		final MenuItem conItem = LocalUtility.menuBarForKey(CONTROL);
		final MenuItem terItem = LocalUtility.menuBarForKey(TERMINO);
		final MenuItem servItem = LocalUtility.menuBarForKey(SERVER);
		final MenuItem logsItem = LocalUtility.menuBarForKey(FHIRLOG);
		final MenuItem generateCsItem = LocalUtility.menuBarForKey(FHIRCODESYS);

		Utility.getItems(fhirMenu).addAll(moduleFhirItem, genItem, conItem, terItem, servItem, logsItem,
				generateCsItem);

		menuBar.getMenus().addAll(validationMenu, xdmMenu, cdaMenu, xpathMenu, artDecorMenu, fhirMenu, linkMenu,
				paramMenu, docMenu);

		genItem.setVisible(false);
		conItem.setVisible(false);
		terItem.setVisible(false);
		servItem.setVisible(false);
		logsItem.setVisible(false);
		generateCsItem.setVisible(false);

		genItem.setDisable(false);
		conItem.setDisable(false);
		terItem.setDisable(false);
		servItem.setDisable(false);
		logsItem.setDisable(false);
		generateCsItem.setDisable(false);

		moduleFhirItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				genItem.fire();
			}
		});

		Utility.getStylesheets(menuBar).add(getClass().getResource(Constant.CSS).toExternalForm());

		final Map<MenuItem, String> listMenuItem = new ConcurrentHashMap<>();
		final Section section = IniFile.read(TOOLS);
		if (section != null) {
			for (final Entry<String, String> entry : section.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				listMenuItem.put(new MenuItem(key), value);
			}
		}

		if (listMenuItem.isEmpty()) {
			Utility.getItems(linkMenu).addAll(outilItem, jarItem);
		} else {
			final SeparatorMenuItem sep = new SeparatorMenuItem();
			Utility.getItems(linkMenu).addAll(outilItem, jarItem, sep);
			for (final Entry<MenuItem, String> entry : listMenuItem.entrySet()) {
				if (entry.getValue().endsWith(Constant.EXEFILE)) {
					new ImageView(Constant.EXE).setEffect(sepiaTone);
					entry.getKey().setGraphic(new ImageView(Constant.EXE));
				} else if (entry.getValue().endsWith(Constant.EXTJARFILE)) {
					new ImageView(Constant.JARFILE).setEffect(sepiaTone);
					entry.getKey().setGraphic(new ImageView(Constant.JARFILE));
				}
				Utility.getItems(linkMenu).addAll(entry.getKey());
				entry.getKey().setOnAction(new EventHandler<>() {
					@Override
					public void handle(final ActionEvent event) {
						if (entry.getValue() != null) {
							if (new File(entry.getValue()).getName().endsWith(Constant.EXTJARFILE)) {
								try {
									final String command[] = { Constant.JAVA, Constant.JARF, entry.getValue() };
									final ProcessBuilder pbuilder = new ProcessBuilder(command);
									pbuilder.redirectErrorStream(true);
									final Process process = pbuilder.start();
									final InputStream istream = process.getInputStream();
									// thread to handle or gobble text sent from input stream
									new Thread(() -> {
										// try with resources
										try (BufferedReader reader = new BufferedReader(
												new InputStreamReader(istream));) {
											String line;
											while ((line = reader.readLine()) != null) {
												LOG.info(line);
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}).start();
									// thread to get exit value from process without blocking
									Thread waitForThread = new Thread(() -> {
										try {
											process.waitFor();
										} catch (final InterruptedException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									});
									waitForThread.start();
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							} else if (new File(entry.getValue()).getName().endsWith(Constant.EXEFILE)) {
								final ProcessBuilder pBuilder = new ProcessBuilder(entry.getValue(), "", "fr");
								pBuilder.redirectErrorStream(true);
								Process process;
								try {
									process = pBuilder.start();
									final InputStream istream = process.getInputStream();
									// thread to handle or gobble text sent from input stream
									new Thread(() -> {
										// try with resources
										try (BufferedReader reader = new BufferedReader(
												new InputStreamReader(istream));) {
											String line;
											while ((line = reader.readLine()) != null) {
												if (reader.readLine() == null) {
													final Alert alert = new Alert(AlertType.ERROR);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(Constant.GUI);
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												}
												LOG.info(line);
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}).start();
									// thread to get exit value from process without blocking
									Thread waitForThread = new Thread(() -> {
										try {
											process.waitFor();
										} catch (final InterruptedException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									});
									waitForThread.start();
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							}
						}
					}
				});
			}
		}

		final HBox hBoxImg = new HBox();
		hBoxImg.setPadding(new Insets(0, 0, 20, 0));
		Utility.getChildrenHNode(hBoxImg).add(imageViewAns);

		hBoxImg.setOnMouseEntered(e -> hBoxImg.setEffect(shadow));
		hBoxImg.setOnMouseExited(e -> hBoxImg.setEffect(null));

		hBoxImg.setStyle("-fx-background-color: white;-fx-border-radius: 5;-fx-border-style: groove;;");
		hBoxImg.setMaxSize(100, 100);
		hBoxImg.setTranslateX(10);

		final JFXButton btn = new JFXButton("");
		btn.setOnMouseEntered(e -> btn.setEffect(shadow));
		btn.setOnMouseExited(e -> btn.setEffect(null));
		btn.setTranslateY(10);
		final ImageView viewb = new ImageView(Constant.ACCEUIL);
		viewb.setEffect(sepiaTone);
		btn.setGraphic(viewb);
		btn.setStyle(Constant.STYLE1);
		btn.setPrefSize(45, 45);
		btn.setMinSize(45, 45);
		btn.setMaxSize(45, 45);
		final Tooltip tooltipbtn = LocalUtility.createBoundTooltip("message.accueil");
		btn.setTooltip(tooltipbtn);

		final Region spacer100 = new Region();
		spacer100.setMaxWidth(10);
		HBox.setHgrow(spacer100, Priority.ALWAYS);

		final Region spacer102 = new Region();
		spacer102.setMaxWidth(5);
		HBox.setHgrow(spacer102, Priority.ALWAYS);

		final Region spacer109 = new Region();
		spacer109.setMinWidth(10);
		HBox.setHgrow(spacer109, Priority.ALWAYS);

		final BorderPane layout = new BorderPane();
		layout.setTop(menuBar);

		final HBox hbAcceuil = new HBox();
		hbAcceuil.setPadding(new Insets(0, 0, 0, 5));
		Utility.getChildren(hbAcceuil).addAll(btn, spacer109, layout);

		final HBox flagBox = new HBox(5, buttonFrensh, buttonEnglish, buttonEspagnol);
		flagBox.setPadding(new Insets(10, 5, 0, 0));
		flagBox.setAlignment(Pos.TOP_RIGHT);

		final HBox vbAcceuil = new HBox();
		Utility.getChildren(vbAcceuil).addAll(hbAcceuil, spacer101, flagBox);

		final FileChooser fileChooser = new FileChooser();
		fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
		Utility.newExtFilter(fileChooser).addAll(new ExtensionFilter("CDA files (*.xml)", EXTXML));

		final FileChooser fileChooser1 = new FileChooser();
		fileChooser1.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
		Utility.newExtFilter(fileChooser1).addAll(new ExtensionFilter("META files (*.xml)", EXTXML));

		final JFXButton button1 = new JFXButton("");
		button1.setOnMouseEntered(e -> button1.setEffect(shadow));
		button1.setOnMouseExited(e -> button1.setEffect(null));
		final ImageView view = new ImageView(Constant.FOLDERPHOTO);
		view.setEffect(sepiaTone);
		button1.setGraphic(view);
		button1.setStyle(Constant.STYLE1);
		button1.setPrefSize(40, 40);
		button1.setMinSize(40, 40);
		button1.setMaxSize(40, 40);
		final Tooltip tooltip1 = LocalUtility.createBoundTooltip("messsage.open.cda");
		button1.setTooltip(tooltip1);

		button1.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String sCheminnn = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sCheminnn).exists()) {
						fileChooser.setInitialDirectory(new File(sCheminnn));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						textFieldCda.setText(file.getAbsolutePath());
						if (file != null) {
							IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
							IniFile.write(LASTCDAFILE, file.getAbsolutePath(), MEMORY);
							IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
						}
					}
				});
			}
		});

		// supprimer BOM CDA
		final JFXButton button01 = new JFXButton("");
		button01.setOnMouseEntered(e -> button01.setEffect(shadow));
		button01.setOnMouseExited(e -> button01.setEffect(null));
		final ImageView view01 = new ImageView(Constant.BOM);
		view01.setEffect(sepiaTone);
		button01.setGraphic(view01);
		button01.setStyle(Constant.STYLE1);
		button01.setPrefSize(40, 40);
		button01.setMinSize(40, 40);
		button01.setMaxSize(40, 40);
		final Tooltip tooltip01 = LocalUtility.createBoundTooltip("message.bom.delete.cda");
		button01.setTooltip(tooltip01);

		button01.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					try {
						if (textFieldCda.getText().isEmpty()) {
							final Alert alert = new Alert(AlertType.ERROR);
							final DialogPane dialogPane = alert.getDialogPane();
							Utility.getStylesheets(dialogPane)
									.add(getClass().getResource(Constant.CSS).toExternalForm());
							Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText(LocalUtility.getString(ERRORCDA));
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						} else {
							BomService.saveAsUTF8WithoutBOM(textFieldCda.getText(), Charset.defaultCharset());
							final Alert alert = new Alert(AlertType.INFORMATION);
							final DialogPane dialogPane = alert.getDialogPane();
							Utility.getStylesheets(dialogPane)
									.add(getClass().getResource(Constant.CSS).toExternalForm());
							Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText(LocalUtility.getString(SUCCES));
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						}
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
				});
			}
		});

		// supprimer BOM META
		final JFXButton button02 = new JFXButton("");
		button02.setOnMouseEntered(e -> button02.setEffect(shadow));
		button02.setOnMouseExited(e -> button02.setEffect(null));
		final ImageView view02 = new ImageView(Constant.BOM);
		view02.setEffect(sepiaTone);
		button02.setGraphic(view02);
		button02.setStyle(Constant.STYLE1);
		button02.setPrefSize(40, 40);
		button02.setMinSize(40, 40);
		button02.setMaxSize(40, 40);
		final Tooltip tooltip02 = LocalUtility.createBoundTooltip("message.bom.delete.meta");
		button02.setTooltip(tooltip02);

		button02.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					try {
						if (textFieldMeta.getText().isEmpty()) {
							final Alert alert = new Alert(AlertType.ERROR);
							final DialogPane dialogPane = alert.getDialogPane();
							Utility.getStylesheets(dialogPane)
									.add(getClass().getResource(Constant.CSS).toExternalForm());
							Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText(LocalUtility.getString(ERRORMETA));
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						} else {
							BomService.saveAsUTF8WithoutBOM(textFieldMeta.getText(), Charset.defaultCharset());
							final Alert alert = new Alert(AlertType.INFORMATION);
							final DialogPane dialogPane = alert.getDialogPane();
							Utility.getStylesheets(dialogPane)
									.add(getClass().getResource(Constant.CSS).toExternalForm());
							Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText(LocalUtility.getString(SUCCES));
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						}
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
				});
			}
		});

		// meta button
		final JFXButton button4 = new JFXButton("");
		button4.setOnMouseEntered(e -> button4.setEffect(shadow));
		button4.setOnMouseExited(e -> button4.setEffect(null));
		final ImageView view4 = new ImageView(Constant.XML);
		view4.setEffect(sepiaTone);
		button4.setGraphic(view4);
		button4.setStyle(Constant.STYLE1);
		button4.setPrefSize(40, 40);
		button4.setMinSize(40, 40);
		button4.setMaxSize(40, 40);
		final Tooltip tooltip4 = LocalUtility.createBoundTooltip("message.open.meta");
		button4.setTooltip(tooltip4);

		button4.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {

					final String file = textFieldMeta.getText();
					if (file != null && !file.isEmpty()) {
						final Desktop desktop = Desktop.getDesktop();
						try {
							desktop.open(new File(file));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString(ERRORMETA));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		// xml button
		final JFXButton button3 = new JFXButton("");
		button3.setOnMouseEntered(e -> button3.setEffect(shadow));
		button3.setOnMouseExited(e -> button3.setEffect(null));
		final ImageView view3 = new ImageView(Constant.XML);
		view3.setEffect(sepiaTone);
		button3.setGraphic(view3);
		button3.setStyle(Constant.STYLE1);
		button3.setPrefSize(40, 40);
		button3.setMinSize(40, 40);
		button3.setMaxSize(40, 40);
		final Tooltip tooltip3 = LocalUtility.createBoundTooltip("message.open.cda.extern");
		button3.setTooltip(tooltip3);
		button3.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String file = textFieldCda.getText();
					if (file != null && !file.isEmpty()) {
						final Desktop desktop = Desktop.getDesktop();
						try {
							desktop.open(new File(file));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString(ERRORCDA));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		// xml button
		final JFXButton buttonPdf = new JFXButton("");
		buttonPdf.setOnMouseEntered(e -> buttonPdf.setEffect(shadow));
		buttonPdf.setOnMouseExited(e -> buttonPdf.setEffect(null));
		final ImageView viewPdf = new ImageView(Constant.EXPORTPDF);
		viewPdf.setEffect(sepiaTone);
		buttonPdf.setGraphic(viewPdf);
		buttonPdf.setStyle(Constant.STYLE1);
		buttonPdf.setPrefSize(40, 40);
		buttonPdf.setMinSize(40, 40);
		buttonPdf.setMaxSize(40, 40);
		final Tooltip tooltipPdf = LocalUtility.createBoundTooltip("message.generate.pdf");
		buttonPdf.setTooltip(tooltipPdf);

		buttonPdf.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty()) {
					final String value = ParametrageService.readValueInPropFile(FOLDERFOP);
					if (value != null && !value.isEmpty()) {
						runTask(taskUpdateStage, progress);
						Platform.runLater(() -> {
							boolean isAutoPdf = false;
							final File file = new File(textFieldCda.getText());
							final File fileXsl = new File(file.getAbsolutePath());
							final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder dbuilder;
							try {
								dbuilder = dbf.newDocumentBuilder();
								final org.w3c.dom.Document doc = dbuilder.parse(fileXsl);
								doc.getDocumentElement().normalize();
								final NodeList nodeList = doc.getChildNodes();
								for (int itr = 0; itr < nodeList.getLength(); itr++) {
									final org.w3c.dom.Node node = nodeList.item(itr);
									if (Constant.STYLESHEET.equals(node.getNodeName())) {
										isAutoPdf = true;
									} else {
										isAutoPdf = false;
									}
								}
							} catch (final IOException | ParserConfigurationException | SAXException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}

							Path pdf = null;
							try {
								pdf = Files.createTempFile(null, Constant.PDFEXT);
							} catch (final IOException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
							final Path pdfFile = pdf;
							File fileCda = null;
							Path tempDirectory = null;
							try {
								final String tempDir = System.getProperty(Constant.TMPDIR);
								if (!isAutoPdf) {
									fileCda = PdfUtility.copyFromJarFile(Constant.CDAFO, new File(tempDir).toPath());
								} else {
									fileCda = file;
								}
								PdfUtility.copyFromJarFile(Constant.CDALN, new File(tempDir).toPath());
								PdfUtility.copyFromJarFile(Constant.CDANARR, new File(tempDir).toPath());

								tempDirectory = Paths.get(value);
							} catch (final IOException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
							// Start DATAMATRIX
							InputStream targetStream;
							Path targetFilePath = null;
							try {
								final Path file1 = Files.createTempFile(null, Constant.HTMLEXT);
								final String tempDir = System.getProperty(Constant.TMPDIR);
								final Path pathDir = new File(tempDir).toPath();
								final File sourceFile = fileCda;
								targetFilePath = pathDir.resolve(sourceFile.getName());
								final Path stream = PdfUtility.transform(sourceFile.getAbsolutePath(),
										textFieldCda.getText(), file1);
								final File url1 = new File(stream.toString());
								targetStream = Files.newInputStream(Paths.get(url1.toURI()));
								final Document doc = Jsoup.parse(targetStream, Constant.UTF8, "");
								final Collection<Element> contents = doc.getElementsByClass("barcodeStyle");
								for (final Element content : contents) {
									if (content != null) {
										final String dataMatrixValue = content.val();
										if (dataMatrixValue != null && !dataMatrixValue.isEmpty()) {
											final Map<EncodeHintType, Object> hints = new ConcurrentHashMap<>();
											hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
											final BitMatrix matrix = new MultiFormatWriter().encode(dataMatrixValue,
													BarcodeFormat.DATA_MATRIX, 200, 200, hints);
											final Path paths = Files.createTempFile(null, Constant.PNGEXT);
											MatrixToImageWriter.writeToPath(matrix, "PNG", paths);
											final Document document = Jsoup.parse(targetFilePath.toFile(), "UTF-8", "",
													Parser.xmlParser());
											final Element externalGraphic = document.getElementById("datamatrixId");
											if (externalGraphic != null) {
												externalGraphic.attr("src", "url('file:///" + paths.toString() + "')");
											}
											final File outputFile = targetFilePath.toFile();
											try (BufferedWriter writer = Files
													.newBufferedWriter(Paths.get(outputFile.toURI()))) {
												writer.write(document.outerHtml());
											}
										}
									}
								}
							} catch (final IOException | WriterException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
							// FIN DATAMATRIX
							if (Files.exists(tempDirectory)) {
								String cmd = "cd " + '"' + tempDirectory.toString() + '"' + " && fop -xml " + '"'
										+ textFieldCda.getText() + '"' + " -xsl " + '"' + targetFilePath.toString()
										+ '"' + " -pdf " + '"' + pdfFile.toString() + '"';
								cmd = cmd.replace("\\", "/");
								final String[] command = new String[3];
								command[0] = Constant.CMD;
								command[1] = Constant.C_SLASH;
								command[2] = cmd;
								final ProcessBuilder pbuilder = new ProcessBuilder();
								pbuilder.command(command);
								Process process;
								try {
									process = pbuilder.start();
									final BufferedReader errStreamReader = new BufferedReader(
											new InputStreamReader(process.getErrorStream()));

									String line = errStreamReader.readLine();
									while (line != null) {
										line = errStreamReader.readLine();
									}
									final String namePDF = new File(textFieldCda.getText()).getName();
									final File basedir = new File(System.getProperty(Constant.TMPDIR));
									if (new File(basedir, Utility.removeExtension(namePDF) + Constant.PDFEXT)
											.exists()) {
										new File(basedir, Utility.removeExtension(namePDF) + Constant.PDFEXT).delete();
									}
									File pdfFinal = new File(basedir,
											Utility.removeExtension(namePDF) + Constant.PDFEXT);
									if (pdfFile != null) {
										// rename if file exists
										int index = 1;
										while (pdfFinal.exists()) {
											final String path1 = pdfFinal.getAbsolutePath();
											final int idot = path1.lastIndexOf('.');
											final String path2 = path1.substring(0, idot) + "(" + ++index + ")"
													+ path1.substring(idot);
											pdfFinal = Utility.getFile(path2);
										}
										ConvertPdfToPdfAB1.main(pdfFile.toString(), pdfFinal);
										final File folder = new File(Constant.PDFFOLDER);
										if (!folder.exists()) {
											folder.mkdirs();
										}
										final Path sourcePath = Paths.get(pdfFinal.toString());
										final Path destinationPath = Paths
												.get(folder + "\\" + new File(pdfFinal.toString()).getName());
										Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
										if (folder.exists() && folder.isDirectory()) {
											try {
												final Desktop desktop = Desktop.getDesktop();
												desktop.open(folder);
												if (destinationPath.toFile().exists()) {
													Desktop.getDesktop().open(destinationPath.toFile());
												}
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}
										}
									}
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							}
						});
					} else {
						final Alert alert = new Alert(AlertType.WARNING);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.error.pdf"));
						alert.setHeaderText(null);
						final ButtonType okButtonType = new ButtonType("OK");
						final ButtonType cancelButtonType = new ButtonType(
								LocalUtility.getString("message.alert.cancel"));
						Utility.getButtonTypes(alert).setAll(okButtonType, cancelButtonType);
						alert.showAndWait().ifPresent(response -> {
							if (response.equals(okButtonType)) {
								paramItem.fire();
								thirdStage.setOnCloseRequest(ev -> {
									runTask(taskUpdateStage, progress);
									final String valueF = ParametrageService.readValueInPropFile(FOLDERFOP);
									Platform.runLater(() -> {
										boolean isAutoPdf = false;
										final File file = new File(textFieldCda.getText());
										final File fileXsl = new File(file.getAbsolutePath());
										final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
										DocumentBuilder dbuilder;
										try {
											dbuilder = dbf.newDocumentBuilder();
											final org.w3c.dom.Document doc = dbuilder.parse(fileXsl);
											doc.getDocumentElement().normalize();
											final NodeList nodeList = doc.getChildNodes();
											for (int itr = 0; itr < nodeList.getLength(); itr++) {
												final org.w3c.dom.Node node = nodeList.item(itr);
												if (Constant.STYLESHEET.equals(node.getNodeName())) {
													isAutoPdf = true;
												} else {
													isAutoPdf = false;
												}
											}
										} catch (final IOException | ParserConfigurationException | SAXException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}

										Path pdf = null;
										try {
											pdf = Files.createTempFile(null, Constant.PDFEXT);
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
										final Path pdfFile = pdf;
										File fileCda = null;
										Path tempDirectory = null;
										try {
											final String tempDir = System.getProperty(Constant.TMPDIR);
											if (isAutoPdf) {
												fileCda = file;
											} else {
												fileCda = PdfUtility.copyFromJarFile(Constant.CDAFO,
														new File(tempDir).toPath());
											}
											PdfUtility.copyFromJarFile(Constant.CDALN, new File(tempDir).toPath());
											PdfUtility.copyFromJarFile(Constant.CDANARR, new File(tempDir).toPath());

											tempDirectory = Paths.get(valueF);
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
										// Start DATAMATRIX
										InputStream targetStream;
										Path targetFilePath = null;
										try {
											final Path file1 = Files.createTempFile(null, Constant.HTMLEXT);
											final String tempDir = System.getProperty(Constant.TMPDIR);
											final Path pathDir = new File(tempDir).toPath();
											final File sourceFile = fileCda;
											targetFilePath = pathDir.resolve(sourceFile.getName());
											final Path stream = PdfUtility.transform(sourceFile.getAbsolutePath(),
													textFieldCda.getText(), file1);
											final File url1 = new File(stream.toString());
											targetStream = Files.newInputStream(Paths.get(url1.toURI()));
											final Document doc = Jsoup.parse(targetStream, Constant.UTF8, "");
											final Collection<Element> contents = doc.getElementsByClass("barcodeStyle");
											for (final Element content : contents) {
												if (content != null) {
													final String dataMatrixValue = content.val();
													if (dataMatrixValue != null && !dataMatrixValue.isEmpty()) {
														final Map<EncodeHintType, Object> hints = new ConcurrentHashMap<>();
														hints.put(EncodeHintType.DATA_MATRIX_SHAPE,
																SymbolShapeHint.FORCE_SQUARE);
														final BitMatrix matrix = new MultiFormatWriter().encode(
																dataMatrixValue, BarcodeFormat.DATA_MATRIX, 200, 200,
																hints);
														final Path paths = Files.createTempFile(null, Constant.PNGEXT);
														MatrixToImageWriter.writeToPath(matrix, "PNG", paths);
														final Document document = Jsoup.parse(targetFilePath.toFile(),
																"UTF-8", "", Parser.xmlParser());
														final Element externalGraphic = document
																.getElementById("datamatrixId");
														if (externalGraphic != null) {
															externalGraphic.attr("src",
																	"url('file:///" + paths.toString() + "')");
														}
														final File outputFile = targetFilePath.toFile();
														try (BufferedWriter writer = Files
																.newBufferedWriter(Paths.get(outputFile.toURI()))) {
															writer.write(document.outerHtml());
														}
													}
												}
											}
										} catch (final IOException | WriterException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
										// FIN DATAMATRIX
										if (Files.exists(tempDirectory)) {
											String cmd = "cd " + '"' + tempDirectory.toString() + '"' + " && fop -xml "
													+ '"' + textFieldCda.getText() + '"' + " -xsl " + '"'
													+ targetFilePath.toString() + '"' + " -pdf " + '"'
													+ pdfFile.toString() + '"';
											cmd = cmd.replace("\\", "/");
											final String[] command = new String[3];
											command[0] = Constant.CMD;
											command[1] = Constant.C_SLASH;
											command[2] = cmd;
											final ProcessBuilder pbuilder = new ProcessBuilder();
											pbuilder.command(command);
											Process process;
											try {
												process = pbuilder.start();
												final BufferedReader errStreamReader = new BufferedReader(
														new InputStreamReader(process.getErrorStream()));

												String line = errStreamReader.readLine();
												while (line != null) {
													line = errStreamReader.readLine();
												}
												final String namePDF = new File(textFieldCda.getText()).getName();
												final File basedir = new File(System.getProperty(Constant.TMPDIR));
												if (new File(basedir,
														Utility.removeExtension(namePDF) + Constant.PDFEXT).exists()) {
													new File(basedir,
															Utility.removeExtension(namePDF) + Constant.PDFEXT)
															.delete();
												}
												File pdfFinal = new File(basedir,
														Utility.removeExtension(namePDF) + Constant.PDFEXT);
												if (pdfFile != null) {
													// rename if file exists
													int index = 1;
													while (pdfFinal.exists()) {
														final String path1 = pdfFinal.getAbsolutePath();
														final int idot = path1.lastIndexOf('.');
														final String path2 = path1.substring(0, idot) + "(" + ++index
																+ ")" + path1.substring(idot);
														pdfFinal = Utility.getFile(path2);
													}
													ConvertPdfToPdfAB1.main(pdfFile.toString(), pdfFinal);
													final File folder = new File(Constant.PDFFOLDER);
													if (!folder.exists()) {
														folder.mkdirs();
													}
													final Path sourcePath = Paths.get(pdfFinal.toString());
													final Path destinationPath = Paths.get(
															folder + "\\" + new File(pdfFinal.toString()).getName());
													Files.copy(sourcePath, destinationPath,
															StandardCopyOption.REPLACE_EXISTING);
													if (folder.exists() && folder.isDirectory()) {
														try {
															final Desktop desktop = Desktop.getDesktop();
															desktop.open(folder);
															if (destinationPath.toFile().exists()) {
																Desktop.getDesktop().open(destinationPath.toFile());
															}
														} catch (final IOException e) {
															if (LOG.isInfoEnabled()) {
																final String error = e.getMessage();
																LOG.error(error);
															}
														}
													}
												}
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}
										}
									});
								});
							} else if (response.equals(cancelButtonType)) {
								alert.close();
							}
						});
					}
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(ERRORCDA));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		final JFXButton button2 = new JFXButton("");
		button2.setOnMouseEntered(e -> button2.setEffect(shadow));
		button2.setOnMouseExited(e -> button2.setEffect(null));
		final ImageView view1 = new ImageView(Constant.FOLDERPHOTO);
		view1.setEffect(sepiaTone);
		button2.setGraphic(view1);
		button2.setStyle(Constant.STYLE1);
		button2.setPrefSize(40, 40);
		button2.setMinSize(40, 40);
		button2.setMaxSize(40, 40);
		final Tooltip tooltip002 = LocalUtility.createBoundTooltip("message.open.metadonne");
		button2.setTooltip(tooltip002);

		button2.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String sCheminn = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sCheminn).exists()) {
						fileChooser1.setInitialDirectory(new File(sCheminn));
					} else {
						fileChooser1.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser1.showOpenDialog(stage);
					if (file != null) {
						textFieldMeta.setText(file.getAbsolutePath());
						if (file != null) {
							IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
							IniFile.write(LASTMAFILE, file.getAbsolutePath(), MEMORY);
							IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
						}
					}
				});
			}
		});

		final ImageView view5 = new ImageView(Constant.VALIDATE);
		view5.setEffect(sepiaTone);
		validationCdaItem.setGraphic(view5);

		final ImageView view99 = new ImageView(Constant.FOLDERV);
		view99.setEffect(sepiaTone);
		validAllCdaItem.setGraphic(view99);

		final DirectoryChooser directoryChooser1 = new DirectoryChooser();
		directoryChooser1.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));

		validAllCdaItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
				if (new File(sChemin).exists()) {
					directoryChooser1.setInitialDirectory(new File(sChemin));
				} else {
					directoryChooser1.setInitialDirectory(new File(Constant.DISK));
				}
				final File selectedDirectory = directoryChooser1.showDialog(stage);
				if (selectedDirectory != null && selectedDirectory.isDirectory()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						webEngine.load(null);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
						final File[] liste = selectedDirectory.listFiles();
						String consoleAll = "";
						int count = 0;
						for (final File item : liste) {
							count++;
							if (item.isFile() && item.getName().endsWith(Constant.EXTXML)) {
								String console = ValidationService.validateCda(item, Constant.MODELCDA,
										Constant.URLVALIDATION, Constant.API, null);
								ValidationService.displayLastReport();
								final String filePath = SaxonValidator.getNewFilePath().toFile().getParent();
								final String pathParent = item.getParentFile().getAbsolutePath();
								final File valid = ValidationService.createValidFolder(new File(pathParent));
								final File invalid = ValidationService.createInValidFolder(new File(pathParent));
								if (!console.contains(LocalUtility.getString("message.error.server"))
										&& !console.contains("FAILED")) {
									console = console.replace(filePath, valid.getAbsolutePath());
									consoleAll = consoleAll.concat("\n" + console);
									final File file = new File(
											valid + "\\" + ValidationService.getNameWithoutExtension(item.getName()));
									ValidationService.createFolder(file);
									final File fileP = SaxonValidator.getNewFilePath().toFile();
									final File fileP1 = SaxonValidator.getNewFilePath1().toFile();
									final File fileP2 = SaxonValidator.getNewFilePath2().toFile();
									final File fileP3 = SaxonValidator.getNewFilePath3().toFile();
									final File fileP4 = SaxonValidator.getNewFilePath4().toFile();
									try {
										if (fileP.exists()) {
											FileUtils.copyFile(fileP, new File(file + "\\" + fileP.getName()));
											if (count < liste.length) {
												fileP.delete();
											}
										}
										if (fileP1.exists()) {
											FileUtils.copyFile(fileP1, new File(file + "\\" + fileP1.getName()));
											if (count < liste.length) {
												fileP1.delete();
											}
										}
										if (fileP2.exists()) {
											FileUtils.copyFile(fileP2, new File(file + "\\" + fileP2.getName()));
											if (count < liste.length) {
												fileP2.delete();
											}
										}
										if (fileP3.exists()) {
											FileUtils.copyFile(fileP3, new File(file + "\\" + fileP3.getName()));
											if (count < liste.length) {
												fileP3.delete();
											}
										}
										if (fileP4.exists()) {
											FileUtils.copyFile(fileP4, new File(file + "\\" + fileP4.getName()));
											if (count < liste.length) {
												fileP4.delete();
											}
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								} else {
									final File file = new File(
											invalid + "\\" + ValidationService.getNameWithoutExtension(item.getName()));
									console = console.replace(filePath, invalid.getAbsolutePath());
									consoleAll = consoleAll.concat("\n" + console);
									ValidationService.createFolder(file);
									final File fileP = SaxonValidator.getNewFilePath().toFile();
									final File fileP1 = SaxonValidator.getNewFilePath1().toFile();
									final File fileP2 = SaxonValidator.getNewFilePath2().toFile();
									final File fileP3 = SaxonValidator.getNewFilePath3().toFile();
									final File fileP4 = SaxonValidator.getNewFilePath4().toFile();
									try {
										if (fileP.exists()) {
											FileUtils.copyFile(fileP, new File(file + "\\" + fileP.getName()));
											if (count < liste.length) {
												fileP.delete();
											}
										}
										if (fileP1.exists()) {
											FileUtils.copyFile(fileP1, new File(file + "\\" + fileP1.getName()));
											if (count < liste.length) {
												fileP1.delete();
											}
										}
										if (fileP2.exists()) {
											FileUtils.copyFile(fileP2, new File(file + "\\" + fileP2.getName()));
											if (count < liste.length) {
												fileP2.delete();
											}
										}
										if (fileP3.exists()) {
											FileUtils.copyFile(fileP3, new File(file + "\\" + fileP3.getName()));
											if (count < liste.length) {
												fileP3.delete();
											}
										}
										if (fileP4.exists()) {
											FileUtils.copyFile(fileP4, new File(file + "\\" + fileP4.getName()));
											if (count < liste.length) {
												fileP4.delete();
											}
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								}
							}
						}
						textAreaConsole.setText(consoleAll);
						IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
						IniFile.write(LASTPUSED, selectedDirectory.getAbsolutePath(), MEMORY);
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					});
				}
			}
		});

		validationCdaItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						webEngine.load(null);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
						final String console = ValidationService.validateCda(new File(textFieldCda.getText()),
								Constant.MODELCDA, Constant.URLVALIDATION, Constant.API, null);
						final String ret = ValidationService.displayLastReport();
						textAreaConsole.setText(console);
						if (ret.contains(REPORTHTML)) {
							webEngine.load(FILEP + SaxonValidator.getNewFilePath4().toString());
						} else {
							final String url = WebViewSample.class.getClassLoader().getResource(Constant.ERRORIMG)
									.toExternalForm();
							webEngine.load(url);
						}
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}

						if (!Constant.LOGFILE.exists()) {
							try {
								Constant.LOGFILE.createNewFile();
							} catch (final IOException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
						}
						try (BufferedWriter myWriter = Files
								.newBufferedWriter(Paths.get(Constant.LOGFILE.getAbsolutePath()))) {
							myWriter.write(console);
							myWriter.close();
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					});
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(ERRORCDA));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}

		});

		final Region spacer1 = new Region();
		spacer1.setMaxWidth(10);
		HBox.setHgrow(spacer1, Priority.ALWAYS);
		// validate button
		final ImageView view7 = new ImageView(Constant.METADT);
		view7.setEffect(sepiaTone);
		valMetaItem.setGraphic(view7);
		textAreaConsole.setEditable(false);
		textAreaConsole.setText(LocalUtility.getString("message.welcome"));
		valMetaItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldMeta.getText().isEmpty()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						webEngine.load(null);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
						final String console = ValidationService.validateMeta(new File(textFieldMeta.getText()),
								Constant.MODEL, Constant.ASIPXDM, Constant.URLVALIDATION);
						final String ret = ValidationService.displayLastReport();
						textAreaConsole.setText(console);
						if (ret.contains(REPORTHTML)) {
							webEngine.load(FILEP + SaxonValidator.getNewFilePath4().toString());
						} else {
							final String url = WebViewSample.class.getClassLoader().getResource(Constant.ERRORIMG)
									.toExternalForm();
							webEngine.load(url);
						}
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					});
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(ERRORMETA));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		// XDM button
		final ImageView view03 = new ImageView(Constant.XDM);
		view03.setEffect(sepiaTone);
		xdmArchive.setGraphic(view03);

		xdmArchive.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					// Sélection du fichier CDA à controler
					final FileChooser fileChooser = new FileChooser();
					fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
					Utility.newExtFilter(fileChooser).addAll(new ExtensionFilter("IHE XDM", "*.zip"),
							new ExtensionFilter("All files", "*.*"));
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						fileChooser.setInitialDirectory(new File(sChemin));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);

						final Map<String, String> path = XdmService.openXDMFile(stage, file);
						final Iterator<Entry<String, String>> iterator = path.entrySet().iterator();
						while (iterator.hasNext()) {
							@SuppressWarnings("rawtypes")
							final Map.Entry mapentry = iterator.next();
							textFieldCda.setText((String) mapentry.getKey());
							textFieldMeta.setText((String) mapentry.getValue());
						}
						IniFile.write(LASTCDAFILE, textFieldCda.getText(), MEMORY);
						IniFile.write(LASTMAFILE, textFieldMeta.getText(), MEMORY);
						IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
						final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
								.toExternalForm();
						webEngine.load(url);
					}
				});
			}
		});

		// CROSS button
		final ImageView view04 = new ImageView(Constant.CROSS);
		view04.setEffect(sepiaTone);
		crossValItem.setGraphic(view04);

		crossValItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty() && !textFieldMeta.getText().isEmpty()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						webEngine.load(null);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
						scaleTransition.stop();
						final String console = CrossValidationService.crossValidate(new File(textFieldCda.getText()),
								new File(textFieldMeta.getText()), Constant.URLVALIDATION);
						final String ret = CrossValidationService.displayLastReport();
						textAreaConsole.setText(console);
						if (ret.contains(REPORTHTML)) {
							webEngine.load(FILEP + SaxonCrossValidator.getNewFilePath4().toString());
						} else {
							final String url = WebViewSample.class.getClassLoader().getResource(Constant.ERRORIMG)
									.toExternalForm();
							webEngine.load(url);
						}
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					});
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString("message.errormeta.errorcda"));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		final ImageView viewExport = new ImageView(Constant.EXPORT);
		viewExport.setEffect(sepiaTone);
		exportItem.setGraphic(viewExport);

		exportItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					Path finalFile;
					if (SaxonValidator.getNewFilePath4().toFile().exists()) {
						final List<String> pdfFiles = new ArrayList<>();
						try {
							final Path file = Files.createTempFile(null, Constant.PDFEXT);
							final String formattedXml = PdfUtility
									.xmlToPdf(SaxonValidator.getNewFilePath2().toString());
							PdfUtility.exportXMLToPDF(formattedXml, file.toString());
							final Path pdfFilePath = Files.createTempFile(null, Constant.PDFEXT);
							final String url = new File(SaxonValidator.getNewFilePath4().toString()).toURI().toURL()
									.toString();
							final OutputStream ostream = Files.newOutputStream(Paths.get(pdfFilePath.toUri()));
							final ITextRenderer renderer = new ITextRenderer();
							renderer.setDocument(url);
							renderer.layout();
							renderer.createPDF(ostream);
							ostream.close();
							pdfFiles.add(pdfFilePath.toString());
							pdfFiles.add(file.toString());
							finalFile = Files.createTempFile(null, Constant.PDFEXT);
							PdfUtility.mergePDFs(pdfFiles, finalFile.toString());
							Desktop.getDesktop().open(finalFile.toFile());
						} catch (final IOException | com.lowagie.text.DocumentException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.available.repport"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		// CROSS button
		final ImageView view05 = new ImageView(Constant.RAPPORT);
		view05.setEffect(sepiaTone);
		displayItem.setGraphic(view05);
		displayItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					if (SaxonValidator.getNewFilePath4().toFile().exists()) {
						textAreaConsole.clear();
						webEngine.load(null);
						fadeTransition.stop();
						browserEngine.setOpacity(1.0);
						scaleTransition.stop();
						browserEngine.setScaleX(1.0);
						browserEngine.setScaleY(1.0);
						final String path = ValidationService.displayLastReport();
						try {
							final String content = Files.readString(Constant.LOGFILE.toPath());
							textAreaConsole.setText(content);
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						if (path.contains(REPORTHTML)) {
							webEngine.load(FILEP + SaxonValidator.getNewFilePath4().toString());
						} else {
							final String url = WebViewSample.class.getClassLoader().getResource(Constant.ERRORIMG)
									.toExternalForm();
							webEngine.load(url);
						}
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.available.repport"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		final ImageView view06 = new ImageView(Constant.OPEN);
		view06.setEffect(sepiaTone);
		openItem.setGraphic(view06);

		openItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					if (SaxonValidator.getNewFilePath4().toFile().exists()) {
						final Stage stageTeen = new Stage();
						// open new scene for xdm
						final VBox root = new VBox();
						final TextArea area1 = new TextArea();
						final TextArea area2 = new TextArea();
						final TextArea area3 = new TextArea();
						final TextArea area4 = new TextArea();

						area1.setEditable(false);
						area2.setEditable(false);
						area3.setEditable(false);
						area4.setEditable(false);

						area1.setStyle(Constant.STYILE);
						area2.setStyle(Constant.STYILE);
						area3.setStyle(Constant.STYILE);
						area4.setStyle(Constant.STYILE);
						try {
							final Document doc1 = Jsoup.parse(SaxonValidator.getNewFilePath4().toFile(), Constant.UTF8);
							area1.setText(doc1.toString());
							if (SaxonValidator.getNewFilePath3().toFile().exists()) {
								final Document doc = Jsoup.parse(SaxonValidator.getNewFilePath3().toFile(),
										Constant.UTF8);
								area2.setText(doc.toString());
							}
							if (SaxonValidator.getNewFilePath2().toFile().exists()) {
								final Document doc = Jsoup.parse(SaxonValidator.getNewFilePath2().toFile(),
										Constant.UTF8);
								area3.setText(doc.toString());
							}
							if (SaxonValidator.getNewFilePath1().toFile().exists()) {
								final Document doc = Jsoup.parse(SaxonValidator.getNewFilePath1().toFile(),
										Constant.UTF8);
								area4.setText(doc.toString());
							}
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						final VBox vbox1 = new VBox();
						final Label label1 = new Label("document_last_report_HTML.html");
						label1.setStyle(FONTTIME1);
						final Region spacer1 = new Region();
						spacer1.setMaxWidth(700);
						HBox.setHgrow(spacer1, Priority.ALWAYS);
						final HBox hb1 = new HBox();
						final JFXButton button1 = new JFXButton("");
						button1.setOnMouseEntered(e -> button1.setEffect(shadow));
						button1.setOnMouseExited(e -> button1.setEffect(null));
						final ImageView view = new ImageView(Constant.OPENWITH);
						view.setEffect(sepiaTone);
						button1.setGraphic(view);
						button1.setStyle(Constant.STYLE1);
						button1.setPrefSize(30, 30);
						button1.setMinSize(30, 30);
						button1.setMaxSize(30, 30);
						hb1.getChildren().addAll(label1, spacer1, button1);
						button1.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									if (Desktop.isDesktopSupported()) {
										try {
											final Desktop desktop = Desktop.getDesktop();
											if (SaxonValidator.getNewFilePath4().toFile().exists()) {
												desktop.open(SaxonValidator.getNewFilePath4().toFile());
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								});
							}
						});
						vbox1.getChildren().addAll(hb1, area1);
						final VBox vbox2 = new VBox();
						final Label label2 = new Label("document_validation_last_request.xml");
						label2.setStyle(FONTTIME1);
						final Region spacer2 = new Region();
						spacer2.setMaxWidth(700);
						HBox.setHgrow(spacer2, Priority.ALWAYS);

						final HBox hb2 = new HBox();
						final JFXButton button2 = new JFXButton("");
						button2.setOnMouseEntered(e -> button2.setEffect(shadow));
						button2.setOnMouseExited(e -> button2.setEffect(null));
						final ImageView view2 = new ImageView(Constant.OPENWITH);
						view2.setEffect(sepiaTone);
						button2.setGraphic(view2);
						button2.setStyle(Constant.STYLE1);
						button2.setPrefSize(30, 30);
						button2.setMinSize(30, 30);
						button2.setMaxSize(30, 30);
						hb2.getChildren().addAll(label2, spacer2, button2);

						button2.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									if (Desktop.isDesktopSupported()) {
										try {
											final Desktop desktop = Desktop.getDesktop();
											if (SaxonValidator.getNewFilePath3().toFile().exists()) {
												desktop.open(SaxonValidator.getNewFilePath3().toFile());
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								});
							}
						});

						vbox2.getChildren().addAll(hb2, area2);

						final VBox vbox3 = new VBox();
						final Label label3 = new Label("document_validation_last_report.xml");
						label3.setStyle(FONTTIME1);

						final Region spacer3 = new Region();
						spacer3.setMaxWidth(700);
						HBox.setHgrow(spacer3, Priority.ALWAYS);

						final HBox hb3 = new HBox();
						final JFXButton button3 = new JFXButton("");
						button3.setOnMouseEntered(e -> button3.setEffect(shadow));
						button3.setOnMouseExited(e -> button3.setEffect(null));
						final ImageView view3 = new ImageView(Constant.OPENWITH);
						view3.setEffect(sepiaTone);
						button3.setGraphic(view3);
						button3.setStyle(Constant.STYLE1);
						button3.setPrefSize(30, 30);
						button3.setMinSize(30, 30);
						button3.setMaxSize(30, 30);
						hb3.getChildren().addAll(label3, spacer3, button3);

						button3.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									if (Desktop.isDesktopSupported()) {
										try {
											final Desktop desktop = Desktop.getDesktop();
											if (SaxonValidator.getNewFilePath2().toFile().exists()) {
												desktop.open(SaxonValidator.getNewFilePath2().toFile());
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								});
							}
						});

						vbox3.getChildren().addAll(hb3, area3);

						final VBox vbox4 = new VBox();
						final Label label4 = new Label("document_validation_last_result.xml");
						label4.setStyle(FONTTIME1);

						final Region spacer4 = new Region();
						spacer4.setMaxWidth(700);
						HBox.setHgrow(spacer4, Priority.ALWAYS);

						final HBox hb4 = new HBox();
						final JFXButton button4 = new JFXButton("");
						button4.setOnMouseEntered(e -> button4.setEffect(shadow));
						button4.setOnMouseExited(e -> button4.setEffect(null));
						final ImageView view4 = new ImageView(Constant.OPENWITH);
						view4.setEffect(sepiaTone);
						button4.setGraphic(view4);
						button4.setStyle(Constant.STYLE1);
						button4.setPrefSize(30, 30);
						button4.setMinSize(30, 30);
						button4.setMaxSize(30, 30);
						hb4.getChildren().addAll(label4, spacer4, button4);

						button4.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									if (Desktop.isDesktopSupported()) {
										try {
											final Desktop desktop = Desktop.getDesktop();
											if (SaxonValidator.getNewFilePath1().toFile().exists()) {
												desktop.open(SaxonValidator.getNewFilePath1().toFile());
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								});
							}
						});

						vbox4.getChildren().addAll(hb4, area4);

						final SplitPane splitPane = new SplitPane();
						splitPane.setStyle(BORDERSTYLE);
						splitPane.setOrientation(Orientation.HORIZONTAL);
						splitPane.setDividerPositions(0.5f, 0.5f);
						splitPane.setPadding(new Insets(20, 0, 0, 20));
						Utility.getItemsSplit(splitPane).addAll(vbox1, vbox2);

						area1.setPrefHeight(Integer.MAX_VALUE);
						area1.setPrefWidth(Integer.MAX_VALUE);

						area2.setPrefHeight(Integer.MAX_VALUE);
						area2.setPrefWidth(Integer.MAX_VALUE);

						final SplitPane splitPane2 = new SplitPane();
						splitPane2.setStyle(BORDERSTYLE);
						splitPane2.setOrientation(Orientation.HORIZONTAL);
						splitPane2.setDividerPositions(0.5f, 0.5f);
						splitPane2.setPadding(new Insets(20, 0, 0, 20));
						Utility.getItemsSplit(splitPane2).addAll(vbox3, vbox4);

						area3.setPrefHeight(Integer.MAX_VALUE);
						area3.setPrefWidth(Integer.MAX_VALUE);

						area4.setPrefHeight(Integer.MAX_VALUE);
						area4.setPrefWidth(Integer.MAX_VALUE);

						root.getChildren().addAll(splitPane, splitPane2);

						final Scene scene = new Scene(root, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
						Utility.getStyleScene(scene)
								.add(WebViewSample.class.getResource(Constant.CSS).toExternalForm());
						stageTeen.setMaximized(true);
						stageTeen.setScene(scene);
						stageTeen.titleProperty().bind(LocalUtility.createStringBinding("message.display.repport"));
						stageTeen.show();

					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.available.repport"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		uuidItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					if (!textFieldCda.getText().isEmpty()) {
						textAreaConsole.clear();
						final Map<List<String>, List<String>> map = ControlCdaService.checkUUID(textFieldCda.getText());
						List<String> key = new ArrayList<>();
						List<String> value = new ArrayList<>();
						for (final Map.Entry<List<String>, List<String>> entry : map.entrySet()) {
							key = entry.getKey();
							value = entry.getValue();
						}
						final StringBuilder fieldContent = new StringBuilder("");
						fieldContent.append(LocalUtility.getString("message.list.uuid")
								+ new File(textFieldCda.getText()).getName() + "\n\n");
						for (final String str : key) {
							fieldContent.append(LocalUtility.getString("message.valid.uuid") + str + "\n");
						}
						fieldContent.append("\n" + LocalUtility.getString("message.list.uuid.invalid")
								+ new File(textFieldCda.getText()).getName() + "\n\n");
						for (final String str : value) {
							fieldContent.append(LocalUtility.getString("message.invalid.uuid") + str + "\n");
						}
						textAreaConsole.setText(fieldContent.toString());
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.load.cda"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
				final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
						.toExternalForm();
				webEngine.load(url);
			}
		});

		calculItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					if (!textFieldCda.getText().isEmpty()) {
						textAreaConsole.clear();
						try {
							BomService.saveAsUTF8WithoutBOM(new File(textFieldCda.getText()).getAbsolutePath(),
									StandardCharsets.UTF_8);
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						final String hash = ControlCdaService.getHash(textFieldCda.getText());
						final String size = ControlCdaService.printFileSizeNIO(textFieldCda.getText());
						textAreaConsole.setText(hash + size);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(hash), null);
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.load.cda"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
				final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
						.toExternalForm();
				webEngine.load(url);
			}
		});

		xpathSItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String fileCda = textFieldCda.getText();
					if (fileCda != null && !fileCda.isEmpty()) {
						final Stage secondStage = new Stage();
						secondStage.setOnShowing(ev -> {
							final StackPane stack = new StackPane();
							textAr.setEditable(false);
							textAr.setPrefSize(1000, 1000);
							Inutility2.readFileContents(textAr, new File(fileCda));
							stack.setPadding(new Insets(5));
							final ListView<String> listView = new ListView<>();
							stack.getChildren().add(listView);
							final HBox listHeaderBox2 = new HBox();
							listHeaderBox2.setAlignment(Pos.BASELINE_RIGHT);
							listHeaderBox2.getChildren().add(textAr);
							final StackPane sp5 = new StackPane();
							sp5.getChildren().add(listHeaderBox2);
							final SplitPane sp3 = new SplitPane();
							sp3.getItems().addAll(sp5);
							final StackPane sp2 = new StackPane();
							sp2.getChildren().add(stack);
							final SplitPane splitP = new SplitPane();
							splitP.getItems().addAll(sp3, sp2);

							final VBox vBox = new VBox();
							vBox.setPadding(new Insets(10, 10, 10, 10));
							final Label label2 = LocalUtility
									.labelForValue(() -> LocalUtility.get("message.expr.test"));
							label2.setPadding(new Insets(10, 0, 10, 0));
							label2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							final TextField textField2 = new TextField();
							textField2.setOnMouseEntered(e -> textField2.setEffect(shadow));
							textField2.setOnMouseExited(e -> textField2.setEffect(null));
							final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
							final double width = screenSize.getWidth();
							textField2.setPrefWidth(width - 60);
							textField2.setPrefHeight(30);
							textField2.setMinHeight(30);
							textField2.setMaxHeight(30);
							textField2.setStyle(Constant.STYILE);
							textField2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							textField2.promptTextProperty().bind(LocalUtility.createStringBinding("message.namespace"));
							textField2.setFocusTraversable(false);
							textAr.setWrapText(true);

							final JFXButton button2 = new JFXButton("");
							button2.setOnMouseEntered(e -> button2.setEffect(shadow));
							button2.setOnMouseExited(e -> button2.setEffect(null));
							final ImageView view2 = new ImageView(Constant.SEARCH);
							view2.setEffect(sepiaTone);
							button2.setGraphic(view2);
							button2.setStyle(Constant.STYLE1);
							button2.setPrefSize(30, 30);
							button2.setMinSize(30, 30);
							button2.setMaxSize(30, 30);
							final Tooltip tooltip = LocalUtility.createBoundTooltip("message.search.gen");
							button2.setTooltip(tooltip);
							button2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							final HBox hbox = new HBox();
							final Region spacerB = new Region();
							spacerB.setMaxWidth(20);
							HBox.setHgrow(spacerB, Priority.ALWAYS);
							hbox.getChildren().addAll(textField2, spacerB, button2);

							button2.setOnAction(new EventHandler<>() {
								@Override
								public void handle(final ActionEvent event) {
									Platform.runLater(() -> {
										listView.setItems(null);
										InputStream targetStream;
										try {
											if (!textField2.getText().isEmpty()) {
												targetStream = Files.newInputStream(Paths.get(fileCda));
												final String strFile = IniFile.readFileContents(targetStream);
												final List<String> result = XPathLineEvaluator.evaluate(strFile,
														textField2.getText());
												if (!result.isEmpty()) {
													final ObservableList<String> items = FXCollections
															.observableArrayList(result);
													listView.setItems(items);
												} else {
													final Alert alert = new Alert(AlertType.WARNING);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(LocalUtility.get("message.xpath.empty"));
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												}
											} else {
												final Alert alert = new Alert(AlertType.WARNING);
												final DialogPane dialogPane = alert.getDialogPane();
												Inutility2.getStyleDialog(dialogPane)
														.add(getClass().getResource(Constant.CSS).toExternalForm());
												Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(LocalUtility.get("message.errorxpath"));
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait();
											}
										} catch (final IOException e1) {
											if (LOG.isInfoEnabled()) {
												final String error = e1.getMessage();
												LOG.error(error);
											}
										}
									});
								}
							});

							listView.getSelectionModel().selectedItemProperty()
									.addListener((observable, oldValue, newValue) -> {
										if (newValue != null) {
											final String[] value = newValue.split(" : Line ");
											Inutility2.goToLine(textAr, value[1]);
										}
									});

							vBox.getChildren().addAll(label2, hbox, splitP);
							final Scene scene = new Scene(vBox);
							scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							Platform.runLater(() -> {
								secondStage.setScene(scene);
								secondStage.setTitle(new File(fileCda).getAbsolutePath());
								secondStage.setMaxWidth(Integer.MAX_VALUE);
								secondStage.setMaxHeight(Integer.MAX_VALUE);
								secondStage.setMaximized(true);
								secondStage.getIcons().add(new Image(Thread.currentThread().getContextClassLoader()
										.getResourceAsStream("images/logo-1.jpg")));
							});
							scene.widthProperty().addListener(new ChangeListener<>() {
								@Override
								public void changed(final ObservableValue<? extends Number> observableValue,
										final Number oldSceneWidth, final Number newSceneWidth) {
									if (newSceneWidth.doubleValue() >= 1509 && newSceneWidth.doubleValue() < 1632) {
										textAr.setStyle(
												"-fx-font-size:14pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE;  -fx-background-color: white;");
									} else if (newSceneWidth.doubleValue() >= 1632
											&& newSceneWidth.doubleValue() >= 1728) { // ecran
																						// 17
																						// pouces
										textAr.setStyle(
												"-fx-font-size:16pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE; -fx-background-color: white;");
									} else if (newSceneWidth.doubleValue() < 1509 && newSceneWidth.doubleValue() > 0) {
										textAr.setStyle(
												"-fx-font-size:10pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE;  -fx-background-color: white; ");
									}
								}
							});
						});
						secondStage.setOnCloseRequest(ev -> {
							ev.consume();
							secondStage.close();
						});
						secondStage.show();
					} else {
						final Alert alert = new Alert(AlertType.WARNING);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.get(ERRORCDA));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		xpathMItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String fileCda = textFieldMeta.getText();
					if (fileCda != null && !fileCda.isEmpty()) {
						final Stage secondStage = new Stage();
						secondStage.setOnShowing(ev -> {
							final StackPane stack = new StackPane();
							textArMeta.setEditable(false);
							textArMeta.setPrefSize(1000, 1000);
							Inutility2.readFileContents(textArMeta, new File(fileCda));
							stack.setPadding(new Insets(5));
							final ListView<String> listView = new ListView<>();
							stack.getChildren().add(listView);
							final HBox listHeaderBox2 = new HBox();
							listHeaderBox2.setAlignment(Pos.BASELINE_RIGHT);
							listHeaderBox2.getChildren().add(textArMeta);
							final StackPane sp5 = new StackPane();
							sp5.getChildren().add(listHeaderBox2);
							final SplitPane sp3 = new SplitPane();
							sp3.getItems().addAll(sp5);
							final StackPane sp2 = new StackPane();
							sp2.getChildren().add(stack);
							final SplitPane splitP = new SplitPane();
							splitP.getItems().addAll(sp3, sp2);

							final VBox vBox = new VBox();
							vBox.setPadding(new Insets(10, 10, 10, 10));
							final Label label2 = LocalUtility
									.labelForValue(() -> LocalUtility.get("message.expr.test"));
							label2.setPadding(new Insets(10, 0, 10, 0));
							label2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							final TextField textField2 = new TextField();
							textField2.setOnMouseEntered(e -> textField2.setEffect(shadow));
							textField2.setOnMouseExited(e -> textField2.setEffect(null));
							final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
							final double width = screenSize.getWidth();
							textField2.setPrefWidth(width - 60);
							textField2.setPrefHeight(30);
							textField2.setMinHeight(30);
							textField2.setMaxHeight(30);
							textField2.setStyle(Constant.STYILE);
							textField2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							textField2.promptTextProperty().bind(LocalUtility.createStringBinding("message.namespace"));
							textField2.setFocusTraversable(false);
							textArMeta.setWrapText(true);
							final JFXButton button2 = new JFXButton("");
							button2.setOnMouseEntered(e -> button2.setEffect(shadow));
							button2.setOnMouseExited(e -> button2.setEffect(null));
							final ImageView view2 = new ImageView(Constant.SEARCH);
							view2.setEffect(sepiaTone);
							button2.setGraphic(view2);
							button2.setStyle(Constant.STYLE1);
							button2.setPrefSize(30, 30);
							button2.setMinSize(30, 30);
							button2.setMaxSize(30, 30);
							final Tooltip tooltip = LocalUtility.createBoundTooltip("message.search.gen");
							button2.setTooltip(tooltip);
							button2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

							final HBox hbox = new HBox();
							final Region spacerB = new Region();
							spacerB.setMaxWidth(20);
							HBox.setHgrow(spacerB, Priority.ALWAYS);
							hbox.getChildren().addAll(textField2, spacerB, button2);

							button2.setOnAction(new EventHandler<>() {
								@Override
								public void handle(final ActionEvent event) {
									Platform.runLater(() -> {
										listView.setItems(null);
										InputStream targetStream;
										try {
											if (!textField2.getText().isEmpty()) {
												targetStream = Files.newInputStream(Paths.get(fileCda));
												final String strFile = IniFile.readFileContents(targetStream);
												final List<String> result = XPathLineEvaluator.evaluate(strFile,
														textField2.getText());
												if (!result.isEmpty()) {
													final ObservableList<String> items = FXCollections
															.observableArrayList(result);
													listView.setItems(items);
												} else {
													final Alert alert = new Alert(AlertType.WARNING);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(LocalUtility.get("message.xpath.empty"));
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												}
											} else {
												final Alert alert = new Alert(AlertType.WARNING);
												final DialogPane dialogPane = alert.getDialogPane();
												Inutility2.getStyleDialog(dialogPane)
														.add(getClass().getResource(Constant.CSS).toExternalForm());
												Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(LocalUtility.get("message.errorxpath"));
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait();
											}
										} catch (final IOException e1) {
											if (LOG.isInfoEnabled()) {
												final String error = e1.getMessage();
												LOG.error(error);
											}
										}
									});
								}
							});

							listView.getSelectionModel().selectedItemProperty()
									.addListener((observable, oldValue, newValue) -> {
										if (newValue != null) {
											final String[] value = newValue.split(" : ");
											Inutility2.goToLine(textArMeta, value[1]);
										}
									});

							vBox.getChildren().addAll(label2, hbox, splitP);
							final Scene scene = new Scene(vBox);
							scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							Platform.runLater(() -> {
								secondStage.setScene(scene);
								secondStage.setTitle(new File(fileCda).getAbsolutePath());
								secondStage.setMaxWidth(Integer.MAX_VALUE);
								secondStage.setMaxHeight(Integer.MAX_VALUE);
								secondStage.setMaximized(true);
								secondStage.getIcons().add(new Image(Thread.currentThread().getContextClassLoader()
										.getResourceAsStream("images/logo-1.jpg")));
							});
							scene.widthProperty().addListener(new ChangeListener<>() {
								@Override
								public void changed(final ObservableValue<? extends Number> observableValue,
										final Number oldSceneWidth, final Number newSceneWidth) {
									if (newSceneWidth.doubleValue() >= 1509 && newSceneWidth.doubleValue() < 1632) {
										textArMeta.setStyle(
												"-fx-font-size:14pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE; -fx-background-color: #e9e4e6;");
									} else if (newSceneWidth.doubleValue() >= 1632
											&& newSceneWidth.doubleValue() >= 1728) { // ecran
																						// 17
																						// pouces
										textArMeta.setStyle(
												"-fx-font-size:16pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE; -fx-background-color: #e9e4e6;");
									} else if (newSceneWidth.doubleValue() < 1509 && newSceneWidth.doubleValue() > 0) {
										textArMeta.setStyle(
												"-fx-font-size:10pt; -fx-font-weight:normal; -fx-font-family:Monaco, 'Courier New', MONOSPACE; -fx-background-color: #e9e4e6;");
									}
								}
							});
						});
						secondStage.setOnCloseRequest(ev -> {
							ev.consume();
							secondStage.close();
						});
						secondStage.show();
					} else {
						final Alert alert = new Alert(AlertType.WARNING);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.get(ERRORMETA));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		xpathItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final Stage stage = new Stage();
					final VBox root = new VBox();
					final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.verif.xpath"));
					label.setPadding(new Insets(10, 0, 10, 0));
					label.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					final Label label1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.search.folder"));
					label1.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					label1.setPadding(new Insets(10, 0, 10, 0));
					final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					final double width = screenSize.getWidth();
					final HBox hbox = new HBox();
					final TextField textField = new TextField();
					textField.setOnMouseEntered(e -> textField.setEffect(shadow));
					textField.setOnMouseExited(e -> textField.setEffect(null));
					textField.setPadding(new Insets(10, 0, 10, 0));
					textField.setPrefWidth(width - 100);
					textField.setPrefHeight(35);
					textField.setMinHeight(35);
					textField.setMaxHeight(35);
					textField.setStyle(Constant.STYILE);
					textField.setEditable(false);
					textField.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					textField.setPromptText(LocalUtility.get("message.prompt.folder"));
					textField.setFocusTraversable(false);

					final JFXButton button1 = new JFXButton("");
					button1.setOnMouseEntered(e -> button1.setEffect(shadow));
					button1.setOnMouseExited(e -> button1.setEffect(null));
					final ImageView view = new ImageView(Constant.FOLDERPHOTO1);
					view.setEffect(sepiaTone);
					button1.setGraphic(view);
					button1.setStyle(Constant.STYLE1);
					button1.setPrefSize(35, 35);
					button1.setMinSize(35, 35);
					button1.setMaxSize(35, 35);
					button1.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					final Tooltip tooltip10 = LocalUtility.createBoundTooltip(Constant.TITLE);
					button1.setTooltip(tooltip10);
					final DirectoryChooser directoryChooser = new DirectoryChooser();
					directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
					button1.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
								if (new File(sChemin).exists()) {
									directoryChooser.setInitialDirectory(new File(sChemin));
								} else {
									directoryChooser.setInitialDirectory(new File(Constant.DISK));
								}
								final File selectedDirectory = directoryChooser.showDialog(stage);
								if (selectedDirectory != null && selectedDirectory.isDirectory()) {
									textField.setText(selectedDirectory.getAbsolutePath());
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, selectedDirectory.getAbsolutePath(), MEMORY);
								}
							});
						}
					});

					final Region spacerB = new Region();
					spacerB.setMaxWidth(10);
					HBox.setHgrow(spacerB, Priority.ALWAYS);

					Utility.getChildren(hbox).addAll(textField, spacerB, button1);

					final Region spacer1 = new Region();
					spacer1.setMaxHeight(20);
					VBox.setVgrow(spacer1, Priority.ALWAYS);

					final Label label3 = LocalUtility.labelForValue(() -> LocalUtility.get("message.biblio.test"));
					label3.setPadding(new Insets(10, 0, 10, 0));
					label3.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					final File myObj = new File(Constant.XPATHTXT);
					if (myObj.exists() && myObj.length() > 0) {
						InputStream targetStream;
						try {
							targetStream = Files.newInputStream(Paths.get(myObj.toURI()));
							final String fileContent = Utility.readFileContents(targetStream);
							final String[] words = fileContent.split("\n");
							comboBox.setItems(FXCollections.observableArrayList(words));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					}
					final TilePane tilePane = new TilePane(comboBox);

					comboBox.setPrefWidth(width - 100);
					comboBox.setPrefHeight(45);
					comboBox.setMinHeight(45);
					comboBox.setMaxHeight(45);
					comboBox.setPadding(new Insets(10, 0, 10, 0));

					final Label label2 = LocalUtility.labelForValue(() -> LocalUtility.get("message.expr.test"));
					label2.setPadding(new Insets(10, 0, 10, 0));
					label2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					final TextField textField2 = new TextField();
					textField2.setOnMouseEntered(e -> textField2.setEffect(shadow));
					textField2.setOnMouseExited(e -> textField2.setEffect(null));
					textField2.setMaxWidth(width - 100);
					textField2.setPrefHeight(35);
					textField2.setMinHeight(35);
					textField2.setMaxHeight(35);
					textField2.setPadding(new Insets(10, 0, 10, 0));
					textField2.setStyle(Constant.STYILE);
					textField2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					textField2.promptTextProperty().bind(LocalUtility.createStringBinding("message.namespace"));
					textField2.setFocusTraversable(false);
					comboBox.valueProperty().addListener(new ChangeListener<String>() {
						@Override
						public void changed(final ObservableValue<? extends String> over, final String txt,
								final String txt1) {
							textField2.setText(txt1);
						}
					});

					final Region spacer = new Region();
					spacer.setMaxHeight(20);
					VBox.setVgrow(spacer, Priority.ALWAYS);

					final RadioButton radio = LocalUtility.radioForKey("message.display.file.xpath");
					final RadioButton radio1 = LocalUtility.radioForKey("message.display.nofile.xpath");

					radio.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					radio1.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					final ToggleGroup toggleGroup = new ToggleGroup();

					radio.setToggleGroup(toggleGroup);
					radio1.setToggleGroup(toggleGroup);

					radio.setPadding(new Insets(10, 0, 10, 0));
					radio1.setPadding(new Insets(10, 0, 10, 0));

					final ListView<String> areaView = new ListView<>();
					areaView.setPadding(new Insets(10, 0, 10, 0));
					areaView.setStyle(Constant.STYILE);
					areaView.setMaxWidth(width - 100);
					areaView.setPrefHeight(1000);
					areaView.setEditable(false);
					areaView.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					final Region spacer2 = new Region();
					spacer2.setMaxHeight(20);
					VBox.setVgrow(spacer2, Priority.ALWAYS);

					final Region spacer3 = new Region();
					spacer3.setMaxHeight(20);
					VBox.setVgrow(spacer3, Priority.ALWAYS);

					final Region spacer4 = new Region();
					spacer4.setMaxHeight(20);
					VBox.setVgrow(spacer4, Priority.ALWAYS);

					final VBox hboxV = new VBox();
					Utility.getChildren(hboxV).addAll(radio, radio1);

					final HBox hBox = new HBox();
					final JFXButton button2 = new JFXButton("");
					button2.setOnMouseEntered(e -> button2.setEffect(shadow));
					button2.setOnMouseExited(e -> button2.setEffect(null));
					final ImageView view2 = new ImageView(Constant.SEARCH);
					view2.setEffect(sepiaTone);
					button2.setGraphic(view2);
					button2.setStyle(Constant.STYLE1);
					button2.setPrefSize(40, 40);
					button2.setMinSize(40, 40);
					button2.setMaxSize(40, 40);
					final Tooltip tooltip = LocalUtility.createBoundTooltip("message.search.gen");
					button2.setTooltip(tooltip);
					button2.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					areaView.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent mouseEvent) {
							if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

								if (mouseEvent.getClickCount() == 2) {
									final String file = areaView.getSelectionModel().getSelectedItem();
									if (file != null && !file.isEmpty()) {
										final Desktop desktop = Desktop.getDesktop();
										try {
											desktop.open(new File(file));
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								}
							}
						}
					});

					button2.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (!textField.getText().isEmpty() && !textField2.getText().isEmpty()) {
									final String text = textField2.getText();
									final File dir = new File(textField.getText());
									final File[] liste = dir.listFiles();
									String bool = null;
									if (liste != null) {
										if (radio.isSelected() || radio1.isSelected()) {
											if (radio.isSelected()) {
												final ObservableList<String> observableList = FXCollections
														.observableArrayList();
												areaView.setItems(null);
												for (final File item : liste) {
													if (item.isFile() && item.getName().endsWith(Constant.EXTXML)) {
														bool = ControlCdaService.lookupxpath(item.getAbsolutePath(),
																textField2.getText());
														if (bool == "true") {
															observableList.add(item.getAbsolutePath());
														}
													}
												}
												if (!observableList.isEmpty()) {
													areaView.setItems(observableList);
												} else if (observableList.isEmpty()
														&& (bool == "true" || bool == "false")) {
													final Alert alert = new Alert(AlertType.WARNING);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(LocalUtility.get("message.nodata"));
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												} else if (observableList.isEmpty()
														&& (!bool.equals("true") || bool.equals("false"))) {
													final Alert alert = new Alert(AlertType.ERROR);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(bool);
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												}
											} else if (radio1.isSelected()) {
												final ObservableList<String> observableList = FXCollections
														.observableArrayList();
												areaView.setItems(null);
												for (final File item : liste) {
													if (item.isFile() && item.getName().endsWith(Constant.EXTXML)) {
														bool = ControlCdaService.lookupxpath(item.getAbsolutePath(),
																textField2.getText());
														if (bool.equals("false")) {
															observableList.add(item.getAbsolutePath());
														}
													}
												}
												if (!observableList.isEmpty()) {
													areaView.setItems(observableList);
												} else if (observableList.isEmpty()
														&& (bool.equals("true") || bool.equals("false"))) {
													final Alert alert = new Alert(AlertType.WARNING);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(LocalUtility.get("message.nodata"));
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												} else if (observableList.isEmpty()
														&& (!bool.equals("true") || !bool.equals("false"))) {
													final Alert alert = new Alert(AlertType.ERROR);
													final DialogPane dialogPane = alert.getDialogPane();
													Inutility2.getStyleDialog(dialogPane)
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
													dialogPane.setMinHeight(130);
													dialogPane.setMaxHeight(130);
													dialogPane.setPrefHeight(130);
													alert.setContentText(bool);
													alert.setHeaderText(null);
													alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
													alert.showAndWait();
												}
											}
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Inutility2.getStyleDialog(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.get("message.opt.valid"));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}

									} else {
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Inutility2.getStyleDialog(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.get("message.noexpr.xpath"));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									}
									final File myObj = new File(Constant.XPATHTXT);
									if (!myObj.exists()) {
										try {
											myObj.createNewFile();
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
									final List<String> find = searchStandard(myObj.getAbsolutePath(),
											textField2.getText());
									if (find == null || find.isEmpty()) {
										try {
											Files.write(Paths.get(myObj.getAbsolutePath()),
													(textField2.getText() + "\n").getBytes(),
													StandardOpenOption.APPEND);
											InputStream targetStream;
											try {
												targetStream = Files.newInputStream(Paths.get(myObj.toURI()));
												final String fileContent = Utility.readFileContents(targetStream);
												final String[] words = fileContent.split("\n");
												comboBox.setItems(FXCollections.observableArrayList(words));
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
									textField2.setText(text);
								} else {
									final Alert alert = new Alert(AlertType.ERROR);
									final DialogPane dialogPane = alert.getDialogPane();
									Inutility2.getStyleDialog(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.get("message.search.folder.xpath"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}
							});
						}
					});

					final Region spacer5 = new Region();
					spacer5.setMaxWidth(width - 610);
					HBox.setHgrow(spacer5, Priority.ALWAYS);
					hBox.setPadding(new Insets(10, 0, 10, 0));
					Utility.getChildren(hBox).addAll(hboxV, spacer5, button2);

					Utility.getChildren(root).addAll(label, spacer, label1, hbox, spacer1, label3, tilePane, spacer4,
							label2, textField2, spacer2, hBox, spacer3, areaView);
					root.setPadding(new Insets(20, 20, 20, 20));

					final Scene scene = new Scene(root, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					stage.setMaximized(true);
					stage.setScene(scene);
					stage.titleProperty().bind(LocalUtility.createStringBinding("message.module.search.xpath"));
					stage.show();

				});
			}
		});

		bioItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					if (!textFieldCda.getText().isEmpty()) {
						textAreaConsole.clear();
						try {
							BomService.saveAsUTF8WithoutBOM(new File(textFieldCda.getText()).getAbsolutePath(),
									StandardCharsets.UTF_8);
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						final Map<List<String>, List<String>> map = ControlCdaService
								.controleLoincCodes(textFieldCda.getText());
						List<String> key = new ArrayList<>();
						List<String> value = new ArrayList<>();
						for (final Map.Entry<List<String>, List<String>> entry : map.entrySet()) {
							key = entry.getKey();
							value = entry.getValue();
						}
						final StringBuilder fieldContent = new StringBuilder("");
						fieldContent.append(LocalUtility.getString("message.code.loinc.connu") + "\n");
						for (final String str : key) {
							fieldContent.append(LocalUtility.getString("message.loinc.connu") + str + "\n");
						}
						fieldContent.append("\n" + LocalUtility.getString("message.code.loinc.inconnu") + "\n");
						for (final String str : value) {
							fieldContent.append(LocalUtility.getString("message.loinc.inconnu") + str + "\n");
						}
						textAreaConsole.setText(fieldContent.toString());
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.get("message.load.cda"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
				final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
						.toExternalForm();
				webEngine.load(url);
			}
		});

		bomItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					textAreaConsole.clear();
					final DirectoryChooser directoryChooser = new DirectoryChooser();
					directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						directoryChooser.setInitialDirectory(new File(sChemin));
					} else {
						directoryChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File selectedDirectory = directoryChooser.showDialog(stage);
					if (selectedDirectory != null && selectedDirectory.isDirectory()) {
						final Instant startTime = Instant.now();
						String retour = "Début de traitement: " + startTime + "\n";
						retour = retour + ControlCdaService.bomAllCda(selectedDirectory.listFiles());
						retour = retour + "Validation terminée. \n";
						textAreaConsole.setText(retour);
						final Alert alert = new Alert(AlertType.INFORMATION);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString(SUCCES));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
						if (selectedDirectory != null) {
							IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
						}
						if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
							textAreaConsole.setStyle(Constant.STYLE83);
						} else {
							textAreaConsole.setStyle(Constant.STYILE);
						}
					}
				});
				final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
						.toExternalForm();
				webEngine.load(url);
			}
		});

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final double width = screenSize.getWidth();
		textFieldCda.setMinSize(width / 3, button1.getPrefHeight());
		textFieldMeta.setMinSize(width / 3, button2.getPrefHeight());
		textFieldCda.setMaxSize(width / 3, button1.getPrefHeight());
		textFieldMeta.setMaxSize(width / 3, button2.getPrefHeight());
		textFieldCda.setPrefSize(width / 3, button1.getPrefHeight());
		textFieldMeta.setPrefSize(width / 3, button2.getPrefHeight());
		textFieldCda.setStyle(Constant.STYILE);
		textFieldMeta.setStyle(Constant.STYILE);
		if (Constant.BIENVENUE.equals(textAreaConsole.getText())) {
			textAreaConsole.setStyle(Constant.STYLE83);
		} else {
			textAreaConsole.setStyle(Constant.STYILE);
		}
		final Region spacer15 = new Region();
		spacer15.setMaxWidth(5);
		HBox.setHgrow(spacer15, Priority.ALWAYS);

		final Region spacer17 = new Region();
		spacer17.setMaxWidth(5);
		HBox.setHgrow(spacer17, Priority.ALWAYS);

		final Region spacer19 = new Region();
		spacer19.setMaxWidth(5);
		HBox.setHgrow(spacer19, Priority.ALWAYS);

		final ToolBar toolBar = new ToolBar();
		toolBar.setPrefHeight(40);
		toolBar.setMinHeight(40);
		toolBar.setMaxHeight(40);
		toolBar.getItems().add(button1);
		toolBar.getItems().add(new Separator());
		toolBar.getItems().add(button01);
		toolBar.getItems().add(new Separator());
		toolBar.getItems().add(button3);
		toolBar.getItems().add(new Separator());
		toolBar.getItems().add(buttonPdf);
		final BorderPane vBoxTool = new BorderPane(toolBar);
		vBoxTool.setPadding(Insets.EMPTY);

		final HBox hbox1 = new HBox();
		labelCda.setPadding(new Insets(10, 10, 0, 0));
		labelCda.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #4A4A4A;");
		labelMeta.setPadding(new Insets(10, 10, 0, 0));
		labelMeta.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #4A4A4A;");
		hbox1.setPadding(new Insets(20, 0, 0, 0));
		hbox1.getChildren().addAll(labelCda, textFieldCda, vBoxTool);

		final ToolBar toolBarM = new ToolBar();
		toolBarM.setPrefHeight(40);
		toolBarM.setMinHeight(40);
		toolBarM.setMaxHeight(40);
		toolBarM.getItems().add(button2);
		toolBarM.getItems().add(new Separator());
		toolBarM.getItems().add(button02);
		toolBarM.getItems().add(new Separator());
		toolBarM.getItems().add(button4);

		final BorderPane vBoxToolM = new BorderPane(toolBarM);
		vBoxToolM.setPadding(Insets.EMPTY);

		final HBox hbox2 = new HBox();
		hbox2.setPadding(new Insets(10, 0, 0, 0));
		hbox2.getChildren().addAll(labelMeta, textFieldMeta, vBoxToolM);

		final ImageView view41 = new ImageView(Constant.META);
		view41.setEffect(sepiaTone);
		xdmMeta.setGraphic(view41);
		xdmMeta.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty()) {
					Platform.runLater(() -> {
						final Stage stageTwo = new Stage();
						// open new scene for xdm
						final VBox root = new VBox();
						final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.list.file.lot"));
						label.setStyle(FONTTIME);
						label.setPadding(new Insets(0, 0, 0, 20));
						final HBox hbox = new HBox();
						final ObservableList<String> names = FXCollections.observableArrayList(textFieldCda.getText());
						final ListView<String> listView = new ListView<>(names);
						listView.setPrefSize(1200, 100);
						listView.setStyle(FONTTIME);

						listView.setCellFactory(lv -> new ListCell<>() {
							@Override
							protected void updateItem(final String item, final boolean empty) {
								super.updateItem(item, empty);
								if (empty || item == null) {
									setText(null);
									setGraphic(null);
								} else {
									setAlignment(Pos.TOP_LEFT);
									setText(item);
									listView.heightProperty().addListener((obs, oldVal, newVal) -> {
										setPrefHeight(newVal.doubleValue());
									});
								}
							}
						});

						final JFXButton button = new JFXButton("");
						button.setOnMouseEntered(e -> button.setEffect(shadow));
						button.setOnMouseExited(e -> button.setEffect(null));
						final ImageView view = new ImageView(Constant.ADDFILE);
						view.setEffect(sepiaTone);
						button.setGraphic(view);
						button.setStyle(Constant.STYLE1);
						button.setPrefSize(40, 40);
						button.setMinSize(40, 40);
						button.setMaxSize(40, 40);
						final Tooltip tooltip = LocalUtility.createBoundTooltip("message.add.cda.list");
						button.setTooltip(tooltip);
						button.setPadding(new Insets(0, 0, 0, 10));

						button.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final FileChooser fileChooserTwo = new FileChooser();
									fileChooserTwo.titleProperty()
											.bind(LocalUtility.createStringBinding(Constant.CHOOSE));
									final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
											"XML files (*.xml)", EXTXML);
									final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
									if (new File(sChemin).exists()) {
										fileChooserTwo.setInitialDirectory(new File(sChemin));
									} else {
										fileChooserTwo.setInitialDirectory(new File(Constant.DISK));
									}
									fileChooserTwo.getExtensionFilters().add(extFilter);
									final File file = fileChooserTwo.showOpenDialog(stageTwo);
									if (file != null) {
										listView.getItems().add(file.getAbsolutePath());
										if (file != null) {
											IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(),
													SCHCLEANER);
										}
									}
								});
							}
						});

						final JFXButton buttonR = new JFXButton("");
						buttonR.setOnMouseEntered(e -> buttonR.setEffect(shadow));
						buttonR.setOnMouseExited(e -> buttonR.setEffect(null));
						final ImageView viewR = new ImageView(Constant.REMOVEFILE);
						viewR.setEffect(sepiaTone);
						buttonR.setGraphic(viewR);
						buttonR.setStyle(Constant.STYLE1);
						buttonR.setPrefSize(40, 40);
						buttonR.setMinSize(40, 40);
						buttonR.setMaxSize(40, 40);
						final Tooltip tooltipR = LocalUtility.createBoundTooltip("message.remove.cda.list");
						buttonR.setTooltip(tooltipR);
						buttonR.setPadding(new Insets(0, 0, 0, 10));

						buttonR.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final int selectedIdx = listView.getSelectionModel().getSelectedIndex();
									if (selectedIdx != -1) {
										listView.getItems().remove(selectedIdx);
									}
								});
							}
						});

						final Region spacer = new Region();
						spacer.setMaxHeight(50);
						VBox.setVgrow(spacer, Priority.ALWAYS);

						final VBox vbox = new VBox();
						hbox.setPadding(new Insets(10, 0, 0, 20));
						vbox.getChildren().addAll(button, spacer, buttonR);

						final HBox hbox1 = new HBox();
						final Label label1 = LocalUtility
								.labelForValue(() -> LocalUtility.get("message.comm.soumission"));
						label1.setStyle(FONTTIME);
						final TextField textField1 = new TextField();
						textField1.setOnMouseEntered(e -> textField1.setEffect(shadow));
						textField1.setOnMouseExited(e -> textField1.setEffect(null));
						textField1.setPrefWidth(950);
						textField1.setText(LocalUtility.getString("message.comm.write"));
						textField1.setStyle(FONTTIME);

						final Region spacer1 = new Region();
						spacer1.setMaxWidth(20);
						HBox.setHgrow(spacer1, Priority.ALWAYS);

						hbox1.getChildren().addAll(label1, spacer1, textField1);
						hbox1.setPadding(new Insets(10, 0, 0, 20));

						final HBox hbox2 = new HBox();
						final Label label2 = LocalUtility.labelForValue(() -> LocalUtility.get("message.template.id"));
						label2.setStyle(FONTTIME);
						final TextField textField2 = new TextField();
						textField2.setOnMouseEntered(e -> textField2.setEffect(shadow));
						textField2.setOnMouseExited(e -> textField2.setEffect(null));
						textField2.setPrefWidth(950);
						textField2.setStyle(FONTTIME);

						final Region spacer2 = new Region();
						spacer2.setMaxWidth(137);
						HBox.setHgrow(spacer2, Priority.ALWAYS);

						hbox2.getChildren().addAll(label2, spacer2, textField2);
						hbox2.setPadding(new Insets(10, 0, 0, 20));

						final HBox hbox3 = new HBox();
						hbox3.getChildren().addAll(label2, spacer2, textField2);
						hbox3.setPadding(new Insets(10, 0, 0, 20));

						final JFXButton buttonMeta = new JFXButton("");
						buttonMeta.setOnMouseEntered(e -> buttonMeta.setEffect(shadow));
						buttonMeta.setOnMouseExited(e -> buttonMeta.setEffect(null));
						final ImageView viewMeta = new ImageView(Constant.METAFILE);
						viewMeta.setEffect(sepiaTone);
						buttonMeta.setAlignment(Pos.CENTER);
						buttonMeta.setGraphic(viewMeta);
						buttonMeta.setStyle(Constant.STYLE1);
						buttonMeta.setPrefSize(50, 50);
						buttonMeta.setMinSize(50, 50);
						buttonMeta.setMaxSize(50, 50);
						final Tooltip tooltipMeta = LocalUtility.createBoundTooltip("message.generate.meta");
						buttonMeta.setTooltip(tooltipMeta);
						buttonMeta.setPadding(new Insets(0, 0, 0, 10));

						final TextArea textArea = new TextArea();
						textArea.setStyle("-fx-font-size: 18;");
						textArea.setEditable(true);
						textArea.setStyle(Constant.STYLETX);
						textArea.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						buttonMeta.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									final String str = XdmService.generateMeta(listView.getItems(), fieldUrl1.getText(),
											fieldUrl2.getText(), fieldUrl3.getText(), fieldUrl4.getText(),
											fieldUrl5.getText(), fieldUrl6.getText());
									if (str.endsWith(Constant.EXTXML)) {
										final Path path = Paths.get(Constant.INTEROPFOLDER + "\\" + "nouveauDoc.xml");
										try (InputStream targetStream = Files.newInputStream(Paths.get(path.toUri()))) {
											BomService.saveAsUTF8WithoutBOM(path.toFile().getAbsolutePath(),
													StandardCharsets.UTF_8);
											textArea.setText(Utility.readFileContents(targetStream));
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											Inutility2.getStyleDialog(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(SUCCES));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									} else {
										final Alert alert = new Alert(AlertType.ERROR);
										final DialogPane dialogPane = alert.getDialogPane();
										Inutility2.getStyleDialog(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
										dialogPane.setMinHeight(170);
										dialogPane.setMaxHeight(170);
										dialogPane.setPrefHeight(170);
										alert.setContentText(str);
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									}
								});
							}
						});

						final JFXButton buttonSave = new JFXButton("");
						buttonSave.setOnMouseEntered(e -> buttonSave.setEffect(shadow));
						buttonSave.setOnMouseExited(e -> buttonSave.setEffect(null));
						final ImageView viewSave = new ImageView(Constant.SAVEFILE);
						viewSave.setEffect(sepiaTone);
						buttonSave.setGraphic(viewSave);
						buttonSave.setStyle(Constant.STYLE1);
						buttonSave.setPrefSize(50, 50);
						buttonSave.setMinSize(50, 50);
						buttonSave.setMaxSize(50, 50);
						final Tooltip tooltipSave = LocalUtility.createBoundTooltip("message.save.meta");
						buttonSave.setTooltip(tooltipSave);
						buttonSave.setPadding(new Insets(0, 0, 0, 10));
						buttonSave.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final FileChooser fileChooserTwo = new FileChooser();
									fileChooserTwo.titleProperty()
											.bind(LocalUtility.createStringBinding(Constant.CHOOSE));
									final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
											"XML files (*.xml)", EXTXML);
									final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
									if (new File(sChemin).exists()) {
										fileChooserTwo.setInitialDirectory(new File(sChemin));
									} else {
										fileChooserTwo.setInitialDirectory(new File(Constant.DISK));
									}
									fileChooserTwo.getExtensionFilters().add(extFilter);
									final File file = fileChooserTwo.showSaveDialog(stageTwo);
									if (file != null) {
										saveTextToFile(textArea.getText(), file);
										try {
											BomService.saveAsUTF8WithoutBOM(file.getAbsolutePath(),
													StandardCharsets.UTF_8);
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
										if (file != null) {
											IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(),
													SCHCLEANER);
											IniFile.write(LASTMAFILE, file.getAbsolutePath(), MEMORY);
										}
									}
								});
							}
						});

						final JFXButton buttonGen = new JFXButton("");
						buttonGen.setOnMouseEntered(e -> buttonGen.setEffect(shadow));
						buttonGen.setOnMouseExited(e -> buttonGen.setEffect(null));
						final ImageView viewGen = new ImageView(Constant.ZIPFILE);
						viewGen.setEffect(sepiaTone);
						buttonGen.setGraphic(viewGen);
						buttonGen.setStyle(Constant.STYLE1);
						buttonGen.setPrefSize(50, 50);
						buttonGen.setMinSize(50, 50);
						buttonGen.setMaxSize(50, 50);
						final Tooltip tooltipGen = LocalUtility.createBoundTooltip("message.generate.ihe.xdm");
						buttonGen.setTooltip(tooltipGen);
						buttonGen.setPadding(new Insets(0, 0, 0, 10));
						buttonGen.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final DirectoryChooser directoryChooser = new DirectoryChooser();
									directoryChooser.titleProperty()
											.bind(LocalUtility.createStringBinding(Constant.TITLE));
									final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
									if (new File(sChemin).exists()) {
										directoryChooser.setInitialDirectory(new File(sChemin));
									} else {
										directoryChooser.setInitialDirectory(new File(Constant.DISK));
									}
									final File selectedDirectory = directoryChooser.showDialog(stageTwo);
									if (names != null && !names.isEmpty() && selectedDirectory != null) {
										final String value = ParametrageService.readValueInPropFile(FOLDERFOP);
										if (value != null && !value.isEmpty()) {
											runTask(taskUpdateStage, progress);
											Platform.runLater(() -> {
												IheXdmService.generateIheXdmZip(names,
														selectedDirectory.getAbsolutePath(), value);
												final Alert alert = new Alert(AlertType.INFORMATION);
												final DialogPane dialogPane = alert.getDialogPane();
												Utility.getStylesheets(dialogPane)
														.add(getClass().getResource(Constant.CSS).toExternalForm());
												Utility.getStyleClass(dialogPane);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(LocalUtility.getString(SUCCES));
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait();
												if (selectedDirectory != null) {
													IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(),
															SCHCLEANER);
												}
											});
										} else {
											final Alert alert = new Alert(AlertType.WARNING);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString("message.error.pdf"));
											alert.setHeaderText(null);
											// Add custom buttons: OK and Cancel
											final ButtonType okButtonType = new ButtonType("OK");
											final ButtonType cancelButtonType = new ButtonType(
													LocalUtility.getString("message.alert.cancel"));
											alert.getButtonTypes().setAll(okButtonType, cancelButtonType);
											// Show the dialog and capture the user's choice
											alert.showAndWait().ifPresent(response -> {
												if (response.equals(okButtonType)) {
													paramItem.fire();
													thirdStage.setOnCloseRequest(ev -> {
														runTask(taskUpdateStage, progress);
														final String valueF = ParametrageService
																.readValueInPropFile(FOLDERFOP);
														Platform.runLater(() -> {
															IheXdmService.generateIheXdmZip(names,
																	selectedDirectory.getAbsolutePath(), valueF);
															final Alert alertt = new Alert(AlertType.INFORMATION);
															final DialogPane dialogPanee = alertt.getDialogPane();
															dialogPanee.getStylesheets().add(getClass()
																	.getResource(Constant.CSS).toExternalForm());
															dialogPanee.getStyleClass().add(Constant.DIALOG);
															dialogPanee.setMinHeight(130);
															dialogPanee.setMaxHeight(130);
															dialogPanee.setPrefHeight(130);
															alertt.setContentText(LocalUtility.getString(SUCCES));
															alertt.setHeaderText(null);
															alertt.getDialogPane().lookupButton(ButtonType.OK)
																	.setVisible(true);
															alertt.showAndWait();
															if (selectedDirectory != null) {
																IniFile.write(LASTDSELECTED,
																		selectedDirectory.getAbsolutePath(),
																		SCHCLEANER);
															}
														});
													});
												} else if (response.equals(cancelButtonType)) {
													alert.close();
												}
											});
										}
									}
								});
							}
						});

						final JFXButton buttonVal = new JFXButton("");
						buttonVal.setOnMouseEntered(e -> buttonVal.setEffect(shadow));
						buttonVal.setOnMouseExited(e -> buttonVal.setEffect(null));
						final ImageView viewVal = new ImageView(Constant.METADT);
						viewVal.setEffect(sepiaTone);
						buttonVal.setGraphic(viewVal);
						buttonVal.setStyle(Constant.STYLE1);
						buttonVal.setPrefSize(50, 50);
						buttonVal.setMinSize(50, 50);
						buttonVal.setMaxSize(50, 50);
						final Tooltip tooltipVal = LocalUtility.createBoundTooltip("message.validation.meta");
						buttonVal.setTooltip(tooltipVal);
						buttonVal.setPadding(new Insets(0, 0, 0, 10));
						buttonVal.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									try {
										if (!textArea.getText().isEmpty()) {
											final Path temp = Files.createTempFile("META", Constant.EXTXML);
											final BufferedWriter writer = new BufferedWriter(
													Files.newBufferedWriter(Paths.get(temp.toUri())));
											writer.write(textArea.getText());
											writer.close();
											ValidationService.validateMeta(temp.toFile(), Constant.MODEL,
													Constant.ASIPXDM, Constant.URLVALIDATION);
											final String ret = ValidationService.displayLastReport();
											if (ret.contains(REPORTHTML)) {
												webEngine1.load(FILEP + SaxonValidator.getNewFilePath4().toString());
											} else {
												final String url = WebViewSample.class.getClassLoader()
														.getResource(Constant.ERRORIMG).toExternalForm();
												webEngine1.load(url);
											}
											temp.toFile().delete();
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(ERRORMETA));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});

						final JFXButton buttonCrossVal = new JFXButton("");
						buttonCrossVal.setOnMouseEntered(e -> buttonCrossVal.setEffect(shadow));
						buttonCrossVal.setOnMouseExited(e -> buttonCrossVal.setEffect(null));
						final ImageView viewCrossVal = new ImageView(Constant.CROSS);
						viewCrossVal.setEffect(sepiaTone);
						buttonCrossVal.setGraphic(viewCrossVal);
						buttonCrossVal.setStyle(Constant.STYLE1);
						buttonCrossVal.setPrefSize(50, 50);
						buttonCrossVal.setMinSize(50, 50);
						buttonCrossVal.setMaxSize(50, 50);
						final Tooltip tooltipCrossVal = LocalUtility
								.createBoundTooltip("message.cross.valider.cda.meta");
						buttonCrossVal.setTooltip(tooltipCrossVal);
						buttonCrossVal.setPadding(new Insets(0, 0, 0, 10));
						buttonCrossVal.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									try {
										webEngine1.load(null);
										if (!textArea.getText().isEmpty()) {
											final Path temp = Files.createTempFile("CROSS", Constant.EXTXML);
											final BufferedWriter writer = new BufferedWriter(
													Files.newBufferedWriter(Paths.get(temp.toUri())));
											writer.write(textArea.getText());
											writer.close();
											CrossValidationService.crossValidate(new File(names.get(0)), temp.toFile(),
													Constant.URLVALIDATION);
											final String ret = CrossValidationService.displayLastReport();
											if (ret.contains(REPORTHTML)) {
												webEngine1
														.load(FILEP + SaxonCrossValidator.getNewFilePath4().toString());
											} else {
												final String url = WebViewSample.class.getClassLoader()
														.getResource(Constant.ERRORIMG).toExternalForm();
												webEngine1.load(url);
											}
											temp.toFile().delete();
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(ERRORMETA));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});

						final JFXButton buttonVerif = new JFXButton("");
						buttonVerif.setOnMouseEntered(e -> buttonVerif.setEffect(shadow));
						buttonVerif.setOnMouseExited(e -> buttonVerif.setEffect(null));
						final ImageView viewVerif = new ImageView(Constant.CHECK);
						viewVerif.setEffect(sepiaTone);
						buttonVerif.setGraphic(viewVerif);
						buttonVerif.setStyle(Constant.STYLE1);
						buttonVerif.setPrefSize(50, 50);
						buttonVerif.setMinSize(50, 50);
						buttonVerif.setMaxSize(50, 50);
						final Tooltip tooltipVerif = LocalUtility.createBoundTooltip("message.verif.error.meta");
						buttonVerif.setTooltip(tooltipVerif);
						buttonVerif.setPadding(new Insets(0, 0, 0, 10));
						buttonVerif.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									if (!textArea.getText().isEmpty()) {
										final String area = IheXdmService.verifErrorMeta(textArea, textArea.getText());
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(area);
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} else {
										final Alert alert = new Alert(AlertType.ERROR);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(ERRORMETA));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									}
								});
							}
						});

						final Region spacer3 = new Region();
						spacer3.setMaxWidth(10);
						HBox.setHgrow(spacer3, Priority.ALWAYS);

						final Region spacer4 = new Region();
						spacer4.setMaxWidth(10);
						HBox.setHgrow(spacer4, Priority.ALWAYS);

						final Region spacer5 = new Region();
						spacer5.setMaxWidth(10);
						HBox.setHgrow(spacer5, Priority.ALWAYS);

						final Region spacer6 = new Region();
						spacer6.setMaxWidth(10);
						HBox.setHgrow(spacer6, Priority.ALWAYS);

						final Region spacer7 = new Region();
						spacer7.setMaxWidth(10);
						HBox.setHgrow(spacer7, Priority.ALWAYS);

						final HBox hbox4 = new HBox();
						hbox4.getChildren().addAll(buttonMeta, spacer3, buttonSave, spacer4, buttonGen, spacer5,
								buttonVal, spacer6, buttonCrossVal, spacer7, buttonVerif);
						hbox4.setPadding(new Insets(20, 0, 0, 20));

						final SplitPane splitPane = new SplitPane();
						splitPane.setStyle(BORDERSTYLE);
						splitPane.setOrientation(Orientation.HORIZONTAL);
						splitPane.setDividerPositions(0.5f, 0.5f);
						splitPane.setPadding(new Insets(20, 0, 0, 20));

						textArea.setPrefHeight(Integer.MAX_VALUE);
						textArea.setPrefWidth(Integer.MAX_VALUE);
						final HBox box = new HBox();
						box.getChildren().add(browserEngine1);

						final HBox box1 = new HBox();
						box1.getChildren().add(textArea);

						Utility.getItemsSplit(splitPane).addAll(box1, box);

						hbox.getChildren().addAll(listView, vbox);
						root.getChildren().addAll(label, hbox, hbox1, hbox2, hbox3, hbox4, splitPane);

						final Scene scene = new Scene(root, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
						scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
						scene.setFill(Color.LIGHTGRAY);
						stageTwo.setMaximized(true);
						stageTwo.setScene(scene);
						stageTwo.setTitle("metaGenerator");
						stageTwo.show();

						stageTwo.setOnCloseRequest(new EventHandler<>() {
							@Override
							public void handle(final WindowEvent event) {
								webEngine1.load(null);
								textArea.clear();
							}
						});

					});

				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(ERRORCDA));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		final ImageView view42 = new ImageView(Constant.ZIPXDM);
		view42.setEffect(sepiaTone);
		xdmMulti.setGraphic(view42);
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));

		xdmMulti.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						directoryChooser.setInitialDirectory(new File(sChemin));
					} else {
						directoryChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File selectedDirectory = directoryChooser.showDialog(stage);
					if (selectedDirectory != null) {
						final Dialog<ButtonType> dialog = new Dialog<>();
						dialog.getDialogPane().getStylesheets()
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						dialog.titleProperty().bind(LocalUtility.createStringBinding("message.confirm"));
						dialog.setHeaderText(LocalUtility.getString("message.choice.opt.xdm"));
						dialog.setGraphic(new ImageView(Constant.ZIPXDM1));
						final ButtonType okButtonType = new ButtonType(LocalUtility.getString("message.generate"),
								ButtonData.OK_DONE);
						dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
						final GridPane grid = new GridPane();
						grid.setHgap(10);
						grid.setVgap(10);
						grid.setPadding(new Insets(20, 150, 10, 10));
						final CheckBox checkBox1 = new CheckBox(LocalUtility.getString("message.valid.meta.online"));
						final CheckBox checkBox2 = new CheckBox(LocalUtility.getString("message.valid.metacda.online"));
						grid.add(checkBox1, 0, 0);
						grid.add(checkBox2, 0, 1);
						dialog.getDialogPane().setContent(grid);
						final Optional<ButtonType> result = dialog.showAndWait();
						if (result.get().equals(okButtonType)) {
							textAreaConsole.clear();
							webEngine.load(null);
							fadeTransition.stop();
							browserEngine.setOpacity(1.0);
							scaleTransition.stop();
							browserEngine.setScaleX(1.0);
							browserEngine.setScaleY(1.0);
							runTask(taskUpdateStage, progress);
							Platform.runLater(() -> {
								final List<String> listFileOk = new ArrayList<>();
								final List<String> listFileNotOk = new ArrayList<>();
								final File iheXdm = new File(selectedDirectory + "\\IHE_XDM");
								final Instant startTime = Instant.now();
								if (selectedDirectory != null && selectedDirectory.listFiles() != null
										&& selectedDirectory.listFiles().length > 0) {
									final String value = ParametrageService.readValueInPropFile(FOLDERFOP);
									if (value != null && !value.isEmpty()) {
										textAreaConsole.setText("Début de traitement: " + startTime + "\n");
										if (iheXdm.exists()) {
											IheXdmUtilities.deleteDirectory(iheXdm);
										}
										if (!iheXdm.exists()) {
											iheXdm.mkdirs();
										}
										final File validCda = new File(iheXdm + "\\VALID_CDA");
										if (!validCda.exists()) {
											validCda.mkdirs();
										}
										final File invalidCda = new File(iheXdm + "\\INVALID_CDA");
										if (!invalidCda.exists()) {
											invalidCda.mkdirs();
										}
										final File invalidCdaMeta = new File(invalidCda + "\\ERREUR-METADATA");
										if (!invalidCdaMeta.exists()) {
											invalidCdaMeta.mkdirs();
										}
										final File invalidCdaCross = new File(invalidCda + "\\ERREUR-CROSSVAL");
										if (!invalidCdaCross.exists()) {
											invalidCdaCross.mkdirs();
										}
										for (final File file : selectedDirectory.listFiles()) {
											if (file.isFile()
													&& "xml".equals(FilenameUtils.getExtension(file.getName()))) {
												final boolean isOk = IheXdmService.generateAllIheXdmZip(
														file.getAbsolutePath(), validCda.getAbsolutePath(),
														invalidCdaMeta.getAbsolutePath(),
														invalidCdaCross.getAbsolutePath(), checkBox1.isSelected(),
														checkBox2.isSelected(), fieldUrl1.getText(),
														fieldUrl2.getText(), fieldUrl3.getText(), fieldUrl4.getText(),
														fieldUrl5.getText(), fieldUrl6.getText(), value);
												if (isOk) {
													listFileOk.add(file.getAbsolutePath());
												} else {
													listFileNotOk.add(file.getAbsolutePath());
												}
											}
										}
										textAreaConsole.setStyle(Constant.STYILE);
										textAreaConsole.appendText(
												"Répertoire de génération de IHE_XDM " + validCda.getParent() + "\n\n");
										textAreaConsole.appendText("CDA invalide:" + "\n\n");
										for (final String str : listFileNotOk) {
											textAreaConsole.appendText(str + "\n");
										}
										textAreaConsole.appendText("\n" + "CDA valide: " + "\n\n");
										for (final String str : listFileOk) {
											textAreaConsole.appendText(str + "\n");
										}
										final Instant endTime = Instant.now();
										final String finalStr = "Traitement terminé." + "\n" + "Durée du traitement: "
												+ Duration.between(startTime, endTime).getSeconds() + " " + "secondes."
												+ " \n";
										textAreaConsole.appendText(finalStr);
										Alert alert = null;
										if (listFileNotOk.isEmpty()) {
											alert = new Alert(AlertType.INFORMATION);
											alert.setContentText(LocalUtility.getString(SUCCES));
										} else {
											alert = new Alert(AlertType.WARNING);
											alert.setContentText(LocalUtility.getString("message.succes.error.val"));
										}
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} else {
										final Alert alert = new Alert(AlertType.WARNING);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString("message.error.pdf"));
										alert.setHeaderText(null);
										// Add custom buttons: OK and Cancel
										final ButtonType okButton = new ButtonType("OK");
										final ButtonType cancelButtonType = new ButtonType(
												LocalUtility.getString("message.alert.cancel"));
										alert.getButtonTypes().setAll(okButton, cancelButtonType);
										// Show the dialog and capture the user's choice
										alert.showAndWait().ifPresent(response -> {
											if (response == okButton) {
												paramItem.fire();
												thirdStage.setOnCloseRequest(ev -> {
													runTask(taskUpdateStage, progress);
													textAreaConsole.setText("Début de traitement: " + startTime + "\n");
													if (iheXdm.exists()) {
														IheXdmUtilities.deleteDirectory(iheXdm);
													}
													if (!iheXdm.exists()) {
														iheXdm.mkdirs();
													}
													final File validCda = new File(iheXdm + "\\VALID_CDA");
													if (!validCda.exists()) {
														validCda.mkdirs();
													}
													final File invalidCda = new File(iheXdm + "\\INVALID_CDA");
													if (!invalidCda.exists()) {
														invalidCda.mkdirs();
													}
													final File invalidCdaMeta = new File(
															invalidCda + "\\ERREUR-METADATA");
													if (!invalidCdaMeta.exists()) {
														invalidCdaMeta.mkdirs();
													}
													final File invalidCdaCross = new File(
															invalidCda + "\\ERREUR-CROSSVAL");
													if (!invalidCdaCross.exists()) {
														invalidCdaCross.mkdirs();
													}
													final String valueF = ParametrageService
															.readValueInPropFile(FOLDERFOP);
													Platform.runLater(() -> {
														for (final File file : selectedDirectory.listFiles()) {
															if (file.isFile() && "xml".equals(
																	FilenameUtils.getExtension(file.getName()))) {

																final boolean isOk = IheXdmService.generateAllIheXdmZip(
																		file.getAbsolutePath(),
																		validCda.getAbsolutePath(),
																		invalidCdaMeta.getAbsolutePath(),
																		invalidCdaCross.getAbsolutePath(),
																		checkBox1.isSelected(), checkBox2.isSelected(),
																		fieldUrl1.getText(), fieldUrl2.getText(),
																		fieldUrl3.getText(), fieldUrl4.getText(),
																		fieldUrl5.getText(), fieldUrl6.getText(),
																		valueF);
																if (isOk) {
																	listFileOk.add(file.getAbsolutePath());
																} else {
																	listFileNotOk.add(file.getAbsolutePath());
																}
															}
														}
														textAreaConsole.setStyle(Constant.STYILE);
														textAreaConsole
																.appendText("Répertoire de génération de IHE_XDM "
																		+ validCda.getParent() + "\n\n");
														textAreaConsole.appendText("CDA invalide:" + "\n\n");
														for (final String str : listFileNotOk) {
															textAreaConsole.appendText(str + "\n");
														}
														textAreaConsole.appendText("\n" + "CDA valide: " + "\n\n");
														for (final String str : listFileOk) {
															textAreaConsole.appendText(str + "\n");
														}
														final Instant endTime = Instant.now();
														final String finalStr = "Traitement terminé." + "\n"
																+ "Durée du traitement: "
																+ Duration.between(startTime, endTime).getSeconds()
																+ " " + "secondes." + " \n";
														textAreaConsole.appendText(finalStr);
														Alert alertt = null;
														if (listFileNotOk.isEmpty()) {
															alertt = new Alert(AlertType.INFORMATION);
															alertt.setContentText(LocalUtility.getString(SUCCES));
														} else {
															alertt = new Alert(AlertType.WARNING);
															alertt.setContentText(
																	LocalUtility.getString("message.succes.error.val"));
														}
														final DialogPane dialogPanee = alertt.getDialogPane();
														dialogPanee.getStylesheets().add(
																getClass().getResource(Constant.CSS).toExternalForm());
														dialogPanee.getStyleClass().add(Constant.DIALOG);
														dialogPanee.setMinHeight(130);
														dialogPanee.setMaxHeight(130);
														dialogPanee.setPrefHeight(130);
														alertt.setHeaderText(null);
														alertt.getDialogPane().lookupButton(ButtonType.OK)
																.setVisible(true);
														alertt.showAndWait();
													});
												});
											} else if (response == cancelButtonType) {
												alert.close();
											}
										});
									}

								}
							});
							if (selectedDirectory != null) {
								IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
								IniFile.write(LASTPUSED, selectedDirectory.getAbsolutePath(), MEMORY);
							}
							final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
									.toExternalForm();
							webEngine.load(url);
						}

					}
				});
			}

		});

		final ImageView view44 = new ImageView(Constant.UUID);
		view44.setEffect(sepiaTone);
		uuidItem.setGraphic(view44);

		final ImageView view45 = new ImageView(Constant.HACH);
		view45.setEffect(sepiaTone);
		calculItem.setGraphic(view45);

		final ImageView view46 = new ImageView(Constant.BIO);
		view46.setEffect(sepiaTone);
		bioItem.setGraphic(view46);

		final ImageView view55 = new ImageView(Constant.XPATH);
		view55.setEffect(sepiaTone);
		xpathItem.setGraphic(view55);

		final ImageView view105 = new ImageView(Constant.CDAONE);
		view105.setEffect(sepiaTone);
		xpathSItem.setGraphic(view105);

		final ImageView view106 = new ImageView(Constant.METAFILE);
		view106.setEffect(sepiaTone);
		xpathMItem.setGraphic(view106);

		final ImageView view47 = new ImageView(Constant.BOM1);
		view47.setEffect(sepiaTone);
		bomItem.setGraphic(view47);

		final ImageView view50 = new ImageView(Constant.PARAMPHOTO);
		view50.setEffect(sepiaTone);
		paramItem.setGraphic(view50);

		final ImageView view52 = new ImageView(Constant.INIFILE);
		view52.setEffect(sepiaTone);
		paramIItem.setGraphic(view52);

		final ImageView view82 = new ImageView(Constant.LOG);
		view82.setEffect(sepiaTone);
		logItem.setGraphic(view82);

		final ImageView view89 = new ImageView(Constant.TREE);
		view89.setEffect(sepiaTone);
		arboItem.setGraphic(view89);

		final ImageView view51 = new ImageView(Constant.MAPPHOTO);
		view51.setEffect(sepiaTone);
		paramMItem.setGraphic(view51);

		paramcItem.setGraphic(view57);
		view57.setEffect(sepiaTone);
		view58.setEffect(sepiaTone);

		final ImageView view60 = new ImageView(Constant.PARAM);
		view60.setEffect(sepiaTone);
		paramMenu.setGraphic(view60);

		final ImageView view66 = new ImageView(Constant.INFO);
		view66.setEffect(sepiaTone);
		docItem.setGraphic(view66);

		final ImageView view65 = new ImageView(Constant.DOCU);
		view65.setEffect(sepiaTone);
		docMenu.setGraphic(view65);

		final ImageView view61 = new ImageView(Constant.CDA);
		view61.setEffect(sepiaTone);
		cdaMenu.setGraphic(view61);

		final ImageView view64 = new ImageView(Constant.XPATHMENU);
		view64.setEffect(sepiaTone);
		xpathMenu.setGraphic(view64);

		final ImageView view62 = new ImageView(Constant.XDM1);
		view62.setEffect(sepiaTone);
		xdmMenu.setGraphic(view62);

		final ImageView view63 = new ImageView(Constant.VALIDATION);
		view63.setEffect(sepiaTone);
		validationMenu.setGraphic(view63);

		final ImageView view49 = new ImageView(Constant.FAVICON);
		view49.setEffect(sepiaTone);
		artDecorMenu.setGraphic(view49);

		final ImageView view76 = new ImageView(Constant.FHIR);
		view76.setEffect(sepiaTone);
		fhirMenu.setGraphic(view76);

		final ImageView view77 = new ImageView(Constant.GENFHIR);
		view77.setEffect(sepiaTone);
		genItem.setGraphic(view77);

		final ImageView view227 = new ImageView(Constant.MODULEFHIR);
		view227.setEffect(sepiaTone);
		moduleFhirItem.setGraphic(view227);

		final ImageView view78 = new ImageView(Constant.CONFHIR);
		view78.setEffect(sepiaTone);
		conItem.setGraphic(view78);

		final ImageView view79 = new ImageView(Constant.TERFHIR);
		view79.setEffect(sepiaTone);
		terItem.setGraphic(view79);

		final ImageView view80 = new ImageView(Constant.SERFHIR);
		view80.setEffect(sepiaTone);
		servItem.setGraphic(view80);

		final ImageView view81 = new ImageView(Constant.LOGSFHIR);
		view81.setEffect(sepiaTone);
		logsItem.setGraphic(view81);

		final ImageView view182 = new ImageView(Constant.CODESYSFHIR);
		view182.setEffect(sepiaTone);
		generateCsItem.setGraphic(view182);

		final ImageView view59 = new ImageView(Constant.JAR);
		view59.setEffect(sepiaTone);
		linkMenu.setGraphic(view59);

		final ImageView view69 = new ImageView(Constant.EXEC);
		view69.setEffect(sepiaTone);
		jarItem.setGraphic(view69);

		final ImageView view75 = new ImageView(Constant.ANS);
		view75.setEffect(sepiaTone);
		outilItem.setGraphic(view75);

		final ImageView view70 = new ImageView(Constant.CLEAN);
		view70.setEffect(sepiaTone);
		artDecorCItem.setGraphic(view70);

		final ImageView view71 = new ImageView(Constant.STAT);
		view71.setEffect(sepiaTone);
		statDecorItem.setGraphic(view71);

		final ImageView view72 = new ImageView(Constant.GAZELLE);
		view72.setEffect(sepiaTone);
		schItem.setGraphic(view72);

		final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE).toExternalForm();
		webEngine.load(url);
		browserEngine.getChildrenUnmodifiable().addListener(new ListChangeListener<>() {
			@Override
			public void onChanged(Change<? extends Node> change) {
				final Set<Node> nodes = browserEngine.lookupAll(".scroll-bar");
				for (final Node node : nodes) {
					if (node instanceof ScrollBar) {
						final ScrollBar sbuilder = (ScrollBar) node;
						if (sbuilder.getOrientation().equals(Orientation.HORIZONTAL)) {
							sbuilder.setVisible(false);
						}
					}
				}
			}
		});

		outilItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final Stage stage = new Stage();
					startPane(stage);
					stage.setTitle(LocalUtility.getString("message.menu.outil.ans"));
					stage.setMaximized(false);
					stage.setResizable(false);
					final Image imageV = new Image(Constant.ANS);
					stage.getIcons().add(imageV);
					stage.show();
					if (filePath != null && filePath.isEmpty()) {
						IniFile.write(new File(filePath).getName(), new File(filePath).getAbsolutePath(), TOOLS);
						linkMenu.getItems().clear();
						final Map<MenuItem, String> listMenuItem = new ConcurrentHashMap<>();
						final Section section = IniFile.read(TOOLS);
						if (section != null) {
							for (final Entry<String, String> entry : section.entrySet()) {
								final String key = entry.getKey();
								final String value = entry.getValue();
								listMenuItem.put(new MenuItem(key), value);
							}
						}
						if (listMenuItem.isEmpty()) {
							Utility.getItems(linkMenu).addAll(outilItem, jarItem);
						} else {
							final SeparatorMenuItem sep = new SeparatorMenuItem();
							Utility.getItems(linkMenu).addAll(outilItem, jarItem, sep);
							for (final Entry<MenuItem, String> entry : listMenuItem.entrySet()) {
								if (entry.getValue().endsWith(Constant.EXTJARFILE)) {
									new ImageView(Constant.JARFILE).setEffect(sepiaTone);
									entry.getKey().setGraphic(new ImageView(Constant.JARFILE));
								}
								Utility.getItems(linkMenu).addAll(entry.getKey());
								entry.getKey().setOnAction(new EventHandler<>() {
									@Override
									public void handle(final ActionEvent event) {
										if (entry.getValue() != null) {
											if (new File(entry.getValue()).getName().endsWith(Constant.EXTJARFILE)) {
												try {
													final String command[] = { Constant.JAVA, Constant.JARF,
															entry.getValue() };
													final ProcessBuilder pbuilder = new ProcessBuilder(command);
													pbuilder.redirectErrorStream(true);
													final Process process = pbuilder.start();
													final InputStream istream = process.getInputStream();
													// thread to handle or gobble text sent from input stream
													new Thread(() -> {
														// try with resources
														try (BufferedReader reader = new BufferedReader(
																new InputStreamReader(istream));) {
															String line;
															while ((line = reader.readLine()) != null) {
																LOG.info(line);
															}
														} catch (final IOException e) {
															if (LOG.isInfoEnabled()) {
																final String error = e.getMessage();
																LOG.error(error);
															}
														}
													}).start();
													// thread to get exit value from process without blocking
													Thread waitForThread = new Thread(() -> {
														try {
															process.waitFor();
														} catch (final InterruptedException e) {
															if (LOG.isInfoEnabled()) {
																final String error = e.getMessage();
																LOG.error(error);
															}
														}
													});
													waitForThread.start();
												} catch (final IOException e) {
													if (LOG.isInfoEnabled()) {
														final String error = e.getMessage();
														LOG.error(error);
													}
												}
											}
										}
									}
								});
							}
						}
					}
				});
			}
		});

		jarItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final FileChooser fileChooser = new FileChooser();
					fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
					final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"Files (*.jar) (*.exe)", "*.jar", "*.exe");
					fileChooser.getExtensionFilters().add(extFilter);
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						fileChooser.setInitialDirectory(new File(sChemin));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						if (file.getName().endsWith(Constant.EXTJARFILE)) {
							try {
								final String command[] = { Constant.JAVA, Constant.JARF, file.getAbsolutePath() };
								final ProcessBuilder pbuilder = new ProcessBuilder(command);
								pbuilder.redirectErrorStream(true);
								final Process process = pbuilder.start();
								final InputStream istream = process.getInputStream();
								// thread to handle or gobble text sent from input stream
								new Thread(() -> {
									// try with resources
									try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
										String line;
										while ((line = reader.readLine()) != null) {
											LOG.info(line);
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								}).start();
								// thread to get exit value from process without blocking
								Thread waitForThread = new Thread(() -> {
									try {
										process.waitFor();
									} catch (final InterruptedException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
								waitForThread.start();
							} catch (final IOException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
							IniFile.write(file.getName(), file.getAbsolutePath(), TOOLS);
							linkMenu.getItems().clear();
							final Map<MenuItem, String> listMenuItem = new ConcurrentHashMap<>();
							final Section section = IniFile.read(TOOLS);
							if (section != null) {
								for (final Entry<String, String> entry : section.entrySet()) {
									final String key = entry.getKey();
									final String value = entry.getValue();
									listMenuItem.put(new MenuItem(key), value);
								}
							}

							if (listMenuItem.isEmpty()) {
								Utility.getItems(linkMenu).addAll(outilItem, jarItem);
							} else {
								final SeparatorMenuItem sep = new SeparatorMenuItem();
								Utility.getItems(linkMenu).addAll(outilItem, jarItem, sep);
								for (final Entry<MenuItem, String> entry : listMenuItem.entrySet()) {
									if (entry.getValue().endsWith(Constant.EXEFILE)) {
										new ImageView(Constant.EXE).setEffect(sepiaTone);
										entry.getKey().setGraphic(new ImageView(Constant.EXE));
									} else if (entry.getValue().endsWith(Constant.EXTJARFILE)) {
										new ImageView(Constant.JARFILE).setEffect(sepiaTone);
										entry.getKey().setGraphic(new ImageView(Constant.JARFILE));
									}
									Utility.getItems(linkMenu).addAll(entry.getKey());
									entry.getKey().setOnAction(new EventHandler<>() {
										@Override
										public void handle(final ActionEvent event) {
											if (entry.getValue() != null) {
												if (new File(entry.getValue()).getName()
														.endsWith(Constant.EXTJARFILE)) {
													try {
														final String command[] = { Constant.JAVA, Constant.JARF,
																entry.getValue() };
														final ProcessBuilder pbuilder = new ProcessBuilder(command);
														pbuilder.redirectErrorStream(true);
														final Process process = pbuilder.start();
														final InputStream istream = process.getInputStream();
														// thread to handle or gobble text sent from input stream
														new Thread(() -> {
															// try with resources
															try (BufferedReader reader = new BufferedReader(
																	new InputStreamReader(istream));) {
																String line;
																while ((line = reader.readLine()) != null) {
																	LOG.info(line);
																}
															} catch (final IOException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														}).start();
														// thread to get exit value from process without blocking
														final Thread waitForThread = new Thread(() -> {
															try {
																process.waitFor();
															} catch (final InterruptedException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														});
														waitForThread.start();
													} catch (final IOException e) {
														if (LOG.isInfoEnabled()) {
															final String error = e.getMessage();
															LOG.error(error);
														}
													}
												} else if (new File(entry.getValue()).getName()
														.endsWith(Constant.EXEFILE)) {
													final ProcessBuilder pBuilder = new ProcessBuilder(entry.getValue(),
															"", "fr");
													pBuilder.redirectErrorStream(true);
													Process process;
													try {
														process = pBuilder.start();
														final InputStream istream = process.getInputStream();
														// thread to handle or gobble text sent from input stream
														new Thread(() -> {
															// try with resources
															try (BufferedReader reader = new BufferedReader(
																	new InputStreamReader(istream));) {
																String line;
																while ((line = reader.readLine()) != null) {
																	if (reader.readLine() == null) {
																		final Alert alert = new Alert(AlertType.ERROR);
																		final DialogPane dialogPane = alert
																				.getDialogPane();
																		Utility.getStyleClass(dialogPane);
																		dialogPane.setMinHeight(130);
																		dialogPane.setMaxHeight(130);
																		dialogPane.setPrefHeight(130);
																		alert.setContentText(Constant.GUI);
																		alert.setHeaderText(null);
																		alert.getDialogPane()
																				.lookupButton(ButtonType.OK)
																				.setVisible(true);
																		alert.showAndWait();
																	}
																	LOG.info(line);
																}
															} catch (final IOException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														}).start();
														// thread to get exit value from process without blocking
														final Thread waitForThread = new Thread(() -> {
															try {
																process.waitFor();
															} catch (final InterruptedException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														});
														waitForThread.start();
													} catch (final IOException e) {
														if (LOG.isInfoEnabled()) {
															final String error = e.getMessage();
															LOG.error(error);
														}
													}
												}
											}
										}
									});
								}
							}
						} else if (file.getName().endsWith(Constant.EXEFILE)) {
							final ProcessBuilder pBuilder = new ProcessBuilder(file.getAbsolutePath(), "", "fr");
							pBuilder.redirectErrorStream(true);
							Process process;
							try {
								process = pBuilder.start();
								final InputStream istream = process.getInputStream();
								// thread to handle or gobble text sent from input stream
								new Thread(() -> {
									// try with resources
									try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
										String line;
										while ((line = reader.readLine()) != null) {
											if (reader.readLine() == null) {
												final Alert alert = new Alert(AlertType.ERROR);
												final DialogPane dialogPane = alert.getDialogPane();
												Utility.getStyleClass(dialogPane);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(Constant.GUI);
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait();
											}
											LOG.info(line);
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								}).start();
								// thread to get exit value from process without blocking
								final Thread waitForThread = new Thread(() -> {
									try {
										process.waitFor();
									} catch (final InterruptedException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
								waitForThread.start();
							} catch (final IOException e) {
								if (LOG.isInfoEnabled()) {
									final String error = e.getMessage();
									LOG.error(error);
								}
							}
							IniFile.write(file.getName(), file.getAbsolutePath(), TOOLS);
							linkMenu.getItems().clear();
							final Map<MenuItem, String> listMenuItem = new ConcurrentHashMap<>();
							final Section section = IniFile.read(TOOLS);
							if (section != null) {
								for (final Entry<String, String> entry : section.entrySet()) {
									final String key = entry.getKey();
									final String value = entry.getValue();
									listMenuItem.put(new MenuItem(key), value);
								}
							}

							if (listMenuItem.isEmpty()) {
								Utility.getItems(linkMenu).addAll(outilItem, jarItem);
							} else {
								final SeparatorMenuItem sep = new SeparatorMenuItem();
								Utility.getItems(linkMenu).addAll(outilItem, jarItem, sep);
								for (final Entry<MenuItem, String> entry : listMenuItem.entrySet()) {
									if (entry.getValue().endsWith(Constant.EXEFILE)) {
										new ImageView(Constant.EXE).setEffect(sepiaTone);
										entry.getKey().setGraphic(new ImageView(Constant.EXE));
									} else if (entry.getValue().endsWith(Constant.EXTJARFILE)) {
										new ImageView(Constant.JARFILE).setEffect(sepiaTone);
										entry.getKey().setGraphic(new ImageView(Constant.JARFILE));
									}
									Utility.getItems(linkMenu).addAll(entry.getKey());
									entry.getKey().setOnAction(new EventHandler<>() {
										@Override
										public void handle(final ActionEvent event) {
											if (entry.getValue() != null) {
												if (new File(entry.getValue()).getName()
														.endsWith(Constant.EXTJARFILE)) {
													try {
														final String command[] = { Constant.JAVA, Constant.JARF,
																entry.getValue() };
														final ProcessBuilder pbuilder = new ProcessBuilder(command);
														pbuilder.redirectErrorStream(true);
														final Process process = pbuilder.start();
														final InputStream istream = process.getInputStream();
														// thread to handle or gobble text sent from input stream
														new Thread(() -> {
															// try with resources
															try (BufferedReader reader = new BufferedReader(
																	new InputStreamReader(istream));) {
																String line;
																while ((line = reader.readLine()) != null) {
																	LOG.info(line);
																}
															} catch (final IOException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														}).start();
														// thread to get exit value from process without blocking
														final Thread waitForThread = new Thread(() -> {
															try {
																process.waitFor();
															} catch (final InterruptedException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														});
														waitForThread.start();
													} catch (final IOException e) {
														if (LOG.isInfoEnabled()) {
															final String error = e.getMessage();
															LOG.error(error);
														}
													}
												} else if (new File(entry.getValue()).getName()
														.endsWith(Constant.EXEFILE)) {
													final ProcessBuilder pBuilder = new ProcessBuilder(entry.getValue(),
															"", "fr");
													pBuilder.redirectErrorStream(true);
													Process process;
													try {
														process = pBuilder.start();
														final InputStream istream = process.getInputStream();
														// thread to handle or gobble text sent from input stream
														new Thread(() -> {
															// try with resources
															try (BufferedReader reader = new BufferedReader(
																	new InputStreamReader(istream));) {
																String line;
																while ((line = reader.readLine()) != null) {
																	if (reader.readLine() == null) {
																		final Alert alert = new Alert(AlertType.ERROR);
																		final DialogPane dialogPane = alert
																				.getDialogPane();
																		Utility.getStyleClass(dialogPane);
																		dialogPane.setMinHeight(130);
																		dialogPane.setMaxHeight(130);
																		dialogPane.setPrefHeight(130);
																		alert.setContentText(Constant.GUI);
																		alert.setHeaderText(null);
																		alert.getDialogPane()
																				.lookupButton(ButtonType.OK)
																				.setVisible(true);
																		alert.showAndWait();
																	}
																	LOG.info(line);
																}
															} catch (final IOException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														}).start();
														// thread to get exit value from process without blocking
														final Thread waitForThread = new Thread(() -> {
															try {
																process.waitFor();
															} catch (final InterruptedException e) {
																if (LOG.isInfoEnabled()) {
																	final String error = e.getMessage();
																	LOG.error(error);
																}
															}
														});
														waitForThread.start();
													} catch (final IOException e) {
														if (LOG.isInfoEnabled()) {
															final String error = e.getMessage();
															LOG.error(error);
														}
													}
												}
											}
										}
									});
								}
							}
						}
					}
				});
			}
		});

		artDecorCItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final FileChooser fileChooser = new FileChooser();
					fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
					final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)",
							"*.xml");
					fileChooser.getExtensionFilters().add(extFilter);
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						fileChooser.setInitialDirectory(new File(sChemin));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						final File fileTransformed = ArtDecorService.transform(file.getAbsolutePath());
						if (fileTransformed != null && fileTransformed.length() > 0) {
							textAreaConsole.setStyle(Constant.STYILE);
							textAreaConsole.setText(LocalUtility.getString("message.template.path")
									+ fileTransformed.getAbsolutePath());
							final Alert alert = new Alert(AlertType.INFORMATION);
							final DialogPane dialogPane = alert.getDialogPane();
							final ButtonType okButtonType = new ButtonType(LocalUtility.getString("message.open"),
									ButtonData.OK_DONE);
							dialogPane.getButtonTypes().addAll(okButtonType);
							Utility.getStylesheets(dialogPane)
									.add(getClass().getResource(Constant.CSS).toExternalForm());
							Utility.getStyleClass(dialogPane);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText(LocalUtility.getString(SUCCES));
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							final Optional<ButtonType> result = alert.showAndWait();
							if (result.isPresent()) {
								if (result.get() == okButtonType) {
									final Desktop desktop = Desktop.getDesktop();
									try {
										desktop.open(fileTransformed);
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								} else if (result.get() == ButtonType.OK) {
									alert.close();
								}
							}
						}
					}
				});
			}
		});

		terItem.setOnAction(new EventHandler<>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10, 10, 10, 10));

					final JFXButton btnS = LocalUtility.buttonForKeyMFX("message.menu.fhir.termino.chargement");
					btnS.setOnMouseEntered(e -> btnS.setEffect(shadow));
					btnS.setOnMouseExited(e -> btnS.setEffect(null));

					final ImageView viewC = new ImageView(Constant.UPLOAD);
					viewC.setEffect(sepiaTone);
					btnS.setGraphic(viewC);
					btnS.setStyle(Constant.STYLE11);
					btnS.setPrefSize(220, 37);
					btnS.setMinSize(220, 37);
					btnS.setMaxSize(220, 37);

					final Region spacerr = new Region();
					spacerr.setMaxWidth(10);
					HBox.setHgrow(spacerr, Priority.ALWAYS);

					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					field.setPadding(new Insets(5, 5, 5, 5));
					field.setStyle(Constant.STYLE8);
					field.setPrefWidth(650);
					field.setMinWidth(650);
					field.setMaxWidth(650);
					field.setPrefHeight(37);
					field.setMinHeight(37);
					field.setMaxHeight(37);

					fieldTermino = IniFile.read("LOAD-TERMINOLOGY", FHIR);
					if (fieldTermino != null && !fieldTermino.isEmpty()) {
						field.setText(fieldTermino);
					}

					final JFXButton btnAddTer = LocalUtility.buttonForKeyMFX("message.menu.fhir.termino.add");
					btnAddTer.setOnMouseEntered(e -> btnAddTer.setEffect(shadow));
					btnAddTer.setOnMouseExited(e -> btnAddTer.setEffect(null));

					final ImageView viewAdd = new ImageView(Constant.ADD);
					viewAdd.setEffect(sepiaTone);
					btnAddTer.setGraphic(viewAdd);
					btnAddTer.setStyle(Constant.STYLE11);
					btnAddTer.setPrefSize(200, 37);
					btnAddTer.setMinSize(200, 37);
					btnAddTer.setMaxSize(200, 37);

					final Region spaceradd = new Region();
					spaceradd.setMaxWidth(10);
					HBox.setHgrow(spaceradd, Priority.ALWAYS);

					final ComboBox<String> comboBox = new ComboBox<>();
					comboBox.getItems().add("");
					comboBox.getItems().add(LocalUtility.getString("massage.mapping.invalid"));
					comboBox.setPrefWidth(350);
					comboBox.getStyleClass().add("custom-combo-box");

					final Region spacer = new Region();
					spacer.setMaxWidth(850);
					HBox.setHgrow(spacer, Priority.ALWAYS);

					final HBox hBoxLoad = new HBox();
					hBoxLoad.getChildren().addAll(btnS, spacerr, field, spaceradd, btnAddTer, spacer, comboBox);

					final TableView<ItemEntity> tableView = new TableView<>();
					tableView.prefHeightProperty().bind(vBox.heightProperty().multiply(0.85));

					final TableColumn<ItemEntity, Number> lineNumberColumn = new TableColumn<>("LINE");
					lineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));

					final TableColumn<ItemEntity, String> nameColumn = new TableColumn<>("NAME");
					nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

					final TableColumn<ItemEntity, Integer> oidColumn = new TableColumn<>("OID");
					oidColumn.setCellValueFactory(new PropertyValueFactory<>("oid"));

					final TableColumn<ItemEntity, String> urlColumn = new TableColumn<>("URI");
					urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));

					final TableColumn<ItemEntity, String> containsColumn = new TableColumn<>("CONTENT");
					containsColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));

					final TableColumn<ItemEntity, String> actionColumn = new TableColumn<>("ACTION");

					tableView.getColumns().addAll(lineNumberColumn, nameColumn, oidColumn, urlColumn, containsColumn,
							actionColumn);

					final TextField filterField = new TextField();
					filterField.setOnMouseEntered(e -> filterField.setEffect(shadow));
					filterField.setOnMouseExited(e -> filterField.setEffect(null));
					filterField.setStyle(Constant.STYLE8);
					filterField.setPromptText(LocalUtility.get("message.search.list"));
					filterField.setPrefWidth(880);
					filterField.setMaxWidth(880);

					actionColumn.setCellFactory(col -> {
						return new TableCell<ItemEntity, String>() {
							private final JFXButton button1 = new JFXButton("");
							{
								button1.setOnMouseEntered(e -> button1.setEffect(shadow));
								button1.setOnMouseExited(e -> button1.setEffect(null));
								button1.setStyle(Constant.STYLE1);
								button1.setPrefHeight(40);
								button1.setMinHeight(40);
								button1.setMaxHeight(40);
								final Tooltip tooltip1 = LocalUtility.createBoundTooltip("message.update.delete.item");
								button1.setTooltip(tooltip1);
								final ImageView viewC = new ImageView(Constant.UPDATEDEL);
								viewC.setEffect(sepiaTone);
								button1.setGraphic(viewC);

								button1.setOnAction(event -> {
									final ItemEntity item = getTableView().getItems().get(getIndex());
									openNewStage(item, tableView, filterField);
								});
							}

							@Override
							protected void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								setGraphic(button1);
							}
						};
					});

					lineNumberColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
					nameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
					oidColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.30));
					urlColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
					containsColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
					actionColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));

					final VBox vbox = new VBox(filterField, tableView);
					final Region space = new Region();
					space.setMaxHeight(20);
					VBox.setVgrow(space, Priority.ALWAYS);

					vBox.getChildren().addAll(hBoxLoad, space, vbox);
					terStage = new Stage();
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					terStage.titleProperty().bind(LocalUtility.createStringBinding(TERMINO));
					terStage.setScene(scene);
					terStage.setMaximized(true);
					terStage.show();

					btnAddTer.setOnAction(new EventHandler<>() {
						@Override
						public void handle(ActionEvent event) {
							Platform.runLater(() -> {
								final Stage newStage = new Stage();
								newStage.initModality(Modality.APPLICATION_MODAL);
								final VBox vbox = new VBox();
								final TextField section = new TextField("");
								final TextField name = new TextField();
								final TextField uri = new TextField();
								final TextField oid = new TextField();
								final TextField content = new TextField();
								section.setStyle(
										"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
								name.setStyle(
										"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
								uri.setStyle(
										"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
								oid.setStyle(
										"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
								content.setStyle(
										"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
								vbox.getChildren().addAll(createStyledLabel("Section: "), section,
										createStyledLabel("Name: "), name, createStyledLabel("Oid: "), oid,
										createStyledLabel("Uri: "), uri, createStyledLabel("Content: "), content);
								vbox.setPadding(new Insets(10, 10, 10, 10));

								final JFXButton button1 = new JFXButton("");
								button1.setOnMouseEntered(e -> button1.setEffect(shadow));
								button1.setOnMouseExited(e -> button1.setEffect(null));
								button1.setStyle(Constant.STYLE1);
								button1.setPrefHeight(40);
								button1.setMinHeight(40);
								button1.setMaxHeight(40);
								final Tooltip tooltip1 = LocalUtility
										.createBoundTooltip("message.menu.fhir.termino.add");
								button1.setTooltip(tooltip1);
								button1.setText(LocalUtility.getString("message.menu.fhir.termino.add"));
								final HBox hbox = new HBox();
								hbox.getChildren().addAll(button1);
								hbox.setAlignment(Pos.CENTER);

								final Region spacer102 = new Region();
								spacer101.setPrefHeight(5);
								VBox.setVgrow(spacer102, Priority.ALWAYS);

								vbox.getChildren().addAll(spacer102, hbox);

								final Scene scene = new Scene(vbox, 400, 400);
								newStage.setScene(scene);
								newStage.setTitle(LocalUtility.getString("message.menu.fhir.termino.add"));
								newStage.show();

								button1.setOnAction(new EventHandler<>() {
									@Override
									public void handle(ActionEvent event) {
										runTask(taskUpdateStage, progress);
										Platform.runLater(() -> {
											if (section.getText() != null && !section.getText().isEmpty()) {
												IniFile.writeTermino("NAME", name.getText(), section.getText(),
														new File(fieldTermino));
												IniFile.writeTermino("OID", oid.getText(), section.getText(),
														new File(fieldTermino));
												IniFile.writeTermino("URI", uri.getText(), section.getText(),
														new File(fieldTermino));
												IniFile.writeTermino("CONTENT", content.getText(), section.getText(),
														new File(fieldTermino));
												FhirUtilities.loadTerminology(new File(fieldTermino));
												final ObservableList<ItemEntity> itemList = FXCollections
														.observableArrayList();
												for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER
														.size(); iRow++) {
													final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER
															.get(iRow);
													ItemEntity item = new ItemEntity();
													item.setName(mapping[0]);
													item.setOid(mapping[1]);
													item.setUrl(mapping[2]);
													item.setContenu(mapping[3]);
													item.setLineNumber(Integer.parseInt(mapping[5]));
													item.setSection(mapping[4]);
													itemList.add(item);
												}
												final FilteredList<ItemEntity> filteredList = new FilteredList<>(
														itemList, p -> true);
												tableView.setItems(filteredList);

												filterField.textProperty()
														.addListener((observable, oldValue, newValue) -> {
															filteredList.setPredicate(item -> {
																if (newValue == null || newValue.isEmpty()) {
																	return true;
																}
																final String lowerCaseFilter = newValue.toLowerCase();
																return Optional.ofNullable(item.getName())
																		.map(name -> name.toLowerCase()
																				.contains(lowerCaseFilter))
																		.orElse(false);

															});
														});

												final Alert alert = new Alert(AlertType.INFORMATION);
												final DialogPane dialogPane = alert.getDialogPane();
												Utility.getStylesheets(dialogPane)
														.add(getClass().getResource(Constant.CSS).toExternalForm());
												Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(LocalUtility.getString(SUCCES));
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait().ifPresent(response -> {
													if (response == ButtonType.OK) {
														newStage.close();
													}
												});
											} else {
												final Alert alert = new Alert(AlertType.ERROR);
												final DialogPane dialogPane = alert.getDialogPane();
												Utility.getStylesheets(dialogPane)
														.add(getClass().getResource(Constant.CSS).toExternalForm());
												Utility.getStyleClass(dialogPane);
												dialogPane.setMinHeight(130);
												dialogPane.setMaxHeight(130);
												dialogPane.setPrefHeight(130);
												alert.setContentText(LocalUtility.getString("message.empty.section"));
												alert.setHeaderText(null);
												alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
												alert.showAndWait();

											}
										});
									}
								});
							});
						}

					});

					btnS.setOnAction(new EventHandler<>() {
						@Override
						public void handle(ActionEvent event) {
							runTask(taskUpdateStage, progress);
							Platform.runLater(() -> {
								if (FhirUtilities.CODESYSTEMSONTOSERVER.isEmpty()
										&& FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.isEmpty()) {
									final double wndwWidth = 150.0d;
									final double wndhHeigth = 150.0d;
									progress = new ProgressIndicator();
									progress.setMinWidth(wndwWidth);
									progress.setMinHeight(wndhHeigth);
									progress.setProgress(0.25F);
									final VBox updatePane = new VBox();
									updatePane.setPadding(new Insets(10));
									updatePane.setSpacing(5.0d);
									updatePane.setAlignment(Pos.CENTER);
									Utility.getChildrenNode(updatePane).addAll(progress);
									updatePane.setStyle(FONTIME2);
									taskUpdateStage = new Stage(StageStyle.UNDECORATED);
									taskUpdateStage.setScene(new Scene(updatePane, 170, 170));

									Task<Void> loadTerminologyTask = new Task<>() {
										@Override
										protected Void call() throws Exception {
											if (FhirUtilities.CODESYSTEMSONTOSERVER.isEmpty()
													&& FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.isEmpty()) {
												FhirUtilities.loadTerminology(new File(field.getText()));
											}
											return null;
										}
									};
									loadTerminologyTask.setOnSucceeded(ev -> {
										taskUpdateStage.close();
										final ObservableList<ItemEntity> itemList = FXCollections.observableArrayList();
										for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER.size(); iRow++) {
											final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER.get(iRow);
											ItemEntity item = new ItemEntity();
											item.setName(mapping[0]);
											item.setOid(mapping[1]);
											item.setUrl(mapping[2]);
											item.setContenu(mapping[3]);
											item.setLineNumber(Integer.parseInt(mapping[5]));
											item.setSection(mapping[4]);
											itemList.add(item);
										}

										final FilteredList<ItemEntity> filteredList = new FilteredList<>(itemList,
												p -> true);
										tableView.setItems(filteredList);

										filterField.textProperty().addListener((observable, oldValue, newValue) -> {
											filteredList.setPredicate(item -> {
												if (newValue == null || newValue.isEmpty()) {
													return true;
												}
												final String lowerCaseFilter = newValue.toLowerCase();
												return Optional.ofNullable(item.getName())
														.map(name -> name.toLowerCase().contains(lowerCaseFilter))
														.orElse(false);

											});
										});

										comboBox.setOnAction(evt -> {
											final int selectedItem = comboBox.getSelectionModel().getSelectedIndex();
											if (selectedItem == 1) {

												FilteredList<ItemEntity> filteredData = new FilteredList<>(itemList,
														item -> item.getName() == null || item.getName().isEmpty()
																|| item.getOid() == null || item.getOid().isEmpty()
																|| item.getUrl() == null || item.getUrl().isEmpty()
																|| item.getContenu() == null
																|| item.getContenu().isEmpty());

												// Set the filtered list as the items for the TableView
												tableView.setItems(filteredData);
												final FilteredList<ItemEntity> filteredListe = new FilteredList<>(
														filteredData, p -> true);
												tableView.setItems(filteredListe);

												filterField.textProperty()
														.addListener((observable, oldValue, newValue) -> {
															filteredListe.setPredicate(item -> {
																if (newValue == null || newValue.isEmpty()) {
																	return true;
																}
																final String lowerCaseFilter = newValue.toLowerCase();
																return Optional.ofNullable(item.getName())
																		.map(name -> name.toLowerCase()
																				.contains(lowerCaseFilter))
																		.orElse(false);
															});
														});
											} else if (selectedItem == 0) {
												final ObservableList<ItemEntity> itemListes = FXCollections
														.observableArrayList();
												for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER
														.size(); iRow++) {
													final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER
															.get(iRow);
													ItemEntity item = new ItemEntity();
													item.setName(mapping[0]);
													item.setOid(mapping[1]);
													item.setUrl(mapping[2]);
													item.setContenu(mapping[3]);
													item.setLineNumber(Integer.parseInt(mapping[5]));
													item.setSection(mapping[4]);
													itemListes.add(item);
												}
												final FilteredList<ItemEntity> filteredListes = new FilteredList<>(
														itemListes, p -> true);
												tableView.setItems(filteredListes);

												filterField.textProperty()
														.addListener((observable, oldValue, newValue) -> {
															filteredListes.setPredicate(item -> {
																if (newValue == null || newValue.isEmpty()) {
																	return true;
																}
																final String lowerCaseFilter = newValue.toLowerCase();
																return Optional.ofNullable(item.getName())
																		.map(name -> name.toLowerCase()
																				.contains(lowerCaseFilter))
																		.orElse(false);

															});
														});
											}
										});

									});
									progress.progressProperty().bind(loadTerminologyTask.progressProperty());
									taskUpdateStage.show();
									new Thread(loadTerminologyTask).start();
								}
								final ObservableList<ItemEntity> itemList = FXCollections.observableArrayList();
								for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER.size(); iRow++) {
									final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER.get(iRow);
									ItemEntity item = new ItemEntity();
									item.setName(mapping[0]);
									item.setOid(mapping[1]);
									item.setUrl(mapping[2]);
									item.setContenu(mapping[3]);
									item.setLineNumber(Integer.parseInt(mapping[5]));
									item.setSection(mapping[4]);
									itemList.add(item);
								}
								final FilteredList<ItemEntity> filteredList = new FilteredList<>(itemList, p -> true);
								tableView.setItems(filteredList);

								filterField.textProperty().addListener((observable, oldValue, newValue) -> {
									filteredList.setPredicate(item -> {
										if (newValue == null || newValue.isEmpty()) {
											return true;
										}
										final String lowerCaseFilter = newValue.toLowerCase();
										return Optional.ofNullable(item.getName())
												.map(name -> name.toLowerCase().contains(lowerCaseFilter))
												.orElse(false);

									});
								});

								comboBox.setOnAction(ev -> {
									final int selectedItem = comboBox.getSelectionModel().getSelectedIndex();
									if (selectedItem == 1) {

										FilteredList<ItemEntity> filteredData = new FilteredList<>(itemList,
												item -> item.getName() == null || item.getName().isEmpty()
														|| item.getOid() == null || item.getOid().isEmpty()
														|| item.getUrl() == null || item.getUrl().isEmpty()
														|| item.getContenu() == null || item.getContenu().isEmpty());

										tableView.setItems(filteredData);
										final FilteredList<ItemEntity> filteredListe = new FilteredList<>(filteredData,
												p -> true);
										tableView.setItems(filteredListe);

										filterField.textProperty().addListener((observable, oldValue, newValue) -> {
											filteredListe.setPredicate(item -> {
												if (newValue == null || newValue.isEmpty()) {
													return true;
												}
												final String lowerCaseFilter = newValue.toLowerCase();
												return Optional.ofNullable(item.getName())
														.map(name -> name.toLowerCase().contains(lowerCaseFilter))
														.orElse(false);
											});
										});
									} else if (selectedItem == 0) {
										final ObservableList<ItemEntity> itemListes = FXCollections
												.observableArrayList();
										for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER.size(); iRow++) {
											final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER.get(iRow);
											ItemEntity item = new ItemEntity();
											item.setName(mapping[0]);
											item.setOid(mapping[1]);
											item.setUrl(mapping[2]);
											item.setContenu(mapping[3]);
											item.setLineNumber(Integer.parseInt(mapping[5]));
											item.setSection(mapping[4]);
											itemList.add(item);
										}
										final FilteredList<ItemEntity> filteredListes = new FilteredList<>(itemListes,
												p -> true);
										tableView.setItems(filteredListes);

										filterField.textProperty().addListener((observable, oldValue, newValue) -> {
											filteredListes.setPredicate(item -> {
												if (newValue == null || newValue.isEmpty()) {
													return true;
												}
												final String lowerCaseFilter = newValue.toLowerCase();
												return Optional.ofNullable(item.getName())
														.map(name -> name.toLowerCase().contains(lowerCaseFilter))
														.orElse(false);

											});
										});
									}
								});

							});
						}
					});

				});
			}
		});

		generateCsItem.setOnAction(new EventHandler<>() {

			@SuppressWarnings("unchecked")
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					final GridPane gridPane = new GridPane();
					gridPane.setPadding(new Insets(20));
					gridPane.setHgap(10);
					gridPane.setVgap(20);

					final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.menu.fhir.import"));
					label.setStyle(Constant.STYLE66);

					final TextField textField = new TextField();
					textField.setOnMouseEntered(e -> textField.setEffect(shadow));
					textField.setOnMouseExited(e -> textField.setEffect(null));
					textField.setPadding(new Insets(5, 5, 5, 5));
					textField.setStyle(Constant.STYLE8);
					textField.setPrefWidth(500);
					textField.setPrefHeight(30);

					final JFXButton btnSF = new JFXButton("");
					btnSF.setOnMouseEntered(e -> btnSF.setEffect(shadow));
					btnSF.setOnMouseExited(e -> btnSF.setEffect(null));
					final ImageView viewSF = new ImageView(Constant.SFILE);
					viewSF.setEffect(sepiaTone);
					btnSF.setGraphic(viewSF);
					btnSF.setStyle(Constant.STYLE1);
					btnSF.setPrefSize(30, 30);
					btnSF.setMinSize(30, 30);
					btnSF.setMaxSize(30, 30);
					final Tooltip tooltipSF = LocalUtility.createBoundTooltip("message.choose.file");
					btnSF.setTooltip(tooltipSF);

					final TableView<CodeSystemEntity> tableView = new TableView<>();

					TableColumn<CodeSystemEntity, String> codeColumn = new TableColumn<>("Code");
					codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

					TableColumn<CodeSystemEntity, String> nameColumn = new TableColumn<>("DisplayName");
					nameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));

					TableColumn<CodeSystemEntity, String> sysNameColumn = new TableColumn<>("SystemName");
					sysNameColumn.setCellValueFactory(new PropertyValueFactory<>("systemName"));

					TableColumn<CodeSystemEntity, String> sysCodeColumn = new TableColumn<>("SystemCode");
					sysCodeColumn.setCellValueFactory(new PropertyValueFactory<>("systemCode"));

					TableColumn<CodeSystemEntity, String> activeColumn = new TableColumn<>("Actif");
					activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

					tableView.getColumns().addAll(codeColumn, nameColumn, sysNameColumn, sysCodeColumn, activeColumn);

					final ObservableList<CodeSystemEntity> data = FXCollections
							.observableArrayList(new CodeSystemEntity("", "", "", "", ""));

					tableView.setItems(data);

					codeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
					nameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
					sysNameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
					sysCodeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
					activeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));

					final Stage stage = new Stage();
					final TextArea textArea = new TextArea();
					final TextArea textArea1 = new TextArea();

					btnSF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"EXCEL files (*.xlsx)", "*.xlsx");
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTPUSED, MEMORY);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showOpenDialog(stage);
							if (file != null) {
								textField.setText(file.getAbsolutePath());
							}
							runTask(taskUpdateStage, progress);
							Platform.runLater(() -> {
								textArea.clear();
								textArea1.clear();
								tableView.getItems().clear();
								final Workbook workbookRdf = new Workbook();
								// Load an Excel file
								workbookRdf.loadFromFile(textField.getText());
								Worksheet worksheet = null;
								for (final Object sheet : workbookRdf.getWorksheets()) {
									final String sheetName = ((Worksheet) sheet).getName();
									if (sheetName.equalsIgnoreCase(Constant.VOCANS)) {
										worksheet = ((Worksheet) sheet);
									}
								}
								final List<CodeSystemEntity> listR = new ArrayList<>();
								final int maxRow = worksheet.getLastRow();
								String obsolet = null;
								int count = 0;
								for (int row = 2; row <= maxRow; row++) {
									final CodeSystemEntity response = new CodeSystemEntity();
									final CellRange cell1 = worksheet.getCellRange(row, 1);
									final CellRange cell2 = worksheet.getCellRange(row, 2);
									final CellRange cell3 = worksheet.getCellRange(row, 3);
									final CellRange cell4 = worksheet.getCellRange(row, 4);
									final CellRange cell10 = worksheet.getCellRange(row, 10);
									if (cell1.getValue() == null || cell1.getValue().isEmpty()) {
										final String message = "\nLe code de la ligne " + row + " est absent.";
										textArea.appendText(message);
									} else {
										response.setCode(cell1.getValue());
									}
									if (cell2.getValue() == null || cell2.getValue().isEmpty()) {
										final String message = "\nLe display de la ligne " + row + " et du code '"
												+ cell1.getValue() + "' est absent.";
										textArea.appendText(message);

									} else {
										response.setDisplayName(cell2.getValue());
									}

									if (cell3.getValue() == null || cell3.getValue().isEmpty()) {
										final String message = "\nLe systemName de la ligne " + row + " et du code '"
												+ cell1.getValue() + "' est absent.";
										textArea.appendText(message);
									} else {
										response.setSystemName(cell3.getValue());
									}

									if (cell4.getValue() == null || cell4.getValue().isEmpty()) {
										final String message = "\nLe codeSystem.code  de la ligne " + row
												+ " et du code '" + cell1.getValue() + "' est absent.";
										textArea.appendText(message);
									} else {
										response.setSystemCode(cell4.getValue());
									}
									if (cell10.getValue() == null || cell10.getValue().isEmpty()) {
										obsolet = "NON";
										response.setObsolete(obsolet);
										response.setActive(obsolet);
									} else {
										obsolet = cell10.getValue().toUpperCase();
										response.setObsolete(obsolet);
										response.setActive(obsolet);
									}

									if ("NON".equalsIgnoreCase(response.getObsolete()) || response.getObsolete() == null
											|| response.getObsolete().isEmpty()) {
										listR.add(response);
										count++;
									}
								}

								final ObservableList<CodeSystemEntity> data = FXCollections.observableArrayList();
								data.addAll(listR);
								tableView.setItems(data);
								textArea.appendText("\n\n === NOMBRE DE LIGNES IMPORTEES: " + count + " ===\n");
								IniFile.write("LAST-PATH-USED", new File(textField.getText()).getParent(), "MEMORY");

							});
						}
					});

					final Label label1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.nom.code.system"));
					label1.setStyle(Constant.STYLE66);

					final TextField textField1 = new TextField();
					textField1.setOnMouseEntered(e -> textField1.setEffect(shadow));
					textField1.setOnMouseExited(e -> textField1.setEffect(null));
					textField1.setPadding(new Insets(5, 5, 5, 5));
					textField1.setStyle(Constant.STYLE8);
					textField1.setPrefWidth(200);
					textField1.setPrefHeight(30);
					textField1.setText("TRE_R308_TAASIP");

					final Label label2 = new Label("ID");
					label2.setStyle(Constant.STYLE66);

					final TextField textField2 = new TextField();
					textField2.setOnMouseEntered(e -> textField2.setEffect(shadow));
					textField2.setOnMouseExited(e -> textField2.setEffect(null));
					textField2.setPadding(new Insets(5, 5, 5, 5));
					textField2.setStyle(Constant.STYLE8);
					textField2.setPrefWidth(200);
					textField2.setPrefHeight(30);
					textField2.setText("1.2.250.1.213.1.1.4.322");

					final JFXButton btnC = LocalUtility.buttonForKeyMFX("message.convert.termino");
					btnC.setOnMouseEntered(e -> btnC.setEffect(shadow));
					btnC.setOnMouseExited(e -> btnC.setEffect(null));
					final ImageView viewC = new ImageView(Constant.CONVERTFHIR);
					viewC.setEffect(sepiaTone);
					btnC.setGraphic(viewC);
					btnC.setStyle(Constant.STYLE1);
					btnC.setPrefSize(140, 40);
					btnC.setMinSize(140, 40);
					btnC.setMaxSize(140, 40);

					btnC.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (!textField.getText().isEmpty()) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									int iLine = 0;
									final org.hl7.fhir.r4.model.CodeSystem monCS = new org.hl7.fhir.r4.model.CodeSystem();
									for (int rowIndex = 0; rowIndex < tableView.getItems().size(); rowIndex++) {
										boolean bLineIsCorrect = true;
										String sCode = null;
										final CodeSystemEntity rowData = tableView.getItems().get(rowIndex);
										if (rowData != null && rowData.getCode() != null) {
											sCode = rowData.getCode();
										}
										String sDisplayName = null;
										if (rowData != null && rowData.getDisplayName() != null) {
											sDisplayName = rowData.getDisplayName();
											if (sDisplayName != null)
												sDisplayName = sDisplayName.replace("\n", "").replace("\r", "");
											sDisplayName = sDisplayName.replace("\"", "'");
											sDisplayName = sDisplayName.trim();
										}
										textArea.appendText("\n" + sCode + " " + sDisplayName);
										final String sSystemCode = "1.2.250.1.213.1.1.4.322";
										if (sCode == null) {
											sCode = "";
										}
										if (sDisplayName == null) {
											sDisplayName = "";
										}
										if (sCode.trim().equals("") || sDisplayName.trim().equals("")) {
											bLineIsCorrect = false;
										}
										if (iLine == 0) {
											// Première ligne : on créé le Header
											final String sUrl = IniFile.read("RACINE-CANONIQUE", "FHIR")
													+ "CodeSystem/";
											createCodeSystemHeader(textField2.getText(), sUrl + sSystemCode,
													"urn:oid:" + sSystemCode, textField1.getText(), "V1", "TA_ASIP",
													monCS);
										}
										if (bLineIsCorrect) {
											final org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent monConcept = new org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent();
											monConcept.setCode(sCode);
											monConcept.setDisplay(sDisplayName);
											monCS.getConcept().add(monConcept);
											iLine++;
										}
									}
									final FhirContext fhirContext = FhirContext.forR4();
									final String codeSystemCn = fhirContext.newJsonParser().setPrettyPrint(true)
											.encodeResourceToString(monCS);
									textArea1.setText(codeSystemCn);
								});
							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.empty.csv"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}
						}
					});

					final JFXButton btnE = LocalUtility.buttonForKeyMFX("message.datasave");
					btnE.setOnMouseEntered(e -> btnE.setEffect(shadow));
					btnE.setOnMouseExited(e -> btnE.setEffect(null));
					final ImageView viewCE = new ImageView(Constant.SAVEDISKFILE);
					viewCE.setEffect(sepiaTone);
					btnE.setGraphic(viewCE);
					btnE.setStyle(Constant.STYLE1);
					btnE.setPrefSize(140, 40);
					btnE.setMinSize(140, 40);
					btnE.setMaxSize(140, 40);

					btnE.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (!textArea1.getText().isEmpty()) {
								final String fichierSortieJson = new File(textField.getText()).getParent()
										+ File.separator + textField1.getText() + ".json";
								try {
									Files.write(new File(fichierSortieJson).toPath(), textArea1.getText().getBytes());
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.info(error);
									}
								}
								final Alert alert = new Alert(AlertType.INFORMATION);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString(SUCCES));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								final Optional<ButtonType> result = alert.showAndWait();
								if (result.isPresent() && result.get() == ButtonType.OK) {
									try {
										Desktop.getDesktop().open(new File(fichierSortieJson));
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.info(error);
										}
									}
								}
							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.empty.json"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}
						}
					});

					final JFXButton btnO = new JFXButton("");
					btnO.setOnMouseEntered(e -> btnO.setEffect(shadow));
					btnO.setOnMouseExited(e -> btnO.setEffect(null));
					final ImageView viewCO = new ImageView(Constant.OPENFOLDER);
					viewCO.setEffect(sepiaTone);
					btnO.setGraphic(viewCO);
					btnO.setStyle(Constant.STYLE1);
					btnO.setPrefSize(40, 40);
					btnO.setMinSize(40, 40);
					btnO.setMaxSize(40, 40);

					btnO.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							final String fichierSortieJson = new File(textField.getText()).getParent() + File.separator
									+ textField1.getText() + ".json";
							if (new File(fichierSortieJson).exists()) {
								File folder = new File(fichierSortieJson).getParentFile();
								if (folder.exists() && folder.isDirectory()) {
									try {
										Desktop.getDesktop().open(folder);
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.info(error);
										}
									}
								}
							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.empty.save.file"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}
						}
					});

					final Region spacerB = new Region();
					spacerB.setPrefWidth(500);
					spacerB.setMaxWidth(500);
					spacerB.setMinWidth(500);
					HBox.setHgrow(spacerB, Priority.ALWAYS);

					gridPane.add(label, 0, 0);
					gridPane.add(textField, 1, 0);
					gridPane.add(btnSF, 2, 0);
					gridPane.add(label1, 0, 1);
					gridPane.add(textField1, 1, 1);
					gridPane.add(label2, 2, 1);
					gridPane.add(textField2, 3, 1);
					gridPane.add(spacerB, 4, 1);
					gridPane.add(btnC, 5, 1);
					gridPane.add(btnE, 6, 1);
					gridPane.add(btnO, 7, 1);

					final HBox hBox = new HBox();
					hBox.getChildren().add(gridPane);

					final VBox vBox = new VBox();

					final StackPane leftPane = new StackPane();
					leftPane.setPadding(new Insets(10));
					leftPane.getChildren().add(tableView);
					final StackPane rightPane = new StackPane();
					rightPane.setPadding(new Insets(10));
					textArea.setWrapText(true);
					textArea.setEditable(false);
					rightPane.getChildren().add(textArea);

					final SplitPane splitPane = new SplitPane();
					splitPane.getItems().addAll(leftPane, rightPane);
					splitPane.setDividerPositions(0.4);

					splitPane.setMaxHeight(600);
					splitPane.setPrefHeight(600);
					splitPane.setMinHeight(600);

					textArea1.setWrapText(true);
					textArea1.setEditable(false);
					textArea1.setMaxHeight(280);
					textArea1.setPrefHeight(280);
					textArea1.setMinHeight(280);
					textArea1.setPadding(new Insets(10, 0, 10, 0));

					vBox.getChildren().addAll(hBox, splitPane, textArea1);

					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					stage.titleProperty().bind(LocalUtility.createStringBinding(FHIRCODESYS));
					stage.setScene(scene);
					stage.setMaximized(true);
					stage.show();
				});
			}
		});

		logsItem.setOnAction(new EventHandler<>() {

			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					final GridPane gridPane = new GridPane();
					gridPane.setPadding(new Insets(20));
					gridPane.setHgap(10);
					gridPane.setVgap(20);

					final ComboBox<String> comboBox1 = new ComboBox<>();
					comboBox1.setPadding(new Insets(5, 5, 5, 5));
					comboBox1.setPrefWidth(400);
					comboBox1.getStyleClass().add("custom-combo-box");

					final TextArea textArea = new TextArea();
					textArea.setWrapText(true);
					textArea.setEditable(false);
					textArea.setPadding(new Insets(5, 5, 5, 5));
					textArea.prefHeightProperty().bind(gridPane.heightProperty());
					textArea.prefWidthProperty().bind(gridPane.widthProperty());

					final Label typeServ = new Label();
					typeServ.setStyle(Constant.STYLE66);

					final StringProperty lineCountText = new SimpleStringProperty(LocalUtility.get("message.nbr.line"));
					typeServ.textProperty().bind(lineCountText);

					final List<File> list = FhirUtilities.getLogFiles();
					comboBox1.getItems().add("");
					for (final File file : list) {
						comboBox1.getItems().add(file.getName());
					}

					comboBox1.setOnAction(ev -> {
						final int selectedItem = comboBox1.getSelectionModel().getSelectedIndex();
						if (selectedItem != 0) {
							final String fileName = comboBox1.getValue();
							if (new File(Constant.LOGFHIRFOLDER + "\\" + fileName) != null) {
								try {
									final String content = Files.readString(Path
											.of(new File(Constant.LOGFHIRFOLDER + "\\" + fileName).getAbsolutePath()));
									textArea.setText(content);
								} catch (final IOException ex) {
									if (LOG.isInfoEnabled()) {
										final String error = ex.getMessage();
										LOG.info(error);
									}
								}
							}

						} else if (selectedItem == 0) {
							textArea.clear();
						}
						final String texts = textArea.getText();
						final int lineCount = texts.isEmpty() ? 0 : texts.split("\n").length;
						lineCountText.set(LocalUtility.get("message.nbr.line") + lineCount);
					});

					final ComboBox<String> comboBox2 = new ComboBox<>();
					comboBox2.setPadding(new Insets(5, 5, 5, 5));
					comboBox2.setPrefWidth(400);
					comboBox2.getStyleClass().add("custom-combo-box");
					comboBox2.getItems().addAll("", LocalUtility.getString("message.delete.line"),
							LocalUtility.getString("message.delete.double"),
							LocalUtility.getString("message.line.error"));

					comboBox2.setOnAction(ev -> {
						final int selectedItem = comboBox2.getSelectionModel().getSelectedIndex();
						// supprimer les lignes vides
						if (selectedItem == 1) {
							final String text = textArea.getText();
							final String cleanedText = text.replaceAll("(?m)^[ \t]*\r?\n", "");
							textArea.setText(cleanedText);
							// supprimer les doublons
						} else if (selectedItem == 2) {
							String text = textArea.getText();
							final List<String> lines = List.of(text.split("\n"));
							final Set<String> uniqueLines = new LinkedHashSet<>(lines);
							final String cleanedText = String.join("\n", uniqueLines);
							textArea.setText(cleanedText);
							// ligne commençant par - en erreur
						} else if (selectedItem == 3) {
							final String text = textArea.getText();
							final List<String> errorLines = List.of(text.split("\n")).stream()
									.filter(line -> line.startsWith("(-)")).collect(Collectors.toList());
							textArea.setText(String.join("\n", errorLines));

						} else if (selectedItem == 0) {
							final String fileName = comboBox1.getValue();
							if (new File(Constant.LOGFHIRFOLDER + "\\" + fileName) != null) {
								try {
									final String content = Files.readString(Path
											.of(new File(Constant.LOGFHIRFOLDER + "\\" + fileName).getAbsolutePath()));
									textArea.setText(content);
								} catch (final IOException ex) {
									if (LOG.isInfoEnabled()) {
										final String error = ex.getMessage();
										LOG.info(error);
									}
								}
							}
						}
						final String texts = textArea.getText();
						final int lineCount = texts.isEmpty() ? 0 : texts.split("\n").length;
						lineCountText.set(LocalUtility.get("message.nbr.line") + lineCount);
					});

					gridPane.add(comboBox1, 0, 0);
					gridPane.add(comboBox2, 1, 0);
					gridPane.add(typeServ, 0, 1);
					gridPane.add(textArea, 0, 2, 2, 1);

					logStage = new Stage();
					final Scene scene = new Scene(gridPane);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					logStage.titleProperty().bind(LocalUtility.createStringBinding(FHIRLOG));
					logStage.setScene(scene);
					logStage.setMaximized(true);
					logStage.show();
				});
			}
		});

		servItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					final GridPane gridPane = new GridPane();
					gridPane.setAlignment(Pos.TOP_LEFT);
					gridPane.setHgap(20);
					gridPane.setVgap(40);
					gridPane.setPadding(new Insets(20, 20, 20, 20));
					final Label typeServ = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.fhir.type.server"));
					typeServ.setStyle(Constant.STYLE66);
					gridPane.add(typeServ, 0, 0);
					final TextField typeServField = new TextField();
					typeServField.setOnMouseEntered(e -> typeServField.setEffect(shadow));
					typeServField.setOnMouseExited(e -> typeServField.setEffect(null));
					typeServField.setPadding(new Insets(5, 5, 5, 5));
					typeServField.setStyle(Constant.STYLE8);
					typeServField.setPrefWidth(500);
					typeServField.setPrefHeight(30);
					gridPane.add(typeServField, 1, 0);
					final Label urlServ = LocalUtility.labelForValue(() -> LocalUtility.get("message.fhir.url.server"));
					urlServ.setStyle(Constant.STYLE66);
					gridPane.add(urlServ, 0, 1);
					final TextField urlServField = new TextField();
					urlServField.setOnMouseEntered(e -> urlServField.setEffect(shadow));
					urlServField.setOnMouseExited(e -> urlServField.setEffect(null));
					urlServField.setPadding(new Insets(5, 5, 5, 5));
					urlServField.setStyle(Constant.STYLE8);
					urlServField.setPrefWidth(500);
					urlServField.setPrefHeight(30);
					gridPane.add(urlServField, 1, 1);
					final Label usernameLabel = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.fhir.user.server"));
					usernameLabel.setStyle(Constant.STYLE66);
					gridPane.add(usernameLabel, 0, 2);
					final TextField usernameField = new TextField();
					usernameField.setOnMouseEntered(e -> usernameField.setEffect(shadow));
					usernameField.setOnMouseExited(e -> usernameField.setEffect(null));
					usernameField.setPadding(new Insets(5, 5, 5, 5));
					usernameField.setStyle(Constant.STYLE8);
					usernameField.setPrefWidth(500);
					usernameField.setPrefHeight(30);
					gridPane.add(usernameField, 1, 2);
					final Label passwordLabel = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.fhir.pwd.server"));
					passwordLabel.setStyle(Constant.STYLE66);
					gridPane.add(passwordLabel, 0, 3);
					final PasswordField passwordField = new PasswordField();
					passwordField.setOnMouseEntered(e -> passwordField.setEffect(shadow));
					passwordField.setOnMouseExited(e -> passwordField.setEffect(null));
					passwordField.setPadding(new Insets(5, 5, 5, 5));
					passwordField.setStyle(Constant.STYLE8);
					passwordField.setPrefWidth(500);
					passwordField.setPrefHeight(30);
					gridPane.add(passwordField, 1, 3);

					final Label loginLabel = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.fhir.login.server"));
					loginLabel.setStyle(Constant.STYLE66);
					gridPane.add(loginLabel, 0, 4);
					final TextField loginField = new TextField();
					loginField.setOnMouseEntered(e -> loginField.setEffect(shadow));
					loginField.setOnMouseExited(e -> loginField.setEffect(null));
					loginField.setPadding(new Insets(5, 5, 5, 5));
					loginField.setStyle(Constant.STYLE8);
					loginField.setPrefWidth(500);
					loginField.setPrefHeight(30);
					gridPane.add(loginField, 1, 4);

					final Label passwordLoginLabel = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.fhir.pwdlogin.server"));
					passwordLoginLabel.setStyle(Constant.STYLE66);
					gridPane.add(passwordLoginLabel, 0, 5);
					final PasswordField passwordLoginField = new PasswordField();
					passwordLoginField.setOnMouseEntered(e -> passwordLoginField.setEffect(shadow));
					passwordLoginField.setOnMouseExited(e -> passwordLoginField.setEffect(null));
					passwordLoginField.setPadding(new Insets(5, 5, 5, 5));
					passwordLoginField.setStyle(Constant.STYLE8);
					passwordLoginField.setPrefWidth(500);
					passwordLoginField.setPrefHeight(30);
					gridPane.add(passwordLoginField, 1, 5);

					final JFXButton btnS = LocalUtility.buttonForKeyMFX("message.update");
					btnS.setOnMouseEntered(e -> btnS.setEffect(shadow));
					btnS.setOnMouseExited(e -> btnS.setEffect(null));
					final ImageView viewC = new ImageView(Constant.CHECKVALIDATE);
					viewC.setEffect(sepiaTone);
					btnS.setGraphic(viewC);
					btnS.setStyle(Constant.STYLE1);
					btnS.setPrefSize(140, 40);
					btnS.setMinSize(140, 40);
					btnS.setMaxSize(140, 40);
					GridPane.setHalignment(btnS, HPos.RIGHT);
					gridPane.add(btnS, 1, 6);

					final StackPane root = new StackPane();
					root.getChildren().add(gridPane);
					servStage = new Stage();
					final Scene scene = new Scene(root, 800, 600);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					servStage.titleProperty().bind(LocalUtility.createStringBinding(SERVER));
					servStage.setScene(scene);
					servStage.setMaximized(false);
					servStage.centerOnScreen();
					servStage.show();

					urlServField.setText(IniFile.read("URL-ONTOSERVER", FHIR));
					typeServField.setText(IniFile.read("SERVER-TYPE", FHIR));
					usernameField.setText(IniFile.read("LOINC-USER", FHIR));
					passwordField.setText(IniFile.read("LOINC-PASSWORD", FHIR));
					loginField.setText(IniFile.read("LOGIN", FHIR));
					passwordLoginField.setText(IniFile.read("PASSWORD", FHIR));

					btnS.setOnAction(new EventHandler<>() {
						@Override
						public void handle(ActionEvent event) {
							Platform.runLater(() -> {
								IniFile.write("URL-ONTOSERVER", urlServField.getText(), FHIR);
								IniFile.write("SERVER-TYPE", typeServField.getText(), FHIR);
								IniFile.write("LOINC-USER", usernameField.getText(), FHIR);
								IniFile.write("LOINC-PASSWORD", passwordField.getText(), FHIR);
								IniFile.write("LOGIN", loginField.getText(), FHIR);
								IniFile.write("PASSWORD", passwordLoginField.getText(), FHIR);
								final Alert alert = new Alert(AlertType.INFORMATION);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString(SUCCES));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							});
						}
					});
				});
			}
		});

		conItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10, 10, 10, 10));
					final StackPane leftPane = new StackPane();
					leftPane.setPadding(new Insets(10));
					final StackPane rightPane = new StackPane();
					rightPane.setPadding(new Insets(10));
					final SplitPane splitPane = new SplitPane();
					splitPane.setMaxHeight(900);
					splitPane.setPrefHeight(900);
					splitPane.setMinHeight(900);
					splitPane.getItems().addAll(leftPane, rightPane);
					splitPane.setDividerPositions(0.3);
					final CheckBox checkAll = LocalUtility.boxForKeyCheck("message.check.all");
					if (listViewCq == null) {
						listViewCq = new ListView<>();
					}

					final Map<String, Boolean> selectedItems = new HashMap<>();
					final Map<String, Boolean> disabledItems = new HashMap<>();
					for (final String item : itemsFiltred) {
						selectedItems.put(item, false);
						disabledItems.put(item, false);
					}
					listViewCq.prefHeightProperty().bind(splitPane.heightProperty());
					listViewCq.setCellFactory(param -> new ListCell<String>() {
						private final CheckBox checkBox;

						{
							checkBox = new CheckBox();
							checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
								if (getItem() != null && !disabledItems.get(getItem())) {
									selectedItems.put(getItem(), isNowSelected);
									numberOfCheckedItems = countCheckedItems(selectedItems);
									updateCheckedItemsCount(textAreaStat);
								}
							});
						}

						@Override
						protected void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
							if (empty || item == null) {
								setGraphic(null);
							} else {
								final String escapedText = item.replace("_", "__");
								checkBox.setText(escapedText);
								checkBox.setSelected(selectedItems.get(item));
								checkBox.setDisable(disabledItems.get(item));
								setGraphic(checkBox);
							}
						}
					});

					// "Select All" CheckBox listener to select/deselect all checkboxes
					checkAll.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
						for (String item : itemsFiltred) {
							if (!disabledItems.get(item)) {
								selectedItems.put(item, isNowSelected);
							}
						}
						numberOfCheckedItems = countCheckedItems(selectedItems);
						updateCheckedItemsCount(textAreaStat);
						listViewCq.refresh(); // Refresh to update checkbox states in cells
					});

					final VBox layout = new VBox(10);
					layout.getChildren().addAll(checkAll, listViewCq);
					layout.prefHeightProperty().bind(splitPane.heightProperty());
					leftPane.getChildren().add(layout);

					final Label textAreaTitle = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.menu.fhir.stat"));
					textAreaTitle.setStyle(Constant.STYLE6);
					if (textAreaStat == null) {
						textAreaStat = new TextArea();
					}
					textAreaStat.positionCaret(0);
					textAreaStat.setWrapText(true);
					textAreaStat.setEditable(false);
					textAreaStat.setPadding(new Insets(5, 5, 5, 5));
					textAreaStat.getStyleClass().add("text-area-green");

					final VBox vbox = new VBox(5);
					vbox.getChildren().addAll(textAreaTitle, textAreaStat);
					textAreaStat.prefHeightProperty().bind(vbox.heightProperty());

					if (textAreaS == null) {
						textAreaS = new TextArea();
					}
					textAreaS.clear();
					textAreaS.setWrapText(true);
					textAreaS.setEditable(false);
					textAreaS.setPadding(new Insets(5, 5, 5, 5));
					textAreaS.getStyleClass().add("text-area-green");

					final SplitPane pane = new SplitPane(vbox, textAreaS);
					pane.setDividerPositions(0.3);
					pane.setOrientation(Orientation.VERTICAL);
					pane.prefHeightProperty().bind(splitPane.heightProperty());

					final VBox layoutArea = new VBox(pane);
					rightPane.getChildren().add(layoutArea);

					final JFXButton btnS = LocalUtility.buttonForKeyMFX("message.controle.jdv");
					btnS.setOnMouseEntered(e -> btnS.setEffect(shadow));
					btnS.setOnMouseExited(e -> btnS.setEffect(null));
					final ImageView viewC = new ImageView(Constant.LAUNCH);
					viewC.setEffect(sepiaTone);
					btnS.setGraphic(viewC);
					btnS.setStyle(Constant.STYLE1);
					btnS.setPrefSize(250, 42);
					btnS.setMinSize(250, 42);
					btnS.setMaxSize(250, 42);

					final HBox hbox = new HBox();
					hbox.setAlignment(Pos.CENTER_RIGHT);
					hbox.getChildren().add(btnS);

					final Region space = new Region();
					space.setMaxHeight(15);
					VBox.setVgrow(space, Priority.ALWAYS);

					vBox.getChildren().addAll(splitPane, space, hbox);

					conStage = new Stage();
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					conStage.titleProperty().bind(LocalUtility.createStringBinding(CONTROL));
					conStage.setScene(scene);
					conStage.setMaximized(true);
					conStage.show();

					conStage.setOnCloseRequest(ev -> {
						if (listViewItems.getItems().isEmpty()) {
							listViewCq = new ListView<>();
							textAreaStat = new TextArea();
							textAreaS = new TextArea();
						}
					});

					btnS.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {

							final List<String> selectedOptions = selectedItems.entrySet().stream()
									.filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
							final int nbItems = selectedOptions.size();
							if (nbItems != 0) {
								textAreaS.clear();
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									for (String jdv : selectedOptions) {
										jdv = jdv.replace("__", "_");
										jdv = jdv.replace("-", "_");
										if (jdv.endsWith(" ")) {
											// Remove only the last space
											jdv = jdv.substring(0, jdv.length() - 1);
										}

										if (jdv.endsWith(".json.json")) {
											jdv = jdv.substring(0, jdv.length() - 5);
										}
										File file = new File(Constant.JSONFHIRFOLDER + "\\" + jdv + ".json");
										if (file != null && !file.exists()) {
											final Path folderPaths = Paths.get(textFieldFhir.getText());
											try {
												file = findFile(folderPaths, jdv).toFile();
											} catch (final IOException e1) {
												if (LOG.isInfoEnabled()) {
													final String error = e1.getMessage();
													LOG.error(error);
												}
											}
										}

										if (file.exists()) {
											String content = null;
											try {
												content = new String(
														Files.readAllBytes(Paths.get(file.getAbsolutePath())));
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}
											final String sJsonName = FhirUtilities.getJsonPathValue(content, "$.name");
											final String sID = FhirUtilities.getJsonPathValue(content, "$.id");
											final String sStatus = FhirUtilities.getJsonPathValue(content, "$.status");
											final String sFileName = FhirUtilities.buildJdvFileName(sJsonName, sStatus,
													sID);
											// Étape 1 : Validation du JDV ($VALIDATE)
											String validationResponse = "";
											final String sUrl = IniFile.read("URL-ONTOSERVER", "FHIR")
													+ "ValueSet/$validate";
											final String response = FhirUtilities.postGlobalRequest(sUrl, content,
													"POST");
											final String sourceRep = textFieldF.getText();
											if (!response.startsWith("!!!{")) {
												try {
													// Parsing de la réponse en objet JSON
													ObjectMapper mapper = new ObjectMapper();
													JsonNode jsonResponse = mapper.readTree(response);
													validationResponse = jsonResponse.toPrettyString();
												} catch (final Exception ex) {
													// Gestion des erreurs de parsing
													validationResponse = response;
													textAreaS.appendText("\n" + response);
													FhirUtilities.moveJdvFile(sFileName, sourceRep, "PARSING-ERROR");
													return;
												}
											} else {
												// Gestion des erreurs de connexion
												validationResponse = "!!!{CONNEXION_ERROR}:" + response;
												textAreaS.appendText("\n" + validationResponse);
											}

											// Traitement de validationResponse
											if (validationResponse.startsWith("!!!{")) {
												// Erreur dans la requête REST
												textAreaS.appendText(
														"\nErreur dans la requête REST. Fin du traitement.");
												textAreaS.appendText("\n" + validationResponse);
												FhirUtilities.moveJdvFile(sFileName, sourceRep, "REST-ERROR");
												return;
											}

											// Contrôle de la présence d'erreurs dans la réponse
											if (FhirUtilities.getFhirValidationErrors(validationResponse, false) > 0) {
												// Erreurs relevées dans la validation
												textAreaS.appendText("\n$validate: " + validationResponse);
												FhirUtilities.moveJdvFile(sFileName, sourceRep, "VALIDATE-FAIL");
												return;
											}
											textAreaS.appendText("\n\n------------------------------------------\n");
											textAreaS.appendText(jdv);
											textAreaS.appendText("\n------------------------------------------\n");
											StringBuilder sResult = new StringBuilder();
											sResult = controleUnitaireS(content, sResult, true, file.length() / 1024,
													file);
											textAreaS.appendText("\n" + sResult.toString());
											textAreaS.positionCaret(textAreaS.getText().length());
										}
									}
									try (BufferedWriter writer = Files.newBufferedWriter(
											Paths.get(Constant.LOGFHIRFOLDER + "\\" + "Log-system-no-content.log"))) {
										writer.write(textAreaS.getText());
										final List<String> selected = selectedItems.entrySet().stream()
												.filter(entry -> entry.getValue() && !disabledItems.get(entry.getKey()))
												.map(Map.Entry::getKey).collect(Collectors.toList());

										for (final String item : selected) {
											selectedItems.put(item, false); // Uncheck the item
											disabledItems.put(item, true); // Disable the item
										}

										listViewCq.refresh();
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(SUCCES));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait().ifPresent(result -> {
											if (result == ButtonType.OK) {
												numberOfCheckedItems = 0;
												updateCheckedItemsCount(textAreaStat);
											}
										});
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.info(error);
										}
									}
								});
							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.select.jdv"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
								listViewItems.getSelectionModel().clearSelection();
							}
						}
					});

				});
			}
		});

		genItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(() -> {
					if (!new File(Constant.LOGFHIRFOLDER).exists()) {
						new File(Constant.LOGFHIRFOLDER).mkdir();
					}

					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10, 10, 10, 10));

					final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.label.csv2fhir"));
					label.setPadding(new Insets(5, 5, 20, 5));
					label.setStyle(Constant.STYLE6);

					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					field.setPadding(new Insets(5, 5, 5, 5));
					field.setStyle(Constant.STYLE8);
					field.setPrefWidth(650);
					field.setMinWidth(650);
					field.setMaxWidth(650);
					field.setPrefHeight(30);
					field.setMinHeight(30);
					field.setMaxHeight(30);

					final Region space = new Region();
					space.setMaxWidth(5);
					HBox.setHgrow(space, Priority.ALWAYS);

					final JFXButton btnF = new JFXButton("");
					btnF.setOnMouseEntered(e -> btnF.setEffect(shadow));
					btnF.setOnMouseExited(e -> btnF.setEffect(null));
					final ImageView viewF = new ImageView(Constant.SFILE);
					viewF.setEffect(sepiaTone);
					btnF.setGraphic(viewF);
					btnF.setStyle(Constant.STYLE1);
					btnF.setPrefSize(30, 30);
					btnF.setMinSize(30, 30);
					btnF.setMaxSize(30, 30);
					final Tooltip tooltipF = LocalUtility.createBoundTooltip("message.title.folder");
					btnF.setTooltip(tooltipF);

					final Region space1 = new Region();
					space1.setMaxWidth(5);
					HBox.setHgrow(space1, Priority.ALWAYS);

					final JFXButton btnS = new JFXButton("");
					btnS.setOnMouseEntered(e -> btnS.setEffect(shadow));
					btnS.setOnMouseExited(e -> btnS.setEffect(null));
					final ImageView viewS = new ImageView(Constant.SAVEDISKFILE);
					viewS.setEffect(sepiaTone);
					btnS.setGraphic(viewS);
					btnS.setStyle(Constant.STYLE1);
					btnS.setPrefSize(30, 30);
					btnS.setMinSize(30, 30);
					btnS.setMaxSize(30, 30);
					final Tooltip tooltipS = LocalUtility.createBoundTooltip("message.datasave");
					btnS.setTooltip(tooltipS);

					final Region space2 = new Region();
					space2.setMaxWidth(5);
					HBox.setHgrow(space2, Priority.ALWAYS);

					final JFXButton btnSA = new JFXButton("");
					btnSA.setOnMouseEntered(e -> btnSA.setEffect(shadow));
					btnSA.setOnMouseExited(e -> btnSA.setEffect(null));
					final ImageView viewSA = new ImageView(Constant.SAVEASFILE);
					viewSA.setEffect(sepiaTone);
					btnSA.setGraphic(viewSA);
					btnSA.setStyle(Constant.STYLE1);
					btnSA.setPrefSize(30, 30);
					btnSA.setMinSize(30, 30);
					btnSA.setMaxSize(30, 30);
					final Tooltip tooltipSA = LocalUtility.createBoundTooltip("message.datasaveas");
					btnSA.setTooltip(tooltipSA);

					final Region space3 = new Region();
					space3.setMaxWidth(5);
					HBox.setHgrow(space3, Priority.ALWAYS);

					final JFXButton btnAf = new JFXButton("");
					btnAf.setOnMouseEntered(e -> btnAf.setEffect(shadow));
					btnAf.setOnMouseExited(e -> btnAf.setEffect(null));
					final ImageView viewAf = new ImageView(Constant.ARROWS);
					viewAf.setEffect(sepiaTone);
					btnAf.setGraphic(viewAf);
					btnAf.setStyle(Constant.STYLE1);
					btnAf.setPrefSize(30, 30);
					btnAf.setMinSize(30, 30);
					btnAf.setMaxSize(30, 30);
					final Tooltip tooltipAf = LocalUtility.createBoundTooltip("message.refresh");
					btnAf.setTooltip(tooltipAf);

					final Region space4 = new Region();
					space4.setMaxWidth(5);
					HBox.setHgrow(space4, Priority.ALWAYS);

					final JFXButton btnD = new JFXButton("");
					btnD.setOnMouseEntered(e -> btnD.setEffect(shadow));
					btnD.setOnMouseExited(e -> btnD.setEffect(null));
					final ImageView viewD = new ImageView(Constant.ERASER);
					viewD.setEffect(sepiaTone);
					btnD.setGraphic(viewD);
					btnD.setStyle(Constant.STYLE1);
					btnD.setPrefSize(30, 30);
					btnD.setMinSize(30, 30);
					btnD.setMaxSize(30, 30);
					final Tooltip tooltipD = LocalUtility.createBoundTooltip("message.delete.gen");
					btnD.setTooltip(tooltipD);

					final JFXButton btnControl = new JFXButton("");
					btnControl.setOnMouseEntered(e -> btnControl.setEffect(shadow));
					btnControl.setOnMouseExited(e -> btnControl.setEffect(null));
					final ImageView viewControl = new ImageView(Constant.CONFHIR);
					viewControl.setEffect(sepiaTone);
					btnControl.setGraphic(viewControl);
					btnControl.setText(LocalUtility.getString(CONTROL));
					btnControl.setStyle(Constant.STYLE1);
					btnControl.setPrefSize(150, 40);
					btnControl.setMinSize(150, 40);
					btnControl.setMaxSize(150, 40);
					final Tooltip tooltipControl = LocalUtility.createBoundTooltip(CONTROL);
					btnControl.setTooltip(tooltipControl);

					final JFXButton terminoControl = new JFXButton("");
					terminoControl.setOnMouseEntered(e -> terminoControl.setEffect(shadow));
					terminoControl.setOnMouseExited(e -> terminoControl.setEffect(null));
					final ImageView viewtermino = new ImageView(Constant.TERFHIR);
					viewtermino.setEffect(sepiaTone);
					terminoControl.setGraphic(viewtermino);
					terminoControl.setText(LocalUtility.getString(TERMINO));
					terminoControl.setStyle(Constant.STYLE1);
					terminoControl.setPrefSize(150, 40);
					terminoControl.setMinSize(150, 40);
					terminoControl.setMaxSize(150, 40);
					final Tooltip tooltipterControl = LocalUtility.createBoundTooltip(TERMINO);
					terminoControl.setTooltip(tooltipterControl);

					final JFXButton serverControl = new JFXButton("");
					serverControl.setOnMouseEntered(e -> serverControl.setEffect(shadow));
					serverControl.setOnMouseExited(e -> serverControl.setEffect(null));
					final ImageView viewserControl = new ImageView(Constant.SERFHIR);
					viewserControl.setEffect(sepiaTone);
					serverControl.setGraphic(viewserControl);
					serverControl.setText(LocalUtility.getString(SERVER));
					serverControl.setStyle(Constant.STYLE1);
					serverControl.setPrefSize(180, 40);
					serverControl.setMinSize(180, 40);
					serverControl.setMaxSize(180, 40);
					final Tooltip tooltipSerControl = LocalUtility.createBoundTooltip(SERVER);
					serverControl.setTooltip(tooltipSerControl);

					final JFXButton logControl = new JFXButton("");
					logControl.setOnMouseEntered(e -> logControl.setEffect(shadow));
					logControl.setOnMouseExited(e -> logControl.setEffect(null));
					final ImageView viewLogControl = new ImageView(Constant.LOGSFHIR);
					viewLogControl.setEffect(sepiaTone);
					logControl.setGraphic(viewLogControl);
					logControl.setText(LocalUtility.getString(FHIRLOG));
					logControl.setStyle(Constant.STYLE1);
					logControl.setPrefSize(150, 40);
					logControl.setMinSize(150, 40);
					logControl.setMaxSize(150, 40);
					final Tooltip tooltipLogControl = LocalUtility.createBoundTooltip(FHIRLOG);
					logControl.setTooltip(tooltipLogControl);

					btnControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (listViewItems.getItems().isEmpty()) {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.jdv.empty"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							} else {
								conItem.fire();
								numberOfCheckedItems = 0;
								updateCheckedItemsCount(textAreaStat);
							}
						}
					});

					final JFXButton codeSysControl = new JFXButton("");
					codeSysControl.setOnMouseEntered(e -> codeSysControl.setEffect(shadow));
					codeSysControl.setOnMouseExited(e -> codeSysControl.setEffect(null));
					final ImageView viewCodeSysControl = new ImageView(Constant.CODESYSFHIR);
					viewLogControl.setEffect(sepiaTone);
					codeSysControl.setGraphic(viewCodeSysControl);
					codeSysControl.setText(LocalUtility.getString(FHIRCODESYS));
					codeSysControl.setStyle(Constant.STYLE1);
					codeSysControl.setPrefSize(190, 40);
					codeSysControl.setMinSize(190, 40);
					codeSysControl.setMaxSize(190, 40);
					final Tooltip tooltipcodeSysControl = LocalUtility.createBoundTooltip(FHIRCODESYS);
					codeSysControl.setTooltip(tooltipcodeSysControl);

					codeSysControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (listViewItems.getItems().isEmpty()) {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.jdv.empty"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							} else {
								generateCsItem.fire();
							}
						}
					});

					terminoControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							terItem.fire();
						}
					});

					serverControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							servItem.fire();
						}
					});

					logControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							logsItem.fire();
						}
					});

					codeSysControl.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							generateCsItem.fire();
						}
					});

					final Region space44 = new Region();
					space44.setMaxWidth(500);
					HBox.setHgrow(space44, Priority.ALWAYS);

					final HBox hbox = new HBox();
					hbox.getChildren().addAll(field, space, btnF, space1, btnS, space2, btnSA, space3, btnAf, space4,
							btnD, space44, btnControl, terminoControl, serverControl, logControl, codeSysControl);

					final TextArea textArea = new TextArea();
					textArea.setWrapText(true);
					textArea.setEditable(false);
					textArea.setPrefSize(825, 600);
					textArea.setPadding(new Insets(10));
					textArea.getStyleClass().add("text-area-green");

					final Region spaceArea = new Region();
					spaceArea.setMaxWidth(5);
					HBox.setHgrow(spaceArea, Priority.ALWAYS);

					final Region spaceAreaV = new Region();
					spaceAreaV.setMaxHeight(10);
					VBox.setVgrow(spaceAreaV, Priority.ALWAYS);

					final double width = screenSize.getWidth();
					textAreaInput = new TextArea();
					textAreaInput.getStyleClass().add("text-area-green");
					textAreaInput.setWrapText(true);
					textAreaInput.setEditable(true);
					textAreaInput.setPrefSize(width - 850, 600);
					textAreaInput.setPadding(new Insets(10));

					vBox.setPadding(new Insets(10, 10, 10, 10));

					final HBox hboxTextArea = new HBox();
					hboxTextArea.getChildren().addAll(textArea, spaceArea, textAreaInput);

					final JFXButton btnC = LocalUtility.buttonForKeyMFX("message.convert.svs2fhir");
					btnC.setOnMouseEntered(e -> btnC.setEffect(shadow));
					btnC.setOnMouseExited(e -> btnC.setEffect(null));
					final ImageView viewC = new ImageView(Constant.CONVERTSVS);
					viewC.setEffect(sepiaTone);
					btnC.setGraphic(viewC);
					btnC.setStyle(Constant.STYLE1);
					btnC.setPrefSize(170, 42);
					btnC.setMinSize(170, 42);
					btnC.setMaxSize(170, 42);

					final Region spaceButton = new Region();
					spaceButton.setMaxHeight(20);
					VBox.setVgrow(spaceButton, Priority.ALWAYS);

					final CheckBox checkboxLimite = LocalUtility.boxForKeyCheck("message.limite.generation");
					checkboxLimite.setStyle(FONTTIME);
					checkboxLimite.setSelected(true);

					final TextField textFieldNum = new TextField();
					textFieldNum.setStyle("-fx-font-size: 12px;");
					textFieldNum.setPrefHeight(20);
					textFieldNum.setPrefWidth(40);
					textFieldNum.setMaxWidth(40);
					textFieldNum.setMaxHeight(20);
					textFieldNum.setMinHeight(20);
					textFieldNum.setMinWidth(40);
					textFieldNum.setText("50");

					checkboxLimite.setOnAction(ev -> {
						if (checkboxLimite.isSelected()) {
							textFieldNum.setDisable(false);
							textFieldNum.setText("50");
						} else {
							textFieldNum.setDisable(true);
							textFieldNum.setText("");
						}
					});

					final Region spaceLabel = new Region();
					spaceLabel.setMaxWidth(5);
					spaceLabel.setPrefWidth(5);
					spaceLabel.setMinWidth(5);
					HBox.setHgrow(spaceLabel, Priority.ALWAYS);

					final Label labelFolder = LocalUtility.labelForValue(() -> LocalUtility.get("message.rep.sortie"));
					labelFolder.setStyle(FONTTIME);
					labelFolder.setPadding(new Insets(5, 0, 5, 0));

					final Region spaceLabelF = new Region();
					spaceLabelF.setMaxWidth(10);
					spaceLabelF.setPrefWidth(10);
					spaceLabelF.setMinWidth(10);
					HBox.setHgrow(spaceLabelF, Priority.ALWAYS);
					textFieldF = new TextField();
					textFieldF.setOnMouseEntered(e -> textFieldF.setEffect(shadow));
					textFieldF.setOnMouseExited(e -> textFieldF.setEffect(null));
					textFieldF.setPadding(new Insets(5, 5, 5, 5));
					textFieldF.setStyle(Constant.STYLE8);
					textFieldF.setPrefWidth(450);
					textFieldF.setMinWidth(450);
					textFieldF.setMaxWidth(450);
					textFieldF.setPrefHeight(30);
					textFieldF.setMinHeight(30);
					textFieldF.setMaxHeight(30);
					textFieldF.setText(Constant.JSONFHIRFOLDER);

					final Region spaceBtn = new Region();
					spaceBtn.setMaxWidth(5);
					spaceBtn.setPrefWidth(5);
					spaceBtn.setMinWidth(5);
					HBox.setHgrow(spaceBtn, Priority.ALWAYS);

					final JFXButton btnO = new JFXButton("");
					btnO.setOnMouseEntered(e -> btnO.setEffect(shadow));
					btnO.setOnMouseExited(e -> btnO.setEffect(null));
					final ImageView viewO = new ImageView(Constant.FOLDERPHOTO2);
					viewO.setEffect(sepiaTone);
					btnO.setGraphic(viewO);
					btnO.setStyle(Constant.STYLE1);
					btnO.setPrefSize(30, 30);
					btnO.setMinSize(30, 30);
					btnO.setMaxSize(30, 30);
					final Tooltip tooltipO = LocalUtility.createBoundTooltip("message.title.folder");
					btnO.setTooltip(tooltipO);

					final Region spaceBtna = new Region();
					spaceBtna.setMaxWidth(5);
					spaceBtna.setPrefWidth(5);
					spaceBtna.setMinWidth(5);
					HBox.setHgrow(spaceBtna, Priority.ALWAYS);

					final JFXButton btnDa = new JFXButton("");
					btnDa.setOnMouseEntered(e -> btnDa.setEffect(shadow));
					btnDa.setOnMouseExited(e -> btnDa.setEffect(null));
					final ImageView viewDa = new ImageView(Constant.REMOVEFOLDER);
					viewDa.setEffect(sepiaTone);
					btnDa.setGraphic(viewDa);
					btnDa.setStyle(Constant.STYLE1);
					btnDa.setPrefSize(30, 30);
					btnDa.setMinSize(30, 30);
					btnDa.setMaxSize(30, 30);
					final Tooltip tooltipDa = LocalUtility.createBoundTooltip("message.remove.folder");
					btnDa.setTooltip(tooltipDa);

					final Region spaceBtnz = new Region();
					spaceBtnz.setMaxWidth(5);
					spaceBtnz.setPrefWidth(5);
					spaceBtnz.setMinWidth(5);
					HBox.setHgrow(spaceBtnz, Priority.ALWAYS);

					final JFXButton btnDz = new JFXButton("");
					btnDz.setOnMouseEntered(e -> btnDz.setEffect(shadow));
					btnDz.setOnMouseExited(e -> btnDz.setEffect(null));
					final ImageView viewDz = new ImageView(Constant.ADDFOLDER);
					viewDz.setEffect(sepiaTone);
					btnDz.setGraphic(viewDz);
					btnDz.setStyle(Constant.STYLE1);
					btnDz.setPrefSize(30, 30);
					btnDz.setMinSize(30, 30);
					btnDz.setMaxSize(30, 30);
					final Tooltip tooltipDz = LocalUtility.createBoundTooltip("message.add.folder");
					btnDz.setTooltip(tooltipDz);

					final HBox vboxCh = new HBox();
					vboxCh.getChildren().addAll(checkboxLimite, textFieldNum);

					final VBox vboxCheck = new VBox(0, vboxCh);
					vboxCheck.setPadding(new Insets(5));
					final HBox interm = new HBox(10, btnC, vboxCheck);
					interm.setPadding(new Insets(10));
					final HBox hboxCheck = new HBox(10, interm);
					hboxCheck.setStyle(
							"-fx-background-color: transparent; -fx-border-color: #333333;-fx-border-radius: 5;");

					final Region spacerB = new Region();
					spacerB.setPrefHeight(25);
					spacerB.setMaxHeight(25);
					spacerB.setMinHeight(25);
					VBox.setVgrow(spacerB, Priority.ALWAYS);

					final StackPane leftPane = new StackPane();
					leftPane.setPadding(new Insets(10));

					final HBox hboxBtn = new HBox();
					hboxBtn.getChildren().addAll(labelFolder, spaceLabelF, textFieldF, spaceLabel, spaceBtn, btnO,
							spaceBtna, btnDa, spaceBtnz, btnDz);

					final Label labelTermino = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.file.sortie"));
					labelTermino.setStyle(FONTTIME);
					labelTermino.setPadding(new Insets(5, 0, 5, 0));

					final Region spaceLabelTermino = new Region();
					spaceLabelTermino.setMaxWidth(90);
					spaceLabelTermino.setPrefWidth(90);
					spaceLabelTermino.setMinWidth(90);
					HBox.setHgrow(spaceLabelTermino, Priority.ALWAYS);

					final TextField textFieldFF = new TextField();
					textFieldFF.setOnMouseEntered(e -> textFieldFF.setEffect(shadow));
					textFieldFF.setOnMouseExited(e -> textFieldFF.setEffect(null));
					textFieldFF.setPadding(new Insets(5, 5, 5, 5));
					textFieldFF.setStyle(Constant.STYLE8);
					textFieldFF.setPrefWidth(450);
					textFieldFF.setMinWidth(450);
					textFieldFF.setMaxWidth(450);
					textFieldFF.setPrefHeight(30);
					textFieldFF.setMinHeight(30);
					textFieldFF.setMaxHeight(30);

					final String fieldTermino = IniFile.read("LOAD-TERMINOLOGY", FHIR);
					if (fieldTermino != null && !fieldTermino.isEmpty()) {
						textFieldFF.setText(fieldTermino);
					}

					final JFXButton btnSF = new JFXButton("");
					btnSF.setOnMouseEntered(e -> btnSF.setEffect(shadow));
					btnSF.setOnMouseExited(e -> btnSF.setEffect(null));
					final ImageView viewSF = new ImageView(Constant.SFILE);
					viewSF.setEffect(sepiaTone);
					btnSF.setGraphic(viewSF);
					btnSF.setStyle(Constant.STYLE1);
					btnSF.setPrefSize(30, 30);
					btnSF.setMinSize(30, 30);
					btnSF.setMaxSize(30, 30);
					final Tooltip tooltipSF = LocalUtility.createBoundTooltip("message.choose.file");
					btnSF.setTooltip(tooltipSF);

					final Region spacerLab = new Region();
					spacerLab.setMaxWidth(10);
					spacerLab.setPrefWidth(10);
					spacerLab.setMinWidth(10);
					HBox.setHgrow(spacerLab, Priority.ALWAYS);

					final HBox hboxBtn2 = new HBox();
					hboxBtn2.getChildren().addAll(labelTermino, spaceLabelTermino, textFieldFF, spacerLab, btnSF);

					final Label labelOpr = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.operation.unitaire"));
					labelOpr.setStyle(Constant.STYLE6);
					labelOpr.setPadding(new Insets(10, 0, 0, 0));

					final JFXButton btnV = LocalUtility.buttonForKeyMFX("message.validate.valueset");
					btnV.setOnMouseEntered(e -> btnV.setEffect(shadow));
					btnV.setOnMouseExited(e -> btnV.setEffect(null));
					final ImageView viewV = new ImageView(Constant.VALIDATEVS);
					viewDz.setEffect(sepiaTone);
					btnV.setGraphic(viewV);
					btnV.setStyle(Constant.STYLE1);
					btnV.setPrefSize(200, 40);
					btnV.setMinSize(200, 40);
					btnV.setMaxSize(200, 40);

					final JFXButton btnDza = LocalUtility.buttonForKeyMFX("message.controle.code");
					btnDza.setOnMouseEntered(e -> btnDza.setEffect(shadow));
					btnDza.setOnMouseExited(e -> btnDza.setEffect(null));
					final ImageView viewDza = new ImageView(Constant.SECURITY);
					viewDza.setEffect(sepiaTone);
					btnDza.setGraphic(viewDza);
					btnDza.setStyle(Constant.STYLE1);
					btnDza.setPrefSize(200, 40);
					btnDza.setMinSize(200, 40);
					btnDza.setMaxSize(200, 40);

					final Region spacerV = new Region();
					spacerV.setPrefWidth(5);
					spacerV.setMaxWidth(5);
					spacerV.setMinWidth(5);
					HBox.setHgrow(spacerV, Priority.ALWAYS);

					final HBox hboxBtnV = new HBox();

					final Region spacerVunitaire = new Region();
					spacerVunitaire.setPrefHeight(10);
					spacerVunitaire.setMaxHeight(10);
					spacerVunitaire.setMinHeight(10);
					VBox.setVgrow(spacerVunitaire, Priority.ALWAYS);

					final Region spacerU = new Region();
					spacerU.setPrefWidth(110);
					spacerU.setMaxWidth(110);
					spacerU.setMinWidth(110);
					HBox.setHgrow(spacerU, Priority.ALWAYS);

					hboxBtnV.getChildren().addAll(labelOpr, spacerU, btnV, spacerV, btnDza);

					final Region spacerUs = new Region();
					spacerUs.setPrefHeight(15);
					spacerUs.setMaxHeight(15);
					spacerUs.setMinHeight(15);
					VBox.setVgrow(spacerUs, Priority.ALWAYS);

					final Region spacerVunitaire2 = new Region();
					spacerVunitaire2.setPrefHeight(10);
					spacerVunitaire2.setMaxHeight(10);
					spacerVunitaire2.setMinHeight(10);
					VBox.setVgrow(spacerVunitaire2, Priority.ALWAYS);

					final Label labelFhir = LocalUtility.labelForValue(() -> LocalUtility.get("message.rep.jdv"));
					labelFhir.setStyle(FONTTIME);
					labelFhir.setPadding(new Insets(5, 0, 5, 0));

					final Region spaceLabelFhir = new Region();
					spaceLabelFhir.setMaxWidth(60);
					spaceLabelFhir.setPrefWidth(60);
					spaceLabelFhir.setMinWidth(60);
					HBox.setHgrow(spaceLabelFhir, Priority.ALWAYS);

					textFieldFhir.setOnMouseEntered(e -> textFieldFhir.setEffect(shadow));
					textFieldFhir.setOnMouseExited(e -> textFieldFhir.setEffect(null));
					textFieldFhir.setPadding(new Insets(5, 5, 5, 5));
					textFieldFhir.setStyle(Constant.STYLE8);
					textFieldFhir.setPrefWidth(450);
					textFieldFhir.setMinWidth(450);
					textFieldFhir.setMaxWidth(450);
					textFieldFhir.setPrefHeight(30);
					textFieldFhir.setMinHeight(30);
					textFieldFhir.setMaxHeight(30);

					final JFXButton btnSFF = new JFXButton("");
					btnSFF.setOnMouseEntered(e -> btnSFF.setEffect(shadow));
					btnSFF.setOnMouseExited(e -> btnSFF.setEffect(null));
					final ImageView viewSFF = new ImageView(Constant.SFFILE);
					viewSFF.setEffect(sepiaTone);
					btnSFF.setGraphic(viewSFF);
					btnSFF.setStyle(Constant.STYLE1);
					btnSFF.setPrefSize(30, 30);
					btnSFF.setMinSize(30, 30);
					btnSFF.setMaxSize(30, 30);
					final Tooltip tooltipSFF = LocalUtility.createBoundTooltip("message.choose.file");
					btnSFF.setTooltip(tooltipSFF);

					final CheckBox checkBox = new CheckBox();
					checkBox.setSelected(false);
					final Label labelCheck = new Label(LocalUtility.getString("message.folder.check"));
					labelCheck.setStyle(FONTTIME);
					checkBox.setStyle(FONTTIME);
					final HBox hBox = new HBox(10, checkBox, labelCheck);
					hBox.setPadding(new Insets(5, 5, 5, 5));

					final Region spacerLab1 = new Region();
					spacerLab1.setMaxWidth(10);
					spacerLab1.setPrefWidth(10);
					spacerLab1.setMinWidth(10);
					HBox.setHgrow(spacerLab1, Priority.ALWAYS);

					final Region spacerLab2 = new Region();
					spacerLab2.setMaxWidth(10);
					spacerLab2.setPrefWidth(10);
					spacerLab2.setMinWidth(10);
					HBox.setHgrow(spacerLab2, Priority.ALWAYS);

					final HBox hboxBtnF = new HBox();
					hboxBtnF.getChildren().addAll(labelFhir, spaceLabelFhir, textFieldFhir, spacerLab1, btnSFF,
							spacerLab2, hBox);

					final Region spacerVunitaire3 = new Region();
					spacerVunitaire3.setPrefHeight(10);
					spacerVunitaire3.setMaxHeight(10);
					spacerVunitaire3.setMinHeight(10);
					VBox.setVgrow(spacerVunitaire3, Priority.ALWAYS);

					final VBox hboxBtnVbox = new VBox();
					hboxBtnVbox.getChildren().addAll(spacerUs, hboxBtnF, spacerVunitaire3, hboxBtn, spacerVunitaire2,
							hboxBtn2, spacerVunitaire, hboxBtnV);

					leftPane.getChildren().add(hboxBtnVbox);

					final StackPane rightPane = new StackPane();
					rightPane.setPadding(new Insets(10));

					itemsFiltred = FXCollections.observableArrayList();

					filteredItems = new FilteredList<>(itemsFiltred, s -> true);
					listViewItems = new ListView<>(filteredItems);
					listViewItems.setMaxHeight(150);

					listViewItems.setOnMouseClicked(ev -> {
						if (ev.getClickCount() == 1) {
							final String selectedItem = listViewItems.getSelectionModel().getSelectedItem();
							if (selectedItem != null) {
								String filePath = Constant.JSONFHIRFOLDER + "\\" + selectedItem + ".json";
								if (filePath.endsWith(".json.json")) {
									filePath = filePath.substring(0, filePath.length() - 5);
								}
								if (filePath != null && new File(filePath).exists()) {
									textAreaInput.setText(FhirUtilities.readFileContent(new File(filePath)));
								}
								if (filePath != null && !new File(filePath).exists()) {
									final Path folderPaths = Paths.get(textFieldFhir.getText());
									try {
										final Path result = findFile(folderPaths, filePath);
										textAreaInput.setText(FhirUtilities.readFileContent(result.toFile()));
									} catch (final IOException e1) {
										if (LOG.isInfoEnabled()) {
											final String error = e1.getMessage();
											LOG.error(error);
										}
									}
								}
							}
						}
					});

					listViewItems.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
						@Override
						public ListCell<String> call(final ListView<String> param) {
							return new ListCell<String>() {
								@Override
								protected void updateItem(String item, boolean empty) {
									super.updateItem(item, empty);
									if (empty || item == null) {
										setText(null);
										setStyle("");
									} else {
										setText(item);
										setStyle("-fx-background-color: #93f9b911;");
									}
								}
							};
						}
					});

					final TextField filterInput = new TextField();
					filterInput.setOnMouseEntered(e -> filterInput.setEffect(shadow));
					filterInput.setOnMouseExited(e -> filterInput.setEffect(null));
					filterInput.setStyle(Constant.STYLE8);
					filterInput.setPromptText(LocalUtility.get("message.search.field"));
					filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredItems.setPredicate(item -> {
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							final String lowerCaseFilter = newValue.toLowerCase();
							return item.toLowerCase().contains(lowerCaseFilter);
						});
					});
					final VBox layout = new VBox(10, filterInput, listViewItems);
					rightPane.getChildren().add(layout);

					final SplitPane splitPane = new SplitPane();
					splitPane.setStyle(
							"-fx-background-color: transparent; -fx-border-color: #333333;-fx-border-radius: 5;");
					Platform.runLater(() -> {
						final Node divider = splitPane.lookup(".split-pane-divider");
						if (divider != null) {
							divider.setStyle("-fx-background-color: transparent;");
						}
					});
					splitPane.getItems().addAll(rightPane, leftPane);
					splitPane.setDividerPositions(0.435);

					vBox.getChildren().addAll(label, hbox, spaceAreaV, hboxTextArea, spaceButton, hboxCheck, spacerB,
							splitPane);

					final Stage genStage = new Stage();
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					genStage.titleProperty().bind(LocalUtility.createStringBinding("message.menu.fhir.generate"));
					genStage.setScene(scene);
					genStage.setMaximized(true);
					genStage.show();

					genStage.setOnCloseRequest(ev -> {
						listViewCq = new ListView<>();
						textAreaStat = new TextArea();
						textAreaS = new TextArea();
						if (terStage != null && terStage.isShowing()) {
							terStage.close();
						}
						if (logStage != null && logStage.isShowing()) {
							logStage.close();
						}
						if (conStage != null && conStage.isShowing()) {
							conStage.close();
						}
						if (servStage != null && servStage.isShowing()) {
							servStage.close();
						}
					});

					btnV.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final String selectedItem = listViewItems.getSelectionModel().getSelectedItem();
								if (selectedItem != null) {
									final String jdvContent = textAreaInput.getText();
									final StringBuilder rapportErreurs = new StringBuilder();
									final String value = FhirUtilities.getValueSetFromJson(jdvContent, rapportErreurs);
									textAreaInput.setText(value);
									listViewItems.getSelectionModel().clearSelection();
								} else {
									final Alert alert = new Alert(AlertType.ERROR);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.select.list"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
									listViewItems.getSelectionModel().clearSelection();
								}
							});
						}
					});

					btnSF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"INI files (*.ini)", "*.ini");
							fileChooser.getExtensionFilters().add(extFilter);
							final File file = fileChooser.showOpenDialog(genStage);
							if (file != null) {
								textFieldFF.setText(file.getAbsolutePath());
							}
							runTask(taskUpdateStage, progress);
							Platform.runLater(() -> {
								if (FhirUtilities.CODESYSTEMSONTOSERVER.isEmpty()
										&& FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.isEmpty()) {
									FhirUtilities.loadTerminology(file);
								}
								IniFile.write("LOAD-TERMINOLOGY", file.getAbsolutePath(), FHIR);
								final Alert alert = new Alert(AlertType.INFORMATION);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.file.load"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							});

						}
					});

					btnSFF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final DirectoryChooser directoryChooser = new DirectoryChooser();
								directoryChooser.setTitle(LocalUtility.getString("message.select.directory"));
								final String sChemin = IniFile.read(LASTJDVPUSED, FHIR);
								if (sChemin != null && new File(sChemin).exists()) {
									directoryChooser.setInitialDirectory(new File(sChemin));
								} else {
									directoryChooser.setInitialDirectory(new File(Constant.DISK));
								}
								final File file = directoryChooser.showDialog(genStage);
								if (file != null) {
									runTask(taskUpdateStage, progress);
									Platform.runLater(() -> {
										textFieldFhir.setText(file.getAbsolutePath());
										IniFile.write(LASTJDVPUSED, textFieldFhir.getText(), FHIR);
										final Path folderPath = Paths.get(file.getAbsolutePath());
										final List<String> fileDetails = new ArrayList<>();
										try {
											// Traverse the folder and its subfolders
											if (checkBox.isSelected()) {
												Files.walkFileTree(folderPath, new SimpleFileVisitor<>() {
													@Override
													public FileVisitResult visitFile(Path file,
															BasicFileAttributes attrs) throws IOException {
														// Add file name and size to the map
														fileDetails.add(file.toFile().getName());
														return FileVisitResult.CONTINUE;
													}
												});
											} else {
												final String directoryPath = folderPath.toString();
												final File directory = new File(directoryPath);
												if (directory.exists() && directory.isDirectory()) {
													final File[] files = directory.listFiles();
													if (files != null) {
														for (final File filee : files) {
															if (filee.isFile()) {
																fileDetails.add(filee.getName());
															}
														}
													}
												}
											}
											itemsFiltred = FXCollections.observableArrayList(fileDetails);
											filteredItems = new FilteredList<>(itemsFiltred, s -> true);
											listViewItems.setItems(filteredItems);
											listViewItems
													.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
														@Override
														public ListCell<String> call(final ListView<String> param) {
															return new ListCell<String>() {
																@Override
																protected void updateItem(String item, boolean empty) {
																	super.updateItem(item, empty);
																	if (empty || item == null) {
																		setText(null);
																		setStyle("");
																	} else {
																		setText(item);
																		setStyle("-fx-background-color: #93f9b911;");
																	}
																}
															};
														}
													});
											final List<String> listFile = new ArrayList<>();
											for (String fileName : fileDetails) {
												final int index = fileName.indexOf(".json");
												fileName = fileName.substring(0, index);
												final Path folderPaths = Paths.get(textFieldFhir.getText());
												String fileNameToSearch = fileName + ".json";
												try {
													final Path result = findFile(folderPaths, fileNameToSearch);
													if (result.toFile().exists() && result.toFile().isFile()) {
														FhirUtilities.content
																.append(FhirUtilities.readFileContent(result.toFile()));
														FhirUtilities.content.append("\n\n");
														listFile.add(result.toFile().getAbsolutePath());
													}
												} catch (final IOException e) {
													if (LOG.isInfoEnabled()) {
														final String error = e.getMessage();
														LOG.error(error);
													}
												}
											}
											textAreaInput.clear();
											textAreaInput.setText(FhirUtilities.content.toString());
											listViewCq = new ListView<>();
											listViewCq.setItems(filteredItems);
											// textArea Statistique
											textAreaStat = new TextArea();
											FhirUtilities.distinctCodeSys(listFile);
											FhirUtilities.distinctCode(listFile, textFieldFF.getText());
											if (FhirUtilities.CODESYSTEMSONTOSERVER.isEmpty()
													&& FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.isEmpty()) {
												FhirUtilities.loadTerminology(new File(fieldTermino));
											}
											final long fileCount = Files.list(Paths.get(file.getAbsolutePath()))
													.filter(Files::isRegularFile) // Filters regular files
													.count();
											listSize = (int) fileCount;
											textAreaStat.setText("\nNombre de jeux de valeurs convertis en Fhir: "
													+ listSize + "\nNombre de jeux de valeurs cochés: "
													+ numberOfCheckedItems + "/" + listSize
													+ "\nNombre de CodeSystems utilisés par les Jdv: "
													+ FhirUtilities.distinctCodeSystemUsed.size()
													+ "\n\n ***** Liste des codes systèmes utilisés par les JDV *****\n\n");

											final StringBuilder batchBuilder = new StringBuilder();
											int count = 0;
											for (final String item : FhirUtilities.distinctCodeSystemUsed) {
												String oid = IniFile.getOidForUri(textFieldFF.getText(), item);
												if (oid != null) {
													if (oid.contains("urn:oid:")) {
														oid = oid.replaceFirst("^urn:oid:", "");
													}
													batchBuilder.append("\n").append(oid).append(" --- ").append(item)
															.append("\n");
													if (++count % 10 == 0) {
														textAreaStat.appendText(batchBuilder.toString());
														batchBuilder.setLength(0);
													}
												}
											}
											// Append any remaining items
											if (batchBuilder.length() > 0) {
												textAreaStat.appendText(batchBuilder.toString());
											}

											textAreaStat.appendText(
													"\n\\n ***** Liste des codes utilisés par les JDV *****\n\n");

											final StringBuilder batchBuildere = new StringBuilder();
											int counte = 0;
											for (final String item : FhirUtilities.distinctCodeUsed) {
												batchBuildere.append("\n").append(item).append("\n");
												// Append every 10 items to reduce UI updates
												if (++counte % 10 == 0) {
													textAreaStat.appendText(batchBuildere.toString());
													batchBuildere.setLength(0);
												}
											}
											// Append any remaining items
											if (batchBuilder.length() > 0) {
												textAreaStat.appendText(batchBuildere.toString());
											}
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									});
								}
							});
						}
					});

					btnO.setOnAction(new EventHandler<>() {

						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final DirectoryChooser directoryChooser = new DirectoryChooser();
								directoryChooser.setTitle(LocalUtility.getString("message.select.directory"));
								if (!textFieldF.getText().isEmpty()) {
									directoryChooser.setInitialDirectory(new File(textFieldF.getText()));
								}
								final File file = directoryChooser.showDialog(genStage);
								if (file != null) {
									textFieldF.setText(file.getAbsolutePath());
								}
							});
						}
					});

					btnDa.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final File file = new File(textFieldF.getText());
								if (file != null && !textFieldF.getText().isEmpty() && file.exists()) {
									FhirUtilities.deleteFilesInDirectory(file.toPath());
									final Alert alert = new Alert(AlertType.INFORMATION);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString(SUCCES));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									final Optional<ButtonType> result = alert.showAndWait();
									if (result.isPresent() && result.get() == ButtonType.OK) {
										itemsFiltred = FXCollections.observableArrayList();
										filteredItems = new FilteredList<>(itemsFiltred, s -> true);
										listViewItems.setItems(filteredItems);
										textAreaInput.clear();
									}
								} else {
									final Alert alert = new Alert(AlertType.ERROR);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.alert.rep"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}
							});
						}
					});

					btnDz.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (Desktop.isDesktopSupported()) {
									try {
										final File file = new File(textFieldF.getText());
										if (file != null && !textFieldF.getText().isEmpty() && file.exists()) {
											Desktop.getDesktop().open(file);
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString("message.alert.rep"));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								}
							});
						}
					});

					btnDza.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (listViewItems.getSelectionModel().getSelectedItem() == null) {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.select.list"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
								listViewItems.getSelectionModel().clearSelection();
							} else {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									String textContent = textAreaInput.getText();
									try {
										final JSONObject obj = new JSONObject(textContent);
										textContent = obj.toString();
									} catch (final Exception ex) {
										if (LOG.isInfoEnabled()) {
											final String error = ex.getMessage();
											LOG.error(error);
										}
									}
									final String sJsonName = FhirUtilities.getJsonPathValue(textContent, "$.name");
									final String sID = FhirUtilities.getJsonPathValue(textContent, "$.id");
									final String sStatus = FhirUtilities.getJsonPathValue(textContent, "$.status");
									final String sFileName = FhirUtilities.buildJdvFileName(sJsonName, sStatus, sID);
									// Étape 1 : Validation du JDV ($VALIDATE)
									String validationResponse = "";
									final String sUrl = IniFile.read("URL-ONTOSERVER", "FHIR") + "ValueSet/$validate";
									final String response = FhirUtilities.postGlobalRequest(sUrl, textContent, "POST");
									final String sourceRep = textFieldF.getText();
									if (!response.startsWith("!!!{")) {
										try {
											// Parsing de la réponse en objet JSON
											ObjectMapper mapper = new ObjectMapper();
											JsonNode jsonResponse = mapper.readTree(response);
											validationResponse = jsonResponse.toPrettyString();
										} catch (final Exception ex) {
											// Gestion des erreurs de parsing
											validationResponse = response;
											textAreaS.appendText("\n" + response);
											FhirUtilities.moveJdvFile(sFileName, sourceRep, "PARSING-ERROR");
											return;
										}
									} else {
										// Gestion des erreurs de connexion
										validationResponse = "!!!{CONNEXION_ERROR}:" + response;
										textAreaS.appendText("\n" + validationResponse);
									}

									// Traitement de validationResponse
									if (validationResponse.startsWith("!!!{")) {
										// Erreur dans la requête REST
										textAreaS.appendText("\nErreur dans la requête REST. Fin du traitement.");
										textAreaS.appendText("\n" + validationResponse);
										FhirUtilities.moveJdvFile(sFileName, sourceRep, "REST-ERROR");
										return;
									}

									// Contrôle de la présence d'erreurs dans la réponse
									if (FhirUtilities.getFhirValidationErrors(validationResponse, false) > 0) {
										// Erreurs relevées dans la validation
										textAreaS.appendText("\n$validate: " + validationResponse);
										FhirUtilities.moveJdvFile(sFileName, sourceRep, "VALIDATE-FAIL");
										return;
									}
									StringBuilder sValeurRetour = new StringBuilder();
									final File file = new File(Constant.JSONFHIRFOLDER + "\\"
											+ listViewItems.getSelectionModel().getSelectedItem());
									sValeurRetour = controleUnitaire(textAreaInput.getText(), sValeurRetour, false,
											file.length() / 1024, file);
									textAreaInput.setText(sValeurRetour.toString());
									listViewItems.getSelectionModel().clearSelection();
								});
							}
						}
					});

					btnF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final FileChooser fileChooser = new FileChooser();
								fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
								final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
										"All files (*.*)", "*.*");
								fileChooser.getExtensionFilters().add(extFilter);
								final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
								if (new File(sChemin).exists()) {
									fileChooser.setInitialDirectory(new File(sChemin));
								} else {
									fileChooser.setInitialDirectory(new File(Constant.DISK));
								}
								final File file = fileChooser.showOpenDialog(genStage);
								if (file != null) {
									runTask(taskUpdateStage, progress);
									textArea.clear();
									field.setText(file.getAbsolutePath());
									try {
										textArea.setText(new String(Files.readAllBytes(Paths.get(file.toURI())),
												StandardCharsets.UTF_8));
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
									IniFile.write("LAST-INPUT-FILE", field.getText(), FHIR);
								}
							});
						}
					});

					final String fieldText = IniFile.read("LAST-INPUT-FILE", FHIR);
					if (fieldText != null && !fieldText.isEmpty()) {
						field.setText(fieldText);
						textArea.clear();
						try {
							textArea.setText(new String(Files.readAllBytes(Paths.get(new File(fieldText).toURI())),
									StandardCharsets.UTF_8));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					}

					btnS.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (!field.getText().isEmpty()) {
									try {
										Files.write(new File(field.getText()).toPath(),
												textArea.getText().getBytes(StandardCharsets.UTF_8));
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(SUCCES));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								} else {
									final FileChooser fileChooser = new FileChooser();
									FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
											"All files (*.*)", "*.*");
									fileChooser.getExtensionFilters().add(extFilter);
									final File file = fileChooser.showSaveDialog(genStage);
									if (file != null) {
										try {
											Files.write(file.toPath(),
													textArea.getText().getBytes(StandardCharsets.UTF_8));
											IniFile.write("LAST-INPUT-FILE", file.getAbsolutePath(), "FHIR");
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(SUCCES));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										} catch (final IOException e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								}
							});
						}
					});

					btnSA.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final FileChooser fileChooser = new FileChooser();
								FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
										"All files (*.*)", "*.*");
								fileChooser.getExtensionFilters().add(extFilter);
								final File file = fileChooser.showSaveDialog(genStage);
								if (file != null) {
									try {
										Files.write(file.toPath(), textArea.getText().getBytes(StandardCharsets.UTF_8));
										IniFile.write("LAST-INPUT-FILE", file.getAbsolutePath(), "FHIR");
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(SUCCES));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								}
							});
						}
					});

					btnAf.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (!field.getText().isEmpty()) {
									try {
										final String content = new String(
												Files.readAllBytes(Paths.get(field.getText())), StandardCharsets.UTF_8);
										textArea.setText(content);
										field.setText(field.getText());
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									final Alert alert = new Alert(AlertType.WARNING);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.errorfile2"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}

							});
						}
					});

					btnD.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								field.setText("");
								textArea.setText("");
								field.clear();
								textArea.clear();
							});
						}
					});

					btnC.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (!field.getText().isEmpty() && !textArea.getText().isEmpty()
									&& !textFieldFF.getText().isEmpty()) {
								final double wndwWidth = 150.0d;
								final double wndhHeigth = 150.0d;
								progress = new ProgressIndicator();
								progress.setMinWidth(wndwWidth);
								progress.setMinHeight(wndhHeigth);
								progress.setProgress(0.25F);
								final VBox updatePane = new VBox();
								updatePane.setPadding(new Insets(10));
								updatePane.setSpacing(5.0d);
								updatePane.setAlignment(Pos.CENTER);
								Utility.getChildrenNode(updatePane).addAll(progress);
								updatePane.setStyle(FONTIME2);
								taskUpdateStage = new Stage(StageStyle.UNDECORATED);
								taskUpdateStage.setScene(new Scene(updatePane, 170, 170));
								Task<Void> loadTerminologyTask = new Task<>() {
									@Override
									protected Void call() throws Exception {
										if (FhirUtilities.CODESYSTEMSONTOSERVER.isEmpty()
												&& FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.isEmpty()) {
											FhirUtilities.loadTerminology(new File(fieldTermino));
										}
										return null;
									}
								};
								loadTerminologyTask.setOnSucceeded(ev -> {
									taskUpdateStage.close();
									openModal(genStage, textFieldF.getText(), field.getText(), checkboxLimite,
											textFieldNum);
								});
								progress.progressProperty().bind(loadTerminologyTask.progressProperty());
								taskUpdateStage.show();
								new Thread(loadTerminologyTask).start();
							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(150);
								dialogPane.setMaxHeight(150);
								dialogPane.setPrefHeight(150);
								alert.setContentText(LocalUtility.getString("message.errorfile"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}
						}
					});

				});
			}
		});

		statDecorItem.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final FileChooser fileChooser = new FileChooser();
					fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
					final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)",
							"*.xml");
					fileChooser.getExtensionFilters().add(extFilter);
					final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
					if (new File(sChemin).exists()) {
						fileChooser.setInitialDirectory(new File(sChemin));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						final String fileTransformed = file.getAbsolutePath() + " :\n\n"
								+ ArtDecorService.convertXmlToJson(file.getAbsolutePath());
						textAreaConsole.setStyle(Constant.STYILE);
						textAreaConsole.setText(fileTransformed);
						final Alert alert = new Alert(AlertType.INFORMATION);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString(SUCCES));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		paramcItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					String url1 = null;
					if (LocalUtility.getLocale().equals(Locale.FRENCH)) {
						url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILE).toExternalForm();
					} else if (LocalUtility.getLocale().equals(Locale.ENGLISH)) {
						url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILEEN).toExternalForm();
					} else {
						url1 = WebViewSample.class.getClassLoader().getResource(Constant.HTMLFILEES).toExternalForm();
					}
					webEngine.load(url1);
					fadeTransition.stop();
					browserEngine.setOpacity(1.0);
					scaleTransition.stop();
					browserEngine.setScaleX(1.0);
					browserEngine.setScaleY(1.0);
				});
			}
		});

		schItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final Stage stage = new Stage();
					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10, 10, 10, 10));
					final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.label.koudou"));
					label.setPadding(new Insets(5, 5, 0, 5));
					label.setStyle(Constant.STYLE66);
					final Label label1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.label.koudou1"));
					label1.setPadding(new Insets(5, 5, 0, 5));
					label1.setStyle(Constant.STYLE66);
					final Label label2 = LocalUtility.labelForValue(() -> LocalUtility.get("message.label.koudou2"));
					label2.setPadding(new Insets(5, 5, 0, 5));
					label2.setStyle(Constant.STYLE66);

					final VBox hb = new VBox();
					hb.setStyle(Constant.STYLEBORDER);

					final Label labelSch = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.label.koudou.sch"));
					labelSch.setPadding(new Insets(5, 5, 5, 5));
					labelSch.setStyle(Constant.STYLE66);
					hb.getChildren().addAll(label, label1, label2);

					final double width = screenSize.getWidth();
					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					field.setPadding(new Insets(5, 5, 5, 5));
					field.setStyle(Constant.STYLE8);
					field.setPrefWidth(width - 280);
					field.setPrefHeight(30);
					field.setMinHeight(30);
					field.setMaxHeight(30);

					final JFXButton btnF = new JFXButton("");
					btnF.setOnMouseEntered(e -> btnF.setEffect(shadow));
					btnF.setOnMouseExited(e -> btnF.setEffect(null));
					final ImageView viewF = new ImageView(Constant.SSCHFILE);
					viewF.setEffect(sepiaTone);
					btnF.setGraphic(viewF);
					btnF.setStyle(Constant.STYLE1);
					btnF.setPrefSize(30, 30);
					btnF.setMinSize(30, 30);
					btnF.setMaxSize(30, 30);
					final Tooltip tooltipF = LocalUtility.createBoundTooltip("message.select.sch");
					btnF.setTooltip(tooltipF);

					final FileChooser fileChooser = new FileChooser();
					fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
					final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SCH files (*.sch)",
							"*.sch");
					fileChooser.getExtensionFilters().add(extFilter);
					final String sChemin = IniFile.read("LAST-PATH-USED", MEMORY);
					if (new File(sChemin).exists()) {
						fileChooser.setInitialDirectory(new File(sChemin));
					} else {
						fileChooser.setInitialDirectory(new File(Constant.DISK));
					}

					btnF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(ActionEvent event) {
							Platform.runLater(() -> {
								final File file = fileChooser.showOpenDialog(stage);
								if (file != null) {
									field.setText(file.getAbsolutePath());
								}
							});
						}
					});

					final JFXButton btnSF = new JFXButton("");
					btnSF.setOnMouseEntered(e -> btnSF.setEffect(shadow));
					btnSF.setOnMouseExited(e -> btnSF.setEffect(null));
					final ImageView viewSF = new ImageView(Constant.SFILE);
					viewSF.setEffect(sepiaTone);
					btnSF.setGraphic(viewSF);
					btnSF.setStyle(Constant.STYLE1);
					btnSF.setPrefSize(30, 30);
					btnSF.setMinSize(30, 30);
					btnSF.setMaxSize(30, 30);
					final Tooltip tooltipSF = LocalUtility.createBoundTooltip("message.select.folder.sch");
					btnSF.setTooltip(tooltipSF);

					final DirectoryChooser directoryChooser = new DirectoryChooser();
					directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
					if (new File(sChemin).exists()) {
						directoryChooser.setInitialDirectory(new File(sChemin));
					} else {
						directoryChooser.setInitialDirectory(new File(Constant.DISK));
					}

					btnSF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(ActionEvent event) {
							Platform.runLater(() -> {
								final File selectedDirectory = directoryChooser.showDialog(stage);
								if (selectedDirectory != null && selectedDirectory.isDirectory()) {
									field.setText(selectedDirectory.getAbsolutePath());
								}
							});
						}
					});

					final Region spacer = new Region();
					spacer.setMaxWidth(5);
					HBox.setHgrow(spacer, Priority.ALWAYS);

					final Region spacer1 = new Region();
					spacer1.setMaxWidth(5);
					HBox.setHgrow(spacer1, Priority.ALWAYS);

					final HBox hbox = new HBox();
					hbox.setPadding(new Insets(30, 0, 50, 0));
					hbox.getChildren().addAll(labelSch, field, spacer, btnF, spacer1, btnSF);

					final Label labelChoice = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.label.choice"));
					labelChoice.setPadding(new Insets(5, 5, 10, 5));
					labelChoice.setStyle(Constant.STYLE66);

					final CheckBox checkBox1 = new CheckBox(LocalUtility.getString("message.delete.addr"));
					checkBox1.setStyle(Constant.STYLE66);
					checkBox1.setPadding(new Insets(5, 15, 10, 5));

					final CheckBox checkBox2 = new CheckBox(LocalUtility.getString("message.mutualisation.jdv"));
					checkBox2.setStyle(Constant.STYLE66);
					checkBox2.setPadding(new Insets(5, 15, 10, 5));

					final VBox vbox = new VBox();
					vbox.getChildren().addAll(labelChoice, checkBox1, checkBox2);
					vbox.setStyle(Constant.STYLEBORDER);

					final TextField fieldRnm = new TextField();
					fieldRnm.setOnMouseEntered(e -> fieldRnm.setEffect(shadow));
					fieldRnm.setOnMouseExited(e -> fieldRnm.setEffect(null));
					fieldRnm.setPadding(new Insets(5, 5, 5, 5));
					fieldRnm.setStyle(Constant.STYLE8);
					fieldRnm.setPrefWidth(width - 300);
					fieldRnm.setPrefHeight(30);
					fieldRnm.setMinHeight(30);
					fieldRnm.setMaxHeight(30);
					fieldRnm.setText("schematron.sch");

					final ImageView viewP = new ImageView(Constant.PROCESS);
					final JFXButton button = new JFXButton("");
					button.setGraphic(viewP);
					button.setOnMouseEntered(e -> button.setEffect(shadow));
					button.setOnMouseExited(e -> button.setEffect(null));
					button.setText(LocalUtility.getString("message.retraite"));
					button.setStyle(Constant.STYLE101);

					final Region spacer3 = new Region();
					spacer3.setMaxWidth(width - 100);
					HBox.setHgrow(spacer3, Priority.ALWAYS);

					final Region spacer4 = new Region();
					spacer4.setMaxHeight(30);
					VBox.setVgrow(spacer4, Priority.ALWAYS);

					final HBox hboxButton = new HBox();
					hboxButton.getChildren().addAll(spacer3, button);

					final VBox vbox1 = new VBox();
					vbox1.setPadding(new Insets(10, 10, 10, 10));
					final Label labelRnm = LocalUtility.labelForValue(() -> LocalUtility.get("message.rename.sch"));
					labelRnm.setPadding(new Insets(5, 5, 10, 5));
					labelRnm.setStyle(Constant.STYLE66);
					vbox1.getChildren().addAll(labelRnm, fieldRnm);
					vbox1.setStyle(Constant.STYLEBORDER);

					final Region spacer2 = new Region();
					spacer2.setMaxHeight(30);
					VBox.setVgrow(spacer2, Priority.ALWAYS);

					final TextArea area = new TextArea();
					area.setWrapText(true);
					area.setEditable(false);
					area.setPrefSize(Integer.MAX_VALUE, 400);

					final Region spacer5 = new Region();
					spacer5.setMaxHeight(30);
					VBox.setVgrow(spacer5, Priority.ALWAYS);

					vBox.getChildren().addAll(hb, hbox, vbox, spacer2, vbox1, spacer4, hboxButton, spacer5, area);
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					stage.titleProperty().bind(LocalUtility.createStringBinding("message.artdecor.koudou"));
					stage.setScene(scene);
					stage.setMaximized(true);
					stage.show();

					button.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							if (!field.getText().isEmpty()) {
								if (checkBox1.isSelected() || checkBox2.isSelected()) {
									area.clear();
									runTask(taskUpdateStage, progress);
									Platform.runLater(() -> {
										if (checkBox1.isSelected()) {
											final Path path = Paths.get(field.getText());
											if (!Files.isDirectory(path) && field.getText().endsWith(".sch")) {
												final List<String> list = RemoveSch.extract(field.getText());
												if (list != null && !list.isEmpty()) {
													area.appendText("\n" + LocalUtility.getString("message.delete.addr")
															+ " :\n\n");
													for (final String file : list) {
														boolean bool = RemoveSch.validate(file);
														if (bool) {
															area.appendText(LocalUtility.getString("message.valid.file")
																	+ new File(file).getAbsolutePath() + "\n");
														} else {
															area.appendText(
																	LocalUtility.getString("message.invalid.file")
																			+ new File(file).getAbsolutePath() + "\n");
														}
													}
												}
											} else if (Files.isDirectory(path)) {
												if (path.toFile().listFiles().length > 0) {
													area.appendText("\n" + LocalUtility.getString("message.delete.addr")
															+ " :\n\n");
													for (final File file : path.toFile().listFiles()) {
														final Path pathF = Paths.get(file.getAbsolutePath());
														if (Files.isDirectory(pathF)) {
															if (pathF.toFile().listFiles().length > 0) {
																for (final File fileF : pathF.toFile().listFiles()) {
																	if (fileF.getAbsolutePath().endsWith(".sch")) {
																		final List<String> list = RemoveSch
																				.extract(fileF.getAbsolutePath());
																		if (list != null && !list.isEmpty()) {
																			for (final String fileFF : list) {
																				boolean bool = RemoveSch
																						.validate(fileFF);
																				if (bool) {
																					area.appendText(LocalUtility
																							.getString(
																									"message.valid.file")
																							+ new File(fileFF)
																									.getAbsolutePath()
																							+ "\n");
																				} else {
																					area.appendText(LocalUtility
																							.getString(
																									"message.invalid.file")
																							+ new File(fileFF)
																									.getAbsolutePath()
																							+ "\n");
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
										if (checkBox2.isSelected()) {
											final Path path = Paths.get(field.getText());
											if (!Files.isDirectory(path) && field.getText().endsWith(".sch")) {
												try {
													boolean bool = Mutualisation.mutualisation(field.getText(),
															fieldRnm.getText());
													final String fileSelect = field.getText();
													final String repSelect = Paths.get(fileSelect).getParent()
															.toString();
													final String newNameFile = repSelect + "\\" + fieldRnm.getText();
													area.appendText(
															"\n" + LocalUtility.getString("message.mutualisation.jdv")
																	+ " :\n\n");
													if (bool) {
														area.appendText(LocalUtility.getString("message.valid.file")
																+ newNameFile + "\n");
													} else {
														area.appendText(LocalUtility.getString("message.invalid.file")
																+ newNameFile + "\n");
													}
												} catch (final IOException e) {
													if (LOG.isInfoEnabled()) {
														final String error = e.getMessage();
														LOG.error(error);
													}
												}
											} else if (Files.isDirectory(path)) {
												if (path.toFile().listFiles().length > 0) {
													area.appendText(
															"\n" + LocalUtility.getString("message.mutualisation.jdv")
																	+ " :\n\n");
													for (final File file : path.toFile().listFiles()) {
														final Path pathF = Paths.get(file.getAbsolutePath());
														if (Files.isDirectory(pathF)) {
															if (pathF.toFile().listFiles().length > 0) {
																for (final File fileF : pathF.toFile().listFiles()) {
																	if (fileF.getAbsolutePath().endsWith(".sch")) {
																		try {
																			boolean bool = Mutualisation.mutualisation(
																					fileF.getAbsolutePath(),
																					fieldRnm.getText());
																			final String fileSelect = fileF
																					.getAbsolutePath();
																			final String repSelect = Paths
																					.get(fileSelect).getParent()
																					.toString();
																			final String newNameFile = repSelect + "\\"
																					+ fieldRnm.getText();
																			if (bool) {
																				area.appendText(LocalUtility
																						.getString("message.valid.file")
																						+ newNameFile + "\n");
																			} else {
																				area.appendText(LocalUtility.getString(
																						"message.invalid.file")
																						+ newNameFile + "\n");
																			}
																		} catch (final IOException e) {
																			if (LOG.isInfoEnabled()) {
																				final String error = e.getMessage();
																				LOG.error(error);
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									});
								} else {
									final Alert alert = new Alert(AlertType.ERROR);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.errorcheckbox"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}

							} else {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Utility.getStylesheets(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Utility.getStyleClass(dialogPane);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.errorsch"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}

						}
					});
				});
			}
		});

		docItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final Stage stage = new Stage();
					final VBox vBox = new VBox();
					final TextArea area = new TextArea();
					area.setEditable(false);
					area.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
					Utility.getChildren(vBox).add(area);
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					// Setting title to the Stage
					stage.titleProperty().bind(LocalUtility.createStringBinding("message.documentation.readme"));
					// Adding scene to the stage
					stage.setScene(scene);
					stage.setMaximized(true);
					stage.show();

					String url = null;
					if (LocalUtility.getLocale().equals(Locale.FRENCH)) {
						url = Utility.getContextClassLoader().getResource(Constant.MDFILE).toExternalForm();
					} else if (LocalUtility.getLocale().equals(Locale.ENGLISH)) {
						url = Utility.getContextClassLoader().getResource(Constant.MDFILEEN).toExternalForm();
					} else {
						url = Utility.getContextClassLoader().getResource(Constant.MDFILEES).toExternalForm();
					}
					File dest;
					dest = new File(Constant.INTEROPFOLDER + "\\document.md");
					try {
						FileUtils.copyURLToFile(new URL(url), dest);
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
					InputStream targetStream;
					try {
						targetStream = Files.newInputStream(Paths.get(dest.toURI()));
						area.setText(IniFile.readFileContents(targetStream));
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
				});
			}
		});

		logItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final Stage stage = new Stage();
					// Creating a scene object
					final VBox vBox = new VBox();
					final TextArea area = new TextArea();
					area.setEditable(false);
					area.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
					Utility.getChildren(vBox).add(area);
					final Scene scene = new Scene(vBox);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					// Setting title to the Stage
					stage.titleProperty().bind(LocalUtility.createStringBinding("message.param.log"));
					// Adding scene to the stage
					stage.setScene(scene);
					stage.setMaximized(true);
					stage.show();
					final File dest = new File(Constant.LOGFOLDFER + "\\log.log");
					if (dest.exists()) {
						InputStream targetStream;
						try {
							targetStream = Files.newInputStream(Paths.get(dest.toURI()));
							area.setText(IniFile.readFileContents(targetStream));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.error.not.log"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		arboItem.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					treeView = new TreeView<>();
					Label emptyLabel = new Label(LocalUtility.getString(ERRORCDA));
					Label emptyLabel1 = new Label(LocalUtility.getString(ERRORMETA));
					final String file = textFieldCda.getText();
					if (file != null && !file.isEmpty()) {
						emptyLabel.setText("");
						final org.w3c.dom.Document xmlDocument = XmlToGraph.loadXmlDocument(textFieldCda.getText());
						if (xmlDocument == null) {
							return;
						}
						try {
							final TreeItem<String> root = com.ans.cda.service.parametrage.Handler
									.readXML(new File(file));
							treeView = new TreeView<>(root);
							treeView.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							final ContextMenu contextMenu = new ContextMenu();
							final MenuItem copyMenuItem = LocalUtility.menuBarForKey("message.param.copy");
							contextMenu.getItems().add(copyMenuItem);
							treeView.setCellFactory(tv -> {
								TreeCell<String> cell = new TreeCell<>() {
									@Override
									protected void updateItem(final String item, final boolean empty) {
										super.updateItem(item, empty);
										setText(empty ? null : item);
									}
								};
								cell.setOnContextMenuRequested(ev -> {
									if (!cell.isEmpty()) {
										contextMenu.show(cell, ev.getScreenX(), ev.getScreenY());
									}
								});
								return cell;
							});
							copyMenuItem.setOnAction(e -> {
								final TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
								if (selectedItem != null) {
									final Clipboard clipboard = Clipboard.getSystemClipboard();
									final ClipboardContent content = new ClipboardContent();
									if (selectedItem.getValue() != null && !selectedItem.getValue().contains("=")) {
										content.putString(selectedItem.getValue());
										clipboard.setContent(content);
									} else if (selectedItem.getValue() != null
											&& selectedItem.getValue().contains("=")) {
										final String[] words = selectedItem.getValue().split("=");
										content.putString(words[1]);
										clipboard.setContent(content);
									}
								}
							});
							XmlToGraph.expandAll(treeView.getRoot());
						} catch (final IOException | SAXException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					}
					treeViewMeta = new TreeView<>();
					final String fileMeta = textFieldMeta.getText();
					if (fileMeta != null && !fileMeta.isEmpty()) {
						emptyLabel1.setText("");
						final org.w3c.dom.Document xmlDocument = XmlToGraph.loadXmlDocument(textFieldMeta.getText());
						if (xmlDocument == null) {
							return;
						}
						try {
							final TreeItem<String> root = com.ans.cda.service.parametrage.Handler
									.readXML(new File(fileMeta));
							treeViewMeta = new TreeView<>(root);
							treeViewMeta.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							final ContextMenu contextMenu = new ContextMenu();
							final MenuItem copyMenuItem = LocalUtility.menuBarForKey("message.param.copy");
							contextMenu.getItems().add(copyMenuItem);
							treeViewMeta.setCellFactory(tv -> {
								TreeCell<String> cell = new TreeCell<>() {
									@Override
									protected void updateItem(final String item, final boolean empty) {
										super.updateItem(item, empty);
										setText(empty ? null : item);
									}
								};
								cell.setOnContextMenuRequested(ev -> {
									if (!cell.isEmpty()) {
										contextMenu.show(cell, ev.getScreenX(), ev.getScreenY());
									}
								});
								return cell;
							});
							copyMenuItem.setOnAction(e -> {
								final TreeItem<String> selectedItem = treeViewMeta.getSelectionModel()
										.getSelectedItem();
								if (selectedItem != null) {
									final Clipboard clipboard = Clipboard.getSystemClipboard();
									final ClipboardContent content = new ClipboardContent();
									if (selectedItem.getValue() != null && !selectedItem.getValue().contains("=")) {
										content.putString(selectedItem.getValue());
										clipboard.setContent(content);
									} else if (selectedItem.getValue() != null
											&& selectedItem.getValue().contains("=")) {
										final String[] words = selectedItem.getValue().split("=");
										content.putString(words[1]);
										clipboard.setContent(content);
									}
								}
							});
							XmlToGraph.expandAll(treeViewMeta.getRoot());
						} catch (final IOException | SAXException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					}
					final StackPane stackPane = new StackPane();
					stackPane.getChildren().addAll(treeView, emptyLabel);

					final StackPane stackPane1 = new StackPane();
					stackPane1.getChildren().addAll(treeViewMeta, emptyLabel1);

					final SplitPane hbS2 = new SplitPane();
					hbS2.getItems().addAll(stackPane, stackPane1);
					hbS2.setDividerPositions(0.5f, 0.5f);

					final Scene scene = new Scene(hbS2, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
					final Stage primaryStage = new Stage();
					primaryStage.setTitle(LocalUtility.getString("message.open.cda.tree"));
					primaryStage.setScene(scene);
					primaryStage.setMaximized(true);
					primaryStage.show();
				});
			}
		});

		paramItem.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10));
					vBox.setSpacing(5);

					final KeyCombination keyCombCtrZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
					final KeyCombination keyCombCtrY = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);

					final Label labelPdf = LocalUtility.labelForValue(() -> LocalUtility.get("message.label.pdf"));
					labelPdf.setPadding(new Insets(5, 5, 5, 5));
					labelPdf.setStyle(Constant.STYLE6);

					final Label labelFop = LocalUtility.labelForValue(() -> LocalUtility.get("message.fop.path"));
					final TextField fieldFop = new TextField();
					fieldFop.setOnMouseEntered(e -> fieldFop.setEffect(shadow));
					fieldFop.setOnMouseExited(e -> fieldFop.setEffect(null));
					labelFop.setPadding(new Insets(5, 5, 5, 5));
					labelFop.setStyle(Constant.STYLE5);
					fieldFop.setPadding(new Insets(5, 5, 5, 5));
					fieldFop.setStyle(Constant.STYLE8);

					// Create a key combination for "Ctrl + C" or "Cmd + C" on Mac
					final KeyCombination copyCombination = new KeyCodeCombination(KeyCode.V,
							KeyCombination.CONTROL_DOWN);
					// Add a key event filter to detect the copy event
					fieldFop.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
						if (copyCombination.match(ev)) {
							fieldFop.getScene().getWindow().requestFocus();
							fieldFop.requestFocus();
							fieldFop.setOnKeyReleased(e -> {
								final String pastedText = fieldFop.getText();
								if (!pastedText.isEmpty()) {
									if (pastedText.endsWith("fop-2.8\\fop")) {
										fieldFop.setText(pastedText);
										IniFile.write(LASTDSELECTED, pastedText, SCHCLEANER);
										IniFile.write(LASTPUSED, pastedText, MEMORY);
									} else {
										final String original = pastedText;
										final String word = Constant.FDSYLE;
										final int index = original.indexOf(word);
										if (index != -1) {
											String result = original.substring(0, index).trim();
											result = result + Constant.PATHFOP;
											fieldFop.setText(result);
											if (result != null) {
												IniFile.write(LASTDSELECTED, result, SCHCLEANER);
												IniFile.write(LASTPUSED,
														new File(result).getParentFile().getAbsolutePath(), MEMORY);
											}
										}
									}
								}
							});
						}
					});

					final Label label = LocalUtility.labelForValue(() -> LocalUtility.get("message.validecross"));
					label.setPadding(new Insets(5, 5, 5, 5));
					label.setStyle(Constant.STYLE6);

					final Label labelApi = LocalUtility.labelForValue(() -> LocalUtility.get("message.urllastresult"));
					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					labelApi.setPadding(new Insets(5, 5, 5, 5));
					labelApi.setStyle(Constant.STYLE5);
					field.setPadding(new Insets(5, 5, 5, 5));
					field.setStyle(Constant.STYLE8);

					final Label labelApi1 = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.validatelastresult"));
					final TextField field1 = new TextField();
					field1.setOnMouseEntered(e -> field1.setEffect(shadow));
					field1.setOnMouseExited(e -> field1.setEffect(null));
					labelApi1.setPadding(new Insets(5, 5, 5, 5));
					labelApi1.setStyle(Constant.STYLE5);
					field1.setPadding(new Insets(5, 5, 5, 5));
					field1.setStyle(Constant.STYLE8);

					final Label labelSch = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.validatelastrepport"));
					final TextField fieldSch = new TextField();
					fieldSch.setOnMouseEntered(e -> fieldSch.setEffect(shadow));
					fieldSch.setOnMouseExited(e -> fieldSch.setEffect(null));
					labelSch.setPadding(new Insets(5, 5, 5, 5));
					labelSch.setStyle(Constant.STYLE5);
					fieldSch.setPadding(new Insets(5, 5, 5, 5));
					fieldSch.setStyle(Constant.STYLE8);

					final Label labelXsl = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.validatelastdemand"));
					final TextField fieldXsl = new TextField();
					fieldXsl.setOnMouseEntered(e -> fieldXsl.setEffect(shadow));
					fieldXsl.setOnMouseExited(e -> fieldXsl.setEffect(null));
					labelXsl.setPadding(new Insets(5, 5, 5, 5));
					labelXsl.setStyle(Constant.STYLE5);
					fieldXsl.setPadding(new Insets(5, 5, 5, 5));
					fieldXsl.setStyle(Constant.STYLE8);

					final Label labelCross = LocalUtility.labelForValue(() -> LocalUtility.get("message.htmlrepport"));
					final TextField fieldCross = new TextField();
					fieldCross.setOnMouseEntered(e -> fieldCross.setEffect(shadow));
					fieldCross.setOnMouseExited(e -> fieldCross.setEffect(null));
					labelCross.setPadding(new Insets(5, 5, 5, 5));
					labelCross.setStyle(Constant.STYLE5);
					fieldCross.setPadding(new Insets(5, 5, 5, 5));
					fieldCross.setStyle(Constant.STYLE8);

					final Label labelXdm = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.phschfolderValid"));
					final TextField fieldXdm = new TextField();
					fieldXdm.setOnMouseEntered(e -> fieldXdm.setEffect(shadow));
					fieldXdm.setOnMouseExited(e -> fieldXdm.setEffect(null));
					labelXdm.setPadding(new Insets(5, 5, 5, 5));
					labelXdm.setStyle(Constant.STYLE5);
					fieldXdm.setPadding(new Insets(5, 5, 5, 5));
					fieldXdm.setStyle(Constant.STYLE8);

					final Label labelAllXdm = LocalUtility.labelForValue(() -> LocalUtility.get("message.phschfolder"));
					final TextField fieldAllXdm = new TextField();
					fieldAllXdm.setOnMouseEntered(e -> fieldAllXdm.setEffect(shadow));
					fieldAllXdm.setOnMouseExited(e -> fieldAllXdm.setEffect(null));
					labelAllXdm.setPadding(new Insets(5, 5, 5, 5));
					labelAllXdm.setStyle(Constant.STYLE5);
					fieldAllXdm.setPadding(new Insets(5, 5, 5, 5));
					fieldAllXdm.setStyle(Constant.STYLE8);

					final Label labelSourceSch = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.folderschsource"));
					final TextField fieldSourceSch = new TextField();
					fieldSourceSch.setOnMouseEntered(e -> fieldSourceSch.setEffect(shadow));
					fieldSourceSch.setOnMouseExited(e -> fieldSourceSch.setEffect(null));
					labelSourceSch.setPadding(new Insets(5, 5, 5, 5));
					labelSourceSch.setStyle(Constant.STYLE5);
					fieldSourceSch.setPadding(new Insets(5, 5, 5, 5));
					fieldSourceSch.setStyle(Constant.STYLE8);

					final Label labelDestSch = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.folderdestsch"));
					final TextField fieldDestSch = new TextField();
					fieldDestSch.setOnMouseEntered(e -> fieldDestSch.setEffect(shadow));
					fieldDestSch.setOnMouseExited(e -> fieldDestSch.setEffect(null));
					labelDestSch.setPadding(new Insets(5, 5, 5, 5));
					labelDestSch.setStyle(Constant.STYLE5);
					fieldDestSch.setPadding(new Insets(5, 5, 5, 5));
					fieldDestSch.setStyle(Constant.STYLE8);

					final Label labelDestXsl = LocalUtility.labelForValue(() -> LocalUtility.get("message.folderxsl"));
					final TextField fieldDestXsl = new TextField();
					fieldDestXsl.setOnMouseEntered(e -> fieldDestXsl.setEffect(shadow));
					fieldDestXsl.setOnMouseExited(e -> fieldDestXsl.setEffect(null));
					labelDestXsl.setPadding(new Insets(5, 5, 5, 5));
					labelDestXsl.setStyle(Constant.STYLE5);
					fieldDestXsl.setPadding(new Insets(5, 5, 5, 5));
					fieldDestXsl.setStyle(Constant.STYLE8);

					final Label labelUrl1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.url.valueset"));
					fieldUrl1.setOnMouseEntered(e -> fieldUrl1.setEffect(shadow));
					fieldUrl1.setOnMouseExited(e -> fieldUrl1.setEffect(null));
					labelUrl1.setPadding(new Insets(5, 5, 5, 5));
					labelUrl1.setStyle(Constant.STYLE5);
					fieldUrl1.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl1.setStyle(Constant.STYLE8);

					final Label labelUrl2 = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.url.codesystem"));
					fieldUrl2.setOnMouseEntered(e -> fieldUrl2.setEffect(shadow));
					fieldUrl2.setOnMouseExited(e -> fieldUrl2.setEffect(null));
					labelUrl2.setPadding(new Insets(5, 5, 5, 5));
					labelUrl2.setStyle(Constant.STYLE5);
					fieldUrl2.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl2.setStyle(Constant.STYLE8);

					final Label labelUrl3 = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.url.codesystem.other"));
					fieldUrl3.setOnMouseEntered(e -> fieldUrl3.setEffect(shadow));
					fieldUrl3.setOnMouseExited(e -> fieldUrl3.setEffect(null));
					labelUrl3.setPadding(new Insets(5, 5, 5, 5));
					labelUrl3.setStyle(Constant.STYLE5);
					fieldUrl3.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl3.setStyle(Constant.STYLE8);

					final Label labelUrl4 = LocalUtility.labelForValue(() -> LocalUtility.get("message.url.other1"));
					fieldUrl4.setOnMouseEntered(e -> fieldUrl4.setEffect(shadow));
					fieldUrl4.setOnMouseExited(e -> fieldUrl4.setEffect(null));
					labelUrl4.setPadding(new Insets(5, 5, 5, 5));
					labelUrl4.setStyle(Constant.STYLE5);
					fieldUrl4.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl4.setStyle(Constant.STYLE8);

					final Label labelUrl5 = LocalUtility.labelForValue(() -> LocalUtility.get("message.url.other"));
					fieldUrl5.setOnMouseEntered(e -> fieldUrl5.setEffect(shadow));
					fieldUrl5.setOnMouseExited(e -> fieldUrl5.setEffect(null));
					labelUrl5.setPadding(new Insets(5, 5, 5, 5));
					labelUrl5.setStyle(Constant.STYLE5);
					fieldUrl5.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl5.setStyle(Constant.STYLE8);

					final Label labelUrl6 = LocalUtility.labelForValue(() -> LocalUtility.get("message.url.other2"));
					fieldUrl6.setOnMouseEntered(e -> fieldUrl6.setEffect(shadow));
					fieldUrl6.setOnMouseExited(e -> fieldUrl6.setEffect(null));
					labelUrl6.setPadding(new Insets(5, 5, 5, 5));
					labelUrl6.setStyle(Constant.STYLE5);
					fieldUrl6.setPadding(new Insets(5, 5, 5, 5));
					fieldUrl6.setStyle(Constant.STYLE8);

					final Region spacer = new Region();
					spacer.setMaxWidth(width - 100);
					HBox.setHgrow(spacer, Priority.ALWAYS);

					final Region spacer1 = new Region();
					spacer1.setMaxHeight(50);
					VBox.setVgrow(spacer1, Priority.ALWAYS);

					final JFXButton button = new JFXButton("");
					button.setOnMouseEntered(e -> button.setEffect(shadow));
					button.setOnMouseExited(e -> button.setEffect(null));
					button.setText(LocalUtility.getString("message.datasave"));
					button.setStyle(Constant.STYLE10);
					button.setDisable(true);

					field.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldFop.textProperty().addListener(new ChangeListener<String>() {
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue,
								String newValue) {
							button.setDisable(newValue.trim().isEmpty());
						}
					});

					fieldFop.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
						}
					});

					fieldUrl1.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldUrl2.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldUrl3.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldUrl4.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldUrl5.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldUrl6.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					field1.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldSch.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldXsl.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldCross.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldXdm.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldAllXdm.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldSourceSch.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldDestSch.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					fieldDestXsl.setOnKeyPressed(new EventHandler<>() {
						@Override
						public void handle(final KeyEvent event) {
							if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
								event.consume();
							}
							button.setDisable(false);
						}
					});

					final Region spacer10 = new Region();
					spacer10.setMaxHeight(20);
					VBox.setVgrow(spacer10, Priority.ALWAYS);

					final Region spacer11 = new Region();
					spacer11.setMaxHeight(20);
					VBox.setVgrow(spacer11, Priority.ALWAYS);

					final HBox hbox = new HBox();
					hbox.getChildren().addAll(spacer, button);

					fieldFop.setText(ValidationService.getFOPDirectory());
					field.setText(SaxonValidator.getNewFilePath().toString());
					field1.setText(SaxonValidator.getNewFilePath1().toString());
					fieldSch.setText(SaxonValidator.getNewFilePath2().toString());
					fieldXsl.setText(SaxonValidator.getNewFilePath3().toString());
					fieldCross.setText(SaxonValidator.getNewFilePath4().toString());
					fieldXdm.setText(ValidationService.getgPoolDirectory());
					fieldAllXdm.setText(ValidationService.getgSchDirectory());
					fieldSourceSch.setText(ValidationService.getgReportsDirectory());
					fieldDestSch.setText(ValidationService.getgSvrlDirectory());
					fieldDestXsl.setText(ValidationService.getgSvrlDirectoryXsl());

					final Label labelP = LocalUtility.labelForValue(() -> LocalUtility.get("message.config.path")
							+ Constant.INTEROPFOLDER + "\\config.properties");
					labelP.setTextFill(Color.RED);

					labelP.setStyle(Constant.STYLE19);

					vBox.getChildren().addAll(labelPdf, labelFop, fieldFop, label, labelApi, field, labelApi1, field1,
							labelSch, fieldSch, labelXsl, fieldXsl, labelCross, fieldCross, labelXdm, fieldXdm,
							labelAllXdm, fieldAllXdm, labelSourceSch, fieldSourceSch, labelDestSch, fieldDestSch,
							labelDestXsl, fieldDestXsl, labelUrl1, fieldUrl1, labelUrl2, fieldUrl2, labelUrl3,
							fieldUrl3, labelUrl4, fieldUrl4, labelUrl5, fieldUrl5, labelUrl6, fieldUrl6, spacer11,
							labelP, spacer1, hbox, spacer10);

					vBox.setPadding(new Insets(20, 10, 0, 10));

					vBox.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					final ScrollPane scroll = new ScrollPane();
					final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					final double width = screenSize.getWidth();
					vBox.setPrefWidth(width - 100);
					scroll.setPrefWidth(width - 100);
					scroll.setContent(vBox);
					scroll.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					// Creating a scene object
					final Scene scene = new Scene(scroll);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					// Setting title to the Stage
					thirdStage.titleProperty().bind(LocalUtility.createStringBinding("message.param"));
					// Adding scene to the stage
					thirdStage.setScene(scene);
					thirdStage.setMaximized(true);
					thirdStage.show();

					button.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								final Alert alertC = new Alert(AlertType.CONFIRMATION);
								alertC.setTitle("Confirmation");
								alertC.setHeaderText(null);
								alertC.setContentText(LocalUtility.getString("message.confirm.dialog"));
								final DialogPane dialogPaneC = alertC.getDialogPane();
								dialogPaneC.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
								dialogPaneC.getStyleClass().add(Constant.DIALOG);
								final Optional<ButtonType> result = alertC.showAndWait();
								if (result.get() == ButtonType.OK) {
									final boolean isOk = ParametrageService.writeInFile(field.getText(),
											field1.getText(), fieldSch.getText(), fieldXsl.getText(),
											fieldCross.getText(), fieldXdm.getText(), fieldAllXdm.getText(),
											fieldSourceSch.getText(), fieldDestSch.getText(), fieldDestXsl.getText(),
											fieldUrl1.getText(), fieldUrl2.getText(), fieldUrl3.getText(),
											fieldFop.getText(), fieldUrl4.getText(), fieldUrl5.getText(),
											fieldUrl6.getText());
									if (isOk) {
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(SUCCES));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} else {
										final Alert alert = new Alert(AlertType.ERROR);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString("message.error.update.url"));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									}
								}
							});
						}
					});

					field.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"TXT files (*.txt)", "*.txt");
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showSaveDialog(thirdStage);
							if (file != null) {
								field.setText(file.getAbsolutePath());
								IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
								IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
							}
						}
					});

					fieldFop.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(thirdStage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								if (selectedDirectory.getPath().endsWith("fop-2.8\\fop")) {
									fieldFop.setText(selectedDirectory.getAbsolutePath());
									if (selectedDirectory != null) {
										IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
										IniFile.write(LASTPUSED, selectedDirectory.getParentFile().getAbsolutePath(),
												MEMORY);
									}
								} else {
									final String original = selectedDirectory.getPath();
									final String word = Constant.FDSYLE;
									final int index = original.indexOf(word);
									if (index != -1) {
										String result = original.substring(0, index).trim();
										result = result + Constant.PATHFOP;
										fieldFop.setText(result);
										if (result != null) {
											IniFile.write(LASTDSELECTED, result, SCHCLEANER);
											IniFile.write(LASTPUSED, new File(result).getParentFile().getAbsolutePath(),
													MEMORY);
										}
									}
								}
							}
						}
					});

					field1.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"XML files (*.xml)", EXTXML);
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showSaveDialog(thirdStage);
							if (file != null) {
								field1.setText(file.getAbsolutePath());
								if (file != null) {
									IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
								}
							}
						}
					});

					fieldSch.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"XML files (*.xml)", EXTXML);
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showSaveDialog(thirdStage);
							if (file != null) {
								fieldSch.setText(file.getAbsolutePath());
								if (file != null) {
									IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
								}
							}
						}
					});

					fieldXsl.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"XML files (*.xml)", EXTXML);
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showSaveDialog(thirdStage);
							if (file != null) {
								fieldXsl.setText(file.getAbsolutePath());
								if (file != null) {
									IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
								}
							}
						}
					});

					fieldCross.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final FileChooser fileChooser = new FileChooser();
							fileChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.CHOOSE));
							final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
									"HTML files (*.html)", "*.html");
							fileChooser.getExtensionFilters().add(extFilter);
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								fileChooser.setInitialDirectory(new File(sChemin));
							} else {
								fileChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File file = fileChooser.showSaveDialog(thirdStage);
							if (file != null) {
								fieldCross.setText(file.getAbsolutePath());
								if (file != null) {
									IniFile.write(LASTDSELECTED, file.getParentFile().getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, file.getParentFile().getAbsolutePath(), MEMORY);
								}
							}
						}
					});

					fieldXdm.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(stage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								fieldXdm.setText(selectedDirectory.getAbsolutePath());
								if (selectedDirectory != null) {
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, Constant.INTEROPINIFILE.getParentFile().getAbsolutePath(),
											MEMORY);
								}

							}
						}
					});

					fieldAllXdm.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(stage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								fieldAllXdm.setText(selectedDirectory.getAbsolutePath());
								if (selectedDirectory != null) {
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
									IniFile.write(LASTPUSED, Constant.INTEROPINIFILE.getParentFile().getAbsolutePath(),
											MEMORY);
								}

							}
						}
					});

					fieldSourceSch.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(stage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								fieldSourceSch.setText(selectedDirectory.getAbsolutePath());
								if (selectedDirectory != null) {
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
								}

							}
						}
					});

					fieldDestSch.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(stage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								fieldDestSch.setText(selectedDirectory.getAbsolutePath());
								if (selectedDirectory != null) {
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
								}

							}
						}
					});

					fieldDestXsl.setOnMouseClicked(e -> {
						if (e.getClickCount() == 2) {
							final DirectoryChooser directoryChooser = new DirectoryChooser();
							directoryChooser.titleProperty().bind(LocalUtility.createStringBinding(Constant.TITLE));
							final String sChemin = IniFile.read(LASTDSELECTED, SCHCLEANER);
							if (new File(sChemin).exists()) {
								directoryChooser.setInitialDirectory(new File(sChemin));
							} else {
								directoryChooser.setInitialDirectory(new File(Constant.DISK));
							}
							final File selectedDirectory = directoryChooser.showDialog(stage);
							if (selectedDirectory != null && selectedDirectory.isDirectory()) {
								fieldDestXsl.setText(selectedDirectory.getAbsolutePath());
								if (selectedDirectory != null) {
									IniFile.write(LASTDSELECTED, selectedDirectory.getAbsolutePath(), SCHCLEANER);
								}

							}
						}
					});
				});
			}
		});

		paramIItem.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final File myfile = Constant.INTEROPINIFILE;
					if (!myfile.exists()) {
						final VBox vBox = new VBox();
						vBox.setPadding(new Insets(10));
						vBox.setSpacing(5);

						final TitledPane paneOneS = new TitledPane();
						paneOneS.setExpanded(false);
						paneOneS.setText(SCHCLEANER);

						final Label labelSApi = new Label(LASTDSELECTED);
						final HBox hboxxS = new HBox();
						hboxxS.getChildren().add(labelSApi);
						final TextField fieldS = new TextField();
						fieldS.setOnMouseEntered(e -> fieldS.setEffect(shadow));
						fieldS.setOnMouseExited(e -> fieldS.setEffect(null));
						labelSApi.setPadding(new Insets(5, 5, 5, 5));
						labelSApi.setStyle(Constant.STYLE51);
						fieldS.setPadding(new Insets(5, 10, 5, 0));
						fieldS.setStyle(Constant.STYLE81);

						final SplitPane hbS2 = new SplitPane();
						hbS2.getItems().addAll(hboxxS, fieldS);
						hbS2.setDividerPositions(0.2f, 0.8f);

						final VBox vbS1 = new VBox();
						vbS1.getChildren().addAll(hbS2);

						paneOneS.setContent(vbS1);

						final TitledPane paneOne = new TitledPane();
						paneOne.setExpanded(false);
						paneOne.setText(MEMORY);

						final Label labelApi = new Label(LASTCDAFILE);
						final HBox hboxx = new HBox();
						hboxx.getChildren().add(labelApi);
						final TextField field = new TextField();
						field.setOnMouseEntered(e -> field.setEffect(shadow));
						field.setOnMouseExited(e -> field.setEffect(null));
						labelApi.setPadding(new Insets(5, 5, 5, 5));
						labelApi.setStyle(Constant.STYLE51);
						field.setPadding(new Insets(5, 10, 5, 0));
						field.setStyle(Constant.STYLE81);
						field.setEditable(false);

						final SplitPane hb1 = new SplitPane();
						hb1.getItems().addAll(hboxx, field);
						hb1.setDividerPositions(0.2f, 0.8f);

						final Label labelApi1 = new Label(LASTPUSED);
						final HBox hboxx1 = new HBox();
						hboxx1.getChildren().add(labelApi1);
						final TextField field1 = new TextField();
						field1.setOnMouseEntered(e -> field1.setEffect(shadow));
						field1.setOnMouseExited(e -> field1.setEffect(null));
						labelApi1.setPadding(new Insets(5, 5, 5, 5));
						labelApi1.setStyle(Constant.STYLE51);
						field1.setPadding(new Insets(5, 10, 5, 0));
						field1.setStyle(Constant.STYLE81);
						field1.setEditable(false);

						final SplitPane hb2 = new SplitPane();
						hb2.getItems().addAll(hboxx1, field1);
						hb2.setDividerPositions(0.2f, 0.8f);

						final Label labelSch = new Label(LASTMAFILE);
						final HBox hboxx2 = new HBox();
						hboxx2.getChildren().add(labelSch);
						final TextField fieldSch = new TextField();
						fieldSch.setOnMouseEntered(e -> fieldSch.setEffect(shadow));
						fieldSch.setOnMouseExited(e -> fieldSch.setEffect(null));
						labelSch.setPadding(new Insets(5, 5, 5, 5));
						labelSch.setStyle(Constant.STYLE51);
						fieldSch.setPadding(new Insets(5, 10, 5, 0));
						fieldSch.setStyle(Constant.STYLE81);
						fieldSch.setEditable(false);

						final SplitPane hb3 = new SplitPane();
						hb3.getItems().addAll(hboxx2, fieldSch);
						hb3.setDividerPositions(0.2f, 0.8f);

						final VBox vb1 = new VBox();
						vb1.getChildren().addAll(hb1, hb2, hb3);

						paneOne.setContent(vb1);

						final Label labelXsl = new Label("FILE-NAME");
						final HBox hboxx3 = new HBox();
						hboxx3.getChildren().add(labelXsl);
						final TextField fieldXsl = new TextField();
						fieldXsl.setOnMouseEntered(e -> fieldXsl.setEffect(shadow));
						fieldXsl.setOnMouseExited(e -> fieldXsl.setEffect(null));
						labelXsl.setPadding(new Insets(5, 5, 5, 5));
						labelXsl.setStyle(Constant.STYLE51);
						fieldXsl.setPadding(new Insets(5, 5, 5, 5));
						fieldXsl.setStyle(Constant.STYLE81);
						fieldXsl.setEditable(false);

						final Label labelCross = new Label("LAST-REQUEST");
						final HBox hboxx4 = new HBox();
						hboxx4.getChildren().add(labelCross);
						final TextField fieldCross = new TextField();
						fieldCross.setOnMouseEntered(e -> fieldCross.setEffect(shadow));
						fieldCross.setOnMouseExited(e -> fieldCross.setEffect(null));
						labelCross.setPadding(new Insets(5, 5, 5, 5));
						labelCross.setStyle(Constant.STYLE51);
						fieldCross.setPadding(new Insets(5, 5, 5, 5));
						fieldCross.setStyle(Constant.STYLE81);
						fieldCross.setEditable(false);

						final Label labelXdm = new Label("API-URL-PROD");
						final HBox hboxx5 = new HBox();
						hboxx5.getChildren().add(labelXdm);
						final TextField fieldXdm = new TextField();
						fieldXdm.setOnMouseEntered(e -> fieldXdm.setEffect(shadow));
						fieldXdm.setOnMouseExited(e -> fieldXdm.setEffect(null));
						labelXdm.setPadding(new Insets(5, 5, 5, 5));
						labelXdm.setStyle(Constant.STYLE51);
						fieldXdm.setPadding(new Insets(5, 5, 5, 5));
						fieldXdm.setStyle(Constant.STYLE81);
						fieldXdm.setEditable(false);

						final Label labelAllXdm = new Label("CDA-VALIDATION-SERVICE-NAME");
						final HBox hboxx6 = new HBox();
						hboxx6.getChildren().add(labelAllXdm);
						final TextField fieldAllXdm = new TextField();
						fieldAllXdm.setOnMouseEntered(e -> fieldAllXdm.setEffect(shadow));
						fieldAllXdm.setOnMouseExited(e -> fieldAllXdm.setEffect(null));
						labelAllXdm.setPadding(new Insets(5, 5, 5, 5));
						labelAllXdm.setStyle(Constant.STYLE51);
						fieldAllXdm.setPadding(new Insets(5, 5, 5, 5));
						fieldAllXdm.setStyle(Constant.STYLE81);
						fieldAllXdm.setEditable(false);

						final Label labelSourceSch = new Label("METADATA-VALIDATION-SERVICE-NAME");
						final HBox hboxx7 = new HBox();
						hboxx7.getChildren().add(labelSourceSch);
						final TextField fieldSourceSch = new TextField();
						fieldSourceSch.setOnMouseEntered(e -> fieldSourceSch.setEffect(shadow));
						fieldSourceSch.setOnMouseExited(e -> fieldSourceSch.setEffect(null));
						labelSourceSch.setPadding(new Insets(5, 5, 5, 5));
						labelSourceSch.setStyle(Constant.STYLE51);
						fieldSourceSch.setPadding(new Insets(5, 5, 5, 5));
						fieldSourceSch.setStyle(Constant.STYLE81);
						fieldSourceSch.setEditable(false);

						final Label labelDestXsl = new Label("ASS_A11");
						final HBox hboxx9 = new HBox();
						hboxx9.getChildren().add(labelDestXsl);
						final TextField fieldDestXsl = new TextField();
						fieldDestXsl.setOnMouseEntered(e -> fieldDestXsl.setEffect(shadow));
						fieldDestXsl.setOnMouseExited(e -> fieldDestXsl.setEffect(null));
						labelDestXsl.setPadding(new Insets(5, 5, 5, 5));
						labelDestXsl.setStyle(Constant.STYLE51);
						fieldDestXsl.setPadding(new Insets(5, 5, 5, 5));
						fieldDestXsl.setStyle(Constant.STYLE81);
						fieldDestXsl.setEditable(false);

						final Label labelallXdm = new Label("ASS_X04");
						final HBox hboxx10 = new HBox();
						hboxx10.getChildren().add(labelallXdm);
						final TextField fieldallXdm = new TextField();
						fieldallXdm.setOnMouseEntered(e -> fieldallXdm.setEffect(shadow));
						fieldallXdm.setOnMouseExited(e -> fieldallXdm.setEffect(null));
						labelallXdm.setPadding(new Insets(5, 5, 5, 5));
						labelallXdm.setStyle(Constant.STYLE51);
						fieldallXdm.setPadding(new Insets(5, 5, 5, 5));
						fieldallXdm.setStyle(Constant.STYLE81);
						fieldallXdm.setEditable(false);

						final Label labelJ01 = new Label("JDV_J01");
						final HBox hboxx11 = new HBox();
						hboxx11.getChildren().add(labelJ01);
						final TextField fieldJ01 = new TextField();
						fieldJ01.setOnMouseEntered(e -> fieldJ01.setEffect(shadow));
						fieldJ01.setOnMouseExited(e -> fieldJ01.setEffect(null));
						labelJ01.setPadding(new Insets(5, 5, 5, 5));
						labelJ01.setStyle(Constant.STYLE51);
						fieldJ01.setPadding(new Insets(5, 5, 5, 5));
						fieldJ01.setStyle(Constant.STYLE81);
						fieldJ01.setEditable(false);

						final Label labelJ02 = new Label("JDV_J02");
						final HBox hboxx12 = new HBox();
						hboxx12.getChildren().add(labelJ02);
						final TextField fieldJ02 = new TextField();
						fieldJ02.setOnMouseEntered(e -> fieldJ02.setEffect(shadow));
						fieldJ02.setOnMouseExited(e -> fieldJ02.setEffect(null));
						labelJ02.setPadding(new Insets(5, 5, 5, 5));
						labelJ02.setStyle(Constant.STYLE51);
						fieldJ02.setPadding(new Insets(5, 5, 5, 5));
						fieldJ02.setStyle(Constant.STYLE81);
						fieldJ02.setEditable(false);

						final Label labelJ04 = new Label("JDV_J04");
						final HBox hboxx13 = new HBox();
						hboxx13.getChildren().add(labelJ04);
						final TextField fieldJ04 = new TextField();
						fieldJ04.setOnMouseEntered(e -> fieldJ04.setEffect(shadow));
						fieldJ04.setOnMouseExited(e -> fieldJ04.setEffect(null));
						labelJ04.setPadding(new Insets(5, 5, 5, 5));
						labelJ04.setStyle(Constant.STYLE51);
						fieldJ04.setPadding(new Insets(5, 5, 5, 5));
						fieldJ04.setStyle(Constant.STYLE81);
						fieldJ04.setEditable(false);

						final Label labelJ06 = new Label("JDV_J06");
						final HBox hboxx14 = new HBox();
						hboxx14.getChildren().add(labelJ06);
						final TextField fieldJ06 = new TextField();
						fieldJ06.setOnMouseEntered(e -> fieldJ06.setEffect(shadow));
						fieldJ06.setOnMouseExited(e -> fieldJ06.setEffect(null));
						labelJ06.setPadding(new Insets(5, 5, 5, 5));
						labelJ06.setStyle(Constant.STYLE51);
						fieldJ06.setPadding(new Insets(5, 5, 5, 5));
						fieldJ06.setStyle(Constant.STYLE81);
						fieldJ06.setEditable(false);

						final Label labelJ10 = new Label("JDV_J10");
						final HBox hboxx15 = new HBox();
						hboxx15.getChildren().add(labelJ10);
						final TextField fieldJ10 = new TextField();
						fieldJ10.setOnMouseEntered(e -> fieldJ10.setEffect(shadow));
						fieldJ10.setOnMouseExited(e -> fieldJ10.setEffect(null));
						labelJ10.setPadding(new Insets(5, 5, 5, 5));
						labelJ10.setStyle(Constant.STYLE51);
						fieldJ10.setPadding(new Insets(5, 5, 5, 5));
						fieldJ10.setStyle(Constant.STYLE81);
						fieldJ10.setEditable(false);

						final Label labelA04 = new Label("TRE_A04");
						final HBox hboxx16 = new HBox();
						hboxx16.getChildren().add(labelA04);
						final TextField fieldA04 = new TextField();
						fieldA04.setOnMouseEntered(e -> fieldA04.setEffect(shadow));
						fieldA04.setOnMouseExited(e -> fieldA04.setEffect(null));
						labelA04.setPadding(new Insets(5, 5, 5, 5));
						labelA04.setStyle(Constant.STYLE51);
						fieldA04.setPadding(new Insets(5, 5, 5, 5));
						fieldA04.setStyle(Constant.STYLE81);
						fieldA04.setEditable(false);

						final Label labelA05 = new Label("TRE_A05");
						final HBox hboxx17 = new HBox();
						hboxx17.getChildren().add(labelA05);
						final TextField fieldA05 = new TextField();
						fieldA05.setOnMouseEntered(e -> fieldA05.setEffect(shadow));
						fieldA05.setOnMouseExited(e -> fieldA05.setEffect(null));
						labelA05.setPadding(new Insets(5, 5, 5, 5));
						labelA05.setStyle(Constant.STYLE51);
						fieldA05.setPadding(new Insets(5, 5, 5, 5));
						fieldA05.setStyle(Constant.STYLE81);
						fieldA05.setEditable(false);

						final Label labelA06 = new Label("TRE_A06");
						final HBox hboxx18 = new HBox();
						hboxx18.getChildren().add(labelA06);
						final TextField fieldA06 = new TextField();
						fieldA06.setOnMouseEntered(e -> fieldA06.setEffect(shadow));
						fieldA06.setOnMouseExited(e -> fieldA06.setEffect(null));
						labelA06.setPadding(new Insets(5, 5, 5, 5));
						labelA06.setStyle(Constant.STYLE51);
						fieldA06.setPadding(new Insets(5, 5, 5, 5));
						fieldA06.setStyle(Constant.STYLE81);
						fieldA06.setEditable(false);

						final Label labelTa = new Label("TYPEWRITER-ACTIVATED");
						final HBox hboxx19 = new HBox();
						hboxx19.getChildren().add(labelTa);
						final TextField fieldTa = new TextField();
						fieldTa.setOnMouseEntered(e -> fieldTa.setEffect(shadow));
						fieldTa.setOnMouseExited(e -> fieldTa.setEffect(null));
						labelTa.setPadding(new Insets(5, 5, 5, 5));
						labelTa.setStyle(Constant.STYLE51);
						fieldTa.setPadding(new Insets(5, 5, 5, 5));
						fieldTa.setStyle(Constant.STYLE81);
						fieldTa.setEditable(false);

						final Label labelSi = new Label("SILENCE");
						final HBox hboxx20 = new HBox();
						hboxx20.getChildren().add(labelSi);
						final TextField fieldSi = new TextField();
						fieldSi.setOnMouseEntered(e -> fieldSi.setEffect(shadow));
						fieldSi.setOnMouseExited(e -> fieldSi.setEffect(null));
						labelSi.setPadding(new Insets(5, 5, 5, 5));
						labelSi.setStyle(Constant.STYLE51);
						fieldSi.setPadding(new Insets(5, 5, 5, 5));
						fieldSi.setStyle(Constant.STYLE81);
						fieldSi.setEditable(false);

						final Label labelDc = new Label("DEFAULT_CDA_NAME");
						final HBox hboxx21 = new HBox();
						hboxx21.getChildren().add(labelDc);
						final TextField fieldDc = new TextField();
						fieldDc.setOnMouseEntered(e -> fieldDc.setEffect(shadow));
						fieldDc.setOnMouseExited(e -> fieldDc.setEffect(null));
						labelDc.setPadding(new Insets(5, 5, 5, 5));
						labelDc.setStyle(Constant.STYLE51);
						fieldDc.setPadding(new Insets(5, 5, 5, 5));
						fieldDc.setStyle(Constant.STYLE81);
						fieldDc.setEditable(false);

						final Label labelDm = new Label("DEFAULT_METADATA_NAME");
						final HBox hboxx22 = new HBox();
						hboxx22.getChildren().add(labelDm);
						final TextField fieldDm = new TextField();
						fieldDm.setOnMouseEntered(e -> fieldDm.setEffect(shadow));
						fieldDm.setOnMouseExited(e -> fieldDm.setEffect(null));
						labelDm.setPadding(new Insets(5, 5, 5, 5));
						labelDm.setStyle(Constant.STYLE51);
						fieldDm.setPadding(new Insets(5, 5, 5, 5));
						fieldDm.setStyle(Constant.STYLE81);
						fieldDm.setEditable(false);

						final Label labelU = new Label("URL-ONTOSERVER");
						final HBox hboxx23 = new HBox();
						hboxx23.getChildren().add(labelU);
						final TextField fieldU = new TextField();
						fieldU.setOnMouseEntered(e -> fieldU.setEffect(shadow));
						fieldU.setOnMouseExited(e -> fieldU.setEffect(null));
						labelU.setPadding(new Insets(5, 5, 5, 5));
						labelU.setStyle(Constant.STYLE51);
						fieldU.setPadding(new Insets(5, 5, 5, 5));
						fieldU.setStyle(Constant.STYLE81);
						fieldU.setEditable(false);

						final Label labelFc = new Label("FHIR2SVS-CONVERTER");
						final HBox hboxx24 = new HBox();
						hboxx24.getChildren().add(labelFc);
						final TextField fieldFc = new TextField();
						fieldFc.setOnMouseEntered(e -> fieldFc.setEffect(shadow));
						fieldFc.setOnMouseExited(e -> fieldFc.setEffect(null));
						labelFc.setPadding(new Insets(5, 5, 5, 5));
						labelFc.setStyle(Constant.STYLE51);
						fieldFc.setPadding(new Insets(5, 5, 5, 5));
						fieldFc.setStyle(Constant.STYLE81);
						fieldFc.setEditable(false);

						final Label labelT = new Label("LOAD-TERMINOLOGY");
						final HBox hboxxT = new HBox();
						hboxxT.getChildren().add(labelT);
						final TextField fieldT = new TextField();
						fieldT.setOnMouseEntered(e -> fieldT.setEffect(shadow));
						fieldT.setOnMouseExited(e -> fieldT.setEffect(null));
						labelT.setPadding(new Insets(5, 5, 5, 5));
						labelT.setStyle(Constant.STYLE51);
						fieldT.setPadding(new Insets(5, 5, 5, 5));
						fieldT.setStyle(Constant.STYLE81);
						fieldT.setEditable(false);

						final Label labelC = new Label("RACINE-CANONIQUE");
						final HBox hboxxC = new HBox();
						hboxxC.getChildren().add(labelC);
						final TextField fieldC = new TextField();
						fieldC.setOnMouseEntered(e -> fieldC.setEffect(shadow));
						fieldC.setOnMouseExited(e -> fieldC.setEffect(null));
						labelC.setPadding(new Insets(5, 5, 5, 5));
						labelC.setStyle(Constant.STYLE51);
						fieldT.setPadding(new Insets(5, 5, 5, 5));
						fieldC.setStyle(Constant.STYLE81);
						fieldC.setEditable(false);

						final Label labelR = new Label("ROOT-DIRECTORY");
						final HBox hboxx25 = new HBox();
						hboxx25.getChildren().add(labelR);
						final TextField fieldR = new TextField();
						fieldR.setOnMouseEntered(e -> fieldR.setEffect(shadow));
						fieldR.setOnMouseExited(e -> fieldR.setEffect(null));
						labelR.setPadding(new Insets(5, 5, 5, 5));
						labelR.setStyle(Constant.STYLE51);
						fieldR.setPadding(new Insets(5, 5, 5, 5));
						fieldR.setStyle(Constant.STYLE81);
						fieldR.setEditable(false);

						button.setText(LocalUtility.getString("message.update"));
						button.setOnMouseEntered(e -> button.setEffect(shadow));
						button.setOnMouseExited(e -> button.setEffect(null));
						button.setStyle(Constant.STYLE30);
						button.setDisable(true);

						final Region spacer = new Region();
						spacer.setMaxWidth(width - 100);
						HBox.setHgrow(spacer, Priority.ALWAYS);

						final HBox hbox = new HBox();
						hbox.getChildren().addAll(spacer, button);

						field.setText(IniFile.read(LASTCDAFILE, MEMORY));
						field1.setText(IniFile.read(LASTPUSED, MEMORY));
						fieldSch.setText(IniFile.read(LASTMAFILE, MEMORY));
						fieldXsl.setText(IniFile.read("FILE-NAME", "LOINC"));
						fieldCross.setText(IniFile.read("LAST-REQUEST", "DIAGNOSTIC"));
						fieldXdm.setText(IniFile.read("API-URL-PROD", API));
						fieldAllXdm.setText(IniFile.read("CDA-VALIDATION-SERVICE-NAME", API));
						fieldSourceSch.setText(IniFile.read("METADATA-VALIDATION-SERVICE-NAME", API));
						fieldDestXsl.setText(IniFile.read("ASS_A11", NOS));
						fieldallXdm.setText(IniFile.read("ASS_X04", NOS));
						fieldJ01.setText(IniFile.read("JDV_J01", NOS));
						fieldJ02.setText(IniFile.read("JDV_J02", NOS));
						fieldJ04.setText(IniFile.read("JDV_J04", NOS));
						fieldJ06.setText(IniFile.read("JDV_J06", NOS));
						fieldJ10.setText(IniFile.read("JDV_J10", NOS));
						fieldA04.setText(IniFile.read("TRE_A04", NOS));
						fieldA05.setText(IniFile.read("TRE_A05", NOS));
						fieldA06.setText(IniFile.read("TRE_A06", NOS));

						fieldTa.setText(IniFile.read("TYPEWRITER-ACTIVATED", "JOSHUA5"));
						fieldSi.setText(IniFile.read("SILENCE", "JOSHUA5"));

						fieldDc.setText(IniFile.read("DEFAULT_CDA_NAME", "IHE_XDM"));
						fieldDm.setText(IniFile.read("DEFAULT_METADATA_NAME", "IHE_XDM"));

						fieldU.setText(IniFile.read("URL-ONTOSERVER", FHIR));
						fieldFc.setText(IniFile.read("FHIR2SVS-CONVERTER", FHIR));
						fieldT.setText(IniFile.read("LOAD-TERMINOLOGY", FHIR));
						fieldC.setText(IniFile.read("RACINE-CANONIQUE", FHIR));

						fieldR.setText(IniFile.read("ROOT-DIRECTORY", "PH-SCHEMATRON"));

						fieldS.setText(IniFile.read(LASTDSELECTED, SCHCLEANER));

						final Label labelP = LocalUtility.labelForValue(
								() -> LocalUtility.get("message.ini.path") + Constant.INTEROPINIFILE.getAbsolutePath());
						labelP.setTextFill(Color.RED);

						labelP.setStyle(Constant.STYLE19);

						final SplitPane hb4 = new SplitPane();
						hb4.getItems().addAll(hboxx3, fieldXsl);
						hb4.setDividerPositions(0.2f, 0.8f);

						final VBox vb2 = new VBox();
						vb2.getChildren().addAll(hb4);

						final TitledPane paneTwo = new TitledPane();
						paneTwo.setExpanded(false);
						paneTwo.setText("LOINC");
						paneTwo.setContent(vb2);

						final SplitPane hb5 = new SplitPane();
						hb5.getItems().addAll(hboxx4, fieldCross);
						hb5.setDividerPositions(0.2f, 0.8f);

						final VBox vb3 = new VBox();
						vb3.getChildren().addAll(hb5);

						final TitledPane paneThree = new TitledPane();
						paneThree.setExpanded(false);
						paneThree.setText(LocalUtility.getString("message.diag"));
						paneThree.setContent(vb3);

						final SplitPane hb6 = new SplitPane();
						hb6.getItems().addAll(hboxx5, fieldXdm);
						hb6.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb7 = new SplitPane();
						hb7.getItems().addAll(hboxx6, fieldAllXdm);
						hb7.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb8 = new SplitPane();
						hb8.getItems().addAll(hboxx7, fieldSourceSch);
						hb8.setDividerPositions(0.2f, 0.8f);

						final VBox vb4 = new VBox();
						vb4.getChildren().addAll(hb6, hb7, hb8);

						final TitledPane paneFor = new TitledPane();
						paneFor.setExpanded(false);
						paneFor.setText(API);
						paneFor.setContent(vb4);

						final SplitPane hb10 = new SplitPane();
						hb10.getItems().addAll(hboxx9, fieldDestXsl);
						hb10.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb11 = new SplitPane();
						hb11.getItems().addAll(hboxx10, fieldallXdm);
						hb11.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb12 = new SplitPane();
						hb12.getItems().addAll(hboxx11, fieldJ01);
						hb12.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb13 = new SplitPane();
						hb13.getItems().addAll(hboxx12, fieldJ02);
						hb13.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb14 = new SplitPane();
						hb14.getItems().addAll(hboxx13, fieldJ04);
						hb14.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb15 = new SplitPane();
						hb15.getItems().addAll(hboxx14, fieldJ06);
						hb15.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb16 = new SplitPane();
						hb16.getItems().addAll(hboxx15, fieldJ10);
						hb16.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb17 = new SplitPane();
						hb17.getItems().addAll(hboxx16, fieldA04);
						hb17.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb18 = new SplitPane();
						hb18.getItems().addAll(hboxx17, fieldA05);
						hb18.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb19 = new SplitPane();
						hb19.getItems().addAll(hboxx18, fieldA06);
						hb19.setDividerPositions(0.2f, 0.8f);

						final VBox vb6 = new VBox();
						vb6.getChildren().addAll(hb10, hb11, hb12, hb13, hb14, hb15, hb16, hb17, hb18, hb19);

						final TitledPane paneSex = new TitledPane();
						paneSex.setExpanded(false);
						paneSex.setText(NOS);
						paneSex.setContent(vb6);

						final SplitPane hb20 = new SplitPane();
						hb20.getItems().addAll(hboxx19, fieldTa);
						hb20.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb21 = new SplitPane();
						hb21.getItems().addAll(hboxx20, fieldSi);
						hb21.setDividerPositions(0.2f, 0.8f);

						final VBox vb7 = new VBox();
						vb7.getChildren().addAll(hb20, hb21);

						final TitledPane paneSeven = new TitledPane();
						paneSeven.setExpanded(false);
						paneSeven.setText("JOSHUA5");
						paneSeven.setContent(vb7);

						final SplitPane hb22 = new SplitPane();
						hb22.getItems().addAll(hboxx21, fieldDc);
						hb22.setDividerPositions(0.2f, 0.8f);
						fieldDc.setPrefWidth(1500);

						final SplitPane hb23 = new SplitPane();
						hb23.getItems().addAll(hboxx22, fieldDm);
						hb23.setDividerPositions(0.2f, 0.8f);
						fieldDm.setPrefWidth(1500);

						final VBox vb8 = new VBox();
						vb8.getChildren().addAll(hb22, hb23);

						final TitledPane paneEight = new TitledPane();
						paneEight.setExpanded(false);
						paneEight.setText("IHE_XDM");
						paneEight.setContent(vb8);

						final SplitPane hb24 = new SplitPane();
						hb24.getItems().addAll(hboxx23, fieldU);
						hb24.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb25 = new SplitPane();
						hb25.getItems().addAll(hboxx24, fieldFc);
						hb25.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb282 = new SplitPane();
						hb282.getItems().addAll(hboxxT, fieldT);
						hb282.setDividerPositions(0.2f, 0.8f);

						final SplitPane hb283 = new SplitPane();
						hb283.getItems().addAll(hboxxC, fieldC);
						hb283.setDividerPositions(0.2f, 0.8f);

						final VBox vb9 = new VBox();
						vb9.getChildren().addAll(hb24, hb25, hb282, hb283);

						final TitledPane paneNine = new TitledPane();
						paneNine.setExpanded(false);
						paneNine.setText(FHIR);
						paneNine.setContent(vb9);

						final SplitPane hb26 = new SplitPane();
						hb26.getItems().addAll(hboxx25, fieldR);
						hb26.setDividerPositions(0.2f, 0.8f);

						final VBox vb10 = new VBox();
						vb10.getChildren().addAll(hb26);

						final TitledPane paneTeen = new TitledPane();
						paneTeen.setExpanded(false);
						paneTeen.setText("PH-SCHEMATRON");
						paneTeen.setContent(vb10);

						final Label labelMain = new Label("URL-ANS-LOGO");
						final HBox hboxx26 = new HBox();
						hboxx26.getChildren().add(labelMain);
						final TextField fieldMain = new TextField();
						fieldMain.setOnMouseEntered(e -> fieldMain.setEffect(shadow));
						fieldMain.setOnMouseExited(e -> fieldMain.setEffect(null));
						fieldMain.setText(IniFile.read("URL-ANS-LOGO", "MAINFORM"));
						labelMain.setPadding(new Insets(5, 5, 5, 5));
						labelMain.setStyle(Constant.STYLE51);
						fieldMain.setPadding(new Insets(5, 5, 5, 5));
						fieldMain.setStyle(Constant.STYLE81);
						fieldMain.setEditable(false);

						final SplitPane hb27 = new SplitPane();
						hb27.getItems().addAll(hboxx26, fieldMain);
						hb27.setDividerPositions(0.2f, 0.8f);

						final VBox vb11 = new VBox();
						vb11.getChildren().addAll(hb27);

						final TitledPane paneEleven = new TitledPane();
						paneEleven.setExpanded(false);
						paneEleven.setText("MAINFORM");
						paneEleven.setContent(vb11);
						paneEleven.setPadding(new Insets(0, 0, 20, 0));

						final Label label3 = new Label("1.3.6.1.4.1.19376.1.3.3");
						final HBox hboxx28 = new HBox();
						hboxx28.getChildren().add(label3);
						final TextField field3 = new TextField();
						field3.setOnMouseEntered(e -> field3.setEffect(shadow));
						field3.setOnMouseExited(e -> field3.setEffect(null));
						label3.setPadding(new Insets(5, 5, 5, 5));
						label3.setStyle(Constant.STYLE51);
						field3.setPadding(new Insets(5, 5, 5, 5));
						field3.setStyle(Constant.STYLE81);
						field3.setEditable(false);

						final SplitPane hb29 = new SplitPane();
						hb29.getItems().addAll(hboxx28, field3);
						hb29.setDividerPositions(0.2f, 0.8f);

						final Label labelMeta = new Label("METADATA");
						final HBox hboxx27 = new HBox();
						hboxx27.getChildren().add(labelMeta);
						final TextField fieldMeta = new TextField();
						fieldMeta.setOnMouseEntered(e -> fieldMeta.setEffect(shadow));
						fieldMeta.setOnMouseExited(e -> fieldMeta.setEffect(null));
						labelMeta.setPadding(new Insets(5, 5, 5, 5));
						labelMeta.setStyle(Constant.STYLE51);
						fieldMeta.setPadding(new Insets(5, 5, 5, 5));
						fieldMeta.setStyle(Constant.STYLE81);
						fieldMeta.setEditable(false);

						final SplitPane hb28 = new SplitPane();
						hb28.getItems().addAll(hboxx27, fieldMeta);
						hb28.setDividerPositions(0.2f, 0.8f);

						final Label label40 = new Label("1.2.250.1.213.1.1.1.40");
						final HBox hboxx29 = new HBox();
						hboxx29.getChildren().add(label40);
						final TextField field40 = new TextField();
						field40.setOnMouseEntered(e -> field40.setEffect(shadow));
						field40.setOnMouseExited(e -> field40.setEffect(null));
						label40.setPadding(new Insets(5, 5, 5, 5));
						label40.setStyle(Constant.STYLE51);
						field40.setPadding(new Insets(5, 5, 5, 5));
						field40.setStyle(Constant.STYLE81);
						field40.setEditable(false);

						final SplitPane hb30 = new SplitPane();
						hb30.getItems().addAll(hboxx29, field40);
						hb30.setDividerPositions(0.2f, 0.8f);

						final Label label41 = new Label("1.2.250.1.213.1.1.1.41");
						final HBox hboxx30 = new HBox();
						hboxx30.getChildren().add(label41);
						final TextField field41 = new TextField();
						field41.setOnMouseEntered(e -> field41.setEffect(shadow));
						field41.setOnMouseExited(e -> field41.setEffect(null));
						label41.setPadding(new Insets(5, 5, 5, 5));
						label41.setStyle(Constant.STYLE51);
						field41.setPadding(new Insets(5, 5, 5, 5));
						field41.setStyle(Constant.STYLE81);
						field41.setEditable(false);

						final SplitPane hb31 = new SplitPane();
						hb31.getItems().addAll(hboxx30, field41);
						hb31.setDividerPositions(0.2f, 0.8f);

						final Label label15 = new Label("1.2.250.1.213.1.1.1.15");
						final HBox hboxx31 = new HBox();
						hboxx31.getChildren().add(label15);
						final TextField field15 = new TextField();
						field15.setOnMouseEntered(e -> field15.setEffect(shadow));
						field15.setOnMouseExited(e -> field15.setEffect(null));
						label15.setPadding(new Insets(5, 5, 5, 5));
						label15.setStyle(Constant.STYLE51);
						field15.setPadding(new Insets(5, 5, 5, 5));
						field15.setStyle(Constant.STYLE81);
						field15.setEditable(false);

						final SplitPane hb32 = new SplitPane();
						hb32.getItems().addAll(hboxx31, field15);
						hb32.setDividerPositions(0.2f, 0.8f);

						final Label label16 = new Label("1.2.250.1.213.1.1.1.16");
						final HBox hboxx32 = new HBox();
						hboxx32.getChildren().add(label16);
						final TextField field16 = new TextField();
						field16.setOnMouseEntered(e -> field16.setEffect(shadow));
						field16.setOnMouseExited(e -> field16.setEffect(null));
						label16.setPadding(new Insets(5, 5, 5, 5));
						label16.setStyle(Constant.STYLE51);
						field16.setPadding(new Insets(5, 5, 5, 5));
						field16.setStyle(Constant.STYLE81);
						field16.setEditable(false);

						final SplitPane hb33 = new SplitPane();
						hb33.getItems().addAll(hboxx32, field16);
						hb33.setDividerPositions(0.2f, 0.8f);

						final Label label25 = new Label("1.2.250.1.213.1.1.1.25");
						final HBox hboxx33 = new HBox();
						hboxx33.getChildren().add(label25);
						final TextField field25 = new TextField();
						field25.setOnMouseEntered(e -> field25.setEffect(shadow));
						field25.setOnMouseExited(e -> field25.setEffect(null));
						label25.setPadding(new Insets(5, 5, 5, 5));
						label25.setStyle(Constant.STYLE51);
						field25.setPadding(new Insets(5, 5, 5, 5));
						field25.setStyle(Constant.STYLE81);
						field25.setEditable(false);

						final SplitPane hb34 = new SplitPane();
						hb34.getItems().addAll(hboxx33, field25);
						hb34.setDividerPositions(0.2f, 0.8f);

						final Label label17 = new Label("1.2.250.1.213.1.1.1.17");
						final HBox hboxx34 = new HBox();
						hboxx34.getChildren().add(label17);
						final TextField field17 = new TextField();
						field17.setOnMouseEntered(e -> field17.setEffect(shadow));
						field17.setOnMouseExited(e -> field17.setEffect(null));
						label17.setPadding(new Insets(5, 5, 5, 5));
						label17.setStyle(Constant.STYLE51);
						field17.setPadding(new Insets(5, 5, 5, 5));
						field17.setStyle(Constant.STYLE81);
						field17.setEditable(false);

						final SplitPane hb35 = new SplitPane();
						hb35.getItems().addAll(hboxx34, field17);
						hb35.setDividerPositions(0.2f, 0.8f);

						final Label label28 = new Label("1.2.250.1.213.1.1.1.28");
						final HBox hboxx35 = new HBox();
						hboxx35.getChildren().add(label28);
						final TextField field28 = new TextField();
						field28.setOnMouseEntered(e -> field28.setEffect(shadow));
						field28.setOnMouseExited(e -> field28.setEffect(null));
						label28.setPadding(new Insets(5, 5, 5, 5));
						label28.setStyle(Constant.STYLE51);
						field28.setPadding(new Insets(5, 5, 5, 5));
						field28.setStyle(Constant.STYLE81);
						field28.setEditable(false);

						final SplitPane hb36 = new SplitPane();
						hb36.getItems().addAll(hboxx35, field28);
						hb36.setDividerPositions(0.2f, 0.8f);

						final Label label27 = new Label("1.2.250.1.213.1.1.1.27");
						final HBox hboxx36 = new HBox();
						hboxx36.getChildren().add(label27);
						final TextField field27 = new TextField();
						field27.setOnMouseEntered(e -> field27.setEffect(shadow));
						field27.setOnMouseExited(e -> field27.setEffect(null));
						label27.setPadding(new Insets(5, 5, 5, 5));
						label27.setStyle(Constant.STYLE51);
						field27.setPadding(new Insets(5, 5, 5, 5));
						field27.setStyle(Constant.STYLE81);
						field27.setEditable(false);

						final SplitPane hb37 = new SplitPane();
						hb37.getItems().addAll(hboxx36, field27);
						hb37.setDividerPositions(0.2f, 0.8f);

						final Label label8 = new Label("1.2.250.1.213.1.1.1.8");
						final HBox hboxx37 = new HBox();
						hboxx37.getChildren().add(label8);
						final TextField field8 = new TextField();
						field8.setOnMouseEntered(e -> field8.setEffect(shadow));
						field8.setOnMouseExited(e -> field8.setEffect(null));
						label8.setPadding(new Insets(5, 5, 5, 5));
						label8.setStyle(Constant.STYLE51);
						field8.setPadding(new Insets(5, 5, 5, 5));
						field8.setStyle(Constant.STYLE81);
						field8.setEditable(false);

						final SplitPane hb38 = new SplitPane();
						hb38.getItems().addAll(hboxx37, field8);
						hb38.setDividerPositions(0.2f, 0.8f);

						final Label label26 = new Label("1.2.250.1.213.1.1.1.26");
						final HBox hboxx38 = new HBox();
						hboxx38.getChildren().add(label26);
						final TextField field26 = new TextField();
						field26.setOnMouseEntered(e -> field26.setEffect(shadow));
						field26.setOnMouseExited(e -> field26.setEffect(null));
						label26.setPadding(new Insets(5, 5, 5, 5));
						label26.setStyle(Constant.STYLE51);
						field26.setPadding(new Insets(5, 5, 5, 5));
						field26.setStyle(Constant.STYLE81);
						field26.setEditable(false);

						final SplitPane hb39 = new SplitPane();
						hb39.getItems().addAll(hboxx38, field26);
						hb39.setDividerPositions(0.2f, 0.8f);

						final Label label22 = new Label("1.2.250.1.213.1.1.1.22");
						final HBox hboxx40 = new HBox();
						hboxx40.getChildren().add(label22);
						final TextField field22 = new TextField();
						field22.setOnMouseEntered(e -> field22.setEffect(shadow));
						field22.setOnMouseExited(e -> field22.setEffect(null));
						label22.setPadding(new Insets(5, 5, 5, 5));
						label22.setStyle(Constant.STYLE51);
						field22.setPadding(new Insets(5, 5, 5, 5));
						field22.setStyle(Constant.STYLE81);
						field22.setEditable(false);

						final SplitPane hb41 = new SplitPane();
						hb41.getItems().addAll(hboxx40, field22);
						hb41.setDividerPositions(0.2f, 0.8f);

						final Label label24 = new Label("1.2.250.1.213.1.1.1.24");
						final HBox hboxx41 = new HBox();
						hboxx41.getChildren().add(label24);
						final TextField field24 = new TextField();
						field24.setOnMouseEntered(e -> field24.setEffect(shadow));
						field24.setOnMouseExited(e -> field24.setEffect(null));
						label24.setPadding(new Insets(5, 5, 5, 5));
						label24.setStyle(Constant.STYLE51);
						field24.setPadding(new Insets(5, 5, 5, 5));
						field24.setStyle(Constant.STYLE81);
						field24.setEditable(false);

						final SplitPane hb42 = new SplitPane();
						hb42.getItems().addAll(hboxx41, field24);
						hb42.setDividerPositions(0.2f, 0.8f);

						final Label label23 = new Label("1.2.250.1.213.1.1.1.23");
						final HBox hboxx42 = new HBox();
						hboxx42.getChildren().add(label23);
						final TextField field23 = new TextField();
						field23.setOnMouseEntered(e -> field23.setEffect(shadow));
						field23.setOnMouseExited(e -> field23.setEffect(null));
						label23.setPadding(new Insets(5, 5, 5, 5));
						label23.setStyle(Constant.STYLE51);
						field23.setPadding(new Insets(5, 5, 5, 5));
						field23.setStyle(Constant.STYLE81);
						field23.setEditable(false);

						final SplitPane hb43 = new SplitPane();
						hb43.getItems().addAll(hboxx42, field23);
						hb43.setDividerPositions(0.2f, 0.8f);

						final Label label21 = new Label("1.2.250.1.213.1.1.1.21");
						final HBox hboxx43 = new HBox();
						hboxx43.getChildren().add(label21);
						final TextField field21 = new TextField();
						field21.setOnMouseEntered(e -> field21.setEffect(shadow));
						field21.setOnMouseExited(e -> field21.setEffect(null));
						label21.setPadding(new Insets(5, 5, 5, 5));
						label21.setStyle(Constant.STYLE51);
						field21.setPadding(new Insets(5, 5, 5, 5));
						field21.setStyle(Constant.STYLE81);
						field21.setEditable(false);

						final SplitPane hb44 = new SplitPane();
						hb44.getItems().addAll(hboxx43, field21);
						hb44.setDividerPositions(0.2f, 0.8f);

						final Label label29 = new Label("1.2.250.1.213.1.1.1.29");
						final HBox hboxx44 = new HBox();
						hboxx44.getChildren().add(label29);
						final TextField field29 = new TextField();
						field29.setOnMouseEntered(e -> field29.setEffect(shadow));
						field29.setOnMouseExited(e -> field29.setEffect(null));
						label29.setPadding(new Insets(5, 5, 5, 5));
						label29.setStyle(Constant.STYLE51);
						field29.setPadding(new Insets(5, 5, 5, 5));
						field29.setStyle(Constant.STYLE81);
						field29.setEditable(false);

						final SplitPane hb45 = new SplitPane();
						hb45.getItems().addAll(hboxx44, field29);
						hb45.setDividerPositions(0.2f, 0.8f);

						final Label label121 = new Label("1.2.250.1.213.1.1.1.12.1");
						final HBox hboxx45 = new HBox();
						hboxx45.getChildren().add(label121);
						final TextField field121 = new TextField();
						field121.setOnMouseEntered(e -> field121.setEffect(shadow));
						field121.setOnMouseExited(e -> field121.setEffect(null));
						label121.setPadding(new Insets(5, 5, 5, 5));
						label121.setStyle(Constant.STYLE51);
						field121.setPadding(new Insets(5, 5, 5, 5));
						field121.setStyle(Constant.STYLE81);
						field121.setEditable(false);

						final SplitPane hb46 = new SplitPane();
						hb46.getItems().addAll(hboxx45, field121);
						hb46.setDividerPositions(0.2f, 0.8f);

						final Label label123 = new Label("1.2.250.1.213.1.1.1.12.3");
						final HBox hboxx46 = new HBox();
						hboxx46.getChildren().add(label123);
						final TextField field123 = new TextField();
						field123.setOnMouseEntered(e -> field123.setEffect(shadow));
						field123.setOnMouseExited(e -> field123.setEffect(null));
						label123.setPadding(new Insets(5, 5, 5, 5));
						label123.setStyle(Constant.STYLE51);
						field123.setPadding(new Insets(5, 5, 5, 5));
						field123.setStyle(Constant.STYLE81);
						field123.setEditable(false);

						final SplitPane hb47 = new SplitPane();
						hb47.getItems().addAll(hboxx46, field123);
						hb47.setDividerPositions(0.2f, 0.8f);

						final Label label122 = new Label("1.2.250.1.213.1.1.1.12.2");
						final HBox hboxx47 = new HBox();
						hboxx47.getChildren().add(label122);
						final TextField field122 = new TextField();
						field122.setOnMouseEntered(e -> field122.setEffect(shadow));
						field122.setOnMouseExited(e -> field122.setEffect(null));
						label122.setPadding(new Insets(5, 5, 5, 5));
						label122.setStyle(Constant.STYLE51);
						field122.setPadding(new Insets(5, 5, 5, 5));
						field122.setStyle(Constant.STYLE81);
						field122.setEditable(false);

						final SplitPane hb48 = new SplitPane();
						hb48.getItems().addAll(hboxx47, field122);
						hb48.setDividerPositions(0.2f, 0.8f);

						final Label label18 = new Label("1.2.250.1.213.1.1.1.18");
						final HBox hboxx48 = new HBox();
						hboxx48.getChildren().add(label18);
						final TextField field18 = new TextField();
						field18.setOnMouseEntered(e -> field18.setEffect(shadow));
						field18.setOnMouseExited(e -> field18.setEffect(null));
						label18.setPadding(new Insets(5, 5, 5, 5));
						label18.setStyle(Constant.STYLE51);
						field18.setPadding(new Insets(5, 5, 5, 5));
						field18.setStyle(Constant.STYLE81);
						field18.setEditable(false);

						final SplitPane hb49 = new SplitPane();
						hb49.getItems().addAll(hboxx48, field18);
						hb49.setDividerPositions(0.2f, 0.8f);

						final Label label20 = new Label("1.2.250.1.213.1.1.1.20");
						final HBox hboxx49 = new HBox();
						hboxx49.getChildren().add(label20);
						final TextField field20 = new TextField();
						field20.setOnMouseEntered(e -> field20.setEffect(shadow));
						field20.setOnMouseExited(e -> field20.setEffect(null));
						label20.setPadding(new Insets(5, 5, 5, 5));
						label20.setStyle(Constant.STYLE51);
						field20.setPadding(new Insets(5, 5, 5, 5));
						field20.setStyle(Constant.STYLE81);
						field20.setEditable(false);

						final SplitPane hb50 = new SplitPane();
						hb50.getItems().addAll(hboxx49, field20);
						hb50.setDividerPositions(0.2f, 0.8f);

						final Label label30 = new Label("1.2.250.1.213.1.1.1.30");
						final HBox hboxx50 = new HBox();
						hboxx50.getChildren().add(label30);
						final TextField field30 = new TextField();
						field30.setOnMouseEntered(e -> field30.setEffect(shadow));
						field30.setOnMouseExited(e -> field30.setEffect(null));
						label30.setPadding(new Insets(5, 5, 5, 5));
						label30.setStyle(Constant.STYLE51);
						field30.setPadding(new Insets(5, 5, 5, 5));
						field30.setStyle(Constant.STYLE81);
						field30.setEditable(false);

						final SplitPane hb51 = new SplitPane();
						hb51.getItems().addAll(hboxx50, field30);
						hb51.setDividerPositions(0.2f, 0.8f);

						final Label label38 = new Label("1.2.250.1.213.1.1.1.38");
						final HBox hboxx51 = new HBox();
						hboxx51.getChildren().add(label38);
						final TextField field38 = new TextField();
						field38.setOnMouseEntered(e -> field38.setEffect(shadow));
						field38.setOnMouseExited(e -> field38.setEffect(null));
						label38.setPadding(new Insets(5, 5, 5, 5));
						label38.setStyle(Constant.STYLE51);
						field38.setPadding(new Insets(5, 5, 5, 5));
						field38.setStyle(Constant.STYLE81);
						field38.setEditable(false);

						final SplitPane hb52 = new SplitPane();
						hb52.getItems().addAll(hboxx51, field38);
						hb52.setDividerPositions(0.2f, 0.8f);

						final Label label37 = new Label("1.2.250.1.213.1.1.1.37");
						final HBox hboxx52 = new HBox();
						hboxx52.getChildren().add(label37);
						final TextField field37 = new TextField();
						field37.setOnMouseEntered(e -> field37.setEffect(shadow));
						field37.setOnMouseExited(e -> field37.setEffect(null));
						label37.setPadding(new Insets(5, 5, 5, 5));
						label37.setStyle(Constant.STYLE51);
						field37.setPadding(new Insets(5, 5, 5, 5));
						field37.setStyle(Constant.STYLE81);
						field37.setEditable(false);

						final SplitPane hb53 = new SplitPane();
						hb53.getItems().addAll(hboxx52, field37);
						hb53.setDividerPositions(0.2f, 0.8f);

						final Label label46 = new Label("1.2.250.1.213.1.1.1.46");
						final HBox hboxx53 = new HBox();
						hboxx53.getChildren().add(label46);
						final TextField field46 = new TextField();
						field46.setOnMouseEntered(e -> field46.setEffect(shadow));
						field46.setOnMouseExited(e -> field46.setEffect(null));
						label46.setPadding(new Insets(5, 5, 5, 5));
						label46.setStyle(Constant.STYLE51);
						field46.setPadding(new Insets(5, 5, 5, 5));
						field46.setStyle(Constant.STYLE81);
						field46.setEditable(false);

						final SplitPane hb54 = new SplitPane();
						hb54.getItems().addAll(hboxx53, field46);
						hb54.setDividerPositions(0.2f, 0.8f);

						final Label label13 = new Label("1.2.250.1.213.1.1.1.13");
						final HBox hboxx54 = new HBox();
						hboxx54.getChildren().add(label13);
						final TextField field13 = new TextField();
						field13.setOnMouseEntered(e -> field13.setEffect(shadow));
						field13.setOnMouseExited(e -> field13.setEffect(null));
						label13.setPadding(new Insets(5, 5, 5, 5));
						label13.setStyle(Constant.STYLE51);
						field13.setPadding(new Insets(5, 5, 5, 5));
						field13.setStyle(Constant.STYLE81);
						field13.setEditable(false);

						final SplitPane hb55 = new SplitPane();
						hb55.getItems().addAll(hboxx54, field13);
						hb55.setDividerPositions(0.2f, 0.8f);

						final Label label32 = new Label("1.2.250.1.213.1.1.1.32");
						final HBox hboxx55 = new HBox();
						hboxx55.getChildren().add(label32);
						final TextField field32 = new TextField();
						field32.setOnMouseEntered(e -> field32.setEffect(shadow));
						field32.setOnMouseExited(e -> field32.setEffect(null));
						label32.setPadding(new Insets(5, 5, 5, 5));
						label32.setStyle(Constant.STYLE51);
						field32.setPadding(new Insets(5, 5, 5, 5));
						field32.setStyle(Constant.STYLE81);
						field32.setEditable(false);

						final SplitPane hb56 = new SplitPane();
						hb56.getItems().addAll(hboxx55, field32);
						hb56.setDividerPositions(0.2f, 0.8f);

						final Label label53 = new Label("1.2.250.1.213.1.1.1.5.3");
						final HBox hboxx56 = new HBox();
						hboxx56.getChildren().add(label53);
						final TextField field53 = new TextField();
						field53.setOnMouseEntered(e -> field53.setEffect(shadow));
						field53.setOnMouseExited(e -> field53.setEffect(null));
						label53.setPadding(new Insets(5, 5, 5, 5));
						label53.setStyle(Constant.STYLE51);
						field53.setPadding(new Insets(5, 5, 5, 5));
						field53.setStyle(Constant.STYLE81);
						field53.setEditable(false);

						final SplitPane hb57 = new SplitPane();
						hb57.getItems().addAll(hboxx56, field53);
						hb57.setDividerPositions(0.2f, 0.8f);

						final Label label51 = new Label("1.2.250.1.213.1.1.1.5.1");
						final HBox hboxx57 = new HBox();
						hboxx57.getChildren().add(label51);
						final TextField field51 = new TextField();
						field51.setOnMouseEntered(e -> field51.setEffect(shadow));
						field51.setOnMouseExited(e -> field51.setEffect(null));
						label51.setPadding(new Insets(5, 5, 5, 5));
						label51.setStyle(Constant.STYLE51);
						field51.setPadding(new Insets(5, 5, 5, 5));
						field51.setStyle(Constant.STYLE81);
						field51.setEditable(false);

						final SplitPane hb58 = new SplitPane();
						hb58.getItems().addAll(hboxx57, field51);
						hb58.setDividerPositions(0.2f, 0.8f);

						final Label label52 = new Label("1.2.250.1.213.1.1.1.5.2");
						final HBox hboxx58 = new HBox();
						hboxx58.getChildren().add(label52);
						final TextField field52 = new TextField();
						field52.setOnMouseEntered(e -> field52.setEffect(shadow));
						field52.setOnMouseExited(e -> field52.setEffect(null));
						label52.setPadding(new Insets(5, 5, 5, 5));
						label52.setStyle(Constant.STYLE51);
						field52.setPadding(new Insets(5, 5, 5, 5));
						field52.setStyle(Constant.STYLE81);
						field52.setEditable(false);

						final SplitPane hb59 = new SplitPane();
						hb59.getItems().addAll(hboxx58, field52);
						hb59.setDividerPositions(0.2f, 0.8f);

						final Label label125 = new Label("1.2.250.1.213.1.1.1.12.5");
						final HBox hboxx59 = new HBox();
						hboxx59.getChildren().add(label125);
						final TextField field125 = new TextField();
						field125.setOnMouseEntered(e -> field125.setEffect(shadow));
						field125.setOnMouseExited(e -> field125.setEffect(null));
						label125.setPadding(new Insets(5, 5, 5, 5));
						label125.setStyle(Constant.STYLE51);
						field125.setPadding(new Insets(5, 5, 5, 5));
						field125.setStyle(Constant.STYLE81);
						field125.setEditable(false);

						final SplitPane hb60 = new SplitPane();
						hb60.getItems().addAll(hboxx59, field125);
						hb60.setDividerPositions(0.2f, 0.8f);

						final Label label124 = new Label("1.2.250.1.213.1.1.1.12.4");
						final HBox hboxx60 = new HBox();
						hboxx60.getChildren().add(label124);
						final TextField field124 = new TextField();
						field124.setOnMouseEntered(e -> field124.setEffect(shadow));
						field124.setOnMouseExited(e -> field124.setEffect(null));
						label124.setPadding(new Insets(5, 5, 5, 5));
						label124.setStyle(Constant.STYLE51);
						field124.setPadding(new Insets(5, 5, 5, 5));
						field124.setStyle(Constant.STYLE81);
						field124.setEditable(false);

						final SplitPane hb61 = new SplitPane();
						hb61.getItems().addAll(hboxx60, field124);
						hb61.setDividerPositions(0.2f, 0.8f);

						final Label label42 = new Label("1.2.250.1.213.1.1.1.42");
						final HBox hboxx61 = new HBox();
						hboxx61.getChildren().add(label42);
						final TextField field42 = new TextField();
						field42.setOnMouseEntered(e -> field42.setEffect(shadow));
						field42.setOnMouseExited(e -> field42.setEffect(null));
						label42.setPadding(new Insets(5, 5, 5, 5));
						label42.setStyle(Constant.STYLE51);
						field42.setPadding(new Insets(5, 5, 5, 5));
						field42.setStyle(Constant.STYLE81);
						field42.setEditable(false);

						final SplitPane hb62 = new SplitPane();
						hb62.getItems().addAll(hboxx61, field42);
						hb62.setDividerPositions(0.2f, 0.8f);

						final Label label201 = new Label("1.3.6.1.4.1.19376.1.2.20");
						final HBox hboxx62 = new HBox();
						hboxx62.getChildren().add(label201);
						final TextField field201 = new TextField();
						field201.setOnMouseEntered(e -> field201.setEffect(shadow));
						field201.setOnMouseExited(e -> field201.setEffect(null));
						label201.setPadding(new Insets(5, 5, 5, 5));
						label201.setStyle(Constant.STYLE51);
						field201.setPadding(new Insets(5, 5, 5, 5));
						field201.setStyle(Constant.STYLE81);
						field201.setEditable(false);

						final SplitPane hb63 = new SplitPane();
						hb63.getItems().addAll(hboxx62, field201);
						hb63.setDividerPositions(0.2f, 0.8f);

						field3.setText(IniFile.read("1.3.6.1.4.1.19376.1.3.3", APIMAPPING));
						fieldMeta.setText(IniFile.read("METADATA", APIMAPPING));
						field40.setText(IniFile.read("1.2.250.1.213.1.1.1.40", APIMAPPING));
						field41.setText(IniFile.read("1.2.250.1.213.1.1.1.41", APIMAPPING));
						field15.setText(IniFile.read("1.2.250.1.213.1.1.1.15", APIMAPPING));
						field16.setText(IniFile.read("1.2.250.1.213.1.1.1.16", APIMAPPING));
						field25.setText(IniFile.read("1.2.250.1.213.1.1.1.25", APIMAPPING));
						field17.setText(IniFile.read("1.2.250.1.213.1.1.1.17", APIMAPPING));
						field28.setText(IniFile.read("1.2.250.1.213.1.1.1.28", APIMAPPING));
						field27.setText(IniFile.read("1.2.250.1.213.1.1.1.27", APIMAPPING));
						field8.setText(IniFile.read("1.2.250.1.213.1.1.1.8", APIMAPPING));
						field26.setText(IniFile.read("1.2.250.1.213.1.1.1.26", APIMAPPING));
						field22.setText(IniFile.read("1.2.250.1.213.1.1.1.22", APIMAPPING));
						field24.setText(IniFile.read("1.2.250.1.213.1.1.1.24", APIMAPPING));
						field23.setText(IniFile.read("1.2.250.1.213.1.1.1.23", APIMAPPING));
						field21.setText(IniFile.read("1.2.250.1.213.1.1.1.21", APIMAPPING));
						field29.setText(IniFile.read("1.2.250.1.213.1.1.1.29", APIMAPPING));
						field121.setText(IniFile.read("1.2.250.1.213.1.1.1.12.1", APIMAPPING));
						field123.setText(IniFile.read("1.2.250.1.213.1.1.1.12.3", APIMAPPING));
						field122.setText(IniFile.read("1.2.250.1.213.1.1.1.12.2", APIMAPPING));
						field18.setText(IniFile.read("1.2.250.1.213.1.1.1.18", APIMAPPING));
						field20.setText(IniFile.read("1.2.250.1.213.1.1.1.20", APIMAPPING));
						field30.setText(IniFile.read("1.2.250.1.213.1.1.1.30", APIMAPPING));
						field38.setText(IniFile.read("1.2.250.1.213.1.1.1.38", APIMAPPING));
						field46.setText(IniFile.read("1.2.250.1.213.1.1.1.46", APIMAPPING));
						field13.setText(IniFile.read("1.2.250.1.213.1.1.1.13", APIMAPPING));
						field32.setText(IniFile.read("1.2.250.1.213.1.1.1.32", APIMAPPING));
						field53.setText(IniFile.read("1.2.250.1.213.1.1.1.5.3", APIMAPPING));
						field51.setText(IniFile.read("1.2.250.1.213.1.1.1.5.1", APIMAPPING));
						field52.setText(IniFile.read("1.2.250.1.213.1.1.1.5.2", APIMAPPING));
						field125.setText(IniFile.read("1.2.250.1.213.1.1.1.12.5", APIMAPPING));
						field124.setText(IniFile.read("1.2.250.1.213.1.1.1.12.4", APIMAPPING));
						field42.setText(IniFile.read("1.2.250.1.213.1.1.1.42", APIMAPPING));
						field201.setText(IniFile.read("1.3.6.1.4.1.19376.1.2.20", APIMAPPING));
						field201.setText(IniFile.read("1.2.250.1.213.1.1.1.20", APIMAPPING));
						field37.setText(IniFile.read("1.2.250.1.213.1.1.1.37", APIMAPPING));

						final VBox vb12 = new VBox();
						vb12.getChildren().addAll(hb28, hb29, hb30, hb31, hb32, hb33, hb34, hb35, hb36, hb37, hb38,
								hb39, hb41, hb42, hb43, hb44, hb45, hb46, hb47, hb48, hb49, hb50, hb51, hb52, hb53,
								hb54, hb55, hb56, hb57, hb58, hb59, hb60, hb61, hb62, hb63);

						final TitledPane pane12 = new TitledPane();
						pane12.setExpanded(false);
						pane12.setText(APIMAPPING);
						pane12.setContent(vb12);
						pane12.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						final VBox vboxArea = new VBox();

						area.getStylesheets().add(getClass().getResource(Constant.NOTEPAD).toExternalForm());

						final KeyCombination keyCombCtrZ = new KeyCodeCombination(KeyCode.Z,
								KeyCombination.SHORTCUT_DOWN);
						final KeyCombination keyCombCtrY = new KeyCodeCombination(KeyCode.Y,
								KeyCombination.SHORTCUT_DOWN);
						area.setOnKeyPressed(new EventHandler<>() {
							@Override
							public void handle(final KeyEvent event) {
								if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
									event.consume();
								}
								button.setDisable(false);
							}
						});

						final File myFile = Constant.INTEROPINIFILE;
						InputStream targetStream;
						try {
							targetStream = Files.newInputStream(Paths.get(myFile.toURI()));
							area.setText(IniFile.readFileContents(targetStream));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						vboxArea.getChildren().add(area);
						paneArea.setExpanded(false);
						paneArea.setText(LocalUtility.getString("message.file.ini"));
						paneArea.setContent(vboxArea);
						paneArea.setPadding(new Insets(0, 0, 20, 0));

						accordion.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						accordion.getPanes().addAll(pane12, paneOne, paneOneS, paneTwo, paneThree, paneFor, paneSex,
								paneSeven, paneEight, paneNine, paneTeen, paneEleven, paneArea);

						final Label label = LocalUtility
								.labelForValue(() -> LocalUtility.get("message.param.ini.file"));
						label.setStyle(Constant.STYLE82);
						label.setPadding(new Insets(20, 0, 20, 0));

						vBox.getChildren().addAll(label, accordion, labelP, hbox);

						vBox.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						final ScrollPane scroll = new ScrollPane();
						scroll.setContent(vBox);
						scroll.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						final Stage thirdStage = new Stage();
						// Creating a scene object
						final Scene scene = new Scene(scroll);
						scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
						scene.setFill(Color.LIGHTGRAY);
						GridPane.setHgrow(vBox, Priority.ALWAYS);
						GridPane.setVgrow(vBox, Priority.ALWAYS);

						scroll.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
						// Setting title to the Stage
						thirdStage.setTitle("InteropStudio2024.ini");
						// Adding scene to the stage
						thirdStage.setScene(scene);
						thirdStage.setMaximized(true);
						thirdStage.show();
						button.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									try {
										final Alert alertC = new Alert(AlertType.CONFIRMATION);
										alertC.setTitle("Confirmation");
										alertC.setHeaderText(null);
										alertC.setContentText(LocalUtility.getString("message.confirm.dialog"));
										final DialogPane dialogPaneC = alertC.getDialogPane();
										dialogPaneC.getStylesheets()
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										dialogPaneC.getStyleClass().add(Constant.DIALOG);
										final Optional<ButtonType> result = alertC.showAndWait();
										if (result.get() == ButtonType.OK) {
											accordion.setExpandedPane(null);
											final Path fileName = Path.of(myFile.getAbsolutePath());
											Files.writeString(fileName, area.getText());
											final String fileContent = Files.readString(fileName);
											area.clear();
											area.setText(fileContent);
											try {
												final Map<String, Map<String, String>> iniFileContents = new ConcurrentHashMap<>();
												final INIConfiguration iniConfiguration = new INIConfiguration();
												try (final BufferedReader fileReader = Files
														.newBufferedReader(Paths.get(myfile.toURI()))) {
													iniConfiguration.read(fileReader);
												} catch (final ConfigurationException e) {
													if (LOG.isInfoEnabled()) {
														final String error = e.getMessage();
														LOG.error(error);
													}
												}

												for (final String section : iniConfiguration.getSections()) {
													final Map<String, String> subSectionMap = new ConcurrentHashMap<>();
													final SubnodeConfiguration confSection = iniConfiguration
															.getSection(section);
													final Iterator<String> keyIterator = confSection.getKeys();
													while (keyIterator.hasNext()) {
														final String key = keyIterator.next();
														final String value = confSection.getProperty(key).toString();
														subSectionMap.put(key, value);
													}
													iniFileContents.put(section, subSectionMap);
												}

												accordion.getPanes().clear();
												for (final Entry<String, Map<String, String>> entry : iniFileContents
														.entrySet()) {
													final String key = entry.getKey();
													final Map<String, String> value = entry.getValue();
													final TitledPane newPane = new TitledPane();
													newPane.setExpanded(false);
													newPane.setText(key);
													final VBox vb = new VBox();
													for (final Entry<String, String> entryValue : value.entrySet()) {
														final String keyV = entryValue.getKey();
														final String valueV = entryValue.getValue();
														final Label label = new Label(keyV);
														final HBox hbox = new HBox();
														hbox.getChildren().add(label);
														final TextField field = new TextField();
														field.setOnMouseEntered(e -> field.setEffect(shadow));
														field.setOnMouseExited(e -> field.setEffect(null));
														label.setPadding(new Insets(5, 5, 5, 5));
														label.setStyle(Constant.STYLE51);
														field.setPadding(new Insets(5, 10, 5, 0));
														field.setStyle(Constant.STYLE81);
														field.setText(valueV);
														field.setPrefWidth(1500);
														field.setDisable(true);
														final SplitPane hbPane = new SplitPane();
														hbPane.getItems().addAll(hbox, field);
														hbPane.setDividerPositions(0.2f, 0.8f);
														vb.getChildren().addAll(hbPane);
													}
													newPane.setContent(vb);
													newPane.getStylesheets()
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													accordion.getPanes().add(newPane);
													GridPane.setHgrow(vBox, Priority.ALWAYS);
													GridPane.setVgrow(vBox, Priority.ALWAYS);
													accordion.getStylesheets()
															.add(getClass().getResource(Constant.CSS).toExternalForm());

												}
												paneArea.setPadding(new Insets(20, 0, 20, 0));
												accordion.getPanes().add(paneArea);
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}

											button.setDisable(true);
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(SUCCES));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});
					} else {
						final VBox vBox = new VBox();
						vBox.setPadding(new Insets(10));
						vBox.setSpacing(5);
						button.setText(LocalUtility.getString("message.update"));
						button.setStyle(Constant.STYLE30);
						button.setDisable(true);
						button.setOnMouseEntered(e -> button.setEffect(shadow));
						button.setOnMouseExited(e -> button.setEffect(null));

						final Region spacer = new Region();
						spacer.setMaxWidth(width - 100);
						HBox.setHgrow(spacer, Priority.ALWAYS);

						final HBox hbox = new HBox();
						hbox.getChildren().addAll(spacer, button);

						final Label labelP = LocalUtility.labelForValue(
								() -> LocalUtility.get("message.ini.path") + Constant.INTEROPINIFILE.getAbsolutePath());
						labelP.setTextFill(Color.RED);
						labelP.setStyle(Constant.STYLE19);

						final VBox vboxArea = new VBox();
						area.getStylesheets().add(getClass().getResource(Constant.NOTEPAD).toExternalForm());
						final KeyCombination keyCombCtrZ = new KeyCodeCombination(KeyCode.Z,
								KeyCombination.SHORTCUT_DOWN);
						final KeyCombination keyCombCtrY = new KeyCodeCombination(KeyCode.Y,
								KeyCombination.SHORTCUT_DOWN);
						area.setOnKeyPressed(new EventHandler<>() {

							@Override
							public void handle(KeyEvent event) {
								if (keyCombCtrZ.match(event) || keyCombCtrY.match(event)) {
									event.consume();
								}
								button.setDisable(false);
							}
						});

						final File myFile = Constant.INTEROPINIFILE;
						try (InputStream targetStream = Files.newInputStream(Paths.get(myfile.toURI()))) {
							area.setText(IniFile.readFileContents(targetStream));
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
						vboxArea.getChildren().add(area);

						paneArea.setExpanded(false);
						paneArea.setText(LocalUtility.getString("message.file.ini"));
						paneArea.setContent(vboxArea);
						paneArea.setPadding(new Insets(0, 0, 20, 0));

						final Label label = LocalUtility
								.labelForValue(() -> LocalUtility.get("message.param.ini.file"));
						label.setStyle(Constant.STYLE82);
						label.setPadding(new Insets(20, 0, 10, 0));

						constructAccordion();

						vBox.getChildren().addAll(label, accordion, labelP, hbox);
						vBox.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						final ScrollPane scroll = new ScrollPane();
						scroll.setContent(vBox);
						scroll.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						final Stage thirdStage = new Stage();
						// Creating a scene object
						final Scene scene = new Scene(scroll);
						scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
						scene.setFill(Color.LIGHTGRAY);
						GridPane.setHgrow(vBox, Priority.ALWAYS);
						GridPane.setVgrow(vBox, Priority.ALWAYS);

						scroll.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
						// Setting title to the Stage
						thirdStage.setTitle("InteropStudio2024.ini");
						// Adding scene to the stage
						thirdStage.setScene(scene);
						thirdStage.setMaximized(true);
						thirdStage.show();

						button.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									try {
										final Alert alertC = new Alert(AlertType.CONFIRMATION);
										alertC.setTitle("Confirmation");
										alertC.setHeaderText(null);
										alertC.setContentText(LocalUtility.getString("message.confirm.dialog"));
										final DialogPane dialogPaneC = alertC.getDialogPane();
										dialogPaneC.getStylesheets()
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										dialogPaneC.getStyleClass().add(Constant.DIALOG);
										final Optional<ButtonType> result = alertC.showAndWait();
										if (result.get() == ButtonType.OK) {
											accordion.setExpandedPane(null);
											final Path fileName = Path.of(myFile.getAbsolutePath());
											Files.writeString(fileName, area.getText());
											final String fileContent = Files.readString(fileName);
											area.clear();
											area.setText(fileContent);
											try {
												final Map<String, Map<String, String>> iniFileContents = new ConcurrentHashMap<>();
												final INIConfiguration iniConfiguration = new INIConfiguration();
												try (BufferedReader fileReader = Files
														.newBufferedReader(Paths.get(myFile.toURI()))) {
													iniConfiguration.read(fileReader);
												} catch (final ConfigurationException e) {
													if (LOG.isInfoEnabled()) {
														final String error = e.getMessage();
														LOG.error(error);
													}
												}

												for (final String section : iniConfiguration.getSections()) {
													final Map<String, String> subSectionMap = new ConcurrentHashMap<>();
													final SubnodeConfiguration confSection = iniConfiguration
															.getSection(section);
													final Iterator<String> keyIterator = confSection.getKeys();
													while (keyIterator.hasNext()) {
														final String key = keyIterator.next();
														final String value = confSection.getProperty(key).toString();
														subSectionMap.put(key, value);
													}
													iniFileContents.put(section, subSectionMap);
												}

												accordion.getPanes().clear();
												for (final Entry<String, Map<String, String>> entry : iniFileContents
														.entrySet()) {
													final String key = entry.getKey();
													final Map<String, String> value = entry.getValue();
													final TitledPane newPane = new TitledPane();
													newPane.setExpanded(false);
													newPane.setText(key);
													final VBox vbMap = new VBox();
													for (final Entry<String, String> entryValue : value.entrySet()) {
														final String keyV = entryValue.getKey();
														final String valueV = entryValue.getValue();
														final Label label = new Label(keyV);
														final HBox hbox = new HBox();
														hbox.getChildren().add(label);
														final TextField field = new TextField();
														field.setOnMouseEntered(e -> field.setEffect(shadow));
														field.setOnMouseExited(e -> field.setEffect(null));
														label.setPadding(new Insets(5, 5, 5, 5));
														label.setStyle(Constant.STYLE51);
														field.setPadding(new Insets(5, 10, 5, 0));
														field.setStyle(Constant.STYLE81);
														field.setText(valueV);
														field.setPrefWidth(1500);
														final SplitPane hbPane = new SplitPane();
														hbPane.getItems().addAll(hbox, field);
														hbPane.setDividerPositions(0.2f, 0.8f);
														vbMap.getChildren().addAll(hbPane);
													}
													newPane.setContent(vbMap);
													newPane.getStylesheets()
															.add(getClass().getResource(Constant.CSS).toExternalForm());
													accordion.getPanes().add(newPane);
													GridPane.setHgrow(vBox, Priority.ALWAYS);
													GridPane.setVgrow(vBox, Priority.ALWAYS);
													accordion.getStylesheets()
															.add(getClass().getResource(Constant.CSS).toExternalForm());

												}
												paneArea.setPadding(new Insets(20, 0, 20, 0));
												accordion.getPanes().add(paneArea);
											} catch (final IOException e) {
												if (LOG.isInfoEnabled()) {
													final String error = e.getMessage();
													LOG.error(error);
												}
											}
											button.setDisable(true);
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(SUCCES));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});
					}
				});
			}
		});

		paramMItem.setOnAction(new EventHandler<>() {

			@SuppressWarnings("unchecked")
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final VBox vBox = new VBox();
					vBox.setPadding(new Insets(10));
					vBox.setSpacing(5);

					final Label labelApi = new Label("OID");
					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					labelApi.setPadding(new Insets(5, 5, 5, 5));
					labelApi.setStyle(Constant.STYLE5);
					field.setPadding(new Insets(5, 5, 5, 5));
					field.setStyle(Constant.STYLE8);
					field.setPromptText(LocalUtility.get("message.prompt.oid"));
					field.setFocusTraversable(false);

					final JFXButton btnF = new JFXButton("");
					btnF.setOnMouseEntered(e -> btnF.setEffect(shadow));
					btnF.setOnMouseExited(e -> btnF.setEffect(null));
					final ImageView viewF = new ImageView(Constant.TRASH);
					viewF.setEffect(sepiaTone);
					btnF.setGraphic(viewF);
					btnF.setStyle(Constant.STYLE1);
					btnF.setPrefSize(30, 30);
					btnF.setMinSize(30, 30);
					btnF.setMaxSize(30, 30);
					final Tooltip tooltipF = LocalUtility.createBoundTooltip("message.delete.gen");
					btnF.setTooltip(tooltipF);

					final Label labelApi1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.name.gazelle"));
					final TextField field1 = new TextField();
					field1.setOnMouseEntered(e -> field1.setEffect(shadow));
					field1.setOnMouseExited(e -> field1.setEffect(null));
					labelApi1.setPadding(new Insets(5, 5, 5, 5));
					labelApi1.setStyle(Constant.STYLE5);
					field1.setPadding(new Insets(5, 5, 5, 5));
					field1.setStyle(Constant.STYLE8);
					field1.setPromptText(LocalUtility.get("message.prompt.gazelle"));
					field1.setFocusTraversable(false);

					final Label labelApiF = LocalUtility.labelForValue(() -> LocalUtility.get("message.filter.oid"));
					final TextField fieldF = new TextField();
					fieldF.setOnMouseEntered(e -> fieldF.setEffect(shadow));
					fieldF.setOnMouseExited(e -> fieldF.setEffect(null));
					labelApiF.setPadding(new Insets(5, 5, 5, 5));
					labelApiF.setStyle(Constant.STYLE5);
					fieldF.setPadding(new Insets(5, 5, 5, 5));
					fieldF.setStyle(Constant.STYLE8);

					final JFXButton btn = new JFXButton("");
					btn.setOnMouseEntered(e -> btn.setEffect(shadow));
					btn.setOnMouseExited(e -> btn.setEffect(null));
					final ImageView viewb = new ImageView(Constant.TRASH);
					viewb.setEffect(sepiaTone);
					btn.setGraphic(viewb);
					btn.setStyle(Constant.STYLE1);
					btn.setPrefSize(30, 30);
					btn.setMinSize(30, 30);
					btn.setMaxSize(30, 30);
					final Tooltip tooltipbtn = LocalUtility.createBoundTooltip("message.delete.gen");
					btn.setTooltip(tooltipbtn);

					final JFXButton btn1 = new JFXButton("");
					btn1.setOnMouseEntered(e -> btn1.setEffect(shadow));
					btn1.setOnMouseExited(e -> btn1.setEffect(null));
					final ImageView viewb1 = new ImageView(Constant.TRASH);
					viewb1.setEffect(sepiaTone);
					btn1.setGraphic(viewb1);
					btn1.setStyle(Constant.STYLE1);
					btn1.setPrefSize(30, 30);
					btn1.setMinSize(30, 30);
					btn1.setMaxSize(30, 30);
					final Tooltip tooltipbtn1 = LocalUtility.createBoundTooltip("message.delete.gen");
					btn1.setTooltip(tooltipbtn1);

					final JFXButton btnF1 = new JFXButton("");
					btnF1.setOnMouseEntered(e -> btn1.setEffect(shadow));
					btnF1.setOnMouseExited(e -> btn1.setEffect(null));
					final ImageView viewF1 = new ImageView(Constant.TRASH);
					viewF1.setEffect(sepiaTone);
					btnF1.setGraphic(viewF1);
					btnF1.setStyle(Constant.STYLE1);
					btnF1.setPrefSize(30, 30);
					btnF1.setMinSize(30, 30);
					btnF1.setMaxSize(30, 30);
					final Tooltip tooltipF1 = LocalUtility.createBoundTooltip("message.delete.gen");
					btnF1.setTooltip(tooltipF1);

					final Label labelApi1F = LocalUtility
							.labelForValue(() -> LocalUtility.get("message.filter.servname"));
					final TextField field1F = new TextField();
					field1F.setOnMouseEntered(e -> field1F.setEffect(shadow));
					field1F.setOnMouseExited(e -> field1F.setEffect(null));
					labelApi1F.setPadding(new Insets(5, 5, 5, 5));
					labelApi1F.setStyle(Constant.STYLE5);
					field1F.setPadding(new Insets(5, 5, 5, 5));
					field1F.setStyle(Constant.STYLE8);

					final Label labelP = LocalUtility.labelForValue(() -> LocalUtility.get("message.config.path")
							+ Constant.INTEROPFOLDER + "\\config.properties");
					labelP.setPadding(new Insets(5, 5, 5, 5));
					labelP.setStyle(Constant.STYLE19);
					labelP.setTextFill(Color.RED);

					final Region spacer = new Region();
					spacer.setMaxWidth(width - 100);
					HBox.setHgrow(spacer, Priority.ALWAYS);

					final Region spacer1 = new Region();
					spacer1.setMaxHeight(10);
					VBox.setVgrow(spacer1, Priority.ALWAYS);

					final JFXButton button = new JFXButton("");
					button.setOnMouseEntered(e -> button.setEffect(shadow));
					button.setOnMouseExited(e -> button.setEffect(null));
					button.setText(LocalUtility.getString("message.datasave"));
					button.setStyle(Constant.STYLE10);

					final JFXButton buttonRemove = new JFXButton("");
					buttonRemove.setOnMouseEntered(e -> buttonRemove.setEffect(shadow));
					buttonRemove.setOnMouseExited(e -> buttonRemove.setEffect(null));
					buttonRemove.setText(LocalUtility.getString("message.delete.oid"));
					buttonRemove.setStyle(Constant.STYLE10);

					final TableView<ParamEntity> listView = new TableView<>();
					listView.setPrefWidth(700);
					final List<ParamEntity> map = ParametrageService.readOidInPropFile();
					final ObservableList<ParamEntity> observableMap = FXCollections.observableList(map);
					final FilteredList<ParamEntity> filteredData = new FilteredList<>(observableMap, p -> true);
					listView.setItems(observableMap);

					final TableColumn<ParamEntity, String> nameCol = new TableColumn<>("OID");
					nameCol.setPrefWidth(350);
					nameCol.setCellFactory(centeredCellFactory());
					nameCol.setCellValueFactory(new PropertyValueFactory<ParamEntity, String>("oid"));

					final TableColumn<ParamEntity, String> valueCol = new TableColumn<>(
							LocalUtility.getString("message.value"));
					valueCol.setPrefWidth(350);
					valueCol.setCellFactory(centeredCellFactory());
					valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
					listView.getColumns().addAll(nameCol, valueCol);

					final TableViewSelectionModel<ParamEntity> selectionModel = listView.getSelectionModel();
					selectionModel.setSelectionMode(SelectionMode.SINGLE);
					listView.setRowFactory(tv -> {
						final TableRow<ParamEntity> row = new TableRow<>();
						row.setOnMouseClicked(ev -> {
							if (!row.isEmpty()) {
								field.setText(listView.getSelectionModel().getSelectedItem().getOid());
								field1.setText(listView.getSelectionModel().getSelectedItem().getValue());
							}
						});
						return row;
					});

					fieldF.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(person -> {
							// If filter text is empty, display all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							final String lowerCaseFilter = newValue.toLowerCase();

							if (person.getOid().toLowerCase().contains(lowerCaseFilter)) {
								return true; // Filter matches first name.
							}
							return false; // Does not match.
						});
					});

					field1F.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredData.setPredicate(person -> {
							// If filter text is empty, display all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							final String lowerCaseFilter = newValue.toLowerCase();

							if (person.getValue().toLowerCase().contains(lowerCaseFilter)) {
								return true; // Filter matches first name.
							}
							return false; // Does not match.
						});
					});

					final SortedList<ParamEntity> sortedData = new SortedList<>(filteredData);
					sortedData.comparatorProperty().bind(listView.comparatorProperty());
					listView.setItems(sortedData);

					final Region spacer12 = new Region();
					spacer12.setMaxHeight(30);
					VBox.setVgrow(spacer12, Priority.ALWAYS);

					final Region spacer13 = new Region();
					spacer13.setMaxHeight(50);
					VBox.setVgrow(spacer13, Priority.ALWAYS);

					final HBox hbox = new HBox();
					hbox.getChildren().addAll(spacer, buttonRemove, button);

					final HBox hbBoxx = new HBox();
					hbBoxx.getChildren().addAll(fieldF, btn);
					fieldF.setPrefWidth(900);

					btn.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								fieldF.clear();
							});
						}
					});

					btn1.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								field1F.clear();
							});
						}
					});

					final HBox hbBoxx1 = new HBox();
					hbBoxx1.getChildren().addAll(field1F, btn1);
					field1F.setPrefWidth(900);

					final HBox hbBoxF = new HBox();
					hbBoxF.getChildren().addAll(field, btnF);
					field.setPrefWidth(900);

					btnF.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								field.clear();
							});
						}
					});

					final HBox hbBoxF1 = new HBox();
					hbBoxF1.getChildren().addAll(field1, btnF1);
					field1.setPrefWidth(900);

					btnF1.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								field1.clear();
							});
						}
					});

					vBox.getChildren().addAll(labelApi, hbBoxF, labelApi1, hbBoxF1, listView, labelApiF, hbBoxx,
							labelApi1F, hbBoxx1, spacer12, labelP, spacer1, hbox, spacer13);

					vBox.setPadding(new Insets(20, 10, 0, 10));

					vBox.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

					final Stage fourStage = new Stage();
					// Creating a scene object
					final Scene scene = new Scene(vBox, 900, 900);
					scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					scene.setFill(Color.LIGHTGRAY);
					// Setting title to the Stage
					fourStage.titleProperty().bind(LocalUtility.createStringBinding("message.oid.mapping"));
					// Adding scene to the stage
					fourStage.setScene(scene);
					fourStage.setMaximized(false);
					fourStage.show();
					button.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (!field.getText().isEmpty() && !field1.getText().isEmpty()) {
									final Alert alertC = new Alert(AlertType.CONFIRMATION);
									alertC.setTitle("Confirmation");
									alertC.setHeaderText(null);
									alertC.setContentText(LocalUtility.getString("message.confirm.dialog"));
									final DialogPane dialogPaneC = alertC.getDialogPane();
									dialogPaneC.getStylesheets()
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									dialogPaneC.getStyleClass().add(Constant.DIALOG);
									final Optional<ButtonType> result = alertC.showAndWait();
									if (result.get() == ButtonType.OK) {
										final boolean isOk = ParametrageService.writeInPropFile(field.getText(),
												field1.getText());
										if (isOk) {
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString(SUCCES));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
											final List<ParamEntity> map = ParametrageService.readOidInPropFile();
											final ObservableList<ParamEntity> observableMap = FXCollections
													.observableList(map);
											listView.setItems(observableMap);
											field.setText("");
											field1.setText("");
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Utility.getStylesheets(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Utility.getStyleClass(dialogPane);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString("message.error.update.oid"));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									}
								} else {
									final Alert alert = new Alert(AlertType.WARNING);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.data.oid.gazelle"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}
							});
						}
					});

					buttonRemove.setOnAction(new EventHandler<>() {
						@Override
						public void handle(final ActionEvent event) {
							Platform.runLater(() -> {
								if (listView.getSelectionModel().getSelectedItem() != null) {
									final Alert alertC = new Alert(AlertType.CONFIRMATION);
									alertC.setTitle("Confirmation");
									alertC.setHeaderText(null);
									alertC.setContentText(LocalUtility.getString("message.confirm.dialog"));
									final DialogPane dialogPaneC = alertC.getDialogPane();
									dialogPaneC.getStylesheets()
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									dialogPaneC.getStyleClass().add(Constant.DIALOG);
									final Optional<ButtonType> result = alertC.showAndWait();
									if (result.get() == ButtonType.OK) {
										ParametrageService
												.removePro(listView.getSelectionModel().getSelectedItem().getOid());
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										Utility.getStylesheets(dialogPane)
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										Utility.getStyleClass(dialogPane);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText(LocalUtility.getString(SUCCES));
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
										final List<ParamEntity> map = ParametrageService.readOidInPropFile();
										final ObservableList<ParamEntity> observableMap = FXCollections
												.observableList(map);
										listView.setItems(observableMap);
										field.setText("");
										field1.setText("");
									}
								} else {
									final Alert alert = new Alert(AlertType.WARNING);
									final DialogPane dialogPane = alert.getDialogPane();
									Utility.getStylesheets(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString("message.delete.oid.ok"));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									alert.showAndWait();
								}
							});
						}
					});
				});
			}
		});

		final Region spacer5 = new Region();
		spacer5.setMaxWidth(10);
		HBox.setHgrow(spacer5, Priority.ALWAYS);

		final Region spacer6 = new Region();
		spacer6.setMaxWidth(10);
		HBox.setHgrow(spacer6, Priority.ALWAYS);

		final Region spacer7 = new Region();
		spacer7.setMaxWidth(10);
		HBox.setHgrow(spacer7, Priority.ALWAYS);

		final Region spacer8 = new Region();
		spacer8.setMaxWidth(10);
		HBox.setHgrow(spacer8, Priority.ALWAYS);

		final Region spacer9 = new Region();
		spacer9.setMaxWidth(10);
		HBox.setHgrow(spacer9, Priority.ALWAYS);

		final VBox vBoxAll = new VBox(5);
		vBoxAll.setPadding(new Insets(0, 0, 0, 40));
		vBoxAll.getChildren().addAll(hbox1, hbox2);

		final SplitPane splitPane = new SplitPane();
		splitPane.setStyle(BORDERSTYLE);
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.1f, 0.9f);
		Utility.getItemsSplit(splitPane).addAll(hBoxImg, vBoxAll);

		final VBox splitPaneVV = new VBox();
		splitPaneVV.setStyle(BORDERSTYLE);
		Utility.getChildren(splitPaneVV).addAll(vbAcceuil, splitPane);

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setContent(browserEngine);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		final BorderPane borderPane = new BorderPane();
		borderPane.setCenter(textAreaConsole);
		borderPane.setPrefHeight(Integer.MAX_VALUE);

		final SplitPane splitPaneV = new SplitPane();
		splitPaneV.setStyle(BORDERSTYLE);
		splitPaneV.setOrientation(Orientation.HORIZONTAL);
		splitPaneV.setDividerPositions(0.5f, 0.5f);
		Utility.getItemsSplit(splitPaneV).addAll(borderPane, scrollPane);

		vBox.getChildren().addAll(splitPaneVV, splitPaneV);

		final VBox hBox2 = new VBox();
		hBox2.setMinSize(vBox.getPrefWidth(), vBox.getPrefHeight());
		final ObservableList<Node> listHB = Utility.getChildrenNode(hBox2);
		listHB.addAll(vBox);
		hBox2.setStyle("-fx-background-color: #F5F5F5;");

		final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		final double scale = screenBounds.getWidth() / 1920;
		stage.setWidth(stage.getWidth() * scale);
		stage.setHeight(stage.getHeight() * scale);

		final Scene scene = new Scene(hBox2);
		scene.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
		scene.setFill(Color.LIGHTGRAY);
		stage.setTitle("InteropStudio 2024");
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();

		browserEngine.requestFocus();
		browserEngine.setOnMouseEntered(event -> handleMouseEntered(event));
		browserEngine.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				final String urlLocation = webEngine.getLocation();
				final String filePath = urlLocation.substring(5);
				final File file = new File(filePath);
				if (file.getName().equals(Constant.LASTREPORT)) {
					handleDoubleClick(file);
				}
			}
		});

		btn.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					textFieldCda.clear();
					textFieldMeta.clear();
					final String url = WebViewSample.class.getClassLoader().getResource(Constant.INTEROPFILE)
							.toExternalForm();
					webEngine.load(url);
					final Locale local = LocalUtility.getLocale();
					if (local.equals(Locale.FRENCH)) {
						textAreaConsole.setText(Constant.BIENVENUEFR);
					} else if (local.equals(Locale.ENGLISH)) {
						textAreaConsole.setText(Constant.BIENVENUEEN);
					} else {
						textAreaConsole.setText(Constant.BIENVENIDOS);
					}
					textAreaConsole.setStyle(Constant.STYLE83);
					paramcItem.setGraphic(view57);
					final List<Window> windows = Stage.getWindows();
					final Iterator<Window> iter = windows.iterator();
					final List<Stage> stages = new ArrayList<>();
					while (iter.hasNext()) {
						final Window window = iter.next();
						if (!window.getScene().equals(scene)) {
							final Stage stage = (Stage) window;
							stages.add(stage);
						}
					}
					for (final Stage stage : stages) {
						stage.close();
					}
				});
			}
		});

		stage.setOnCloseRequest(new EventHandler<>() {
			@Override
			public void handle(final WindowEvent event) {
				final File file = new File(Constant.INTEROPFOLDER + "\\7z.exe");
				if (file.exists()) {
					file.delete();
				}
				final File dll = new File(Constant.INTEROPFOLDER + "\\7z.dll");
				if (dll.exists()) {
					dll.delete();
				}
				stage.close();
				Platform.exit();
			}
		});

	}

	/**
	 * saveTextToFile
	 * 
	 * @param content
	 * @param file
	 */
	private void saveTextToFile(final String content, final File file) {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println(content);
			writer.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * constructAccordion
	 */
	private void constructAccordion() {
		try {
			accordion.setExpandedPane(null);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			final double width = screenSize.getWidth();
			accordion.setMaxWidth(width - 100);
			final File myFile = Constant.INTEROPINIFILE;
			final Path fileName = Path.of(myFile.getAbsolutePath());
			Files.writeString(fileName, area.getText());
			final String fileContent = Files.readString(fileName);
			area.clear();
			area.setText(fileContent);
			final Map<String, Map<String, String>> iniFileContents = new ConcurrentHashMap<>();
			final INIConfiguration iniConfiguration = new INIConfiguration();
			try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(myFile.toURI()))) {
				iniConfiguration.read(fileReader);
			} catch (final ConfigurationException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}

			for (final String section : iniConfiguration.getSections()) {
				final Map<String, String> subSectionMap = new ConcurrentHashMap<>();
				final SubnodeConfiguration confSection = iniConfiguration.getSection(section);
				final Iterator<String> keyIterator = confSection.getKeys();
				while (keyIterator.hasNext()) {
					final String key = keyIterator.next();
					final String value = confSection.getProperty(key).toString();
					subSectionMap.put(key, value);
				}
				iniFileContents.put(section, subSectionMap);
			}

			accordion.getPanes().clear();
			for (final Entry<String, Map<String, String>> entry : iniFileContents.entrySet()) {
				final String key = entry.getKey();
				final Map<String, String> value = entry.getValue();
				final TitledPane newPane = new TitledPane();
				newPane.setExpanded(false);
				newPane.setText(key);
				final VBox vbMap = new VBox();
				for (final Entry<String, String> entryValue : value.entrySet()) {
					final String keyV = entryValue.getKey();
					final String valueV = entryValue.getValue();
					final Label label = new Label(keyV);
					final HBox hbox = new HBox();
					hbox.getChildren().add(label);
					final TextField field = new TextField();
					field.setOnMouseEntered(e -> field.setEffect(shadow));
					field.setOnMouseExited(e -> field.setEffect(null));
					label.setPadding(new Insets(5, 5, 5, 5));
					label.setStyle(Constant.STYLE51);
					field.setPadding(new Insets(5, 10, 5, 0));
					field.setStyle(Constant.STYLE81);
					field.setText(valueV);
					field.setPrefWidth(1500);
					final SplitPane hbMap = new SplitPane();
					hbMap.getItems().addAll(hbox, field);
					hbMap.setDividerPositions(0.2f, 0.8f);
					vbMap.getChildren().addAll(hbMap);
				}
				newPane.setContent(vbMap);
				newPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
				accordion.getPanes().add(newPane);
				accordion.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
			}
			paneArea.setPadding(new Insets(20, 0, 20, 0));
			accordion.getPanes().add(paneArea);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * Afficher la documentation de l'application
	 * 
	 * @param pathFile
	 * @param search
	 * @return
	 */
	public List<String> searchStandard(final String pathFile, final String search) {
		List<String> wholeData;
		final List<String> found = new ArrayList<>();
		try {
			wholeData = Files.readAllLines(Path.of(pathFile));
			for (final String wholeDatum : wholeData) {
				if (wholeDatum.contains(search))
					found.add(wholeDatum);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return found;
	}

	/**
	 * createTablePath
	 * 
	 * @param login
	 * @param pwd
	 * @throws SQLException
	 */
	public static void createTablePath() throws SQLException {
		try (Connection connection = DriverManager.getConnection(Constant.JDBCURL, Constant.USERNAME,
				Constant.PASSWORD)) {
			final String sql = "Create table IF NOT EXISTS `path` (ID int primary key, name varchar(50), filepath varchar(500))";
			try (Statement statement = connection.createStatement()) {
				statement.execute(sql);
				connection.close();
			}
		}
	}

	/**
	 * update
	 * 
	 * @param name
	 * @param filepath
	 * @throws SQLException
	 */
	public static boolean update(final String name, final String filepath) throws SQLException {
		boolean isOk;
		try (Connection conn = DriverManager.getConnection(Constant.JDBCURL, Constant.USERNAME, Constant.PASSWORD)) {
			final String sqlSelect = "SELECT * FROM `path` where name = " + "'" + name + "'";
			try (Statement statement = conn.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(sqlSelect)) {
					if (resultSet.next()) {
						final String sql = "UPDATE `path` SET filepath = " + "'" + filepath + "'" + " WHERE name = "
								+ "'" + name + "'";
						try (PreparedStatement stmt = conn.prepareStatement(sql)) {
							stmt.executeUpdate();
							isOk = true;
						}
					} else {
						int rowCount;
						try (ResultSet rset = statement.executeQuery("SELECT * FROM `path`")) {
							rset.last();
							rowCount = rset.getRow();
						}
						final String sql = "Insert into `path` (ID, name, filepath) VALUES (?, ?, ?)";
						try (PreparedStatement stmt = conn.prepareStatement(sql)) {
							stmt.setInt(1, rowCount + 1);
							stmt.setString(2, name);
							stmt.setString(3, filepath);
							stmt.executeUpdate();
							isOk = true;
						}
					}
				}
			}
		}
		return isOk;
	}

	/**
	 * selectPath
	 * 
	 * @param login
	 * @param pwd
	 * @throws SQLException
	 */
	public static String selectPath(final String name) throws SQLException {
		final String nul = null;
		String exist = null;
		try {
			try (Connection conn = DriverManager.getConnection(Constant.JDBCURL, Constant.USERNAME,
					Constant.PASSWORD)) {
				final String sqlSelect = "SELECT * FROM `path` where name = " + "'" + name + "'";
				try (Statement statement = conn.createStatement()) {
					try (ResultSet resultSet = statement.executeQuery(sqlSelect)) {
						if (resultSet.next()) {
							exist = resultSet.getString(3);
						}
					}
				}
			}
		} catch (final SQLException e) {
			exist = nul;
		}
		return exist;
	}

	/**
	 * startPane
	 * 
	 * @param stage
	 */
	public void startPane(final Stage stage) {
		final JFXButton buttonViewer = LocalUtility.buttonForKeyMFX("message.viewer.cda");
		final JFXButton buttonConverter = LocalUtility.buttonForKeyMFX("message.convert.jdv");
		final JFXButton buttonGUI = LocalUtility.buttonForKeyMFX("message.validateur.cda");
		final TextField pathField1 = new TextField("");
		final TextField pathField2 = new TextField("");
		final TextField pathField3 = new TextField("");
		final FileChooser fileChooser = new FileChooser();
		final FileChooser fileChooser2 = new FileChooser();
		final FileChooser fileChooser3 = new FileChooser();
		final Stage secondStage = new Stage();
		final VBox finalVbox = new VBox();
		// Start ProgressBar creation
		final double wndwWidth = 150.0d;
		final double wndhHeigth = 150.0d;
		final ProgressIndicator progress = new ProgressIndicator();
		progress.setMinWidth(wndwWidth);
		progress.setMinHeight(wndhHeigth);
		progress.setProgress(0.25F);

		final VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10));
		updatePane.setSpacing(5.0d);
		updatePane.setAlignment(Pos.CENTER);
		Inutility2.getChildrenNode(updatePane).addAll(progress);
		updatePane.setStyle(FONTIME2);

		final Stage taskUpdateStage = new Stage(StageStyle.UNDECORATED);
		taskUpdateStage.setScene(new Scene(updatePane, 170, 170));

		final JFXButton pathButton = LocalUtility.buttonForKeyMFX("message.add.path.api");
		final ImageView viewP = new ImageView(Constant.PPHOTO);
		pathButton.setGraphic(viewP);
		pathButton.setStyle(Constant.STYLE1);

		pathField1.setDisable(true);
		pathField2.setDisable(true);
		pathField3.setDisable(true);

		// setOnAction pathButton
		pathButton.setOnAction(event -> {
			try {
				String exist1;
				String exist2;
				String exist3;
				try {
					exist1 = selectPath(Constant.TCCGUIPATH);
					exist2 = selectPath(Constant.VIEWERPATH);
					exist3 = selectPath(Constant.CONVERTJDVPATH);
					pathField1.setText(exist2);
					pathField2.setText(exist3);
					pathField3.setText(exist1);
				} catch (final SQLException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}

				createTablePath();
				final VBox hboxV = new VBox();
				final Label pathLabel1 = LocalUtility.labelForValue(() -> LocalUtility.get("message.viewer.cda.path"));
				pathLabel1.setStyle(Constant.STYLE13);
				pathLabel1.setPadding(new Insets(5, 0, 0, 0));
				pathField1.setStyle(Constant.STYLE12);
				pathField1.setPrefWidth(600);
				pathField1.setPrefHeight(40);
				Inutility2.newExtFilter(fileChooser).add(new FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar"));
				final ImageView view = new ImageView(Constant.OPENPHOTO);
				final JFXButton button1 = new JFXButton("");
				button1.setGraphic(view);
				button1.setStyle(Constant.STYLE1);
				button1.setPrefSize(40, 40);
				button1.setMinSize(40, 40);
				button1.setMaxSize(40, 40);

				final Label pathLabel2 = LocalUtility.labelForValue(() -> LocalUtility.get("message.convert.jdv.path"));
				pathLabel2.setStyle(Constant.STYLE13);
				pathLabel2.setPadding(new Insets(5, 0, 0, 0));
				pathField2.setStyle(Constant.STYLE12);
				pathField2.setPrefWidth(600);
				pathField2.setPrefHeight(40);
				Inutility2.newExtFilter(fileChooser2)
						.add(new FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar"));
				final ImageView view2 = new ImageView(Constant.OPENPHOTO);
				final JFXButton button2 = new JFXButton("");
				button2.setGraphic(view2);
				button2.setStyle(Constant.STYLE1);
				button2.setPrefSize(40, 40);
				button2.setMinSize(40, 40);
				button2.setMaxSize(40, 40);

				final Label pathLabel3 = LocalUtility.labelForValue(() -> LocalUtility.get("message.val.gui.path"));
				pathLabel3.setStyle(Constant.STYLE13);
				pathLabel3.setPadding(new Insets(5, 0, 0, 0));
				pathField3.setStyle(Constant.STYLE12);
				pathField3.setPrefWidth(600);
				pathField3.setPrefHeight(40);
				Inutility2.newExtFilter(fileChooser3)
						.add(new FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar"));
				final ImageView view3 = new ImageView(Constant.OPENPHOTO);
				final JFXButton button3 = new JFXButton("");
				button3.setGraphic(view3);
				button3.setStyle(Constant.STYLE1);
				button3.setPrefSize(40, 40);
				button3.setMinSize(40, 40);
				button3.setMaxSize(40, 40);

				button1.setOnAction(new EventHandler<>() {
					@Override
					public void handle(ActionEvent event) {
						Platform.runLater(() -> {
							final File file = fileChooser.showOpenDialog(secondStage);
							if (file != null) {
								pathField1.setText(file.getAbsolutePath());
							}
						});
					}
				});

				button2.setOnAction(new EventHandler<>() {
					@Override
					public void handle(ActionEvent event) {
						Platform.runLater(() -> {
							final File file = fileChooser2.showOpenDialog(secondStage);
							if (file != null) {
								pathField2.setText(file.getAbsolutePath());
							}
						});
					}
				});

				button3.setOnAction(new EventHandler<>() {
					@Override
					public void handle(ActionEvent event) {
						Platform.runLater(() -> {
							final File file = fileChooser3.showOpenDialog(secondStage);
							if (file != null) {
								pathField3.setText(file.getAbsolutePath());
							}
						});
					}
				});

				final HBox hb1 = new HBox();
				hb1.getChildren().addAll(pathField1, button1);
				final HBox hb2 = new HBox();
				hb2.getChildren().addAll(pathField2, button2);
				final HBox hb3 = new HBox();
				hb3.getChildren().addAll(pathField3, button3);

				final JFXButton submit = LocalUtility.buttonForKeyMFX("message.soumettre");
				submit.setStyle(Constant.STYLE1);

				final JFXButton cancel = LocalUtility.buttonForKeyMFX("message.reinitialiser");
				cancel.setStyle(Constant.STYLE1);

				final Region spacer11 = new Region();
				spacer11.setMaxWidth(10);
				HBox.setHgrow(spacer11, Priority.ALWAYS);

				final Region spacer12 = new Region();
				spacer12.setMaxWidth(10);
				HBox.setHgrow(spacer12, Priority.ALWAYS);

				final Region spacer13 = new Region();
				spacer13.setMaxWidth(10);
				HBox.setHgrow(spacer13, Priority.ALWAYS);

				final HBox hb4 = new HBox();
				hb4.getChildren().addAll(submit, spacer11, cancel);
				hb4.setPadding(new Insets(20, 0, 0, 0));
				hb4.setAlignment(Pos.CENTER);

				hboxV.getChildren().addAll(pathLabel1, hb1, pathLabel2, hb2, pathLabel3, hb3, spacer13, hb4);
				hboxV.setPadding(new Insets(10, 10, 0, 10));

				final Scene scene = new Scene(hboxV, 700, 350);
				Inutility2.getStyleScene(scene).add(WebViewSample.class.getResource(Constant.CSS).toExternalForm());
				secondStage.setTitle(LocalUtility.getString("message.add.path.api"));
				secondStage.setScene(scene);
				secondStage.setMaximized(false);
				secondStage.setResizable(false);
				secondStage.show();
				finalVbox.setDisable(true);

				// setOnAction cancel
				cancel.setOnAction(new EventHandler<>() {
					@Override
					public void handle(ActionEvent event) {
						Platform.runLater(() -> {
							pathField1.setText("");
							pathField2.setText("");
							pathField3.setText("");
						});
					}
				});

				// setOnAction submit
				submit.setOnAction(new EventHandler<>() {
					@Override
					public void handle(ActionEvent event) {
						Platform.runLater(() -> {
							try {
								boolean isOk = false;
								boolean isOk1 = false;
								boolean isOk2 = false;
								boolean isOk3 = false;
								createTablePath();
								if (pathField1 != null) {
									if (pathField1.getText() != null && !pathField1.getText().isEmpty()) {
										isOk = update(Constant.VIEWERPATH, pathField1.getText());
									}
								}
								if (pathField2 != null) {
									if (pathField2.getText() != null && !pathField2.getText().isEmpty()) {
										isOk1 = update(Constant.CONVERTJDVPATH, pathField2.getText());
									}
								}
								if (pathField3 != null) {
									if (pathField3.getText() != null && !pathField3.getText().isEmpty()) {
										isOk2 = update(Constant.TCCGUIPATH, pathField3.getText());
									}
								}

								if (isOk) {
									buttonViewer.setDisable(false);
								}
								if (isOk1) {
									buttonConverter.setDisable(false);
								}
								if (isOk2) {
									buttonGUI.setDisable(false);
								}
								if (isOk || isOk1 || isOk2 || isOk3) {
									final Alert alert = new Alert(AlertType.INFORMATION);
									final DialogPane dialogPane = alert.getDialogPane();
									Inutility2.getStyleDialog(dialogPane)
											.add(getClass().getResource(Constant.CSS).toExternalForm());
									Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
									dialogPane.setMinHeight(130);
									dialogPane.setMaxHeight(130);
									dialogPane.setPrefHeight(130);
									alert.setContentText(LocalUtility.getString(SUCCES));
									alert.setHeaderText(null);
									alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
									final Optional<ButtonType> result = alert.showAndWait();
									if (result.get().equals(ButtonType.OK)) {
										secondStage.close();
										finalVbox.setDisable(false);
									}
								}
							} catch (final SQLException e) {
								final Alert alert = new Alert(AlertType.ERROR);
								final DialogPane dialogPane = alert.getDialogPane();
								Inutility2.getStyleDialog(dialogPane)
										.add(getClass().getResource(Constant.CSS).toExternalForm());
								Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
								dialogPane.setMinHeight(130);
								dialogPane.setMaxHeight(130);
								dialogPane.setPrefHeight(130);
								alert.setContentText(LocalUtility.getString("message.close.popup"));
								alert.setHeaderText(null);
								alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
								alert.showAndWait();
							}
						});
					}
				});

			} catch (final SQLException e) {
				final Alert alert = new Alert(AlertType.ERROR);
				final DialogPane dialogPane = alert.getDialogPane();
				Inutility2.getStyleDialog(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
				Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
				dialogPane.setMinHeight(130);
				dialogPane.setMaxHeight(130);
				dialogPane.setPrefHeight(130);
				alert.setContentText(LocalUtility.getString("message.close.popup"));
				alert.setHeaderText(null);
				alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
				alert.showAndWait();
			}
		});
		// Set an action for the "Login" button to validate the credentials.
		final HBox rootHbox = new HBox(30);
		rootHbox.setAlignment(Pos.CENTER);
		buttonViewer.setStyle(Constant.STYLE1);
		buttonConverter.setStyle(Constant.STYLE1);
		buttonGUI.setStyle(Constant.STYLE1);
		rootHbox.getChildren().addAll(buttonViewer, buttonConverter, buttonGUI);

		final HBox hboxB = new HBox();
		hboxB.setAlignment(Pos.CENTER);
		hboxB.setPadding(new Insets(20, 0, 0, 0));
		hboxB.getChildren().addAll(pathButton);

		final Region spacer2 = new Region();
		spacer2.setMaxHeight(20);
		VBox.setVgrow(spacer2, Priority.ALWAYS);

		finalVbox.getChildren().addAll(hboxB, spacer2, rootHbox);

		buttonViewer.setMinSize(rootHbox.getPrefWidth(), rootHbox.getPrefHeight());
		buttonConverter.setMinSize(rootHbox.getPrefWidth(), rootHbox.getPrefHeight());
		buttonGUI.setMinSize(rootHbox.getPrefWidth(), rootHbox.getPrefHeight());

		try {
			final String exist11 = selectPath(Constant.TCCGUIPATH);
			final String exist21 = selectPath(Constant.VIEWERPATH);
			final String exist31 = selectPath(Constant.CONVERTJDVPATH);
			if (exist11 == null || exist11.isEmpty()) {
				buttonGUI.setDisable(true);
			} else {
				buttonGUI.setDisable(false);
			}
			if (exist21 == null || exist21.isEmpty()) {
				buttonViewer.setDisable(true);
			} else {
				buttonViewer.setDisable(false);
			}
			if (exist31 == null || exist31.isEmpty()) {
				buttonConverter.setDisable(true);
			} else {
				buttonConverter.setDisable(false);
			}
		} catch (final SQLException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

		final ImageView viewV = new ImageView(Constant.VPHOTO);
		buttonViewer.setGraphic(viewV);

		final ImageView viewC = new ImageView(Constant.CPHOTO);
		buttonConverter.setGraphic(viewC);

		final ImageView viewG = new ImageView(Constant.GPHOTO);
		buttonGUI.setGraphic(viewG);

		// setOnAction buttonGUI
		buttonGUI.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					String exist = null;
					try {
						exist = selectPath(Constant.TCCGUIPATH);
						filePath = exist;
					} catch (final SQLException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
					final File existFile = new File(exist);
					if (exist != null && existFile.exists()) {
						String cdaPath = "";
						if (!textFieldCda.getText().isEmpty()) {
							cdaPath = textFieldCda.getText();
						}
						try {
							final String command[] = { "java", "-jar", exist, cdaPath, "fr" };
							final ProcessBuilder pbuilder = new ProcessBuilder(command);
							pbuilder.redirectErrorStream(true);
							final Process process = pbuilder.start();
							final InputStream istream = process.getInputStream();
							// thread to handle or gobble text sent from input stream
							new Thread(() -> {
								// try with resources
								try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
									String line;
									while ((line = reader.readLine()) != null) {
										LOG.info(line);
									}
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							}).start();
							// thread to get exit value from process without blocking
							final Thread waitForThread = new Thread(() -> {
								try {
									process.waitFor();
								} catch (final InterruptedException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							});
							waitForThread.start();
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.verif.tcc"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		// setOnAction buttonViewer
		buttonViewer.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					String exist = null;
					try {
						exist = selectPath(Constant.VIEWERPATH);
						filePath = exist;
					} catch (final SQLException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
					final File existFile = new File(exist);
					if (exist != null && existFile.exists()) {
						try {
							final String command[] = { Constant.JAVA, Constant.JARF, exist };
							final ProcessBuilder pbuilder = new ProcessBuilder(command);
							pbuilder.redirectErrorStream(true);
							final Process process = pbuilder.start();
							final InputStream istream = process.getInputStream();
							// thread to handle or gobble text sent from input stream
							new Thread(() -> {
								// try with resources
								try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
									String line;
									while ((line = reader.readLine()) != null) {
										LOG.info(line);
									}
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							}).start();
							// thread to get exit value from process without blocking
							final Thread waitForThread = new Thread(() -> {
								try {
									process.waitFor();
								} catch (final InterruptedException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							});
							waitForThread.start();
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.verif.viewer"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		// setOnAction buttonConverter
		buttonConverter.setOnAction(new EventHandler<>() {

			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					String exist = null;
					try {
						exist = selectPath(Constant.CONVERTJDVPATH);
						filePath = exist;
					} catch (final SQLException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
					final File existFile = new File(exist);
					if (exist != null && existFile.exists()) {
						try {
							final String command[] = { Constant.JAVA, Constant.JARF, exist };
							final ProcessBuilder pbuilder = new ProcessBuilder(command);
							pbuilder.redirectErrorStream(true);
							final Process process = pbuilder.start();
							final InputStream istream = process.getInputStream();
							// thread to handle or gobble text sent from input stream
							new Thread(() -> {
								// try with resources
								try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
									String line;
									while ((line = reader.readLine()) != null) {
										if (reader.readLine() == null) {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											Inutility2.getStyleDialog(dialogPane)
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText(LocalUtility.getString("message.verif.conv"));
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
										LOG.info(line);
									}
								} catch (final IOException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							}).start();
							// thread to get exit value from process without blocking
							final Thread waitForThread = new Thread(() -> {
								try {
									process.waitFor();
								} catch (final InterruptedException e) {
									if (LOG.isInfoEnabled()) {
										final String error = e.getMessage();
										LOG.error(error);
									}
								}
							});
							waitForThread.start();
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
							}
						}
					} else {
						final Alert alert = new Alert(AlertType.ERROR);
						final DialogPane dialogPane = alert.getDialogPane();
						Inutility2.getStyleDialog(dialogPane)
								.add(getClass().getResource(Constant.CSS).toExternalForm());
						Inutility2.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString("message.verif.conv"));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait();
					}
				});
			}
		});

		final Scene scene = new Scene(finalVbox, 550, 150);
		Inutility2.getStyleScene(scene).add(WebViewSample.class.getResource(Constant.CSS).toExternalForm());
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<>() {

			@Override
			public void handle(WindowEvent event) {
				if (secondStage != null && secondStage.isShowing()) {
					secondStage.close();
				}
			}
		});

		secondStage.setOnCloseRequest(new EventHandler<>() {
			@Override
			public void handle(WindowEvent event) {
				finalVbox.setDisable(false);
			}
		});
	}

	/**
	 * handleDoubleClick
	 * 
	 * @param file
	 */
	private void handleDoubleClick(final File file) {
		if (file != null && Desktop.isDesktopSupported()) {
			final Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
	}

	/**
	 * handleMouseEntered
	 * 
	 * @param event
	 */
	private void handleMouseEntered(final MouseEvent event) {
		final String urlLocation = webEngine.getLocation();
		final String filePath = urlLocation.substring(5);
		final File file = new File(filePath);
		final Tooltip tooltipEngine = LocalUtility.createBoundTooltip("message.click.me");
		if (file.getName().equals(Constant.LASTREPORT)) {
			Tooltip.install(browserEngine, tooltipEngine);
		} else {
			Tooltip.uninstall(browserEngine, tooltipEngine);
		}
	}

	/**
	 * centeredCellFactory
	 * 
	 * @return
	 */
	private Callback<TableColumn<ParamEntity, String>, TableCell<ParamEntity, String>> centeredCellFactory() {
		return column -> new TableCell<ParamEntity, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(item);
					setAlignment(Pos.CENTER);
				}
			}
		};
	}

	/**
	 * openModal
	 * 
	 * @param owner
	 */
	public static int openModal(final Stage owner, final String textFieldF, final String field,
			final CheckBox checkboxLimite, final TextField textFieldNum) {
		yesClicked.set(false);
		final Stage modalStage = new Stage();
		modalStage.initModality(Modality.WINDOW_MODAL);
		modalStage.initOwner(owner);
		final VBox modalContent = new VBox();
		modalContent.setStyle("-fx-padding: 20; -fx-background-color: #93f9b911;");
		final Label label = new Label(LocalUtility.getString("message.delete.json"));
		label.setStyle(Constant.STYLE13);
		label.setPadding(new Insets(5, 0, 0, 0));
		final HBox buttonContent = new HBox();
		buttonContent.setPadding(new Insets(15, 0, 0, 280));
		final Button noButton = LocalUtility.buttonForKey("message.no");
		final Button yesButton = LocalUtility.buttonForKey("message.oui");

		final Region spacer = new Region();
		spacer.setMaxWidth(10);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		yesButton.setStyle(Constant.STYLE1);
		noButton.setStyle(Constant.STYLE1);
		buttonContent.getChildren().addAll(yesButton, spacer, noButton);
		modalContent.getChildren().addAll(label, buttonContent);
		yesButton.setPrefSize(40, 20);
		noButton.setPrefSize(40, 20);
		final Scene modalScene = new Scene(modalContent, 430, 100);
		modalStage.setScene(modalScene);
		modalStage.setTitle(LocalUtility.getString("message.confirm"));
		modalStage.setOnHidden(event -> {
			runTask(taskUpdateStage, progress);
			Platform.runLater(() -> {
				modalStage.setOnShowing(ev -> {
					FhirUtilities.initializeLogFiles();
				});
				listConverted = new ArrayList<>();
				listSize = 0;
				if (!yesClicked.get()) {
					textAreaInput.clear();
					itemsFiltred.clear();
					filteredItems.clear();
					listViewItems.getItems().clear();
					listConverted = FhirUtilities.convertSvsJdv2Fhir(textFieldF, new File(field), checkboxLimite,
							textFieldNum); // ajouter la couleur pour chaque checkbox (rouge, orange,
					listSize = listConverted.size(); // vert)
					itemsFiltred = FXCollections.observableArrayList(listConverted);
					filteredItems = new FilteredList<>(itemsFiltred, s -> true);
					listViewItems.setItems(filteredItems);
					textAreaInput.setText(FhirUtilities.content.toString());
					listViewCq = new ListView<>();
					listViewCq.setItems(filteredItems);
					textAreaStat = new TextArea();
					textAreaStat.setText("\nNombre de jeux de valeurs convertis en Fhir: " + listSize
							+ "\nNombre de jeux de valeurs cochés: " + numberOfCheckedItems + "/" + listSize
							+ "\nNombre de CodeSystems utilisés par les Jdv: "
							+ FhirUtilities.distinctCodeSystemUsed.size() + "\nNombre de mappings Oid/Url réalisés:  "
							+ FhirUtilities.nbMappingDone + "\nNombre de mappings Oid/Url échoués:  "
							+ FhirUtilities.nbMappingFailed
							+ "\n\n ***** Liste des codes systèmes utilisés par les JDV *****\n\n");

					final StringBuilder batchBuilder = new StringBuilder();
					int count = 0;
					for (final String item : FhirUtilities.distinctCodeSystemUsed) {
						String uri = FhirUtilities.getUriFromOId(item);
						batchBuilder.append("\n").append(item).append(" --- ").append(uri).append("\n");
						// Append every 10 items to reduce UI updates
						if (++count % 10 == 0) {
							textAreaStat.appendText(batchBuilder.toString());
							batchBuilder.setLength(0); // Clear the builder
						}
					}
					// Append any remaining items
					if (batchBuilder.length() > 0) {
						textAreaStat.appendText(batchBuilder.toString());
					}

					textAreaStat.appendText("\\n\\n ***** Liste des codes utilisés par les JDV *****\\n");

					final StringBuilder batchBuildere = new StringBuilder();
					int counte = 0;
					for (final String item : FhirUtilities.distinctCodeUsed) {
						batchBuildere.append("\n").append(item).append("\n");
						// Append every 10 items to reduce UI updates
						if (++counte % 10 == 0) {
							textAreaStat.appendText(batchBuildere.toString());
							batchBuildere.setLength(0); // Clear the builder
						}
					}
					// Append any remaining items
					if (batchBuilder.length() > 0) {
						textAreaStat.appendText(batchBuildere.toString());
					}
					final Alert alert = new Alert(AlertType.INFORMATION);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane)
							.add(WebViewSample.class.getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(SUCCES));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();

				} else {
					textAreaInput.clear();
					itemsFiltred.clear();
					filteredItems.clear();
					listViewItems.getItems().clear();
					final File file = new File(textFieldF);
					final Path directoryToDelete = Paths.get(file.toURI());
					try {
						FhirUtilities.deleteContentsRecursively(directoryToDelete);
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.info(error);
						}
					}
					listConverted = FhirUtilities.convertSvsJdv2Fhir(textFieldF, new File(field), checkboxLimite,
							textFieldNum);
					listSize = listConverted.size();
					itemsFiltred = FXCollections.observableArrayList(listConverted);
					filteredItems = new FilteredList<>(itemsFiltred, s -> true);
					listViewItems.setItems(filteredItems);
					textAreaInput.setText(FhirUtilities.content.toString());
					listViewCq = new ListView<>();
					listViewCq.setItems(filteredItems);
					listViewCq = new ListView<>();
					listViewCq.setItems(filteredItems);
					textAreaStat = new TextArea();
					textAreaStat.setText("\nNombre de jeux de valeurs convertis en Fhir: " + listSize
							+ "\nNombre de jeux de valeurs cochés: " + numberOfCheckedItems + "/" + listSize
							+ "\nNombre de CodeSystems utilisés par les Jdv: "
							+ FhirUtilities.distinctCodeSystemUsed.size() + "\nNombre de mappings Oid/Url réalisés:  "
							+ FhirUtilities.nbMappingDone + "\nNombre de mappings Oid/Url échoués:  "
							+ FhirUtilities.nbMappingFailed
							+ "\n\n ***** Liste des codes systèmes utilisés par les JDV *****\n");

					final StringBuilder batchBuilder = new StringBuilder();
					int count = 0;
					for (final String item : FhirUtilities.distinctCodeSystemUsed) {
						String uri = FhirUtilities.getUriFromOId(item);
						batchBuilder.append("\n").append(item).append(" --- ").append(uri).append("\n");
						// Append every 10 items to reduce UI updates
						if (++count % 10 == 0) {
							textAreaStat.appendText(batchBuilder.toString());
							batchBuilder.setLength(0); // Clear the builder
						}
					}
					// Append any remaining items
					if (batchBuilder.length() > 0) {
						textAreaStat.appendText(batchBuilder.toString());
					}

					textAreaStat.appendText("\\n\\n ***** Liste des codes utilisés par les JDV *****\\n");

					final StringBuilder batchBuildere = new StringBuilder();
					int counte = 0;
					for (final String item : FhirUtilities.distinctCodeUsed) {
						batchBuildere.append("\n").append(item).append("\n");
						// Append every 10 items to reduce UI updates
						if (++counte % 10 == 0) {
							textAreaStat.appendText(batchBuildere.toString());
							batchBuildere.setLength(0); // Clear the builder
						}
					}
					// Append any remaining items
					if (batchBuilder.length() > 0) {
						textAreaStat.appendText(batchBuildere.toString());
					}

					final Alert alert = new Alert(AlertType.INFORMATION);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane)
							.add(WebViewSample.class.getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(SUCCES));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();

				}

			});
		});

		noButton.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				yesClicked.set(false);
				modalStage.close();
			}
		});

		yesButton.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				yesClicked.set(true);
				modalStage.close();
			}
		});

		modalStage.showAndWait();
		return listConverted.size();
	}

	/**
	 * controleUnitaire
	 * 
	 * @param pJdvContent
	 * @param pValeurRetour
	 * @param bMoveFile
	 * @return
	 */
	public StringBuilder controleUnitaire(final String pJdvContent, final StringBuilder pValeurRetour,
			final boolean bMoveFile, final double size, final File sJdvName) {
		final ValueSet vs = FhirUtilities.getValueSetFromJson(pJdvContent);
		final String sLoincOID = "2.16.840.1.113883.6.1";
		iErrorLevel = 0;
		sDestinationDirectory = "ALL-VALID";
		bAllValid = true;
		textAreaInput.setText("");
		if (vs != null) {
			final List<Map.Entry<ValueSet.ConceptSetComponent, ValueSet.ConceptReferenceComponent>> allCodesWithParent = vs
					.getCompose().getInclude().stream()
					.flatMap(include -> include.getConcept().stream().map(concept -> Map.entry(include, concept)))
					.collect(Collectors.toList());

			if (size > 100) {
				allCodesWithParent.parallelStream().forEach(entry -> {
					final ValueSet.ConceptSetComponent monComposantConcept = entry.getKey();
					final ValueSet.ConceptReferenceComponent monCode = entry.getValue();

					// Add debug statements to verify values
					final String codeSystem = monComposantConcept.getSystem();
					final String codeCode = monCode.getCode();
					final String codeDisplay = monCode.getDisplay();
					try {
						if (codeSystem.toUpperCase().contains(sLoincOID.toUpperCase())
								|| codeSystem.equals("https://interop.esante.gouv.fr/ig/nos/CodeSystem-TRE-A04-Loinc")
								|| codeSystem.equals("http://loinc.org")) {
							final boolean loincResponse = FhirUtilities.getLoincLookUpResult("http://loinc.org",
									codeCode, IniFile.read("LOINC-USER", "FHIR"),
									IniFile.read("LOINC-PASSWORD", "FHIR"));
							if (loincResponse) {
								pValeurRetour.append("\n(+) Code ").append(codeCode).append(" existant dans la LOINC.");
								sDestinationDirectory = "ALL-VALID";
							} else {
								pValeurRetour.append("\n(-){UNKNOWN-CODE} Code ").append(codeCode)
										.append(" inconnu dans la LOINC.");
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								bAllValid = false;
							}
						} else {
							final String validationResult = FhirUtilities.getOntoserverValidateCodeResults(codeSystem,
									codeCode, codeDisplay, vs.getName());
							pValeurRetour.append("\n").append(validationResult);
							if (validationResult.startsWith("(-){INCORRECT-DISPLAY}")) {
								if (iErrorLevel < 4) {
									iErrorLevel = 4;
									sDestinationDirectory = "INCORRECT-DISPLAY";
								}
								bAllValid = false;
							} else if (validationResult.startsWith("(-){UNKNOWN-CODE}")) {
								bAllValid = false;
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
							} else if (validationResult.startsWith("(-){UNKNOWN-SYSTEM}")) {
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "UNKNOWN-SYSTEM";
								}
							} else if (validationResult.startsWith("(-){NO-INTERPRETATION}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "NO-INTERPRETATION";
								}
							} else if (validationResult.startsWith("(-){UNKNOWN-ERROR}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 6) {
									iErrorLevel = 6;
									sDestinationDirectory = "VALIDATE-FAIL";
								}
							}
						}
					} catch (final Exception ex) {
						pValeurRetour.append(ex.getMessage()).append("\n").append(ex.toString()).append("\n")
								.append(ex.getStackTrace());
						if (iErrorLevel < 6) {
							iErrorLevel = 6;
							sDestinationDirectory = "VALIDATE-FAIL";
						}
					}

				});
			} else {
				allCodesWithParent.stream().forEach(entry -> {
					final ValueSet.ConceptSetComponent monComposantConcept = entry.getKey();
					final ValueSet.ConceptReferenceComponent monCode = entry.getValue();

					// Add debug statements to verify values
					final String codeSystem = monComposantConcept.getSystem();
					final String codeCode = monCode.getCode();
					final String codeDisplay = monCode.getDisplay();
					try {
						if (codeSystem.toUpperCase().contains(sLoincOID.toUpperCase())
								|| codeSystem.equals("https://interop.esante.gouv.fr/ig/nos/CodeSystem-TRE-A04-Loinc")
								|| codeSystem.equals("http://loinc.org")) {
							final boolean loincResponse = FhirUtilities.getLoincLookUpResult("http://loinc.org",
									codeCode, IniFile.read("LOINC-USER", "FHIR"),
									IniFile.read("LOINC-PASSWORD", "FHIR"));
							if (loincResponse) {
								pValeurRetour.append("\n(+) Code ").append(codeCode).append(" existant dans la LOINC.");
								sDestinationDirectory = "ALL-VALID";
							} else {
								pValeurRetour.append("\n(-){UNKNOWN-CODE} Code ").append(codeCode)
										.append(" inconnu dans la LOINC.");
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								bAllValid = false;
							}
						} else {
							final String validationResult = FhirUtilities.getOntoserverValidateCodeResults(codeSystem,
									codeCode, codeDisplay, vs.getName());
							pValeurRetour.append("\n").append(validationResult);
							if (validationResult.startsWith("(-){INCORRECT-DISPLAY}")) {
								if (iErrorLevel < 4) {
									iErrorLevel = 4;
									sDestinationDirectory = "INCORRECT-DISPLAY";
								}
								bAllValid = false;
							} else if (validationResult.startsWith("(-){UNKNOWN-CODE}")) {
								bAllValid = false;
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
							} else if (validationResult.startsWith("(-){UNKNOWN-SYSTEM}")) {
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "UNKNOWN-SYSTEM";
								}
							} else if (validationResult.startsWith("(-){NO-INTERPRETATION}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "NO-INTERPRETATION";
								}
							} else if (validationResult.startsWith("(-){UNKNOWN-ERROR}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 6) {
									iErrorLevel = 6;
									sDestinationDirectory = "VALIDATE-FAIL";
								}
							}
						}
					} catch (final Exception ex) {
						pValeurRetour.append(ex.getMessage()).append("\n").append(ex.toString()).append("\n")
								.append(ex.getStackTrace());
						if (iErrorLevel < 6) {
							iErrorLevel = 6;
							sDestinationDirectory = "VALIDATE-FAIL";
						}
					}
				});
			}

		} else {
			sDestinationDirectory = "VALIDATE-FAILED";
			bAllValid = false;
		}
		if (bAllValid) {
			sDestinationDirectory = "ALL-VALID";
			if (bMoveFile) {
				FhirUtilities.moveJdvFile(sJdvName.getAbsolutePath(), textFieldF.getText(), sDestinationDirectory);
			}
		} else {
			if (bMoveFile) {
				FhirUtilities.moveJdvFile(sJdvName.getAbsolutePath(), textFieldF.getText(), sDestinationDirectory);
			}
		}
		return pValeurRetour;
	}

	/**
	 * controleUnitaire
	 * 
	 * @param pJdvContent
	 * @param pValeurRetour
	 * @param bMoveFile
	 * @return
	 */
	public StringBuilder controleUnitaireS(final String pJdvContent, final StringBuilder pValeurRetour,
			final boolean bMoveFile, final double size, final File sJdvName) {
		final ValueSet vs = FhirUtilities.getValueSetFromJson(pJdvContent);
		final String sLoincOID = "2.16.840.1.113883.6.1";
		iErrorLevel = 0;
		sDestinationDirectory = "ALL-VALID";
		bAllValid = true;
		textAreaInput.setText("");
		if (vs != null) {
			final List<Map.Entry<ValueSet.ConceptSetComponent, ValueSet.ConceptReferenceComponent>> allCodesWithParent = vs
					.getCompose().getInclude().stream()
					.flatMap(include -> include.getConcept().stream().map(concept -> Map.entry(include, concept)))
					.collect(Collectors.toList());

			if (size > 100) {
				allCodesWithParent.parallelStream().forEach(entry -> {
					final ValueSet.ConceptSetComponent monComposantConcept = entry.getKey();
					final ValueSet.ConceptReferenceComponent monCode = entry.getValue();

					// Add debug statements to verify values
					final String codeSystem = monComposantConcept.getSystem();
					final String codeCode = monCode.getCode();
					final String codeDisplay = monCode.getDisplay();
					try {
						if (codeSystem.toUpperCase().contains(sLoincOID.toUpperCase())
								|| codeSystem.equals("https://interop.esante.gouv.fr/ig/nos/CodeSystem-TRE-A04-Loinc")
								|| codeSystem.equals("http://loinc.org")) {
							final boolean loincResponse = FhirUtilities.getLoincLookUpResult("http://loinc.org",
									codeCode, IniFile.read("LOINC-USER", "FHIR"),
									IniFile.read("LOINC-PASSWORD", "FHIR"));
							if (loincResponse) {
								pValeurRetour.append("\n(+) Code ").append(codeCode).append(" existant dans la LOINC.");
								sDestinationDirectory = "ALL-VALID";
							} else {
								pValeurRetour.append("\n(-){UNKNOWN-CODE} Code ").append(codeCode)
										.append(" inconnu dans la LOINC.");
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-CODE",
										"\n(-){UNKNOWN-CODE} Code " + codeCode + " inconnu dans la LOINC.");
								bAllValid = false;
							}
						} else {
							final String validationResult = FhirUtilities.getOntoserverValidateCodeResult(codeSystem,
									codeCode, codeDisplay, vs.getName());
							pValeurRetour.append("\n").append(validationResult);
							if (validationResult.startsWith("(-){INCORRECT-DISPLAY}")) {
								if (iErrorLevel < 4) {
									iErrorLevel = 4;
									sDestinationDirectory = "INCORRECT-DISPLAY";
								}
								Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.log"),
										(validationResult + "\n").getBytes(), StandardOpenOption.APPEND);
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\INCORRECT-DISPLAY", validationResult);
								bAllValid = false;
							} else if (validationResult.startsWith("(-){UNKNOWN-CODE}")) {
								bAllValid = false;
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-CODE", validationResult);
							} else if (validationResult.startsWith("(-){UNKNOWN-SYSTEM}")) {
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "UNKNOWN-SYSTEM";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-SYSTEM", validationResult);
							} else if (validationResult.startsWith("(-){NO-INTERPRETATION}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "NO-INTERPRETATION";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\NO-INTERPRETATION", validationResult);
							} else if (validationResult.startsWith("(-){UNKNOWN-ERROR}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 6) {
									iErrorLevel = 6;
									sDestinationDirectory = "VALIDATE-FAIL";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\VALIDATE-FAIL", validationResult);
							}
						}
					} catch (final Exception ex) {
						pValeurRetour.append(ex.getMessage()).append("\n").append(ex.toString()).append("\n")
								.append(ex.getStackTrace());
						if (iErrorLevel < 6) {
							iErrorLevel = 6;
							sDestinationDirectory = "VALIDATE-FAIL";
						}
						FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\VALIDATE-FAIL", ex.getMessage());
					}

				});
			} else {
				allCodesWithParent.stream().forEach(entry -> {
					final ValueSet.ConceptSetComponent monComposantConcept = entry.getKey();
					final ValueSet.ConceptReferenceComponent monCode = entry.getValue();

					// Add debug statements to verify values
					final String codeSystem = monComposantConcept.getSystem();
					final String codeCode = monCode.getCode();
					final String codeDisplay = monCode.getDisplay();
					try {
						if (codeSystem.toUpperCase().contains(sLoincOID.toUpperCase())
								|| codeSystem.equals("https://interop.esante.gouv.fr/ig/nos/CodeSystem-TRE-A04-Loinc")
								|| codeSystem.equals("http://loinc.org")) {
							final boolean loincResponse = FhirUtilities.getLoincLookUpResult("http://loinc.org",
									codeCode, IniFile.read("LOINC-USER", "FHIR"),
									IniFile.read("LOINC-PASSWORD", "FHIR"));
							if (loincResponse) {
								pValeurRetour.append("\n(+) Code ").append(codeCode).append(" existant dans la LOINC.");
								sDestinationDirectory = "ALL-VALID";
							} else {
								pValeurRetour.append("\n(-){UNKNOWN-CODE} Code ").append(codeCode)
										.append(" inconnu dans la LOINC.");
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-CODE",
										"\n(-){UNKNOWN-CODE} Code " + codeCode + " inconnu dans la LOINC.");
								bAllValid = false;
							}
						} else {
							final String validationResult = FhirUtilities.getOntoserverValidateCodeResult(codeSystem,
									codeCode, codeDisplay, vs.getName());
							pValeurRetour.append("\n").append(validationResult);
							if (validationResult.startsWith("(-){INCORRECT-DISPLAY}")) {
								if (iErrorLevel < 4) {
									iErrorLevel = 4;
									sDestinationDirectory = "INCORRECT-DISPLAY";
								}
								Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.log"),
										(validationResult + "\n").getBytes(), StandardOpenOption.APPEND);
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\INCORRECT-DISPLAY", validationResult);
								bAllValid = false;
							} else if (validationResult.startsWith("(-){UNKNOWN-CODE}")) {
								bAllValid = false;
								if (iErrorLevel < 5) {
									iErrorLevel = 5;
									sDestinationDirectory = "UNKNOWN-CODE";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-CODE", validationResult);
							} else if (validationResult.startsWith("(-){UNKNOWN-SYSTEM}")) {
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "UNKNOWN-SYSTEM";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\UNKNOWN-SYSTEM", validationResult);
							} else if (validationResult.startsWith("(-){NO-INTERPRETATION}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 3) {
									iErrorLevel = 3;
									sDestinationDirectory = "NO-INTERPRETATION";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\NO-INTERPRETATION", validationResult);
							} else if (validationResult.startsWith("(-){UNKNOWN-ERROR}")) {
								// Display incorrect
								bAllValid = false;
								if (iErrorLevel < 6) {
									iErrorLevel = 6;
									sDestinationDirectory = "VALIDATE-FAIL";
								}
								FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\VALIDATE-FAIL", validationResult);
							}
						}
					} catch (final Exception ex) {
						pValeurRetour.append(ex.getMessage()).append("\n").append(ex.toString()).append("\n")
								.append(ex.getStackTrace());
						if (iErrorLevel < 6) {
							iErrorLevel = 6;
							sDestinationDirectory = "VALIDATE-FAIL";
						}
						FhirUtilities.log(Constant.LOGFHIRFOLDER + "\\VALIDATE-FAIL", ex.getMessage());
					}

				});
			}

		} else {
			sDestinationDirectory = "VALIDATE-FAILED";
			bAllValid = false;
		}
		if (bAllValid) {
			sDestinationDirectory = "ALL-VALID";
			if (bMoveFile) {
				FhirUtilities.moveJdvFile(sJdvName.getAbsolutePath(), textFieldF.getText(), sDestinationDirectory);
			}
		} else {
			if (bMoveFile) {
				FhirUtilities.moveJdvFile(sJdvName.getAbsolutePath(), textFieldF.getText(), sDestinationDirectory);
			}
		}
		return pValeurRetour;
	}

	/**
	 * openNewStage
	 * 
	 * @param item
	 */
	private void openNewStage(final ItemEntity item, final TableView<ItemEntity> tableView,
			final TextField filterField) {
		final Stage newStage = new Stage();
		newStage.initModality(Modality.APPLICATION_MODAL);
		final VBox vbox = new VBox();
		name = new TextField(item.getName());
		uri = new TextField(item.getUrl());
		oid = new TextField(item.getOid());
		content = new TextField(item.getContenu());
		name.setStyle("-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
		uri.setStyle("-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
		oid.setStyle("-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
		content.setStyle(
				"-fx-font-size: 13px; -fx-text-fill: black; -fx-background-color: lightgrey; -fx-padding: 5px;");
		vbox.getChildren().addAll(createStyledLabel("Name: "), name, createStyledLabel("Oid: "), oid,
				createStyledLabel("Uri: "), uri, createStyledLabel("Content: "), content);
		vbox.setPadding(new Insets(10, 10, 10, 10));

		final JFXButton button1 = new JFXButton("");
		button1.setOnMouseEntered(e -> button1.setEffect(shadow));
		button1.setOnMouseExited(e -> button1.setEffect(null));
		final ImageView view = new ImageView(Constant.UPDATE);
		final Glow sepiaTone = new Glow();
		sepiaTone.setLevel(0);
		view.setEffect(sepiaTone);
		button1.setGraphic(view);
		button1.setStyle(Constant.STYLE1);
		button1.setPrefSize(150, 40);
		button1.setMinSize(150, 40);
		button1.setMaxSize(150, 40);
		final Tooltip tooltip1 = LocalUtility.createBoundTooltip("message.update");
		button1.setTooltip(tooltip1);
		button1.setText(LocalUtility.getString("message.update"));

		button1.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					IniFile.updateIniFile(fieldTermino, item.getSection(), name.getText(), oid.getText(), uri.getText(),
							content.getText());
					FhirUtilities.loadTerminology(new File(fieldTermino));
					final ObservableList<ItemEntity> itemList = FXCollections.observableArrayList();
					for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER.size(); iRow++) {
						final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER.get(iRow);
						ItemEntity item = new ItemEntity();
						item.setName(mapping[0]);
						item.setOid(mapping[1]);
						item.setUrl(mapping[2]);
						item.setContenu(mapping[3]);
						item.setLineNumber(Integer.parseInt(mapping[5]));
						item.setSection(mapping[4]);
						itemList.add(item);
					}
					final FilteredList<ItemEntity> filteredList = new FilteredList<>(itemList, p -> true);
					tableView.setItems(filteredList);

					filterField.textProperty().addListener((observable, oldValue, newValue) -> {
						filteredList.setPredicate(item -> {
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}
							final String lowerCaseFilter = newValue.toLowerCase();
							return Optional.ofNullable(item.getName())
									.map(name -> name.toLowerCase().contains(lowerCaseFilter)).orElse(false);

						});
					});

					final Alert alert = new Alert(AlertType.INFORMATION);
					final DialogPane dialogPane = alert.getDialogPane();
					Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
					Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText(LocalUtility.getString(SUCCES));
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait().ifPresent(response -> {
						if (response == ButtonType.OK) {
							newStage.close();
						}
					});
				});
			}
		});

		final JFXButton button2 = new JFXButton("");
		button2.setOnMouseEntered(e -> button2.setEffect(shadow));
		button2.setOnMouseExited(e -> button1.setEffect(null));
		final ImageView view1 = new ImageView(Constant.DELETE);
		sepiaTone.setLevel(0);
		view1.setEffect(sepiaTone);
		button2.setGraphic(view1);
		button2.setStyle(Constant.STYLE1);
		button2.setPrefSize(150, 40);
		button2.setMinSize(150, 40);
		button2.setMaxSize(150, 40);
		final Tooltip tooltip2 = LocalUtility.createBoundTooltip("message.remove.termino");
		button2.setTooltip(tooltip2);
		button2.setText(LocalUtility.getString("message.remove.termino"));

		button2.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				runTask(taskUpdateStage, progress);
				Platform.runLater(() -> {
					final String sectionName = item.getSection();
					try {
						IniFile.removeSectionFromIniFile(fieldTermino, sectionName);
						FhirUtilities.loadTerminology(new File(fieldTermino));
						final ObservableList<ItemEntity> itemList = FXCollections.observableArrayList();
						for (int iRow = 0; iRow < FhirUtilities.CODESYSTEMSONTOSERVER.size(); iRow++) {
							final String[] mapping = FhirUtilities.CODESYSTEMSONTOSERVER.get(iRow);
							ItemEntity item = new ItemEntity();
							item.setName(mapping[0]);
							item.setOid(mapping[1]);
							item.setUrl(mapping[2]);
							item.setContenu(mapping[3]);
							item.setLineNumber(Integer.parseInt(mapping[5]));
							item.setSection(mapping[4]);
							itemList.add(item);
						}
						final FilteredList<ItemEntity> filteredList = new FilteredList<>(itemList, p -> true);
						tableView.setItems(filteredList);

						filterField.textProperty().addListener((observable, oldValue, newValue) -> {
							filteredList.setPredicate(item -> {
								if (newValue == null || newValue.isEmpty()) {
									return true;
								}
								final String lowerCaseFilter = newValue.toLowerCase();
								return Optional.ofNullable(item.getName())
										.map(name -> name.toLowerCase().contains(lowerCaseFilter)).orElse(false);

							});
						});

						final Alert alert = new Alert(AlertType.INFORMATION);
						final DialogPane dialogPane = alert.getDialogPane();
						Utility.getStylesheets(dialogPane).add(getClass().getResource(Constant.CSS).toExternalForm());
						Utility.getStyleClass(dialogPane).add(Constant.DIALOG);
						dialogPane.setMinHeight(130);
						dialogPane.setMaxHeight(130);
						dialogPane.setPrefHeight(130);
						alert.setContentText(LocalUtility.getString(SUCCES));
						alert.setHeaderText(null);
						alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
						alert.showAndWait().ifPresent(response -> {
							if (response == ButtonType.OK) {
								newStage.close();
							}
						});
					} catch (final Exception e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.info(error);
						}
					}
				});
			}
		});

		final Region spacer101 = new Region();
		spacer101.setPrefWidth(5);
		HBox.setHgrow(spacer101, Priority.ALWAYS);

		final HBox hbox = new HBox();
		hbox.getChildren().addAll(button1, spacer101, button2);

		final Region spacer102 = new Region();
		spacer101.setPrefHeight(5);
		VBox.setVgrow(spacer102, Priority.ALWAYS);

		vbox.getChildren().addAll(spacer102, hbox);

		final Scene scene = new Scene(vbox, 400, 400);
		newStage.setScene(scene);
		newStage.setTitle(LocalUtility.getString("message.termino.details"));
		newStage.show();
	}

	/**
	 * Method to create a styled Label
	 * 
	 * @param text
	 * @return
	 */
	private Label createStyledLabel(final String text) {
		final Label label = new Label(text);
		label.setStyle("-fx-font-size: 13px; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 5px;");
		return label;
	}

	/**
	 * findFile
	 * 
	 * @param folderPath
	 * @param fileNameToSearch
	 * @throws IOException
	 */
	public static Path findFile(final Path folderPath, final String fileNameToSearch) throws IOException {
		final Path[] foundFile = new Path[1];
		Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// Check if the current file matches the search file name
				if (file.getFileName().toString().equals(fileNameToSearch)) {
					foundFile[0] = file;
					return FileVisitResult.TERMINATE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return foundFile[0];
	}

	/**
	 * createCodeSystemHeader
	 * 
	 * @param pID
	 * @param pURL
	 * @param pOID
	 * @param pName
	 * @param pVersion
	 * @param pDescription
	 * @param pMonCS
	 */
	public static void createCodeSystemHeader(final String pID, final String pURL, final String pOID,
			final String pName, final String pVersion, final String pDescription,
			final org.hl7.fhir.r4.model.CodeSystem pMonCS) {
		pMonCS.setId(pID);
		pMonCS.setUrl(pURL);
		pMonCS.setVersion(pVersion);
		pMonCS.setStatus(PublicationStatus.ACTIVE);
		pMonCS.setDescription(pDescription);
		final String name = pName.replace("-", "_");
		pMonCS.setName(name);
//	    String sDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + "-00:00";
		final Date date = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
		pMonCS.setDate(date);
		pMonCS.setExperimental(false);
		pMonCS.setPublisher("Agence du Numérique en Santé (ANS) - 2-10 Rue d'Oradour-sur-Glane, 75015 Paris");
		pMonCS.setCaseSensitive(false);

		// Meta
		final Meta md = new Meta();
		String[] profiles = { "http://hl7.org/fhir/StructureDefinition/shareablecodesystem" };
		// Convert String[] to List<CanonicalType>
		final List<CanonicalType> canonicalProfiles = Arrays.stream(profiles).map(CanonicalType::new)
				.collect(Collectors.toList());
		md.setProfile(canonicalProfiles);
		pMonCS.setMeta(md);

		// Extension effectivePeriod
		final Period valuePeriod = new Period();
		final String isoString = "2001-01-01T12:00:00+01:00";
		// Parse the ISO 8601 string to OffsetDateTime
		final OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		// Convert to java.util.Date
		final Date date1 = Date.from(offsetDateTime.toInstant());
		valuePeriod.setStart(date1);
		final Extension effectivePeriod = new Extension(
				"http://hl7.org/fhir/StructureDefinition/resource-effectivePeriod", valuePeriod);
		pMonCS.getExtension().add(effectivePeriod);

		String sIdentifierValue = pOID;
		if (!sIdentifierValue.startsWith("urn:oid:")) {
			sIdentifierValue = "urn:oid:" + pOID;
		}
		final Identifier identifier = new Identifier();
		identifier.setValue(sIdentifierValue);
		identifier.setSystem("urn:ietf:rfc:3986");
		pMonCS.getIdentifier().add(identifier);
		pMonCS.setContent(CodeSystemContentMode.COMPLETE);
	}

	/**
	 * countCheckedItems
	 * 
	 * @param selectedItems
	 * @return
	 */
	public int countCheckedItems(final Map<String, Boolean> selectedItems) {
		return (int) selectedItems.values().stream().filter(Boolean::booleanValue).count();
	}

	/**
	 * updateCheckedItemsCount
	 * 
	 * @param textAreaStat
	 * @param numberOfCheckedItems
	 * @param totalItems
	 */
	private void updateCheckedItemsCount(final TextArea textAreaStat) {
		final String oldText = textAreaStat.getText();
		final String regex = "(Nombre de jeux de valeurs cochés: )\\d+/\\d+";
		final String newText = oldText.replaceAll(regex, "$1" + numberOfCheckedItems + "/" + listSize);
		textAreaStat.setText(newText);
	}

}