import java.lang.*;
import java.util.*;
import java.io.*;

public class FPGrowth{

    /** the name of the transcation file */
    private String inputPath;
    /** total number of transactions in transcation file */
    private int numTransactions; 
    /** minimum support for a frequent itemset in percentage, e.g. 0.8 */
    private double minSup; 
    /** minimum confidence for a rule in percentage, e.g. 0.8 */
    private double minConf;
    /** the list of collected frequent itemsets */
    private Map<String, Integer> freqItemsets;
    /** the list of association rules */
    private Map<String, Double> associationRules;
    /** the FP-Growth Tree */
    private FPTree tree; 
    /** list of transactions in transcation file */
    private List<String[]> transactions = new ArrayList<String[]>();
    /** a control variable to decide the Entry construction type in preprocessTransaction */
    private Boolean isMining = false;


    public FPGrowth(String[] args){
        inputPath = args[0];
        minSup = Double.valueOf(args[1]).doubleValue();
        minConf = Double.valueOf(args[2]).doubleValue();
        log("Threshold of Support:" + minSup);
        log("Threshold of Confidence:" + minConf);  
    }

    public Map<String, Double> findAssociationRules(){
        associationRules = new HashMap<String, Double>();
        
        for(String itemset_str: freqItemsets.keySet()){
            String[] itemset = itemset_str.split(",");
            List<String[]> candidates = new ArrayList<String[]>();
            for(String item : itemset){
                candidates.add(new String[] {item});
            }
            genRule(itemset, freqItemsets.get(itemset_str), candidates);
        }
        
        return associationRules;
    }


    /*private Boolean existPattern(TreeNode node, PriorityQueue<Entry> pq){
        if(pq.isEmpty()){
            return true;
        }
        else if(!node.isRoot()){
            Entry entry = pq.poll();
            if(node.getItem().equals(entry.getKey())){
                return existPattern(node.getParent(), pq);
            }
        }
        return false;
    }

    private int countSupport(String[] itemset, String[] order){
        PriorityQueue<Entry> pq = new PriorityQueue<Entry>(Collections.reverseOrder());
        for(String item : itemset){
            pq.add(new Entry(item, 1, order));
        }

        int support_cnt = 0;   
        Entry entry = pq.poll();
        TreeNode link = tree.headerTable.get(entry.getKey());
        while(link!=null){ 
            if(existPattern(link, pq)){        
                support_cnt += link.getCount();
            }
            link = link.getNext();
        }
        return support_cnt;
    }*/

    private List<String[]> genCandidates(List<String[]> itemsets){
        List<String[]> candidates = new ArrayList<String[]>();
        Set<String> candidateStrings = new HashSet<String>();

        for(int i=0; i<itemsets.size(); i++) {
            for(int j=0; j<itemsets.size(); j++) {
                if(i==j) continue;

                String[] X = itemsets.get(i);
                String[] Y = itemsets.get(j);

                int ndifferent = 0;
                String diffItem = "";
                for(int a=0; a<X.length; a++){
                    boolean identical = false;
                    for(int b=0; b<Y.length; b++){
                        if(Y[b].equals(X[a])){
                            identical = true;
                            break;
                        }
                    }
                    if(!identical){
                        ndifferent++;
                        diffItem = X[a];
                    }
                }

                assert(ndifferent>0);

                if (ndifferent==1) {
                    String[] candidate =  new String[Y.length+1];
                    for(int b=0; b<Y.length; b++){
                        candidate[b] = Y[b];
                    }
                    candidate[Y.length] = diffItem;

                	Arrays.sort(candidate);
                    String candidateString = Arrays.toString(candidate);
                    if(candidateStrings.contains(candidateString)) continue;
                    else{
                        candidateStrings.add(candidateString);
                        candidates.add(candidate);
                    }
                }
            }
        }
        return candidates;
    }
    

