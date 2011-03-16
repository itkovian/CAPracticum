
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class CacheSim {

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
			double bits = Math.log(blocks) / Math.log(2);
			if (bits != (int) bits) {
				System.out.println("Error: <blocks> must be a power of 2.");
				System.out.println();
				printUsage();
			}

			// Get the size of a cache block in bytes from the second
			// command line parameter. If it is not a power of two, then
			// print an error message, usage instructions and then bail.
			int size = Integer.parseInt(args[1]);
			bits = Math.log(size) / Math.log(2);
			if (bits != (int) bits) {
				System.out.println("Error: <size> must be a power of 2.");
				System.out.println();
				printUsage();
			}

			// process parameters
			String input = args[2];
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
				cache = new NWayAssociativeCache(blocks, size, 1);
			}
			else {
				if (nway) {
					cache = new NWayAssociativeCache(blocks, size, ways);
				} else {
					cache = new NWayAssociativeCache(1, size, blocks);
				}
			}

			if (input.compareTo("rowMajor")==0)
			{
				System.out.println("rowMajor configuration");
				rowMajor(cache, 8);
			}
			else if (input.compareTo("columnMajor")==0)
			{
				System.out.println("columnMajor configuration");
				columnMajor(cache, 8);
			}
			else if (input.compareTo("matrixMultiply")==0)
			{
				System.out.println("matrix mulitply configuration");
				matrixMultiply(cache, 8);
			}
			else if (input.compareTo("matrixTiledMultiply")==0)
			{
				System.out.println("matrix tiled mulitply configuration");
				matrixTiledMultiply(cache, 8);
			}
			else
			{
				System.out.println("bad configuration parameter");
				System.exit(-1);
			}

		}
	}

    private static void printUsage() {
        System.out
                .println("Usage: java CacheSim <blocks> <size> <input> [<assoc>] [ways]");
        System.out
                .println("  <blocks>: # of cache blocks (must be a power of 2).");
        System.out
                .println("    <size>: Size of each cache block (must be a power of 2).");
        System.out.println("    <input>: Choose from rowMajor, columnMajor, matrixMultiply or matrixTiledMultiply");
        System.out
                .println("   <assoc>: Omitted or false for direct mapped cache,");
        System.out.println("            true for fully associative cache.");
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
		else if (matrix.compareTo("Cr")==0 || matrix.compareTo("C")==0)
			base = 8*size*size + 128;
		else
		{
			System.out.println("Matrix " + matrix + "not recognized, please use A, B or C");
			System.exit(-1);
		}


		// Address is calculated (multiply by assumes int equals 4B).
		int address = base + 4*((i * size) + j);

		// Print out the address  (Optional)
		System.out.println(matrix + ": " +  address);

    return cache.request(address);
	}


	// This function generates the access pattern.
	private static void rowMajor(Cache cache, int size)
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

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Input Matrix ///////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// A
		System.out.println("A=\t");
		for (int i=0;i<size;i++)
		{	
			System.out.println("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(A[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Now do B = A*2
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				B[ i ][ j ] = A[ i ][ j ]*2;

				// Forward the access to the cache.
				requests++;
				if (sendCacheRequest(cache, "A", i, j, size)) 
				{
					hits++;
				}
				requests++;
				if (sendCacheRequest(cache, "B", i, j, size)) 
				{
					hits++;
				}

			}
		}

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Output Matrix //////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// B 
		System.out.println("A=\t");

		for (int i=0;i<size;i++)
		{	
			System.out.println("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(B[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}
		System.out.println();

		// Force a cache dump (optional)
		//cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("//////////////////////////////// Statistics ////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);
		System.out.println();
		System.out.println("////////////////////////////////////////////////////////////////////////////////");

	}



	// This function generates the access pattern.
	private static void columnMajor(Cache cache, int size)
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

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Input Matrix ///////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// A
		System.out.println("A=\t");
		for (int i=0;i<size;i++)
		{	
			System.out.println("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(A[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Now do B = A*2
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				B[ j ][ i ] = A[ j ][ i ]*2;

				// Forward the access to the cache.
				requests++;
				if (sendCacheRequest(cache, "A", j, i, size)) 
				{
					hits++;
				}
				requests++;
				if (sendCacheRequest(cache, "B", j, i, size)) 
				{
					hits++;
				}

			}
		}

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Output Matrix //////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// B 
		System.out.println("A=\t");

		for (int i=0;i<size;i++)
		{	
			System.out.println("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(B[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}
		System.out.println();

		// Force a cache dump (optional)
		//cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("//////////////////////////////// Statistics ////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);
		System.out.println();
		System.out.println("////////////////////////////////////////////////////////////////////////////////");

	}


	// This function generates the access pattern for a straightforward matrix multiplication.
	private static void matrixMultiply(Cache cache, int size)
	{
		// Statistics
		int requests = 0;
		int hits = 0;

		// Define matrices for A*B=C
		int A[][] = new int[size][size];
		int B[][] = new int[size][size];
		int C[][] = new int[size][size];

		// Initialize matrix
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				A[ i ][ j ] = i;
				B[ i ][ j ] = j;
				C[ i ][ j ] = 0;
			}
		}
	
		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Input Matrix ///////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// A
		System.out.println("A=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(A[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}
		// B
		System.out.println("B=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(B[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Now do A*B=C
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				for (int k=0;k<size;k++)
				{
					C[ i ][ j ] = C[ i ][ j ]+ A[ i ][ k ]* B[ k ][ j ];

					// Read C[k][i]
					requests++;
					if (sendCacheRequest(cache, "C", i, j, size)) 
					{
						hits++;
					}
					
					// Read A[j][i]
					requests++;
					if (sendCacheRequest(cache, "A", i, k, size)) 
					{
						hits++;
					}
					
					// Read B[k][j]
					requests++;
					if (sendCacheRequest(cache, "B", k, j, size)) 
					{
						hits++;
					}
					
					// Write C[k][i]
					requests++;
					if (sendCacheRequest(cache, "Cr", i, j, size)) 
					{
						hits++;
					}

				}
			}
		}

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Output Matrix //////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// C
		System.out.println("C=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(C[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Force a cache dump (optional)
		//cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("//////////////////////////////// Statistics ////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);
		System.out.println();
		System.out.println("////////////////////////////////////////////////////////////////////////////////");

	}





	// This function generates the access pattern for a straightforward matrix multiplication.
	private static void matrixTiledMultiply(Cache cache, int size)
	{
		// Set tilesize
		int tilesize = 2;
		if (size%tilesize != 0)
		{
			System.out.println("tilesize must be N*size!");
			System.exit(-1);
		}

		// Statistics
		int requests = 0;
		int hits = 0;

		// Define matrices for A*B=C
		int A[][] = new int[size][size];
		int B[][] = new int[size][size];
		int C[][] = new int[size][size];

		// Initialize matrix
		for (int i=0;i<size;i++)
		{	
			for (int j=0;j<size;j++)
			{
				A[ i ][ j ] = i;
				B[ i ][ j ] = j;
				C[ i ][ j ] = 0;
			}
		}
	
		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Input Matrix ///////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// A
		System.out.println("A=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(A[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}
		// B
		System.out.println("B=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(B[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Now do A*B=C
		for (int i0=0;i0<size;i0+=tilesize)
		{	
			for (int j0=0;j0<size;j0+=tilesize)
			{
				for (int k0=0;k0<size;k0+=tilesize)
				{
					for (int i=i0;i<(i0+tilesize);i++)
					{
						for (int j=j0;j<(j0+tilesize);j++)
						{
							for (int k=k0;k<(k0+tilesize);k++)
							{
								C[ i ][ j ] = C[ i ][ j ]+ A[ i ][ k ]* B[ k ][ j ];

								// Read C[i][j]
								requests++;
								if (sendCacheRequest(cache, "C", i, j, size)) 
								{
									hits++;
								}

								// Read A[j][k]
								requests++;
								if (sendCacheRequest(cache, "A", i, k, size)) 
								{
									hits++;
								}

								// Read B[k][j]
								requests++;
								if (sendCacheRequest(cache, "B", k, j, size)) 
								{
									hits++;
								}

								// Write C[i][j]
								requests++;
								if (sendCacheRequest(cache, "Cr", i, j, size)) 
								{
									hits++;
								}
							}
						}
					}
				}
			}
		}

		// Print matrix (for verification)
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("/////////////////////////////// Output Matrix //////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		// C
		System.out.println("C=\t");
		for (int i=0;i<size;i++)
		{
			System.out.print("\t");
			for (int j=0;j<size;j++)
			{
				System.out.print(C[ i ][ j ]);
				System.out.print("\t");
			}
			System.out.println();
		}

		// Force a cache dump (optional)
		//cache.request(-1);
		
		// Report the results of the simulation.
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("//////////////////////////////// Statistics ////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////////////");
		System.out.println("Total Requests: " + requests);
		System.out.println("    Cache Hits: " + hits);
		System.out.println("      Hit Rate: " + ((double) hits)/ requests);
		System.out.println();
		System.out.println("////////////////////////////////////////////////////////////////////////////////");

	}


}
