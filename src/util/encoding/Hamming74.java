package util.encoding;

import java.util.Arrays;

import util.BitSet2;
import util.Bytes;

/**
 * Methods to use a 7,4 (4 to 7) hamming code.
 * This can be used for error detection and even correction.
 * Detection will be correct for up to (and including) 2 bit errors.
 * Correction will be correct for up to (and including) 1 bit error.
 * 
 * Usage (using correction):
 * Encode your data (DATA_BITS long) using encode()
 * Send the encoded data.
 * *optional* check if an error has occured using hasError() on the received encoded data.
 * Correct the encoded data in case of bit errors using getCorrect() on the received encoded data.
 * Decode the data back to the original data using decode() on the corrected received encoded data.
 * 
 * @author I3anaan
 *
 */
public class Hamming74 {

	public static void main(String[] args){
		//printMatrix(matrixG);
		BitSet2 bs0 = new BitSet2();
		bs0.set(0,true);
		bs0.set(1,false);
		bs0.set(2,true);
		bs0.set(3,true);
		System.out.println(Hamming74.encode(bs0)+"\n");
		BitSet2 bs1 = new BitSet2();
		bs1.set(0,false);
		bs1.set(1,true);
		bs1.set(2,true);
		bs1.set(3,false);
		bs1.set(4,false);
		bs1.set(5,true);
		bs1.set(6,true);
		printMatrix(getSyndrome(bs1));
		System.out.println(hasError(bs1));
		System.out.println(getCorrected(bs1));
	}
	
	
	public static final int DATA_BITS = 4;
	public static final int ENCODED_BITS = 7;
	/*
	 * A00	A0m
	 * An0	Anm
	 */
	public static final int[][] matrixG = new int[][]{
		new int[]{1,1,0,1},
		new int[]{1,0,1,1},
		new int[]{1,0,0,0},
		new int[]{0,1,1,1},
		new int[]{0,1,0,0},
		new int[]{0,0,1,0},
		new int[]{0,0,0,1}}; 
	public static final int[][] matrixH = new int[][]{
		new int[]{1,0,1,0,1,0,1},
		new int[]{0,1,1,0,0,1,1},
		new int[]{0,0,0,1,1,1,1}};
	public static final int[][] matrixR = new int[][]{
		new int[]{0,0,1,0,0,0,0},
		new int[]{0,0,0,0,1,0,0},
		new int[]{0,0,0,0,0,1,0},
		new int[]{0,0,0,0,0,0,1}};
	
	/**
	 * Multiply matrix a times b: a*b
	 * Format:
	 * int[][]{ 	int[]{0,0,0,0},
	 * 				int[]{1,1,1,1},
	 * 				int[]{0,0,0,0},
	 * 				int[]{0,1,1,0}	};
	 * @return result matrix;
	 */
	public static int[][] mult(int a[][], int b[][]){//a[n][m], b[m][p]
		   if(a.length == 0) return new int[0][0];
		   if(a[0].length != b.length) return null; //invalid dims
		 
		   int n = a[0].length;
		   int m = a.length;
		   int p = b[0].length;
		 
		   int ans[][] = new int[m][p];
		 
		   for(int i = 0;i < m;i++){
		      for(int j = 0;j < p;j++){
		         for(int k = 0;k < n;k++){
		            ans[i][j] += a[i][k] * b[k][j];
		         }
		      }
		   }
		   return ans;
		}
	
	/**
	 * Encodes the given data.
	 * DATA_BITS amount go in, ENCODED_BITS come out.
	 * The encoded bits are then more resilient to errors.
	 * @param data	The data to encode (takes the first DATA_BITS bits)
	 * @return	The encoded bits, of length ENCODED_BITS
	 */
	public static BitSet2 encode(BitSet2 data){
		int[][] matrixData = new int[DATA_BITS][1];
		for(int i=0;i<DATA_BITS;i++){
			matrixData[i][0]=data.get(i) ? 1 : 0;
		}
		
		int[][] matrixEncoded = mult(matrixG,matrixData);
		int[][] matrixEncodedBoolean = mod(matrixEncoded,2);
		BitSet2 result = new BitSet2();
		for(int i=0;i<ENCODED_BITS;i++){
			result.set(i,matrixEncodedBoolean[i][0]==1);
		}
		return result;
	}
	
	/**
	 * Gets the syndrome of the data.
	 * This data should be the encoded data, gotten using decode()
	 * The syndrome is used to detect and correct errors.
	 * @param data	The data of length ENCODED_BITS to be decoded
	 * @return	The syndrome vector used to check or correct errors.
	 */
	public static int[][] getSyndrome(BitSet2 data){
		int[][] matrixData = new int[ENCODED_BITS][1];
		for(int i=0;i<ENCODED_BITS;i++){
			matrixData[i][0]=data.get(i) ? 1 : 0;
		}		
		return mod(mult(matrixH,matrixData),2);
	}
	
	/**
	 * @param syndrome The syndrome vector gotten from decoding data.
	 * @return	Whether or not the data has an error in it.
	 * 			This result is correct as long as there are not 3 or more bit errors.
	 * 			Will also return true (error) if the syndrome format is wrong.
	 */
	public static boolean hasError(int[][] syndrome){
		if(syndrome[0].length!=1){return true;}//Wrong format;
		for(int n=0;n<syndrome.length;n++){
			if(syndrome[n][0]==1){return true;}//bit error			
		}
		return false;
	}
	
	/**
	 * @return Whether or not the data has a bit error.
	 */
	public static boolean hasError(BitSet2 data){
		return hasError(getSyndrome(data));
	}
	
	
	/**
	 * Gets the corrected data from the given data.
	 * Will give correct result as long as there are only 0 or 1 bit errors.
	 * @param data	The encoded data to correct.
	 * @return	The corrected (but still encoded) data
	 */
	public static BitSet2 getCorrected(BitSet2 data){
		int[][] syndrom = getSyndrome(data);
		if(!hasError(syndrom)){
			return data;
		}else{
			int errorIndex=-1;
			for(int i=0; i<syndrom.length;i++){
				errorIndex = errorIndex+ (int)(syndrom[i][0]*Math.pow(2, i));
			}
			data.flip(errorIndex);
			return data;
		}
	}
	
	/**
	 * Decodes encoded data.
	 * takes ENCODED_BITS and returns DATA_BITS.
	 * @param data	encoded data to decode
	 * @return	decoded data.
	 */
	public static BitSet2 decode(BitSet2 data){
		int[][] matrixData = new int[ENCODED_BITS][1];
		for(int i=0;i<ENCODED_BITS;i++){
			matrixData[i][0]=data.get(i) ? 1 : 0;
		}
		
		int[][] decoded = mod(mult(matrixR,matrixData),2);
		BitSet2 result = new BitSet2();
		for(int n =0;n<decoded.length;n++){
			result.set(n,decoded[n][0]==1);
		}
		return result;
	}
	
	/**
	 * Does the modulo operator on every matrix entry, and saves it all in a new matrix.
	 */
	public static int[][] mod(int a[][],int mod){
		int[][] b = new int[a.length][a[0].length];
		
		for(int m=0;m<a.length;m++){
			for(int n=0;n<a[m].length;n++){
				b[m][n] = a[m][n]%mod;				
			}
		}
		return b;
	}
	
	/**
	 * Prints the matrix
	 */
	public static void printMatrix(int[][] a){
		for(int n=0;n<a.length;n++){
			for(int m=0;m<a[0].length;m++){
				System.out.print(a[n][m]+"\t");			
			}
			System.out.print("\n");
		}
	}
}
