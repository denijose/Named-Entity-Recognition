/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neTag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author denis
 */
public class NETag {
    
    private static String CURRENT;
    private static String TEMPDIR;
    private static String TESTFILENAME = "/home/denis/Projects/NLP_HW2/NER/ner.esp.blind.test";
    private static String TESTFILENAMEFORMEGAM = "/ne.test";
    private static String MODELFILENAME;
    
    private static String pne;

    
    public static void main(String[] args) throws IOException{
        pne = "NONE";
        parseArgs(args);
        createTestFileForMegaM();
        callMegaM(TESTFILENAMEFORMEGAM);
    }
    
        public static void parseArgs(String[] args) {
              if(args.length < 1){
             System.out.println("Insufficient arguments. Usage -");
             System.out.println("java -jar netag.jar MODEL");
             System.exit(1);
       }
       MODELFILENAME = args[0];
    }

    private static void createTestFileForMegaM() throws FileNotFoundException, IOException {
        CURRENT = new File( "/home/denis/Projects/NLP_HW2/megam_0.92" ).getCanonicalPath();
        TEMPDIR = CURRENT + "/denisjos_temp";
        File tempDir = new File(TEMPDIR);
        if(!tempDir.exists())
            tempDir.mkdir();
        //System.out.println("Please kill the process at the shell or will exist at the end of the inputstream automatically...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader br = new BufferedReader(new FileReader(TESTFILENAME));
            
        File testFileForMegaM = new File(tempDir.getAbsolutePath()+TESTFILENAMEFORMEGAM);
                
        String line = null;       
        while((line=br.readLine()) != null){
//            if(line.equalsIgnoreCase("1")){
//                 System.out.println("Exiting...");
//                break;
//            }   
            if(testFileForMegaM.exists())
                 testFileForMegaM.delete();
            testFileForMegaM.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(testFileForMegaM.getAbsolutePath()));

            String nw = null;
            String pw1 = "Start", pw2 = "Start"; 
            String pt1 = "None", pt2 = "None";
            String nt = "None";
            String pne = null;
            String[] components = line.split(" ");
            for(int i=0; i<components.length; i++){
                                String word = components[i].split("/")[0];
                if(word.equalsIgnoreCase("#"))
                    word = "HASH";
                String posTag =  components[i].split("/")[1];
                if(i==components.length-1){
                    nw="End";
                    nt="None";
                }    
                else{
                    nw = components[i+1].split("/")[0];
                    nt = components[i+1].split("/")[1];
                }    
                String trainLine ="0 w:" + word + " t:" + posTag + " pne:" + pne  +" pt1:"+pt1 + " nt:" + nt +" pw1:" + pw1 + " pw2:" + pw2 + " nw:" + nw;
                bw.write(trainLine);
                //if(!(i==components.length-1))
                    bw.write("\n");
                pw2=pw1;
                pw1=word;
                pt2=pt1;
                pt1=posTag;
                //pne=ne;
            }
            bw.close();
            //System.out.println("Calling MegaM to tag...");
            callMegaM(TESTFILENAMEFORMEGAM);
            finalOutput(line,false);
        }
        br.close();
        //bw.close();
    }

    private static void callMegaM(String fileName) {        
        try{
            String cmd = CURRENT + "/megam.opt " + "-predict " + MODELFILENAME + "  -nc multitron " + TEMPDIR + "/" +fileName;
            Process pr = Runtime.getRuntime().exec(cmd);
            InputStream stdin = pr.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            //System.out.println("");
            
            File outPutFile = new File(TEMPDIR + "/out");
            if(outPutFile.exists())
                outPutFile.delete();
            outPutFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPutFile.getAbsolutePath()));
            while ( (line = br.readLine()) != null)
                bw.write(line+ "\n");
           
            int exitVal = pr.waitFor();
            //System.out.println("Process exitValue: " + exitVal);
            bw.close();
            br.close();
        } catch (IOException | InterruptedException t){
            t.printStackTrace();
        }
    }

    private static void finalOutput(String line, boolean write) throws FileNotFoundException, IOException {
            String[] components = line.split(" ");            
            BufferedReader br = new BufferedReader(new FileReader(TEMPDIR + "/out"));
            String outputLineforMega = new String();
            String outputLineforSysOut = new String();
            String line2;
            int i=0;
            while( (line2=br.readLine()) != null){
                String tag = line2.split("\t")[0];
                pne = tag;                
                if(components[i].split("/").length == 2)
                    outputLineforMega += components[i++]+"/"+tag + " ";
                else{
                    String one = components[i].split("/")[0];
                    String two = components[i++].split("/")[1];
                   outputLineforSysOut += one + "/" + two + "/" + tag + " ";
                  }
                    
            }
            if(write)
                System.out.println(outputLineforSysOut);
             else
                onceMore(outputLineforMega);
           
               br.close();
        
    }
    
    


private static void onceMore(String line) throws IOException{
        File testFileForMegaM2 = new File(TEMPDIR+"/"+TESTFILENAMEFORMEGAM+"2");
         if(testFileForMegaM2.exists())
                 testFileForMegaM2.delete();
         testFileForMegaM2.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(testFileForMegaM2.getAbsolutePath()));
            
            String nw = null;
            String pw1 = "Start", pw2 = "Start"; 
            String pt1 = "None", pt2 = "None";
            String nt = "None";
            String pne = "Start";
            String ne;
            String[] components = line.split(" ");
            for(int i=0; i<components.length; i++){
                String word = components[i].split("/")[0];
                if(word.equalsIgnoreCase("#"))
                    word = "HASH";
                String posTag =  components[i].split("/")[1];
                ne = components[i].split("/")[2];
                if(i==components.length-1){
                    nw="End";
                    nt="None";
                }    
                else{
                    nw = components[i+1].split("/")[0];
                    nt = components[i+1].split("/")[1];
                }    
                String trainLine ="0 w:" + word + " t:" + posTag + " pne:" + pne  +" pt1:"+pt1 + " nt:" + nt +" pw1:" + pw1 + " pw2:" + pw2 + " nw:" + nw;
                bw.write(trainLine);
                //if(!(i==components.length-1))
                    bw.write("\n");
                pw2=pw1;
                pw1=word;
                pt2=pt1;
                pt1=posTag;
                pne=ne;
           
            
    }           
            bw.close();
            callMegaM(TESTFILENAMEFORMEGAM+"2");
            finalOutput(line,true);
    }
}