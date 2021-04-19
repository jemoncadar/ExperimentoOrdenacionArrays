

public class OrdenaThread extends Thread{

	private int[] arr;
	int ini, fin; //ordenaremos [ini, fin]
	
	public OrdenaThread(int[] arr, int ini, int fin)
	{
		this.arr = arr;
		this.ini = ini;
		this.fin = fin;
	}
	
	@Override
	public void run()
	{
		//System.out.println("running");
		int i = ini;
		int j = ini+1;
		while(i < fin)
		{
			if (arr[j] < arr[i])
			{
				swap(i, j);
			}
			j++;
			if (j > fin)
			{
				i++;
				j = i+1;
			}
		}
	}
	
	private void swap(int i, int j)
	{
		int aux = arr[i];
		arr[i] = arr[j];
		arr[j] = aux;
	}
}
