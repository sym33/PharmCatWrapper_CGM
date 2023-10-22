package application;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.google.api.client.repackaged.com.google.common.base.Strings;

import ca.uhn.hl7v2.parser.Parser;

@SpringBootApplication
@ComponentScan(basePackages={"views", "controllers", "application", "model"})
public class Main implements CommandLineRunner {
	
	@Value("${default.folder.vcf}")
	private String defaultVcfFolder;
	
	@Value("${default.folder.order}")
	private String defaultOrderFolder;
	
	@Value("${default.folder.out}")
	private String defaultOutFolder;
	
	@Autowired
	ReportTransformer transformer;

	@Autowired
	Parser hl7v2Parser;
	
	static AnalyzerService service = null;
	
	static Logger logger = Logger.getLogger("VCFAnalyzer");
	
	private static ApplicationContext applicationContext = null;

	public static void main(String[] args) {
	    String mode = args != null && args.length > 0 ? args[0] : null;

	    if (logger.isDebugEnabled()) {
	        logger.debug("PID:" + ManagementFactory.getRuntimeMXBean().getName() + 
	                     " Application mode:" + mode + " context:" + applicationContext);
	    }
	    if (applicationContext != null && mode != null && "stop".equals(mode)) {
	    	service.stop();
	        System.exit(SpringApplication.exit(applicationContext, new ExitCodeGenerator() {
	            @Override
	            public int getExitCode() {
	                return 0;
	            }
	        }));
	    }
	    else {
	        SpringApplication app = new SpringApplication(Main.class);
	        applicationContext = app.run(args);
	        if (logger.isDebugEnabled()) {
	            logger.debug("PID:" + ManagementFactory.getRuntimeMXBean().getName() + 
	                         " Application started context:" + applicationContext);
	        }
	    }
	}
    
    @Override
    public void run(String... args) throws Exception {
    	StringBuilder sb = new StringBuilder();
    	for(String s : args)
    		sb.append(s).append(" ");
    	logger.info("VCFAnalyzer received commands: " + sb.toString());
    	System.out.println("VCFAnalyzer received commands: " + sb.toString());
    	
		if(Strings.isNullOrEmpty(defaultOrderFolder) || Strings.isNullOrEmpty(defaultOutFolder) || Strings.isNullOrEmpty(defaultVcfFolder))
			throw new NullPointerException("The default folders in application.properties must not be null.");
		
		System.out.println("Looking for orders in: " + defaultOrderFolder);
		System.out.println("Looking for variant call file in: " + defaultVcfFolder);
		System.out.println("Writing observations in: " + defaultOrderFolder);
		if(service == null) {
			service = new AnalyzerService(transformer, hl7v2Parser);
			service.setOrderFolderPath(new File(defaultOrderFolder));
			service.setOutputFolderPath(new File(defaultOutFolder));
			service.setVcfFolderPath(new File(defaultVcfFolder));
		}

    	
    	if(args.length > 0) {
    		if("start".equals(args[0]))
    			service.start();
    		if("stop".equals(args[0]))
        		service.stop();
    	} else if (service.isRunning()) {
    		service.stop();
    	} else {
    		service.start();
    	}
    }
}
