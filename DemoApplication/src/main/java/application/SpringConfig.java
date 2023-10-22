package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import model.MappingProperties;

@Configuration
public class SpringConfig {
	
	@Autowired
	MappingProperties mappingMap;
	
	@Bean
	public TaskExecutor taskExecutor() {
		TaskExecutor exec = new SimpleAsyncTaskExecutor();
		return exec;
	}
	
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
	@Scope("prototype")
	public AnalyzerRunnable analyzerRunnable() {
		return new AnalyzerRunnable();
	}

	public void setMappingMap(MappingProperties mappingMap) {
		this.mappingMap = mappingMap;
	}
	
	

}
