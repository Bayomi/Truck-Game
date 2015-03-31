package student;

import java.util.*; 

import game.Board;
import game.Edge;
import game.Game;
import game.Manager;
import game.Node;
import game.Parcel;
import game.Truck;




public class MyManager extends Manager {

	private ArrayList<Truck> listOfTrucks = new ArrayList<Truck>();
	private ArrayList<Parcel> listOfParcels = new ArrayList <Parcel>(); 
	boolean isRunning = true;
	private HashMap<Truck, ArrayList<Parcel>> truckData = new HashMap<Truck, ArrayList<Parcel>>();
		
			
	/**TO DO LIST:
	 * 1. Check if Dijkstra is working.
	 * 2. Parcels are not being picked up, delivered, game doesn't end and we're fucked.
	 */

	@Override
	public void run() {
		
		/*Choose arbitrary Truck t
		 *Set t to deliver p
		 *Store p in data structure in t's user data
		 */

		/*Get all the trucks*/
		for (game.Truck t: getGame().getBoard().getTrucks()) {
			listOfTrucks.add(t); 
		}

		/*Get all the parcels*/
		for (game.Parcel p: getGame().getBoard().getParcels()) {
			listOfParcels.add(p); 
		}

		/*Assigns each parcel to a truck, until there are either no more trucks
		 * or no more parcels. Changes the status of each truck to waiting.  
		 */ 

		/*If there are more trucks than parcels, fill some trucks with all parcels.*/
		if (listOfTrucks.size() > listOfParcels.size()) {
			for (int k = 0; k < listOfParcels.size(); k ++) {
				setAndPick (listOfTrucks.get(k), listOfParcels.get(k)); 
                truckData.put(listOfTrucks.get(k), null);
			}} else {

				/*There aren't enough trucks to pick up all parcels.*/

				/*Fill all trucks with some parcels.*/

				int divisionParcel = ((listOfParcels.size())/listOfTrucks.size());
				System.out.println(divisionParcel);
				
				
				for(int i = 0; i<listOfTrucks.size(); i++){
					
					ArrayList<Parcel> remainOfParcels = new ArrayList <Parcel>();	
				
					for(int k = 0; k <= divisionParcel && !listOfParcels.isEmpty(); k++){
					
						remainOfParcels.add(listOfParcels.get(0));
						listOfParcels.remove(0);
					}
							
					truckData.put(listOfTrucks.get(i), remainOfParcels);
					
				}
			}

      for(Truck t: listOfTrucks){
    	  System.out.println(truckData.get(t));
      }
		isRunning = false;
	}

	/**Helper method used to set user data, travel path and pick up load.*/
	public void setAndPick (Truck t, Parcel p) {

		t.setUserData(p); 
		t.setTravelPath(findShortestPath(t.getLocation(), p.start)); 
		if (t.getLocation() == p.start)
			t.pickupLoad (p); 	
	}


	@Override
	public void truckNotification(Truck t, Notification message) {

		

		if (message.equals(Manager.Notification.WAITING)) { 

			/*Basic preprocessing is not done. Return.*/
			if (isRunning){
				return; 
			}

			/*While there are parcels to be delivered*/
			if (!getParcels().isEmpty()) {
				/*If truck in not empty*/
				if (t.getLoad() != null ) {
					
					/*If truck reached load`s destination*/
					if (t.getLocation().equals(t.getLoad().destination)) {
						if(truckData.get(t) != null){
						truckData.get(t).remove(t.getLoad());
						t.dropoffLoad(); 
						} else{
						t.dropoffLoad();
						t.setTravelPath (findShortestPath (t.getLocation(), getGame().getBoard().getTruckDepot()));	
					}
						
					
				} else 
						
						t.setTravelPath(findShortestPath(t.getLocation(), t.getLoad().destination));
					

				} else {
					
										
					if(truckData.get(t) != null || t.getUserData() == null){
	
						if(!truckData.get(t).isEmpty()) {
					
							boolean newOrOld = false;
							
							for (Parcel p: t.getLocation().getParcels()) {
								if(truckData.get(t).contains(p)){
									
									t.pickupLoad(p);
									newOrOld = true;
									break;
							}
						}
						
						if(!newOrOld){
					
							
							Parcel p = truckData.get(t).get(0);
							t.setTravelPath(findShortestPath(t.getLocation(), p.start)); 
							if (t.getLocation() == p.start)
								t.pickupLoad (p); 	
						
						}
					
						} else	
								
							t.setTravelPath (findShortestPath (t.getLocation(), getGame().getBoard().getTruckDepot()));
					
						} else{
					
						if(t.getLocation().getParcels().contains(t.getUserData())){
							for(Parcel pr : t.getLocation().getParcels()){
								if(pr.equals(t.getUserData())){
									t.pickupLoad(pr);
								}
							}
						}
					}
				}

				} 

				else
					/*No more parcels to pick up. Send the truck to the home depot.*/
					t.setTravelPath (findShortestPath (t.getLocation(), getGame().getBoard().getTruckDepot())); 

				/*When all trucks are back at home depot, finish game.*/
				if (getGame().getBoard().allTrucksAreAtDepot())
					getGame().isFinished(); 
				
			}	
	

}

		/**Find the shortest path to destination node.*/
		public List<Node> findShortestPath(Node source, Node target){


			/*Every node has infinite distance*/

			HashMap<Node, NodeInfo> nodeMap= new HashMap<Node,NodeInfo>();
			HashSet<Node> nodes = this.getBoard().getNodes();
			GriesHeap<Node> vertexQueue = new GriesHeap<Node>();

			for(Node n: nodes) {

				NodeInfo nf = new NodeInfo(Double.POSITIVE_INFINITY, null);
				nodeMap.put(n, nf);
				vertexQueue.add(n, Double.POSITIVE_INFINITY);
			}

			nodeMap.get(source).setPriority(0);
			vertexQueue.updatePriority(source, 0);

			while (!vertexQueue.isEmpty()) {
				Node u = vertexQueue.poll();

				/*Visit every edge exiting u.*/
				for (Edge e : u.getExits())
				{
					Node v = e.getOther(u);

					nodeMap.get(u).getPriority();

					double distanceThroughU = nodeMap.get(u).getPriority() + e.length;

					if (distanceThroughU < nodeMap.get(v).getPriority()) 
					{

						nodeMap.get(v).setPriority(distanceThroughU);
						nodeMap.get(v).setBackpointer(u);
						vertexQueue.updatePriority(v, distanceThroughU);
					}
				}
			}

			/*Return the path.*/
			List<Node> path = new ArrayList<Node>();

			for (Node vertex = target; vertex != null; vertex = nodeMap.get(vertex).getBackpointer())
				path.add(vertex);
			Collections.reverse(path);

			return path;

		}


		private static class NodeInfo {

			private double priority;  
			private Node backpointer;        

			/** Constructor: an instance with priority p at index i. */
			private NodeInfo(double p, Node i) {
				priority= p;
				backpointer= i;
			}

			public double getPriority(){
				return this.priority;
			}

			public Node getBackpointer(){
				return this.backpointer;
			}

			public void setPriority(double prior){
				this.priority = prior;
			}

			public void setBackpointer(Node back){
				this.backpointer = back;
			}
		}

	}