import java.util.*;

public class Main {
    public static void main(String[] args) {
        int n = 5;
        int m = 3;
    List<List<Integer>> result = com(n,m);
    for(List<Integer> combination : result){
        for(Integer number : combination){
            System.out.print(number + " ");
        }
        System.out.println();
    }

    }
    public static List<List<Integer>> com(int n, int m){
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> combination = new ArrayList<>();
        back(result,combination,1,n,m);
        return result;
    }

    private static void back(List<List<Integer>> result,List<Integer> combination,int start,int n,int m){
        if(combination.size() == m){
            result.add(new ArrayList<>(combination));
            return;
        }
        for(int i = start;i<=n;i++){
            combination.add(i);
            back(result,combination,i+1,n,m);
            combination.remove(combination.size()-1);
        }
    }
}