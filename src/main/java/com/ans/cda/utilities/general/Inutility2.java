package com.ans.cda.utilities.general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * I18N utility class
 * 
 * @author bensa Nizar
 */
public final class Inutility2 {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(Inutility2.class);

	/**
	 * INUtility constructor
	 */
	private Inutility2() {
		// empty constructor
	}

	/**
	 * newObsMenu
	 * 
	 * @param fileChooser
	 */
	public static ObservableList<ExtensionFilter> newExtFilter(final FileChooser fileChooser) {
		return fileChooser.getExtensionFilters();
	}

	/**
	 * getStyleScene
	 * 
	 * @param scene
	 */
	public static ObservableList<String> getStyleScene(final Scene scene) {
		return scene.getStylesheets();
	}

	/**
	 * getStyleDialog
	 * 
	 * @param dialogPane
	 */
	public static ObservableList<String> getStyleDialog(final DialogPane dialogPane) {
		return dialogPane.getStylesheets();
	}

	/**
	 * getStyleClass
	 * 
	 * @param dialogPane
	 */
	public static ObservableList<String> getStyleClass(final DialogPane dialogPane) {
		return dialogPane.getStyleClass();
	}

	/**
	 * getChildrenNode
	 * 
	 * @param vBox
	 * @return
	 */
	public static ObservableList<Node> getChildrenNode(final VBox vBox) {
		return vBox.getChildren();
	}

	/**
	 * newClassLoader
	 * 
	 */
	public static ClassLoader newClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * getStyleImageView
	 * 
	 * @param dialogPane
	 */
	public static ObservableList<String> getStyleImageView(final BorderPane dialogPane) {
		return dialogPane.getStylesheets();
	}

	/**
	 * getCaretPositionForLine
	 * 
	 * @param lineNumber
	 * @return
	 */
	public static int getCaretPositionForLine(final TextArea textAr, final int lineNumber) {
		final String[] lines = textAr.getText().split("\n");
		int position = 0;
		for (int i = 0; i < lineNumber - 1; i++) {
			position += lines[i].length() + 1;
		}
		return position;
	}

	/**
	 * readFileContents
	 * 
	 * @param file
	 */
	public static void readFileContents(final TextArea textAr, final File file) {
		try (BufferedReader breader = new BufferedReader(new FileReader(file))) {
			final StringBuilder content = new StringBuilder();
			String line;
			int lineNumber = 1;
			while ((line = breader.readLine()) != null) {
				content.append(lineNumber).append(' ').append(line).append('\n');
				lineNumber++;
			}
			textAr.setText(content.toString());
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * goToLine
	 * 
	 * @param textArea
	 * @param lineStr
	 */
	public static void goToLine(final TextArea textAr, final String lineStr) {
		try {
			final int lineNumber = Integer.parseInt(lineStr);
			final String[] lines = textAr.getText().split("\n");
			if (lineNumber > 0 && lineNumber <= lines.length) {
				final int start = getCaretPositionForLine(textAr, lineNumber);
				final int end = start + lines[lineNumber - 1].length();
				textAr.selectRange(start, end);
				textAr.setStyle(
						"-fx-highlight-fill: linear-gradient(#328BDB 0%, #207BCF 25%, #1973C9 75%, #0A65BF 100%); -fx-highlight-text-fill: white;");
				textAr.requestFocus();
			}
		} catch (final NumberFormatException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

}