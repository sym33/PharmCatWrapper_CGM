package controllers;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

import com.google.common.base.Strings;

import application.AnalyzerRunnable;
import application.Main;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import model.AnalyzerState;
import model.VcfAnalyzerTask;

@FXMLController
public class VcfAnalyzerController implements Observer {

	@Autowired
	ApplicationContext context;
	
	@Value("${default.folder.vcf}")
	private String defaultVcfFolder;
	
	@Value("${default.folder.order}")
	private String defaultOrderFolder;
	
	@Value("${default.folder.out}")
	private String defaultOutFolder;
	
	@Autowired
	TaskExecutor taskExecutor;
	
	public enum ViewState {
		READY, RUNNDING, STOPPED, STOPPING
	}

	@FXML
	private Button btnVcfFolderSelect, btnOrderFolderSelect, btnOutputFolderSelect, btnStart, btnStop;
	@FXML
	private TextField tfVcfFolderPath, tfOrderFolderPath, tfOutputFolderPath;
	@FXML
	ListView<VcfAnalyzerTask> lvToDoList, lvDoneList;
	@FXML
	Label lbCurrentFile;

	private AnalyzerRunnable analyzerRunnable;

	private ObservableList<VcfAnalyzerTask> tasklist = FXCollections.observableArrayList();
	private ObservableList<VcfAnalyzerTask> finishedTasks = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		lvToDoList.setCellFactory(param -> new ListCell<VcfAnalyzerTask>() {
			@Override
			protected void updateItem(VcfAnalyzerTask item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null || item.getKey() == null) {
					setText(null);
				} else {
					setText(item.getKey());
				}
			}
		});
		lvToDoList.setItems(tasklist);
		lvDoneList.setCellFactory(param -> new ListCell<VcfAnalyzerTask>() {
			@Override
			protected void updateItem(VcfAnalyzerTask item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null || item.getKey() == null) {
					setText(null);
				} else {
					setText(item.getKey());
				}
			}
		});
		lvDoneList.setItems(finishedTasks);
		
		File f = new File(defaultVcfFolder);	
		if(f.exists()) {
			tfVcfFolderPath.setText(f.getAbsolutePath());
		}
		f = new File(defaultOrderFolder);	
		if(f.exists()) {
			tfOrderFolderPath.setText(f.getAbsolutePath());
		}
		f = new File(defaultOutFolder);	
		if(f.exists()) {
			tfOutputFolderPath.setText(f.getAbsolutePath());
		}
		if (isViewStateReady()) {
			setViewState(ViewState.READY);
		}
	}

	@FXML
	private void btnVcfFolderSelect_OnAction(ActionEvent e) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File f = new File(defaultVcfFolder);	
		if(f.exists())
			directoryChooser.setInitialDirectory(f);
		File selectedDirectory = directoryChooser.showDialog(Main.getStage());

		if (selectedDirectory == null) {
			tfVcfFolderPath.setText(defaultVcfFolder);
		} else {
			tfVcfFolderPath.setText(selectedDirectory.getAbsolutePath());
			if (isViewStateReady()) {
				setViewState(ViewState.READY);
			}
		}

	}

	@FXML
	private void btnOrderFolderSelect_OnAction(ActionEvent e) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File f = new File(defaultOrderFolder);	
		if(f.exists())
			directoryChooser.setInitialDirectory(f);
		File selectedDirectory = directoryChooser.showDialog(Main.getStage());
		if (selectedDirectory == null) {
			tfOrderFolderPath.setText(defaultOrderFolder);
		} else {
			tfOrderFolderPath.setText(selectedDirectory.getAbsolutePath());
			if (isViewStateReady()) {
				setViewState(ViewState.READY);
			}
		}

	}

	@FXML
	private void btnOutputFolderSelect_OnAction(ActionEvent e) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File f = new File(defaultOutFolder);	
		if(f.exists())
			directoryChooser.setInitialDirectory(f);
		File selectedDirectory = directoryChooser.showDialog(Main.getStage());
		if (selectedDirectory == null) {
			tfOutputFolderPath.setText(defaultOutFolder);
		} else {
			tfOutputFolderPath.setText(selectedDirectory.getAbsolutePath());
			if (isViewStateReady()) {
				setViewState(ViewState.READY);
			}
		}

	}

	@FXML
	private void btnStart_OnAction(ActionEvent e) {
		setViewState(ViewState.RUNNDING);
	}

	@FXML
	private void btnStop_OnAction(ActionEvent e) {
		setViewState(ViewState.STOPPING);
	}

	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(() -> {
			if (arg instanceof ViewState) {
				setViewState((ViewState) arg);
			} else {

				AnalyzerState state = (AnalyzerState) arg;
				tasklist.clear();
				tasklist.addAll(state.getOpenTasks());
				finishedTasks.clear();
				finishedTasks.addAll(state.getFinishedTasks());
				lbCurrentFile
						.setText((state.getCurrentTask() == null ? "----------" : state.getCurrentTask().getKey()));
			}
		});
	}

	private boolean isViewStateReady() {
		if (!tfVcfFolderPath.getText().isEmpty() && !tfOrderFolderPath.getText().isEmpty()
				&& !tfOutputFolderPath.getText().isEmpty()) {
			return true;
		}
		return false;
	}

	private void setViewState(ViewState state) {
		if (state == ViewState.RUNNDING) {
			btnStart.setDisable(true);
			btnVcfFolderSelect.setDisable(true);
			btnOrderFolderSelect.setDisable(true);
			btnOutputFolderSelect.setDisable(true);
			btnStop.setDisable(false);
			analyzerRunnable = context.getBean(AnalyzerRunnable.class);
			analyzerRunnable.setOrderFolderPath(new File(tfOrderFolderPath.getText()));
			analyzerRunnable.setVcfFolderPath(new File(tfVcfFolderPath.getText()));
			analyzerRunnable.setOutputFolderPath(new File(tfOutputFolderPath.getText()));
			analyzerRunnable.register(this);
			taskExecutor.execute(analyzerRunnable);
		}
		if (state == ViewState.STOPPING) {
			analyzerRunnable.setRunning(false);
			btnStop.setDisable(true);
			btnStop.setText("Stopping...");
		}
		if (state == ViewState.STOPPED) {
			btnStart.setDisable(false);
			btnVcfFolderSelect.setDisable(false);
			btnOrderFolderSelect.setDisable(false);
			btnOutputFolderSelect.setDisable(false);
			btnStop.setDisable(true);
			tasklist.clear();
			finishedTasks.clear();
			lbCurrentFile.setText("----------");
			btnStop.setText("Stop");
		}
		if (state == ViewState.READY) {
			btnStart.setDisable(false);
		}
	}

}
