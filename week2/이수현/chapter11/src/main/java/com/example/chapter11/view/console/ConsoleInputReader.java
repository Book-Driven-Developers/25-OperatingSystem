package com.example.chapter11.view.console;

import java.util.Scanner;

public class ConsoleInputReader {
  private final Scanner scanner = new Scanner(System.in);
  public String readLine(){
    try {
      if (!scanner.hasNextLine()) return null;
      return scanner.nextLine();
    } catch (Exception e) {
      return null;
    }
  }
}
