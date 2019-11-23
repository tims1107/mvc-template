package com.spectra.model;

import ca.uhn.hl7v2.model.AbstractMessage;


import java.util.Map;

public interface HL7Message {
	AbstractMessage getMessage(PatientRecord patientRecord, Map<String, Object> paramMap);
}
