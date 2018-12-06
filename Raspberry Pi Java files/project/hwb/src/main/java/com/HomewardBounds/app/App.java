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

public class App {

	public static void main(String[] args) throws Exception {
//open all yesses and add them to no's
        String roomNum = args[0];
		int result = Integer.parseInt(roomNum);

		for (int i=1;i<=result;i++){
//create file for training, both yes and no
			File file = new File ("./rooms/combined"+Integer.valueOf(i)+".arff");
            System.out.println(i);
			if (file.createNewFile()){
				System.out.println("File is created!");
			}
			else{
				System.out.println("File already exists.");
			}
//open new file for writing
			BufferedWriter writer = null;
	        try {
	            writer = new BufferedWriter(new FileWriter("./rooms/combined"+Integer.valueOf(i)+".arff", true));
                System.out.println("Opened combined for writing\n");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
//open YES file for reading
			BufferedReader readerYES = null;
			String fileName= "./rooms/trainingYES"+Integer.valueOf(i)+".arff";
			try {
                System.out.println("open yes\n");

        	    readerYES = new BufferedReader(new FileReader(fileName));
        	} 
			catch (Exception e) {
                System.out.println("open yes fail\n");

        	    e.printStackTrace();
        	}
//open NO file for reading
			BufferedReader readerNO = null;
            String fileName2= "./rooms/trainingNO"+Integer.valueOf(i)+".arff";
            try {
                System.out.println("open no\n");

                readerNO = new BufferedReader(new FileReader(fileName2));
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("open no fail\n");
            }

			String str;
//write NO data
			while ((str = readerNO.readLine()) != null) {
                writer.write("\n"+str);
                System.out.println(str);
            };
			readerNO.close();
//write YES data
			while ((str = readerYES.readLine()) != null) {
   	 	        writer.write("\n"+str);
   	 	    };
			readerYES.close();
			writer.close();
			System.out.println("wrote to\n");
            BufferedReader trainingReader  = null;
	        try {
	            trainingReader = new BufferedReader(new FileReader("./rooms/combined"+Integer.valueOf(i)+".arff"));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        Instances dataAll = new Instances(trainingReader);

			trainingReader.close();
			dataAll.setClassIndex(dataAll.numAttributes() - 1);
			
			J48 dTree = new J48();
		
			dTree.buildClassifier(dataAll);
		
			// save labeled data
			weka.core.SerializationHelper.write("/home/pi/Documents/rooms/Room"+Integer.valueOf(i)+".model", dTree);
		}
	}

}
