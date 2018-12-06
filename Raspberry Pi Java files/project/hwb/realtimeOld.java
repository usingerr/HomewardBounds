package com.HomewardBounds.app;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import java.nio.charset.*;
import java.nio.file.*;

public class realTime {

	public static void main(String[] args) throws Exception {

        String roomNum = args[0];
        int result = Integer.parseInt(roomNum);

		String first="";
		Classifier dTree = (Classifier) weka.core.SerializationHelper.read("/home/pi/Documents/rooms/Room1.model");
//waiting for it to start populating
		while(first.length()<1){
    	    FileReader file = new FileReader("realtime.arff");
			first =  readFile("realtime.arff",Charset.defaultCharset());
			file.close();
		}
		//WHILE LOPP START
        int metaCounter=0;
		while(true){
	        FileReader file2 = new FileReader("realtime.arff");
			String second =  readFile("realtime.arff",Charset.defaultCharset());
			file2.close();
			if (!first.equals(second) && second.length()>1){
				first = second;
				Instances unlabeled = null;
				try{
		        	unlabeled = new Instances(new BufferedReader(new FileReader("realtime.arff")));
				}
				catch(IOException e){
					continue;
				}
		        // set class attribute
		        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
		        // create copy
		        Instances labeled = new Instances(unlabeled);
		        // label instances
				int count=0;
		        for (int i = 0; i < unlabeled.numInstances(); i++) {
		            double clsLabel = dTree.classifyInstance(unlabeled.instance(i));
		            //clsLabel is 1.0 (y) or 0.0 (n)
			        if (clsLabel == 1.0){
			            System.out.println("NO");
			        }
			        else{
						count++;
			            System.out.println("YES");
			        }
				}
				if (count < 5){
                    System.out.println("SAFE");
                    metaCounter=0;
				}
				else if (count==5 && metaCounter<3){
	                System.out.println("BEEP");
					metaCounter++;
				}
				else if (count==5 && metaCounter==3){
                    System.out.println("ZAP");
                    metaCounter++;
				}
				else{
                    System.out.println("FORCE STOP ZAP");
				}

		        System.out.println("----------------------------------");
			}
		
		}
		//END
	}

static String readFile(String path, Charset encoding) 
  throws IOException 
{
  byte[] encoded = Files.readAllBytes(Paths.get(path));
  return new String(encoded, encoding);
}
}
