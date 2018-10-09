package seedu.address.ui;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.CloseTriviaTestViewEvent;
import seedu.address.commons.events.ui.DisplayBrowserEventChangedEvent;
import seedu.address.commons.events.ui.DisplayImportHelpChangedEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.events.ui.ExtraInformationDisplayChangeEvent;
import seedu.address.commons.events.ui.ShowHelpRequestEvent;
import seedu.address.commons.events.ui.ShowTriviaTestViewEvent;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private BrowserPanel browserPanel;
    private PersonListPanel personListPanel;
    private CardListPanel cardListPanel;
    private QuestionListPanel questionListPanel;
    private AnswerListPanel answerListPanel;
    private Config config;
    private UserPrefs prefs;
    private HelpWindow helpWindow;
    private ImportHelpDisplay importHelpDisplay;

    @FXML
    private StackPane extraInfomationPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane cardListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private StackPane testMQuestionListPanelPlaceholder;

    @FXML
    private StackPane testMAnswerListPanelPlaceholder;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTitle(config.getAppTitle());
        setWindowDefaultSize(prefs);

        setAccelerators();
        registerAsAnEventHandler(this);

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        ImportHelpDisplay importHelpDisplay = new ImportHelpDisplay();
        extraInfomationPlaceholder.getChildren().add(importHelpDisplay.getRoot()); // index 0
        browserPanel = new BrowserPanel();
        extraInfomationPlaceholder.getChildren().add(browserPanel.getRoot()); // index 1

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        cardListPanel = new CardListPanel(logic.getFilteredCardList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());
        cardListPanelPlaceholder.getChildren().add(cardListPanel.getRoot());
        ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    public CardListPanel getCardListPanel() {
        return cardListPanel;
    }

    void releaseResources() {
        browserPanel.freeResources();
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleHelp();
    }

    @Subscribe
    private void handleShowTriviaTestiewEvent(ShowTriviaTestViewEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));

        questionListPanel = new QuestionListPanel(event.getTest().getQuestions());
        testMQuestionListPanelPlaceholder.getChildren().add(questionListPanel.getRoot());

        answerListPanel = new AnswerListPanel(event.getTest().getAnswers());
        testMAnswerListPanelPlaceholder.getChildren().add(answerListPanel.getRoot());
    }

    @Subscribe
    private void handleCloseTriviaTestViewEvent(CloseTriviaTestViewEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));

        testMQuestionListPanelPlaceholder.getChildren().clear();
        testMAnswerListPanelPlaceholder.getChildren().clear();
    }

    @Subscribe
    private void handleExtraInfomationDisplayChangeEvent(ExtraInformationDisplayChangeEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (event.getToDisplay().equals("ImportHelpDisplay")) {
            // display import help
            EventsCenter.getInstance().post(new DisplayImportHelpChangedEvent(true));
            // hide browser
            EventsCenter.getInstance().post(new DisplayBrowserEventChangedEvent(false));
        } else if (event.getToDisplay().equals("Browser")) {
            // hide import help
            EventsCenter.getInstance().post(new DisplayImportHelpChangedEvent(false));
            // display browser
            EventsCenter.getInstance().post(new DisplayBrowserEventChangedEvent(true));
        }
    }

//    private void displayImportHelp() {
//        ObservableList<Node> children = extraInfomationPlaceholder.getChildren();
//        assert(children.size() > 1);
//        Node topNode = children.get(children.size()-1);
//        if (topNode.getId().equals("ImportHelpDisplay")) {
//            return;
//        }
//
//        topNode.setVisible(false);
//        topNode.toBack();
//        Node toDisplay = children.get(children.size()-2);
//        toDisplay.setVisible(true);
//    }
}
