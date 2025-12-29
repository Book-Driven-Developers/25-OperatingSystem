package com.example.chapter12.synchronizationTools;

public enum ToolType {
  MUTEX{
    @Override
    public SynchronizationTool create() {
      return new MutexTool();
    }
  },
  SEMAPHORE {
    @Override
    public SynchronizationTool create() {
      return new SemaphoreTool();
    }
  },
  MONITOR {
    @Override
    public SynchronizationTool create() {
      return new MonitorTool();
    }
  };
  public abstract SynchronizationTool create();

  public static ToolType from(String input) {
    return ToolType.valueOf(input.toUpperCase());
  }
}
