package com.example.chapter13.console;

import java.util.Scanner;

public class ConsoleUI {
  private final Scanner sc = new Scanner(System.in);

  public void println(String s){
    System.out.println(s);
  }

  public void line(){
    System.out.println("--------------------------------------------------");
  }

  public int askInt(String prompt, int min, int max){
    while(true){
      System.out.print(prompt + " ");
      String in = sc.nextLine().trim();
      try{
        int v = Integer.parseInt(in);
        if( v< min || v > max) throw new IllegalArgumentException();
        return v;
      }catch (Exception e){
        System.out.println("입력이 올바르지 않습니다. (" + min + " ~ " + max + ")");
      }
    }
  }

  public String ask(String prompt){
    System.out.print(prompt + " ");
    return sc.nextLine();
  }
}