    private int countSupport(String[] itemset){
        int count = 0;
        for(String[] t : transactions){
            for(int a=0, b=0; b<t.length; b++){
                if(t[b].equals(itemset[a])){
                    a++;
                }
                if(a==itemset.length){
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private void genRule(String[] itemset, int count, List<String[]> candidates){
        int k = itemset.length;
        int m = candidates.get(0).length;

        if(k >= m+1){
            for(int i=candidates.size()-1; i>=0; i--){
                String[] candidate = candidates.get(i);

                Set<String> f = new HashSet<String>(Arrays.asList(itemset));
                Set<String> h = new HashSet<String>(Arrays.asList(candidate));
                f.removeAll(h);
                String[] antecedent = f.toArray(new String[f.size()]);
                Arrays.sort(antecedent);

                double conf = (double)count/countSupport(antecedent);
                if (conf >= minConf){
                    String rule = Arrays.toString(antecedent) +" -> "+ Arrays.toString(candidate);
                    //log(rule + ", Confidence: " + conf);
                    associationRules.put(rule, conf);
                }
                else{
                    candidates.remove(i);
                }
            }
            candidates = genCandidates(candidates);
            if(candidates.size()!=0){
                genRule(itemset, count, candidates);
            }
        }
        return;
    }

    public Map<String, Integer> findFreqItemsets(){       
        /** the list of collected frequent itemsets */
        freqItemsets = new HashMap<String, Integer>();
        /** the list of frequent 1-itemsets */
        Map<String, Integer> freqItems;
        
        freqItems = scanDatasource(); 
        //log("Threshold of Support Count:" + minSup*numTransactions);
        //log("Threshold of Confidence:" + minConf);
        //log("Number of transactions:" + numTransactions);
        //log("Number of frequent 1-itemsets: " + freqItems.size());
        //log("---");
        
        tree = buildFPTree(freqItems, transactions);

        isMining = true;
        for(String item: tree.headerTable.keySet()){
            //log("[[[[[["+item+"]]]]]]");
            mineFPTree(tree, item, "");    
        }    

        return freqItemsets;
    }

    

    /**
     * 1. Using the pointer in the header table, decompose the FP-Tree into multiple subtrees, 
     *    each represent a subproblem (ex. finding frequent itemsets ending in ee)
     * 2. For each subproblem, traverse the corresponding subtree bottom-up to obtain 
     *    conditional pattern bases for the subproblem recursively.
     */
    private void mineFPTree(FPTree tree, String item, String suffix){
        //log("Item: "+item);
        //log("Original Suffix: "+suffix);
        String itemset = item;
        TreeNode link = tree.headerTable.get(item);
        List<String[]> conditionalPtnBases = new ArrayList<String[]>();
        while(link!=null){
            
            if(suffix.length()!=0){
                itemset = item +","+ suffix;
            }
            
            Integer count = freqItemsets.get(itemset);
            if (count == null) {
                freqItemsets.put(itemset, link.getCount());
                count = 1;
            }
            else {
                freqItemsets.put(itemset, count + link.getCount());
            }
            
            //log("Frequent Suffix: "+itemset);

            String[] ptnBase = findConditionalPatternBases(link);
            //log("Its PtnBase: "+Arrays.toString(ptnBase));
            if (ptnBase.length!=0){
                for (int i=0; i<link.getCount(); i++){
                    conditionalPtnBases.add(ptnBase);
                }
            }

            link = link.getNext();
        }
        //log("---------");

        if(conditionalPtnBases.isEmpty()){
            return;
        }
        else{
            Map<String, Integer> freqItems = scanDatasource(conditionalPtnBases);

            FPTree subTree = buildFPTree(freqItems, conditionalPtnBases);
            
            for(String subItem: subTree.headerTable.keySet()){
                mineFPTree(subTree, subItem, itemset);
            }
        }
    }

    private String[] findConditionalPatternBases(TreeNode node){    
        List<String> PtnBase = new ArrayList<String>();
        node = node.getParent();
        while(node.isRoot()==false){
            PtnBase.add(node.getItem());
            node = node.getParent();
        }
        return PtnBase.toArray(new String[PtnBase.size()]);
    }



    /**
     * Scan the database, get the frequent itemsets of length 1, 
     * and sort these 11-itemsets in decreasing support count.
     */
    private Map<String, Integer> scanDatasource(){
        Map<String, Integer> freqItems = new HashMap<String, Integer>();
        try{
            BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
            while (data_in.ready()) {
                String[] t = data_in.readLine().split("[,]+[\\s]*");
                if(t.length==0){
                    continue;
                }
                Arrays.sort(t);
                transactions.add(t);

                for (String item : t){
                    Integer count = freqItems.get(item);
                    if (count == null) {
                        freqItems.put(item, 1);
                        count = 1;
                    }
                    else {
                        freqItems.put(item, count + 1);
                    }
                }        

                numTransactions ++;
            }
            data_in.close();

            double threshold = minSup*numTransactions;
            for(Iterator<Map.Entry<String, Integer>> it = freqItems.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Integer> entry = it.next();
                if(entry.getValue() < threshold) {
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return freqItems;
    }

    private Map<String, Integer> scanDatasource(List<String[]> transactions){
        Map<String, Integer> freqItems = new HashMap<String, Integer>();
        for (String[] t: transactions){
            for (String item : t){
                Integer count = freqItems.get(item);
                if (count == null) {
                    freqItems.put(item, 1);
                    count = 1;
                }
                else {
                    freqItems.put(item, count + 1);
                }
            }        
        }

        double threshold = minSup*numTransactions;
        for(Iterator<Map.Entry<String, Integer>> it = freqItems.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            if(entry.getValue() < threshold) {
                it.remove();
            }
        }

        return freqItems;
    }

    

    /**
     * 1. Create the root node (null)    
     * 2. Read a transaction at a time. Sort items in the transaction acoording to the last step.
     * 3. For each transaction, insert items to the FP-Tree from the root node and 
     *    increment occurence record at every inserted node.
     * 4. Create a new child node if reaching the leaf node before the insersion completes.
     * 5. If a new child node is created, link it from the last node consisting of the same item.
    */
    private FPTree buildFPTree(Map<String, Integer> freqItems, List<String[]> transactions){
        // Create the root node (null) 
        FPTree tree = new FPTree();

        // Read a transaction at a time. Sort items in the transaction acoording to the last step.
        for(String[] t: transactions){
            PriorityQueue<Entry> pq = preprocessTransaction(t, freqItems);
            //log("-----transaction: "+Arrays.toString(t)+"-----");

            // For each transaction, insert items to the FP-Tree from the root node
            tree.insertItemsToNode(tree.root, pq);
        }
        return tree;
    }

    

    private PriorityQueue<Entry> preprocessTransaction(String[] t, Map<String, Integer> freqItems){
        PriorityQueue<Entry> pq = new PriorityQueue<Entry>();
        for(String item: t){
            Integer count = freqItems.get(item);
            if (count!=null){
                //log("item:"+item+", count: "+count);
                if(isMining){
                    pq.add(new Entry(item, count, t));
                }
                else{
                    pq.add(new Entry(item, count));
                }
            }
        }
        return pq;
    }

    private void log(String message){
        System.out.println(message);
    }
}
