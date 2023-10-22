package application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import views.VcfAnalyzerView;

@SpringBootApplication
@ComponentScan(basePackages={"views", "controllers", "application", "model"})
public class Main  extends AbstractJavaFxApplicationSupport{

    public static void main(String[] args) {
	        launchApp(Main.class, VcfAnalyzerView.class, args);
    }
}