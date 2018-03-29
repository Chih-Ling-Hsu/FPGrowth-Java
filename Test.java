import java.lang.*;
import java.util.*;
import java.io.*;

public class Test{
    public static void main(String[] args){
        long start, end;        
		Runtime instance = Runtime.getRuntime();    // get Runtime instance        

        FPGrowth ap = new FPGrowth(args);

        instance.gc();
        start = System.nanoTime();        
        Map<String, Integer> freqItemsets = ap.findFreqItemsets();
        end = System.nanoTime();
        

        System.out.println("\n\nList of frequent itemsets found");
        System.out.println("*******************************");
        for(String itemset: freqItemsets.keySet()){
            System.out.println("["+(itemset)+"]" + ", Support Count: " + freqItemsets.get(itemset));
        }

        System.out.println("------------------------------------");        
        System.out.println("Number of frequent itemsets found: " + freqItemsets.size());
        System.out.println("Execution time is: "+((double)(end-start)/1000000000) + " seconds.");

        checkMemory();

        if(args.length<3){
            return;
        }

        
        instance.gc();
        start = System.nanoTime();     
        Map<String, Double> associationRules = ap.findAssociationRules();
        end = System.nanoTime();


        System.out.println("\n\nList of association rules found");
        System.out.println("*******************************");
        for(String rule: associationRules.keySet()){
            System.out.println(rule + ", Confidence: " + associationRules.get(rule));
        }

        System.out.println("------------------------------------");    
        System.out.println("Number of association rules found: " + associationRules.size());    
        System.out.println("Execution time is: "+((double)(end-start)/1000000000) + " seconds.");
        
        checkMemory();
    }

    public static void checkMemory(){
 
		//System.out.println("\n***** Heap utilization statistics [MB] *****");

		Runtime instance = Runtime.getRuntime();    // get Runtime instance      
        int mb = 1024 * 1024;  

		// available memory
		//System.out.println("Total Memory: " + (double) instance.totalMemory() / mb);
 
		// free memory
		//System.out.println("Free Memory: " + (double) instance.freeMemory() / mb);
 
		// used memory
		System.out.println("Used Memory: "
				+ (double) (instance.totalMemory() - instance.freeMemory()) / mb);
 
		// Maximum available memory
		//System.out.println("Max Memory: " + (double) instance.maxMemory() / mb);

    }
}