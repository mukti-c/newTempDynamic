package protego.com.protegomaximus;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Tranny {

    static String trainingSet = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "kddreduced.arff";
    //String csvFile = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "connection.csv";
    static String modelFile = Environment.getExternalStorageDirectory().getAbsolutePath()  + File.separator + "model.txt";
    static Instances instances;
    //FilteredClassifier classifier = new FilteredClassifier();
    static AdaBoostM1 classifier = new AdaBoostM1();

    public Tranny() {

    }

    //Builds Classifier
    public int build() {
        int flag = 0;
        Instances traindata = null;

        ArffLoader loader = new ArffLoader();
        try {
            loader.setFile(new File(trainingSet));
            traindata = loader.getDataSet();
            traindata.setClassIndex(traindata.numAttributes() - 1);
        } catch (IOException e) {
            flag = 1;
            e.printStackTrace();
        }

        try {
            classifier.buildClassifier(traindata);
        } catch (Exception e) {

            flag = 2;
            e.printStackTrace();
        }

        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(modelFile));
            out.writeObject(classifier);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Getting the source of the trained model
        String fpathnew = "/sdcard/finalmodel.txt";
        File f = new File(fpathnew);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            try {
                bw.write(classifier.toSource("AdaBoostM1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flag;
    }

    //Evalutes the built Classifier model
    public String evaluate () {

        String [] options = new String[2];
        options[0] = "-t";
        options[1] = trainingSet;

        String out = null;

        try {
            out = Evaluation.evaluateModel(classifier, options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    //Classifies data
    public static String classify(String csv) {

        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new FileInputStream(modelFile));
            try {
                Object tmp = in.readObject();
                classifier = (AdaBoostM1) tmp;
                in.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String out = "";

        ArffLoader arff = new ArffLoader();
        try {
            arff.setFile(new File("/sdcard/"+"kddreduced.arff"));
            instances = arff.getStructure();

            instances.setClassIndex(instances.numAttributes()-1);

            DenseInstance instance = new DenseInstance(29);
            instance.setDataset(instances);

            String [] stringvalues = csv.split(",");

            instance.setValue(0, Double.parseDouble(stringvalues[0]));
            instance.setValue(1, stringvalues[1]);
            instance.setValue(2, stringvalues[2]);
            instance.setValue(3, stringvalues[3]);
            instance.setValue(4, Double.parseDouble(stringvalues[4]));
            instance.setValue(5, Double.parseDouble(stringvalues[5]));
            instance.setValue(6, Double.parseDouble(stringvalues[6]));
            instance.setValue(7, Double.parseDouble(stringvalues[7]));
            instance.setValue(8, Double.parseDouble(stringvalues[8]));
            instance.setValue(9, Double.parseDouble(stringvalues[9]));
            instance.setValue(10, Double.parseDouble(stringvalues[10]));
            instance.setValue(11, Double.parseDouble(stringvalues[11]));
            instance.setValue(12, Double.parseDouble(stringvalues[12]));
            instance.setValue(13, Double.parseDouble(stringvalues[13]));
            instance.setValue(14, Double.parseDouble(stringvalues[14]));
            instance.setValue(15, Double.parseDouble(stringvalues[15]));
            instance.setValue(16, Double.parseDouble(stringvalues[16]));
            instance.setValue(17, Double.parseDouble(stringvalues[17]));
            instance.setValue(18, Double.parseDouble(stringvalues[18]));
            instance.setValue(19, Double.parseDouble(stringvalues[19]));
            instance.setValue(20, Double.parseDouble(stringvalues[20]));
            instance.setValue(21, Double.parseDouble(stringvalues[21]));
            instance.setValue(22, Double.parseDouble(stringvalues[22]));
            instance.setValue(23, Double.parseDouble(stringvalues[23]));
            instance.setValue(24, Double.parseDouble(stringvalues[24]));
            instance.setValue(25, Double.parseDouble(stringvalues[25]));
            instance.setValue(26, Double.parseDouble(stringvalues[26]));
            instance.setValue(27, Double.parseDouble(stringvalues[27]));

            instances.add(instance);

        } catch (IOException e) {
            e.printStackTrace();
        }

        double pred = 0;
        try {
            pred = classifier.classifyInstance(instances.instance(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        out = instances.classAttribute().value((int) pred);

        return out;
    }

}