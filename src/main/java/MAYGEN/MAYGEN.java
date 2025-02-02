package MAYGEN;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.group.Permutation;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class MAYGEN {
	public static int size=0;
	public static int total=0;
	public static int[] ys;
	public static int[] zs;
	public static int[] nonCanonicalIndices = new int[2];
	public static int hIndex=0;
	public static int count=0;
	public static int matrixSize=0;
	public static boolean verbose = false;
	public static boolean tsvoutput = true;
	public static boolean writeSDF = false;
	public static boolean callForward=true;
	public static int[] connectivityIndices=new int[2];
	public static boolean learningFromConnectivity=false; 
	public static SDFWriter outFile;
	public static String formula;
	public static String filename;
	public static boolean flag=true;
	public static boolean learningFromCanonicalTest=false;
	public static boolean biggest=true;
	public static ArrayList<List<Permutation>> formerPermutations= new ArrayList<List<Permutation>>();
	public static int[] degrees;
	public static int[] initialDegrees;
	public static List<Integer> initialPartition;
	public static IChemObjectBuilder builder=SilentChemObjectBuilder.getInstance();
	public static IAtomContainer atomContainer= builder.newInstance(IAtomContainer.class);
	public static ArrayList<List<Integer>> partitionList = new ArrayList<List<Integer>>();
	public static List<String> symbols = new ArrayList<String>();
	public static List<Integer> occurrences  = new ArrayList<Integer>();
	public static Map<String, Integer> valences; 
	public static int[][] max;
	public static int[][] L;
	public static int[][] C;
	public static int r=0; 
	public static int y=0;
	public static int z=0;
	static {
		//The atom valences from CDK.
		valences = new HashMap<String, Integer>();
			
		valences.put("C", 4);
		valences.put("N", 3);
		valences.put("O", 2);
		valences.put("S", 2);
		valences.put("P", 3);
		valences.put("F", 1);
		valences.put("I", 1);
		valences.put("Cl", 1);
		valences.put("Br", 1);
		valences.put("H", 1);
	}
	
	/**
	 * Basic functions
	 */

	 public static Integer[] permuteArray(Integer[] array, int i, int j) {
    	 int temp=0;
    	 temp= array[i];
    	 array[i]=array[j];
    	 array[j]=temp;
    	 return array; 
     }
	 
	/**
	 * Summing entries of a list.
	 * @param list List<Integer>
	 * @return int sum
	 */
	
	public static int sum(List<Integer> list) {
		int sum=0;
		for(int i=0;i<list.size();i++) {
			sum=sum+list.get(i);
		}
		return sum;
	}
	
	/**
	 * Summing entries of an array.
	 * @param array int[]
	 * @return int sum
	 */
	
	public static int sum(int[] array) {
		int sum=0;
		for(int i=0;i<array.length;i++) {
			sum=sum+array[i];
		}
		return sum;
	}
	
	/**
	 * Summing entries of a list until a given index.
	 * @param list List<Integer>
	 * @return int sum
	 */
	
	public static int sum(List<Integer> list, int index) {
		int sum=0;
		for(int i=0;i<=index;i++) {
			sum+=list.get(i);
		}
		return sum;
	}
	
	
	public static int atomOccurrunce(String[] info) {
		 int number=1;
		 if(info.length >1) {
			 number=Integer.parseInt(info[1]);
		 }
		 return number;
	}
	
	public static int[] actArray(int[] strip, Permutation p) {
		int permLength= p.size();
		int newIndex=0;
		int arrayLength=strip.length;
		int[] modified = new int[arrayLength];
		for(int i=0; i<permLength;i++) {
			newIndex=p.get(i);
			modified[newIndex]=strip[i]; 
		}
		return modified;
	}
	
	/**
	  * Values for an id permutation for a given size
	  * @param size 
	  * @return
	  */
	 public static int[] idValues(int size) {
		 int[] id= new int[size];
		 for(int i=0;i<size;i++) {
			 id[i]=i;
		 }
		 return id;
	 }
	 
	 /**
	  * Build id permutation
	  */
	 
	 public static Permutation idPermutation(int size) {
		 return new Permutation(size);
	 }
	 
	 /**
	  * The initializer function, reading the formula to set the degrees,
	  * partition and file directory variables.
	  * @param formula String molecular formula
	  * @param filedir String file directory
	  * @throws IOException
	  * @throws CDKException
	  * @throws CloneNotSupportedException
	  */
	 
	
	 /**
	  * Setting the symbols and occurrences global variables.
	  * @param formula  String molecular formula
	  */
	 
	 public static int partSize=0;
	 public static int totalHydrogen=0;
	 public static List<String> firstSymbols= new ArrayList<String>();
	 public static ArrayList<Integer> firstOccurrences = new ArrayList<Integer>();
	 public static boolean callHydrogenDistributor=false;
	 public static boolean justH=false;
	 public static void getSymbolsOccurrences(String formula) {
		 String[] atoms = normalizeFormula(formula).split("(?=[A-Z])");
		 String[] info;
		 int hydrogens=0;
		 for(String atom : atoms) {
			 info = atom.split("(?=[0-9])", 2); 
			 if(!info[0].equals("H")) {
				 matrixSize=matrixSize+atomOccurrunce(info);
				 firstSymbols.add(info[0]);
				 firstOccurrences.add(atomOccurrunce(info));
				 symbols.add(info[0]);
				 occurrences.add(atomOccurrunce(info));
				 hIndex=hIndex+atomOccurrunce(info);
			 }else {
				 hydrogens=atomOccurrunce(info);
			 }
		 }
		 
		 if(hydrogens!=0) {
			 totalHydrogen+=hydrogens;
			 if(matrixSize!=0) {
				 callHydrogenDistributor=true;
				 matrixSize=matrixSize+hydrogens;
				 firstSymbols.add("H");
				 firstOccurrences.add(hydrogens);
			 }else {
				 justH=true;
				 callHydrogenDistributor=false;
				 matrixSize=matrixSize+hydrogens;
				 occurrences.add(hydrogens);
				 symbols.add("H");
				 hIndex=hydrogens;
				 firstSymbols.add("H");
				 firstOccurrences.add(hydrogens);
			 } 
		 }else {
			 callHydrogenDistributor=false;
		 }
	 }

	public static String normalizeFormula(String formula) {
		String[] from = {"c", "n", "o", "s", "p", "f", "i", "cl", "CL", "br", "BR", "h"};
		String[] to = {"C", "N", "O", "S", "P", "F", "I", "Cl", "Cl", "Br", "Br", "H"};
	 	return StringUtils.replaceEach(formula, from, to);
	}


	public static boolean canBuildGraph(String formula) {
		 boolean check=true;
		 String[] atoms = normalizeFormula(formula).split("(?=[A-Z])");
		 String[] info;
		 String symbol;
		 int occur, valence;
		 int size=0;
		 int sum=0;
		 for(String atom : atoms) {
			 info = atom.split("(?=[0-9])", 2);
			 symbol=info[0];
			 valence=valences.get(symbol);
			 occur=atomOccurrunce(info);
			 size+=occur;
			 sum+=(valence*occur);
		 }
		 total=size;
		 if(sum<2*(size-1)) {
			 check=false;
		 }
		 return check;
	 }
	 /**
	  * Degree sequence is set from formula
	  * @param formula String molecular formula
	  * @return int[] valences 
	  */
	 public static int[] firstDegrees;
	 public static void initialDegrees(){
		 int size= sum(occurrences);
		 initialDegrees= new int[size];
		 firstDegrees= new int[sum(firstOccurrences)];
		 int index=0;
		 int firstIndex=0;
		 for(int i=0;i<symbols.size();i++) {
			 String symbol= symbols.get(i);
			 if(justH) {
				 for(int j=0;j<occurrences.get(i);j++) {
					 initialDegrees[index]=valences.get(symbol);
					 index++; 
			     } 
			 }else {
				 for(int j=0;j<occurrences.get(i);j++) {
			    	 if(!symbol.equals("H")) {
			    		 initialDegrees[index]=valences.get(symbol);
				    	 index++; 
			    	 }
			     }
			 }
		 }
		 for(int i=0; i<firstSymbols.size();i++) {
			 String symbol= firstSymbols.get(i);
			 for(int j=0;j<firstOccurrences.get(i);j++) {
		    	 firstDegrees[firstIndex]=valences.get(symbol);
		    	 firstIndex++; 
		     }
		 }
	 }
	
	 /**
	  * Building an atom container from a string of atom-implicit hydrogen information.
	  * If provided, fragments are also added.
	  * @return atom container new atom container
	  * @throws IOException
	  * @throws CloneNotSupportedException
	  * @throws CDKException
	  */
		
	 public static void build() {
		 for(int i=0;i<firstSymbols.size();i++) {
			 for(int j=0;j<firstOccurrences.get(i);j++) {
				 atomContainer.addAtom(new Atom(firstSymbols.get(i)));
			 }
		 }
		    
		 for(IAtom atom: atomContainer.atoms()) {
			 atom.setImplicitHydrogenCount(0);
		 }
	 }
	
	 /**
	  * Building an atom container for an adjacency matrix
	  * @param mat int[][] adjacency matrix
	  * @return
	  * @throws CloneNotSupportedException
	  */
		
	 public static IAtomContainer buildC(int[][] mat) throws CloneNotSupportedException {
		 IAtomContainer ac2= atomContainer.clone();
		 for(int i=0;i<mat.length;i++) {
			 for(int j=i+1;j<mat.length;j++) {
				 if(mat[i][j]==1) {
					 ac2.addBond(i, j, Order.SINGLE);
				 }else if(mat[i][j]==2) {
					 ac2.addBond(i, j, Order.DOUBLE);
				 }else if(mat[i][j]==3) {
					 ac2.addBond(i, j, Order.TRIPLE);
				 }
			 }
		 }
			
		 ac2=AtomContainerManipulator.removeHydrogens(ac2);
		 return ac2;
	 }
	 
	 /**
	  * ********************************************************************
	  */

	 public static boolean equalSetCheck(int[] original, int[] permuted, List<Integer> partition) {
		 int[] temp= cloneArray(permuted);
		 temp=descendingSortWithPartition(temp, partition);
		 return equalSetCheck(partition, original, temp);
	 }
	 
	 public static Integer [] getBlocks(int[] row, int begin, int end) {
		 return IntStream.range(begin, end).mapToObj(i->row[i]).toArray(Integer[]::new);
	 }

	 public static boolean equalSetCheck(List<Integer> partition, int[] original, int[] permuted){
		 boolean check=true;		 
		 int i=0;
		 if(partition.size()==size) {
			 for(int d=0;d<size;d++) {
				 if(original[d]!=permuted[d]) {
					 check=false;
					 break;
				 }
			 }
		 }else {
			 for(Integer p:partition) {
				 if(compareIndexwise(original, permuted, i, (p+i))) {
					 i=i+p;
				 }else {
					 check=false;
					 break;
				 }
			 } 
		 }
		 return check;
	 }
	 
	 public static boolean compareIndexwise(int[] array, int[] array2, int index1, int index2) {
		 boolean check=true;
		 for(int i=index1;i<index2;i++) {
			 if(array[i]!=array2[i]) {
				 check=false;
				 break;
			 }
		 }
		 return check;
	 }
	 public static boolean equalBlockCheck(int index, int[][] A, Permutation cycleTransposition, Permutation perm){
		 boolean check=true;
		 int[] canonical= A[index];
		 int[] original= A[index];
		 int newIndex= findIndex(index, cycleTransposition);		 
		 Permutation pm=perm.multiply(cycleTransposition);
		 original=cloneArray(A[newIndex]);
		 original=actArray(original,pm); 
		 if(!Arrays.equals(canonical,original)) {
			 check=false;
		 }
		 return check;
	 }
	 
	 public static int[] descendingSort(int[] array, int index0, int index1) {
		 int temp=0;
		 for (int i = index0; i < index1; i++) {     
			 for (int j = i+1; j < index1; j++) {     
				 if(array[i] < array[j]) {    
					 temp = array[i];    
	                 array[i] = array[j];    
	                 array[j] = temp;    
	             }     
	         }     
		 }
		 return array;
	 }
	 
	 public static int[] descendingSortWithPartition(int[] array, List<Integer> partition) {
		 int i=0;
		 for(Integer p:partition) {
			 array=descendingSort(array,i,i+p);
			 i=i+p;
		 }
		 return array;
	 }
	 
	 public static boolean biggerCheck(int index, int[] original, int[] permuted, List<Integer> partition) {
		 int[] sorted= cloneArray(permuted);
		 sorted=descendingSortWithPartition(sorted, partition);
		 return descendingOrderUpperMatrix(index,partition, original, sorted);
	 }
	 
	 public static void setBiggest(int index, int[][] A, Permutation permutation, List<Integer> partition) {
		 biggest=true;
		 int[] check = row2compare(index, A, permutation);
		 if(!biggerCheck(index, A[index],check,partition)) {
			 biggest=false;
		 }
	 }
	 
	 public static void getLernenIndices(int index, int[][] A, List<Permutation> cycles, List<Integer> partition) {
		 for(Permutation cycle: cycles) {
			 int[] check = row2compare(index, A, cycle);
			 if(!biggerCheck(index, A[index],check,partition)) {
				 setLernenIndices(index, cycle, A, check, partition);
				 break;
			 } 
		 }
	 }

	 public static void setLernenIndices(int rowIndex1, Permutation cycle, int[][] A, int[] mapped, List<Integer> partition) {
		 nonCanonicalIndices= new int[2];
		 learningFromCanonicalTest=false;
		 int rowIndex2 = cycle.get(rowIndex1);
		 Permutation permutation = getNonCanonicalMakerPermutation(mapped, cycle, partition);
		 learningFromCanonicalTest = true;
		 nonCanonicalIndices = upperIndex(rowIndex1, rowIndex2, A, permutation);
	 }
	 
	 public static Permutation getNonCanonicalMakerPermutation(int[] array, Permutation cycle, List<Integer> partition) {
		 int[] sorted= cloneArray(array);
		 sorted=descendingSortWithPartition(sorted, partition);
		 Permutation permutation = getCanonicalPermutation(sorted, array, partition);
		 return permutation.multiply(cycle);
	 }
	 
	 public static boolean firstCheck(int index, int[][]A, List<Integer> partition) {
		 boolean check = true;
		 if(partition.size()!=size) {
			 if(!descendingOrdercomparison(partition,A[index])) {
				 check=false;
				 int[] array = cloneArray(A[index]);
				 array = descendingSortWithPartition(array, partition);
				 Permutation canonicalPermutation = getCanonicalPermutation(array,A[index],partition);
				 learningFromCanonicalTest=true;  
				 nonCanonicalIndices = upperIndex(index, index, A, canonicalPermutation); 
			 } 
		 }
		 return check;
	 }
	 
	 /**
	  * Learning from the canonical test - Grund  Chapter 3.4.1
	  */
		 
	 /**
	  * By a given permutation, checking which entry is mapped to the index.
	  * @param perm Permutation
	  * @param index int entry index in the row
	  * @return int
	  */
	 
	 public static int getPermutedIndex(Permutation perm, int index) {
		 int out=0;
		 for(int i=0; i<perm.size();i++) {
			 if(perm.get(i)==index) {
				 out+=i;
				 break;
			 }
		 }
		 return out;
	 }
	 /**
	  * Looking for the upper limit where the original entry
	  * is smaller. 
	  * @param index int row index
	  * @param A int[] original row to compare
	  * @param permutation Permutation permutation from canonical test
	  * @return int upper limit.
	  */
		
	 
	 public static int[] limit(int index, int nextRowIndex, int[][] A, Permutation permutation) {
		 int[] original= A[index];
		 int[] permuted= A[nextRowIndex];
		 int[] limit= new int[2];
		 limit[0]=index;
		 int newIndex, value, newValue;
		 for(int i=index+1; i<size;i++) {
			 newIndex= getPermutedIndex(permutation, i);
			 value= original[i];
			 newValue= permuted[newIndex];
			 if(value!= newValue) {
				 if(value< newValue) {
					 limit[1]=i;
				 }
				 break;
			 }
		 }	
		 return limit;
		}
		
		/**
		 * Looking for the maximum index where the entry is not zero.
		 * @param index int row index
		 * @param size int row size
		 * @param original int[] original row to compare
		 * @param perm Permutation permutation from canonical test
		 * @return int lowerIndex
		 */
		
		public static int[] lowerIndex(int index, int nextRowIndex, int[][] A, Permutation permutation) {
			int max=0;
			int upperLimit=limit(index, nextRowIndex, A, permutation)[1];
			int[] permuted= A[nextRowIndex];
			int newIndex, newValue;
			for(int i=index+1; i<upperLimit;i++) {
				newIndex = getPermutedIndex(permutation, i);
				newValue = permuted[newIndex];
				if(newValue>0) {
					if(max<newIndex) {
						max=newIndex;
					}
				}
			}
			int[] pair= new int[2];
			pair[0]=nextRowIndex;
			pair[1]=max;
			return pair;
		}
		
		/**
		 * As explained in Grund 3.4.1; we need to calculate upperIndex.
		 * First, we need our j index where the original row become 
		 * smaller, then calculating the lower index. Based on these two
		 * values and the value of j in the permutation, we calculate
		 * our upper index. 
		 * 
		 * This upper index is used for the 'learning from canonical test'
		 * method. 
		 * 
		 * @param index int row index
		 * @param size  int row size
		 * @param original int[] original row to compare
		 * @param perm Permutation permutation from canonical test
		 * @return int upper index
		 */
		
		public static int[] upperIndex(int index, int nextRowIndex, int[][] A, Permutation permutation) {
			int[] limit  = limit(index, nextRowIndex, A, permutation);
			int[] lowerLimit = lowerIndex(index, nextRowIndex, A, permutation);
			int[] upperLimit= new int[2];
			upperLimit[0]=nextRowIndex;
			upperLimit[1]=getPermutedIndex(permutation, limit[1]);
			int[] maximalIndices = getMaximumPair(upperLimit,getMaximumPair(limit,lowerLimit));			
			maximalIndices=maximalIndexWithNonZeroEntry(A, maximalIndices);	
			return getTranspose(maximalIndices);
		}
		
		public static int[] maximalIndexWithNonZeroEntry(int[][] A, int[] maximalIndices) {
			int rowIndex= maximalIndices[0];
			int columnIndex= maximalIndices[1];
			if((columnIndex>rowIndex) && A[rowIndex][columnIndex]!=0) {
				return maximalIndices;
			}else {
				int[] output= new int[2];
				for(int i=columnIndex; i<size;i++) {
					if(A[rowIndex][i]>0) {
						output[0]=rowIndex;
						output[1]=i;
						break;
					}
				}
				return output;
			}
		}
		
		public static int[] getTranspose(int[] indices) {
			int[] out= new int[2];
			if(indices[0]>indices[1]) {
				out[0]=indices[1];
				out[1]=indices[0];
				return out;
			}else {
				return indices;
			}
		}
		
		public static int[] getMaximumPair(int[] a, int[] b) {
			if(a[0]>b[0]) {
				return a;
			}else if(b[0]>a[0]) {
				return b;
			}else {
				if(a[1]>b[1]) {
					return a;
				}else if(b[1]>a[1]) {
					return b;
				}else {
					return a;
				}
			}
		}

	 public static boolean compare(int[] arr, int[] arr2, int index1, int index2) {
		 boolean check=true;
		 for(int i=index1;i<index2;i++) {
			 if(arr[i]!=arr2[i]) {
				 if(arr[i]<arr2[i]) {
					 check=false;
					 break; 
				 }else if(arr[i]>arr2[i]) {
					 check=true;
					 break;
				 }
			 }
		 }
		 return check;
	 }
	
	 public static boolean descendingOrderUpperMatrix(int index, List<Integer> partition, int[] original, int[] permuted){
		 boolean check=true;		 
		 int i=index+1;
		 int p=0;
		 for(int k=index+1;k<partition.size();k++) {
			 p = partition.get(k);
			 if(desOrderCheck(original,i,(i+p))) {	 
				 if(!compareIndexwise(original, permuted, i, (i+p))){
			 		 check=compare(original, permuted, i, (i+p));
			 		 break;
				 }
				 i=i+p;
			 }else {
				 check=false;
				 break;
			 }
		 }
		 return check;
	 }

	 public static boolean desOrderCheck(int[] array, int f, int l) {
		 boolean check=true;
		 for(int i=f;i<l-1;i++) {
			 if(array[i]<array[i+1]) {
				 check=false;
				 break;
			 }
		 }
		 return check;
	 }
	 
	 public static boolean descendingOrdercomparison(List<Integer> partition, int[] row){
		 boolean check=true;		 
		 int i=0;
		 for(Integer part: partition) {
			 if(!desOrderCheck(row,i,(part+i))) {
				 check=false;
				 break;
			 }else {
				 i=i+part;
			 }
		 }
		 return check;
	 }
	 
	/**
	 *********************************************************************
	 */
	
	/**
	 * Candidate Matrix Generation Functions
	 */
	
	 /**
	  * L; upper triangular matrix like given in 3.2.1.
	  * For (i,j), after the index, giving the maximum line capacity.
	  * @param degrees int[] valences
	  * @return upper triangular matrix
	  */
		
	 public static void upperTriangularL(){
		 L= new int[hIndex][hIndex]; 
		 if(hIndex==2) {
			 for(int i=0;i<hIndex;i++) {
				 for(int j=i+1;j<hIndex;j++) {
					 L[i][j]= Math.min(degrees[i], Lsum(i,j));
				 }
			 } 
		 }else {
			 for(int i=0;i<hIndex;i++) {
				 for(int j=i+1;j<hIndex;j++) {
					 L[i][j]= Math.min(degrees[i], Lsum(i,j+1));
				 }
			 }
		 }
	 }
	 
	 /**
	  * C; upper triangular matrix like given in 3.2.1.
	  * For (i,j), after the index, giving the maximum column capacity.
	  * @param degrees int[] valences
	  * @return upper triangular matrix
	  */
		
	 public static int[][] upperTriangularC(){
		 C= new int[hIndex][hIndex]; 
		 if(hIndex==2) {
			 for(int i=0;i<hIndex;i++) {
				 for(int j=i+1;j<hIndex;j++) {
					 C[i][j]= Math.min(degrees[j], Csum(i,j));
				 }
			 }
		 }else {
			 for(int i=0;i<hIndex;i++) {
				 for(int j=i+1;j<hIndex;j++) {
					 C[i][j]= Math.min(degrees[j], Csum(i+1,j));
				 }
			 }
		 }
		 return C;
	 }
		
	 /**
	  * Summing ith rows entries starting from the jth column.
	  * @param i int row index
	  * @param j int column index
	  * @return
	  */
	 
	 public static int Lsum(int i, int j) {
		 int sum=0;
		 for(int k=j;k<hIndex;k++) {
			 sum=sum+max[i][k];
		 }
		 return sum;
	 }
		
	 /**
	  * Summing ith column entries starting from the jth row.
	  * @param i int column index
	  * @param j int row index
	  * @return
	  */
	 
	 public static int Csum(int i, int j) {
		 int sum=0;
		 for(int k=i;k<hIndex;k++) {
			 sum=sum+max[k][j];
		 }
		 return sum;
	 }
	 		
	 /**
	  * Possible maximal edge multiplicity for the atom pair (i,j).
	  * @param degrees int[] valences
	  */
		
	 public static void maximalMatrix() {
		 max= new int[hIndex][hIndex];
		 for(int i=0;i<hIndex;i++) {
			 for(int j=0; j<hIndex;j++) {
				 int di= degrees[i];
				 int dj= degrees[j];	
				 if(i==j) {
					 max[i][j]=0;
				 }else {
					 if(di!=dj) {
						 max[i][j]=Math.min(di, dj);
					 }else if (di==dj && i!=j) {
						 if(justH) {
							 max[i][j]=(di); 
						 }else {
							 max[i][j]=(di-1);
						 }
					 }
				 }
			 }
		 }
	 }
		
	 /**
	  * Initialization of global variables for the generate of structures
	  * for given degree list.
	  * 
	  * @param degreeList int[] valences
	  * @throws IOException
	  * @throws CloneNotSupportedException
	  * @throws CDKException
	  */
	 
	 public static void generate(int[] degreeList) throws IOException, CloneNotSupportedException, CDKException {
		 int[][] A   = new int[matrixSize][matrixSize];
		 degrees=degreeList;
		 flag=true;
		 maximalMatrix();
		 upperTriangularL();
		 upperTriangularC();

		 int[] indices= new int[2];
		 indices[0]=0;
		 indices[1]=1;
		 callForward=true;
		 r=0;
		 y=ys[r];
		 z=zs[r];
		 while(flag) {
			 nextStep(A,indices);
	 		 if(!flag) {
	 			 break;
	 		 }
	 		 if(learningFromConnectivity) {
	 			indices=connectivityIndices;
	 			findR(indices);
	 			y=ys[r];
	 			clearFormers(false,y);
	 			learningFromConnectivity=false;
	 			callForward=false;
	 		 }else {
	 			if(learningFromCanonicalTest) {
	 				indices=successor(nonCanonicalIndices,max.length);
	 				findR(indices);	 				
	 				learningFromCanonicalTest=false;
	 				callForward=false;
	 			}
	 		 }
		 }
	}
	
	/**
	 * Calculation of the next index pair in a matrix.
	 * 
	 * @param indices int[] index pair.
	 * @param size int row length.
	 * @return int[]
	 */
	 
    public static int[] successor(int[] indices, int size) {
    	int i0= indices[0];
    	int i1= indices[1];
    	if(i1<(size-1)) {
    		 indices[0]=i0;
    		 indices[1]=(i1+1);
    	}else if(i0<(size-2) && i1==(size-1)) {
    		 indices[0]=(i0+1);
    		 indices[1]=(i0+2);
    	}
    	return indices;
    }
    
    /**
	 * Calculation of the former index pair in a matrix.
	 * 
	 * @param indices int[] index pair.
	 * @param size int row length.
	 * @return int[]
	 */
    
    public static int[] predecessor(int[] indices, int size) {
    	int i0= indices[0];
   	 	int i1= indices[1];
   	 	if(i0==i1-1) {
   	 		indices[0]=(i0-1);
   	 		indices[1]=(size-1);
   	 	}else {
   	 		indices[0]=i0;
   	 		indices[1]=(i1-1);
   	 	}
   	 	return indices;
    }
    
    /**
     * Calling
     * @param A
     * @param indices
     * @return
     * @throws IOException
     * @throws CloneNotSupportedException
     * @throws CDKException
     */
    
	public static int[][] nextStep(int[][] A, int[] indices) throws IOException, CloneNotSupportedException, CDKException{
		if(callForward) {
			return forward(A, indices);
		}else {
			return backward(A,indices);
		} 
	}
	
	/**
	 * After generating matrices, adding the hydrogen with respect to
	 * the pre-hydrogen distribution. 
	 * @param A int[][] adjacency matrix
	 * @param index int beginning index for the hydrogen setting
	 * @return
	 */
	
	public static int[][] addHydrogens(int[][] A, int index){
		if(callHydrogenDistributor) {
			int hIndex=index;
			int limit=0;
			int hydrogen=0;
			for(int i=0;i<index;i++) {
				hydrogen=initialDegrees[i]-sum(A[i]);
				limit=hIndex+hydrogen;
				for(int j=hIndex;j<limit;j++) {
					A[i][j]=1;
					A[j][i]=1;
				}
				if(hydrogen!=0) {
					hIndex=hIndex+hydrogen;
				}
			}
		}
		return A;
	}
	
	/**
	 * In backward function, updating the block index.
	 * @param r int block index
	 * @param indices ArrayList<Integer> atom valences
	 * @return
	 */
	 
	 public static void updateR(int[] indices) {
		 int y=ys[r];
		 int z=zs[r];
		 if(indices[0]<y) {
			 r--;
		 }else if(indices[0]>z) {
			 r++;
		 }
	 }
	 
	 public static void findR(int[] indices) {
		 int block=0;
		 int index=0;
		 int rowIndex= indices[0];
		 for(Integer part: initialPartition) {
			 if(index<=rowIndex && rowIndex<(index+part)) {
				 break;
			 }
			 block++;
			 index=index+part;
		 }
		 r=block;
	 }
	 /**
	  * The third line of the backward method in Grund 3.2.3. The criteria 
	  * to decide which function is needed: forward or backward.
	  * 
	  * @param x 	the value in the adjacency matrix A[i][j]
	  * @param lInverse   lInverse value of indices {i,j}
	  * @param l
	  * @return
	  */
	 
	 public static boolean backwardCriteria(int x, int lInverse, int l) {
		boolean check=false;
		int newX= (x-1);
		if((lInverse-newX)<=l) {
			check=true;
		}
		return check;
	 }
	 
	/**
	  * The third step in Grund 3.2.3.
	  * 
	  * Backward step in the algorithm.
	  * 
	  * @param A 			int[][] adjacency matrix
	  * @param indices 		ArrayList<Integer> indices
	 */
	 
	 public static int[][] backward(int[][] A, int[] indices) {
		int i=indices[0];
		int j=indices[1];	
		if(i==0 && j==1) {
			flag=false;
		}else {
			indices=predecessor(indices, max.length);
			updateR(indices);
			i= indices[0];
			j= indices[1];
			int x= A[i][j];
			int l2= LInverse(i,j,A);
			int c2= CInverse(i,j,A);
			if(x>0 && (backwardCriteria((x),l2,L[i][j]) && backwardCriteria((x),c2,C[i][j]))){
				A[i][j]=(x-1);
				A[j][i]=(x-1);
				indices = successor(indices,max.length);
				updateR(indices);
				callForward=true;
			}else {
				callForward=false;
			}
		}
		return A;
	 }
	 
	/**
	 * Setting successor indices entry if there is a possible filling.
	 * 
	 * @param A int[][] adjacency matrix
	 * @param indices int[] entry indices
	 * @return int[][] 
	 * 
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 * @throws CDKException
	 */
	 
	public static int[][] forward(int[][] A, int[] indices) throws IOException, CloneNotSupportedException, CDKException {
		int i=indices[0];
		int j=indices[1];
		int lInverse= LInverse(i,j,A);
		int cInverse= CInverse(i,j,A);
		int minimumValue = Math.min(max[i][j],Math.min(lInverse,cInverse));
		int maximumValue = maximalEntry(minimumValue, lInverse, L[i][j], cInverse, C[i][j]); 
		callForward=true;
		return forward(lInverse, cInverse, maximumValue, i, j,A,indices);
	} 

	public static int[][] forward(int lInverse, int cInverse, int maximalX,int i, int j, int[][] A, int[] indices) throws CloneNotSupportedException, CDKException {
		if(((lInverse-maximalX)<=L[i][j]) && ((cInverse-maximalX)<=C[i][j])) {
			A[i][j]=maximalX;
			A[j][i]=maximalX;
			if(i==(max.length-2) && j==(max.length-1)) {
				if(canonicalTest(A)) {
					if(connectivityTest(A)){						
						count++;
						if (writeSDF && Objects.nonNull(filename)) {
							IAtomContainer mol = buildC(addHydrogens(A, hIndex));
							outFile.write(mol);
						}
						callForward=false;
					}else {
						callForward=false;
						learningFromConnectivity=true;
					}
				}else {	
					if(!learningFromCanonicalTest) {
						callForward=false;
					}
				}
			}else {	
				if(indices[0]==zs[r] && indices[1]==(max.length-1)) {
					callForward=canonicalTest(A);
					if(callForward) {
						indices=successor(indices,max.length);
						updateR(indices);
					}else {
						callForward=false;
					}
			    }else {
			    	indices=successor(indices,max.length);
		    		updateR(indices);
		    		callForward=true;
			    }
			}
		 }else {
			 callForward=false;
		 }
		 return A;
	 }
	 
	
	 /**
	  * Calculating the maximal entry for the indices.
	  * 
	  * @param min       int minimum of L, C amd maximal matrices for {i,j} indices.
	  * @param lInverse  int Linverse value of {i,j}
	  * @param l         int L value of {i,j}
	  * @param cInverse  int Cinverse value of {i,j}
	  * @param c		 int C value of {i,j}
	  * @return int max
	  */
	 
	 public static int maximalEntry(int min, int lInverse, int l, int cInverse, int c) {
		 int max=0;
		 for(int v=min;v>=0;v--) {
			 if(((lInverse-v)<=l) && ((cInverse-v)<=c)) {
				 max=max+v;
				 break;
			 }
		 }
		 return max;
	 }
	 
	/**
	 * Calculating the sum of the entries in the ith row until the jth column.
	 * @param i int row index
	 * @param j int column index
	 * @param A int[][] adjacency matrix 
	 * @return int
	 */
	
	public static int LInverse(int i, int j, int[][]A) {
		int sum=0;
		if(hIndex==2) {
			for(int s=0;s<=j;s++) {
				sum=sum+A[i][s];
			}
		}else {
			for(int s=0;s<j;s++) {
				sum=sum+A[i][s];
			}
		}
		return degrees[i]-sum;
	}
		 
	/**
	 * Calculating the sum of the entries in the jth column until the ith row.
	 * @param i int row index
	 * @param j int column index
	 * @param A int[][] adjacency matrix 
	 * @return int
	 */
	
	public static int CInverse(int i, int j, int[][]A) {
		int sum=0;
		if(hIndex==2) {
			for(int s=0;s<=i;s++) {
				sum=sum+A[s][j];
			}
		}else {
			for(int s=0;s<i;s++) {
				sum=sum+A[s][j];
			}
		}
		return degrees[j]-sum;
	}
	 
	/**
	 * Based on the new degrees and the former partition, getting 
	 * the new atom partition.
	 * 
	 * @param degrees int[] new atom valences
	 * @param partition ArrayList<Integer> former atom partition
	 * @return ArrayList<Integer>
	 */
	
	public static List<Integer> getPartition(int[] degrees, List<Integer> partition){
    	 List<Integer> newPartition = new ArrayList<Integer>();
		 int i=0;
    	 for(Integer p:partition) {
    		 Integer[] subArray= getBlocks(degrees,i,p+i);
    		 newPartition.addAll(getSubPartition(subArray));
    		 i=i+p; 
    	 }
    	 return newPartition;
	 }
	 
	/**
	 * Calculating the sub partitions for a given group of degrees.
	 * 
	 * @param degrees int[] valences
	 * @return ArrayList<Integer> 
	 */
	
	 public static List<Integer> getSubPartition(Integer[] degrees){
		 List<Integer> partition = new ArrayList<Integer>();
		 int i=0;
	     int size= degrees.length;
	     int count=0;
	     int next=0;
		 while(i<size) {
			 count=nextCount(i,size,degrees,partition);
			 next=(i+count);
			 if(next==size) {
				 break;
			 }else {
				 i=next;
			 }
	     }  
	     return partition;
	 }
	 
	 /**
	  * Counting the occurrence of a value in a degree.
	  * 
	  * @param i 			int index
	  * @param size 		int number
	  * @param degrees		int[] valences
	  * @param partition	ArrayList<Integer> partition
	  * @return int
	  */
	 
	 public static int nextCount(int i, int size, Integer[] degrees, List<Integer> partition) {
		 int count=1;
		 if(i==(size-1)) {
			 partition.add(1);
		 }else {
			 for(int j = i+1; j < size; j++){  
	         	 if(degrees[i] == degrees[j]){  
	        		 count++;  
	        		 if(j==(size-1)){
	        			 partition.add(count);
	        			 break;
	        		 }
	             }else{
	            	 partition.add(count);
	            	 break;
	             }
	         }   
		 }
		 return count;
	 }
	 
	 /**
	  * Main function to initialize the global variables and calling
	  * the generate function.
	  * 
	  * @return List<int[][]>
	  * 
	  * @throws IOException
	  * @throws CDKException
	  * @throws CloneNotSupportedException
	  */
	 
	 public static void run() throws IOException, CDKException, CloneNotSupportedException {
		 if(canBuildGraph(formula)) {
			 clearGlobals();
			 long startTime = System.nanoTime(); 
			 if(verbose) System.out.println("MAYGEN is generating isomers of "+normalizeFormula(formula)+"...");
			 getSymbolsOccurrences(formula);
			 initialDegrees();			 
			 build();
			 if (writeSDF) outFile = new SDFWriter(new FileWriter(filename));
			 structureGenerator();
			 
			 if (writeSDF) outFile.close();
			 long endTime = System.nanoTime() - startTime;
			 double seconds = (double) endTime / 1000000000.0;
		     DecimalFormat d = new DecimalFormat(".#");
		     if(verbose) 
		     {
		    	 System.out.println("The number of structures is: "+count);
			     System.out.println("Time: "+d.format(seconds)+" seconds");
		     }
		     if(tsvoutput) 
		     {
		    	 System.out.println(formula + "\t" + count + "\t" + d.format(seconds));
		     }
		     
		    
		 }else {
			 if(verbose) System.out.println("The input formula, "+formula+", does not represent any molecule.");
		 }
	}
	 
	/**
	 * For several calls of the run function, setting the global variables.
	 */
	 
	public static void clearGlobals() {
		 callForward=true;
		 connectivityIndices=new int[2];
		 learningFromConnectivity=false; 
		 callHydrogenDistributor=false;
		 atomContainer= builder.newInstance(IAtomContainer.class);
		 size=0;
		 nonCanonicalIndices = new int[2];
		 hIndex=0;
		 count=0;
		 matrixSize=0;
		 verbose = false;
		 formerPermutations= new ArrayList<List<Permutation>>();
		 partitionList = new ArrayList<List<Integer>>();
		 symbols = new ArrayList<String>();
		 occurrences  = new ArrayList<Integer>();
		 r=0; 
		 y=0;
		 z=0;
		 partSize=0;
		 firstSymbols= new ArrayList<String>();
		 firstOccurrences = new ArrayList<Integer>();
	 }
	
	/**
	 * If there are hydrogens in the formula, calling the hydrogenDistributor.
	 * This is the pre-hydrogen distribution. Then, the new list of degrees is
	 * defined for each hydrogen distribution.
	 * 
	 * @return List<int[]>
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws CloneNotSupportedException
	 * @throws CDKException
	 */
	
	 public static List<int[]> distributeHydrogens() throws FileNotFoundException, UnsupportedEncodingException, CloneNotSupportedException, CDKException{
		 List<int[]> degreeList= new ArrayList<int[]>();
		 if(!callHydrogenDistributor) {
			 degreeList.add(firstDegrees);
		 }else {
			 List<int[]> distributions = HydrogenDistributor.run(firstOccurrences, firstDegrees);
			 for(int[] dist: distributions) {
				 int[] newDegree= new int[size];
				 for(int i=0;i<size;i++) {
					 newDegree[i]=(firstDegrees[i]-dist[i]);
				 }
				 degreeList.add(newDegree);
			 } 
		 }
		 return degreeList;
	 }
	 
	 /**
	  * Setting the y and z values for each block. 
	  * y is the beginning index and z is the last index 
	  * of a block in the adjacency matrix. 
	  */
	 
	 public static void setYZValues() {
		 ys= new int[partSize+1];
		 zs= new int[partSize+1];
		 for(int i=0;i<=partSize;i++) {
			 ys[i]=findY(i);
			 zs[i]=findZ(i); 
		 }
	 }
	 
	 /**
	  * For a block index r, calculating its first row index. 
	  * @param r int block index
	  * @return int
	  */
	 
	 public static int findY(int r) {
		 return (sum(initialPartition,(r-1)));
	 }
		
	 /**
	  * For a block index r, calculating its last row index. 
	  * @param r int block index
	  * @return int
	  */
	 
	 public static int findZ(int r) {
		 return (sum(initialPartition,r)-1);
	 }	
	 
	 /**
	  * Calling the generate function for each degree values
	  * after the hydrogen distribution.
	  * 
	  * @throws IOException
	  * @throws CloneNotSupportedException
	  * @throws CDKException
	  */
	 
	 public static void structureGenerator() throws IOException, CloneNotSupportedException, CDKException{
		size=initialDegrees.length;
		List<int[]> newDegrees= distributeHydrogens();
		nonCanonicalIndices= new int[2];
		learningFromCanonicalTest=false;
		learningFromConnectivity=false;
		for(int[] degree: newDegrees) {
			System.gc();
			System.runFinalization();
			partSize=0;
			nonCanonicalIndices= new int[2];
			connectivityIndices= new int[2];
			learningFromConnectivity=false;
			learningFromCanonicalTest=false;
			initialPartition=getPartition(degree,occurrences);
			partitionList.clear();
			formerPermutations.clear();
			partSize+=(initialPartition.size()-1);
			setYZValues();
			partitionList.add(0,initialPartition);
			generate(degree);
		}
	 } 
	 	 
	 /**
	  * 3.6.2. Connectivity Test
	  */
		
	 /**
	  * Finding the neighbors of a given index.
	  * @param index int row (atom) index
	  * @param total int number of atoms.
	  * @param mat int[][] adjacency matrix
	  * @return Set<Integer>
	  */
		 
	 public static Set<Integer> nValues(int index, int total, int[][] mat) {
		 Set<Integer> nValues= new HashSet<Integer>();
		 nValues.add(index);
		 int[] theRow = mat[index]; 
		 for(int i=(index+1);i<total;i++) {
			 if(theRow[i]>0) {
				 nValues.add(i);
			 }
		 }
		 return nValues;
	 }
		 
	 /**
	  * Finding the W values of neighbors in the former connectivity partition.
	  * @param nValues ArrayList<Integer> N values 
	  * @param Kformer ArrayList<Integer> the K values of the former step
	  * @return Set<Integer>
	  */
		 
	 public static Set<Integer> wValues(Set<Integer> nValues, int[] Kformer){
		 Set<Integer> wValues= new HashSet<Integer>();
		 for(Integer i:nValues) {
			 wValues.add(Kformer[i]);
		 }
		 return wValues;
	 }
		 
	 /**
	  * Finding the connectivity partition, so the smallest index in the neighborhood.
	  * @param wValues Set<Integer> wValues  
	  * @param kFormer ArrayList<Integer> the K values of the former step
	  * @return ArrayList<Integer>
	  */
		 
	 public static int[] kValues(int total, Set<Integer> wValues, int[] kFormer){
		 int[] kValues= new int[total];
		 int min= Collections.min(wValues);
		 for(int i=0;i<total;i++) {
			 if(wValues.contains(kFormer[i])) {
				 kValues[i]=min;
			 }else {
				 kValues[i]=kFormer[i];
			 }
		 }
		 return kValues;
	 }
	 
	 /**
	  * Initializing the first connectivity partition.
	  * @param total int number of atoms.
	  * @return ArrayList<Integer>
	  */
		 
	 public static int[] initialKList(int total){
		 int[] k= new int[total];
		 for(int i=0;i<total;i++) {
			 k[i]=i;
		 }
		 return k;
	 }
	 
	 /**
	  * Test whether an adjacency matrix is connected or disconnected.
	  * @param mat int[][] adjacency matrix
	  * @return boolean
	  */

	 public static boolean connectivityTest(int[][] mat) {
		 learningFromConnectivity=false;
		 boolean check=false;
		 int[] kValues=initialKList(hIndex);
		 Set<Integer> nValues= new HashSet<Integer>();
		 Set<Integer> wValues= new HashSet<Integer>();
		 Set<Integer> zValues= new HashSet<Integer>();
		 int zValue= 0;
		 for(int i=0;i<hIndex;i++) {
			 nValues= nValues(i, hIndex, mat);
			 wValues=wValues(nValues,kValues);
			 zValue = Collections.min(wValues);
			 zValues.add(zValue);
			 kValues= kValues(hIndex, wValues, kValues);
		 }
		 if(zValue==0 && allIs0(kValues)) {
			 check=true;
		 }else {
			 setLearningFromConnectivity(zValues, kValues);
		 }
		 return check;
	 }
	 
	 /**
	  * If matrix is not connected, setting learninfFromConnectivity 
	  * global variables.
	  *  
	  * @param zValues Set<Integer> minimum index values of each atom's neighborhoods.
	  * @param kValues int[] connectivity partition
	  */
	 
	 public static void setLearningFromConnectivity(Set<Integer> zValues, int[] kValues) {
		 learningFromConnectivity=true;
		 connectivityIndices[0]=minComponentIndex(zValues, kValues);
		 connectivityIndices[1]=hIndex-1;
	 }
	 
	 /**
	  * Getting the minimum component index. Here, components are compared
	  * based on their last indices and sizes.
	  * 
	  * @param zValues Set<Integer> minimum index values of each atom's neighborhoods.
	  * @param kValues int[] connectivity partition
	  * @return int
	  */
	 
	 public static int minComponentIndex(Set<Integer> zValues, int[] kValues) {
		 int index=findMaximalIndexInComponent(kValues, 0);
		 int value=hIndex;
		 for(Integer i: zValues) {
			 value=findMaximalIndexInComponent(kValues, i);
			 if(value<index) {
				 index=value;
			 }
		 }
		 return index; 
	 }
	 
	 /**
	  * Finding the maximal index in a component to compare with other components.
	  * @param kValues int[] connectivity partition
	  * @param value   int   minimum neighborhood index
	  * @return int
	  */
	 
	 public static int findMaximalIndexInComponent(int[] kValues, int value) {
		 int maxIndex=hIndex;
		 for(int i=hIndex-1;i>0;i--) {
			 if(kValues[i]==value) {
				 maxIndex=i;
				 break;
			 }
		 }
		 return maxIndex;
	 }
	 
	 /**
	  * Checks whether all the entries are equal to 0 or not. 
	  * @param list ArrayList<Integer> 
	  * @return boolean
	  */
		 
	 public static boolean allIs0(int[] list) {
		 boolean check=true;
		 for(int i=0;i<list.length;i++) {
			 if(list[i]!=0) {
				 check=false;
				 break;
			 }
		 }
		 return check;
	 }
	 
	 /**
	  * Based on the molecules automorphisms, testing an adjacency matrix is
	  * canonical or not. 
	  * 
	  * @param A int[][] adjacency matrix
	  * @return boolean 
	  */
	
	public static boolean canonicalTest(int[][] A) {
		boolean check=true;
		learningFromCanonicalTest=false;
		y=ys[r];
		z=zs[r];
		if(partSize==r && z!=1) {
			z=z-1;
		}

		boolean test=true;
		for(int i=y;i<=z;i++) {
			test=rowCanonicalTest(i, r, A, partitionList.get(i),canonicalPartition(i,partitionList.get(i)));
			if(!test){	
				check=false;
				break;
			}
		}
		clearFormers(check, y);
		return check;
	}
	
	/**
	 * When an adjacency matrix is non-canonical, cleaning the formerPermutations
	 * and partitionList from the first row of the tested block.
	 * 
	 * @param check boolean canonical test result
	 * @param y		int first row of the tested block
	 */
	
	public static void clearFormers(boolean check, int y) {
		if(check==false) {
			ArrayList<List<Permutation>> newPerms= new ArrayList<List<Permutation>>();
			ArrayList<List<Integer>> newPart= new ArrayList<List<Integer>>();
			for(int i=0;i<y;i++) {
				newPerms.add(formerPermutations.get(i));
			}
			formerPermutations.trimToSize();
			formerPermutations=newPerms;

			for(int i=0;i<y+1;i++) {
				newPart.add(partitionList.get(i));
			}
			partitionList.trimToSize();
			partitionList=newPart;
		}
	}
	
	/**
	 * Calculating all candidate permutations for row canonical test.
	 * 
	 * The DFS multiplication of former automorphisms list with the list
	 * of cycle transpositions of the row.
	 * 
	 * @param index 	int row index
	 * @param cycles 	List<Permutation> cycle transpositions
	 */
	
	public static void candidatePermutations(int index, List<Permutation> cycles) {
		 ArrayList<Permutation> newList= new ArrayList<Permutation>();
		 for(Permutation cycle: cycles) {
			 newList.add(cycle);
		 }
		 if(index!=0) {
			 List<Permutation> formers = formerPermutations.get(index-1); 
			 for(Permutation form: formers) {
				 if(!form.isIdentity()) {
					 newList.add(form);
				 }
			 }
			 List<Permutation> newForm = new ArrayList<Permutation>();
			 for(Permutation frm: formers) {
				 if(!frm.isIdentity()) {
					 newForm.add(frm);
				 }
			 }
			 List<Permutation> newCycles = new ArrayList<Permutation>();
			 if(cycles.size()!=1) {
				 for(Permutation cyc: cycles) {
					 if(!cyc.isIdentity()) {
						 newCycles.add(cyc);
					 }
				 } 
			 }
			 for(Permutation perm: newForm) {
				 for(Permutation cycle: newCycles) {
					 Permutation newPermutation =cycle.multiply(perm);
					 if(!newPermutation.isIdentity()) {
						 newList.add(newPermutation);
					 }
				 }
			 } 
		 }
		 formerPermutations.add(index,newList);
	 }
	
	/**
	 * Canonical test for a row in the tested block.
	 * @param index			int row index
	 * @param r 			int block index
	 * @param A				int[][] adjacency matrix
	 * @param partition		ArrayList<Integer> former partition
	 * @param newPartition 	ArrayList<Integer> canonical partition
	 * @return boolean 
	 */
	
	public static boolean rowCanonicalTest(int index, int r,int[][] A, List<Integer> partition, List<Integer> newPartition) {
		boolean check= true;
		if(!firstCheck(index, A, newPartition)){
			check=false;
	    }else {
	    	y = ys[r];
	    	List<Permutation> cycles= new ArrayList<Permutation>();
			if(partition.size()==size) {
				Permutation id= new Permutation(size);
				cycles.add(id); 
			}else {
				cycles=cycleTranspositions(index, partition); 
			}
	    	candidatePermutations(index, cycles);
	    	check=check(index, y, size, A, newPartition);
	    	if(!check) {
	    		if(cycles.size()!=1) {
	    			getLernenIndices(index, A, cycles, newPartition); 
	    		}
	    	}else {
	    		addPartition(index, newPartition, A);  
	    	}
	     }
		 return check;
	 }
	
	/**
	  * Updating canonical partition list. 
	  * 
	  * @param index row index
	  * @param newPartition atom partition
	  * @param A int[][] adjacency matrix
	  */
	 
	 public static void addPartition(int index, List<Integer> newPartition, int[][] A) {
		 List<Integer> refinedPartition= new ArrayList<Integer>();
		 if(newPartition.size()==size) {
			 refinedPartition=newPartition;
		 }else {
			 refinedPartition= refinedPartitioning(newPartition,A[index]);
		 }
		 if(partitionList.size()==(index+1)) {
			 partitionList.add(refinedPartition);
		 }else {
			 partitionList.set(index+1, refinedPartition);
		 }
	 }
	 
	 /**
	  * Refining the input partition based on the row entries.
	  * 
	  * @param partition ArrayList<Integer> atom partition
	  * @param row int[] row
	  * @return ArrayList<Integer>
	  */
	 
	 public static List<Integer> refinedPartitioning(List<Integer> partition, int[] row){
		 List<Integer> refined= new ArrayList<Integer>();
		 int index=0;
		 int count=1;
		 for(Integer p:partition) {
			 if(p!=1) {
				 for(int i=index;i<p+index-1;i++) {
					 if(i+1<p+index-1) { 
						 if(row[i]==row[i+1]) {
							 count++;
						 }else{
							 refined.add(count);
							 count=1;
						 } 
					 }else {
						 if(row[i]==row[i+1]) {
							 count++;
							 refined.add(count);
							 count=1;
						 }else{
							 refined.add(count);
							 refined.add(1);
							 count=1;
						 }
					 }
				 }
				 index=index+p;
			 }else {
				 index++;
				 refined.add(1);
				 count=1;
			 }
		 }
		 return refined;
	 }
	 
	 /**
	  * For a row given by index, detecting the other row to compare in the block.
	  * For the detection of the next row index, cycle transposition is used.
	  * 
	  * @param index 				int row index
	  * @param A 	 				int[][] adjacency matrix
	  * @param cycleTransposition	Permutation cycle transposition
	  * @return int[]
	  */
	 
	 public static int[] row2compare(int index, int[][] A, Permutation cycleTransposition) {
		 int[] array = cloneArray(A[findIndex(index, cycleTransposition)]);
		 array=actArray(array, cycleTransposition);
		 return array;
	 }
	 	 
	 /**
	  * For a row given by index, detecting the other row to compare in the block.
	  * For the detection of the next row index, cycle transposition and former
	  * permutation are used.
	  * 
	  * @param index 				int row index
	  * @param A 	 				int[][] adjacency matrix
	  * @param cycleTransposition	Permutation cycle transposition
	  * @param formerPermutation	Permutation former permutation
	  * @return int[]
	  */
	 
	 public static int[] row2compare(int index, int[][] A, Permutation cycleTransposition, Permutation formerPermutation) {
		 int[] array = cloneArray(A[findIndex(index, cycleTransposition,formerPermutation)]);
		 Permutation perm = formerPermutation.multiply(cycleTransposition);
		 array= actArray(array,perm);
		 return array;
	 }
	 
	 /**
	  * With the cycle and former permutations, mapping the row index to
	  * another row in the block.
	  * @param index 	int row index
	  * @param cycle 	Permutation cycle transposition
	  * @param former 	Permutation former permutation
	  * @return int
	  */
	 
	 public static int findIndex(int index, Permutation cycle, Permutation former) {
		 int size= cycle.size();
		 int output=0;
		 Permutation perm= cycle.multiply(former);		 
		 for(int i=0;i<size;i++) {
			 if(perm.get(i)==index) {
				 output=i;
				 break;
			 }
		 }
		 return output;
	 }
	 
	 /**
	  * With the cycle permutation, mapping the row index to another
	  * row in the block.
	  * 
	  * @param index 	int row index
	  * @param cycle 	Permutation cycle transposition
	  * @return int
	  */
	 
	 public static int findIndex(int index, Permutation cycle) {
		 int size= cycle.size();
		 int output=0;
		 for(int i=0;i<size;i++) {
			 if(cycle.get(i)==index) {
				 output=i;
				 break;
			 }
		 }
		 return output;
	 }
	 
	 /**
	  * Cloning int array
	  * 
	  * @param array int[] array
	  * @return int[]
	  */
	 
	 public static int[] cloneArray(int[] array) {
		 return array.clone();
	 }
	 
	 /**
	  * Calculating the canonical permutation of a row.
	  * 
	  * In a block, the original and the other rows are compared;
	  * if there is a permutation mapping rows to each other,
	  * canonical permutation, else id permutation is returned.
	  * 
	  * @param originalRow int[] original row
	  * @param rowToCheck  int[] row to compare with
	  * @param partition   ArrayList<Integer> partition
	  * @return Permutation
	  */
	 
	 public static 	Permutation getCanonicalPermutation(int[] originalRow, int[] rowToCheck, List<Integer> partition) {
		 int[] cycles= getCanonicalPermutation(partition, originalRow, rowToCheck);
    	 int[] perm= new int[size];
		 for(int i=0;i<size;i++) {
			 for(int j=0;j<size;j++) {
				 if(i==cycles[j]) {
					 perm[i]=j;
				 } 
			 }
		 }
		 return new Permutation(perm);
     }
	 
	 /**
	  * Calculating the canonical permutation of a row.
	  * 
	  * In a block, the original and the other rows are compared;
	  * if there is a permutation mapping rows to each other,
	  * canonical permutation, else id permutation is returned.
	  * 
	  * @param originalRow int[] original row
	  * @param rowToCheck  int[] row to compare with
	  * @param partition   ArrayList<Integer> partition
	  * @return int[]
	  */
	 	 	 
	 public static int[] getCanonicalPermutation(List<Integer> partition, int[] max, int[] check) {
    	 int[] values= idValues(sum(partition));
    	 int i=0;
    	 if(!equalSetCheck(max,check,partition)) {
    		 return values;
    	 }else {
    		 for(Integer p:partition) {
    			 Integer[] can= getBlocks(max,i,p+i);
    			 Integer[] non= getBlocks(check,i,p+i);
    			 values = getCyclesList(can, non, i, values);
    			 i=i+p; 
    		 }
        	 return values;
    	 }
     }
	 
	 public static int[] getCyclesList(Integer[] max, Integer[] non, int index, int[] values) {
    	 int i=0;
    	 int permutationIndex=0;    	 
    	 while(i<max.length && max[i]!=0) {
    		 if(max[i]!=non[i]) {
        		 permutationIndex = findMatch(max,non, max[i],i);
        		 if(i!=permutationIndex) {
        			 non=permuteArray(non, i, permutationIndex);
        		 }
        		 int temp=values[i+index];
        		 values[i+index]=values[permutationIndex+index];
        		 values[permutationIndex+index]=temp;
    		 }
    		 i++;
    	 }
    	 return values;
     }
	 
	 /**
	  * 
	  * @param max
	  * @param non
	  * @param value
	  * @param start
	  * @return
	  */
	 
	 public static int findMatch(Integer[] max, Integer[] non, int value, int start) {
    	 int size=non.length;
    	 int index=start;
    	 for(int i=start;i<size;i++) {
    		 if(non[i]==value) {
    			 if(max[i]!=non[i]) {
    				 index=i;
        			 break; 
    			 }
    		 }
    	 }
    	 return index;
     }
	 
	 public static Permutation getEqualPerm(Permutation cycleTransposition, int index, int[][] A, List<Integer> newPartition) {
		 int[] check=row2compare(index, A, cycleTransposition);
		 Permutation canonicalPermutation = getCanonicalPermutation(A[index],check,newPartition);
		 return canonicalPermutation;
	 }
	 	 
	public static Permutation getCanonicalCycle(int index, int y, int total, int[][] A, List<Integer> newPartition,Permutation cycleTransposition) {
		biggest=true;
		Permutation canonicalPermutation = idPermutation(total);
		if(!equalBlockCheck(index, A,cycleTransposition, canonicalPermutation)) {
			 canonicalPermutation = getEqualPerm(cycleTransposition, index, A, newPartition);
			 int[] check= row2compare(index, A, cycleTransposition);
			 check = actArray(check,canonicalPermutation);
		 }			 
		 return canonicalPermutation;
	 }
	
	public static boolean check(int index, int y, int total, int[][] A, List<Integer> newPartition) {
		 boolean check=true;
		 List<Permutation> formerList= new ArrayList<Permutation>();
		 List<Permutation> form=formerPermutations.get(index);
		 for(Permutation permutation:form) { 
			 setBiggest(index, A, permutation, newPartition);
			 if(biggest) {
				 Permutation canonicalPermutation = getCanonicalCycle(index, y, total, A, newPartition, permutation);
				 int[] test=row2compare(index, A, permutation);
				 test= actArray(test,canonicalPermutation);	
				 if(descendingOrderUpperMatrix(index, newPartition, A[index], test)) {
					 if(canonicalPermutation.isIdentity()) {
						 if(equalSetCheck(newPartition,A[index],test)) {
							formerList.add(permutation); 
						 }
					 }else {
						 Permutation newPermutation=canonicalPermutation.multiply(permutation);
						 formerList.add(newPermutation);
					 }
				 }else {
					 formerList.clear();
					 check=false;
					 break;
				 }	  
			 }else {
				 formerList.clear();
				 check=false;
				 break;
			 }	  
		 }
		 if(check) {
			 formerPermutations.get(index).clear();
			 formerPermutations.set(index, formerList);
		 }
		 return check;
	 }
	
	public static List<Permutation> cycleTranspositions(int index, List<Integer> partition) {
		 List<Permutation> perms= new ArrayList<Permutation>();
		 int lValue = LValue(partition,index);
		 int[] values;
		 int former;
		 for(int i=0;i<lValue;i++) {
			 values= idValues(size);
			 former =  values[index];
			 values[index] =  values[index+i];
			 values[index+i] = former;
			 Permutation p = new Permutation(values);
			 perms.add(p);
		 } 
		 return perms;
	 }
	
	 /**
	  * Grund Thesis 3.3.3.
	  * To calculate the number of conjugacy classes, used in cycle 
	  * transposition calculation.
	  * @param partEx ArrayList<Integer> former atom partition
	  * @param degree ArrayList<Integer> atom valences
	  * @return
	  */
	 
	 public static int LValue(List<Integer> partEx, int degree) {
		 return (sum(partEx,(degree))-(degree)); 
	 }
	
	 
	 /**
	  * To get the canonical partition like in Grund Thesis 3.3.11
	  * @param i int row index
	  * @param partition ArrayList<Integer> partition
	  * @return
	  */
	 
	public static List<Integer> canonicalPartition(int i, List<Integer> partition){
		 return partitionCriteria(partition,i+1);
	}
	 
	 /**
	  * Add number of 1s into an ArrayList
	  */
		 
	 public static void addOnes(List<Integer> list, int number) {
		 for(int i=0;i<number;i++) {
			 list.add(1);
		 }
	 }
	
	/**
	  *	Grund Thesis 3.3.2 Partitioning criteria (DONE)  
	  * @param partEx the former partition
	  * @param degree degree of the partitioning.
	  * @return
	  */
		 
	 public static List<Integer> partitionCriteria(List<Integer> partEx, int degree){
		 List<Integer> partNew = new ArrayList<Integer>();
		 if(partEx.size()!=size) {
			 addOnes(partNew,degree); 
			 int oldValue= partEx.get(degree-1);
			 if(oldValue>1) {
				 partNew.add(oldValue-1);
				 for(int k=degree;k<partEx.size();k++) {
					 partNew.add(partEx.get(k));
				 }
			 }else if(oldValue==1){
				 for(int k=degree;k<partEx.size();k++) {
					 partNew.add(partEx.get(k));
				 }
			 }
			 return partNew;
		 }else {
			 return partEx;
		 }
	 }
	
	 private void parseArgs(String[] args) throws ParseException{
		 Options options = setupOptions();
		 CommandLineParser parser = new DefaultParser();
		 try {
			 CommandLine cmd = parser.parse(options, args);
			 MAYGEN.formula = cmd.getOptionValue("formula");
			 if (cmd.hasOption("filename")) 
				 {
				 MAYGEN.writeSDF = true;
				 MAYGEN.filename = cmd.getOptionValue("filename");
			}
			 			
			 if (cmd.hasOption("verbose")) MAYGEN.verbose = true;		
			 if (cmd.hasOption("tsvoutput")) MAYGEN.tsvoutput = true;		
		 } catch (ParseException e) {
			 HelpFormatter formatter = new HelpFormatter();
			 formatter.setOptionComparator(null);
			 String header = "\nGenerates 	molecular structures for a given molecular formula."
						 + " The input is a molecular formula string."
						 + "For example 'C2OH4'."
						 + "\n\n";
			 String footer = "\nPlease report issues at https://github.com/MehmetAzizYirik/AlgorithmicGroupTheory";
			 formatter.printHelp( "java -jar MAYGEN.jar", header, options, footer, true );
			 throw new ParseException("Problem parsing command line");
		 }
	 }
				
	 private Options setupOptions(){
		 Options options = new Options();
		 Option formula = Option.builder("f")
					 			.required(true)
					 			.hasArg()
					 			.longOpt("formula")
					 			.desc("formula (required)")
					 			.build();
		 options.addOption(formula);
		 Option verbose = Option.builder("v")
								.required(false)
								.longOpt("verbose")
								.desc("Output more verbose")
								.build();
		 options.addOption(verbose);	
		 Option tvsoutput = Option.builder("t")
					.required(false)
					.longOpt("tsvoutput")
					.desc("Output formula, number of structures and execution time in CSV format")
					.build();
		 options.addOption(tvsoutput);
		 Option filename = Option.builder("o")
					 			.required(false)
					 			.hasArg()
					 			.longOpt("filename")
					 			.desc("Store output in given file")
					 			.build();
		 options.addOption(filename);
		 return options;
	 }
	
	public static void main(String[] args) {
		MAYGEN gen = new MAYGEN();
		try {
			gen.parseArgs(args);
			MAYGEN.run();
		} catch (Exception e) {
			if (MAYGEN.verbose) e.getCause(); 
		}
	}
}
