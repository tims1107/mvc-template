package com.spectra.model;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.parser.CustomModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.log4j.Logger;

import java.util.Map;

public class HL7v23Creator extends AbstractHL7Creator {
	private Logger log = Logger.getLogger(HL7v23Creator.class);
	
	@Override
	public AbstractMessage getMessage(PatientRecord patientRecord, Map<String, Object> paramMap) throws HL7Exception {
		AbstractMessage msg = null;
		if((patientRecord != null) && (paramMap != null)){
			HL7Message hl7Msg = HL7MessageFactory.getHL7v23Message();
			msg = hl7Msg.getMessage(patientRecord, paramMap);
		}
		return msg;
	}

	@Override
	public String getPipedHL7Message(AbstractMessage hl7Message, Map<String, Object> paramMap) throws HL7Exception {
		String hl7Str = null;
		if((hl7Message != null) && (paramMap != null)){
			String hapiCustomPackage = (String)paramMap.get("hapiCustomPackage");
			ModelClassFactory cmf = new CustomModelClassFactory(hapiCustomPackage);
			HapiContext context = new DefaultHapiContext();
			context.setModelClassFactory(cmf);

			Parser parser = context.getPipeParser();

			hl7Str = parser.encode(hl7Message);
		}
		return hl7Str;
	}

	@Override
	public String getXmlHL7Message(AbstractMessage hl7Message, Map<String, Object> paramMap) throws HL7Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
