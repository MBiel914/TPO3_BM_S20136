package Zad1;

import java.io.IOException;

public class Test {
	public static void main(String[] args) throws IOException, InterruptedException {
		String cmd = "AddTopic:Ala";
		System.out.println(cmd.contains(new StringBuffer("AddTopic")));
	}
}
