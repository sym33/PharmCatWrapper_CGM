package application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v24.datatype.CE;
import ca.uhn.hl7v2.model.v24.datatype.ST;
import ca.uhn.hl7v2.model.v24.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v24.message.ORM_O01;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import model.MappingProperties;

public class ReportTransformer {
	
	public MappingProperties mappingMap;

	@Autowired
	HapiContext context = new DefaultHapiContext();
	
	public ORU_R01 transformReport(File report, ORM_O01 request) throws IOException, HL7Exception {
		ORU_R01 result = new ORU_R01();
		result.initQuickstart("ORU", "R01", "T");
		result.getPATIENT_RESULT().getPATIENT().getPID().parse(request.getPATIENT().getPID().encode());
		result.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().parse(request.getPATIENT().getPATIENT_VISIT().getPV1().encode());
		result.getPATIENT_RESULT().getORDER_OBSERVATION().getORC().parse(request.getORDER().getORC().encode());
		result.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR().parse(request.getORDER().getORDER_DETAIL().getOBR().encode());

//		ObjectMapper mapper = new ObjectMapper();
//
//		JsonNode root = mapper.readTree(report);
		
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElem = jsonParser.parse(IOUtils.toString(report.toURI(), Charset.defaultCharset()));
		

		int i = 0;  // index of the obx elements.
		int i2 = 1; // id of the obx elements
		Date analysisDate = new Date();
		for (JsonElement genotypesNode : jsonElem.getAsJsonObject().get("genotypes").getAsJsonArray()) {
			JsonElement phenotypeNode = genotypesNode.getAsJsonObject().get("phenotype");
			JsonElement genotypeNode = genotypesNode.getAsJsonObject().get("calls");
			JsonElement geneNode = genotypesNode.getAsJsonObject().get("gene");

			String genotypeString = arrayNodeToString(genotypeNode);
			String phenotypeString = arrayNodeToString(phenotypeNode);
			
			if(!Strings.isNullOrEmpty(phenotypeString) && !Strings.isNullOrEmpty(genotypeString)) {
				ORU_R01_OBSERVATION obxPhenotype = result.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(i);
				obxPhenotype.getOBX().getObx1_SetIDOBX().setValue(Integer.toString(i2)); // start with 1 instead of 0
				obxPhenotype.getOBX().getValueType().setValue("CE");
				obxPhenotype.getOBX().getObservationIdentifier().getCe1_Identifier().setValue(geneNode.getAsString()); // LOINC Gene Studied ID
				obxPhenotype.getOBX().getObservationIdentifier().getCe2_Text().setValue(geneNode.getAsString() + " Phänotyp");
				obxPhenotype.getOBX().getObservationIdentifier().getCe3_NameOfCodingSystem().setValue("2.16.840.1.113883.6.281"); // oid = HGNC
				obxPhenotype.getOBX().getObservationSubId().setValue("1");
				obxPhenotype.getOBX().getDateTimeOfTheAnalysis().getTimeOfAnEvent().setValue(analysisDate);

				CE ce = new CE(result);
				ce.getCe1_Identifier().setValue(mappingMap.getMetabolizerCodes().get(phenotypeString));
				ce.getCe2_Text().setValue(phenotypeString);
				ce.getCe3_NameOfCodingSystem().setValue("LN");
				obxPhenotype.getOBX().getObservationValue(0).setData(ce);
				
				obxPhenotype.getOBX().getObservationResultStatus().setValue("F");
				
				ORU_R01_OBSERVATION obxGenotype = result.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(i+1);
				obxGenotype.getOBX().getObx1_SetIDOBX().setValue(Integer.toString(i2));
				obxGenotype.getOBX().getValueType().setValue("ST");
				obxGenotype.getOBX().getObservationIdentifier().getCe1_Identifier().setValue(geneNode.getAsString() + "_Genotyp"); // LOINC Drug metab seq var interp-Imp
				obxGenotype.getOBX().getObservationIdentifier().getCe2_Text().setValue(geneNode.getAsString() + " Genotyp");
				obxGenotype.getOBX().getObservationSubId().setValue("2");
				obxGenotype.getOBX().getDateTimeOfTheAnalysis().getTimeOfAnEvent().setValue(analysisDate);

				ST st = new ST(result);
				st.setValue(genotypeString);
				obxGenotype.getOBX().getObservationValue(0).setData(st);
				
				obxGenotype.getOBX().getObservationResultStatus().setValue("F");
				
				i += 2;
				i2 += 1;
			}
		}
		
		
		System.out.println(result.encode());
		return result;
	}

	private String arrayNodeToString(JsonElement node) {
		List<String> lines = new ArrayList<String>();
		node.getAsJsonArray().forEach(n -> {
			if (!n.getAsString().equals("N/A")) {
				lines.add(n.getAsString());
			}
		});
		String s = String.join(";", lines);
		return s;
	}

	public void setMappingMap(MappingProperties mappingMap) {
		this.mappingMap = mappingMap;
	}



	public void setContext(HapiContext context) {
		this.context = context;
	}
	
	
}
