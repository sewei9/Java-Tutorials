class apples{
	public static void main (String args[]){
		int boy, girl;
		boy = 18;
		girl = 68;

		if(boy > 10 && girl < 60){ // AND
			// if(boy > 10 || girl < 60) OR
			System.out.println("You can enter");
		}else{
			System.out.println("You are too young");
		}
	}
}