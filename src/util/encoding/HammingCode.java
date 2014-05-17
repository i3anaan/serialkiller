package util.encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class HammingCode {

	public int dataBitCount = 4;
	public int encodedBitCount = 7;
	/*
	 * A00	A0m
	 * An0	Anm
	 */
	/*public static final int[][] matrixG = new int[][]{
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
	*/
	public int[][] matrixH;
	public int[][] matrixR;
	public int[][] matrixG;
	
	public HammingCode(int dataBitCount){
		generateMatrices(dataBitCount);		
	}
	
	
	/**
	 * Multiply matrix a times b: a*b
	 * Format:
	 * int[][]{ 	int[]{0,0,0,0},
	 * 				int[]{1,1,1,1},
	 * 				int[]{0,0,0,0},
	 * 				int[]{0,1,1,0}	};
	 * @return result matrix;
	 */
	public int[][] mult(int a[][], int b[][]){//a[n][m], b[m][p]
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
	public BitSet2 encode(BitSet2 data){
		int[][] matrixData = new int[dataBitCount][1];
		for(int i=0;i<dataBitCount;i++){
			matrixData[i][0]=data.get(i) ? 1 : 0;
		}
		int[][] matrixEncoded = mult(matrixG,matrixData);
		int[][] matrixEncodedBoolean = mod(matrixEncoded,2);
		BitSet2 result = new BitSet2();
		for(int i=0;i<encodedBitCount;i++){
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
	public int[][] getSyndrome(BitSet2 data){
		int[][] matrixData = new int[encodedBitCount][1];
		for(int i=0;i<encodedBitCount;i++){
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
	public boolean hasError(int[][] syndrome){
		if(syndrome[0].length!=1){return true;}//Wrong format;
		for(int n=0;n<syndrome.length;n++){
			if(syndrome[n][0]==1){return true;}//bit error			
		}
		return false;
	}
	
	/**
	 * @return Whether or not the data has a bit error.
	 */
	public boolean hasError(BitSet2 data){
		return hasError(getSyndrome(data));
	}
	
	
	/**
	 * Gets the corrected data from the given data.
	 * Will give correct result as long as there are only 0 or 1 bit errors.
	 * @param data	The encoded data to correct.
	 * @return	The corrected (but still encoded) data
	 */
	public BitSet2 getCorrected(BitSet2 data){
		int[][] syndrom = getSyndrome(data);
		if(!hasError(syndrom)){
			return data;
		}else{
			int errorIndex=-1;
			for(int i=0; i<syndrom.length;i++){
				errorIndex = errorIndex+ (int)(syndrom[i][0]*Math.pow(2, i));
			}
			BitSet2 result = (BitSet2) data.clone();
			result.flip(errorIndex);
			return result;
		}
	}
	
	/**
	 * Decodes encoded data.
	 * takes ENCODED_BITS and returns DATA_BITS.
	 * @param data	encoded data to decode
	 * @return	decoded data.
	 */
	public BitSet2 decode(BitSet2 data){
		int[][] matrixData = new int[encodedBitCount][1];
		for(int i=0;i<encodedBitCount;i++){
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
		System.out.println("Matrix: ("+a.length+"x"+a[0].length+")");
		for(int n=0;n<a.length;n++){
			for(int m=0;m<a[0].length;m++){
				System.out.print(a[n][m]+"\t");			
			}
			System.out.print("\n");
		}
	}
	
	
	/**
	 * Generates the matrices H, R and G based on the dataBitCount.
	 * These matrices are used in encoding and decoding.
	 * @param dataBitCount	The amount of dataBits this code should support
	 * 			Rounds the dataBitCount up to the nearest power of 2 
	 */
	public void generateMatrices(int dataBitCount){
		//Generate H
		int dataBitCountCeiled = (int)Math.pow(2,Math.ceil(Math.log(dataBitCount)/Math.log(2)));
		this.dataBitCount = dataBitCountCeiled;
		int parityBitsNeeded = (int) Math.ceil(Math.log(dataBitCount)/Math.log(2)) +1;
		this.encodedBitCount = dataBitCountCeiled+parityBitsNeeded;
		int[][] h = new int[parityBitsNeeded][parityBitsNeeded+dataBitCountCeiled];
		for(int i=0;i<parityBitsNeeded;i++){
			int offset = sumTill(i);
			for(int a=offset;a<parityBitsNeeded+dataBitCountCeiled;a=a+((int)Math.pow(2, i))*2){
				for(int b=0;b<Math.min((parityBitsNeeded+dataBitCountCeiled)-a,((int)Math.pow(2, i)));b++){
					h[i][a+b] = 1;
				}
			}
		}
		//H done
		//System.out.print("H: ");
		//printMatrix(h);
		this.matrixH = h;
		
		//Generate R
		int[][] r = new int[dataBitCountCeiled][dataBitCountCeiled+parityBitsNeeded];
		int[] dataIndices = getDataIndices(dataBitCountCeiled);
		for(int i=0;i<dataBitCountCeiled;i++){
			r[i][dataIndices[i]]=1;
		}
		//R done
		//System.out.print("R: ");
		//printMatrix(r);
		this.matrixR = r;
		
		//Generate G
		int[][] g = new int[dataBitCountCeiled+parityBitsNeeded][dataBitCountCeiled];
		int pivots = 0;
		for(int i=0;i<dataBitCountCeiled+parityBitsNeeded;i++){
			if(indexOf(dataIndices,i)!=-1){
				g[i][indexOf(dataIndices,i)]=1;
				pivots++;
			}else{
				g[i] = getCoverRow(h,dataIndices,i-pivots);
			}
		}
		//G done
		//System.out.print("G: ");
		//printMatrix(g);
		this.matrixG = g;
	}
	
	
	
	//The following methods are support methods for generating the matrices H R and G.
	
	
	/**
	 * Returns a array where the index represents the data bit index.
	 * This array represents which data bits the given parity bit covers (1).
	 * @param h	The H matrix
	 * @param dataIndices	The array specifying the indices of the databits (in h)
	 * @param parityBitIndex	The parity bit number to check (this count has nothing to do with databits)
	 * @return	An array specifying which data bits the given parity bit covers.
	 */
	private static int[] getCoverRow(int[][] h, int[] dataIndices,int parityBitIndex) {
		int[] result = new int[dataIndices.length];
		for(int i=0;i<dataIndices.length;i++){
			result[i] = h[parityBitIndex][dataIndices[i]];
		}
		return result;
	}

	/**
	 * Like a factorial, but with addition.
	 * equal to (0<=n<=value)sigma(n)
	 * @param value	Till where to count
	 * @return (0<=n<=value)sigma(n)
	 */
	public static int sumTill(int value){
		int result = 0;
		for(int i=0;i<=value;i++){
			result = result+i;
		}
		return result;
	}
	/**
	 * Makes an array in which the indices of data bits are stored.
	 * These indices are to be used in matrix G and H.
	 * @param dataBitCount	The amount of dataBits to contain
	 * @return an array in which the indices of data bits are stored.
	 */
	public static int[] getDataIndices(int dataBitCount){
		int offset = 0;
		int[] indices = new int[dataBitCount];
		int count=0;
		int p = 0;
		while(count<dataBitCount){
			int d;
			offset++;
			for(d=0;d<Math.pow(2, p)-1 && count<dataBitCount;d++){
				indices[count] =offset;
				offset++;
				count++;
			}
			p++;
		}
		return indices;
	}
	
	/**
	 * Returns the index of the given value in the given array
	 * @param arr	The array to check
	 * @param value	The value to search
	 * @return	First index of the value in array, or -1 if not found;
	 */
	public static int indexOf(int[] arr, int value){
		for(int i=0;i<arr.length;i++){
			if(arr[i]==value){
				return i;
			}
		}
		return -1;
	}
}
