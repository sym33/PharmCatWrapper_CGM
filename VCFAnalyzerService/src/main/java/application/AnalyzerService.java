package application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.pharmgkb.pharmcat.PharmCAT;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ORM_O01;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import ca.uhn.hl7v2.parser.Parser;
import model.AnalyzerState;
import model.VcfAnalyzerTask;

public class AnalyzerService {

	File vcfFolderPath, orderFolderPath, outputFolderPath;
	AnalyzerState state = new AnalyzerState();	
	static boolean running = false;
	
	ReportTransformer transformer;

	Parser hl7v2Parser;

	public AnalyzerService(ReportTransformer transformer, Parser hl7v2Parser) {
		
		this.transformer = transformer;
		this.hl7v2Parser = hl7v2Parser;
	}

	public void start() {
		running = true;
		while (isRunning()) {
			try {
				prepareTasklist();
			} catch (HL7Exception | IOException e1) {
				e1.printStackTrace();
			}

			if (state.getOpenTasks() != null && state.getOpenTasks().size() > 0) {
				VcfAnalyzerTask currentTask = state.getOpenTasks().iterator().next();
				state.setCurrentTask(currentTask);
				state.getOpenTasks().remove(currentTask);
				File tempFolder = new File(outputFolderPath, "temp");
				tempFolder.mkdirs();
				File successFolder = new File(orderFolderPath, "success");
				successFolder.mkdirs();
				File errorFolder = new File(orderFolderPath, "error");
				errorFolder.mkdirs();

				try {
					
					analyzeVcfFile(tempFolder.toPath(), null, null);
					File pharmCatReport = new File(tempFolder, currentTask.getVcf().getName() + ".report.json");
					
					ORU_R01 response = transformer.transformReport(pharmCatReport, currentTask.getOrderMessage());
					Files.write(new File(outputFolderPath, currentTask.getVcf().getName().replace(".vcf", "_response.hl7")).toPath(), response.encode().getBytes());
					
					
					
//					Files.move(currentTask.getVcf().toPath(),
//							new File(outputFolderPath, currentTask.getVcf().getName()).toPath(),
//							StandardCopyOption.REPLACE_EXISTING);
					Files.move(currentTask.getOrder().toPath(),
							new File(successFolder, currentTask.getOrder().getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					
					// delete temp folder
//					Files.walk(tempFolder.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
//							.forEach(File::delete);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					try {
						Files.move(currentTask.getOrder().toPath(),
								new File(errorFolder, currentTask.getOrder().getName()).toPath(),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}

				state.getFinishedTasks().add(currentTask);

				state.setCurrentTask(null);
			} else if (isRunning()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void createTasks(File[] orderFiles, Map<String, File> vcfFiles) throws HL7Exception, IOException {
		// remove all remaining files from open tasklist, otherwise they would be doubled.
		state.getOpenTasks().clear();
		for (File orderFile : orderFiles) {
			String fileContent = readFile(orderFile.toPath(), Charset.forName("utf8"));
			Message msg = hl7v2Parser.parse(fileContent);
			ORM_O01 ormMsg = (ORM_O01) msg;
			String key = ormMsg.getPATIENT().getPID().getPatientID().getCx1_ID().getValue();
			key += "_" + ormMsg.getORDER().getORC().getOrc3_FillerOrderNumber().encode();
			if (vcfFiles.containsKey(key)) {
				VcfAnalyzerTask task = new VcfAnalyzerTask();
				task.setOrderMessage(ormMsg);
				task.setOrder(orderFile);
				task.setVcf(vcfFiles.get(key));
				task.setKey(key);
				state.getOpenTasks().add(task);
			}
		}
	}

	private void analyzeVcfFile(Path outputDir, Path definitionsDir, Path guidelinesDir) throws Exception {
		PharmCAT pharmcat = new PharmCAT(outputDir, definitionsDir, guidelinesDir);
		File f = state.getCurrentTask().getVcf();
		Path astrolabeFile = null;
		pharmcat.writeJson(true);
		pharmcat.execute(f.toPath(), astrolabeFile, f.getName());
	}

	private void prepareTasklist() throws HL7Exception, IOException {
		Map<String, File> vcfFiles = readVcfFilesFromFolder(vcfFolderPath);
		File[] orderFiles = readOrderFilesFromFolder(orderFolderPath);
		createTasks(orderFiles, vcfFiles); // pushes now everything into
											// this.state
	}

	private Map<String, File> readVcfFilesFromFolder(File vcfFolderPath) {
		Map<String, File> map = new HashMap<String, File>();
		File[] files = vcfFolderPath.listFiles((dir, name) -> {
			return name.toLowerCase().endsWith(".vcf");
		});
		for (File f : files) {
			// Get filename and cut of '.vcf'
			String fName = f.getName().split("\\.")[0];
			map.put(fName, f);
		}
		return map;
	}

	private File[] readOrderFilesFromFolder(File orderFolderPath) throws IOException, HL7Exception {
		return orderFolderPath.listFiles((dir, name) -> {
			return name.toLowerCase().endsWith(".hl7");
		});
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public static String readFile(Path path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}

	public void setTransformer(ReportTransformer transformer) {
		this.transformer = transformer;
	}

	public void setVcfFolderPath(File vcfFolderPath) {
		this.vcfFolderPath = vcfFolderPath;
	}

	public void setOrderFolderPath(File orderFolderPath) {
		this.orderFolderPath = orderFolderPath;
	}

	public void setOutputFolderPath(File outputFolderPath) {
		this.outputFolderPath = outputFolderPath;
	}

	public boolean isStopped() {
		return !isRunning();
	}

	public void stop() {
		setRunning(false);
	}
	
	
}
