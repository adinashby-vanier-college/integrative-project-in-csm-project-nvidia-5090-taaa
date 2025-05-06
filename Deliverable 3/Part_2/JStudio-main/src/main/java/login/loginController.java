package login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;

import java.util.function.UnaryOperator;

public class loginController {

    @FXML
    Label userId, userPass, key1, key2;

    @FXML
    HBox box1, box2, box3, box4;

    @FXML
    TextField userIdField, userPasswordField, key1Field, key2Field;

    @FXML
    Button createAccount, enterBtn;

    private final int MAX_CHARS = 10;
    private final String RESTRICTION = "[a-zA-Z\\\\s]*";

    @FXML
    public void initialize() {


        enterBtn.setOnAction(event -> {});
        createAccount.setOnAction(event -> {});

        //todo login logic
        /*
        1. parse through the csv for user name
        2. get the user encrypted password
        3. compare the user entered password (has to not be cap sensitive)
        4. if true launch java application

        1. if no account, the user will enter new credentials
        2. if previous credentials do not exist, put it to csv (username has to be different)
        3. put the encoded user password
        4.launch application
         */
    }

    /**
     * https://www.youtube.com/watch?v=kMplmN2lD1Q
     * https://www.youtube.com/watch?v=qNZCim1SxZc&t=47s
     */
    private void applyTextRestriction(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches(RESTRICTION) && newText.length() <= MAX_CHARS) {
                return change;
            } else {
                return null;
            }
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

}
