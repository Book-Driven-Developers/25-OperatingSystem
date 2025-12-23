package com.example.chapter12.view;

import org.springframework.stereotype.Component;

@Component
public class OutputView {
  public void showHelp(){
    System.out.println("""
            =========================
            Commands:
            입금 <amount>
            출금 <amount>
            잔액
            도움말
            종료
            =========================
            """
    );
  }

  public void showBalance(long balance){
    System.out.println("[잔액] 현재 잔액 = " + balance);
  }

  public void showSuccess(String message){
    System.out.println("[성공] " + message);
  }

  public void showError(String message){
    System.out.println("[에러] " + message);
  }
}
