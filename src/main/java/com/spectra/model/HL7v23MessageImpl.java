package com.spectra.model;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.VersionLogger;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v23.datatype.SN;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.PID;


import ca.uhn.hl7v2.util.XMLUtils;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.spectra.utils.ApplicationProperties;

import com.spectra.v23.group.ZRU_R01_ORDER_OBSERVATION;
import com.spectra.v23.group.ZRU_R01_RESPONSE;
import com.spectra.v23.message.ZRU_R01;
import com.spectra.v23.segment.ZLR;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HL7v23MessageImpl  {
	private static LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	private static Logger log = lc.getLogger("OracleSQL Dao");



	public AbstractMessage getMessage(PatientRecord patientRecord, Map<String, Object> paramMap) {
		//AbstractMessage msg = null;
		ZRU_R01 zru = null;

		if(patientRecord != null){
			String performingLabId = patientRecord.getPerformingLabId();
			Integer labFk = patientRecord.getLabFk();

			String fileSendingFacility = null;
			String sendingApplication = null;
			String sendingFacilityId = null;

			String mshSendingApplication = (String)paramMap.get("mshSendingApplication");
			String mshFileSendingFacility = (String)paramMap.get("mshFileSendingFacility");
			String mshSendingFacilityId = (String)paramMap.get("mshSendingFacilityId");
			String sendingFacilityIdType = (String)paramMap.get("sendingFacilityIdType");
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
			String infoSpectraAddress = (String)paramMap.get("infoSpectraAddress");
			String infoSpectraAddress2 = (String)paramMap.get("infoSpectraAddress2");
			String infoSpectraCity = (String)paramMap.get("infoSpectraCity");
			String infoSpectraState = (String)paramMap.get("infoSpectraState");
			String infoSpectraZip = (String)paramMap.get("infoSpectraZip");
			String medicalDirector = (String)paramMap.get("medicalDirector");

			if((labFk.intValue() == 5) && (performingLabId.indexOf("SW") != -1)){
				//sendout west address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.west.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.west.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.west.msh.4.2");
			}else if((labFk.intValue() == 5) && (performingLabId.indexOf("SE") != -1)){
				//east address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.east.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.east.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.east.msh.4.2");
			}else if((labFk.intValue() == 5) && (performingLabId.indexOf("OUTSEND") != -1)){
				//east address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.east.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.east.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.east.msh.4.2");
			}

			if((labFk.intValue() == 6) && (performingLabId.indexOf("SE") != -1)){
				//sendout east address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.east.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.east.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.east.msh.4.2");
			}else if((labFk.intValue() == 6) && (performingLabId.indexOf("SW") != -1)){
				//west address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.west.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.west.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.west.msh.4.2");
			}else if((labFk.intValue() == 6) && (performingLabId.indexOf("OUTSEND") != -1)){
				//west address
				fileSendingFacility = ApplicationProperties.getProperty("spectra.west.fhs.4");
				sendingApplication = ApplicationProperties.getProperty("spectra.west.msh.3");
				sendingFacilityId = ApplicationProperties.getProperty("spectra.west.msh.4.2");
			}

			HapiContext context = new DefaultHapiContext();

			//ZRU_R01 zru = new ZRU_R01();
			System.out.println(VersionLogger.getVersion());
			zru = new ZRU_R01();
			try{
				zru.initQuickstart("ORU", "R01", "P");

				MSH msh = zru.getMSH();

				msh.getSendingApplication().getNamespaceID().setValue(mshSendingApplication); //EAST
				msh.getSendingFacility().getNamespaceID().setValue(mshFileSendingFacility); //Spectra East
				msh.getSendingFacility().getUniversalID().setValue(mshSendingFacilityId); //31D0961672

				msh.getSendingFacility().getUniversalIDType().setValue(sendingFacilityIdType); //CLIA
				msh.getReceivingFacility().getNamespaceID().setValue(patientRecord.getCid()); //cid
				msh.getReceivingFacility().getUniversalID().setValue(patientRecord.getMhsOrderingFacId()); //Mhs_ordering_fac_id
				String timeOfAnEvent = dateFormat.format(new Date());
				msh.getDateTimeOfMessage().getTimeOfAnEvent().setValue(timeOfAnEvent);

				msh.getMessageControlID().setValue(patientRecord.getAccessionNo());

				ZRU_R01_RESPONSE response = zru.getRESPONSE();
				//log.debug("getMessage(): response: " + (response == null ? "NULL" : response.getName()));
				ORU_R01_PATIENT patient = response.getPATIENT();
				//log.debug("getMessage(): patient: " + (patient == null ? "NULL" : patient.toString()));
				//log.debug("getMessage(): patient: " + (patient == null ? "NULL" : patient.getName()));
				PID pid = patient.getPID();
				//log.debug("getMessage(): pid: " + (pid == null ? "NULL" : pid.getName()));

				pid.getSetIDPatientID().setValue("1");
				pid.getPatientIDExternalID().getID().setValue(patientRecord.getMrnId()); //Mrn_id
				pid.getPatientIDInternalID(0).getID().setValue(patientRecord.getAltPatientId()); //patientrecord.getAlt_patient_id
				String patientName = patientRecord.getPatientName();
				log.debug("getMessage(): patientName: " + (patientName == null ? "NULL" : patientName));
				if(StringUtils.contains(patientName, "^")){
					String[] nameArray = StringUtils.split(patientName, "\\^");
					if((nameArray != null) && (nameArray.length >= 2)){
						log.debug("getMessage(): nameArray[0]: " + (nameArray[0] == null ? "NULL" : nameArray[0]));
						log.debug("getMessage(): nameArray[1]: " + (nameArray[1] == null ? "NULL" : nameArray[1]));
						pid.getPatientName(0).getFamilyName().setValue(nameArray[0]); //last name
						pid.getPatientName(0).getGivenName().setValue(nameArray[1]); // first name
					}
				}else{
					pid.getPatientName(0).getFamilyName().setValue(patientName); //last name
				}
				pid.getMotherSMaidenName().getFamilyName().setValue(patientRecord.getCid()); //cid
				pid.getDateOfBirth().getTimeOfAnEvent().setValue(patientRecord.getDateOfBirth()); //patientrecord.getDate_of_birth
				pid.getSex().setValue(patientRecord.getGender()); //patientrecord.getGender
				pid.getPatientAlias(0).getFamilyName().setValue("T");

				String race = patientRecord.getPatientRace();
				if((race == null) || (race.trim().length() == 0)){
					race = "U";
				}
				pid.getRace().setValue(race); //patientrecord.getPatientRace

				pid.getPatientAddress(0).getStreetAddress().setValue(patientRecord.getPatientAddress1()); //patientrecord.getPatient_address1
				String patientAddress2 = patientRecord.getPatientAddress2();
				if((patientAddress2 != null) && (patientAddress2.length() > 0)){
					//pid.getPatientAddress(1).getStreetAddress().setValue(patientAddress2);
					pid.getPatientAddress(0).getOtherDesignation().setValue(patientAddress2);
				}
				pid.getPatientAddress(0).getCity().setValue(patientRecord.getPatientCity()); //patientrecord.getPatient_city
				pid.getPatientAddress(0).getStateOrProvince().setValue(patientRecord.getPatientState()); //patientrecord.getPatient_state
				pid.getPatientAddress(0).getZipOrPostalCode().setValue(patientRecord.getPatientZip()); //patientrecord.getPatient_zip
				pid.getPhoneNumberHome(0).getAreaCityCode().setValue(patientRecord.getPatientAreaCode());
				pid.getPhoneNumberHome(0).getPhoneNumber().setValue(patientRecord.getPatientPhone());

				String ethnicGroup = patientRecord.getEthnicGroup();
				if((ethnicGroup == null) || (ethnicGroup.trim().length() == 0)){
					ethnicGroup = "U";
				}
				pid.getEthnicGroup().setValue(ethnicGroup); //patientrecord.getEthnicGroup

				OBXRecord obxRec = patientRecord.getObxList().get(0);

				ZRU_R01_ORDER_OBSERVATION orderObservation = ((ZRU_R01_RESPONSE) response).getORDER_OBSERVATION();
				log.debug("getMessage(): orderObservation: " + (orderObservation == null ? "NULL" : orderObservation.getName()));
				OBR obr = orderObservation.getOBR();
				log.debug("getMessage(): obr: " + (obr == null ? "NULL" : obr.getName()));
				obr.getSetIDObservationRequest().setValue("1");
				obr.getPlacerOrderNumber(0).getEntityIdentifier().setValue(patientRecord.getOrderNumber()); //patientrecord.getOrder_number
				obr.getFillerOrderNumber().getEntityIdentifier().setValue(patientRecord.getOrderNumber()); //patientrecord.getOrder_number
				obr.getUniversalServiceIdentifier().getIdentifier().setValue(obxRec.getCompoundTestCode()); //obxrecord.getCompound_test_code
				obr.getUniversalServiceIdentifier().getText().setValue(obxRec.getSeqTestName()); //obxrecord.getSeq_test_name
				obr.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("L");
				obr.getRequestedDateTime().getTimeOfAnEvent().setValue(patientRecord.getSpecimenReceiveDate()); //patientrecord.getSpecimen_receive_date
				obr.getObservationDateTime().getTimeOfAnEvent().setValue(patientRecord.getCollectionDate()); //patientrecord.getCollection_date
				obr.getSpecimenActionCode().setValue("G");
				obr.getSpecimenReceivedDateTime().getTimeOfAnEvent().setValue(patientRecord.getSpecimenReceiveDate()); //patientrecord.getSpecimen_receive_date
				obr.getSpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue(patientRecord.getSpecimenSource()); //patientrecord.getSpecimen_source
				obr.getOrderingProvider(0).getIDNumber().setValue(patientRecord.getProviderId()); //patientrecord.getProvider_id
				String orderingPhyName = patientRecord.getOrderingPhyName();
				log.debug("getMessage(): orderingPhyName: " + (orderingPhyName == null ? "NULL" : orderingPhyName));
				if(StringUtils.contains(orderingPhyName, "^")){
					String[] phyNameArray = StringUtils.split(orderingPhyName, "\\^");
					if((phyNameArray != null) && (phyNameArray.length >= 2)){
						log.debug("getMessage(): nameArray[0]: " + (phyNameArray[0] == null ? "NULL" : phyNameArray[0]));
						log.debug("getMessage(): nameArray[1]: " + (phyNameArray[1] == null ? "NULL" : phyNameArray[1]));
						obr.getOrderingProvider(0).getFamilyName().setValue(phyNameArray[0]); //patientrecord.getOrdering_phy_name
						obr.getOrderingProvider(0).getGivenName().setValue(phyNameArray[1]); //patientrecord.getOrdering_phy_name
					}
				}else{
					obr.getOrderingProvider(0).getFamilyName().setValue(orderingPhyName); //last name
				}
				obr.getOrderCallbackPhoneNumber(0).getAreaCityCode().setValue(patientRecord.getFacilityAreaCode()); //patientrecord.getFacility_area_code
				obr.getOrderCallbackPhoneNumber(0).getPhoneNumber().setValue(patientRecord.getFacilityPhoneNumber()); //patientrecord.getFacility_phone_number
				obr.getResultsRptStatusChngDateTime().getTimeOfAnEvent().setValue(patientRecord.getResRprtStatusChngDtTime()); //patientrecord.getRes_rprt_status_chng_dt_time
				obr.getResultStatus().setValue(patientRecord.getRequisitionStatus()); //patientrecord.getRequisition_status


				ZLR zlr = orderObservation.getZLR();
				log.debug("getMessage(): zlr: " + (zlr == null ? "NULL" : zlr.getName()));
				zlr.getOrderingProviderAddress(0).getStreetAddress().setValue(patientRecord.getFacilityAddress1()); //patientrecord.getFacility_address1
				zlr.getOrderingProviderAddress(0).getOtherDesignation().setValue(patientRecord.getFacilityAddress2());
				zlr.getOrderingProviderAddress(0).getCity().setValue(patientRecord.getFacilityCity());
				zlr.getOrderingProviderAddress(0).getStateOrProvince().setValue(patientRecord.getFacilityState());
				zlr.getOrderingProviderAddress(0).getZipOrPostalCode().setValue(patientRecord.getFacilityZip());
				zlr.getOrderingFacility(0).getOrganizationName().setValue(patientRecord.getFacilityName());
				zlr.getOrderingFacilityAddress(0).getStreetAddress().setValue(patientRecord.getFacilityAddress1());
				zlr.getOrderingFacilityAddress(0).getOtherDesignation().setValue(patientRecord.getFacilityAddress2());
				zlr.getOrderingFacilityAddress(0).getCity().setValue(patientRecord.getFacilityCity());
				zlr.getOrderingFacilityAddress(0).getStateOrProvince().setValue(patientRecord.getFacilityState());
				zlr.getOrderingFacilityAddress(0).getZipOrPostalCode().setValue(patientRecord.getFacilityZip());
				zlr.getOrderingFacilityPhone(0).getAreaCityCode().setValue(patientRecord.getFacilityAreaCode());
				zlr.getOrderingFacilityPhone(0).getPhoneNumber().setValue(patientRecord.getFacilityPhoneNumber());
				zlr.getPatientAge().getNum1().setValue(patientRecord.getPatientAge());


				List<OBXRecord> obxList = patientRecord.getObxList();
				if(obxList != null){
					String isolateName = null;
					int ctr = 0;

/*
					List<OBXRecord> seqObxList = null;
					TreeMap<String, OBXRecord> seqObxMap = new TreeMap<String, OBXRecord>();
					for(OBXRecord obxr : obxList){
						seqObxMap.put((obxr.getSequenceNo() + obxr.getCompoundTestCode() + obxr.getSubTestCode()), obxr);
					}

					if(seqObxMap.size() > 0){
						seqObxList = new ArrayList<OBXRecord>(seqObxMap.values());
					}
*/


					//order obx and nte
					//List<OBXRecord> obxList = patRec.getObxList();
					//log.debug("toHL7Dto(): OBXLIST SIZE: " + (obxList == null ? "NULL" : String.valueOf(obxList.size())));
					//log.debug("toHL7Dto(): OBXLIST: " + (obxList == null ? "NULL" : obxList.toString()));


					//group by order test code
					TreeMap<String, List<OBXRecord>> obxOtcMap = new TreeMap<String, List<OBXRecord>>();
					if(obxList != null){
						for(OBXRecord obx : obxList){
							String otc = obx.getCompoundTestCode();
							if(obxOtcMap.containsKey(otc)){
								List<OBXRecord> obxrList = obxOtcMap.get(otc);
								obxrList.add(obx);
							}else{
								List<OBXRecord> obxrList = new ArrayList<OBXRecord>();
								obxrList.add(obx);
								obxOtcMap.put(otc, obxrList);
							}
						}
						Collection<List<OBXRecord>> valueList = obxOtcMap.values();
						obxList = new ArrayList<>();
						for(List<OBXRecord> value : valueList){
							for(OBXRecord obxr : value){
								obxList.add(obxr);
							}
						}
					}

					TreeMap<Integer, OBXRecord> seqObxMap = new TreeMap<Integer, OBXRecord>();
					if(obxList != null){
						for(OBXRecord obx : obxList){
							Integer seqNo = obx.getSequenceNo();
							if(seqNo != null){
								if(seqObxMap.containsKey(seqNo)){
									Map.Entry<Integer, OBXRecord> lastEntry = seqObxMap.lastEntry();
									Integer lastKey = lastEntry.getKey();
									//seqNo = new Integer(seqNo.intValue() + 1);
									seqNo = new Integer(lastKey.intValue() + 1);
								}
							}else{
								seqNo = new Integer(obxList.size() + 1);
							}
							//seqObxMap.put(obx.getSequenceNo(), obx);
							seqObxMap.put(seqNo, obx);
						}
						obxList = new ArrayList<OBXRecord>(seqObxMap.values());
					}

					List<NTERecord> nteList = patientRecord.getNteList();
					TreeMap<Integer, NTERecord> seqNteMap = new TreeMap<Integer, NTERecord>();
					if(nteList != null){
						for(NTERecord nte : nteList){
							Integer seqNo = nte.getSequenceNo();
							if(seqNo != null){
								if(seqNteMap.containsKey(seqNo)){
									Map.Entry<Integer, NTERecord> lastEntry = seqNteMap.lastEntry();
									Integer lastKey = lastEntry.getKey();
									//seqNo = new Integer(seqNo.intValue() + 1);
									seqNo = new Integer(lastKey.intValue() + 1);
								}
							}else{
								seqNo = new Integer(nteList.size() + 1);
							}
							//seqNteMap.put(nte.getSequenceNo(), nte);
							seqNteMap.put(seqNo, nte);
						}
						nteList = new ArrayList<NTERecord>(seqNteMap.values());
					}
					//end order obx and nte

					//for(int i = 0; i < seqObxList.size(); i++){
					for(int i = 0; i < obxList.size(); i++){
						ctr += 1;

						OBXRecord obxRecord = obxList.get(i);
						NTERecord nteRecord = nteList.get(i);

						if(obxRecord.getSourceOfComment().equalsIgnoreCase("PT-ANALYT") ) {
							isolateName = obxRecord.getSeqResultName().trim();
						}

						ORU_R01_OBSERVATION observation = orderObservation.getOBSERVATION(i);

						String tnr = obxRecord.getTextualNumResult();

						if((obxRecord.getSubTestCode() != null) &&
								(obxRecord.getSeqResultName() != null) &&
								(obxRecord.getTextualNumResult() != null) &&
								(!obxRecord.getTextualNumResult().contains("\n"))){
							OBX obx = observation.getOBX();
							log.debug("toHL7Dto(): obx " + "(" + String.valueOf(i) + "): " + (obx == null ? "NULL" : obx.getName()));
							obx.getSetIDOBX().setValue(String.valueOf(ctr));
							obx.getValueType().setValue("SN");
							if((obxRecord.getLoincCode() != null) && (obxRecord.getLoincCode().length() > 0)){
								obx.getObservationIdentifier().getIdentifier().setValue(obxRecord.getLoincCode());
								obx.getObservationIdentifier().getText().setValue(obxRecord.getLoincName());
								obx.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
							}
							if((obxRecord.getSubTestCode() != null) && (obxRecord.getSubTestCode().length() > 0)){
								StringBuilder builder = new StringBuilder();
								builder.append(obxRecord.getSubTestCode()).append("-1-001");
								obx.getObservationIdentifier().getAlternateIdentifier().setValue(builder.toString());
								obx.getObservationIdentifier().getAlternateText().setValue(obxRecord.getSeqTestName());
								obx.getObservationIdentifier().getNameOfAlternateCodingSystem().setValue("L");
							}
							String testResultType = obxRecord.getTestResultType();

							String op = null;
							String val = null;
							String result = obxRecord.getTextualNumResult();

							if(result.indexOf("<") != -1){
								op = result.substring(0, (result.indexOf("<") + 1));
								val = result.substring((result.indexOf("<") + 1));
							}else if(result.indexOf(">") != -1){
								op = result.substring(0, (result.indexOf(">") + 1));
								val = result.substring((result.indexOf(">") + 1));
							}else if(result.indexOf("<=") != -1){
								op = result.substring(0, (result.indexOf("<=") + 2));
								val = result.substring((result.indexOf("<=") + 2));
							}else if(result.indexOf(">=") != -1){
								op = result.substring(0, (result.indexOf(">=") + 2));
								val = result.substring((result.indexOf(">=") + 2));
							}else if(result.indexOf("=") != -1){
								op = result.substring(0, (result.indexOf("=") + 1));
								val = result.substring((result.indexOf("=") + 1));
							}else{
								val = result;
							}

							if("ST".equals(testResultType)){
								if(op == null){
									String txtNumRes = obxRecord.getTextualNumResult().trim();
									obx.getValueType().setValue("ST");
									ST st = new ST(zru); //obx.testResultType could be ST or NM
									st.setValue(txtNumRes);
									Varies value = obx.getObservationValue(0);
									value.setData(st);


									// add organism name to observation val
									String microOrgName = obxRecord.getMicroOrganismName();
									if(microOrgName != null){
										if((txtNumRes != null) && (txtNumRes.indexOf(microOrgName) == -1)){
											st = new ST(zru); //obx.testResultType could be ST or NM
											st.setValue(microOrgName);
											value = obx.getObservationValue(1);
											value.setData(st);
										}
									}

								}else{
									SN sn = new SN(zru);
									if(op != null){
										sn.getComparator().setValue(op);
									}
									sn.getNum1().setValue("1");
									sn.getSeparatorOrSuffix().setValue(":");
									if(val != null){
										sn.getNum2().setValue(val);
									}
									//sn.getNum2().setValue(obxRecord.getTextualNumResult());
									Varies value = obx.getObservationValue(0);
									value.setData(sn);
								}
							}else if("NM".equals(testResultType)){
								//NM nm = new NM(zru);
								//nm.setValue(obxRecord.getTextualNumResult());
								SN sn = new SN(zru);

								// comment begins
								if(op != null){
									//sn.getComparator().setValue(op);
								}
								//sn.getNum1().setValue("1");
								//sn.getSeparatorOrSuffix().setValue(":");

								// comment ends

								if(val != null){
									//sn.getNum2().setValue(val);
									sn.getNum1().setValue(val);
								}
								//sn.getNum1().setValue(obxRecord.getTextualNumResult());
								Varies value = obx.getObservationValue(0);
								//value.setData(nm);
								value.setData(sn);
							}else{
								//ST st = new ST(zru); //obx.testResultType could be ST or NM
								//st.setValue(obxRecord.getTextualNumResult());
								SN sn = new SN(zru);
								if(op != null){
									sn.getComparator().setValue(op);
								}
								sn.getNum1().setValue("1");
								sn.getSeparatorOrSuffix().setValue(":");
								if(val != null){
									sn.getNum2().setValue(val);
								}
								//sn.getNum1().setValue(obxRecord.getTextualNumResult());
								Varies value = obx.getObservationValue(0);
								//value.setData(st);
								value.setData(sn);
							}

							//obx.getUnits().getText().setValue(obxRecord.getUnits());
							obx.getUnits().getCe1_Identifier().setValue(obxRecord.getUnits());
							obx.getReferencesRange().setValue(obxRecord.getRefRange());
							obx.getAbnormalFlags(0).setValue(obxRecord.getAbnormalFlag());
							obx.getObservResultStatus().setValue(obxRecord.getResultStatus());
							StringBuilder releaseDateTimeBuilder = new StringBuilder();
							releaseDateTimeBuilder.append(obxRecord.getReleaseDate()).append(obxRecord.getReleaseTime().substring(8, 12));



							obx.getDateTimeOfTheObservation().getTimeOfAnEvent().setValue(releaseDateTimeBuilder.toString());
							log.debug("getMessage(): requisition id: " + (patientRecord.getOrderNumber() == null ? "NULL" : patientRecord.getOrderNumber()));
							log.debug("getMessage(): sendingFacilityId: " + (sendingFacilityId == null ? "NULL" : sendingFacilityId));
							log.debug("getMessage(): fileSendingFacility: " + (fileSendingFacility == null ? "NULL" : fileSendingFacility));
							obx.getProducerSID().getIdentifier().setValue(sendingFacilityId); //31D0961672
							obx.getProducerSID().getText().setValue(fileSendingFacility); //Spectra East
							obx.getProducerSID().getNameOfCodingSystem().setValue(sendingFacilityIdType); //CLIA
							obx.getObservationMethod(0).getIdentifier().setValue("FMC");



						}else if(obxRecord.getTextualNumResult().contains("\n")){
							String[] obxTextNumResultArray = obxRecord.getTextualNumResult().split("\n");
							if(obxTextNumResultArray != null) {
								for(int j = 0; j < obxTextNumResultArray.length; j++) {
									if(!(obxTextNumResultArray[j].trim().equalsIgnoreCase(""))) {
										OBX obx = observation.getOBX();
										log.debug("getMessage(): obx " + "(" + String.valueOf(i) + "): " + (obx == null ? "NULL" : obx.getName()));
										obx.getSetIDOBX().setValue(String.valueOf(ctr));
										obx.getValueType().setValue(obxRecord.getTestResultType()); //obxrecord.getTestresult_type
										if((obxRecord.getLoincCode() != null) && (obxRecord.getLoincCode().length() > 0)){
											obx.getObservationIdentifier().getIdentifier().setValue(obxRecord.getLoincCode());
											obx.getObservationIdentifier().getText().setValue(obxRecord.getLoincName());
											obx.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
										}
										if((obxRecord.getSubTestCode() != null) && (obxRecord.getSubTestCode().length() > 0)){
											StringBuilder builder = new StringBuilder();
											builder.append(obxRecord.getSubTestCode()).append("-1-001");
											obx.getObservationIdentifier().getAlternateIdentifier().setValue(builder.toString());
											obx.getObservationIdentifier().getAlternateText().setValue(obxRecord.getSeqTestName());
											obx.getObservationIdentifier().getNameOfAlternateCodingSystem().setValue("L");
										}
										String testResultType = obxRecord.getTestResultType();

										String op = null;
										String val = null;
										//String result = obxRecord.getTextualNumResult();
										String result = obxTextNumResultArray[j];

										if(result.indexOf("<") != -1){
											op = result.substring(0, (result.indexOf("<") + 1));
											val = result.substring((result.indexOf("<") + 1));
										}else if(result.indexOf(">") != -1){
											op = result.substring(0, (result.indexOf(">") + 1));
											val = result.substring((result.indexOf(">") + 1));
										}else if(result.indexOf("<=") != -1){
											op = result.substring(0, (result.indexOf("<=") + 2));
											val = result.substring((result.indexOf("<=") + 2));
										}else if(result.indexOf(">=") != -1){
											op = result.substring(0, (result.indexOf(">=") + 2));
											val = result.substring((result.indexOf(">=") + 2));
										}else if(result.indexOf("=") != -1){
											op = result.substring(0, (result.indexOf("=") + 1));
											val = result.substring((result.indexOf("=") + 1));
										}else{
											val = result;
										}

										if("ST".equals(testResultType)){
											if(op == null){
												//String txtNumRes = obxRecord.getTextualNumResult().trim();
												String txtNumRes = obxTextNumResultArray[j].trim();
												obx.getValueType().setValue("ST");
												ST st = new ST(zru); //obx.testResultType could be ST or NM
												st.setValue(txtNumRes);
												Varies value = obx.getObservationValue(0);
												value.setData(st);


												// add organism name to observation val
												String microOrgName = obxRecord.getMicroOrganismName();
												if(microOrgName != null){
													if((txtNumRes != null) && (txtNumRes.indexOf(microOrgName) == -1)){
														st = new ST(zru); //obx.testResultType could be ST or NM
														st.setValue(microOrgName);
														value = obx.getObservationValue(1);
														value.setData(st);
													}
												}

											}else{
												SN sn = new SN(zru);
												if(op != null){
													sn.getComparator().setValue(op);
												}
												sn.getNum1().setValue("1");
												sn.getSeparatorOrSuffix().setValue(":");
												if(val != null){
													sn.getNum2().setValue(val);
												}
												//sn.getNum2().setValue(obxRecord.getTextualNumResult());
												Varies value = obx.getObservationValue(0);
												value.setData(sn);
											}
										}else if("NM".equals(testResultType)){
											//NM nm = new NM(zru);
											//nm.setValue(obxRecord.getTextualNumResult());
											SN sn = new SN(zru);
											if(op != null){
												sn.getComparator().setValue(op);
											}
											sn.getNum1().setValue("1");
											sn.getSeparatorOrSuffix().setValue(":");
											if(val != null){
												sn.getNum2().setValue(val);
											}
											//sn.getNum1().setValue(obxRecord.getTextualNumResult());
											Varies value = obx.getObservationValue(0);
											//value.setData(nm);
											value.setData(sn);
										}else{
											//ST st = new ST(zru); //obx.testResultType could be ST or NM
											//st.setValue(obxRecord.getTextualNumResult());
											SN sn = new SN(zru);
											if(op != null){
												sn.getComparator().setValue(op);
											}
											sn.getNum1().setValue("1");
											sn.getSeparatorOrSuffix().setValue(":");
											if(val != null){
												sn.getNum2().setValue(val);
											}
											//sn.getNum1().setValue(obxRecord.getTextualNumResult());
											Varies value = obx.getObservationValue(0);
											//value.setData(st);
											value.setData(sn);
										}
										//end result

										obx.getUnits().getText().setValue(obxRecord.getUnits());
										obx.getReferencesRange().setValue(obxRecord.getRefRange());
										obx.getAbnormalFlags(0).setValue(obxRecord.getAbnormalFlag());
										obx.getObservResultStatus().setValue(obxRecord.getResultStatus());
										StringBuilder releaseDateTimeBuilder = new StringBuilder();
										releaseDateTimeBuilder.append(obxRecord.getReleaseDate()).append(obxRecord.getReleaseTime().substring(8, 12));
										obx.getDateTimeOfTheObservation().getTimeOfAnEvent().setValue(releaseDateTimeBuilder.toString());
										log.debug("getMessage(): requisition id: " + (patientRecord.getOrderNumber() == null ? "NULL" : patientRecord.getOrderNumber()));
										log.debug("getMessage(): sendingFacilityId: " + (sendingFacilityId == null ? "NULL" : sendingFacilityId));
										log.debug("getMessage(): fileSendingFacility: " + (fileSendingFacility == null ? "NULL" : fileSendingFacility));
										obx.getProducerSID().getIdentifier().setValue(sendingFacilityId); //31D0961672
										obx.getProducerSID().getText().setValue(fileSendingFacility); //Spectra East
										obx.getProducerSID().getNameOfCodingSystem().setValue(sendingFacilityIdType); //CLIA
										obx.getObservationMethod(0).getIdentifier().setValue("FMC");

										//NTE nte = observation.getNTE();
										//nte.getSetIDNotesAndComments().setValue(String.valueOf(ctr));
										//nte.getSourceOfComment().setValue(obxRecord.getSourceOfComment());
										//nte.getComment(0).setValue(nteRecord.getTestNteComment());

										ctr++;
									}
								}
							}
							ctr--;
						}else{
							ctr -= 1;
						}//if

					}//for
				}//if

/*
				//String hapiCustomPackage = this.configService.getString("hapi.custom.package");
				//ModelClassFactory cmf = new CustomModelClassFactory(ip.getHapiCustomPackage());
				ModelClassFactory cmf = new CustomModelClassFactory(hapiCustomPackage);
				HapiContext context = new DefaultHapiContext();
				context.setModelClassFactory(cmf);

				//HapiContext context = new DefaultHapiContext();
				//HapiContext context = this.createHapiContext();
				Parser parser = context.getPipeParser();
				//hl7String = parser.encode(zru);

				//hl7Builder.append(parser.encode(zru));
				zruBuilder.append(parser.encode(zru));
*/
			}catch(IOException ioe){
				log.error(ioe.getMessage());
				ioe.printStackTrace();
			}catch(HL7Exception hl7e){
				log.error(hl7e.getMessage());
				hl7e.printStackTrace();
			}
		}//if

		return zru;
	}

}
