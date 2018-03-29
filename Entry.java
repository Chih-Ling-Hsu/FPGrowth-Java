import java.util.*;
import java.lang.*;
public class Entry implements Comparable<Entry> {
    private String key;
    private Integer value;
    private List<String> order;

    public Entry(String key, Integer value) {
        this.key = key;
        this.value = value;
        this.order = null;
    }
    public Entry(String key, Integer value, String[] pattern) {
        this.key = key;
        this.value = value;
        this.order = Arrays.asList(pattern);
    }



    // getters
    public String getKey(){
        return key;
    }

    public Integer getValue(){
        return value;
    } 

    @Override
    public int compareTo(Entry other) {
        if(this.order!=null){
            if (this.order.indexOf(other.getKey())>this.order.indexOf(this.getKey())){
                return 1;
            }
            else{
                return -1;
            }
        }
        else{
            int countCmp = other.getValue().compareTo(this.getValue());
            if(countCmp==0){
                return other.getKey().compareTo(this.getKey());
            }
            else{
                return countCmp;
            }       
        } 
    }
}