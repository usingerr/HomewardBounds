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
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class realTime {

	public static void main(String[] args) throws Exception {

        String roomNum = args[0];
        int result = Integer.parseInt(roomNum);
		Classifier[] models = new Classifier[result];

		String first="";
		int x;
        for (int i=0;i<result;i++){
			x=i+1;
			Classifier dTree = (Classifier) weka.core.SerializationHelper.read("/home/pi/Documents/rooms/Room"+Integer.valueOf(x)+".model");
			models[i] = dTree;
			dTree = null;
		}
//waiting for it to start populating
		while(first.length()<1){
    	    FileReader file = new FileReader("realtime.arff");
			first =  readFile("realtime.arff",Charset.defaultCharset());
			file.close();
		}
		//WHILE LOPP START
        int metaCounter=0;
        int metaCounter2=0;
        int[] count= new int[result];
		int alt=0;
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

		while(true){
	        FileReader file2 = new FileReader("realtime.arff");
			String second =  readFile("realtime.arff",Charset.defaultCharset());
			file2.close();
			if (!first.equals(second) && second.length()>1){
				first = second;
				Instances unlabeled = null;
				for (int h=0;h<count.length;h++){
					count[h]=0;
				}
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
		        for (int i = 0; i < unlabeled.numInstances(); i++) {
					//add em up
					double clsLabel = 0;
					for (int j=0;j<result;j++){
		            	clsLabel = models[j].classifyInstance(unlabeled.instance(i));
		            	//clsLabel is 1.0 (y) or 0.0 (n)
			        	if (clsLabel >= 1.0){
			        	    //System.out.println("Room"+Integer.valueOf(j)+"NO");
			        	}
			        	else{
							count[j]++;
			            	//System.out.println("Room"+Integer.valueOf(j)+"YES");
			        	}
					}
				}
				for(int k=0;k<result;k++){
					if (count[k] < 5){
	                    System.out.println("Room"+Integer.valueOf(k+1)+": SAFE");
	                    metaCounter=0;
                        metaCounter2=0;
						pin.low();
					}
					else if (count[k]==5 && metaCounter<3){
		                System.out.println("Room"+Integer.valueOf(k+1)+": BEEP");
						metaCounter++;
                        metaCounter2++;
						pin.high();
					}
					else if (count[k]==5 && metaCounter==3){
	                    System.out.println("Room"+Integer.valueOf(k+1)+": ZAP");
	                    metaCounter++;                        
						metaCounter2++;
						pin.high();
					}
					else{
	                    System.out.println("Room"+Integer.valueOf(k+1)+": FORCE STOP ZAP");
						//pin.low();
					}

				}
				if (alt ==0){
	                System.out.println("- -------------------------------------------");
					alt++;
				}
				else{
					alt--;
					System.out.println("+ -------------------------------------------");
				}
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
