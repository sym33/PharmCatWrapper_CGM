package application;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.repackaged.com.google.common.base.Strings;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.Parser;
import model.MappingProperties;

@Configuration
public class SpringConfig {
	
	@Autowired
	MappingProperties mappingMap;
	
//	@Value("${default.folder.vcf}")
//	private String defaultVcfFolder;
//	
//	@Value("${default.folder.order}")
//	private String defaultOrderFolder;
//	
//	@Value("${default.folder.out}")
//	private String defaultOutFolder;
	
	@Bean
	public ReportTransformer reportTransformer() {
		ReportTransformer transformer = new ReportTransformer();
		transformer.setMappingMap(mappingMap);
		return transformer;
	}
	
	@Bean
	public HapiContext hapiContext() {
		return new DefaultHapiContext();
	}
	
	@Bean
	public Parser hl7v2Parser() {
		Parser p = hapiContext().getGenericParser();
		return p;
	}
	
//	@Bean
//	public AnalyzerService analyzerRunnable() {
//		AnalyzerService service = new AnalyzerService();
//		
//		if(Strings.isNullOrEmpty(defaultOrderFolder) || Strings.isNullOrEmpty(defaultOutFolder) || Strings.isNullOrEmpty(defaultVcfFolder))
//			throw new NullPointerException("The default folders in application.properties must not be null.");
//		service.setOrderFolderPath(new File(defaultOrderFolder));
//		service.setOutputFolderPath(new File(defaultOutFolder));
//		service.setVcfFolderPath(new File(defaultVcfFolder));
//		return service;
//	}

	public void setMappingMap(MappingProperties mappingMap) {
		this.mappingMap = mappingMap;
	}
	
	

}
