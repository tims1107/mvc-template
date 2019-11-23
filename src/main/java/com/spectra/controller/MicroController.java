package com.spectra.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.spectra.model.HL7;
import com.spectra.model.HL7Listener;
import com.spectra.model.OracleDAO;
import config.Configure;
import connections.ConcreteDAO;
import connections.ItemURL;
import connections.TestURL;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class MicroController {

    private static int count = 0;

    private static LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    private static Logger logger = lc.getLogger("Controller");

    private final Map<String, ItemURL> map;

    private OracleDAO oracledao;
    private HL7 hl7gen;
    private final Set<String> newset = new HashSet<String>();
    private final Map<String, TestURL> testmap;


    public MicroController(String [] args) {
        map = (Map<String, ItemURL>) Configure.JsonConfigure(getClass().getClassLoader().getResourceAsStream(args[0]),ItemURL.class);
        testmap = (Map<String, TestURL>) Configure.JsonConfigure(getClass().getClassLoader().getResourceAsStream("test.json"),TestURL.class);

        ItemURL url = map.get("oracle_proxy_ihub_prod");
        TestURL url2 = testmap.get("oracle_eleveng");


        System.out.println(url2.getUri());

//        oracledao = new OracleDAO(new ConcreteDAO(map.get("oracle_proxy_ihub_dev")));
        oracledao = new OracleDAO(new ConcreteDAO(map.get("oracle_proxy_ihub_prod")));

        hl7gen = new HL7();
        setListeners();

    }

    private void setListeners() {


        hl7gen.setListener(new HL7Listener() {
            @Override
            public void m_ListenerSet(String msg) {

            }

            @Override
            public void getSet(String val) {
                newset.add(val);
            }


        });
    }

    public void process(int runprocess){



        List<String> filelist = new ArrayList<String>();

        File dir []  = {new File("U:/HLAB Results Archive/NYS/2019_06"),new File("U:/HLAB Results Archive/NYS/2019_07"),
                new File("U:/HLAB Results Archive/NYS/2019_08"),new File("U:/HLAB Results Archive/NYS/2019_09"),
                new File("U:/HLAB Results Archive/NYS/2019_10"),new File("U:/HLAB Results Archive/NYS/2019_01"),
                new File("U:/HLAB Results Archive/NYS/2019_02"),new File("U:/HLAB Results Archive/NYS/2019_03"),
                new File("U:/HLAB Results Archive/NYS/2019_04"),new File("U:/HLAB Results Archive/NYS/2019_05")};

        File njarchive []  = {new File("P:/2019_10")};


        File files [] = null;
        int count = 1;

        try
        {
            switch (runprocess){
                case 4: // Get ASR results activity
                    oracledao.Connect();
                    //oracledao.m_selActivityByDate("13-NOV-19 21.00.00.000000000 PM");
                    hl7gen.m_Serialize();
                    oracledao.disconnect();
                    System.exit(0);
                    break;
                case 3: // Parse New Jersey sent files
                    for(File dirfile : njarchive) {

                        if(dirfile.exists()) {
                            for (File file : dirfile.listFiles()) {
                                count++;
                                hl7gen.m_ExtractNJHL7(file.getAbsolutePath());
                            }
                        }

                    }

                    System.out.println("====================================================");
                    System.out.println(count);
                    for (String s : newset){
                        System.out.println(s);
                    }
                    break;
                case 1:

                    for(File dirfile : dir) {

                        if(dirfile.exists()) {
                            for (File file : dirfile.listFiles()) {
                                count++;
                                hl7gen.m_ExtractHL7(file.getAbsolutePath());
                            }
                        }

                    }

                    System.out.println("====================================================");
                    System.out.println(count);
                    for (String s : newset){
                        System.out.println(s);
                    }
                    break;


                case 2:
                    logger.error("Running select activity");
                    oracledao.Connect();
                    oracledao.selActivity();
                    break;
                default:
                    System.out.println("No process selected");

            }


            System.exit(0);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            oracledao.disconnect();

        }

    }
}
