
package com.assignment4.tasks;


public class LamportTimestamp {
    private int timestamp;
    public LamportTimestamp(int time){

        timestamp = time;
    }
    public synchronized void tick(){
        timestamp ++;
    }
    public synchronized int getCurrentTimestamp(){
        return timestamp;
    }
    public synchronized void updateClock(int receivedTimestamp){
        timestamp = Math.max(timestamp, receivedTimestamp) + 1;
    }

}
