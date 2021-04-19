
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Main {

	private static final int TAM_INI = 1000; 	//Mínimo tamaño arrays
	private static final int TAM_FIN = 10000; 	//Máximo tamaño arrays
	private static final int TAM_ICR = 50;		//Incremento del tamaño de arrays
	private static final int REPE = 5;			//Veces que se mide el tiempo para hacer la media
	//Número ordenaciones = ( (TAM_FIN-TAM_INI)/TAM_ICR ) *REPE *3
	
	//Experimento
	public static void main(String[] args)
	{
		// fb = fuerza bruta
		// dv = divide y vencerás
		// th = threads
		
		int fbAC, dvAC, thAC; 	//Acumuladores para hacer las medias.
		FileWriter fb = null, dv = null, th = null;
		
		try {
			fb = new FileWriter("fb_result.txt"); //Abrimos ficheros escritura texto
			dv = new FileWriter("dv_result.txt");
			th = new FileWriter("th_result.txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		for (int t = TAM_INI; t <= TAM_FIN; t += TAM_ICR) // t = tamaño array actual
		{
			fbAC = 0; dvAC = 0; thAC = 0; //Acumuladores a 0
			for (int r = 0; r < REPE; r++)
			{
				int[] arrayAOrdenar = randomArray(t, 
						System.currentTimeMillis()); //Semilla es tiempo actual para que sea aleatoria
				
				int[] copia = Arrays.copyOf(arrayAOrdenar, arrayAOrdenar.length); //Ordenamos las copias
				fbAC += ordenaFuerzaBruta(copia);
				if (!ordenado(copia)) throw new RuntimeException("Error al ordenar array: " + Arrays.toString(copia));
				
				copia = Arrays.copyOf(arrayAOrdenar, arrayAOrdenar.length);
				dvAC += ordenaDivideYVenceras(copia);
				if (!ordenado(copia)) throw new RuntimeException("Error al ordenar array" + Arrays.toString(copia));
				
				copia = Arrays.copyOf(arrayAOrdenar, arrayAOrdenar.length);
				thAC += ordenaThreads(copia);
				if (!ordenado(copia)) throw new RuntimeException("Error al ordenar array" + Arrays.toString(copia));
			}
			float fb_result = (float)fbAC/(float)REPE;
			float dv_result = (float)dvAC/(float)REPE;
			float th_result = (float)thAC/(float)REPE;
			try {
				fb.write(Float.toString(fb_result).replace('.', ',')+";"); //Para Excel cambiamos '.' por ','
				dv.write(Float.toString(dv_result).replace('.', ',')+";");
				th.write(Float.toString(th_result).replace('.', ',')+";");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("(TAM = "+t+") "+"TiempoFuerzaBruta: " + 
					fb_result);
			System.out.println("(TAM = "+t+") "+"TiempoDivideYVenceras: " + 
					dv_result);
			System.out.println("(TAM = "+t+") "+"TiempoThreads: " + 
					th_result);
		}
		
		try {
			fb.close(); //Cerramos ficheros
			dv.close();
			th.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	//IMPORTANTE: devuelve tiempo ejecución
	private static int ordenaFuerzaBruta(int[] arrayAOrdenar)
	{
		long ini, fin;
		
		ini = System.currentTimeMillis(); //Iniciamos temporizador
		
		int i = 0, j = 1, arri, arrj;
		while(i < (arrayAOrdenar.length-1))
		{
			arri = arrayAOrdenar[i];
			arrj = arrayAOrdenar[j];
			if (arrj < arri)
			{
				swap(arrayAOrdenar, i, j);
			}
			j++;
			if (j >= arrayAOrdenar.length)
			{
				i++;
				j = i+1;
			}
		}
		//System.out.println(Arrays.toString(arrayAOrdenar));
		
		fin = System.currentTimeMillis(); //Detenemos temporizador
		
		return (int)fin-(int)ini;
	}

	//IMPORTANTE: devuelve tiempo ejecución
	private static int ordenaDivideYVenceras(int[] arrayAOrdenar)
	{
		long ini, fin;
		ini = System.currentTimeMillis(); //Iniciamos temporizador
		
		divideYVencerasREC(arrayAOrdenar, 0, arrayAOrdenar.length-1);
		
		fin = System.currentTimeMillis(); //Detenemos temporizador
		//System.out.println(Arrays.toString(arrayAOrdenar));
		return (int)fin-(int)ini;
	}
	
	private static void divideYVencerasREC(int[] arrayAOrdenar, int ini, int fin)
	{
		if (ini < fin)
		{
			int m = (ini+fin)/2;
			divideYVencerasREC(arrayAOrdenar, ini, m);
			divideYVencerasREC(arrayAOrdenar, m+1, fin);
			merge(arrayAOrdenar, ini, m, fin);
		}
	}

	//IMPORTANTE: devuelve tiempo ejecución
	private static int ordenaThreads(int[] arrayAOrdenar)
	{
		long ini, fin;
		ini = System.currentTimeMillis(); //Iniciamos temporizador
		
		//Ordenamos por partes.
		OrdenaThread ordIzq = new OrdenaThread(arrayAOrdenar, 0, arrayAOrdenar.length/2-1);
		OrdenaThread ordDer = new OrdenaThread(arrayAOrdenar, arrayAOrdenar.length/2, arrayAOrdenar.length-1);	
		
		ordIzq.start();
		ordDer.start();
		try {
			ordIzq.join();
			//System.out.println(Arrays.toString(arrayAOrdenar));
			ordDer.join();
			//System.out.println(Arrays.toString(arrayAOrdenar));
			merge(arrayAOrdenar, 0, arrayAOrdenar.length/2-1, arrayAOrdenar.length-1);
			//System.out.println(Arrays.toString(arrayAOrdenar));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Terminamos temporizador
		fin = System.currentTimeMillis();
		
		return (int)fin - (int)ini;
	}
	
	/**
	 * Mezcla los intervalos [inf, medio] y [medio+1, sup] del array a de forma ordenada
	 */
	private static void merge(int[] a, int inf, int medio, int sup) 
	{
		int i = inf; int j = medio+1;
		int[] b = new int[sup-inf+1];
		int k = 0;
		while(i <= medio && j <= sup)
		{
			if (a[i] <= a[j])
			{
				b[k] = a[i]; i++;
			} else
			{
				b[k] = a[j]; j++;
			} k++;
		}
		while(i <= medio)
		{
			b[k] = a[i];
			i++; k++;
		}
		while(j <= sup)
		{
			b[k] = a[j];
			j++; k++;
		}
		
		k=0;
		for (int f = inf; f <= sup; f++)
		{
			a[f] = b[k]; k++;
		}
	}

	/**
	 * Intercambia las posiciones i y j del array arr
	 */
	private static void swap(int[] arr, int i, int j) {
		int aux = arr[i];
		arr[i] = arr[j];
		arr[j] = aux;
	}

	/**
	 * Devuelve un array de elementos aleatorios (semilla = seed) con tamaño tam y 
	 * tam*3 posibles valores diferentes.
	 */
	private static int[] randomArray(int tam, long seed)
	{
		int [] a = new int[tam];
		Random rnd = new Random(seed);
		
		for (int i = 0; i < tam; i++)
		{
			a[i] = rnd.nextInt(tam*3);
		}
		return a;
	}

	/**
	 * Devuelve true si el array a está ordenado, false en caso contrario
	 */
	private static boolean ordenado(int[] a)
	{
		int i = 0;
		boolean ordenado = true;
		while(i < (a.length-1) && ordenado)
		{
			if (a[i] > a[i+1])
			{
				ordenado = false;
			}
			i++;
		}
		return ordenado;
	}
}




