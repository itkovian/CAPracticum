
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Cachesimulator. The program has by default the following command line arguments:
 * The number of cache blocks,
 * The size of each cach block in bytes,
 * The access pattern name (patroon1 or patroon2),
 * The cache type (true or false for an associative or direct mapped cache, respectively). iOptional and defaults to false,
 * The associativity (optional, only used if the previous argument is set to true).
 */
public class CacheSim {

  public static boolean isPowerOf2(int n) {
    if (n == 1) {
      return true;
    }

    if (n % 2 != 0) {
      return false;
    }
    else {
      return isPowerOf2(n / 2);
    }
  }

	public static void main(String[] args) {

		// If the number of command line arguments is not right
		// print out usage instructions and bail out.
		if (args.length != 3 && args.length != 4 && args.length != 5) {
			printUsage();
		}
		else { // Run the simulation.

			// Get the number of cache blocks from the first command line
			// argument. If it is not a power of two, then print an error
			// message followed by usage instructions and bail out.
			int blocks = Integer.parseInt(args[0]);
      if(! isPowerOf2 (blocks)) {
				System.err.println("Error: <blocks> must be a power of 2.");
				System.err.println();
				printUsage();
      }

			// Get the size of a cache block in bytes from the second
			// command line parameter. If it is not a power of two, then
			// print an error message, usage instructions and then bail.
			int size = Integer.parseInt(args[1]);
      if(! isPowerOf2 (size)) {
				System.err.println("Error: <size> must be a power of 2.");
				System.err.println();
				printUsage();
      }

			// Get the access pattern identifier
			String input = args[2];
      if(! (input.equals("patroon1") || input.equals("patroon2") || input.equals("patroon3"))) {
        System.err.println("Error: pattern should be one of patroon1 or patroon2 or patroon3");
        System.err.println();
        printUsage();
      }

			// Get the cache configuration parameters
			boolean assoc = false;
			boolean nway = false;
			int ways = 0;
			if (args.length == 4) {
				assoc = Boolean.valueOf(args[3]).booleanValue();
			}
			if (args.length == 5) {
				assoc = true;
				nway = true;
				ways = Integer.valueOf(args[4]);
			}

			// Create the appropriate type of cache.
			Cache cache;
			if (!assoc) {
				cache = new DirectMappedCache(blocks, size);
			}
			else {
				if (nway) {
					cache = new NWayAssociativeCache(blocks, size, ways);
				} else {
					cache = new FullyAssociativeCache(blocks, size);
				}
			}

			int requests = 0;
			int hits = 0;

			if (input.compareTo("patroon1")==0)
			{
				System.out.println("Memory Access Pattern: pattern1");
				pattern1(cache, 32);
			}
			else if (input.compareTo("patroon2")==0)
			{
				System.out.println("Memory Access Pattern: pattern2");
				pattern2(cache, 32);
			}
			else if (input.compareTo("patroon3")==0)
			{
				System.out.println("Memory Access Pattern: pattern3");
				pattern3(cache, 32);
			}
			else
			{
				System.out.println("bad configuration parameter");
				System.exit(-1);
			}

		}
	}

	private static void printUsage() {
		System.out.println("Usage: java CacheSim <blocks_per_set> <block_size> <pattern> [<assoc>]");
		System.out.println("\t<blocks_per_set>   : # of cache blocks (must be a power of 2) per set.");
		System.out.println("\t<block_size>       : Size of each cache block (must be a power of 2).");
		System.out.println("\t<pattern>          : Access pattern selection [data1 | data2 | trans].");
		System.out.println("\t<assoc>            : Omitted or false for direct mapped cache,");
		System.out.println("\t                     true for fully associative cache.");
    System.out.println("\t<ways>             : number of ways for an associative cache. only provided");
    System.out.println("\t                     for a n-way associative cache.");   
		System.exit(-1);
	}


	private static boolean sendCacheRequest(Cache cache, String matrix, int i, int j, int size)
	{
		// different matrices are located in different places in the memory, this
		// is modelled by using a matrix-specific base address.
		int base = 0;
		if (matrix.compareTo("A")==0)
			base = 0;
		else if (matrix.compareTo("B")==0)
			base = 4*size*size + 64;
		else if (matrix.compareTo("C")==0)
			base = 8*size*size + 96 ;
		else
		{
			System.err.println("Matrix " + matrix + "not recognized, please use A, B or C");
			System.exit(-1);
		}


		// Address is calculated (multiply by 4 assumes int equals 4B).
		int address = base + 4*((i * size) + j);

		boolean hit = cache.request(address);

		// Print out the address  (comment out the following line if needed)
		//System.out.println(matrix + ": " +  address + (hit ? "" : " *"));

		return hit;
	}


	// Data1 access pattern: .
	private static void pattern2(Cache cache, int size)
	{
		// Statistics
		int requests = 0;
		int hits = 0;

		// Input matrix
		int A[][] = new int[size][size];
		int B[][] = new int[size][size];

		// Initialize matrix
		int counter = 1;
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				A[ i ][ j ] = counter++;
				B[ i ][ j ] = 0;
			}
		}
	
		// Now fill B such that B[i][j] equals the sum of all elements of row i and column j of A
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{ 
				// assuming this fits in a registers and does not generate a cache access.
				int sum = -A[i][j];
				for (int ii=0;ii<size;ii++)
				{
					sum+= A[ii][j];
					requests++;
					if (sendCacheRequest(cache, "A", ii, j, size)) hits++ ;
				}
				for (int jj=0;jj<size;jj++)
				{
					sum += A[i][jj];
					requests++;
					if (sendCacheRequest(cache, "A", i, jj, size)) hits++ ;
				}

				B[ i ][ j ] = sum;
				requests++;
				if(sendCacheRequest(cache, "B", i, j, size)) hits++ ;
			}
		}

		// Force a cache dump (put the following line in comment to prevent the dump).
		cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);

	}


	// transpose access pattern: .
	private static void pattern1(Cache cache, int size)
	{
		// Statistics
		int requests = 0;
		int hits = 0;

		// Input matrix
		int A[][] = new int[size][size];
		int B[][] = new int[size][size];

		// Initialize matrix
		int counter = 1;
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				A[ i ][ j ] = counter++;
				B[ i ][ j ] = 0;
			}
		}
	
		// Now fill B such that B[i][j] equals A[i][j];
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{ 
				B[ i ][ j ] = A[ j ][ i ];
				requests++;
				if(sendCacheRequest(cache, "A", j, i, size)) hits++ ;
				requests++;
				if(sendCacheRequest(cache, "B", i, j, size)) hits++ ;
			}
		}

		// Force a cache dump (put the following line in comment to prevent the dump).
		cache.request(-1);
			
		// Report the results of the simulation.
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);

	}

	// Matrix increment access pattern
	private static void pattern3(Cache cache, int size)
	{
		// Statistics
		int requests = 0;
		int hits = 0;

		// Input matrix
		int A[][] = new int[size][size];

		// Initialize matrix
		int counter = 1;
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				A[ i ][ j ] = counter++;
			}
		}
	
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{ 
				A[ i ][ j ] = A[ i ][ j ]++;
				// read
				requests++;
				if(sendCacheRequest(cache, "A", j, i, size)) hits++ ;
				//write
				requests++;
				if(sendCacheRequest(cache, "A", j, i, size)) hits++ ;
			}
		}

		// Force a cache dump (put the following line in comment to prevent the dump).
		cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);

	}


}
