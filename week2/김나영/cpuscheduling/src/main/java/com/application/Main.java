package com.application;

import com.application.controller.ScheduleController;

public class Main {
    public static void main(String[] args) {
        ScheduleController controller = new ScheduleController(4);

        controller.addProcess(1, 10, 0);
        controller.addProcess(2, 5, 1);
        controller.addProcess(3, 8, 2);
        controller.addProcess(4, 6, 3);

        controller.run();
    }
}