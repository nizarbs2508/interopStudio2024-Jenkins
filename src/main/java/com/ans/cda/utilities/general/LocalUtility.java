package com.ans.cda.utilities.general;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.jfoenix.controls.JFXButton;

import io.github.palexdev.materialfx.controls.MFXRadioButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

/**
 * I18N utility class
 * 
 * @author bensalem Nizar
 */
public final class LocalUtility {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(LocalUtility.class);
	/**
	 * the current selected Locale
	 */
	private static ObjectProperty<Locale> locale;

	/**
	 * INUtility constructor
	 */
	private LocalUtility() {
		// empty constructor
	}

	/**
	 * browser
	 * 
	 * @param url
	 */
	public static void browser(final String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				final Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(URI.create(url));
				}
			}
		} catch (final IOException | InternalError e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
	}

	/**
	 * set default local
	 */
	static {
		locale = new SimpleObjectProperty<>(getDefaultLocale());
		locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
	}

	/**
	 * get the default locale. This is the systems default if contained in the
	 * supported locales, english otherwise.
	 *
	 * @return
	 */
	public static Locale getDefaultLocale() {
		final Locale sysDefault = Locale.getDefault();
		return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.FRENCH;
	}

	/**
	 * get the supported Locales.
	 *
	 * @return List of Locale objects.
	 */
	public static List<Locale> getSupportedLocales() {
		return new ArrayList<>(
				Arrays.asList(Locale.FRENCH, Locale.ENGLISH, Locale.GERMAN, Locale.forLanguageTag("nl-NL"),
						Locale.forLanguageTag("es-ES"), Locale.forLanguageTag("pt-PT"), Locale.ITALIAN));
	}

	/**
	 * getLocale
	 * 
	 * @return
	 */
	public static Locale getLocale() {
		return locale.get();
	}

	/**
	 * setLocale
	 * 
	 * @param locale
	 */
	public static void setLocale(final Locale locale) {
		localeProperty().set(locale);
		Locale.setDefault(locale);
	}

	/**
	 * localeProperty
	 * 
	 * @return
	 */
	public static ObjectProperty<Locale> localeProperty() {
		return locale;
	}

	/**
	 * get
	 *
	 * @param key  message key
	 * @param args optional arguments for the message
	 * @return localized formatted string
	 */
	public static String get(final String key, final Object... args) {
		final ResourceBundle bundle = ResourceBundle.getBundle("properties/messages", getLocale());
		return MessageFormat.format(bundle.getString(key), args);
	}

	/**
	 * creates a String binding to a localized String for the given message bundle
	 * key
	 *
	 * @param key key
	 * @return String binding
	 */
	public static StringBinding createStringBinding(final String key, final Object... args) {
		return Bindings.createStringBinding(() -> get(key, args), locale);
	}

	/**
	 * creates a String Binding to a localized String that is computed by calling
	 * the given func
	 *
	 * @param func function called on every change
	 * @return StringBinding
	 */
	public static StringBinding createStringBinding(final Callable<String> func) {
		return Bindings.createStringBinding(func, locale);
	}

	/**
	 * createBoundTooltip
	 * 
	 * @param key
	 * @return
	 */
	public static Tooltip createBoundTooltip(final String key) {
		final Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(createStringBinding(key, locale));
		return tooltip;
	}

	/**
	 * createBoundTextArea
	 * 
	 * @param key
	 * @return
	 */
	public static TextArea createBoundTextArea(final String key) {
		final TextArea tooltip = new TextArea();
		tooltip.textProperty().bind(createStringBinding(key, locale));
		return tooltip;
	}

	/**
	 * creates a bound Label whose value is computed on language change.
	 *
	 * @param func the function to compute the value
	 * @return Label
	 */
	public static Label labelForValue(final Callable<String> func) {
		final Label label = new Label();
		label.textProperty().bind(createStringBinding(func));
		return label;
	}

	/**
	 * creates a bound Label whose value is computed on language change.
	 *
	 * @param func the function to compute the value
	 * @return Label
	 */
	public static MFXTextField textFieldForValue(final Callable<String> func) {
		final MFXTextField label = new MFXTextField();
		label.textProperty().bind(createStringBinding(func));
		return label;
	}

	/**
	 * creates a bound Button for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static Button buttonForKey(final String key, final Object... args) {
		final Button button = new Button();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * creates a bound Button for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static JFXButton buttonForKeyMFX(final String key, final Object... args) {
		final JFXButton button = new JFXButton();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * boxForKeyCheck
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public static CheckBox boxForKeyCheck(final String key, final Object... args) {
		final CheckBox check = new CheckBox();
		check.textProperty().bind(createStringBinding(key, args));
		return check;
	}

	/**
	 * creates a bound MenuItem for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static MenuItem menuBarForKey(final String key, final Object... args) {
		final MenuItem button = new MenuItem();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}
	
	/**
	 * tabForKey
	 * @param key
	 * @param args
	 * @return
	 */
	public static Tab tabForKey(final String key, final Object... args) {
		final Tab button = new Tab();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * creates a bound MenuItem for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static Menu menuForKey(final String key, final Object... args) {
		final Menu button = new Menu();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * creates a bound MenuItem for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static Menu menuButtonForKey(final String key, final Object... args) {
		final Menu button = new Menu();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * creates a bound RadioButton for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static RadioButton radioForKey(final String key, final Object... args) {
		final RadioButton button = new RadioButton();
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * creates a bound RadioButton for the given resourcebundle key
	 *
	 * @param key  ResourceBundle key
	 * @param args optional arguments for the message
	 * @return Button
	 */
	public static MFXRadioButton radiomfxForKey(final String key, final Object... args) {
		final MFXRadioButton button = new MFXRadioButton("");
		button.textProperty().bind(createStringBinding(key, args));
		return button;
	}

	/**
	 * comboForKey
	 * 
	 * @param keys
	 * @param args
	 * @return
	 */
	public static ComboBox<String> comboForKey(final List<String> keys, final Object... args) {
		final ComboBox<String> combo = new ComboBox<>();
		for (final String key : keys) {
			combo.promptTextProperty().bind(createStringBinding(key, args));
		}
		return combo;
	}

	/**
	 * sets the given Locale in the I18N class and keeps count of the number of
	 * switches.
	 *
	 * @param locale the new local to set
	 */
	public static void switchLanguage(final Locale locale) {
		setLocale(locale);
	}

	/**
	 * getString value
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(final String key) {
		final ResourceBundle bundle = ResourceBundle.getBundle("properties/messages", getLocale());
		return bundle.getString(key);
	}

}